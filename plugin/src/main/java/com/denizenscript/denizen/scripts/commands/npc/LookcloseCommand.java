package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.LookClose;

public class LookcloseCommand extends AbstractCommand {

    public LookcloseCommand() {
        setName("lookclose");
        setSyntax("lookclose (<npc>) (state:<true/false>) (range:<#>) (realistic)");
        setRequiredArguments(0, 4);
    }

    // <--[command]
    // @Name LookClose
    // @Syntax lookclose (<npc>) (state:<true/false>) (range:<#>) (realistic)
    // @Required 0
    // @Maximum 4
    // @Plugin Citizens
    // @Short Interacts with an NPCs 'lookclose' trait as provided by Citizens2.
    // @Group npc
    //
    // @Description
    // Use this command with any NPC to alter the state and options of its 'lookclose'
    // trait. When an NPC's 'lookclose' trait is toggled to true, the NPC's head will
    // follow nearby players. Specifying realistic will enable a higher precision
    // and detection of players, while taking into account 'line-of-sight', however can
    // use more CPU cycles. You may also specify a range integer to specify the number
    // of blocks that will trigger the NPC's attention.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to cause the NPC to begin looking at nearby players.
    // - lookclose true
    //
    // @Usage
    // Use to cause the NPC to stop looking at nearby players.
    // - lookclose false
    //
    // @Usage
    // Use to change the range and make the NPC more realistic
    // - lookclose true range:10 realistic
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (arg.matches("realistic", "realistically")) {
                scriptEntry.addObject("realistic", new ElementTag(true));
            }
            else if (arg.matchesInteger()) {
                scriptEntry.addObject("range", arg.asElement());
            }
            else if (arg.matchesBoolean()) {
                scriptEntry.addObject("toggle", arg.asElement());
            }
            else if (arg.matchesArgumentType(NPCTag.class)) // TODO: better way of handling this?
            {
                ((BukkitScriptEntryData) scriptEntry.entryData).setNPC(arg.asType(NPCTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Only required thing is a valid NPC. This may be an already linked
        // NPC, or one specified by arguments
        if (Utilities.getEntryNPC(scriptEntry) == null) {
            throw new InvalidArgumentsException("NPC linked was missing or invalid.");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), Utilities.getEntryNPC(scriptEntry).debug()
                    + ArgumentHelper.debugObj("realistic", scriptEntry.getObject("realistic"))
                    + ArgumentHelper.debugObj("range", scriptEntry.getObject("range"))
                    + ArgumentHelper.debugObj("toggle", scriptEntry.getObject("toggle")));

        }

        // Get the instance of the trait that belongs to the target NPC
        LookClose trait = Utilities.getEntryNPC(scriptEntry).getCitizen().getTrait(LookClose.class);

        // Handle toggle
        if (scriptEntry.hasObject("toggle")) {
            trait.lookClose(scriptEntry.getElement("toggle").asBoolean());
        }

        // Handle realistic
        if (scriptEntry.hasObject("realistic")) {
            trait.setRealisticLooking(true);
        }
        else {
            trait.setRealisticLooking(false);
        }

        // Handle range
        if (scriptEntry.hasObject("range")) {
            trait.setRange(scriptEntry.getElement("range").asInt());
        }

    }
}
