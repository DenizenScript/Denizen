package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.List;

public class KillCommand extends AbstractCommand {

    public KillCommand() {
        setName("kill");
        setSyntax("kill ({player}/<entity>|...)");
        setRequiredArguments(0, 1);
        isProcedural = false;
    }

    // <--[command]
    // @Name Kill
    // @Syntax kill ({player}/<entity>|...)
    // @Required 0
    // @Maximum 1
    // @Short Kills the player or a list of entities.
    // @Group entity
    //
    // @Description
    // Kills a list of entities, or a single entity.
    //
    // If no entities are specified, the command targets the linked player. If there is no linked
    // player, the command targets the linked NPC. If neither is available, the command errors.
    //
    // @Tags
    // <EntityTag.is_spawned>
    //
    // @Usage
    // Use to kill the linked player
    // - kill
    //
    // @Usage
    // Use to kill the linked NPC
    // - kill <npc>
    //
    // @Usage
    // Use to kill all monsters within 10 blocks of the player
    // - kill <player.location.find_entities[monster].within[10]>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            List<EntityTag> entities = new ArrayList<>();
            if (Utilities.entryHasPlayer(scriptEntry)) {
                entities.add(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity());
            }
            else if (Utilities.entryHasNPC(scriptEntry)) {
                entities.add(Utilities.getEntryNPC(scriptEntry).getDenizenEntity());
            }
            else {
                throw new InvalidArgumentsException("No valid target entities found.");
            }
            scriptEntry.addObject("entities", entities);
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("entities", entities));
        }
        for (EntityTag entity : entities) {
            if (!entity.isLivingEntity()) {
                Debug.echoError(scriptEntry.getResidingQueue(), entity + " is not a living entity!");
                continue;
            }
            entity.getLivingEntity().setHealth(0);
        }
    }
}
