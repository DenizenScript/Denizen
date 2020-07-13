package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

public class RenameCommand extends AbstractCommand {

    public RenameCommand() {
        setName("rename");
        setSyntax("rename [<name>] (t:<entity>|...)");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Rename
    // @Syntax rename [<name>] (t:<entity>|...)
    // @Required 1
    // @Maximum 2
    // @Plugin Citizens
    // @Short Renames the linked NPC or list of entities.
    // @Group npc
    //
    // @Description
    // Renames the linked NPC or list of entities.
    // Functions like the '/npc rename' command.
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
    // - rename Bob t:<[some_npc]>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("name")) {
                scriptEntry.addObject("name", arg.asElement());
            }
            else if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("t", "target", "targets")) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("name")) {
            throw new InvalidArgumentsException("Must specify a name!");
        }
        if (!scriptEntry.hasObject("targets")) {
            if (Utilities.getEntryNPC(scriptEntry) == null || !Utilities.getEntryNPC(scriptEntry).isValid()) {
                throw new InvalidArgumentsException("Must have an NPC attached, or specify a list of targets to rename!");
            }
            scriptEntry.addObject("targets", new ListTag(Utilities.getEntryNPC(scriptEntry)));
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        ElementTag name = scriptEntry.getElement("name");
        ListTag targets = scriptEntry.getObjectTag("targets");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), name.debug() + targets.debug());
        }
        for (ObjectTag target : targets.objectForms) {
            NPC npc = target.asType(NPCTag.class, scriptEntry.context).getCitizen();
            Location prev = npc.isSpawned() ? npc.getEntity().getLocation() : null;
            npc.despawn(DespawnReason.PENDING_RESPAWN);
            npc.setName(name.asString().length() > 128 ? name.asString().substring(0, 128) : name.asString());
            if (prev != null) {
                npc.spawn(prev);
            }
        }
    }
}
