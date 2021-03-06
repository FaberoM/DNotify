package fi.fabianadrian.dnotify.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.locales.MessageKey;
import fi.fabianadrian.dnotify.DNotify;
import fi.fabianadrian.dnotify.Files.Logger;
import fi.fabianadrian.dnotify.Files.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@CommandAlias("dnotify")
@CommandPermission("dnotify.dnotify")
@Description("Base command for dnotify.")
public class CmdDNotify extends BaseCommand {

    private static final Plugin plugin = DNotify.getPlugin();
    private static final PaperCommandManager manager = DNotify.getCommandManager();

    @Subcommand("version")
    public void onVersion(CommandSender sender) {
        sender.sendMessage(DNotify.translate("&f&l[&9&lDnotify&f&l] &dVersion " + plugin.getDescription().getVersion() + "\n&6Author:&7 Fabian Adrian"));
    }

    @Subcommand("toggle")
    @CommandPermission("dnotify.toggle")
    public void onToggle(Player player) {
        CommandIssuer issuer = manager.getCommandIssuer(player);

        boolean disableNotif = !PlayerData.get(player).getBoolean("disable-notifications");

        PlayerData.set(player, "disable-notifications", disableNotif);

        if (disableNotif) {
            issuer.sendInfo(MessageKey.of("notificationsDisabled"));
        } else {
            issuer.sendInfo(MessageKey.of("notificationsEnabled"));
        }
    }

    @Subcommand("purgelogs")
    @CommandPermission("dnotify.purge")
    public void onPurge(CommandIssuer issuer) {
        Logger.purge();
        issuer.sendInfo(MessageKey.of("purgingLogs"));
    }

    @Subcommand("reload")
    @CommandPermission("dnotify.reload")
    public void onReload(CommandIssuer issuer) {
        DNotify.getPlugin().reloadConfig();
        issuer.sendInfo(MessageKey.of("pluginReloaded"));
    }
}
