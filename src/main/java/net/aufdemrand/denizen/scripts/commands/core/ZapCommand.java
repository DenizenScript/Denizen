package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.runnables.Runnable2;
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

		// Initialize required fields
        String script = scriptEntry.getScript();
        String step = null;
        double duration = -1;

		for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesScript(arg)) {
                script = aH.getStringFrom(arg);
                dB.echoDebug(Messages.DEBUG_SET_SCRIPT, script);

            } else if (aH.matchesDuration(arg)) {
                duration = aH.getSecondsFrom(arg);
                dB.echoDebug(Messages.DEBUG_SET_DURATION, String.valueOf(duration));

            } else if (aH.matchesValueArg("STEP", arg, aH.ArgumentType.String)
                    || aH.matchesInteger(arg)) {
                step = aH.getStringFrom(arg);
                dB.echoDebug(Messages.DEBUG_SET_STEP, step);

			} else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

        // Add objects to scriptEntry to use in execute()
        scriptEntry.addObject("script", script);
		scriptEntry.addObject("step", step);
        scriptEntry.addObject("duration", duration);
	}

    //"PlayerName,ScriptName", TaskID
    private static Map<String, Integer> durations = new ConcurrentHashMap<String, Integer>();

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        String script = (String) scriptEntry.getObject("script");
        String step = (String) scriptEntry.getObject("step");
        double duration = (Double) scriptEntry.getObject("duration");

        // Let's get the current step for reference.
        String currentStep = denizen.getScriptEngine().getScriptHelper().getCurrentStep(scriptEntry.getPlayer(), script, false);

        // Special-case for backwards compatibility... ability to use ZAP to count up steps.
        if (step == null) {
            // Okay, no step was identified.. that means we should count up, ie. if currentStep = 1, new step should = 2
            // If the currentStep is a number, increment it. If not, set it to '1' so it can be incremented next time.
            if (aH.matchesInteger(currentStep)) {
                step = String.valueOf(aH.getIntegerFrom(currentStep) + 1);
            } else step = "1";
        }

        // If the durationsMap already contains an entry for this player/script combination, cancel the task since it's probably not
        // desired to change back anymore if another ZAP for this script is taking place.
        if (durations.containsKey(scriptEntry.getPlayer().getName() + "," + script.toUpperCase()))
            try {
                denizen.getServer().getScheduler().cancelTask(durations.get(scriptEntry.getPlayer().getName() + "," + script.toUpperCase()));
            } catch (Exception e) { }

        // One last thing... check for duration.
        if (duration > 0) {
            // If a DURATION is specified, the currentStep should be remembered and restored after the duration.
            scriptEntry.addObject("step", currentStep);
            // And let's take away the duration that was set to avoid a re-duration inception-ion-ion-ion-ion... ;)
            scriptEntry.addObject("duration", -1d);

            // Now let's add a delayed task to set it back after the duration

            // Delays are in ticks, so let's multiply our duration (which is in seconds) by 20. 20 ticks per second.
            long delay = (long) (duration * 20);

            // Set delayed task and put id in a map (for cancellations with CANCELTASK [id])
            dB.echoDebug(Messages.DEBUG_SETTING_DELAYED_TASK, "RESET ZAP for '" + script + "'");
            durations.put(scriptEntry.getPlayer().getName() + "," + script.toUpperCase(),
                    denizen.getServer().getScheduler().scheduleSyncDelayedTask(denizen,
                    new Runnable2<String, ScriptEntry>(script, scriptEntry) {
                        @Override
                        public void run(String script, ScriptEntry scriptEntry) {
                            dB.log(Messages.DEBUG_RUNNING_DELAYED_TASK, "RESET ZAP for '" + script + "'");
                            try {
                                durations.remove(scriptEntry.getPlayer().getName() + "," + script.toUpperCase());
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
        denizen.getSaves().set("Players." + scriptEntry.getPlayer().getName() + "." + script.toUpperCase() + "." + "Current Step", step);
    }

}
