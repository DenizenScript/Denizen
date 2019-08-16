package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.AnimationHelper;
import com.denizenscript.denizen.nms.interfaces.EntityAnimation;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;

import java.util.List;

public class AnimateCommand extends AbstractCommand {

    // <--[command]
    // @Name Animate
    // @Syntax animate [<entity>|...] [animation:<name>]
    // @Required 2
    // @Plugin Citizens
    // @Short Makes a list of entities perform a certain animation.
    // @Group entity
    //
    // @Description
    // Minecraft implements several player and entity animations which the animate command can use, just
    // specify an entity and an animation.
    //
    // Player animations require a Player-type entity or NPC. Available player animations include:
    // ARM_SWING, CRIT, HURT, and MAGIC_CRIT, SIT, SLEEP, SNEAK, STOP_SITTING, STOP_SLEEPING, STOP_SNEAKING,
    // START_USE_MAINHAND_ITEM, START_USE_OFFHAND_ITEM, STOP_USE_ITEM, EAT_FOOD, ARM_SWING_OFFHAND
    //
    // All entities also have available Bukkit's entity effect list:
    // <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/EntityEffect.html>
    //
    // In addition, Denizen adds a few new entity animations:
    // SKELETON_START_SWING_ARM, SKELETON_STOP_SWING_ARM, POLAR_BEAR_START_STANDING, POLAR_BEAR_STOP_STANDING
    //
    // Note that the above list only applies where logical, EG 'WOLF_' animations only apply to wolves.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to make a player appear to get hurt.
    // - animate <player> animation:hurt
    //
    // @Usage
    // Use to make a wolf NPC shake
    // - animate '<n@aufdemrand's wolf>' animation:wolf_shake
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        AnimationHelper animationHelper = NMSHandler.getAnimationHelper();

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                // Entity arg
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }

            if (!scriptEntry.hasObject("animation") &&
                    !scriptEntry.hasObject("effect") &&
                    !scriptEntry.hasObject("nms_animation")) {

                if (arg.matchesEnum(PlayerAnimation.values())) {
                    scriptEntry.addObject("animation", PlayerAnimation.valueOf(arg.getValue().toUpperCase()));
                }
                else if (arg.matchesEnum(EntityEffect.values())) {
                    scriptEntry.addObject("effect", EntityEffect.valueOf(arg.getValue().toUpperCase()));
                }
                else if (animationHelper.hasEntityAnimation(arg.getValue())) {
                    scriptEntry.addObject("nms_animation", arg.getValue());
                }
            }
        }

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }

        if (!scriptEntry.hasObject("effect") && !scriptEntry.hasObject("animation") && !scriptEntry.hasObject("nms_animation")) {
            throw new InvalidArgumentsException("Must specify a valid animation!");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        // Get objects
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        PlayerAnimation animation = scriptEntry.hasObject("animation") ?
                (PlayerAnimation) scriptEntry.getObject("animation") : null;
        EntityEffect effect = scriptEntry.hasObject("effect") ?
                (EntityEffect) scriptEntry.getObject("effect") : null;
        String nmsAnimation = scriptEntry.hasObject("nms_animation") ?
                (String) scriptEntry.getObject("nms_animation") : null;

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (animation != null ?
                    ArgumentHelper.debugObj("animation", animation.name()) : effect != null ?
                    ArgumentHelper.debugObj("effect", effect.name()) :
                    ArgumentHelper.debugObj("animation", nmsAnimation)) +
                    ArgumentHelper.debugObj("entities", entities.toString()));
        }

        // Go through all the entities and animate them
        for (EntityTag entity : entities) {
            if (entity.isSpawned()) {

                try {
                    if (animation != null && entity.getBukkitEntity() instanceof Player) {

                        Player player = (Player) entity.getBukkitEntity();

                        animation.play(player);
                    }
                    else if (effect != null) {
                        entity.getBukkitEntity().playEffect(effect);
                    }
                    else {
                        EntityAnimation entityAnimation = NMSHandler.getAnimationHelper().getEntityAnimation(nmsAnimation);
                        entityAnimation.play(entity.getBukkitEntity());
                    }
                }
                catch (Exception e) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Error playing that animation!");
                } // We tried!
            }
        }
    }
}
