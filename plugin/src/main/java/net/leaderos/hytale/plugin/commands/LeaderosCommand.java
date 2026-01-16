package net.leaderos.hytale.plugin.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
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
    protected boolean canGeneratePermission() {
        return false;  // We will handle permissions manually
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        // Permission Check
        if (!sender.hasPermission("leaderos.help")) {
            ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getCommand().getNoPerm());
            return CompletableFuture.completedFuture(null);
        }

        ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getHelp());
        return CompletableFuture.completedFuture(null);
    }
}
