package net.leaderos.hytale.plugin.helpers;

import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.shared.helpers.DebugAPI;
import net.leaderos.hytale.shared.model.DebugMode;

import java.util.logging.Level;

/**
 * Sends debug to console
 * @author leaderos
 * @since 1.0.0
 */
public class DebugHytale implements DebugAPI {

    /**
     * Sends debug to console
     * @param message to debug
     * @param strict if true, it will send debug even if debug mode is disabled
     */
    @Override
    public void send(String message, boolean strict) {
        if (
                LeaderosPlugin.getInstance().getConfigFile().getSettings().getDebugMode() == DebugMode.ENABLED ||
                        (LeaderosPlugin.getInstance().getConfigFile().getSettings().getDebugMode() == DebugMode.ONLY_ERRORS && strict)
        ) {
            LeaderosPlugin.getInstance().getLogger().at(Level.WARNING).log(
                    "[DEBUG] " + message
            );
        }
    }

    /**
     * Constructor of debug
     */
    public DebugHytale() {}
}
