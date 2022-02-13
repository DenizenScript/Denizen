package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.scripts.containers.core.ItemScriptHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GiveCommand extends AbstractCommand {

    public GiveCommand() {
        setName("give");
        setSyntax("give [xp/<item>|...] (quantity:<#>) (unlimit_stack_size) (to:<inventory>) (slot:<slot>)");
        setRequiredArguments(1, 5);
        isProcedural = false;
    }

    // <--[command]
    // @Name Give
    // @Syntax give [xp/<item>|...] (quantity:<#>) (unlimit_stack_size) (to:<inventory>) (slot:<slot>)
    // @Required 1
    // @Maximum 5
    // @Short Gives the player an item, xp, or money.
    // @Group item
    //
    // @Description
    // Gives the linked player or inventory items, xp.
    //
    // Optionally specify a slot to put the items into. If the slot is already filled, the next available slot will be used.
    // If the inventory is full, the items will be dropped on the ground at the inventory's location.
    // For player inventories, only the storage contents are valid - to equip armor or an offhand item, use <@link command equip>.
    //
    // Specifying "unlimit_stack_size" will allow an item to stack up to 64. This is useful for stacking items
    // with a max stack size that is less than 64 (for example, most weapon and armor items have a stack size of 1).
    //
    // When giving an item, you can specify any valid inventory as a target. If unspecified, the linked player's inventory will be used.
    //
    // If 'xp' is specified, this will give experience points to the linked player.
    //
    // To give money to a player, use <@link command money>.
    //
    // @Tags
    // <PlayerTag.money>
    //
    // @Usage
    // Use to give XP to the player.
    // - give xp quantity:10
    //
    // @Usage
    // Use to give an item to the player.
    // - give iron_sword
    //
    // @Usage
    // Use to give an item and place it in a specific slot if possible.
    // - give WATCH slot:5
    //
    // @Usage
    // Use to give an item to some other defined player.
    // - give diamond player:<[target]>
    // -->

    enum Type {ITEM, MONEY, EXP}

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        for (Material material : Material.values()) {
            if (material.isItem()) {
                tab.add(material.name());
            }
        }
        tab.add(ItemScriptHelper.item_scripts.keySet());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("quantity")
                    && arg.matchesPrefix("q", "qty", "quantity")
                    && arg.matchesFloat()) {
                if (arg.matchesPrefix("q", "qty")) {
                    Deprecations.qtyTags.warn(scriptEntry);
                }
                scriptEntry.addObject("quantity", arg.asElement());
                scriptEntry.addObject("set_quantity", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("money", "coins")) {
                Deprecations.giveTakeMoney.warn(scriptEntry);
                scriptEntry.addObject("type", Type.MONEY);
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("xp", "exp", "experience")) {
                scriptEntry.addObject("type", Type.EXP);
            }
            else if (!scriptEntry.hasObject("unlimit_stack_size")
                    && arg.matches("unlimit_stack_size")) {
                scriptEntry.addObject("unlimit_stack_size", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("items")
                    && !scriptEntry.hasObject("type")
                    && (arg.matchesArgumentList(ItemTag.class))) {
                scriptEntry.addObject("items", arg.asType(ListTag.class).filter(ItemTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("inventory")
                    && arg.matchesPrefix("t", "to")
                    && arg.matchesArgumentType(InventoryTag.class)) {
                scriptEntry.addObject("inventory", arg.asType(InventoryTag.class));
            }
            else if (!scriptEntry.hasObject("slot")
                    && arg.matchesPrefix("slot")) {
                scriptEntry.addObject("slot", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("type", Type.ITEM)
                .defaultObject("unlimit_stack_size", new ElementTag(false))
                .defaultObject("quantity", new ElementTag(1))
                .defaultObject("slot", new ElementTag(1));
        Type type = (Type) scriptEntry.getObject("type");
        if (type == Type.ITEM) {
            if (!scriptEntry.hasObject("items")) {
                throw new InvalidArgumentsException("Must specify item/items!");
            }
            if (!scriptEntry.hasObject("inventory")) {
                if (!Utilities.entryHasPlayer(scriptEntry)) {
                    throw new InvalidArgumentsException("Must specify an inventory to give to!");
                }
                scriptEntry.addObject("inventory", Utilities.getEntryPlayer(scriptEntry).getInventory());
            }
        }
        else {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsException("Must link a player to give money or XP!");
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag unlimit_stack_size = scriptEntry.getElement("unlimit_stack_size");
        InventoryTag inventory = scriptEntry.getObjectTag("inventory");
        ElementTag quantity = scriptEntry.getElement("quantity");
        Type type = (Type) scriptEntry.getObject("type");
        ElementTag slot = scriptEntry.getElement("slot");
        Object items_object = scriptEntry.getObject("items");
        List<ItemTag> items = null;
        if (items_object != null) {
            items = (List<ItemTag>) items_object;
        }
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("Type", type.name()), inventory, quantity, unlimit_stack_size, db("Items", items), slot);
        }
        switch (type) {
            case MONEY:
                if (Depends.economy != null) {
                    Depends.economy.depositPlayer(Utilities.getEntryPlayer(scriptEntry).getOfflinePlayer(), quantity.asDouble());
                }
                else {
                    Debug.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                }
                break;
            case EXP:
                Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().giveExp(quantity.asInt());
                break;
            case ITEM:
                boolean set_quantity = scriptEntry.hasObject("set_quantity");
                boolean limited = !unlimit_stack_size.asBoolean();
                for (ItemTag item : items) {
                    ItemStack is = item.getItemStack();
                    if (is.getType() == Material.AIR) {
                        Debug.echoError("Cannot give air!");
                        continue;
                    }
                    if (set_quantity) {
                        is.setAmount(quantity.asInt());
                    }
                    int slotId = SlotHelper.nameToIndexFor(slot.asString(), inventory.getInventory().getHolder());
                    if (slotId == -1) {
                        Debug.echoError(scriptEntry, "The input '" + slot.asString() + "' is not a valid slot!");
                        return;
                    }
                    List<ItemStack> leftovers = inventory.addWithLeftovers(slotId, limited, is);
                    if (!leftovers.isEmpty()) {
                        Debug.echoDebug(scriptEntry, "The inventory didn't have enough space, the rest of the items have been placed on the floor.");
                        for (ItemStack leftoverItem : leftovers) {
                            inventory.getLocation().getWorld().dropItem(inventory.getLocation(), leftoverItem);
                        }
                    }
                }
                break;
        }
    }
}
