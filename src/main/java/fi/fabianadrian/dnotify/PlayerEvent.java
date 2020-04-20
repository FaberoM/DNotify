package fi.fabianadrian.dnotify;

import fi.fabianadrian.dnotify.Files.Logger;
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

        if (event.getBlock().getType() == Material.DIAMOND_ORE && event.getPlayer().getGameMode() == GameMode.SURVIVAL && event.getBlock().getY() < 17) {

            Block block = event.getBlock();
            Player player = event.getPlayer();

            int SEARCH_RADIUS = 3;
            if (!lastFindLocation.containsKey(player.getUniqueId()) || block.getLocation().distance(lastFindLocation.get(player.getUniqueId())) > SEARCH_RADIUS) {

                //Count how many diamonds there are around.
                int diamondCount = 1;
                for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
                    for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                        for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
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

                double findRate = totalFindAmount / 10.0;

                //Create message component
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                ComponentBuilder message = new ComponentBuilder(player.getName());

                double suspiciousThreshold = config.getDouble("suspicious-threshold");
                if (findRate < suspiciousThreshold) {
                    message.color(ChatColor.BLUE)
                            .append(" found " + diamondCount + " diamonds").color(ChatColor.WHITE);
                } else {
                    message.color(ChatColor.GOLD)
                            .append(" is finding diamonds at a suspicious rate!").color(ChatColor.YELLOW);
                }
                message.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(block.getX() + " " + block.getY() + " " + block.getZ())));
                message.append(" (" + decimalFormat.format(findRate) + "/min)").color(ChatColor.GRAY);

                //Notify players that have permission and has notifications on
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission("dnotify.notify") && !PlayerData.get(onlinePlayer).getBoolean("disable-notifications")) {
                        onlinePlayer.spigot().sendMessage(new TextComponent(message.create()));
                    }
                }

                //Execute a command if specified in config.
                String command = config.getString("suspicious-command");
                if (command != null && !command.equalsIgnoreCase("")) {
                    command = command.replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }

                if (config.getBoolean("logger")) {
                    Logger.write(findRate, player.getName(), diamondCount, block.getX(), block.getY(), block.getZ());
                }
            }

            //Put player and nearby players to findmap
            lastFindLocation.put(player.getUniqueId(), block.getLocation());
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld() == player.getWorld() && p.getLocation().distanceSquared(player.getLocation()) <= 9 && p != player) {
                    lastFindLocation.put(p.getUniqueId(), block.getLocation());
                }
            }
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
