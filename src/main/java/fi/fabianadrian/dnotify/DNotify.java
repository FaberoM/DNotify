package fi.fabianadrian.dnotify;

import co.aikar.commands.PaperCommandManager;
import fi.fabianadrian.dnotify.Commands.CmdDNotify;
import fi.fabianadrian.dnotify.Files.Logger;
import fi.fabianadrian.dnotify.Files.PlayerData;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class DNotify extends JavaPlugin {

    private static DNotify plugin;
    private static PaperCommandManager commandManager;

    public static String translate(final String string) {

        return string == null ? "" : ChatColor.translateAlternateColorCodes('&', string);
    }

    public static DNotify getPlugin() {
        return plugin;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    private void registerCommands() {
        commandManager = new PaperCommandManager(this);

        commandManager.registerCommand(new CmdDNotify());
    }

    public void onEnable() {
        plugin = this;

        saveDefaultConfig();

        PlayerData.setup();
        Logger.setup();

        registerCommands();

        getServer().getPluginManager().registerEvents(new PlayerEvent(), this);

        if (getConfig().getBoolean("metrics")) new Metrics(this, 7160);
    }

    public void onDisable() {
        Logger.onDisable();
    }
}
