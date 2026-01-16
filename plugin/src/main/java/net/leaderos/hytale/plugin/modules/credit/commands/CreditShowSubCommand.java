package net.leaderos.hytale.plugin.modules.credit.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.shared.helpers.MoneyUtil;
import net.leaderos.hytale.shared.helpers.Placeholder;
import net.leaderos.hytale.shared.helpers.RequestUtil;
import net.leaderos.hytale.shared.model.Response;
import net.leaderos.hytale.shared.modules.credit.CreditHelper;

import javax.annotation.Nonnull;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class CreditShowSubCommand extends AbstractAsyncCommand {
    private final RequiredArg<String> playerArg;

    public CreditShowSubCommand() {
        super("show", "Shows the credit amount of a player.");

        addAliases("goster", "göster", "gor", "gör", "bak");

        playerArg = withRequiredArg("player", "Target player", ArgTypes.STRING);
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
        if (!sender.hasPermission("leaderos.credit.see.other")) {
            ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getCommand().getNoPerm());
            return CompletableFuture.completedFuture(null);
        }

        String target = context.get(playerArg);

        if (sender instanceof Player && !RequestUtil.canRequest(sender.getUuid())) {
            ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getHaveRequestOngoing());
            return CompletableFuture.completedFuture(null);
        }

        return runAsync(context, () -> {
            if (sender instanceof Player) {
                RequestUtil.addRequest(sender.getUuid());
            }

            Response targetCredits = CreditHelper.getRequest(target);
            if (Objects.requireNonNull(targetCredits).getResponseCode() == HttpURLConnection.HTTP_OK) {
                ChatUtil.sendMessage(sender, ChatUtil.replacePlaceholders(
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getCreditInfoOther(),
                        new Placeholder("{amount}", MoneyUtil.format(targetCredits.getResponseMessage().getDouble("raw_credits"))),
                        new Placeholder("{target}", target)
                ));
            }
            else
                ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getPlayerNotAvailable());

            if (sender instanceof Player) {
                RequestUtil.invalidate(sender.getUuid());
            }
        }, ForkJoinPool.commonPool());
    }

}
