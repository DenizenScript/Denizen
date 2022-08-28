package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.PushableTrait;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class PushableCommand extends AbstractCommand {

    public PushableCommand() {
        setName("pushable");
        setSyntax("pushable (state:true/false/{toggle}) (delay:<duration>) (returnable:true/false)");
        setRequiredArguments(0, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Pushable
    // @Syntax pushable (state:true/false/{toggle}) (delay:<duration>) (returnable:true/false)
    // @Required 0
    // @Maximum 3
    // @Plugin Citizens
    // @Short Edits the pushable trait for NPCs.
    // @Group npc
    //
    // @Description
    // Enables, disables, toggles, or edits the Pushable trait on the attached NPC.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to toggle the Pushable trait for a specified NPC.
    // - pushable npc:<[some_npc]>
    //
    // @Usage
    // Use to enable the Pushable trait and return after 2 seconds.
    // - pushable state:true delay:2s returnable:true
    // -->

    private enum Toggle {TOGGLE, TRUE, FALSE, ON, OFF}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("state")
                    && arg.matchesPrefix("state", "s")
                    && arg.matchesEnum(Toggle.class)) {
                scriptEntry.addObject("state", arg.asElement());
            }
            else if (!scriptEntry.hasObject("delay")
                    && arg.matchesPrefix("delay", "d")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("delay", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("return")
                    && arg.matchesPrefix("return", "r")
                    && arg.matchesBoolean()) {
                scriptEntry.addObject("return", arg.asElement());
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        NPCTag denizenNPC = Utilities.getEntryNPC(scriptEntry);
        if (denizenNPC == null) {
            Debug.echoError("No valid NPC attached to this queue!");
            return;
        }
        PushableTrait trait = denizenNPC.getPushableTrait();
        ElementTag state = scriptEntry.getElement("state");
        DurationTag delay = scriptEntry.getObjectTag("delay");
        ElementTag returnable = scriptEntry.getElement("return");
        if (state == null && delay == null && returnable == null) {
            state = new ElementTag("TOGGLE");
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), denizenNPC, state, delay, returnable);
        }
        if (delay != null) {
            trait.setDelay(delay.getSecondsAsInt());
        }
        if (returnable != null) {
            trait.setReturnable(returnable.asBoolean());
        }
        if (state != null) {
            switch (Toggle.valueOf(state.asString().toUpperCase())) {

                case TRUE:
                case ON:
                    trait.setPushable(true);
                    break;

                case FALSE:
                case OFF:
                    trait.setPushable(false);
                    break;

                case TOGGLE:
                    trait.setPushable(!trait.isPushable());
                    break;
            }
        }
    }
}
