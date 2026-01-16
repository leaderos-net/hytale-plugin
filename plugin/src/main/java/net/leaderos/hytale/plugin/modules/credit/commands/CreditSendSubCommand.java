package net.leaderos.hytale.plugin.modules.credit.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.plugin.helpers.PlayerUtil;
import net.leaderos.hytale.shared.helpers.MoneyUtil;
import net.leaderos.hytale.shared.helpers.Placeholder;
import net.leaderos.hytale.shared.helpers.RequestUtil;
import net.leaderos.hytale.shared.model.Response;
import net.leaderos.hytale.shared.modules.credit.CreditHelper;
import net.leaderos.hytale.shared.error.Error;

import javax.annotation.Nonnull;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class CreditSendSubCommand extends AbstractAsyncCommand {
    private final RequiredArg<String> playerArg;
    private final RequiredArg<Double> amountArg;

    public CreditSendSubCommand() {
        super("send", "Sends credits to a player.");

        addAliases("gonder", "gönder");

        playerArg = withRequiredArg("player", "Target player", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount of credits to send", ArgTypes.DOUBLE);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();
        CommandUtil.requirePermission(sender, HytalePermissions.fromCommand("leaderos.credit.send"));

        String target = context.get(playerArg);
        Double amount = context.get(amountArg);

        if (!(sender instanceof Player player))
            return CompletableFuture.completedFuture(null);

        if (player.getDisplayName().equalsIgnoreCase(target)) {
            ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getCannotSendCreditYourself());
            return CompletableFuture.completedFuture(null);
        }

        if (amount <= 0) {
            ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getCannotSendCreditNegative());
            return CompletableFuture.completedFuture(null);
        }

        if (!RequestUtil.canRequest(player.getUuid())) {
            ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getHaveRequestOngoing());
            return CompletableFuture.completedFuture(null);
        }

        return runAsync(context, () -> {
            RequestUtil.addRequest(player.getUuid());

            Response sendCreditResponse = CreditHelper.sendCreditRequest(player.getDisplayName(), target, amount);

            if (Objects.requireNonNull(sendCreditResponse).getResponseCode() == HttpURLConnection.HTTP_OK
                    && sendCreditResponse.getResponseMessage().getBoolean("status")) {
                ChatUtil.sendMessage(player, ChatUtil.replacePlaceholders(
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getSuccessfullySentCredit(),
                        new Placeholder("{amount}", MoneyUtil.format(amount)),
                        new Placeholder("{target}", target)
                ));

                PlayerRef targetPlayer = PlayerUtil.findPlayerByName(target);
                if (targetPlayer != null && targetPlayer.isValid())
                    ChatUtil.sendMessage(player,
                            ChatUtil.replacePlaceholders(LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getReceivedCredit(),
                                    new Placeholder("{amount}", MoneyUtil.format(amount)),
                                    new Placeholder("{player}", player.getDisplayName())
                            ));


            } else {
                if (sendCreditResponse.getError() == Error.NOT_ENOUGH_CREDITS) {
                    ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getCannotSendCreditNotEnough());
                } else if (sendCreditResponse.getError() == Error.INVALID_TARGET
                        || sendCreditResponse.getError() == Error.TARGET_USER_NOT_FOUND
                        || sendCreditResponse.getError() == Error.USER_NOT_FOUND) {
                    ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getCannotSendCreditsThisUser());
                } else if (sendCreditResponse.getError() == Error.INVALID_AMOUNT) {
                    ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getCannotSendCreditNegative());
                }
            }

            RequestUtil.invalidate(player.getUuid());
        }, ForkJoinPool.commonPool());
    }

}
