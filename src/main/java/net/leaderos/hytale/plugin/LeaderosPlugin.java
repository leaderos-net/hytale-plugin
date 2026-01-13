package net.leaderos.hytale.plugin;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import net.leaderos.hytale.plugin.commands.LeaderosCommand;

import java.util.logging.Level;

import javax.annotation.Nonnull;

public class LeaderosPlugin extends JavaPlugin {
    private static LeaderosPlugin instance;

    // Define configuration class
    public static class LeaderosPluginConfig {
        public static final BuilderCodec<LeaderosPluginConfig> CODEC =
                BuilderCodec.builder(LeaderosPluginConfig.class, LeaderosPluginConfig::new)
                        .append(new KeyedCodec<>("Url", Codec.STRING),
                                (c, v) -> c.url = v, c -> c.url)
                        .add()
                        .append(new KeyedCodec<>("ApiKey", Codec.STRING),
                                (c, v) -> c.apiKey = v, c -> c.apiKey)
                        .add()
                        .build();

        private String url = "https://yourwebsite.com";
        private String apiKey = "YOUR_API_KEY";

        // Getters
        public String getUrl() { return url; }
        public String getApiKey() { return apiKey; }
    }

    // Config must be declared BEFORE setup() is called
    // The file will be named "config.json" in your plugin's data directory
    private final Config<LeaderosPluginConfig> config = this.withConfig(LeaderosPluginConfig.CODEC);

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

    @Override
    protected void start() {
        // Access config after loading
        LeaderosPluginConfig cfg = config.get();
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
