package fi.fabianadrian.dnotify.Files;

import fi.fabianadrian.dnotify.DNotify;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPOutputStream;

public class Logger {

    private static final File folder = new File(DNotify.getPlugin().getDataFolder(), "Logs");
    private static final File file = new File(folder, "log-" + ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".txt");
    private static final Plugin plugin = DNotify.getPlugin();
    private static final String PREFIX = "[Dnotify] ";
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();

    private static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static String getExtensionOfFile(File file) {
        String fileExtension = "";
        // Get file Name first
        String fileName = file.getName();

        // If fileName do not contain "." or starts with "." then it is not a valid file
        if (fileName.contains(".") && fileName.lastIndexOf(".") != 0) {
            fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        }

        return fileExtension;
    }

    private static void compress(File input, File output) throws IOException {
        try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(output))) {
            try (FileInputStream in = new FileInputStream(input)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        }
    }

    public static void setup() {

        scheduler.runTaskAsynchronously(plugin, () -> {
            //Compress any old logs that have not been compressed.
            File[] logFiles = folder.listFiles();
            try {
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        if (logFile.isFile() && getExtensionOfFile(logFile).equalsIgnoreCase("txt")) {
                            compress(logFile, new File(folder, logFile.getName() + ".gz"));
                            logFile.delete();
                        }
                    }
                }
            } catch (IOException e) {
                severe("Encountered an IOException while compressing old logs!");
            }

            if (DNotify.getPlugin().getConfig().getBoolean("logger")) {
                try {
                    if (!folder.exists()) folder.mkdirs();
                    file.createNewFile();
                } catch (IOException e) {
                    severe("Encountered an IOException while creating logfile!");
                }
            }
        });
    }

    public static void write(double findRate, String name, int count, int x, int y, int z) {

        scheduler.runTaskAsynchronously(plugin, () -> {
            String coordinates = "x" + x + ", y" + y + ", z" + z;

            try {
                Writer output;
                output = new BufferedWriter(new FileWriter(file, true));
                output.append(String.valueOf(round(findRate))).append("/min | ").append(name).append(" | amount: ")
                        .append(String.valueOf(count)).append(" | (").append(coordinates).append(") | ")
                        .append(ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .append(System.lineSeparator()).close();
            } catch (IOException e) {
                severe("Couldn't write a line to log.txt!");
            }
        });
    }

    public static void purge() {

        scheduler.runTaskAsynchronously(plugin, () -> {
            File[] logFiles = folder.listFiles();

            if (logFiles != null) {
                for (File logFile : logFiles) {
                    if (logFile.isFile() && getExtensionOfFile(logFile).equalsIgnoreCase("gz")) {
                        logFile.delete();
                    }
                }
            }
        });
    }

    public static void info(String message) {
        Bukkit.getLogger().info(PREFIX + message);
    }

    public static void severe(String message) {
        Bukkit.getLogger().severe(PREFIX + message);
    }

    public static void onDisable() {

        if (!file.exists()) {
            return;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            if (br.readLine() == null) {
                br.close();
                if (file.delete()) {
                    info("Removing empty logfile");
                }
            }
        } catch (IOException e) {
            severe("Could not read latest log file!");
        }
    }
}
