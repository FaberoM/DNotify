package fi.fabianadrian.dnotify;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

    private Map<UUID, Location> lastFindLocation = new HashMap<>();
    private Map<UUID, HashMap<String, Object>> findHistory = new HashMap<>();

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

                long systemTime = System.currentTimeMillis();

                //Calculate find rate
                HashMap<String, Object> tempMap;
                if (!findHistory.containsKey(player.getUniqueId())) {
                    tempMap = new HashMap<>();
                } else {
                    tempMap = findHistory.get(player.getUniqueId());
                }

                tempMap.put(String.valueOf(systemTime), diamondCount);

                int foundPerQuarterHour = 0;

                for (String key : tempMap.keySet()) {
                    if (Long.parseLong(key) < (systemTime - TimeUnit.MINUTES.toMillis(15))) {
                        tempMap.remove(key);
                    } else {
                        foundPerQuarterHour += (int) tempMap.get(key);
                    }
                }

                double foundPerMinute = foundPerQuarterHour / 15.0;

                DecimalFormat decimalFormat = new DecimalFormat("0.00");

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission("dnotify.notify") && !PlayerData.getData(player).getBoolean("disable-notifications")) {
                        //TODO - Convert this to use acf's language API?

                        //TODO - Add click to teleport option.
                        onlinePlayer.sendMessage(DNotify.translate("&9" + player.getName() + "&f found " + diamondCount + " diamonds &7(" + decimalFormat.format(foundPerMinute) + "/min)"));
                    }
                }

                lastFindLocation.put(player.getUniqueId(), block.getLocation());
                findHistory.put(player.getUniqueId(), tempMap);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (lastFindLocation.containsKey(player.getUniqueId())) {
            PlayerData.setValue(player, "find-location", lastFindLocation.get(player.getUniqueId()));
        }
        if (findHistory.containsKey(player.getUniqueId())) {
            PlayerData.setValue(player, "find-history", findHistory.get(player.getUniqueId()));
        }

        lastFindLocation.remove(player.getUniqueId());
        findHistory.remove(player.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Location location = (Location) PlayerData.getData(player).get("find-location");
        if (location != null) {
            lastFindLocation.put(player.getUniqueId(), location);
        }

        ConfigurationSection historySection = PlayerData.getData(player).getConfigurationSection("find-history");
        if (historySection != null) {
            findHistory.put(player.getUniqueId(), (HashMap<String, Object>) historySection.getValues(false));
        }
    }
}
