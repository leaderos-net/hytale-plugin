package net.leaderos.hytale.plugin.modules.ai;

import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.modules.ai.commands.AiCommand;
import net.leaderos.hytale.shared.modules.LeaderOSModule;

/**
 * AI module
 * @author leaderos
 * @since 1.0
 */
public class AiModule extends LeaderOSModule {

    private CommandRegistration aiCommand;

    /**
     * onEnable method of module
     */
    @Override
    public void onEnable() {
        this.aiCommand = LeaderosPlugin.getInstance().getCommandRegistry().registerCommand(new AiCommand());
    }

    /**
     * onDisable method of module
     */
    @Override
    public void onDisable() {
        this.aiCommand.unregister();
    }

    /**
     * Constructor of AiModule
     */
    public AiModule() {}
} 