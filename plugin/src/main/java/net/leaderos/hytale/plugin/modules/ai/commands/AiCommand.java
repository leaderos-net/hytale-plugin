package net.leaderos.hytale.plugin.modules.ai.commands;

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
import net.leaderos.hytale.shared.model.request.impl.ai.AiRequest;

import javax.annotation.Nonnull;
import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * AI command
 * @author leaderos
 * @since 1.0
 */
public class AiCommand extends AbstractAsyncCommand {
    private final RequiredArg<String> promptArg;

    public AiCommand() {
        super("ai", "Ask the AI a question.");

        addAliases("yapayzeka");

        // Register required arguments (order matters for positional args)
        promptArg = withRequiredArg("prompt", "Prompt", ArgTypes.STRING);
    }

    /**
     * Executes command method
     */
    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        String prompt = context.get(promptArg);
        CommandSender sender = context.sender();
        CommandUtil.requirePermission(sender, HytalePermissions.fromCommand("leaderos.ai"));

        if (!(sender instanceof Player player)) {
            ChatUtil.sendMessage(sender, "This command can only be used by players.");
            return CompletableFuture.completedFuture(null);
        }

        if (!RequestUtil.canRequest(player.getUuid())) {
            ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getHaveRequestOngoing());
            return CompletableFuture.completedFuture(null);
        }

        return runAsync(context, () -> {
            RequestUtil.addRequest(player.getUuid());
            ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getAi().getGenerating());
            try {
                // Get player locale
                String locale = LeaderosPlugin.getInstance().getConfigFile().getSettings().getLang();

                Response aiRequest = new AiRequest(prompt, locale).getResponse();
                if (aiRequest.getResponseCode() == HttpURLConnection.HTTP_OK && aiRequest.getResponseMessage().getBoolean("status")) {
                    String message = aiRequest.getResponseMessage().getJSONObject("data").getString("message");
                    ChatUtil.sendMessage(player,
                            LeaderosPlugin.getInstance().getLangFile().getMessages().getAi().getAiMessage()
                                    .replace("{message}", message)
                    );
                } else {
                    ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getAi().getFailMessage());
                }
            } catch (Exception e) {
                ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getAi().getFailMessage());
            }
            RequestUtil.invalidate(player.getUuid());
        }, ForkJoinPool.commonPool());
    }
} 