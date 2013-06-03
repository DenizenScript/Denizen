package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;

/**
 * Drops things into the world.
 *
 * Usage: - drop [item, entity, or 'experience'] (quantity) [location]
 *
 * @author Jeremy Schroeder
 */

public class DropCommand extends AbstractCommand {


    public String getHelp() {
        return  "Drops things into the world. Must specify something to drop," +
                "such as 'experience', an item, or an entity. Must also" +
                "specify a location, and if more than 1, a quantity. \n" +
                " \n" +
                "Use to drop items, even custom item_scripts. \n" +
                "- drop iron_helmet <npc.location> \n" +
                "- drop butter 5 <notable.location[churn]> \n" +
                "Use to reward the player with some experience. \n" +
                "- drop experience 1000 <player.location> \n" +
                "Use to drop entities, such as boats or minecarts. \n" +
                "- drop e@boat <player.flag[dock_location]>";
    }


    enum Action { DROP_ITEM, DROP_EXP, DROP_ENTITY }


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && !arg.matchesPrefix("qty")
                    && arg.matchesArgumentType(dItem.class)) {
                // Item arg
                scriptEntry.addObject("action", new Element(Action.DROP_ITEM.toString()).setPrefix("action"));
                scriptEntry.addObject("item", arg.asType(dItem.class).setPrefix("item"));  }


            else if (!scriptEntry.hasObject("action")
                    && arg.matchesPrefix("experience, exp, xp"))
                // Experience arg
                scriptEntry.addObject("action", new Element(Action.DROP_EXP.toString()).setPrefix("action"));


            else if (!scriptEntry.hasObject("action")
                    && arg.matchesArgumentType(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("action", new Element(Action.DROP_ENTITY.toString()).setPrefix("action"));
                scriptEntry.addObject("entity", arg.asType(dEntity.class).setPrefix("entity"));  }


            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class).setPrefix("location"));


            else if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                // Quantity arg
                scriptEntry.addObject("qty", arg.asElement().setPrefix("qty"));


            else dB.echoError("Unhandled argument: '" + arg.raw_value + "'");
        }

        // Make sure all required arguments are met

        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify something to drop!");

        if (!scriptEntry.hasObject("location"))
            if (scriptEntry.getPlayer() != null && scriptEntry.getPlayer().isOnline()) {
                scriptEntry.addObject("location", scriptEntry.getPlayer().getLocation().setPrefix("location"));
                dB.echoDebug("Did not specify a location, assuming Player's location.");

            } else throw new InvalidArgumentsException("Must specify a location!");

        if (!scriptEntry.hasObject("qty"))
            scriptEntry.addObject("qty", Element.valueOf("1").setPrefix("qty"));

        // Okay!
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Element qty = (Element) scriptEntry.getObject("qty");
        Element action = (Element) scriptEntry.getObject("action");
        dItem item = (dItem) scriptEntry.getObject("item");
        dEntity entity = (dEntity) scriptEntry.getObject("entity");


        // Report to dB
        dB.report(getName(),
                action.debug() + location.debug() + qty.debug()
                        + (Action.valueOf(action.asString()) == Action.DROP_ITEM ? item.debug() : "")
                        + (Action.valueOf(action.asString()) == Action.DROP_ENTITY ? entity.debug() : ""));


        // Do the drop
        switch (Action.valueOf(action.asString())) {
            case DROP_EXP:
                ((ExperienceOrb) location.getWorld()
                        .spawnEntity(location, EntityType.EXPERIENCE_ORB))
                        .setExperience(qty.asInt());
                break;

            case DROP_ITEM:
                if (qty.asInt() > 1 && item.isUnique())
                    dB.echoDebug("Cannot drop multiples of this item because it is Unique!");
                // TODO: Make a dItem specific 'drop/give' to better keep track of it, like dEntity.
                for (int x = 0; x < qty.asInt(); x++)
                    location.getWorld()
                            .dropItemNaturally(location, item.getItemStack());
                break;

            case DROP_ENTITY:
                if (qty.asInt() > 1 && entity.isUnique())
                    dB.echoDebug("Cannot drop multiples of this entity because it is Unique!");
                for (int x = 0; x < qty.asInt(); x++)
                    entity.spawnAt(location);
                break;
        }

        // Okay!
    }
}