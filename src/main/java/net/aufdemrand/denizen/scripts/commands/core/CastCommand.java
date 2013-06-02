package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
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
 * @author aufdemrand, Jeebiss
 *
 */
public class CastCommand extends AbstractCommand{

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Required fields
        PotionEffect potionEffect;
        List<LivingEntity> targets = new ArrayList<LivingEntity>();
        Duration duration = null;
        int amplifier = 1;
        PotionEffectType potion = null;

        // Iterate through arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesDuration(arg))
                duration = aH.getDurationFrom(arg);

            else if (aH.matchesValueArg("POWER", arg, ArgumentType.Integer))
                amplifier = aH.getIntegerFrom(arg);

            else if (aH.matchesValueArg("TARGETS, TARGET", arg, ArgumentType.String)) {
                // May be multiple targets, so let's treat this as a potential list.
                // dScript list entries are separated by pipes ('|')
                for (String t : aH.getListFrom(arg)) {
                    // If specifying the linked PLAYER
                    if (aH.getStringFrom(t).equalsIgnoreCase("PLAYER") && scriptEntry.getPlayer() != null) {
                        if (scriptEntry.getPlayer() == null)
                            dB.echoError("Cannot add PLAYER as a target! Attached Player is NULL!");
                        else
                            targets.add(scriptEntry.getPlayer().getPlayerEntity());

                    // If specifying the linked NPC
                    } else if (aH.getStringFrom(t).equalsIgnoreCase("NPC") && scriptEntry.getNPC() != null) {
                        if (scriptEntry.getNPC() == null)
                            dB.echoError("Cannot add NPC as a target! Attached NPC is NULL!");
                        else targets.add(scriptEntry.getNPC().getEntity());

                    // If a saved LivingEntity
                    } else if (aH.getLivingEntityFrom(t) != null) {
                        targets.add(aH.getLivingEntityFrom(t));

                    // If nothing could be made of the object
                    } else  {
                        dB.echoError("Invalid TARGET type or unavailable TARGET object!");
                    }
                }
            }

            // Try to match a PotionEffectType (this argument is prefixless, since it's required)
            else if (potion == null) {
                try { potion = PotionEffectType.getByName(aH.getStringFrom(arg)); }
                catch (Exception e) { dB.echoError("Invalid PotionEffectType!"); }
            }

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Set default duration if not specified
        if (duration == null) duration = new Duration(60);

        // No targets specified, let's use defaults if available
        // Target Player by default
        if (targets.isEmpty() && scriptEntry.getPlayer() != null)
            targets.add(scriptEntry.getPlayer().getPlayerEntity());
        // If no Player, target the NPC by default
        if (targets.isEmpty() && scriptEntry.getNPC() != null)
            targets.add(scriptEntry.getNPC().getEntity());

        // No potion specified? Problem!
        if (potion == null)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "PotionType");
        // Still no targets? Problem!
        if (targets.isEmpty())
            throw new InvalidArgumentsException("No valid target(s)! Perhaps you specified a non-existing Player or NPCID?");

        // Denizen durations are in seconds, PotionEffect duration is in ticks, so a little bit of math is necessary
        potionEffect = new PotionEffect(potion, duration.getTicksAsInt(), amplifier);

        // Save items in the scriptEntry
        scriptEntry.addObject("potion", potionEffect);
        scriptEntry.addObject("targets", targets);
        scriptEntry.addObject("duration", duration);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        List<LivingEntity> targets = (List<LivingEntity>) scriptEntry.getObject("targets");
        PotionEffect potion = (PotionEffect) scriptEntry.getObject("potion");
        Duration duration = (Duration) scriptEntry.getObject("duration");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Target(s)", targets.toString())
                        + aH.debugObj("Potion", potion.getType().getName())
                        + aH.debugObj("Amplifier", String.valueOf(potion.getAmplifier()))
                        + duration.debug());

        // Apply the PotionEffect to the targets!
        for (LivingEntity target : targets)
            if (!potion.apply(target))
                dB.echoError("Bukkit was unable to apply '" + potion.getType().getName() + "' to '" + target.toString() + "'.");
    }

}