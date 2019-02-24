package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

public class TraitCommand extends AbstractCommand {

    private enum Toggle {TOGGLE, TRUE, FALSE, ON, OFF}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("state")
                    && arg.matchesPrefix("state", "s")
                    && arg.matchesEnum(Toggle.values())) {
                scriptEntry.addObject("state", new Element(arg.getValue().toUpperCase()));
            }
            else if (!scriptEntry.hasObject("trait")) {
                scriptEntry.addObject("trait", new Element(arg.getValue()));
            }

        }

        if (!scriptEntry.hasObject("trait")) {
            throw new InvalidArgumentsException("Missing trait argument!");
        }

        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

        scriptEntry.defaultObject("state", new Element("TOGGLE"));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element toggle = scriptEntry.getElement("state");
        Element traitName = scriptEntry.getElement("trait");
        NPC npc = ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getCitizen();

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    traitName.debug() +
                            toggle.debug() +
                            ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().debug());

        }

        Class<? extends Trait> trait = CitizensAPI.getTraitFactory().getTraitClass(traitName.asString());

        if (trait == null) {
            dB.echoError(scriptEntry.getResidingQueue(), "Trait not found: " + traitName.asString());
            return;
        }

        switch (Toggle.valueOf(toggle.asString())) {

            case TRUE:
            case ON:
                if (npc.hasTrait(trait)) {
                    dB.echoError(scriptEntry.getResidingQueue(), "NPC already has trait '" + traitName.asString() + "'");
                }
                else {
                    npc.addTrait(trait);
                }
                break;

            case FALSE:
            case OFF:
                if (!npc.hasTrait(trait)) {
                    dB.echoError(scriptEntry.getResidingQueue(), "NPC does not have trait '" + traitName.asString() + "'");
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
