package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class DisengageCommand extends AbstractCommand {

    public DisengageCommand() {
        setName("disengage");
        setSyntax("disengage");
        setRequiredArguments(0, 0);
        isProcedural = false;
    }

    // <--[command]
    // @Name Disengage
    // @Syntax disengage
    // @Required 0
    // @Maximum 0
    // @Plugin Citizens
    // @Short Enables an NPCs triggers that have been temporarily disabled by the engage command.
    // @Group npc
    //
    // @Description
    // Re-enables any toggled triggers that have been disabled by disengage. Using
    // disengage inside scripts must have an NPC to reference, or one may be specified
    // by supplying a valid NPCTag object with the npc argument.
    //
    // This is mostly regarded as an 'interact script command', though it may be used inside
    // other script types. This is because disengage works with the trigger system, which is an
    // interact script-container feature.
    //
    // NPCs that are interacted with while engaged will fire an 'on unavailable' assignment
    // script-container action.
    //
    // See <@link command Engage>
    //
    // @Tags
    // <NPCTag.engaged>
    //
    // @Usage
    // Use to reenable an NPC's triggers, disabled via 'engage'.
    // - engage
    // - chat 'Be right there!'
    // - walk <player.location>
    // - wait 5s
    // - disengage
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Make sure NPC is available
        if (Utilities.getEntryNPC(scriptEntry) == null) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), Utilities.getEntryNPC(scriptEntry));
        }

        // Set Disengaged
        EngageCommand.setEngaged(Utilities.getEntryNPC(scriptEntry).getCitizen(), false);
    }
}
