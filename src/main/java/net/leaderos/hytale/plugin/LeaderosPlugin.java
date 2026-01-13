package net.leaderos.hytale.plugin;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import net.leaderos.hytale.plugin.commands.LeaderosCommand;

import java.util.logging.Level;

import javax.annotation.Nonnull;

public class LeaderosPlugin extends JavaPlugin {
    private static LeaderosPlugin instance;

    public LeaderosPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    public static LeaderosPlugin get() {
        return instance;
    }

    @Override
    protected void setup() {
        instance = this;

        // Register commands
        registerCommands();

        // Register events
        registerEvents();

        info("Plugin has been enabled!");
    }

    public void info(String message) {
        this.getLogger().at(Level.INFO).log("[LeaderOS] " + message);
    }

    private void registerCommands() {
        getCommandRegistry().registerCommand(new LeaderosCommand());
    }

    private void registerEvents() {
        // Join event listener
        getEventRegistry().register(PlayerConnectEvent.class, event -> {
            info("Player connected: " + event.getPlayerRef().getUsername());
        });
    }
}
