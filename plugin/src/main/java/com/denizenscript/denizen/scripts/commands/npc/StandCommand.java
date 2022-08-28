package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.npc.traits.SleepingTrait;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.SittingTrait;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.*;

public class StandCommand extends AbstractCommand {

    public StandCommand() {
        setName("stand");
        setSyntax("stand");
        setRequiredArguments(0, 0);
        isProcedural = false;
    }

    // <--[command]
    // @Name Stand
    // @Syntax stand
    // @Required 0
    // @Maximum 0
    // @Plugin Citizens
    // @Short Causes the NPC to stand up from sitting or sleeping.
    // @Group npc
    //
    // @Description
    // Makes the linked NPC stop sitting or sleeping.
    // To make them sit, see <@link command Sit>.
    // To make them sleep, see <@link command Sleep>.
    //
    // @Tags
    // None
    //
    // @Usage
    // Make the linked NPC stand up.
    // - stand
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        //stand should have no additional arguments
        for (Argument arg : scriptEntry) {
            arg.reportUnhandled();
        }
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        NPCTag npc = Utilities.getEntryNPC(scriptEntry);
        if (!(npc.getEntity() instanceof Player || npc.getEntity() instanceof Sittable || npc.getEntity() instanceof Villager)) {
            Debug.echoError("Entities of type " + npc.getEntityType().name() + " cannot sit or sleep.");
            return;
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("npc", Utilities.getEntryNPC(scriptEntry)));
        }
        Entity entity = npc.getEntity();
        if (entity instanceof Sittable) {
            ((Sittable) entity).setSitting(false);
        }
        else {
            if (npc.getCitizen().hasTrait(SittingTrait.class)) {
                SittingTrait trait = npc.getCitizen().getOrAddTrait(SittingTrait.class);
                trait.stand();
                npc.getCitizen().removeTrait(SittingTrait.class);
            }
            if (npc.getCitizen().hasTrait(SleepingTrait.class)) {
                SleepingTrait trait = npc.getCitizen().getOrAddTrait(SleepingTrait.class);
                trait.wakeUp();
                npc.getCitizen().removeTrait(SleepingTrait.class);
            }
        }
    }
}
