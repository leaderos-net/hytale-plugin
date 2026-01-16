package net.leaderos.hytale.plugin.modules.connect;


import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.pusher.client.connection.ConnectionState;
import lombok.Getter;
import lombok.Setter;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.helpers.PlayerUtil;
import net.leaderos.hytale.plugin.modules.connect.timer.FallbackTimer;
import net.leaderos.hytale.plugin.modules.connect.timer.ReconnectionTimer;
import net.leaderos.hytale.shared.modules.LeaderOSModule;
import net.leaderos.hytale.shared.modules.connect.data.CommandsQueue;
import net.leaderos.hytale.shared.modules.connect.socket.SocketClient;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.plugin.modules.connect.listeners.LoginListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Connect module main class
 *
 * @author leaderos
 * @since 1.0
 */
@Getter
public class ConnectModule extends LeaderOSModule {

    /**
     * Socket client for connect to leaderos
     */
    private SocketClient socket;

    /**
     * LoginListener for load cache
     */
    private static LoginListener loginListener;

    /**
     * Commands Queue file
     */
    @Getter
    private static CommandsQueue commandsQueue;

    @Getter
    @Setter
    private static EventRegistration<Void, PlayerConnectEvent> playerJoinEventRegistration;

    /**
     * onEnable method of module
     */
    public void onEnable() {
        // Load queue data
        commandsQueue = new CommandsQueue(LeaderosPlugin.getInstance().getDataDirectory().toString());

        // Register listeners
        if (LeaderosPlugin.getInstance().getModulesFile().getConnect().isOnlyOnline()) {
            playerJoinEventRegistration = LeaderosPlugin.getInstance().getEventRegistry().register(PlayerConnectEvent.class, LoginListener::onPlayerJoin);
        }

        // Socket connection
        socket = new SocketClient(LeaderosPlugin.getInstance().getConfigFile().getSettings().getApiKey(),
                LeaderosPlugin.getInstance().getModulesFile().getConnect().getServerToken()) {
            /**
             * Executes console command
             * @param commands command list to execute
             * @param username username of player
             */
            @Override
            public void executeCommands(List<String> commands, String username) {
                List<String> validatedCommands = new ArrayList<>();

                // Get command blacklist from config
                List<String> commandBlacklist = LeaderosPlugin.getInstance().getModulesFile().getConnect().getCommandBlacklist();

                // Check if commands are in blacklist
                for (String command : commands) {
                    // If command is not empty and starts with a slash, remove the slash
                    if (command.startsWith("/")) {
                        command = command.substring(1);
                    }

                    // Get the root command (the first word before space)
                    String commandRoot = command.split(" ")[0];

                    // Check if the command is blacklisted
                    if (commandBlacklist.contains(commandRoot)) {
                        ChatUtil.sendConsoleMessage(
                                LeaderosPlugin.getInstance().getLangFile().getMessages().getConnect().getCommandBlacklisted()
                                        .replace("%command%", command)
                        );
                    } else {
                        // If command is valid, add to validatedCommands
                        validatedCommands.add(command);
                    }
                }

                // If player is offline and onlyOnline is true
                if (LeaderosPlugin.getInstance().getModulesFile().getConnect().isOnlyOnline() && PlayerUtil.findPlayerByName(username) == null) {
                    commandsQueue.addCommands(username, validatedCommands);

                    validatedCommands.forEach(command -> {
                        ChatUtil.sendConsoleMessage(
                                LeaderosPlugin.getInstance().getLangFile().getMessages().getConnect().getConnectWillExecuteCommand()
                                        .replace("%command%", command)
                        );
                    });
                } else {
                    // Execute commands
                    validatedCommands.forEach(command -> {
                        HytaleServer.get().getCommandManager().handleCommand(ConsoleSender.INSTANCE, command);
                        ChatUtil.sendConsoleMessage(
                                LeaderosPlugin.getInstance().getLangFile().getMessages().getConnect().getConnectExecutedCommand()
                                                .replace("%command%", command)
                        );
                    });
                }
            }

            @Override
            public void subscribed() {
                ChatUtil.sendConsoleMessage(
                        LeaderosPlugin.getInstance().getLangFile().getMessages().getConnect().getSubscribedChannel()
                );
            }
        };

        try {
            ReconnectionTimer.run();
            FallbackTimer.run();
        } catch (Exception ignored) {}
    }

    /**
     * onDisable method of module
     */
    public void onDisable() {
        // Unregister listeners
        try {
            playerJoinEventRegistration.unregister();
            getCommandsQueue().getExecutor().shutdown();
            if (ReconnectionTimer.task != null) {
                ReconnectionTimer.task.cancel(true);
            }
            if (FallbackTimer.task != null) {
                FallbackTimer.task.cancel(true);
            }
        } catch (Exception ignored) {}
    }

    /**
     * onReload method of module
     */
    public void onReload() {
        socket.getPusher().disconnect();
    }

    /**
     * Check connection and reconnect
     */
    public void reconnect() {
        if (socket.getPusher().getConnection().getState() == ConnectionState.DISCONNECTED) {
            try {
                socket.getPusher().connect();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Constructor of connect
     */
    public ConnectModule() {
    }
}