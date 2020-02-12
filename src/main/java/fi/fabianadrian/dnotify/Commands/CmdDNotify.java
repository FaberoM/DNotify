package fi.fabianadrian.dnotify.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import co.aikar.locales.MessageKey;
import fi.fabianadrian.dnotify.DNotify;
import fi.fabianadrian.dnotify.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@CommandAlias("dnotify")
@CommandPermission("dnotify.dnotify")
@Description("Base command for dnotify.")
public class CmdDNotify extends BaseCommand {

    private static Plugin plugin = DNotify.getPlugin();
    private static PaperCommandManager manager = DNotify.getCommandManager();

    @Default
    public void onDefault(CommandSender sender) {
        sender.sendMessage(DNotify.translate("&b&lD&6&LNotify &f") + plugin.getDescription().getVersion());
    }

    @Subcommand("toggle")
    public void onToggle(Player player) {
        CommandIssuer issuer = manager.getCommandIssuer(player);

        boolean notifStatus = !PlayerData.getData(player).getBoolean("disable-notifications");
        PlayerData.setValue(player, "disable-notifications", notifStatus);

        if (notifStatus) {
            issuer.sendInfo(MessageKey.of("notificationsEnabled"));
        } else {
            issuer.sendInfo(MessageKey.of("notificationsDisabled"));
        }
    }
}
