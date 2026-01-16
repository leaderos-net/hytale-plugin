package net.leaderos.hytale.plugin.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.shared.Shared;
import net.leaderos.hytale.shared.helpers.UrlUtil;

import javax.annotation.Nonnull;

public class LeaderosReloadSubCommand extends CommandBase {
    public LeaderosReloadSubCommand() {
        super("reload", "Reloads plugin configurations.");
    }

    protected void executeSync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();
        CommandUtil.requirePermission(sender, HytalePermissions.fromCommand("leaderos.reload"));

        LeaderosPlugin.getInstance().getConfigFile().load(true);
        LeaderosPlugin.getInstance().getLangFile().load(true);
        LeaderosPlugin.getInstance().getModulesFile().load(true);

        Shared.setLink(UrlUtil.format(LeaderosPlugin.getInstance().getConfigFile().getSettings().getUrl()));
        Shared.setApiKey(LeaderosPlugin.getInstance().getConfigFile().getSettings().getApiKey());

        LeaderosPlugin.getInstance().getModuleManager().reloadModules();
        ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getReload());
    }

}
