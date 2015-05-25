package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class TriggerCommand extends AbstractCommand {

    private enum Toggle {TOGGLE, TRUE, FALSE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("cooldown")
                    && arg.matchesPrefix("cooldown", "c")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("cooldown", arg.asType(Duration.class));

            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrefix("radius", "r")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("radius", arg.asElement());

            else if (!scriptEntry.hasObject("toggle")
                    && arg.matchesEnum(Toggle.values()))
                scriptEntry.addObject("toggle", arg.asElement());

            else if (!scriptEntry.hasObject("npc")
                    && arg.matchesArgumentType(dNPC.class))
                scriptEntry.addObject("npc", arg.asType(dNPC.class));

            else if (!scriptEntry.hasObject("trigger"))
                scriptEntry.addObject("trigger", arg.asElement());

            else
                arg.reportUnhandled();
        }

        if (!scriptEntry.hasObject("trigger"))
            throw new InvalidArgumentsException("Missing name argument!");

        if (!scriptEntry.hasObject("toggle"))
            scriptEntry.addObject("toggle", new Element("TOGGLE"));

        if (!((BukkitScriptEntryData)scriptEntry.entryData).hasNPC() && !scriptEntry.hasObject("npc"))
            throw new InvalidArgumentsException("This command requires a linked NPC!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element toggle = scriptEntry.getElement("toggle");
        Element trigger = scriptEntry.getElement("trigger");
        Element radius = scriptEntry.getElement("radius");
        Duration cooldown = (Duration) scriptEntry.getObject("cooldown");
        dNPC npc = scriptEntry.hasObject("npc") ? (dNPC) scriptEntry.getObject("npc") : ((BukkitScriptEntryData)scriptEntry.entryData).getNPC();

        dB.report(scriptEntry, getName(),
                trigger.debug() + toggle.debug() +
                        (radius != null ? radius.debug(): "") +
                        (cooldown != null ? cooldown.debug(): "") +
                        npc.debug());

        // Add trigger trait
        if (!npc.getCitizen().hasTrait(TriggerTrait.class)) npc.getCitizen().addTrait(TriggerTrait.class);

        TriggerTrait trait = npc.getCitizen().getTrait(TriggerTrait.class);

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

        if (cooldown != null)
            trait.setLocalCooldown(trigger.asString(), cooldown.getSeconds());
    }
}
