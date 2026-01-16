package net.leaderos.hytale.plugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import lombok.Setter;
import net.leaderos.hytale.plugin.api.ModuleManager;
import net.leaderos.hytale.plugin.commands.LeaderosCommand;
import net.leaderos.hytale.plugin.configuration.Config;
import net.leaderos.hytale.plugin.configuration.Language;
import net.leaderos.hytale.plugin.configuration.Modules;
import net.leaderos.hytale.plugin.helpers.ChatUtil;
import net.leaderos.hytale.plugin.helpers.DebugHytale;
import net.leaderos.hytale.plugin.modules.ai.AiModule;
import net.leaderos.hytale.plugin.modules.connect.ConnectModule;
import net.leaderos.hytale.plugin.modules.credit.CreditModule;
import net.leaderos.hytale.plugin.modules.discord.DiscordModule;
import net.leaderos.hytale.plugin.modules.verify.VerifyModule;
import net.leaderos.hytale.shared.Shared;
import net.leaderos.hytale.shared.helpers.Placeholder;
import net.leaderos.hytale.shared.helpers.PluginUpdater;
import net.leaderos.hytale.shared.helpers.UrlUtil;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

@Getter
@Setter
public class LeaderosPlugin extends JavaPlugin {
    /**
     * Instance of plugin
     */
    @Getter
    private static LeaderosPlugin instance;

    /**
     * Config file of plugin
     */
    private Config configFile;

    /**
     * Lang file of plugin
     */
    private Language langFile;

    /**
     * Module file of plugin
     */
    private Modules modulesFile;

    /**
     * Module manager holder
     */
    private ModuleManager moduleManager;

    /**
     * Shared holder
     */
    private Shared shared;

    public LeaderosPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        instance = this;

        // Setup files
        setupFiles();

        this.shared = new Shared(
                UrlUtil.format(getConfigFile().getSettings().getUrl()),
                getConfigFile().getSettings().getApiKey(),
                new DebugHytale()
        );
        this.moduleManager = new ModuleManager();
        getModuleManager().registerModule(new AiModule());
        getModuleManager().registerModule(new VerifyModule());
        getModuleManager().registerModule(new CreditModule());
        getModuleManager().registerModule(new DiscordModule());
        getModuleManager().registerModule(new ConnectModule());

        if (getConfigFile().getSettings().getUrl().equals("https://yourwebsite.com")) {
            ChatUtil.sendConsoleMessage(getLangFile().getMessages().getChangeApiUrl());
        } else if (getConfigFile().getSettings().getUrl().startsWith("http://")) {
            ChatUtil.sendConsoleMessage(getLangFile().getMessages().getChangeApiUrlHttps());
        } else {
            getModuleManager().enableModules();
        }

        // Register commands
        registerCommands();

        // Check updates
        checkUpdate();
    }

    @Override
    protected void shutdown() {
        getModuleManager().disableModules();
    }

    /**
     * Setups config, lang and modules file file
     */
    public void setupFiles() {
        try {
            this.configFile = ConfigManager.create(Config.class, (it) -> {
                it.withConfigurer(new YamlSnakeYamlConfigurer());
                it.withBindFile(new File(getDataDirectory().toFile().getAbsolutePath(), "config.yml"));
                it.saveDefaults();
                it.load(true);
            });
            this.modulesFile = ConfigManager.create(Modules.class, (it) -> {
                it.withConfigurer(new YamlSnakeYamlConfigurer());
                it.withBindFile(new File(getDataDirectory().toFile().getAbsolutePath(), "modules.yml"));
                it.saveDefaults();
                it.load(true);
            });
            String langName = configFile.getSettings().getLang();
            Class langClass = Class.forName("net.leaderos.hytale.plugin.configuration.lang." + langName);
            Class<Language> languageClass = langClass;
            this.langFile = ConfigManager.create(languageClass, (it) -> {
                it.withConfigurer(new YamlSnakeYamlConfigurer());
                it.withBindFile(new File(getDataDirectory().toFile().getAbsolutePath() + "/lang", langName + ".yml"));
                it.saveDefaults();
                it.load(true);
            });
        } catch (Exception exception) {
            throw new RuntimeException("Error loading config.yml");
        }
    }

    private void registerCommands() {
        getCommandRegistry().registerCommand(new LeaderosCommand());
    }

    private void checkUpdate() {
        CompletableFuture.runAsync(() -> {
            String currentVersion = getInstance().getManifest().getVersion().toString();
            PluginUpdater updater = new PluginUpdater(currentVersion);
            try {
                if (updater.checkForUpdates()) {
                    String msg = ChatUtil.replacePlaceholders(
                            getInstance().getLangFile().getMessages().getUpdate(),
                            new Placeholder("%version%", updater.getLatestVersion())
                    );
                    ChatUtil.sendConsoleMessage(msg);
                }
            } catch (Exception ignored) {}
        });
    }
}
