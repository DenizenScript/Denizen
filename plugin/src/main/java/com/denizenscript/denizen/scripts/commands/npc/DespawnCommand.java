package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.trait.trait.Spawned;

import java.util.Collections;
import java.util.List;

public class DespawnCommand extends AbstractCommand {

    public DespawnCommand() {
        setName("despawn");
        setSyntax("despawn (<npc>|...)");
        setRequiredArguments(0, 1);
        isProcedural = false;
    }

    // <--[command]
    // @Name Despawn
    // @Syntax despawn (<npc>|...)
    // @Plugin Citizens
    // @Required 0
    // @Maximum 1
    // @Plugin Citizens
    // @Short Temporarily despawns the linked NPC or a list of NPCs.
    // @Group npc
    //
    // @Description
    // This command will temporarily despawn either the linked NPC or a list of other NPCs.
    // Despawning means they are no longer visible or interactable, but they still exist and can be respawned.
    //
    // @Tags
    // <NPCTag.is_spawned>
    //
    // @Usage
    // Use to despawn the linked NPC.
    // - despawn
    //
    // @Usage
    // Use to despawn several NPCs.
    // - despawn <npc>|<player.selected_npc>|<[some_npc]>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("npcs")
                    && arg.matchesArgumentList(NPCTag.class)) {
                scriptEntry.addObject("npcs", arg.asType(ListTag.class).filter(NPCTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("npcs")) {
            if (Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("npcs", Collections.singletonList(Utilities.getEntryNPC(scriptEntry)));
            }
            else {
                throw new InvalidArgumentsException("Must specify a valid list of NPCs!");
            }
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        List<NPCTag> npcs = (List<NPCTag>) scriptEntry.getObject("npcs");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("NPCs", npcs));
        }
        for (NPCTag npc : npcs) {
            if (npc.getCitizen().hasTrait(Spawned.class)) {
                npc.getCitizen().getOrAddTrait(Spawned.class).setSpawned(false);
            }
            if (npc.isSpawned()) {
                npc.getCitizen().despawn(DespawnReason.PLUGIN);
            }
        }
    }
}
