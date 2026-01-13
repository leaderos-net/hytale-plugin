package net.leaderos.hytale.plugin.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import net.leaderos.hytale.plugin.LeaderosPlugin;

import javax.annotation.Nonnull;

public class LeaderosReloadCommand extends CommandBase {
    private final LeaderosPlugin plugin = LeaderosPlugin.get();

    public LeaderosReloadCommand() {
        super("reload", "Reloads the LeaderOS plugin");
    }

    protected void executeSync(@Nonnull CommandContext ctx) {
        CommandUtil.requirePermission(ctx.sender(), HytalePermissions.fromCommand("leaderos.reload"));

        ctx.sendMessage(Message.raw("LeaderOS plugin reloaded successfully!"));
    }

}
