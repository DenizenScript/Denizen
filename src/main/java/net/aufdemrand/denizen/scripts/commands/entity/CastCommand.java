package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

/**
 * <p>'Casts' a Bukkit PotionEffectType on a LivingEntity target(s).</p>
 *
 * <br><b>dScript Usage:</b><br>
 * <pre>CAST [PotionEffectType] (TARGET(S):NPC|{PLAYER}|LivingEntity) (DURATION:#) (POWER:#)</pre>
 *
 * <ol><tt>Arguments: [] - Required, () - Optional, {} - Default</ol></tt>
 *
 * <ol><tt>[PotionEffectType]</tt><br>
 *         Uses Bukkit's PotionEffectType for specifying the potion effect to use.
 *         See below for a list of valid PotionEffectTypes.</ol>
 *
 * <ol><tt>(TARGET(S):NPC|{PLAYER}|ENTITY.entity|NPC.npcid|PLAYER.player_name)</tt><br>
 *         Optional. Defaults to the attached Player. Can use a dScript list format
 *         to specify multiple targets. The recipient of the PotionEffectType. </ol>
 *
 * <ol><tt>(DURATION:#{60s})</tt><br>
 *         Optional. Number of seconds that the PotionEffectType lasts. If not specified,
 *         assumed 60 seconds.</ol>
 *
 * <ol><tt>(POWER:#{1})</tt><br>
 *         Optional. A higher amplifier means the potion effect happens more often over
 *         its duration and in some cases has more effect on its target. Usually effective
 *         between 1-3.</ol>
 *
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - CAST NIGHT_VISION DURATION:60 <br>
 *  - CAST WITHER TARGET:NPC NPCID:&#60;FLAG.P:enemy_NPCID> CASTER:PLAYER <br>
 *  - CAST REGENERATION DURATION:10 POWER:3 <br>
 *  - CAST CONFUSION TARGET:NPC.25 DURATION:60
 * </ol></tt>
 *
 * <br><b>Extended Usage:</b><br>
 * <ol><tt>
 *  Script: <br>
 *  - ^ENGAGE NOW DURATION:10 <br>
 *  - ^LOOKCLOSE TOGGLE:TRUE DURATION:10 <br>
 *  - CHAT 'The night-time is blinding around here. Allow me to give you sight.' <br>
 *  - WAIT 2 <br>
 *  - ^ANIMATE ANIMATION:ARM_SWING <br>
 *  - CAST NIGHT_VISION DURATION:360 <br>
 *  - NARRATE 'You can see through the night!' <br>
 * </ol></tt>
 *
 * @author aufdemrand, Jeebiss, Morphan1
 *
 */
public class CastCommand extends AbstractCommand{

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("remove")
                && arg.matches("remove, cancel"))
                scriptEntry.addObject("remove", Element.TRUE);

            else if (!scriptEntry.hasObject("duration")
                     && arg.matchesPrefix("duration, d")
                     && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else if (!scriptEntry.hasObject("amplifier")
                     && arg.matchesPrefix("power, p, amplifier, a")
                     && arg.matchesPrimitive(aH.PrimitiveType.Double))
                scriptEntry.addObject("amplifier", arg.asElement());

            else if (!scriptEntry.hasObject("effect")
                     && PotionEffectType.getByName(arg.asElement().asString()) != null) {
                scriptEntry.addObject("effect", PotionEffectType.getByName(arg.asElement().asString()));
            }

            else if (!scriptEntry.hasObject("entities")
                     && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", ((dList) arg.asType(dList.class)).filter(dEntity.class));

            }

            else dB.echoError(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);

        }

        // No targets specified, let's use defaults if available
        scriptEntry.defaultObject("entities", (scriptEntry.hasPlayer() ? Arrays.asList(scriptEntry.getPlayer().getDenizenEntity()) : null),
                (scriptEntry.hasNPC() ? Arrays.asList(scriptEntry.getNPC().getDenizenEntity()) : null));

        // No potion specified? Problem!
        if (!scriptEntry.hasObject("effect"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "PotionType");

        scriptEntry.defaultObject("duration", new Duration(60));
        scriptEntry.defaultObject("amplifier", new Element(1));
        scriptEntry.defaultObject("remove", Element.FALSE);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        PotionEffectType effect = (PotionEffectType) scriptEntry.getObject("effect");
        int amplifier = scriptEntry.getElement("amplifier").asInt();
        Duration duration = (Duration) scriptEntry.getObject("duration");
        boolean remove = scriptEntry.getElement("remove").asBoolean();

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Target(s)", entities.toString())
                        + aH.debugObj("Effect", effect.getName())
                        + aH.debugObj("Amplifier", amplifier)
                        + duration.debug());

        // Apply the PotionEffect to the targets!
        for (dEntity entity : entities) {
            if (entity.getLivingEntity().hasPotionEffect(effect))
                entity.getLivingEntity().removePotionEffect(effect);
            if (remove) continue;
            PotionEffect potion = new PotionEffect(effect, duration.getTicksAsInt(), amplifier);
            if (!potion.apply(entity.getLivingEntity()))
                dB.echoError("Bukkit was unable to apply '" + potion.getType().getName() + "' to '" + entity.toString() + "'.");
        }
    }

}
