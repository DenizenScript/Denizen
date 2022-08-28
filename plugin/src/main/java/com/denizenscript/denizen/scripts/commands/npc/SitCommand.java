package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.SittingTrait;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.*;

public class SitCommand extends AbstractCommand {

    public SitCommand() {
        setName("sit");
        setSyntax("sit (<location>)");
        setRequiredArguments(0, 1);
        isProcedural = false;
    }

    // <--[command]
    // @Name Sit
    // @Syntax sit (<location>)
    // @Required 0
    // @Maximum 1
    // @Plugin Citizens
    // @Short Causes the NPC to sit. To make them stand, see <@link command Stand>.
    // @Group npc
    //
    // @Description
    // Makes the linked NPC sit at the specified location.
    // Use <@link command Stand> to make the NPC stand up again.
    //
    // @Tags
    // <NPCTag.is_sitting>
    //
    // @Usage
    // Make the linked NPC sit at the player's cursor location.
    // - sit <player.cursor_on>
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
        if (!(npc.getEntity() instanceof Player || npc.getEntity() instanceof Sittable)) {
            Debug.echoError("Entities of type " + npc.getEntityType().getName() + " cannot sit.");
            return;
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), npc, location);
        }
        Entity entity = npc.getEntity();
        if (entity instanceof Sittable) {
            ((Sittable) entity).setSitting(true);
        }
        else {
            SittingTrait trait = npc.getCitizen().getOrAddTrait(SittingTrait.class);
            if (location != null) {
                trait.sit(location);
            }
            else {
                trait.sit();
            }
        }
    }
}
