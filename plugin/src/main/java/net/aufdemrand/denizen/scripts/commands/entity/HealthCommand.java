package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.Arrays;
import java.util.List;

public class HealthCommand extends AbstractCommand {


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("target")
                    && arg.matches("player")) {
                if (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                    throw new InvalidArgumentsException("No player attached!");
                }
                scriptEntry.addObject("target", Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity()));
            }

            else if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("qty", arg.asElement());
            }

            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("target", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }

            else if (!scriptEntry.hasObject("action")
                    && arg.matchesPrefix("state")) {
                scriptEntry.addObject("action", arg.asElement());
            }

            else {
                arg.reportUnhandled();
            }
        }


        // Check for required information

        if (!scriptEntry.hasObject("qty") && !scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify a quantity!");
        }
        if (!scriptEntry.hasObject("target")) {
            if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
                throw new InvalidArgumentsException("Missing NPC!");
            }
            scriptEntry.addObject("target", Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity()));
        }

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element qty = scriptEntry.getElement("qty");
        Element action = scriptEntry.getElement("action");
        List<dEntity> targets = (List<dEntity>) scriptEntry.getObject("target");

        dB.report(scriptEntry, getName(), (qty != null ? qty.debug() : "") +
                (action != null ? action.debug() : "") +
                aH.debugObj("target", targets.toString()));

        if (qty == null && action == null) {
            dB.echoError(scriptEntry.getResidingQueue(), "Null quantity!");
        }

        if (action == null) {
            action = Element.TRUE;
        }

        for (dEntity target : targets) {
            if (target.isCitizensNPC()) {
                if (action.asString().equalsIgnoreCase("true")) {
                    target.getDenizenNPC().getCitizen().addTrait(HealthTrait.class);
                }
                else if (action.asString().equalsIgnoreCase("false")) {
                    target.getDenizenNPC().getCitizen().removeTrait(HealthTrait.class);
                }
                else if (target.getDenizenNPC().getCitizen().hasTrait(HealthTrait.class)) {
                    target.getDenizenNPC().getCitizen().removeTrait(HealthTrait.class);
                }
                else {
                    target.getDenizenNPC().getCitizen().addTrait(HealthTrait.class);
                }
            }

            if (qty != null) {
                if (target.isCitizensNPC()) {
                    if (target.getDenizenNPC().getCitizen().hasTrait(HealthTrait.class)) {
                        target.getDenizenNPC().getCitizen().getTrait(HealthTrait.class).setMaxhealth((int) qty.asFloat());
                    }
                    else {
                        dB.echoError(scriptEntry.getResidingQueue(), "NPC doesn't have health trait!");
                    }
                }
                else if (target.isLivingEntity()) {
                    target.getLivingEntity().setMaxHealth(qty.asDouble());
                }
                else {
                    dB.echoError(scriptEntry.getResidingQueue(), "Entity '" + target.identify() + "'is not alive!");
                }
            }
        }
    }
}
