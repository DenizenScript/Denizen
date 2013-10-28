package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Changes a Player's current step for a script. Reminder: ZAP does NOT trigger anything. It merely
 * tells Denizen's ScriptEngine which step should be used WHEN interacting.</p>
 *
 * <b>dScript Usage:</b><br>
 * <pre>ZAP [#|STEP:step_name] (SCRIPT:script_name{current_script}) (DURATION:#{0})</pre>
 *
 * <ol><tt>Arguments: [] - Required () - Optional  {} - Default</ol></tt>
 *
 * <ol><tt>[#|STEP:step_name]</tt><br>
 *         The name of the step that should be enabled. If using numbered steps, an plain integer will
 *         suffice.</ol>
 *
 * <ol><tt>(SCRIPT:script_name{current_script})</tt><br>
 *         Specifies which script should be affected. If not specified, the current interact script will
 *         be used.</ol>
 *
 * <ol><tt>(DURATION:#{0})</tt><br>
 *         Optional. If not specified, no duration is used. If specified, after the duration period,
 *         Denizen will automatically reset the step to the original step. Note: If another ZAP command
 *         is utilized for the same Player and Script during a duration period, the reset in progress
 *         is cancelled.</ol>
 *
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - ZAP SCRIPT:OtherScript 6<br>
 *  - ZAP 'STEP:Just for a minute' DURATION:1m<br>
 * </ol></tt>
 *
 * @author Jeremy Schroeder
 */

public class ZapCommand extends AbstractCommand implements Listener{

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            // If the scripter uses the 'script:step' format, handle it.
            if (!scriptEntry.hasObject("script")
                    && !scriptEntry.hasObject("step")
                    && arg.hasPrefix()
                    && arg.getPrefix().matchesArgumentType(dScript.class)) {
                scriptEntry.addObject("script", arg.getPrefix().asType(dScript.class));
                scriptEntry.addObject("step", arg.asElement());
            }

            // If a script is found, use that to ZAP
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class)
                    && !arg.matchesPrefix("step"))
                scriptEntry.addObject("script", arg.asType(dScript.class));

            // Add argument as step
            else if (!scriptEntry.hasObject("step"))
                scriptEntry.addObject("step", arg.asElement());

            // Lastly duration
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else arg.reportUnhandled();
        }

        // Add default script if none was specified.
        scriptEntry.defaultObject("script", scriptEntry.getScript());

        // Check if player is valid
        if (!scriptEntry.hasPlayer() || !scriptEntry.getPlayer().isValid())
            throw new InvalidArgumentsException("Must have player context!");
    }

    //"PlayerName,ScriptName", TaskID
    private static Map<String, Integer> durations = new ConcurrentHashMap<String, Integer>(8, 0.9f, 1);

    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {

        final dScript script = (dScript) scriptEntry.getObject("script");
        Duration duration = (Duration) scriptEntry.getObject("duration");

        dB.report(scriptEntry, getName(), scriptEntry.getPlayer().debug() + script.debug()
                + (scriptEntry.hasObject("step")
                ? scriptEntry.getElement("step").debug() : aH.debugObj("step", "++ (inc)"))
                + (duration != null ? duration.debug() : ""));

        String step = scriptEntry.hasObject("step") ? scriptEntry.getElement("step").asString() : null;

        // Let's get the current step for reference.
        String currentStep = InteractScriptHelper.getCurrentStep(scriptEntry.getPlayer(), script.getName());

        // Special-case for backwards compatibility: ability to use ZAP to count up steps.
        if (step == null) {
            // Okay, no step was identified.. that means we should count up,
            // ie. if currentStep = 1, new step should = 2
            // If the currentStep is a number, increment it. If not, set it
            // to '1' so it can be incremented next time.
            if (aH.matchesInteger(currentStep)) {
                step = String.valueOf(aH.getIntegerFrom(currentStep) + 1);
            } else step = "1";
        }

        // If the durationsMap already contains an entry for this player/script combination,
        // cancel the task since it's probably not desired to change back anymore if another
        // ZAP for this script is taking place.
        if (durations.containsKey(scriptEntry.getPlayer().getName() + "," + script.getName()))
            try {
                denizen.getServer().getScheduler().cancelTask(durations.get(scriptEntry.getPlayer().getName() + "," + script.getName()));
            } catch (Exception e) { }

        // One last thing... check for duration.
        if (duration != null && duration.getSeconds() > 0) {
            // If a DURATION is specified, the currentStep should be remembered and
            // restored after the duration.
            scriptEntry.addObject("step", new Element(currentStep));
            // And let's take away the duration that was set to avoid a re-duration
            // inception-ion-ion-ion-ion... ;)
            scriptEntry.addObject("duration", Duration.ZERO);

            // Now let's add a delayed task to set it back after the duration

            // Delays are in ticks, so let's multiply our duration (which is in seconds) by 20.
            // 20 ticks per second.
            long delay = (long) (duration.getSeconds() * 20);

            // Set delayed task and put id in a map
            dB.log("Setting delayed task 'RESET ZAP' for '" + script.identify() + "'");
            durations.put(scriptEntry.getPlayer().getName() + "," + script.getName(),
                    denizen.getServer().getScheduler().scheduleSyncDelayedTask(denizen,
                            new Runnable() {
                                @Override
                                public void run() {
                                    dB.log("Running delayed task 'RESET ZAP' for '" + script.identify() + "'");
                                    try {
                                        durations.remove(scriptEntry.getPlayer().getName() + "," + script.getName().toUpperCase());
                                        execute(scriptEntry);
                                    } catch (CommandExecutionException e) {
                                        dB.echoError("Could not run delayed task!");
                                        if (dB.showStackTraces) e.printStackTrace();
                                    }
                                }
                            }, delay));
        }

        //
        // FINALLY! ZAP! Change the step in Saves... your step is now ZAPPED!
        // Fun fact: ZAP is named in homage of ZZT-OOPs ZAP command. Google it.
        //
        denizen.getSaves().set("Players." + scriptEntry.getPlayer().getName()
                + ".Scripts." + script.getName().toUpperCase() + "." + "Current Step", step);
    }

}
