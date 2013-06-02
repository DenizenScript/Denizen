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
 * Drops items or experience in a location.
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
                "- drop iron_helmet location:<npc.location> \n" +
                "- drop i@butter qty:5 location:<notable.location[churn]> \n" +
                "Use to reward the player with some experience. \n" +
                "- drop experience qty:1000 location:<player.location> \n" +
                "Use to drop entities, such as boats or minecarts. \n" +
                "- drop e@boat location:<player.flag[dock_location]>";
    }

    enum Action { DROP_ITEM, DROP_EXP, DROP_ENTITY }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("action")
                    && arg.matchesArgumentType(dItem.class)) {
                // dItem arg
                scriptEntry.addObject("action", new Element(Action.DROP_ITEM.toString()).setPrefix("action"));
                scriptEntry.addObject("item", arg.asType(dItem.class).setPrefix("item"));  }


            else if (!scriptEntry.hasObject("action")
                    && arg.matchesPrefix("experience, exp, xp"))
                // Experience arg
                scriptEntry.addObject("action", new Element(Action.DROP_EXP.toString()).setPrefix("action"));


            else if (!scriptEntry.hasObject("action")
                    && arg.matchesArgumentType(dEntity.class)) {
                // Entity arg
                scriptEntry.addObject("action", new Element(Action.DROP_ITEM.toString()).setPrefix("action"));
                scriptEntry.addObject("entity", arg.asType(dEntity.class).setPrefix("entity"));  }


            else if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class))
                // dLocation arg
                scriptEntry.addObject("location", arg.asType(dLocation.class).setPrefix("location"));


            else if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                // Quantity arg
                scriptEntry.addObject("qty", arg.asElement().setPrefix("qty"));


            else dB.echoError("Unhandled argument: '" + arg.raw_value + "'");
        }

        if (!scriptEntry.hasObject("action"))
            throw new InvalidArgumentsException("Must specify something to drop!");

        if (!scriptEntry.hasObject("qty")) scriptEntry.addObject("qty", Element.valueOf("1"));

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

        switch (Action.valueOf(action.asString())) {
            case DROP_EXP:
                ((ExperienceOrb) location.getWorld()
                        .spawnEntity(location, EntityType.EXPERIENCE_ORB))
                        .setExperience(qty.asInt());
                break;

            case DROP_ITEM:
                location.getWorld()
                        .dropItemNaturally(location, item.getItemStack());
                break;

            case DROP_ENTITY:
                entity.spawnAt(location);
                break;
        }

    }

}