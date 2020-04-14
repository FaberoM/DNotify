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

    private static final File dataFolder = new File(DNotify.getPlugin().getDataFolder(), "PlayerData");

    public static void setup() {
        //Create a folder for playerdata if it doesn't exist.
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
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
            Bukkit.getLogger().severe("Couldn't save playerdata for player " + uuid);
        }
    }

    private static File dataFile(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Couldn't create playerdata for " + uuid);
            }
        }

        return file;
    }
}
