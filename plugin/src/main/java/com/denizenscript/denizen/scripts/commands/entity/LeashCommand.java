package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;

import java.util.List;

public class LeashCommand extends AbstractCommand {

    public LeashCommand() {
        setName("leash");
        setSyntax("leash (cancel) [<entity>|...] (holder:<entity>/<location>)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Leash
    // @Syntax leash (cancel) [<entity>|...] (holder:<entity>/<location>)
    // @Required 1
    // @Maximum 3
    // @Short Sticks a leash on target entity, held by a fence or another entity.
    // @Group entity
    //
    // @Description
    // Attaches a leash to the specified entity.
    // The leash may be attached to a fence, or another entity.
    // Players and Player NPCs may not be leashed.
    // Note that releasing a mob from a fence post may leave the leash attached to that fence post.
    //
    // Non-player NPCs can be leashed if '/npc leashable' is enabled.
    //
    // @Tags
    // <EntityTag.is_leashed>
    // <EntityTag.leash_holder>
    //
    // @Usage
    // Use to attach a leash to the player's target.
    // - leash <player.target> holder:<player>
    //
    // @Usage
    // Use to attach the closest cow in 10 blocks to the fence the player is looking at.
    // - leash <player.location.find_entities[cow].within[10].first> holder:<player.cursor_on>
    //
    // @Usage
    // Use to release the target entity.
    // - leash cancel <player.target>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("cancel")
                    && arg.matches("cancel", "stop")) {
                scriptEntry.addObject("cancel", "");
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(EntityTag.class)) {
                scriptEntry.addObject("entities", arg.asType(ListTag.class).filter(EntityTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("holder")
                    && arg.matchesPrefix("holder", "h")) {

                if (arg.matchesArgumentType(EntityTag.class)) {
                    scriptEntry.addObject("holder", arg.asType(EntityTag.class));
                }
                else if (arg.matchesArgumentType(LocationTag.class)) {
                    scriptEntry.addObject("holder", arg.asType(LocationTag.class));
                }
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("entities")) {
            throw new InvalidArgumentsException("Must specify entity/entities!");
        }
        if (!scriptEntry.hasObject("cancel")) {
            scriptEntry.defaultObject("holder", Utilities.entryDefaultEntity(scriptEntry, false));
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        List<EntityTag> entities = (List<EntityTag>) scriptEntry.getObject("entities");
        EntityTag holder = null;
        LocationTag holderLoc = null;
        Entity Holder = null;
        Object holderObject = scriptEntry.getObject("holder");
        if (holderObject instanceof EntityTag) {
            holder = scriptEntry.getObjectTag("holder");
            Holder = holder.getBukkitEntity();
        }
        else if (holderObject instanceof LocationTag) {
            holderLoc = scriptEntry.getObjectTag("holder");
            Material material = holderLoc.getBlock().getType();
            if (material.name().endsWith("_FENCE")) {
                Holder = holderLoc.getWorld().spawn(holderLoc, LeashHitch.class);
            }
            else {
                Debug.echoError(scriptEntry, "Bad holder location specified - only fences are permitted!");
                return;
            }
        }
        boolean cancel = scriptEntry.hasObject("cancel");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (cancel ? db("cancel", "true") : ""), db("entities", entities), db("holder", holder), db("holder", holderLoc));
        }
        for (EntityTag entity : entities) {
            if (entity.isSpawned() && entity.isLivingEntity()) {
                if (cancel) {
                    entity.getLivingEntity().setLeashHolder(null);
                }
                else {
                    entity.getLivingEntity().setLeashHolder(Holder);
                }
            }
        }
    }
}
