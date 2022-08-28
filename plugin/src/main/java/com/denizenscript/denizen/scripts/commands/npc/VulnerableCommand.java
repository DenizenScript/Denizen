package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.npc.NPC;

public class VulnerableCommand extends AbstractCommand {

    public VulnerableCommand() {
        setName("vulnerable");
        setSyntax("vulnerable (state:{true}/false/toggle)");
        setRequiredArguments(0, 1);
        isProcedural = false;
    }

    // <--[command]
    // @Name Vulnerable
    // @Syntax vulnerable (state:{true}/false/toggle)
    // @Required 0
    // @Maximum 1
    // @Plugin Citizens
    // @Short Sets whether an NPC is vulnerable.
    // @Group npc
    //
    // @Description
    // Toggles whether an NPC can be hurt or not.
    //
    // @Tags
    // <NPCTag.invulnerable>
    //
    // @Usage
    // Makes an NPC vulnerable.
    // - vulnerable state:true
    //
    // @Usage
    // Makes an NPC vulnerable if it is not, and invulnerable if it is.
    // - vulnerable
    //
    // -->

    enum Toggle {TRUE, FALSE, TOGGLE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("action") && arg.matchesEnum(Toggle.class)) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("action", new ElementTag("toggle"));
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag action = scriptEntry.getElement("action");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), Utilities.getEntryNPC(scriptEntry), action);
        }
        NPC npc = Utilities.getEntryNPC(scriptEntry).getCitizen();
        Toggle toggle = Toggle.valueOf(action.asString().toUpperCase());
        npc.setProtected(!(toggle == Toggle.TOGGLE ? npc.isProtected() : action.asBoolean()));
    }
}
