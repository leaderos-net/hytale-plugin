package net.leaderos.hytale.plugin.modules.verify.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.shared.helpers.RequestUtil;
import net.leaderos.hytale.shared.model.Response;
import net.leaderos.hytale.shared.model.request.impl.verify.VerifyRequest;

import javax.annotation.Nonnull;
import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * Verify command
 * @author leaderos
 * @since 1.0
 */
public class VerifyCommand extends AbstractAsyncCommand {
    private final RequiredArg<String> codeArg;

    public VerifyCommand() {
        super("verify", "Verifies Hytale account in game.");

        addAliases("dogrula");

        // Register required arguments (order matters for positional args)
        codeArg = withRequiredArg("code", "Verify Code", ArgTypes.STRING);
    }

    /**
     * Executes command method
     */
    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        String code = context.get(codeArg);
        CommandSender sender = context.sender();
        CommandUtil.requirePermission(sender, HytalePermissions.fromCommand("leaderos.verify"));

        if (!(sender instanceof Player player)) {
            return CompletableFuture.completedFuture(null);
        }

        if (!RequestUtil.canRequest(player.getUuid())) {
            ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getHaveRequestOngoing());
            return CompletableFuture.completedFuture(null);
        }

        return runAsync(context, () -> {
            RequestUtil.addRequest(player.getUuid());

            try {
                String username = player.getDisplayName();
                String uuid = player.getUuid().toString();
                Response verifyRequest = new VerifyRequest(code, username, uuid).getResponse();
                if (verifyRequest.getResponseCode() == HttpURLConnection.HTTP_OK && verifyRequest.getResponseMessage().getBoolean("status")) {
                    ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getVerify().getSuccessMessage());
                } else {
                    ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getVerify().getFailMessage());
                }
            } catch (Exception e) {
                ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getVerify().getFailMessage());
            }

            RequestUtil.invalidate(player.getUuid());
        }, ForkJoinPool.commonPool());
    }
}
