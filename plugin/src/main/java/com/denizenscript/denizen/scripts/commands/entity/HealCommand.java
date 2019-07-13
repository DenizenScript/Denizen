package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HealCommand extends AbstractCommand {

    // <--[command]
    // @Name Heal
    // @Syntax heal (<#.#>) ({player}/<entity>|...)
    // @Required 0
    // @Short Heals the player or list of entities.
    // @Group entity
    //
    // @Description
    // This command heals a player, list of players, entity or list of entities. If no amount is specified it will
    // heal the specified player(s)/entity(s) fully.
    //
    // @Tags
    // <e@entity.health>
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
    // Use to heal a player by the name of Morphan1 fully.
    // - heal p@Morphan1
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        boolean specified_targets = false;

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("amount")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)) {
                scriptEntry.addObject("amount", arg.asElement());
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentType(ListTag.class)) {
                // Entity arg
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(dEntity.class, scriptEntry));
                specified_targets = true;
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentType(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("entities", Arrays.asList(arg.asType(dEntity.class)));
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
            List<dEntity> entities = new ArrayList<>();
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

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) {

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        if (entities == null) {
            return;
        }
        ElementTag amountelement = scriptEntry.getElement("amount");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), amountelement.debug() + ArgumentHelper.debugObj("entities", entities));

        }
        if (amountelement.asDouble() == -1) {
            for (dEntity entity : entities) {
                if (entity.isLivingEntity()) {
                    entity.getLivingEntity().setHealth(entity.getLivingEntity().getMaxHealth());
                }
            }
        }
        else {
            double amount = amountelement.asDouble();
            for (dEntity entity : entities) {
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
