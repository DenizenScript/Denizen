package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class DisengageCommand extends AbstractCommand {

    // <--[command]
    // @Name Disengage
    // @Syntax disengage
    // @Required 0
    // @Plugin Citizens
    // @Short Enables an NPCs triggers that have been temporarily disabled by the engage command.
    // @Group npc
    //
    // @Description
    // Re-enables any toggled triggers that have been disabled by disengage. Using
    // disengage inside scripts must have an NPC to reference, or one may be specified
    // by supplying a valid dNPC object with the npc argument.
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
    // <n@npc.is_engaged>
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
        if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC() == null) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().debug());
        }

        // Set Disengaged
        EngageCommand.setEngaged(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen(), false);
    }
}
