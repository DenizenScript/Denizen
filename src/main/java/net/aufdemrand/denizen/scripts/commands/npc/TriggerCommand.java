package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Configures the TriggerTrait for a NPC.
 *
 * @author Jeremy Schroeder
 */

public class TriggerCommand extends AbstractCommand {

    private enum Toggle {TOGGLE, TRUE, FALSE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("cooldown")
                    && arg.matchesPrefix("cooldown")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("cooldown", arg.asType(Duration.class));

            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrefix("radius")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("radius", arg.asElement());

            else if (!scriptEntry.hasObject("trigger")
                    && arg.matchesPrefix("name"))
                scriptEntry.addObject("trigger", arg.asElement());

            else if (!scriptEntry.hasObject("toggle")
                    && arg.matchesEnum(Toggle.values()))
                scriptEntry.addObject("toggle", arg.asElement());

            else
                arg.reportUnhandled();
        }

        if (!scriptEntry.hasObject("trigger"))
            throw new InvalidArgumentsException("Missing name argument!");

        if (!scriptEntry.hasObject("toggle"))
            scriptEntry.addObject("toggle", new Element("TOGGLE"));

        if (!scriptEntry.hasNPC())
            throw new InvalidArgumentsException("This command requires a linked NPC!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element toggle = scriptEntry.getElement("toggle");
        Element trigger = scriptEntry.getElement("trigger");
        Element radius = scriptEntry.getElement("radius");
        Duration cooldown = (Duration) scriptEntry.getObject("cooldown");

        dB.report(scriptEntry, getName(),
                  trigger.debug() + toggle.debug() +
                  (radius != null ? radius.debug(): "") +
                  (cooldown != null ? cooldown.debug(): ""));

        // Add trigger trait
        if (!scriptEntry.getNPC().getCitizen().hasTrait(TriggerTrait.class)) scriptEntry.getNPC().getCitizen().addTrait(TriggerTrait.class);

        TriggerTrait trait = scriptEntry.getNPC().getCitizen().getTrait(TriggerTrait.class);

        switch (Toggle.valueOf(toggle.asString().toUpperCase())) {
            case TOGGLE:
                trait.toggleTrigger(trigger.asString());
                break;
            case TRUE:
                trait.toggleTrigger(trigger.asString(), true);
                break;
            case FALSE:
                trait.toggleTrigger(trigger.asString(), false);
                break;
        }

        if (radius != null)
            trait.setLocalRadius(trigger.asString(), radius.asInt());

        if (cooldown != null && cooldown.getSeconds() > 0)
            trait.setLocalCooldown(trigger.asString(), cooldown.getSeconds());
    }

}
