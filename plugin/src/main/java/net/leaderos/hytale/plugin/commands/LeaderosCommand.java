package net.leaderos.hytale.plugin.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.ChatUtil;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class LeaderosCommand extends AbstractCommand {
    public LeaderosCommand() {
        super("leaderos", "Lists all available LeaderOS commands.");

        this.addSubCommand(new LeaderosReloadSubCommand());
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();
        CommandUtil.requirePermission(sender, HytalePermissions.fromCommand("leaderos.help"));

        ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getHelp());
        return CompletableFuture.completedFuture(null);
    }
}
