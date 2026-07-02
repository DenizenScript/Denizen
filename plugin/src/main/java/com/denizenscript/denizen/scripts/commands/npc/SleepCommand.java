package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.ArgDefaultNull;
import com.denizenscript.denizencore.scripts.commands.generator.ArgLinear;
import com.denizenscript.denizencore.scripts.commands.generator.ArgName;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.trait.SleepTrait;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class SleepCommand extends AbstractCommand {

    public SleepCommand() {
        setName("sleep");
        setSyntax("sleep (<location>)");
        setRequiredArguments(0, 1);
        isProcedural = false;
        autoCompile();
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

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("location") @ArgLinear @ArgDefaultNull LocationTag location) {
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsRuntimeException("This command requires a linked NPC!");
        }
        NPCTag npc = Utilities.getEntryNPC(scriptEntry);
        if (npc.getEntityType() != EntityType.PLAYER && !(npc.getEntity() instanceof Villager)) {
            Debug.echoError("Only Player or villager type NPCs can sit!");
            return;
        }
        SleepTrait trait = npc.getCitizen().getOrAddTrait(SleepTrait.class);
        if (location != null) {
            trait.setSleeping(location);
        }
        else {
            trait.setSleeping(npc.getLocation());
        }
    }
}
