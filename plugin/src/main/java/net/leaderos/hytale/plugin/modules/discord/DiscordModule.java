package net.leaderos.hytale.plugin.modules.discord;

import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.shared.modules.LeaderOSModule;
import net.leaderos.hytale.plugin.modules.discord.commands.SyncCommand;

/**
 * Discord module
 *
 * @author leaderos
 * @since 1.0
 */
public class DiscordModule extends LeaderOSModule {
    private CommandRegistration syncCommand;

    /**
     * onEnable method of module
     */
    public void onEnable() {
        syncCommand = LeaderosPlugin.getInstance().getCommandRegistry().registerCommand(new SyncCommand());
    }

    /**
     * onDisable method of module
     */
    public void onDisable() {
        syncCommand.unregister();
    }

    /**
     * Constructor of Discord
     */
    public DiscordModule() {
    }
}
