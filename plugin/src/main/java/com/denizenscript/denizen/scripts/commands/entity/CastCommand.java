package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.Deprecations;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class CastCommand extends AbstractCommand {

    public CastCommand() {
        setName("cast");
        setSyntax("cast [<effect>] (remove) (duration:<value>) (amplifier:<#>) (<entity>|...) (no_ambient) (hide_particles) (no_icon) (no_clear)");
        setRequiredArguments(1, 9);
        addRemappedPrefixes("duration", "d");
        addRemappedPrefixes("amplifier", "power", "p", "a");
        isProcedural = false;
        autoCompile();
    }

    // <--[command]
    // @Name Cast
    // @Syntax cast [<effect>] (remove) (duration:<value>) (amplifier:<#>) (<entity>|...) (no_ambient) (hide_particles) (no_icon) (no_clear)
    // @Required 1
    // @Maximum 9
    // @Short Casts a potion effect to a list of entities.
    // @Synonyms Potion,Magic
    // @Group entity
    //
    // @Description
    // Casts or removes a potion effect to or from a list of entities.
    //
    // The effect type must be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>.
    //
    // If you don't specify a duration, it defaults to 60 seconds.
    // An infinite duration will apply an infinite duration potion effect, refer to <@link objecttype DurationTag> for more details.
    //
    // The amplifier is how many levels to *add* over the normal level 1.
    // If you don't specify an amplifier level, it defaults to 1, meaning an effect of level 2 (this is for historical compatibility reasons).
    // Specify "amplifier:0" to have no amplifier applied (ie effect level 1).
    //
    // If no entity is specified, the command will target the linked player.
    // If there isn't one, the command will target the linked NPC. If there isn't one either, the command will error.
    //
    // Optionally, specify "no_ambient" to hide some translucent additional particles, while still rendering the main particles.
    // "Ambient" effects in vanilla come from a beacon, while non-ambient come from a potion.
    //
    // Optionally, specify "hide_particles" to remove the particle effects entirely.
    //
    // Optionally, specify "no_icon" to hide the effect icon in the corner of your screen.
    //
    // Optionally use "no_clear" to prevent clearing any previous effect instance before adding the new one.
    //
    // @Tags
    // <EntityTag.has_effect[<effect>]>
    // <server.potion_effect_types>
    // <EntityTag.effects_data>
    //
    // @Usage
    // Use to cast a level 1 effect onto the linked player or NPC for 50 seconds.
    // - cast speed duration:50s amplifier:0
    //
    // @Usage
    // Use to cast an effect onto the linked player or NPC for an infinite duration with an amplifier of 3 (effect level 4).
    // - cast jump duration:infinite amplifier:3
    //
    // @Usage
    // Use to remove an effect from a specific entity.
    // - cast jump remove <[entity]>
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        for (PotionEffectType effect : PotionEffectType.values()) { // Not an enum for some reason
            tab.add(effect.getName());
        }
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("effect") @ArgLinear ObjectTag effectObject,
                                   @ArgName("remove") boolean remove,
                                   @ArgName("cancel") boolean cancel, // "remove" variant
                                   @ArgName("duration") @ArgPrefixed @ArgDefaultText("60s") DurationTag duration,
                                   @ArgName("amplifier") @ArgPrefixed @ArgDefaultText("1") ElementTag amplifier,
                                   @ArgName("entities") @ArgLinear @ArgDefaultNull ObjectTag entitiesObject,
                                   @ArgName("no_ambient") boolean noAmbient,
                                   @ArgName("hide_particles") boolean hideParticles,
                                   @ArgName("no_icon") boolean noIcon,
                                   @ArgName("no_clear") boolean noClear) {
        PotionEffectType effectType = PotionEffectType.getByName(effectObject.toString());
        if (effectType == null) {
            if (entitiesObject != null && (effectType = PotionEffectType.getByName(entitiesObject.toString())) != null) {
                Deprecations.outOfOrderArgs.warn(scriptEntry);
                ObjectTag swapEntities = entitiesObject;
                entitiesObject = effectObject;
                effectObject = swapEntities;
            }
            if (effectType == null) {
                throw new InvalidArgumentsRuntimeException("Invalid potion effect '" + effectObject + "' specified.");
            }
        }
        if (!amplifier.isInt()) {
            throw new InvalidArgumentsRuntimeException("Invalid amplifier '" + amplifier + "' specified: must be a valid number.");
        }
        List<EntityTag> entities = entitiesObject == null ? null : entitiesObject.asType(ListTag.class, scriptEntry.context).filter(EntityTag.class, scriptEntry.context);
        if (entities == null) {
            entities = Utilities.entryDefaultEntityList(scriptEntry, true);
            if (entities == null) {
                throw new InvalidArgumentsRuntimeException("Must specify entities to apply the effect to.");
            }
        }
        remove = remove || cancel;
        PotionEffect potion = null;
        if (!remove) {
            // 32,780+ ticks shows up as infinite before 1.19
            int ticks = duration.getSeconds() != 0d ? duration.getTicksAsInt() : NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) ? PotionEffect.INFINITE_DURATION : Integer.MAX_VALUE;
            potion = new PotionEffect(effectType, ticks, amplifier.asInt(), !noAmbient, !hideParticles, !noIcon);
        }
        for (EntityTag entity : entities) {
            if ((remove || !noClear) && entity.getLivingEntity().hasPotionEffect(effectType)) {
                entity.getLivingEntity().removePotionEffect(effectType);
            }
            if (!remove) {
                if (!entity.getLivingEntity().addPotionEffect(potion)) {
                    Debug.echoError("Bukkit was unable to apply '" + effectType.getName() + "' to '" + entity + "'.");
                }
            }
        }
    }
}
