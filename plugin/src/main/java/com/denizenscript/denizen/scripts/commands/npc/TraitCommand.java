package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

public class TraitCommand extends AbstractCommand {

    // <--[command]
    // @Name Trait
    // @Syntax trait (state:true/false/{toggle}) [<trait>]
    // @Required 1
    // @Plugin Citizens
    // @Short Adds or removes a trait from an NPC.
    // @Group npc
    //
    // @Description
    // TODO: Document Command Details
    //
    // @Tags
    // <n@npc.has_trait[<trait>]>
    // <n@npc.list_traits>
    //
    // @Usage
    // TODO: Document Command Details
    // -->

    private enum Toggle {TOGGLE, TRUE, FALSE, ON, OFF}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("state")
                    && arg.matchesPrefix("state", "s")
                    && arg.matchesEnum(Toggle.values())) {
                scriptEntry.addObject("state", new ElementTag(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("trait")) {
                scriptEntry.addObject("trait", new ElementTag(arg.getValue()));
            }

        }

        if (!scriptEntry.hasObject("trait")) {
            throw new InvalidArgumentsException("Missing trait argument!");
        }

        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

        scriptEntry.defaultObject("state", new ElementTag("TOGGLE"));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        ElementTag toggle = scriptEntry.getElement("state");
        ElementTag traitName = scriptEntry.getElement("trait");
        NPC npc = Utilities.getEntryNPC(scriptEntry).getCitizen();

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(),
                    traitName.debug() +
                            toggle.debug() +
                            Utilities.getEntryNPC(scriptEntry).debug());

        }

        Class<? extends Trait> trait = CitizensAPI.getTraitFactory().getTraitClass(traitName.asString());

        if (trait == null) {
            Debug.echoError(scriptEntry.getResidingQueue(), "Trait not found: " + traitName.asString());
            return;
        }

        switch (Toggle.valueOf(toggle.asString())) {

            case TRUE:
            case ON:
                if (npc.hasTrait(trait)) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "NPC already has trait '" + traitName.asString() + "'");
                }
                else {
                    npc.addTrait(trait);
                }
                break;

            case FALSE:
            case OFF:
                if (!npc.hasTrait(trait)) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "NPC does not have trait '" + traitName.asString() + "'");
                }
                else {
                    npc.removeTrait(trait);
                }
                break;

            case TOGGLE:
                if (npc.hasTrait(trait)) {
                    npc.removeTrait(trait);
                }
                else {
                    npc.addTrait(trait);
                }
                break;

        }

    }
}
