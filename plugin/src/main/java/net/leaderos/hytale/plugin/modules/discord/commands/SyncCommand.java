package net.leaderos.hytale.plugin.modules.discord.commands;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.shared.Shared;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * Discord sync command
 * @author leaderos
 * @since 1.0
 */
public class SyncCommand extends AbstractAsyncCommand {
    public SyncCommand() {
        super("discord-sync", "Generates a discord account sync link.");

        addAliases("discord-link");
    }

    /**
     * Executes command method
     */
    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();
        CommandUtil.requirePermission(sender, HytalePermissions.fromCommand("leaderos.discord.sync"));

        if (!(sender instanceof Player player)) {
            ChatUtil.sendMessage(sender, "This command can only be used by players.");
            return CompletableFuture.completedFuture(null);
        }

        return runAsync(context, () -> {
            try {
                String link = Shared.getLink() + "/discord/link";
                ChatUtil.sendMessage(player,
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getDiscord().getCommandMessage()
                                .replace("{prefix}", LeaderosPlugin.getInstance().getLangFile().getMessages().getPrefix())
                                .replace("{link}", link)
                );
            } catch (Exception ignored) {
                ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getDiscord().getNoLink());
            }
        }, ForkJoinPool.commonPool());
    }
}
