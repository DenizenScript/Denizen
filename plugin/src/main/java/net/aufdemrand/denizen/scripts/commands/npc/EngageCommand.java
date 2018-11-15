package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.npc.NPC;

import java.util.HashMap;
import java.util.Map;

public class EngageCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Check for NPC
        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

        // Parse arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else {
                arg.reportUnhandled();
            }

        }

        scriptEntry.defaultObject("duration", new Duration(0));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Duration duration = scriptEntry.getdObject("duration");
        dNPC npc = ((BukkitScriptEntryData) scriptEntry.entryData).getNPC();

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), npc.debug() + duration.debug());
        }

        if (duration.getSecondsAsInt() > 0) {
            setEngaged(npc.getCitizen(), duration.getSecondsAsInt());
        }
        else {
            setEngaged(npc.getCitizen(), true);
        }

    }

    /*
     * Engaged NPCs cannot interact with Players
     */
    private static Map<NPC, Long> currentlyEngaged = new HashMap<NPC, Long>();

    /**
     * Checks if the dNPC is ENGAGED. Engaged NPCs do not respond to
     * Player interaction.
     *
     * @param npc the Denizen NPC being checked
     * @return if the dNPC is currently engaged
     */
    public static boolean getEngaged(NPC npc) {
        if (currentlyEngaged.containsKey(npc)) {
            if (currentlyEngaged.get(npc) > System.currentTimeMillis()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a dNPC's ENGAGED status. Engaged NPCs do not respond to Player
     * interaction. Note: Denizen NPC will automatically disengage after the
     * engage_timeout_in_seconds which is set in the Denizen config.yml.
     *
     * @param npc     the dNPC affected
     * @param engaged true sets the dNPC engaged, false sets the dNPC as disengaged
     */
    public static void setEngaged(NPC npc, boolean engaged) {
        if (engaged) {
            currentlyEngaged.put(npc, System.currentTimeMillis()
                    + (long) (Duration.valueOf(Settings.engageTimeoutInSeconds()).getSeconds()) * 1000);
        }
        if (!engaged) {
            currentlyEngaged.remove(npc);
        }
    }

    /**
     * Sets a dNPC as ENGAGED for a specific amount of seconds. Engaged NPCs do not
     * respond to Player interaction. If the NPC is previously engaged, using this will
     * over-ride the previously set duration.
     *
     * @param npc      the dNPC to set as engaged
     * @param duration the number of seconds to engage the dNPC
     */
    public static void setEngaged(NPC npc, int duration) {
        currentlyEngaged.put(npc, System.currentTimeMillis() + duration * 1000);
    }
}
