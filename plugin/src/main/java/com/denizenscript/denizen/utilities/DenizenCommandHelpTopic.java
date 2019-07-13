package com.denizenscript.denizen.utilities;

import org.bukkit.command.CommandSender;
import org.bukkit.help.GenericCommandHelpTopic;

public class DenizenCommandHelpTopic extends GenericCommandHelpTopic {

    private final DenizenCommand denizenCommand;

    public DenizenCommandHelpTopic(DenizenCommand command) {
        super(command);
        this.denizenCommand = command;
    }

    @Override
    public boolean canSee(CommandSender sender) {
        return denizenCommand.canSeeHelp(sender);
    }
}
