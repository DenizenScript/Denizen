package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.objects.MaterialTag;
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
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;

public class TakeCommand extends AbstractCommand {

    public TakeCommand() {
        setName("take");
        setSyntax("take [money/xp/iteminhand/scriptname:<name>/bydisplay:<name>/bycover:<title>|<author>/slot:<slot>/nbt:<key>/material:<material>/<item>|...] (quantity:<#>) (from:<inventory>)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Take
    // @Syntax take [money/xp/iteminhand/iteminoffhand/scriptname:<name>/bydisplay:<name>/bycover:<title>|<author>/slot:<slot>/nbt:<key>/material:<material>/<item>|...] (quantity:<#>) (from:<inventory>)
    // @Required 1
    // @Maximum 3
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
    // Using 'iteminoffhand' will take from the player's off-hand item slot.
    //
    // Using 'scriptname:' will take items with the specified item script name.
    //
    // Using 'bydisplay:' will take items with the specified display name.
    //
    // Using 'bycover:' will take a written book by the specified book title + author pair.
    //
    // Using 'material:' will take items of the specified material type (except for script items).
    //
    // Using 'xp' will take experience from the player.
    //
    // If an economy is registered, using 'money' instead of an item will take money from the player's economy balance.
    //
    // If no quantity is specified, exactly 1 item will be taken.
    //
    // Specifying a raw item without any matching method is considered unreliable and should be avoided.
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
    // - take material:arrow from:<player.enderchest>
    // @Usage
    // Use to take the current holding item from the player's hand
    // - take iteminhand
    // @Usage
    // Use to take 5 emeralds from the player's inventory
    // - take material:emerald quantity:5
    // -->

    private enum Type {MONEY, XP, ITEMINHAND, ITEMINOFFHAND, ITEM, INVENTORY, BYDISPLAY, SLOT, BYCOVER, SCRIPTNAME, NBT, MATERIAL}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("type")
                    && arg.matches("money", "coins")) {
                scriptEntry.addObject("type", Type.MONEY);
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("xp", "exp")) {
                scriptEntry.addObject("type", Type.XP);
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("item_in_hand", "iteminhand")) {
                scriptEntry.addObject("type", Type.ITEMINHAND);
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("item_in_off_hand", "iteminoffhand")) {
                scriptEntry.addObject("type", Type.ITEMINOFFHAND);
            }
            else if (!scriptEntry.hasObject("quantity")
                    && arg.matchesPrefix("q", "qty", "quantity")
                    && arg.matchesFloat()) {
                scriptEntry.addObject("quantity", arg.asElement());
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
                    && arg.matchesPrefix("material")) {
                scriptEntry.addObject("type", Type.MATERIAL);
                scriptEntry.addObject("material", arg.asType(MaterialTag.class));
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
                scriptEntry.addObject("items", ListTag.valueOf(arg.raw_value.replace("item:", ""), scriptEntry.getContext()).filter(ItemTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("inventory")
                    && arg.matchesPrefix("f", "from")
                    && arg.matchesArgumentType(InventoryTag.class)) {
                scriptEntry.addObject("inventory", arg.asType(InventoryTag.class));
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("inventory")) {
                Deprecations.takeCommandInventory.warn(scriptEntry);
                scriptEntry.addObject("type", Type.INVENTORY);
            }
            else if (!scriptEntry.hasObject("inventory")
                    && arg.matches("npc")) {
                Deprecations.takeCommandInventory.warn(scriptEntry);
                scriptEntry.addObject("inventory", Utilities.getEntryNPC(scriptEntry).getDenizenEntity().getInventory());
            }
            else {
                arg.reportUnhandled();
            }
        }

        scriptEntry.defaultObject("type", Type.ITEM)
                .defaultObject("quantity", new ElementTag(1));

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

        InventoryTag inventory = scriptEntry.getObjectTag("inventory");
        ElementTag quantity = scriptEntry.getElement("quantity");
        ElementTag displayname = scriptEntry.getElement("displayname");
        ItemTag scriptitem = scriptEntry.getObjectTag("scriptitem");
        ElementTag slot = scriptEntry.getElement("slot");
        ListTag titleAuthor = scriptEntry.getObjectTag("cover");
        ElementTag nbtKey = scriptEntry.getElement("nbt_key");
        MaterialTag material = scriptEntry.getObjectTag("material");
        Type type = (Type) scriptEntry.getObject("type");

        Object items_object = scriptEntry.getObject("items");
        List<ItemTag> items = null;

        if (items_object != null) {
            items = (List<ItemTag>) items_object;
        }

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), ArgumentHelper.debugObj("Type", type.name())
                            + quantity.debug()
                            + (inventory != null ? inventory.debug() : "")
                            + (displayname != null ? displayname.debug() : "")
                            + (scriptitem != null ? scriptitem.debug() : "")
                            + ArgumentHelper.debugObj("Items", items)
                            + (slot != null ? slot.debug() : "")
                            + (nbtKey != null ? nbtKey.debug() : "")
                            + (material != null ? material.debug() : "")
                            + (titleAuthor != null ? titleAuthor.debug() : ""));
        }

        switch (type) {
            case INVENTORY: {
                inventory.clear();
                break;
            }
            case ITEMINHAND: {
                int inHandAmt = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getEquipment().getItemInMainHand().getAmount();
                int theAmount = (int) quantity.asDouble();
                ItemStack newHandItem = new ItemStack(Material.AIR);
                if (theAmount > inHandAmt) {
                    Debug.echoDebug(scriptEntry, "...player did not have enough of the item in hand, taking all...");
                    Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getEquipment().setItemInMainHand(newHandItem);
                }
                else {
                    // amount is just right!
                    if (theAmount == inHandAmt) {
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getEquipment().setItemInMainHand(newHandItem);
                    }
                    else {
                        // amount is less than what's in hand, need to make a new itemstack of what's left...
                        newHandItem = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getEquipment().getItemInMainHand().clone();
                        newHandItem.setAmount(inHandAmt - theAmount);
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getEquipment().setItemInMainHand(newHandItem);
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().updateInventory();
                    }
                }
                break;
            }
            case ITEMINOFFHAND: {
                int inHandAmt = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getEquipment().getItemInOffHand().getAmount();
                int theAmount = (int) quantity.asDouble();
                ItemStack newHandItem = new ItemStack(Material.AIR);
                if (theAmount > inHandAmt) {
                    Debug.echoDebug(scriptEntry, "...player did not have enough of the item in hand, taking all...");
                    Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getEquipment().setItemInOffHand(newHandItem);
                }
                else {
                    // amount is just right!
                    if (theAmount == inHandAmt) {
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getEquipment().setItemInOffHand(newHandItem);
                    }
                    else {
                        // amount is less than what's in hand, need to make a new itemstack of what's left...
                        newHandItem = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getEquipment().getItemInOffHand().clone();
                        newHandItem.setAmount(inHandAmt - theAmount);
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().getEquipment().setItemInOffHand(newHandItem);
                        Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().updateInventory();
                    }
                }
                break;
            }
            case MONEY: {
                if (Depends.economy == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "No economy loaded! Have you installed Vault and a compatible economy plugin?");
                    return;
                }
                Depends.economy.withdrawPlayer(Utilities.getEntryPlayer(scriptEntry).getOfflinePlayer(), quantity.asDouble());
                break;
            }
            case XP: {
                Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().giveExp(-quantity.asInt());
                break;
            }
            case ITEM: {
                for (ItemTag item : items) {
                    ItemStack is = item.getItemStack();
                    is.setAmount(quantity.asInt());
                    if (!inventory.removeItem(item, item.getAmount())) {
                        Debug.echoDebug(scriptEntry, "Inventory does not contain at least " + quantity.asInt() + " of " + item.identify() + "... Taking all...");
                    }
                }
                break;
            }
            case BYDISPLAY: {
                if (displayname == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a displayname!");
                    return;
                }
                takeByMatcher(inventory, (item) -> item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                        item.getItemMeta().getDisplayName().equalsIgnoreCase(displayname.identify()), quantity.asInt());
                break;
            }
            case BYCOVER: {
                if (titleAuthor == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a cover!");
                    return;
                }
                inventory.removeBook(titleAuthor.get(0), titleAuthor.size() > 1 ? titleAuthor.get(1) : null, quantity.asInt());
                break;
            }
            case NBT: {
                if (nbtKey == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify an NBT key!");
                    return;
                }
                takeByMatcher(inventory, (item) -> CustomNBT.hasCustomNBT(item, nbtKey.asString(), CustomNBT.KEY_DENIZEN), quantity.asInt());
                break;
            }
            case SCRIPTNAME: {
                if (scriptitem == null || scriptitem.getScriptName() == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a valid script name!");
                    return;
                }
                takeByMatcher(inventory, (item) -> scriptitem.getScriptName().equalsIgnoreCase(new ItemTag(item).getScriptName()), quantity.asInt());
                break;
            }
            case MATERIAL: {
                if (material == null) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "Must specify a valid material!");
                    return;
                }
                takeByMatcher(inventory, (item) -> item.getType() == material.getMaterial() && !(new ItemTag(item).isItemscript()), quantity.asInt());
                break;
            }
            case SLOT: {
                int slotId = SlotHelper.nameToIndex(slot.asString());
                if (slotId == -1 || slotId >= inventory.getSize()) {
                    Debug.echoError(scriptEntry.getResidingQueue(), "The input '" + slot.asString() + "' is not a valid slot!");
                    return;
                }
                ItemStack original = inventory.getInventory().getItem(slotId);
                if (original != null && original.getType() != Material.AIR) {
                    if (original.getAmount() > quantity.asInt()) {
                        original.setAmount(original.getAmount() - quantity.asInt());
                        inventory.setSlots(slotId, original);
                    }
                    else {
                        inventory.setSlots(slotId, new ItemStack(Material.AIR));
                    }
                }
                break;
            }
        }
    }

    public void takeByMatcher(InventoryTag inventory, Function<ItemStack, Boolean> matcher, int quantity) {
        int itemsTaken = 0;
        for (ItemStack it : inventory.getContents()) {
            if (itemsTaken < quantity
                    && it != null
                    && matcher.apply(it)) {
                int amt = it.getAmount();
                if (itemsTaken + amt <= quantity) {
                    inventory.getInventory().removeItem(it);
                    itemsTaken += amt;
                }
                else {
                    it.setAmount(amt - (quantity - itemsTaken));
                    break;
                }
            }
        }
    }
}
