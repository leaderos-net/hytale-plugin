package net.leaderos.hytale.plugin.modules.ai.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.shared.helpers.RequestUtil;

import javax.annotation.Nonnull;

public class AiCommand extends AbstractPlayerCommand {

    public AiCommand() {
        super("ai", "Ask the AI a question.");

        addAliases("yapayzeka");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        CommandUtil.requirePermission(player, HytalePermissions.fromCommand("leaderos.ai"));

        if (!RequestUtil.canRequest(player.getUuid())) {
            ChatUtil.sendMessage(player, LeaderosPlugin.getInstance().getLangFile().getMessages().getHaveRequestOngoing());
            return;
        }

        AiPage page = new AiPage(playerRef);
        player.getPageManager().openCustomPage(ref, store, page);
    }
}