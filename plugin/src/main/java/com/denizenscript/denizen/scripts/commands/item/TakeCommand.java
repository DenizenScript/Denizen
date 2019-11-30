package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TakeCommand extends AbstractCommand {

    // <--[command]
    // @Name Take
    // @Syntax take [money/iteminhand/scriptname:<name>/bydisplay:<name>/bycover:<title>|<author>/slot:<slot>/nbt:<key>/<item>|...] (quantity:<#>) (from:<inventory>)
    // @Required 1
    // @Short Takes an item from the player.
    // @Group item
    //
    // @Description
    // Takes items from a player or inventory.
    //
    // If the player or inventory does not have the item being taken, nothing happens.
    //
    // Using 'slot:' will take the items from that specific slot.
    //
    // Using 'nbt:' with a key will take items with the specified NBT key, as set by <@link mechanism ItemTag.nbt>.
    //
    // Using 'iteminhand' will take from the player's held item slot.
    //
    // Using 'scriptname:' will take items with the specified item script name.
    //
    // Using 'bydisplay:' will take items with the specified display name.
    //
    // Using 'bycover:' will take a written book by the specified book title + author pair.
    //
    // If an economy is registered, using 'money' instead of an item will take money from the player's economy balance.
    //
    // If no quantity is specified, exactly 1 item will be taken.
    //
    // Optionally using 'from:' to specify a specific inventory to take from. If not specified, the linked player's inventory will be used.
    //
    // @Tags
    // <PlayerTag.item_in_hand>
    // <PlayerTag.money>
    //
    // @Usage
    // Use to take money from the player
    // - take money quantity:10
    // @Usage
    // Use to take an arrow from the player's enderchest
    // - take arrow from:<player.enderchest>
    // @Usage
    // Use to take the current holding item from the player's hand
    // - take iteminhand
    // @Usage
    // Use to take 5 emeralds from the player's inventory
    // - take emerald quantity:5
    // -->

    private enum Type {MONEY, ITEMINHAND, ITEM, INVENTORY, BYDISPLAY, SLOT, BYCOVER, SCRIPTNAME, NBT}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("type")
                    && arg.matches("money", "coins")) {
                scriptEntry.addObject("type", Type.MONEY);
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("item_in_hand", "iteminhand")) {
                scriptEntry.addObject("type", Type.ITEMINHAND);
            }
            else if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrefix("q", "qty", "quantity")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)) {
                scriptEntry.addObject("qty", arg.asElement());
            }
            else if (!scriptEntry.hasObject("items")
                    && arg.matchesPrefix("bydisplay")
                    && !scriptEntry.hasObject("type")) {
                scriptEntry.addObject("type", Type.BYDISPLAY);
                scriptEntry.addObject("displayname", arg.asElement());
            }
            else if (!scriptEntry.hasObject("items")
                    && arg.matchesPrefix("nbt")
                    && !scriptEntry.hasObject("type")) {
                scriptEntry.addObject("type", Type.NBT);
                scriptEntry.addObject("nbt_key", arg.asElement());
            }
            else if (!scriptEntry.hasObject("type")
                    && !scriptEntry.hasObject("items")
                    && arg.matchesPrefix("bycover")) {
                scriptEntry.addObject("type", Type.BYCOVER);
                scriptEntry.addObject("cover", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("type")
                    && !scriptEntry.hasObject("items")
                    && arg.matchesPrefix("script", "scriptname")) {
                scriptEntry.addObject("type", Type.SCRIPTNAME);
                scriptEntry.addObject("scriptitem", arg.asType(ItemTag.class));
            }
            else if (!scriptEntry.hasObject("slot")
                    && !scriptEntry.hasObject("type")
                    && arg.matchesPrefix("slot")) {
                scriptEntry.addObject("type", Type.SLOT);
                scriptEntry.addObject("slot", arg.asElement());
            }
            else if (!scriptEntry.hasObject("items")
                    && !scriptEntry.hasObject("type")
                    && arg.matchesArgumentList(ItemTag.class)) {
                scriptEntry.addObject("items", ListTag.valueOf(arg.raw_value.replace("item:", "")).filter(ItemTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("inventory")
                    && arg.matchesPrefix("f", "from")
                    && arg.matchesArgumentType(InventoryTag.class)) {
                scriptEntry.addObject("inventory", arg.asType(InventoryTag.class));
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("inventory")) {
                scriptEntry.addObject("type", Type.INVENTORY);
            }
            else if (!scriptEntry.hasObject("inventory")
                    && arg.matches("npc")) {
                scriptEntry.addObject("inventory", Utilities.getEntryNPC(scriptEntry).getDenizenEntity().getInventory());
            }

        }

        scriptEntry.defaultObject("type", Type.ITEM)
                .defaultObject("qty", new ElementTag(1));

        Type type = (Type) scriptEntry.getObject("type");

        if (type != Type.MONEY && scriptEntry.getObject("inventory") == null) {
            scriptEntry.addObject("inventory", Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).getInventory() : null);
        }

        if (!scriptEntry.hasObject("inventory") && type != Type.MONEY) {
            throw new InvalidArgumentsException("Must specify an inventory to take from!");
        }

        if (type == Type.ITEM && scriptEntry.getObject("items") == null) {
            throw new InvalidArgumentsException("Must specify item/items!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        InventoryTag inventory = (InventoryTag) scriptEntry.getObject("inventory");
        ElementTag qty = scriptEntry.getElement("qty");
        ElementTag displayname = scriptEntry.getElement("displayname");
        ItemTag scriptitem = scriptEntry.getObjectTag("scriptitem");
        ElementTag slot = scriptEntry.getElement("slot");
        ListTag titleAuthor = scriptEntry.getObjectTag("cover");
        ElementTag nbtKey = scriptEntry.getElement("nbt_key");
        Type type = (Type) scriptEntry.getObject("type");

        Object items_object = scriptEntry.getObject("items");
        List<ItemTag> items = null;

        if (items_object != null) {
            items = (List<ItemTag>) items_object;
        }

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("Type", type.name())
                            + qty.debug()
                            + (inventory != null ? inventory.debug() : "")
                            + (displayname != null ? displayname.debug() : "")
                            + (scriptitem != null ? scriptitem.debug() : "")
                            + ArgumentHelper.debugObj("Items", items)
                            + (slot != null ? slot.debug() : "")
                            + (nbtKey != null ? nbtKey.debug() : "")
                            + (titleAuthor != null ? titleAuthor.debug() : ""));
        }

        switch (type) {

            case INVENTORY: {
                inventory.clear();
                break;
            }

            case ITEMINHAND: {
                int inHandAmt = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getItemInHand().getAmount();
                int theAmount = (int) qty.asDouble();
                ItemStack newHandItem = new ItemStack(Material.AIR);
                if (theAmount > inHandAmt) {
                    Debug.echoDebug(scriptEntry, "...player did not have enough of the item in hand, so Denizen just took as many as it could. To avoid this situation, use an IF <PLAYER.ITEM_IN_HAND.QTY>.");
                    Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().setItemInHand(newHandItem);
                }
                else {

                    // amount is just right!
                    if (theAmount == inHandAmt) {
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().setItemInHand(newHandItem);
                    }
                    else {
                        // amount is less than what's in hand, need to make a new itemstack of what's left...
                        newHandItem = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getItemInHand().clone();
                        newHandItem.setAmount(inHandAmt - theAmount);
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().setItemInHand(newHandItem);
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().updateInventory();
                    }
                }
                break;
            }

            case MONEY: {
                if (Depends.economy != null) {
                    Depends.economy.withdrawPlayer(Utilities.getEntryPlayer(scriptEntry).getOfflinePlayer(), qty.asDouble());
                }
                else {
                    Debug.echoError(scriptEntry.getResidingQueue(), "No economy loaded! Have you installed Vault and a compatible economy plugin?");
                }
                break;
            }

            case ITEM: {
                for (ItemTag item : items) {
                    ItemStack is = item.getItemStack();
                    is.setAmount(qty.asInt());

                    if (!inventory.removeItem(item, item.getAmount())) {
                        Debug.echoDebug(scriptEntry, "Inventory does not contain at least "
                                + qty.asInt() + " of " + item.getFullString() +
                                "... Taking as much as possible...");
                    }
                }
                break;
            }

            case BYDISPLAY: {
                int found_items = 0;
                if (displayname == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a displayname!");
                    return;
                }
                for (ItemStack it : inventory.getContents()) {
                    if (found_items < qty.asInt() && it != null && it.hasItemMeta() && it.getItemMeta().hasDisplayName() &&
                            it.getItemMeta().getDisplayName().equalsIgnoreCase(displayname.identify())) {
                        int amt = it.getAmount();
                        if (found_items + it.getAmount() <= qty.asInt()) {
                            inventory.getInventory().removeItem(it);
                        }
                        else {
                            it.setAmount(it.getAmount() - (qty.asInt() - found_items));
                            break;
                        }
                        found_items += amt;
                    }
                }
                break;
            }

            case NBT: {
                int found_items = 0;
                if (nbtKey == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify an NBT key!");
                    return;
                }
                for (ItemStack it : inventory.getContents()) {
                    if (found_items < qty.asInt() && it != null && CustomNBT.hasCustomNBT(it, nbtKey.asString(), CustomNBT.KEY_DENIZEN)) {
                        int amt = it.getAmount();
                        if (found_items + it.getAmount() <= qty.asInt()) {
                            inventory.getInventory().removeItem(it);
                        }
                        else {
                            it.setAmount(it.getAmount() - (qty.asInt() - found_items));
                            break;
                        }
                        found_items += amt;
                    }
                }
                break;
            }

            case SCRIPTNAME: {
                if (scriptitem == null || scriptitem.getScriptName() == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a valid script name!");
                    return;
                }

                int script_items = 0;
                for (ItemStack it : inventory.getContents()) {
                    if (script_items < qty.asInt()
                            && it != null
                            && scriptitem.getScriptName().equalsIgnoreCase(new ItemTag(it).getScriptName())) {
                        int amt = it.getAmount();
                        if (script_items + amt <= qty.asInt()) {
                            inventory.getInventory().removeItem(it);
                            script_items += amt;
                        }
                        else {
                            it.setAmount(amt - (qty.asInt() - script_items));
                            break;
                        }
                    }
                }
                break;
            }

            case SLOT: {
                int slotId = SlotHelper.nameToIndex(slot.asString());
                if (slotId == -1) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "The input '" + slot.asString() + "' is not a valid slot!");
                    return;
                }
                inventory.setSlots(slotId, new ItemStack(Material.AIR));
                break;
            }

            case BYCOVER: {
                inventory.removeBook(titleAuthor.get(0),
                        titleAuthor.size() > 1 ? titleAuthor.get(1) : null,
                        qty.asInt());
                break;
            }
        }
    }
}
