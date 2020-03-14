package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

public class TraitCommand extends AbstractCommand {

    public TraitCommand() {
        setName("trait");
        setSyntax("trait (state:true/false/{toggle}) [<trait>]");
        setRequiredArguments(1, 2);
    }

    // <--[command]
    // @Name Trait
    // @Syntax trait (state:true/false/{toggle}) [<trait>]
    // @Required 1
    // @Maximum 2
    // @Plugin Citizens
    // @Short Adds or removes a trait from an NPC.
    // @Group npc
    //
    // @Description
    // This command adds or removes a trait from an NPC.
    //
    // Use "state:true" to add or "state:false" to remove.
    // If neither is specified, the default is "toggle", which means remove if already present or add if not.
    //
    // Note that a redundant instruction, like adding a trait that the NPC already has, will give an error message.
    //
    // The trait input is simply the name of the trait, like "sentinel".
    //
    // @Tags
    // <NPCTag.has_trait[<trait>]>
    // <NPCTag.list_traits>
    //
    // @Usage
    // Use to add the Sentinel trait to the linked NPC.
    // - trait state:true sentinel
    //
    // Use to toggle the MobProx trait on the linked NPC.
    // - trait mobprox
    //
    // -->

    private enum Toggle {TOGGLE, TRUE, FALSE, ON, OFF}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

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
