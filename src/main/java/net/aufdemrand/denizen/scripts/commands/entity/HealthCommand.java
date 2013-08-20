package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class HealthCommand extends AbstractCommand  {


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Interpret arguments

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("target")
                    && arg.matches("player")){
                if (!scriptEntry.hasPlayer())
                    throw new InvalidArgumentsException(dB.Messages.ERROR_NO_PLAYER);
                scriptEntry.addObject("target", arg.asElement());
            }
            if (!scriptEntry.hasObject("qty")
                  && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                  scriptEntry.addObject("qty", arg.asElement());
            if (!scriptEntry.hasObject("action")
                && arg.matchesPrefix("state"))
                    scriptEntry.addObject("action", arg.asElement());
        }


        // Check for required information

        if (!scriptEntry.hasObject("qty") && !scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException(dB.Messages.ERROR_MISSING_OTHER, "QUANTITY");
        if (!scriptEntry.hasObject("target")) {
            if (!scriptEntry.hasNPC())
                throw new InvalidArgumentsException("Missing NPC!");
            scriptEntry.addObject("target", Element.valueOf("npc"));
        }

    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Fetch required objects

        Element qty = scriptEntry.getElement("qty");
        Element action = scriptEntry.getElement("action");
        boolean isplayer = scriptEntry.getElement("target").asString().equalsIgnoreCase("player");

        dB.report(getName(), (qty != null?qty.debug():"") + (action != null?action.debug():""));

        if (qty == null && action == null)
            dB.echoError("Null quantity!");
        if (action != null) {
            if (action.asString().equalsIgnoreCase("true"))
                scriptEntry.getNPC().getCitizen().addTrait(HealthTrait.class);
            else if (action.asString().equalsIgnoreCase("false"))
                scriptEntry.getNPC().getCitizen().removeTrait(HealthTrait.class);
            else
                if (scriptEntry.getNPC().getCitizen().hasTrait(HealthTrait.class))
                    scriptEntry.getNPC().getCitizen().removeTrait(HealthTrait.class);
                else
                    scriptEntry.getNPC().getCitizen().addTrait(HealthTrait.class);
        }
        if (qty != null) {
            if (isplayer) {
                scriptEntry.getPlayer().getPlayerEntity().setMaxHealth(qty.asDouble());
            }
            else {
                if (scriptEntry.getNPC().getCitizen().hasTrait(HealthTrait.class))
                    scriptEntry.getNPC().getHealthTrait().setMaxhealth(qty.asInt());
                else
                    dB.echoError("NPC doesn't have health trait!");
            }
        }
    }


}