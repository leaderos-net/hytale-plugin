package net.leaderos.hytale.plugin.modules.connect.timer;

import com.hypixel.hytale.server.core.HytaleServer;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.api.ModuleManager;
import net.leaderos.hytale.plugin.modules.connect.ConnectModule;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Time checker for update scheduler
 *
 */
public class ReconnectionTimer {

    /**
     * Runnable id for cancel or resume
     */
    public static ScheduledFuture<?> task;
    public static void run() {
        if (task != null) {
            task.cancel(true);
            task = null;
        }

        if (LeaderosPlugin.getInstance().getModulesFile().getConnect().getReconnectionTimer() > 0) {
            task = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                    () -> {
                        // Runs every X seconds after the previous execution completes
                        ConnectModule module = (ConnectModule) ModuleManager.getModule("Connect");
                        module.reconnect();
                    },
                    LeaderosPlugin.getInstance().getModulesFile().getConnect().getReconnectionTimer(), LeaderosPlugin.getInstance().getModulesFile().getConnect().getReconnectionTimer(), TimeUnit.SECONDS
            );
        }
    }
}
