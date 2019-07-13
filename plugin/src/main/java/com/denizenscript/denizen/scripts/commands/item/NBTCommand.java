package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.inventory.ItemStack;

public class NBTCommand extends AbstractCommand {

    // <--[command]
    // @Name NBT
    // @Syntax nbt [<item>] [<key>:<value>]
    // @Required 2
    // @Short Sets the value of an item's NBT key.
    // @Group item
    //
    // @Description
    // Edits an NBT key on an item and the edited item to the 'new_item' entry tag.
    // This can be useful for storing hidden information on items.
    //
    // @Tags
    // <entry[saveName].new_item> returns the item resulting from the NBT change.
    //
    // @Usage
    // Use to set a hidden value on an item and give the item to a player.
    // - nbt i@snow_ball "MyCustomNBT.Damage:10" "save:SnowballOfDeath"
    // - give <entry[SnowballOfDeath].new_item>
    //
    // @Usage
    // Use to edit the NBT of a player's item in hand.
    // - nbt <player.item_in_hand> "MyCustomNBT.Owner:<player>" "save:edited"
    // - inventory set "slot:<player.item_in_hand.slot>" "o:<entry[edited].new_item>"
    //
    // @Usage
    // Use to remove an NBT tag from a player's item in hand.
    // - nbt <player.item_in_hand> "MyCustomNBT.Owner:!" "save:item"
    // - inventory set "slot:<player.item_in_hand.slot>" "o:<entry[item].new_item>"
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("key")
                    && arg.raw_value.split(":", 2).length == 2) {
                String[] flagArgs = arg.raw_value.split(":", 2);
                scriptEntry.addObject("key", new Element(flagArgs[0]));
                scriptEntry.addObject("value", new Element(flagArgs[1]));
            }
            else if (!scriptEntry.hasObject("item")
                    && arg.matchesArgumentType(dItem.class)) {
                scriptEntry.addObject("item", arg.asType(dItem.class));
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("item")) {
            throw new InvalidArgumentsException("Must specify item!");
        }

        if (!scriptEntry.hasObject("key") || !scriptEntry.hasObject("value")) {
            throw new InvalidArgumentsException("Must specify key and value!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        dItem item = scriptEntry.getdObject("item");
        Element key = scriptEntry.getElement("key");
        Element value = scriptEntry.getElement("value");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), item.debug() + key.debug() + value.debug());

        }

        ItemStack itemStack = item.getItemStack();

        if (value.asString().equals("!")) {
            itemStack = CustomNBT.removeCustomNBT(itemStack, key.asString(), CustomNBT.KEY_DENIZEN);
        }
        else {
            itemStack = CustomNBT.addCustomNBT(itemStack, key.asString(), value.asString(), CustomNBT.KEY_DENIZEN);
        }

        scriptEntry.addObject("new_item", new dItem(itemStack));
    }
}
