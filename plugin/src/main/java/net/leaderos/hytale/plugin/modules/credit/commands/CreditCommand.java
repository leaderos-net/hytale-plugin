package net.leaderos.hytale.plugin.modules.credit.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
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

/**
 * CreditCommand class for commands
 * @author leaderos
 * @since 1.0
 */
public class CreditCommand extends AbstractAsyncCommand {
    public CreditCommand() {
        super("credits", "Shows your current credit amount.");

        addAliases("credit", "kredi");

        this.addSubCommand(new CreditSendSubCommand());
        this.addSubCommand(new CreditShowSubCommand());
        this.addSubCommand(new CreditAddSubCommand());
        this.addSubCommand(new CreditRemoveSubCommand());
        this.addSubCommand(new CreditSetSubCommand());
    }

    @Override
    @Nonnull
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();
        CommandUtil.requirePermission(sender, HytalePermissions.fromCommand("leaderos.credit.see"));

        if (!(sender instanceof Player player)) {
            return CompletableFuture.completedFuture(null);
        }

        if (!RequestUtil.canRequest(sender.getUuid())) {
            ChatUtil.sendMessage(sender, LeaderosPlugin.getInstance().getLangFile().getMessages().getHaveRequestOngoing());
            return CompletableFuture.completedFuture(null);
        }

        return runAsync(context, () -> {
            RequestUtil.addRequest(sender.getUuid());

            Response targetCredits = CreditHelper.getRequest(player.getDisplayName());
            if (Objects.requireNonNull(targetCredits).getResponseCode() == HttpURLConnection.HTTP_OK) {
                ChatUtil.sendMessage(player,
                        ChatUtil.replacePlaceholders(LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getCreditInfo(),
                                new Placeholder("{amount}", MoneyUtil.format(targetCredits.getResponseMessage().getDouble("raw_credits")))));
            }
            else
                ChatUtil.sendMessage(player,
                        ChatUtil.replacePlaceholders(LeaderosPlugin.getInstance().getLangFile().getMessages().getCredit().getCreditInfo(),
                                new Placeholder("{amount}", MoneyUtil.format(0.00))));

            RequestUtil.invalidate(sender.getUuid());
        }, ForkJoinPool.commonPool());
    }
}
