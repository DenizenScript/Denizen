package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.List;

public class DropCommand extends AbstractCommand {

    // <--[command]
    // @Name Drop
    // @Syntax drop [<entity_type>/xp/<item>|...] (<location>) (quantity:<#>) (speed:<#.#>) (delay:<duration>)
    // @Required 1
    // @Short Drops an item, entity, or experience orb on a location.
    // @Group world
    //
    // @Description
    // To drop an item, just specify a valid item object. To drop
    // an entity, specify a generic entity object. Drop can also reward players
    // with experience orbs by using the 'xp' argument.
    //
    // For all three usages, you can optionally specify an integer with 'quantity:'
    // prefix to drop multiple items/entities/xp.
    // For items, you can add 'speed:' to modify the launch velocity.
    // You can also add 'delay:' to set the pickup delay of the item.
    //
    // @Tags
    // <e@entity.item>
    // <entry[saveName].dropped_entities> returns a list of entities that were dropped.
    //
    // @Usage
    // Use to drop some loot around the player.
    // - drop i@gold_nugget <cuboid[cu@<player.location.add[-2,-2,-2]>|<player.location.add[2,2,2]>].spawnable_blocks.random>
    //
    // @Usage
    // Use to reward a player with 500 xp.
    // - drop xp quantity:500 <player.location>
    //
    // @Usage
    // Use to drop a nasty surprise (exploding TNT).
    // - drop e@primed_tnt <player.location>
    //
    // @Usage
    // Use to drop an item with a pickup delay at the player's location.
    // - drop i@diamond_sword <player.location> delay:20s
    // -->

    enum Action {DROP_ITEM, DROP_EXP, DROP_ENTITY}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("action")
                    && !arg.matchesPrefix("qty")
                    && arg.matchesArgumentList(dItem.class)) {
                // Item arg
                scriptEntry.addObject("action", new Element(Action.DROP_ITEM.toString()).setPrefix("action"));
                scriptEntry.addObject("item", arg.asType(dList.class).filter(dItem.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("action")
                    && arg.matches("experience", "exp", "xp"))
            // Experience arg
            {
                scriptEntry.addObject("action", new Element(Action.DROP_EXP.toString()).setPrefix("action"));
            }
            else if (!scriptEntry.hasObject("action")
                    && arg.matchesArgumentType(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("action", new Element(Action.DROP_ENTITY.toString()).setPrefix("action"));
                scriptEntry.addObject("entity", arg.asType(dEntity.class).setPrefix("entity"));
            }
            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
            // Location arg
            {
                scriptEntry.addObject("location", arg.asType(dLocation.class).setPrefix("location"));
            }
            else if (!scriptEntry.hasObject("speed")
                    && arg.matchesPrefix("speed")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("speed", arg.asElement());
            }
            else if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
            // Quantity arg
            {
                scriptEntry.addObject("qty", arg.asElement().setPrefix("qty"));
            }
            else if (!scriptEntry.hasObject("delay") && arg.matchesArgumentType(Duration.class)
                    && arg.matchesPrefix("delay", "d")) {
                scriptEntry.addObject("delay", arg.asType(Duration.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Make sure all required arguments are met

        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Must specify something to drop!");
        }

        if (!scriptEntry.hasObject("location")) {
            if (((BukkitScriptEntryData) scriptEntry.entryData).getPlayer() != null && ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().isOnline()) {
                scriptEntry.addObject("location", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getLocation().setPrefix("location"));
                dB.echoDebug(scriptEntry, "Did not specify a location, assuming Player's location.");

            }
            else {
                throw new InvalidArgumentsException("Must specify a location!");
            }
        }

        if (!scriptEntry.hasObject("qty")) {
            scriptEntry.addObject("qty", Element.valueOf("1").setPrefix("qty"));
        }

        // Okay!
    }


    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Get objects
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Element qty = scriptEntry.getElement("qty");
        Element action = scriptEntry.getElement("action");
        Element speed = scriptEntry.getElement("speed");
        List<dItem> items = (List<dItem>) scriptEntry.getObject("item");
        dEntity entity = (dEntity) scriptEntry.getObject("entity");
        Duration delay = (Duration) scriptEntry.getObject("delay");


        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    action.debug() + location.debug() + qty.debug()
                            + (items != null ? aH.debugList("items", items) : "")
                            + (entity != null ? entity.debug() : "")
                            + (speed != null ? speed.debug() : "")
                            + (delay != null ? delay.debug() : ""));
        }

        dList entityList = new dList();

        // Do the drop
        switch (Action.valueOf(action.asString())) {
            case DROP_EXP:
                dEntity orb = new dEntity(location.getWorld().spawnEntity(location, EntityType.EXPERIENCE_ORB));
                ((ExperienceOrb) orb.getBukkitEntity()).setExperience(qty.asInt());
                entityList.add(orb.toString());
                break;

            case DROP_ITEM:
                for (dItem item : items) {
                    if (item.getMaterial().getMaterial() == Material.AIR) {
                        continue;
                    }
                    if (qty.asInt() > 1 && item.isUnique()) {
                        dB.echoDebug(scriptEntry, "Cannot drop multiples of this item because it is Unique!");
                    }
                    for (int x = 0; x < qty.asInt(); x++) {
                        dEntity e = new dEntity(location.getWorld().dropItem(location, item.getItemStack()));
                        if (e.isValid()) {
                            e.setVelocity(e.getVelocity().multiply(speed != null ? speed.asDouble() : 1d));
                            if (delay != null) {
                                ((Item) e.getBukkitEntity()).setPickupDelay(delay.getTicksAsInt());
                            }
                        }
                        entityList.add(e.toString());
                    }
                }
                break;

            case DROP_ENTITY:
                if (qty.asInt() > 1 && entity.isUnique()) {
                    dB.echoDebug(scriptEntry, "Cannot drop multiples of this entity because it is Unique!");
                    entity.spawnAt(location);
                    entityList.add(entity.toString());
                    break;
                }
                for (int x = 0; x < qty.asInt(); x++) {
                    ArrayList<Mechanism> mechanisms = new ArrayList<>();
                    for (Mechanism mechanism : entity.getWaitingMechanisms()) {
                        mechanisms.add(new Mechanism(new Element(mechanism.getName()), mechanism.getValue()));
                    }
                    dEntity ent = new dEntity(entity.getEntityType(), mechanisms);
                    ent.spawnAt(location);
                    entityList.add(ent.toString());
                }
                break;
        }

        // Add entities to context so that the specific entities dropped can be fetched.
        scriptEntry.addObject("dropped_entities", entityList);

    }
}
