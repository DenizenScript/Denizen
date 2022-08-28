package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.LookClose;

public class LookcloseCommand extends AbstractCommand {

    public LookcloseCommand() {
        setName("lookclose");
        setSyntax("lookclose (<npc>) (state:<true/false>) (range:<#>) (realistic)");
        setRequiredArguments(0, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name LookClose
    // @Syntax lookclose (<npc>) (state:<true/false>) (range:<#>) (realistic)
    // @Required 0
    // @Maximum 4
    // @Plugin Citizens
    // @Short Interacts with an NPCs 'lookclose' trait as provided by Citizens.
    // @Group npc
    //
    // @Description
    // Use this command with any NPC to alter the state and options of its 'lookclose' trait.
    // When an NPC's 'lookclose' trait is toggled to true, the NPC's head will follow nearby players.
    // Specifying realistic will enable a higher precision and detection of players, while taking into account 'line-of-sight', however can use more CPU cycles.
    // You may also specify a range integer to specify the number of blocks that will trigger the NPC's attention.
    //
    // @Tags
    // <NPCTag.lookclose>
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
        for (Argument arg : scriptEntry) {
            if (arg.matches("realistic", "realistically")) {
                scriptEntry.addObject("realistic", new ElementTag(true));
            }
            else if (arg.matchesInteger()) {
                scriptEntry.addObject("range", arg.asElement());
            }
            else if (arg.matchesBoolean()) {
                scriptEntry.addObject("toggle", arg.asElement());
            }
            else if (arg.matchesArgumentType(NPCTag.class)) {
                scriptEntry.addObject("npc", arg.asType(NPCTag.class));
                ((BukkitScriptEntryData) scriptEntry.entryData).setNPC(arg.asType(NPCTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("npc", Utilities.getEntryNPC(scriptEntry));
        if (!scriptEntry.hasObject("npc")) {
            throw new InvalidArgumentsException("NPC linked was missing or invalid.");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag realistic = scriptEntry.getElement("realistic");
        ElementTag range = scriptEntry.getElement("range");
        ElementTag toggle = scriptEntry.getElement("toggle");
        NPCTag npc = scriptEntry.getObjectTag("npc");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), npc, realistic, range, toggle);
        }
        LookClose trait = npc.getCitizen().getOrAddTrait(LookClose.class);
        if (toggle != null) {
            trait.lookClose(toggle.asBoolean());
        }
        if (realistic != null && realistic.asBoolean()) {
            trait.setRealisticLooking(true);
        }
        else {
            trait.setRealisticLooking(false);
        }
        if (range != null) {
            trait.setRange(range.asInt());
        }

    }
}
