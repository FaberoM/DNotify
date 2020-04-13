package fi.fabianadrian.dnotify.Files;

import fi.fabianadrian.dnotify.DNotify;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerData {

    private static File playerDataFolder;

    public static void setup() {
        //Create a folder for playerdata if it doesn't exist.
        playerDataFolder = new File(DNotify.getPlugin().getDataFolder(), "Data/PlayerData");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    public static FileConfiguration get(Player player) {
        return get(player.getUniqueId());
    }

    public static FileConfiguration get(UUID uuid) {
        return YamlConfiguration.loadConfiguration(dataFile(uuid));
    }

    public static void set(Player player, String key, Object value) {
        set(player.getUniqueId(), key, value);
    }

    public static void set(UUID uuid, String key, Object value) {
        FileConfiguration file = YamlConfiguration.loadConfiguration(dataFile(uuid));
        file.set(key, value);
        try {
            file.save(dataFile(uuid));
        } catch (IOException e) {
            Bukkit.getLogger().severe("Couldn't create PlayerData.yml for player " + uuid);
        }
    }

    private static File dataFile(UUID uuid) {
        File file = new File(playerDataFolder, uuid + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Couldn't save playerdata for " + uuid);
            }
        }

        return file;
    }
}
