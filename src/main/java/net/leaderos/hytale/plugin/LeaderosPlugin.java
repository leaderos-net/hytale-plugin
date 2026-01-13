package net.leaderos.hytale.plugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.logging.Level;

import javax.annotation.Nonnull;

public class LeaderosPlugin extends JavaPlugin {
    private static LeaderosPlugin instance;

    // Logger instance for this class, automatically named after the class.
    private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();

    public LeaderosPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static LeaderosPlugin get() {
        return instance;
    }

    /**
     * Called when the plugin is enabled.
     * Use this for initialization logic like registering commands or event
     * listeners.
     */
    @Override
    protected void setup() {
        super.setup();
        logger.at(Level.INFO).log("=============================");
        logger.at(Level.INFO).log("BasicPlugin has been enabled!");
        logger.at(Level.INFO).log("=============================");
    }
}
