package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class DisengageCommand extends AbstractCommand {

    public DisengageCommand() {
        setName("disengage");
        setSyntax("disengage (player)");
        setRequiredArguments(0, 1);
        isProcedural = false;
        setBooleansHandled("player");
    }

    // <--[command]
    // @Name Disengage
    // @Syntax disengage (player)
    // @Required 0
    // @Maximum 1
    // @Plugin Citizens
    // @Short Enables an NPCs triggers that have been temporarily disabled by the engage command.
    // @Group npc
    //
    // @Description
    // Re-enables any toggled triggers that have been disabled by disengage.
    // Using disengage inside scripts must have an NPC to reference, or one may be specified by supplying a valid NPCTag object with the npc argument.
    //
    // Engaging an NPC by default affects all players attempting to interact with the NPC.
    // You can optionally specify 'player' to only affect the linked player.
    //
    // This is mostly regarded as an 'interact script command', though it may be used inside other script types.
    // This is because disengage works with the trigger system, which is an interact script-container feature.
    //
    // NPCs that are interacted with while engaged will fire an 'on unavailable' assignment script-container action.
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
    public void execute(ScriptEntry scriptEntry) {
        boolean linkedPlayer = scriptEntry.argAsBoolean("player");
        if (Utilities.getEntryNPC(scriptEntry) == null) {
            throw new InvalidArgumentsRuntimeException("This command requires a linked NPC!");
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), Utilities.getEntryNPC(scriptEntry), db("player", linkedPlayer));
        }
        EngageCommand.setEngaged(Utilities.getEntryNPC(scriptEntry).getCitizen(), linkedPlayer ? Utilities.getEntryPlayer(scriptEntry) : null, false);
    }
}
