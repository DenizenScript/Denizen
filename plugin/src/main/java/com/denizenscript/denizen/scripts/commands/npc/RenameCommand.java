package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

public class RenameCommand extends AbstractCommand {

    public RenameCommand() {
        setName("rename");
        setSyntax("rename [<name>]");
        setRequiredArguments(1, 1);
    }

    // <--[command]
    // @Name Rename
    // @Syntax rename [<name>]
    // @Required 1
    // @Maximum 1
    // @Plugin Citizens
    // @Short Renames the linked NPC.
    // @Group npc
    //
    // @Description
    // Renames the linked NPC.
    // Functions like the '/npc rename' command.
    // NPC names may exceed the 16 character limit of normal Minecraft names.
    //
    // @Tags
    // <NPCTag.name>
    // <NPCTag.nickname>
    //
    // @Usage
    // Use to rename the linked NPC.
    // - rename Bob
    //
    // @Usage
    // Use to rename a different NPC.
    // - rename Bob npc:<[some_npc]>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("name")) {
                scriptEntry.addObject("name", arg.asElement());
            }

        }

        if (!scriptEntry.hasObject("name")) {
            throw new InvalidArgumentsException("Must specify a name!");
        }

        if (Utilities.getEntryNPC(scriptEntry) == null || !Utilities.getEntryNPC(scriptEntry).isValid()) {
            throw new InvalidArgumentsException("Must have a NPC attached!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {

        ElementTag name = scriptEntry.getElement("name");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), name.debug());

        }

        NPC npc = Utilities.getEntryNPC(scriptEntry).getCitizen();

        Location prev = npc.isSpawned() ? npc.getEntity().getLocation() : null;
        npc.despawn(DespawnReason.PENDING_RESPAWN);
        npc.setName(name.asString().length() > 128 ? name.asString().substring(0, 128) : name.asString());
        if (prev != null) {
            npc.spawn(prev);
        }

    }
}
