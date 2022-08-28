package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.npc.traits.SleepingTrait;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class SleepCommand extends AbstractCommand {

    public SleepCommand() {
        setName("sleep");
        setSyntax("sleep (<location>)");
        setRequiredArguments(0, 1);
        isProcedural = false;
    }

    // <--[command]
    // @Name sleep
    // @Syntax sleep (<location>)
    // @Required 0
    // @Maximum 1
    // @Plugin Citizens
    // @Short Causes the NPC to sleep. To make them wake up, see <@link command Stand>.
    // @Group npc
    //
    // @Description
    // Makes the linked NPC sleep at the specified location.
    // Use <@link command Stand> to make the NPC wake back up.
    //
    // @Tags
    // <NPCTag.is_sleeping>
    //
    // @Usage
    // Make the linked NPC sleep at the player's cursor location.
    // - sleep <player.cursor_on>
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesArgumentType(LocationTag.class)
                    && !scriptEntry.hasObject("location")) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag location = scriptEntry.getObjectTag("location");
        NPCTag npc = Utilities.getEntryNPC(scriptEntry);
        if (npc.getEntityType() != EntityType.PLAYER && !(npc.getEntity() instanceof Villager)) {
            Debug.echoError("Only Player or villager type NPCs can sit!");
            return;
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), npc, location);
        }
        SleepingTrait trait = npc.getCitizen().getOrAddTrait(SleepingTrait.class);
        if (location != null) {
            trait.toSleep(location);
        }
        else {
            trait.toSleep();
        }
        if (!trait.isSleeping()) {
            npc.getCitizen().removeTrait(SleepingTrait.class);
        }
    }
}
