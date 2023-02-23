package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.command.TabCompleteHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.List;

public class DropCommand extends AbstractCommand {

    public DropCommand() {
        setName("drop");
        setSyntax("drop [<entity_type>/xp/<item>|...] (<location>) (quantity:<#>) (speed:<#.#>) (delay:<duration>)");
        setRequiredArguments(1, 5);
        isProcedural = false;
    }

    // <--[command]
    // @Name Drop
    // @Syntax drop [<entity_type>/xp/<item>|...] (<location>) (quantity:<#>) (speed:<#.#>) (delay:<duration>)
    // @Required 1
    // @Maximum 5
    // @Short Drops an item, entity, or experience orb on a location.
    // @Group world
    //
    // @Description
    // To drop an item, just specify a valid item object. To drop an entity, specify a generic entity object.
    // Drop can also reward players with experience orbs by using the 'xp' argument.
    //
    // For all three usages, you can optionally specify an integer with 'quantity:' prefix to drop multiple items/entities/xp.
    //
    // For items, you can add 'speed:' to modify the launch velocity.
    // You can also add 'delay:' to set the pickup delay of the item.
    //
    // @Tags
    // <entry[saveName].dropped_entities> returns a list of entities that were dropped.
    // <entry[saveName].dropped_entity> returns a single entity that was dropped (if only one).
    // <EntityTag.item>
    // <EntityTag.experience>
    //
    // @Usage
    // Use to drop some loot around the player.
    // - drop gold_nugget <cuboid[<player.location.add[-2,-2,-2]>|<player.location.add[2,2,2]>].spawnable_blocks.random>
    //
    // @Usage
    // Use to reward a player with 500 xp.
    // - drop xp quantity:500 <player.location>
    //
    // @Usage
    // Use to drop a nasty surprise (exploding TNT).
    // - drop primed_tnt <player.location>
    //
    // @Usage
    // Use to drop an item with a pickup delay at the player's location.
    // - drop diamond_sword <player.location> delay:20s
    // -->

    enum Action {DROP_ITEM, DROP_EXP, DROP_ENTITY}

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        TabCompleteHelper.tabCompleteItems(tab);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("action")
                    && arg.matches("experience", "exp", "xp")) {
                scriptEntry.addObject("action", new ElementTag(Action.DROP_EXP.toString()).setPrefix("action"));
            }
            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrefix("speed")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("speed", arg.asElement());
            }
            else if (!scriptEntry.hasObject("delay") && arg.matchesArgumentType(DurationTag.class)
                    && arg.matchesPrefix("delay", "d")) {
                scriptEntry.addObject("delay", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("quantity")
                    && arg.matchesInteger()
                    && arg.matchesPrefix("quantity", "q", "qty", "a", "amt", "amount")) {
                if (arg.matchesPrefix("q", "qty")) {
                    BukkitImplDeprecations.qtyTags.warn(scriptEntry);
                }
                scriptEntry.addObject("quantity", arg.asElement().setPrefix("quantity"));
            }
            else if (!scriptEntry.hasObject("action")
                    && arg.matchesArgumentList(ItemTag.class)) {
                scriptEntry.addObject("action", new ElementTag(Action.DROP_ITEM.toString()).setPrefix("action"));
                scriptEntry.addObject("item", arg.asType(ListTag.class).filter(ItemTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("action")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("action", new ElementTag(Action.DROP_ENTITY.toString()).setPrefix("action"));
                scriptEntry.addObject("entity", arg.asType(EntityTag.class).setPrefix("entity"));
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class).setPrefix("location"));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify something to drop!");
        }
        if (!scriptEntry.hasObject("location")) {
            if (Utilities.getEntryPlayer(scriptEntry) != null && Utilities.getEntryPlayer(scriptEntry).isOnline()) {
                scriptEntry.addObject("location", Utilities.getEntryPlayer(scriptEntry).getLocation().setPrefix("location"));
                Debug.echoDebug(scriptEntry, "Did not specify a location, assuming Player's location.");
            }
            else {
                throw new InvalidArgumentsException("Must specify a location!");
            }
        }
        if (!scriptEntry.hasObject("quantity")) {
            scriptEntry.addObject("quantity", new ElementTag("1"));
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        LocationTag location = scriptEntry.getObjectTag("location");
        ElementTag quantity = scriptEntry.getElement("quantity");
        ElementTag action = scriptEntry.getElement("action");
        ElementTag speed = scriptEntry.getElement("speed");
        List<ItemTag> items = (List<ItemTag>) scriptEntry.getObject("item");
        EntityTag entity = scriptEntry.getObjectTag("entity");
        DurationTag delay = scriptEntry.getObjectTag("delay");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), action, location, quantity, db("items", items), entity, speed, delay);
        }
        ListTag entityList = new ListTag();
        switch (Action.valueOf(action.asString())) {
            case DROP_EXP:
                EntityTag orb = new EntityTag(location.getWorld().spawnEntity(location, EntityType.EXPERIENCE_ORB));
                ((ExperienceOrb) orb.getBukkitEntity()).setExperience(quantity.asInt());
                entityList.addObject(orb);
                break;
            case DROP_ITEM:
                for (ItemTag item : items) {
                    if (item.getMaterial().getMaterial() == Material.AIR) {
                        continue;
                    }
                    if (quantity.asInt() > 1 && item.isUnique()) {
                        Debug.echoDebug(scriptEntry, "Cannot drop multiples of this item because it is Unique!");
                    }
                    for (int x = 0; x < quantity.asInt(); x++) {
                        EntityTag e = new EntityTag(location.getWorld().dropItem(location, item.getItemStack()));
                        if (e.isValid()) {
                            e.setVelocity(e.getVelocity().multiply(speed != null ? speed.asDouble() : 1d));
                            if (delay != null) {
                                ((Item) e.getBukkitEntity()).setPickupDelay(delay.getTicksAsInt());
                            }
                        }
                        entityList.addObject(e);
                    }
                }
                break;
            case DROP_ENTITY:
                if (quantity.asInt() > 1 && entity.isUnique()) {
                    Debug.echoDebug(scriptEntry, "Cannot drop multiples of this entity because it is Unique!");
                    entity.spawnAt(location);
                    entityList.addObject(entity);
                    break;
                }
                for (int x = 0; x < quantity.asInt(); x++) {
                    ArrayList<Mechanism> mechanisms = new ArrayList<>();
                    for (Mechanism mechanism : entity.getWaitingMechanisms()) {
                        mechanisms.add(new Mechanism(mechanism.getName(), mechanism.value, scriptEntry.context));
                    }
                    EntityTag ent = new EntityTag(entity.getEntityType(), mechanisms);
                    ent.spawnAt(location);
                    entityList.addObject(ent);
                }
                break;
        }
        scriptEntry.saveObject("dropped_entities", entityList);
        if (entityList.size() == 1) {
            scriptEntry.saveObject("dropped_entity", entityList.getObject(0));
        }
    }
}
