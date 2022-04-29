package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class CastCommand extends AbstractCommand {

    public CastCommand() {
        setName("cast");
        setSyntax("cast [<effect>] (remove) (duration:<value>) (amplifier:<#>) (<entity>|...) (no_ambient) (hide_particles) (no_icon)");
        setRequiredArguments(1, 8);
        isProcedural = false;
    }

    // <--[command]
    // @Name Cast
    // @Syntax cast [<effect>] (remove) (duration:<value>) (amplifier:<#>) (<entity>|...) (no_ambient) (hide_particles) (no_icon)
    // @Required 1
    // @Maximum 8
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
    // To cast an effect with a duration which displays as '**:**' or 'infinite' use a duration of '1639s' (1639 seconds) or greater.
    // While it may display as infinite, it will still wear off.
    //
    // The amplifier is how many levels to *add* over the normal level 1.
    // If you don't specify an amplifier level, it defaults to 1, meaning an effect of level 2 (this is for historical compatibility reasons).
    // Specify "amplifier:0" to have no amplifier applied (ie effect level 1).
    //
    // If no player is specified, the command will target the player.
    // If no player is present, the command will target the NPC. If an NPC is not present, there will be an error!
    //
    // Optionally, specify "no_ambient" to hide some translucent additional particles, while still rendering the main particles.
    // "Ambient" effects in vanilla came from a beacon, while non-ambient came from a potion.
    //
    // Optionally, specify "hide_particles" to remove the particle effects entirely.
    //
    // Optionally, specify "no_icon" to hide the effect icon in the corner of your screen.
    //
    // @Tags
    // <EntityTag.has_effect[<effect>]>
    // <server.potion_effect_types>
    // <EntityTag.list_effects>
    //
    // @Usage
    // Use to cast a level 1 effect onto the player.
    // - cast speed amplifier:0
    //
    // @Usage
    // Use to cast an effect onto the player for 120 seconds with an amplifier of 3 (effect level 4).
    // - cast jump d:120 amplifier:3
    //
    // @Usage
    // Use to remove an effect from the player.
    // - if <player.has_effect[jump]>:
    //   - cast jump remove <player>
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        for (PotionEffectType effect : PotionEffectType.values()) { // Not an enum for some reason
            tab.add(effect.getName());
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("remove")
                    && arg.matches("remove", "cancel")) {
                scriptEntry.addObject("remove", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("ambient")
                    && arg.matches("no_ambient")) {
                scriptEntry.addObject("ambient", new ElementTag(false));
            }
            else if (!scriptEntry.hasObject("show_particles")
                    && arg.matches("hide_particles")) {
                scriptEntry.addObject("show_particles", new ElementTag(false));
            }
            else if (!scriptEntry.hasObject("show_icon")
                    && arg.matches("no_icon")) {
                scriptEntry.addObject("show_icon", new ElementTag(false));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("duration", "d")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("amplifier")
                    && arg.matchesPrefix("power", "p", "amplifier", "a")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("amplifier", arg.asElement());
            }
            else if (!scriptEntry.hasObject("effect")
                    && PotionEffectType.getByName(arg.asElement().asString()) != null) {
                scriptEntry.addObject("effect", PotionEffectType.getByName(arg.asElement().asString()));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            scriptEntry.defaultObject("entities", Utilities.entryDefaultEntityList(scriptEntry, true));
        }
        if (!scriptEntry.hasObject("effect")) {
            throw new InvalidArgumentsException("Must specify a valid PotionType!");
        }
        scriptEntry.defaultObject("duration", new DurationTag(60));
        scriptEntry.defaultObject("amplifier", new ElementTag(1));
        scriptEntry.defaultObject("remove", new ElementTag(false));
        scriptEntry.defaultObject("show_particles", new ElementTag(true));
        scriptEntry.defaultObject("ambient", new ElementTag(true));
        scriptEntry.defaultObject("show_icon", new ElementTag(true));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        if (entities == null) {
            throw new InvalidArgumentsRuntimeException("Missing entity target input");
        }
        PotionEffectType effect = (PotionEffectType) scriptEntry.getObject("effect");
        int amplifier = scriptEntry.getElement("amplifier").asInt();
        DurationTag duration = scriptEntry.getObjectTag("duration");
        boolean remove = scriptEntry.getElement("remove").asBoolean();
        ElementTag showParticles = scriptEntry.getElement("show_particles");
        ElementTag ambient = scriptEntry.getElement("ambient");
        ElementTag showIcon = scriptEntry.getElement("show_icon");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("targets", entities), db("effect", effect.getName()), db("amplifier", amplifier), duration, ambient, showParticles, showIcon);
        }
        boolean amb = ambient.asBoolean();
        boolean showP = showParticles.asBoolean();
        boolean icon = showIcon.asBoolean();
        for (EntityTag entity : entities) {
            if (entity.getLivingEntity().hasPotionEffect(effect)) {
                entity.getLivingEntity().removePotionEffect(effect);
            }
            if (remove) {
                continue;
            }
            PotionEffect potion = new PotionEffect(effect, duration.getTicksAsInt(), amplifier, amb, showP, icon);
            if (!potion.apply(entity.getLivingEntity())) {
                Debug.echoError(scriptEntry, "Bukkit was unable to apply '" + potion.getType().getName() + "' to '" + entity + "'.");
            }
        }
    }
}
