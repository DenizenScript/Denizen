package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class TakeCommand extends AbstractCommand {

    public TakeCommand() {
        setName("take");
        setSyntax("take [iteminhand/cursoritem/bydisplay:<name>/bycover:<title>|<author>/slot:<slot>/flagged:<flag>/item:<matcher>] (quantity:<#>) (from:<inventory>)");
        setRequiredArguments(1, 3);
        isProcedural = false;
    }

    // <--[command]
    // @Name Take
    // @Syntax take [iteminhand/cursoritem/bydisplay:<name>/bycover:<title>|<author>/slot:<slot>/flagged:<flag>/item:<matcher>] (quantity:<#>) (from:<inventory>)
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
    // Using 'flagged:' with a flag name will take items with the specified flag name, see <@link language flag system>.
    //
    // Using 'iteminhand' will take from the player's held item slot.
    //
    // Using 'cursoritem' will take from the player's held cursor item (as in, one that's actively being picked up and moved in an inventory screen).
    //
    // Using 'bydisplay:' will take items with the specified display name.
    //
    // Using 'bycover:' will take a written book by the specified book title + author pair.
    //
    // Using 'raw_exact:' (Intentionally undocumented) will compare all raw details of an item exactly. This is almost always a bad idea to use. DO NOT USE.
    //
    // Using 'item:' will take items that match an advanced item matcher, using the system behind <@link language Advanced Object Matching>.
    //
    // Flagged, Slot, ByDisplay, and Raw_Exact, all take a list as input to take multiple different item types at once.
    //
    // If no quantity is specified, exactly 1 item will be taken.
    //
    // Specifying a raw item without any matching method is considered unreliable and should be avoided.
    //
    // Optionally using 'from:' to specify a specific inventory to take from. If not specified, the linked player's inventory will be used.
    //
    // The options 'iteminhand' and 'cursoritem' require a linked player and will ignore the 'from:' inventory.
    //
    // To take xp from a player, use <@link command experience>.
    // To take money from a player, use <@link command money>.
    //
    // @Tags
    // <PlayerTag.item_in_hand>
    //
    // @Usage
    // Use to take an arrow from the player's enderchest
    // - take item:arrow from:<player.enderchest>
    //
    // @Usage
    // Use to take the current holding item from the player's hand
    // - take iteminhand
    //
    // @Usage
    // Use to take 5 emeralds from the player's inventory
    // - take item:emerald quantity:5
    // -->

    private enum Type {MONEY, XP, ITEMINHAND, CURSORITEM, ITEM, BYDISPLAY, SLOT, BYCOVER, SCRIPTNAME, NBT, MATERIAL, FLAGGED, RAWEXACT, MATCHER}

    public static HashSet<Type> requiresPlayerTypes = new HashSet<>(Arrays.asList(Type.XP, Type.MONEY, Type.ITEMINHAND, Type.CURSORITEM));

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("scriptname:", ItemScriptHelper.item_scripts.keySet());
        if (tab.arg.startsWith("material:")) {
            for (Material material : Material.values()) {
                if (material.isItem()) {
                    tab.add("material:" + material.name());
                }
            }
        }
        else if (tab.arg.startsWith("item:")) {
            for (Material material : Material.values()) {
                if (material.isItem()) {
                    tab.add("item:" + material.name());
                }
            }
            tab.addWithPrefix("item:", ItemScriptHelper.item_scripts.keySet());
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("type")
                    && arg.matches("money", "coins")) {
                BukkitImplDeprecations.takeMoney.warn(scriptEntry);
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
                    && arg.matches("cursoritem", "cursor_item")) {
                scriptEntry.addObject("type", Type.CURSORITEM);
            }
            else if (!scriptEntry.hasObject("quantity")
                    && arg.matchesPrefix("q", "qty", "quantity")
                    && arg.matchesFloat()) {
                if (arg.matchesPrefix("q", "qty")) {
                    BukkitImplDeprecations.qtyTags.warn(scriptEntry);
                }
                scriptEntry.addObject("quantity", arg.asElement());
            }
            else if (!scriptEntry.hasObject("items")
                    && arg.matchesPrefix("bydisplay")
                    && !scriptEntry.hasObject("type")) {
                scriptEntry.addObject("type", Type.BYDISPLAY);
                scriptEntry.addObject("displayname", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("items")
                    && arg.matchesPrefix("nbt")
                    && !scriptEntry.hasObject("type")) {
                BukkitImplDeprecations.itemNbt.warn(scriptEntry);
                scriptEntry.addObject("type", Type.NBT);
                scriptEntry.addObject("nbt_key", arg.asElement());
            }
            else if (!scriptEntry.hasObject("items")
                    && arg.matchesPrefix("flagged")
                    && !scriptEntry.hasObject("type")) {
                scriptEntry.addObject("type", Type.FLAGGED);
                scriptEntry.addObject("flag_name", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("type")
                    && !scriptEntry.hasObject("items")
                    && arg.matchesPrefix("bycover")) {
                scriptEntry.addObject("type", Type.BYCOVER);
                scriptEntry.addObject("cover", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("type")
                    && !scriptEntry.hasObject("items")
                    && arg.matchesPrefix("item")) {
                scriptEntry.addObject("type", Type.MATCHER);
                scriptEntry.addObject("matcher_text", arg.asElement());
            }
            else if (!scriptEntry.hasObject("type")
                    && !scriptEntry.hasObject("items")
                    && arg.matchesPrefix("material")) {
                BukkitImplDeprecations.takeRawItems.warn(scriptEntry);
                scriptEntry.addObject("type", Type.MATERIAL);
                scriptEntry.addObject("material", arg.asType(ListTag.class).filter(MaterialTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("type")
                    && !scriptEntry.hasObject("items")
                    && arg.matchesPrefix("script", "scriptname")) {
                BukkitImplDeprecations.takeRawItems.warn(scriptEntry);
                scriptEntry.addObject("type", Type.SCRIPTNAME);
                scriptEntry.addObject("scriptitem", arg.asType(ListTag.class).filter(ItemTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("type")
                    && !scriptEntry.hasObject("items")
                    && arg.matchesPrefix("raw_exact")) {
                scriptEntry.addObject("type", Type.RAWEXACT);
                scriptEntry.addObject("items", arg.asType(ListTag.class).filter(ItemTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("slot")
                    && !scriptEntry.hasObject("type")
                    && arg.matchesPrefix("slot")) {
                scriptEntry.addObject("type", Type.SLOT);
                scriptEntry.addObject("slot", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("items")
                    && !scriptEntry.hasObject("type")
                    && arg.matchesArgumentList(ItemTag.class)) {
                BukkitImplDeprecations.takeRawItems.warn(scriptEntry);
                scriptEntry.addObject("items", arg.asType(ListTag.class).filter(ItemTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("inventory")
                    && arg.matchesPrefix("f", "from")
                    && arg.matchesArgumentType(InventoryTag.class)) {
                scriptEntry.addObject("inventory", arg.asType(InventoryTag.class));
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
        if (requiresPlayerTypes.contains(type) && !Utilities.entryHasPlayer(scriptEntry)) {
            throw new InvalidArgumentsException("Cannot take '" + type.name() + "' without a linked player.");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        InventoryTag inventory = scriptEntry.getObjectTag("inventory");
        ElementTag quantity = scriptEntry.getElement("quantity");
        ListTag displayNameList = scriptEntry.getObjectTag("displayname");
        List<ItemTag> scriptItemList = scriptEntry.getObjectTag("scriptitem");
        ListTag slotList = scriptEntry.getObjectTag("slot");
        ListTag titleAuthor = scriptEntry.getObjectTag("cover");
        ElementTag nbtKey = scriptEntry.getElement("nbt_key");
        ElementTag matcherText = scriptEntry.getElement("matcher_text");
        ListTag flagList = scriptEntry.getObjectTag("flag_name");
        List<MaterialTag> materialList = scriptEntry.getObjectTag("material");
        Type type = (Type) scriptEntry.getObject("type");
        List<ItemTag> items = scriptEntry.getObjectTag("items");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("Type", type.name()), quantity, inventory, displayNameList, db("scriptname", scriptItemList),
                    db("Items", items), slotList, nbtKey, flagList, matcherText, db("material",  materialList), titleAuthor);
        }
        switch (type) {
            case ITEMINHAND: {
                Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
                int inHandAmt = player.getEquipment().getItemInMainHand().getAmount();
                int theAmount = (int) quantity.asDouble();
                ItemStack newHandItem = new ItemStack(Material.AIR);
                if (theAmount > inHandAmt) {
                    Debug.echoDebug(scriptEntry, "...player did not have enough of the item in hand, taking all...");
                    player.getEquipment().setItemInMainHand(newHandItem);
                }
                else {
                    // amount is just right!
                    if (theAmount == inHandAmt) {
                        player.getEquipment().setItemInMainHand(newHandItem);
                    }
                    else {
                        // amount is less than what's in hand, need to make a new itemstack of what's left...
                        newHandItem = player.getEquipment().getItemInMainHand().clone();
                        newHandItem.setAmount(inHandAmt - theAmount);
                        player.getEquipment().setItemInMainHand(newHandItem);
                        player.updateInventory();
                    }
                }
                break;
            }
            case CURSORITEM: {
                Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
                int currentAmount = player.getItemOnCursor().getAmount();
                int takeAmount = (int) quantity.asDouble();
                ItemStack newItem = new ItemStack(Material.AIR);
                if (takeAmount > currentAmount) {
                    Debug.echoDebug(scriptEntry, "...player did not have enough of the item on cursor, taking all...");
                    player.setItemOnCursor(newItem);
                }
                else {
                    if (takeAmount == currentAmount) {
                        player.setItemOnCursor(newItem);
                    }
                    else {
                        newItem = player.getItemOnCursor().clone();
                        newItem.setAmount(currentAmount - takeAmount);
                        player.setItemOnCursor(newItem);
                        player.updateInventory();
                    }
                }
                break;
            }
            case MONEY: {
                if (Depends.economy == null) {
                    Debug.echoError(scriptEntry, "No economy loaded! Have you installed Vault and a compatible economy plugin?");
                    return;
                }
                Depends.economy.withdrawPlayer(Utilities.getEntryPlayer(scriptEntry).getOfflinePlayer(), quantity.asDouble());
                break;
            }
            case XP: {
                BukkitImplDeprecations.takeExperience.warn(scriptEntry);
                Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().giveExp(-quantity.asInt());
                break;
            }
            case RAWEXACT: {
                if (items == null) {
                    Debug.echoError(scriptEntry, "Must specify item/items!");
                    return;
                }
                for (ItemTag targetItem : items) {
                    takeByMatcher(inventory, (item) -> targetItem.matchesRawExact(new ItemTag(item)), quantity.asInt());
                }
                break;
            }
            case ITEM: {
                if (items == null) {
                    Debug.echoError(scriptEntry, "Must specify item/items!");
                    return;
                }
                for (ItemTag item : items) {
                    ItemStack is = item.getItemStack().clone();
                    is.setAmount(quantity.asInt());
                    if (!removeItem(inventory.getInventory(), item, item.getAmount())) {
                        Debug.echoDebug(scriptEntry, "Inventory does not contain at least " + quantity.asInt() + " of " + item.identify() + "... Taking all...");
                    }
                }
                break;
            }
            case BYDISPLAY: {
                if (displayNameList == null) {
                    Debug.echoError(scriptEntry, "Must specify a displayname!");
                    return;
                }
                for (String name : displayNameList) {
                    takeByMatcher(inventory, (item) -> item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                            item.getItemMeta().getDisplayName().equalsIgnoreCase(name), quantity.asInt());
                }
                break;
            }
            case BYCOVER: {
                if (titleAuthor == null) {
                    Debug.echoError(scriptEntry, "Must specify a cover!");
                    return;
                }
                takeByMatcher(inventory, (item) -> item.hasItemMeta() && item.getItemMeta() instanceof BookMeta
                                && equalOrNull(titleAuthor.get(0), ((BookMeta) item.getItemMeta()).getTitle())
                                && (titleAuthor.size() == 1 || equalOrNull(titleAuthor.get(1), ((BookMeta) item.getItemMeta()).getAuthor())), quantity.asInt());
                break;
            }
            case FLAGGED: {
                if (flagList == null) {
                    Debug.echoError(scriptEntry, "Must specify a flag name!");
                    return;
                }
                for (String flag : flagList) {
                    takeByMatcher(inventory, (item) -> new ItemTag(item).getFlagTracker().hasFlag(flag), quantity.asInt());
                }
                break;
            }
            case NBT: {
                if (nbtKey == null) {
                    Debug.echoError(scriptEntry, "Must specify an NBT key!");
                    return;
                }
                takeByMatcher(inventory, (item) -> CustomNBT.hasCustomNBT(item, nbtKey.asString(), CustomNBT.KEY_DENIZEN), quantity.asInt());
                break;
            }
            case SCRIPTNAME: {
                if (scriptItemList == null) {
                    Debug.echoError(scriptEntry, "Must specify a valid script name!");
                    return;
                }
                for (ItemTag scriptedItem : scriptItemList) {
                    String script = scriptedItem.getScriptName();
                    if (script == null) {
                        Debug.echoError(scriptEntry, "Item '" + scriptedItem.debuggable() + "' is not a scripted item, cannot take by scriptname.");
                        continue;
                    }
                    takeByMatcher(inventory, (item) -> script.equalsIgnoreCase(new ItemTag(item).getScriptName()), quantity.asInt());
                }
                break;
            }
            case MATERIAL: {
                if (materialList == null) {
                    Debug.echoError(scriptEntry, "Must specify a valid material!");
                    return;
                }
                for (MaterialTag material : materialList) {
                    takeByMatcher(inventory, (item) -> item.getType() == material.getMaterial() && !(new ItemTag(item).isItemscript()), quantity.asInt());
                }
                break;
            }
            case MATCHER: {
                if (matcherText == null) {
                    Debug.echoError(scriptEntry, "Must specify an item matcher!");
                    return;
                }
                takeByMatcher(inventory, (item) -> new ItemTag(item).tryAdvancedMatcher(matcherText.asString()), quantity.asInt());
                break;
            }
            case SLOT: {
                for (String slot : slotList) {
                    int slotId = SlotHelper.nameToIndexFor(slot, inventory.getInventory().getHolder());
                    if (slotId == -1 || slotId >= inventory.getSize()) {
                        Debug.echoError(scriptEntry, "The input '" + slot + "' is not a valid slot!");
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
                }
                break;
            }
        }
    }

    private static boolean equalOrNull(String a, String b) {
        return b == null || a == null || a.equalsIgnoreCase(b);
    }

    public void takeByMatcher(InventoryTag inventory, Function<ItemStack, Boolean> matcher, int quantity) {
        int itemsTaken = 0;
        ItemStack[] contents = inventory.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack it = contents[i];
            if (itemsTaken < quantity
                    && it != null
                    && matcher.apply(it)) {
                int amt = it.getAmount();
                if (itemsTaken + amt <= quantity) {
                    inventory.getInventory().setItem(i, new ItemStack(Material.AIR));
                    itemsTaken += amt;
                }
                else {
                    it.setAmount(amt - (quantity - itemsTaken));
                    inventory.getInventory().setItem(i, it);
                    break;
                }
            }
        }
    }

    public boolean removeItem(Inventory inventory, ItemTag item, int amount) {
        if (item == null) {
            return false;
        }
        item = new ItemTag(item.getItemStack().clone());
        item.setAmount(1);
        String myItem = CoreUtilities.toLowerCase(item.identify());
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack is = inventory.getItem(i);
            if (is == null) {
                continue;
            }
            is = is.clone();
            int count = is.getAmount();
            is.setAmount(1);
            // Note: this double-parsing is intentional, as part of a hotfix for a larger issue
            String newItem = CoreUtilities.toLowerCase(ItemTag.valueOf(new ItemTag(is).identify(), false).identify());
            if (myItem.equals(newItem)) {
                if (count <= amount) {
                    NMSHandler.itemHelper.setInventoryItem(inventory, null, i);
                    amount -= count;
                    if (amount == 0) {
                        return true;
                    }
                }
                else {
                    is.setAmount(count - amount);
                    NMSHandler.itemHelper.setInventoryItem(inventory, is, i);
                    return true;
                }
            }
        }
        return false;
    }
}
