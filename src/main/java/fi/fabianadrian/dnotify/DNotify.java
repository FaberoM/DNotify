package fi.fabianadrian.dnotify;

import co.aikar.commands.PaperCommandManager;
import fi.fabianadrian.dnotify.Commands.CmdDNotify;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class DNotify extends JavaPlugin {

    private static DNotify plugin;
    private static PaperCommandManager commandManager;

    public static String translate(final String string) {

        if (string == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', string);
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

        registerCommands();

        getServer().getPluginManager().registerEvents(new PlayerEvent(), this);

        PlayerData.setup();
    }

    public void onDisable() {
        plugin = null;
        commandManager = null;
    }
}
