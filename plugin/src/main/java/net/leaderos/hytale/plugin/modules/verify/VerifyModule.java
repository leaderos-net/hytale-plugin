package net.leaderos.hytale.plugin.modules.verify;

import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.shared.modules.LeaderOSModule;
import net.leaderos.hytale.plugin.modules.verify.commands.VerifyCommand;

/**
 * Verify module
 *
 * @author leaderos
 * @since 1.0
 */
public class VerifyModule extends LeaderOSModule {
    private static CommandRegistration verifyCommand;

    /**
     * onEnable method of module
     */
    public void onEnable() {
        verifyCommand = LeaderosPlugin.getInstance().getCommandRegistry().registerCommand(new VerifyCommand());
    }

    /**
     * onDisable method of module
     */
    public void onDisable() {
        verifyCommand.unregister();
    }

    /**
     * Constructor of VerifyModule
     */
    public VerifyModule() {
    }
}
