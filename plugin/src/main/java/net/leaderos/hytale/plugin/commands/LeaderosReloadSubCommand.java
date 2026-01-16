package net.leaderos.hytale.plugin.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.shared.Shared;
import net.leaderos.hytale.shared.helpers.UrlUtil;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class LeaderosReloadSubCommand extends AbstractAsyncCommand {
    public LeaderosReloadSubCommand() {
        super("reload", "Reloads plugin configurations.");
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;  // We will handle permissions manually
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        // Permission Check
        if (!sender.hasPermission("leaderos.reload")) {
            ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getCommand().getNoPerm());
            return CompletableFuture.completedFuture(null);
        }

        return runAsync(context, () -> {
            LeaderosPlugin.getInstance().getConfigFile().load(true);
            LeaderosPlugin.getInstance().getLangFile().load(true);
            LeaderosPlugin.getInstance().getModulesFile().load(true);

            Shared.setLink(UrlUtil.format(LeaderosPlugin.getInstance().getConfigFile().getSettings().getUrl()));
            Shared.setApiKey(LeaderosPlugin.getInstance().getConfigFile().getSettings().getApiKey());

            LeaderosPlugin.getInstance().getModuleManager().reloadModules();
            ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getReload());
        }, ForkJoinPool.commonPool());
    }

}
