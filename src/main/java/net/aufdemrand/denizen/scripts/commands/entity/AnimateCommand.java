package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.List;

import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;

import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.util.PlayerAnimation;

/**
 * Animate a list of entities using a PlayerAnimation from Citizens2
 * or an EntityEffect from Bukkit.
 *
 * @author David Cernat
 */

public class AnimateCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));
            }

            if (!scriptEntry.hasObject("animation") &&
                    !scriptEntry.hasObject("effect")) {

                if (arg.matchesEnum(PlayerAnimation.values())) {
                    scriptEntry.addObject("animation", PlayerAnimation.valueOf(arg.getValue().toUpperCase()));
                }
                else if (arg.matchesEnum(EntityEffect.values())) {
                    scriptEntry.addObject("effect", EntityEffect.valueOf(arg.getValue().toUpperCase()));
                }
            }
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("entities"))
            throw new InvalidArgumentsException("Must specify entity/entities!");

        if (!scriptEntry.hasObject("effect") && !scriptEntry.hasObject("animation"))
            throw new InvalidArgumentsException("Must specify a valid animation!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        PlayerAnimation animation = scriptEntry.hasObject("animation") ?
                (PlayerAnimation) scriptEntry.getObject("animation") : null;
        EntityEffect effect = scriptEntry.hasObject("effect") ?
                (EntityEffect) scriptEntry.getObject("effect") : null;

        // Report to dB
        dB.report(scriptEntry, getName(), (animation != null ?
                aH.debugObj("animation", animation.name()) :
                aH.debugObj("effect", effect.name())) +
                aH.debugObj("entities", entities.toString()));

        // Go through all the entities and animate them
        for (dEntity entity : entities) {
            if (entity.isSpawned()) {

                try {
                    if (animation != null && entity.getBukkitEntity() instanceof Player) {

                        Player player = (Player) entity.getBukkitEntity();

                        // Go through Citizens' PlayerAnimations and find the one
                        // that matches
                        PlayerAnimation[] animationArray = PlayerAnimation.class.getEnumConstants();

                        for (PlayerAnimation current : animationArray) {

                            if (current.equals(animation)) {
                                current.play(player);
                                break;
                            }
                        }
                    } else entity.getBukkitEntity().playEffect(effect);

                } catch (Exception e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Error playing that animation!");
                } // We tried!
            }
        }
    }
}
