package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.List;

public class DropCommand extends AbstractCommand {

    enum Action {DROP_ITEM, DROP_EXP, DROP_ENTITY}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && !arg.matchesPrefix("qty")
                    && arg.matchesArgumentList(dItem.class)) {
                // Item arg
                scriptEntry.addObject("action", new Element(Action.DROP_ITEM.toString()).setPrefix("action"));
                scriptEntry.addObject("item", arg.asType(dList.class).filter(dItem.class));
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
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Element qty = scriptEntry.getElement("qty");
        Element action = scriptEntry.getElement("action");
        Element speed = scriptEntry.getElement("speed");
        List<dItem> items = (List<dItem>) scriptEntry.getObject("item");
        dEntity entity = (dEntity) scriptEntry.getObject("entity");
        Duration delay = (Duration) scriptEntry.getObject("delay");


        // Report to dB
        dB.report(scriptEntry, getName(),
                action.debug() + location.debug() + qty.debug()
                        + (items != null ? aH.debugList("items", items) : "")
                        + (entity != null ? entity.debug() : "")
                        + (speed != null ? speed.debug() : "")
                        + (delay != null ? delay.debug() : ""));

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
                    ArrayList<Mechanism> mechanisms = new ArrayList<Mechanism>();
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
