package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dNPC;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.InvisibleTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Makes a player or an NPC invisible.
 *
 * Note: Only works on NPCs that you have used
 * "/npc playerlist" on!
 *
 * @author aufdemrand
 *
 */
public class InvisibleCommand extends AbstractCommand {

    enum Action { TRUE, FALSE, TOGGLE }
    enum Target { PLAYER, NPC }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("state")
                    && arg.matchesEnum(Action.values()))
                scriptEntry.addObject("state", arg.asElement());

            else if (!scriptEntry.hasObject("target")
                    && arg.matches("PLAYER")
                    && scriptEntry.hasPlayer())
                scriptEntry.addObject("target", scriptEntry.getPlayer().getDenizenEntity());

            else if (!scriptEntry.hasObject("target")
                    && arg.matches("NPC")
                    && scriptEntry.hasNPC())
                scriptEntry.addObject("target", scriptEntry.getNPC().getDenizenEntity());

            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentType(dEntity.class))
                scriptEntry.addObject("target", arg.asType(dEntity.class));

            else
                arg.reportUnhandled();
        }

        if (!scriptEntry.hasObject("state"))
            scriptEntry.addObject("state", new Element("TRUE"));

        if (!scriptEntry.hasObject("target") || !((dEntity)scriptEntry.getdObject("target")).isValid())
            throw new InvalidArgumentsException("Must specify a valid target!");
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Element state = scriptEntry.getElement("state");
        dEntity target = (dEntity) scriptEntry.getObject("target");

        // Report to dB
        dB.report(scriptEntry, getName(), state.debug() + target.debug());

        if (target.isNPC()) {
            NPC npc = target.getNPC();
            if (!npc.hasTrait(InvisibleTrait.class))
                npc.addTrait(InvisibleTrait.class);
            InvisibleTrait trait = npc.getTrait(InvisibleTrait.class);
            switch (Action.valueOf(state.asString().toUpperCase())) {
                case FALSE:
                    trait.setInvisible(false);
                    break;
                case TRUE:
                    trait.setInvisible(true);
                    break;
                case TOGGLE:
                    trait.toggle();
                    break;
            }
        }
        else {
            switch (Action.valueOf(state.asString().toUpperCase())) {
                case FALSE:
                    target.getLivingEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
                    break;
                case TRUE:
                    new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1).apply(target.getLivingEntity());
                    break;
                case TOGGLE:
                    if (target.getLivingEntity().hasPotionEffect(PotionEffectType.INVISIBILITY))
                        target.getLivingEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
                    else
                        new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1).apply(target.getLivingEntity());
                    break;
            }
        }
    }
}
