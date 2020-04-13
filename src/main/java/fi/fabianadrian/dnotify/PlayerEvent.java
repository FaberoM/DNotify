package fi.fabianadrian.dnotify;

import fi.fabianadrian.dnotify.Files.Log;
import fi.fabianadrian.dnotify.Files.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerEvent implements Listener {

    private final Map<UUID, Location> lastFindLocation = new HashMap<>();
    private final Map<UUID, HashMap<String, Object>> findHistory = new HashMap<>();
    private final Configuration config = DNotify.getPlugin().getConfig();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {

        if (event.getBlock().getType() == Material.DIAMOND_ORE && event.getPlayer().getGameMode() == GameMode.SURVIVAL && event.getBlock().getLocation().getBlockY() < 17) {

            Block block = event.getBlock();
            Player player = event.getPlayer();

            int searchRadius = 3;
            if (!lastFindLocation.containsKey(player.getUniqueId()) || block.getLocation().distance(lastFindLocation.get(player.getUniqueId())) > searchRadius) {

                //Count how many diamonds there are around.
                int diamondCount = 1;
                for (int x = -searchRadius; x <= searchRadius; x++) {
                    for (int y = -searchRadius; y <= searchRadius; y++) {
                        for (int z = -searchRadius; z <= searchRadius; z++) {
                            if (block.getRelative(x, y, z).getType() == Material.DIAMOND_ORE && (x != 0 || y != 0 || z != 0)) {
                                diamondCount++;
                            }
                        }
                    }
                }

                //Calculate find rate
                HashMap<String, Object> tempFindMap;
                if (!findHistory.containsKey(player.getUniqueId())) {
                    tempFindMap = new HashMap<>();
                } else {
                    tempFindMap = findHistory.get(player.getUniqueId());
                }

                long systemTime = System.currentTimeMillis();
                tempFindMap.put(String.valueOf(systemTime), diamondCount);

                tempFindMap.keySet().removeIf(e -> Long.parseLong(e) < (systemTime - TimeUnit.MINUTES.toMillis(10)));

                int totalFindAmount = tempFindMap.keySet().size();
                findHistory.put(player.getUniqueId(), tempFindMap);

                double foundRate = totalFindAmount / 10.0;

                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                ComponentBuilder messageComponent = new ComponentBuilder(player.getName());

                double suspiciousThreshold = config.getDouble("suspicious-threshold");
                if (foundRate < suspiciousThreshold) {
                    messageComponent.color(ChatColor.BLUE)
                            .append(" found " + diamondCount + " diamonds ").color(ChatColor.WHITE);

                    if (config.getBoolean("logger")) {
                        Log.write(player.getName() + " found " + diamondCount + " diamonds at (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")");
                    }
                } else {
                    messageComponent.color(ChatColor.GOLD)
                            .append(" is finding diamonds at a suspicious rate! ").color(ChatColor.YELLOW);

                    if (config.getBoolean("logger")) {
                        Log.write(player.getName() + "is finding diamonds at a suspicious rate!");
                    }
                }

                messageComponent.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("At " + block.getX() + " " + block.getY() + " " + block.getZ())));

                TextComponent findRateComponent = new TextComponent("(" + decimalFormat.format(foundRate) + "/min)");
                findRateComponent.setColor(ChatColor.GRAY);

                //Notify players that have permission and has notifications on
                TextComponent finalMessage = new TextComponent(messageComponent.create());
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission("dnotify.notify") && !PlayerData.get(onlinePlayer).getBoolean("disable-notifications")) {
                        onlinePlayer.spigot().sendMessage(finalMessage, findRateComponent);
                    }
                }

                //Execute a command if specified in config.
                String command = config.getString("suspicious-command");
                if (command != null && !command.equalsIgnoreCase("")) {
                    command = command.replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }

            lastFindLocation.put(player.getUniqueId(), block.getLocation());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (lastFindLocation.containsKey(uuid)) {
            PlayerData.set(uuid, "find-location", lastFindLocation.get(uuid));
        }
        if (findHistory.containsKey(uuid)) {
            PlayerData.set(uuid, "find-history", findHistory.get(uuid));
        }

        lastFindLocation.remove(uuid);
        findHistory.remove(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        Location location = (Location) PlayerData.get(uuid).get("find-location");
        if (location != null) {
            lastFindLocation.put(uuid, location);
        }

        ConfigurationSection historySection = PlayerData.get(uuid).getConfigurationSection("find-history");
        if (historySection != null) {
            findHistory.put(uuid, (HashMap<String, Object>) historySection.getValues(false));
        }
    }
}
