package fi.fabianadrian.dnotify.Files;

import fi.fabianadrian.dnotify.DNotify;
import org.bukkit.Bukkit;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private static final File file = new File(DNotify.getPlugin().getDataFolder(), "log.txt");

    public static void setup() {

        try {
            file.createNewFile();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Couldn't create log.txt file!");
        }
    }

    public static void write(String line) {
        try {
            Writer output;
            output = new BufferedWriter(new FileWriter(file, true));
            output.append("[").append(ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)).append("] ");
            output.append(line).append(System.lineSeparator());
            output.close();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Couldn't write a line to log.txt!");
        }
    }
}
