package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.npc.NPC;

import java.util.HashMap;
import java.util.Map;

public class EngageCommand extends AbstractCommand {

    public EngageCommand() {
        setName("engage");
        setSyntax("engage (<duration>)");
        setRequiredArguments(0, 1);
        isProcedural = false;
    }

    // <--[command]
    // @Name Engage
    // @Syntax engage (<duration>)
    // @Required 0
    // @Maximum 1
    // @Plugin Citizens
    // @Short Temporarily disables an NPCs toggled interact script-container triggers.
    // @Group npc
    //
    // @Description
    // Engaging an NPC will temporarily disable any interact script-container triggers. To reverse
    // this behavior, use either the disengage command, or specify a duration in which the engage
    // should timeout. Specifying an engage without a duration will render the NPC engaged until
    // a disengage is used on the NPC. Engaging an NPC affects all players attempting to interact
    // with the NPC.
    //
    // While engaged, all triggers and actions associated with triggers will not 'fire', except
    // the 'on unavailable' assignment script-container action, which will fire for triggers that
    // were enabled previous to the engage command.
    //
    // Engage can be useful when NPCs are carrying out a task that shouldn't be interrupted, or
    // to provide a good way to avoid accidental 'retrigger'.
    //
    // See <@link command Disengage>
    //
    // @Tags
    // <NPCTag.engaged>
    //
    // @Usage
    // Use to make an NPC appear 'busy'.
    // - engage
    // - chat 'Give me a few minutes while I mix you a potion!'
    // - walk <npc.anchor[mixing_station]>
    // - wait 10s
    // - walk <npc.anchor[service_station]>
    // - chat 'Here you go!'
    // - give potion <player>
    // - disengage
    //
    // @Usage
    // Use to avoid 'retrigger'.
    // - engage 5s
    // - take quest_item
    // - flag player finished_quests:->:super_quest
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Check for NPC
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else {
                arg.reportUnhandled();
            }

        }

        scriptEntry.defaultObject("duration", new DurationTag(0));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        DurationTag duration = scriptEntry.getObjectTag("duration");
        NPCTag npc = Utilities.getEntryNPC(scriptEntry);

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), npc.debug() + duration.debug());
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
    private static Map<NPC, Long> currentlyEngaged = new HashMap<>();

    /**
     * Checks if the NPCTag is ENGAGED. Engaged NPCs do not respond to
     * Player interaction.
     *
     * @param npc the Denizen NPC being checked
     * @return if the NPCTag is currently engaged
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
     * Sets a NPCTag's ENGAGED status. Engaged NPCs do not respond to Player
     * interaction. Note: Denizen NPC will automatically disengage after the
     * engage_timeout_in_seconds which is set in the Denizen config.yml.
     *
     * @param npc     the NPCTag affected
     * @param engaged true sets the NPCTag engaged, false sets the NPCTag as disengaged
     */
    public static void setEngaged(NPC npc, boolean engaged) {
        if (engaged) {
            currentlyEngaged.put(npc, System.currentTimeMillis()
                    + (long) (DurationTag.valueOf(Settings.engageTimeoutInSeconds(), CoreUtilities.basicContext).getSeconds()) * 1000);
        }
        if (!engaged) {
            currentlyEngaged.remove(npc);
        }
    }

    /**
     * Sets a NPCTag as ENGAGED for a specific amount of seconds. Engaged NPCs do not
     * respond to Player interaction. If the NPC is previously engaged, using this will
     * over-ride the previously set duration.
     *
     * @param npc      the NPCTag to set as engaged
     * @param duration the number of seconds to engage the NPCTag
     */
    public static void setEngaged(NPC npc, int duration) {
        currentlyEngaged.put(npc, System.currentTimeMillis() + duration * 1000);
    }
}
