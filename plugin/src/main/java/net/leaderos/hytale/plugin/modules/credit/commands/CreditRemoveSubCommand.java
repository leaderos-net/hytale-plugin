package net.leaderos.hytale.plugin.modules.credit.commands;

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

public class CreditRemoveSubCommand extends AbstractAsyncCommand {
    private final RequiredArg<String> playerArg;
    private final RequiredArg<Double> amountArg;

    public CreditRemoveSubCommand() {
        super("remove", "Removes credits from a player.");

        addAliases("sil");

        playerArg = withRequiredArg("player", "Target player", ArgTypes.STRING);
        amountArg = withRequiredArg("amount", "Amount of credits to remove", ArgTypes.DOUBLE);
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();
        CommandUtil.requirePermission(sender, HytalePermissions.fromCommand("leaderos.credit.remove"));

        String target = context.get(playerArg);
        Double amount = context.get(amountArg);

        if (amount <= 0) {
            ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getCannotSendCreditNegative());
            return CompletableFuture.completedFuture(null);
        }

        if (sender instanceof Player && !RequestUtil.canRequest(sender.getUuid())) {
            ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getHaveRequestOngoing());
            return CompletableFuture.completedFuture(null);
        }

        return runAsync(context, () -> {
            if (sender instanceof Player) {
                RequestUtil.addRequest(sender.getUuid());
            }

            Response removeCreditResponse = CreditHelper.removeCreditRequest(target, amount);
            if (Objects.requireNonNull(removeCreditResponse).getResponseCode() == HttpURLConnection.HTTP_OK) {
                ChatUtil.sendMessage(sender, ChatUtil.replacePlaceholders(
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getSuccessfullyRemovedCredit(),
                        new Placeholder("{amount}", MoneyUtil.format(amount)),
                        new Placeholder("{target}", target)
                ));
            }
            else
                ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getTargetPlayerNotAvailable());

            if (sender instanceof Player) {
                RequestUtil.invalidate(sender.getUuid());
            }
        }, ForkJoinPool.commonPool());
    }

}
