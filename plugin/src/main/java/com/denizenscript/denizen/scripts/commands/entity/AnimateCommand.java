package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.AnimationHelper;
import com.denizenscript.denizen.nms.interfaces.EntityAnimation;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;

import java.util.List;

public class AnimateCommand extends AbstractCommand {

    public AnimateCommand() {
        setName("animate");
        setSyntax("animate [<entity>|...] [animation:<name>] (for:<player>|...)");
        setRequiredArguments(2, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Animate
    // @Syntax animate [<entity>|...] [animation:<name>] (for:<player>|...)
    // @Required 2
    // @Maximum 3
    // @Plugin Citizens
    // @Short Makes a list of entities perform a certain animation.
    // @Group entity
    //
    // @Description
    // Minecraft implements several player and entity animations which the animate command can use, just
    // specify an entity and an animation.
    //
    // Player animations require a Player-type entity or NPC. Available player animations include:
    // ARM_SWING, HURT, CRIT, MAGIC_CRIT, SIT, SLEEP, SNEAK, STOP_SITTING, STOP_SLEEPING, STOP_SNEAKING,
    // START_USE_MAINHAND_ITEM, START_USE_OFFHAND_ITEM, STOP_USE_ITEM, EAT_FOOD, ARM_SWING_OFFHAND
    //
    // All entities also have available Bukkit's entity effect list:
    // <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/EntityEffect.html>
    // These EntityEffect options can optionally be played only for specific players with the "for:" argument input.
    //
    // In addition, Denizen adds a few new entity animations:
    // SKELETON_START_SWING_ARM, SKELETON_STOP_SWING_ARM,
    // POLAR_BEAR_START_STANDING, POLAR_BEAR_STOP_STANDING,
    // HORSE_BUCK, HORSE_START_STANDING, HORSE_STOP_STANDING,
    // IRON_GOLEM_ATTACK,
    // VILLAGER_SHAKE_HEAD,
    // SWING_MAIN_HAND, SWING_OFF_HAND
    //
    // Note that the above list only applies where logical, EG 'WOLF_' animations only apply to wolves.
    //
    // In versions 1.20+, to specify the direction of damage for the HURT animation, use <@link mechanism EntityTag.play_hurt_animation>
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to make a player appear to get hurt.
    // - animate <player> animation:hurt
    //
    // @Usage
    // Use to make a wolf NPC shake.
    // - animate <npc> animation:wolf_shake
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("animation:", EntityEffect.values());
        tab.addWithPrefix("animation:", PlayerAnimation.values());
        tab.addWithPrefix("animation:", AnimationHelper.entityAnimations.keySet());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("for")
                    && arg.matchesPrefix("for")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("for", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            if (!scriptEntry.hasObject("animation") &&
                    !scriptEntry.hasObject("effect") &&
                    !scriptEntry.hasObject("nms_animation")) {
                if (arg.matchesEnum(PlayerAnimation.class)) {
                    scriptEntry.addObject("animation", PlayerAnimation.valueOf(arg.getValue().toUpperCase()));
                }
                if (arg.matchesEnum(EntityEffect.class)) {
                    scriptEntry.addObject("effect", EntityEffect.valueOf(arg.getValue().toUpperCase()));
                }
                if (NMSHandler.animationHelper.hasEntityAnimation(arg.getValue())) {
                    scriptEntry.addObject("nms_animation", arg.getValue());
                }
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
        if (!scriptEntry.hasObject("effect") && !scriptEntry.hasObject("animation") && !scriptEntry.hasObject("nms_animation")) {
            throw new InvalidArgumentsException("Must specify a valid animation!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        List<PlayerTag> forPlayers = (List<PlayerTag>) scriptEntry.getObject("for");
        PlayerAnimation animation = scriptEntry.hasObject("animation") ? (PlayerAnimation) scriptEntry.getObject("animation") : null;
        EntityEffect effect = scriptEntry.hasObject("effect") ? (EntityEffect) scriptEntry.getObject("effect") : null;
        String nmsAnimation = scriptEntry.hasObject("nms_animation") ? (String) scriptEntry.getObject("nms_animation") : null;
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    (animation != null ? db("animation", animation.name()) : effect != null ? db("effect", effect.name()) : db("animation", nmsAnimation)),
                    db("entities", entities), db("for", forPlayers));
        }
        for (EntityTag entity : entities) {
            if (entity.isSpawned()) {
                try {
                    if (animation != null && entity.getBukkitEntity() instanceof Player) {
                        Player player = (Player) entity.getBukkitEntity();
                        animation.play(player);
                    }
                    else if (effect != null) {
                        if (forPlayers != null) {
                            for (PlayerTag player : forPlayers) {
                                NMSHandler.packetHelper.sendEntityEffect(player.getPlayerEntity(), entity.getBukkitEntity(), effect);
                            }
                        }
                        else {
                            entity.getBukkitEntity().playEffect(effect);
                        }
                    }
                    else if (nmsAnimation != null) {
                        EntityAnimation entityAnimation = NMSHandler.animationHelper.getEntityAnimation(nmsAnimation);
                        entityAnimation.play(entity.getBukkitEntity());
                    }
                    else {
                        Debug.echoError("No way to play the given animation on entity '" + entity + "'");
                    }
                }
                catch (Exception e) {
                    Debug.echoError(scriptEntry, "Error playing that animation!");
                    Debug.echoError(e);
                }
            }
        }
    }
}
