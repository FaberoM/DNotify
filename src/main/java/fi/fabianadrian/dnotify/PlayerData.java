package fi.fabianadrian.dnotify;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class PlayerData {

    private static File folder;

    public static void setup() {
        folder = new File(DNotify.getPlugin().getDataFolder(), "Data");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private static File loadPlayerFile(Player player) {
        File dataFile = new File(folder, player.getUniqueId() + ".yml");

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Couldn't create PlayerData.yml for player " + player.getName());
            }
        }

        return dataFile;
    }

    public static FileConfiguration getData(Player player) {
        return YamlConfiguration.loadConfiguration(loadPlayerFile(player));
    }

    public static void setValue(Player player, String key, Object value) {
        File dataFile = loadPlayerFile(player);
        FileConfiguration dataConf = YamlConfiguration.loadConfiguration(dataFile);
        dataConf.set(key, value);
        try {
            dataConf.save(loadPlayerFile(player));
        } catch (IOException e) {
            Bukkit.getLogger().severe("Couldn't create PlayerData.yml for player " + player.getName());
        }
    }
}
