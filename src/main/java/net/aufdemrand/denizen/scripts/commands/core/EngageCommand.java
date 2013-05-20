package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.arguments.Duration;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;

import java.util.HashMap;
import java.util.Map;

/**
 * Sets an NPC to ENGAGED in the Denizen Engage List. 
 * When ENGAGEd, a Denizen will not interact with a Player until DISENGAGEd (or timed out).
 *
 * @author Jeremy Schroeder
 */

public class EngageCommand extends AbstractCommand {

	/* ENGAGE (# of Seconds) (NPCID:#)*/

	/* Arguments: [] - Required, () - Optional 
	 * (DURATION:#) Will automatically DISENGAGE after specified amount of seconds.
	 * 		If not set, the Denizen will remain ENGAGEd until a DISENGAGE command is
	 *   	used, or the Denizen config.yml engage_timeout_in_seconds setting. 
	 * (NPCID:#) Changes the Denizen affected to the Citizens2 NPCID specified
	 */

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        // Inialize require fields
        Duration duration = null;

        // Check for NPC
        if (scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);

        // Parse arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesInteger(arg) || aH.matchesDuration(arg)) {
                duration = aH.getDurationFrom(arg);

            } else if (aH.matchesArg("NOW", arg)) {
                // Catch 'NOW' argument... it's already been parsed.

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT);
        }

        // If no duration set, assume 15 seconds.
        if (duration == null)
            duration = new Duration(15d);

        // Stash objects
        scriptEntry.addObject("duration", duration);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Duration duration = (Duration) scriptEntry.getObject("duration");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("NPC", scriptEntry.getNPC().toString())
                        + duration.debug());

        if (duration.getSecondsAsInt() > 0)
            setEngaged(scriptEntry.getNPC().getCitizen(), duration.getSecondsAsInt());
        else
            setEngaged(scriptEntry.getNPC().getCitizen(), true);

    }

    /*
     * Engaged NPCs cannot interact with Players
     */
    private static Map<NPC, Long> currentlyEngaged = new HashMap<NPC, Long>();

    /**
     * Checks if the dNPC is ENGAGED. Engaged NPCs do not respond to
     * Player interaction.
     *
     * @param npc
     * 		the Denizen NPC being checked
     * @return
     *  	if the dNPC is currently engaged
     */
    public static boolean getEngaged(NPC npc) {
        if (currentlyEngaged.containsKey(npc))
            if (currentlyEngaged.get(npc) > System.currentTimeMillis())
                return true;
        return false;
    }

    /**
     * Sets a dNPC's ENGAGED status. Engaged NPCs do not respond to Player
     * interaction. Note: Denizen NPC will automatically disengage after the
     * engage_timeout_in_seconds which is set in the Denizen config.yml.
     *
     * @param npc
     * 		the dNPC affected
     * @param engaged
     * 		true sets the dNPC engaged, false sets the dNPC as disengaged
     */
    public static void setEngaged(NPC npc, boolean engaged) {
        if (engaged) currentlyEngaged.put(npc, System.currentTimeMillis()
                + (long) (Duration.valueOf(Settings.EngageTimeoutInSeconds()).getSeconds()) * 1000 );
        if (!engaged) currentlyEngaged.remove(npc);
    }

    /**
     * Sets a dNPC as ENGAGED for a specific amount of seconds. Engaged NPCs do not
     * respond to Player interaction. If the NPC is previously engaged, using this will
     * over-ride the previously set duration.
     *
     * @param npc
     * 		the dNPC to set as engaged
     * @param duration
     * 		the number of seconds to engage the dNPC
     */
    public static void setEngaged(NPC npc, int duration) {
        currentlyEngaged.put(npc, System.currentTimeMillis() + duration * 1000 );
    }

}