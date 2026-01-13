package net.leaderos.hytale.plugin.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class LeaderosCommand extends AbstractCommandCollection {
    public LeaderosCommand() {
        super("leaderos", "commands.leaderos.description");
        this.addSubCommand(new LeaderosReloadCommand());
    }
}
