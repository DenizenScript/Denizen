package net.aufdemrand.denizen.scripts.commands.entity;

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

        // TODO: UPDATE THIS COMMAND!

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesState(arg))
                scriptEntry.addObject("state", Action.valueOf(aH.getStringFrom(arg).toUpperCase()));

            else if (aH.matchesArg("NPC, PLAYER", arg))
                scriptEntry.addObject("target", Target.valueOf(aH.getStringFrom(arg).toUpperCase()));

        }

        if (scriptEntry.getObject("state") == null)
            throw new InvalidArgumentsException("Must specify a state action!");

        if (scriptEntry.getObject("target") == null)
            throw new InvalidArgumentsException("Must specify a target!");

        if ((scriptEntry.getObject("target") == Target.NPC && scriptEntry.getNPC() == null)
                || (scriptEntry.getObject("target") == Target.PLAYER && scriptEntry.getPlayer() == null))
            throw new InvalidArgumentsException("NPC not found!");
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Action action = (Action) scriptEntry.getObject("state");
        Target target = (Target) scriptEntry.getObject("target");

        // Report to dB
        dB.report(scriptEntry, getName(),
                aH.debugObj("Toggle", action.name())
                        + aH.debugObj("Target", target == Target.NPC ? scriptEntry.getNPC().toString() :
                        scriptEntry.getPlayer().getName()));

        switch (target) {

            case NPC:
                if (!scriptEntry.getNPC().getCitizen().hasTrait(InvisibleTrait.class))
                    scriptEntry.getNPC().getCitizen().addTrait(InvisibleTrait.class);
                InvisibleTrait trait = scriptEntry.getNPC().getCitizen().getTrait(InvisibleTrait.class);

                switch (action) {

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

                break;

            case PLAYER:

                if (scriptEntry.getPlayer() != null) {

                Player player = scriptEntry.getPlayer().getPlayerEntity();
                PotionEffect invis = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1);

                    switch (action) {

                        case FALSE:
                            player.removePotionEffect(PotionEffectType.INVISIBILITY);
                            break;

                        case TRUE:
                            invis.apply(player);
                            break;

                        case TOGGLE:
                            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY))
                                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                            else
                                invis.apply(player);

                            break;
                    }
                }
        }

    }
}
