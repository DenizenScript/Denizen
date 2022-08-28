package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HealCommand extends AbstractCommand {

    public HealCommand() {
        setName("heal");
        setSyntax("heal (<#.#>) ({player}/<entity>|...)");
        setRequiredArguments(0, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Heal
    // @Syntax heal (<#.#>) ({player}/<entity>|...)
    // @Required 0
    // @Maximum 2
    // @Short Heals the player or list of entities.
    // @Group entity
    //
    // @Description
    // This command heals a player, list of players, entity or list of entities.
    //
    // If no amount is specified it will heal the specified player(s)/entity(s) fully.
    //
    // @Tags
    // <EntityTag.health>
    // <EntityTag.health_max>
    //
    // @Usage
    // Use to fully heal a player.
    // - heal
    //
    // @Usage
    // Use to heal a player 5 hearts.
    // - heal 10
    //
    // @Usage
    // Use to heal a defined player fully.
    // - heal <[someplayer]>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_targets = false;

        for (Argument arg : scriptEntry) {

            if (!scriptEntry.hasObject("amount")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("amount", arg.asElement());
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentType(ListTag.class)) {
                // Entity arg
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
                specified_targets = true;
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentType(EntityTag.class)) {
                // Entity arg
                scriptEntry.addObject("entities", Collections.singletonList(arg.asType(EntityTag.class)));
                specified_targets = true;
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("amount")) {
            scriptEntry.addObject("amount", new ElementTag(-1));
        }

        if (!specified_targets) {
            List<EntityTag> entities = new ArrayList<>();
            if (Utilities.getEntryPlayer(scriptEntry) != null) {
                entities.add(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity());
            }
            else if (Utilities.getEntryNPC(scriptEntry) != null) {
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
        if (entities == null) {
            return;
        }
        ElementTag amountelement = scriptEntry.getElement("amount");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), amountelement, db("entities", entities));
        }
        if (amountelement.asDouble() == -1) {
            for (EntityTag entity : entities) {
                if (entity.isLivingEntity()) {
                    entity.getLivingEntity().setHealth(entity.getLivingEntity().getMaxHealth());
                }
            }
        }
        else {
            double amount = amountelement.asDouble();
            for (EntityTag entity : entities) {
                if (entity.getLivingEntity().getHealth() + amount < entity.getLivingEntity().getMaxHealth()) {
                    entity.getLivingEntity().setHealth(entity.getLivingEntity().getHealth() + amount);
                }
                else {
                    entity.getLivingEntity().setHealth(entity.getLivingEntity().getMaxHealth());
                }
            }
        }
    }
}
