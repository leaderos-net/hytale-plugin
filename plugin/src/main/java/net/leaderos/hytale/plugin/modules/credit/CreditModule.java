package net.leaderos.hytale.plugin.modules.credit;

import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import net.leaderos.hytale.plugin.LeaderosPlugin;
import net.leaderos.hytale.plugin.modules.credit.commands.CreditCommand;
import net.leaderos.hytale.shared.modules.LeaderOSModule;


/**
 * Credit module of leaderos-plugin
 *
 * @author leaderos
 * @since 1.0
 */
public class CreditModule extends LeaderOSModule {
    private CommandRegistration creditCommand;

    /**
     * onEnable method of module
     */
    public void onEnable() {
        creditCommand = LeaderosPlugin.getInstance().getCommandRegistry().registerCommand(new CreditCommand());
    }

    /**
     * onDisable method of module
     */
    public void onDisable() {
        creditCommand.unregister();
    }


    /**
     * Constructor of Credit
     */
    public CreditModule() {
    }
}