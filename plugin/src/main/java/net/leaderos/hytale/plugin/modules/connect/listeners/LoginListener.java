package net.leaderos.hytale.plugin.modules.connect.listeners;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.plugin.modules.connect.ConnectModule;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginListener {
    public static void onPlayerJoin(PlayerConnectEvent event) {
        PlayerRef player = event.getPlayerRef();

        ConnectModule.getCommandsQueue().getExecutor().execute(() -> {
            try {
                final List<String> commands = ConnectModule.getCommandsQueue().getCommands(player.getUsername());

                if (commands == null || commands.isEmpty()) return;

                HytaleServer.SCHEDULED_EXECUTOR.schedule(
                        () -> {
                            if (!player.isValid()) return;

                            // Execute commands
                            commands.forEach(command -> {
                                HytaleServer.get().getCommandManager().handleCommand(ConsoleSender.INSTANCE, command);
                                ChatUtil.sendConsoleMessage(
                                        LeaderosPlugin.getInstance().getLangFile().getMessages().getConnect().getConnectExecutedCommandFromQueue()
                                                .replace("%command%", command)
                                );
                            });

                            // Remove commands from queue
                            ConnectModule.getCommandsQueue().getExecutor().execute(() -> {
                                ConnectModule.getCommandsQueue().removeCommands(player.getUsername());
                            });
                        },
                        LeaderosPlugin.getInstance().getModulesFile().getConnect().getExecuteDelay(), TimeUnit.SECONDS
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
