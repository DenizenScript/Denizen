package com.denizenscript.denizen.scripts.commands.item;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.command.TabCompleteHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsRuntimeException;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.inventory.SlotHelper;
import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GiveCommand extends AbstractCommand {

    public GiveCommand() {
        setName("give");
        setSyntax("give [xp/<item>|...] (quantity:<#>) (unlimit_stack_size) (to:<inventory>) (slot:<slot>) (allowed_slots:<slot-matcher>) (ignore_leftovers)");
        setRequiredArguments(1, 7);
        isProcedural = false;
        addRemappedPrefixes("to", "t");
        autoCompile();
    }

    // <--[command]
    // @Name Give
    // @Syntax give [xp/<item>|...] (quantity:<#>) (unlimit_stack_size) (to:<inventory>) (slot:<slot>) (allowed_slots:<slot-matcher>) (ignore_leftovers)
    // @Required 1
    // @Maximum 7
    // @Short Gives the player an item or xp.
    // @Group item
    //
    // @Description
    // Gives the linked player inventory items or xp.
    //
    // Optionally specify a slot to put the items into. If the slot is already filled, the next available slot will be used.
    // If the inventory is full, the items will be dropped on the ground at the inventory's location.
    // For player inventories, only the storage contents are valid - to equip armor or an offhand item, use <@link command equip>.
    //
    // Specifying "unlimit_stack_size" will allow an item to stack up to 64. This is useful for stacking items
    // with a max stack size that is less than 64 (for example, most weapon and armor items have a stack size of 1).
    //
    // When giving an item, you can specify any valid inventory as a target. If unspecified, the linked player's inventory will be used.
    // You may optionally specify a "slot" as any valid slot input per <@link language Slot Inputs> to be the starting slot index.
    // You may optionally specify "allowed_slots" to forcibly restrict the item to only be given to certain specific slots that match a slot-matcher.
    // You may optionally specify "ignore_leftovers" to cause leftover items to be ignored. If not specified, leftover items will be dropped.
    //
    // If 'xp' is specified, this will give experience points to the linked player.
    //
    // To give money to a player, use <@link command money>.
    //
    // @Tags
    // <PlayerTag.xp_level>
    // <PlayerTag.xp_to_next_level>
    // <PlayerTag.inventory>
    // <entry[saveName].leftover_items> returns a ListTag of any item(s) that didn't fit into the inventory.
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

    public enum Type {__ITEM, MONEY, EXP, XP, EXPERIENCE}

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        TabCompleteHelper.tabCompleteItems(tab);
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("quantity") @ArgPrefixed @ArgDefaultText("-1") double quantity,
                                   @ArgName("type") @ArgPrefixed @ArgDefaultText("__item") Type type, // legacy compat
                                   @ArgName("unlimit_stack_size") boolean unlimit_stack_size,
                                   @ArgName("ignore_leftovers") boolean ignore_leftovers,
                                   @ArgName("allowed_slots") @ArgPrefixed @ArgDefaultNull String allowed_slots,
                                   @ArgName("to") @ArgPrefixed @ArgDefaultNull InventoryTag inventory,
                                   @ArgName("slot") @ArgPrefixed @ArgDefaultText("1") String slot,
                                   @ArgName("items") @ArgLinear @ArgSubType(ItemTag.class) List<ItemTag> items) {
        if (type != Type.__ITEM && !Utilities.entryHasPlayer(scriptEntry)) {
            throw new InvalidArgumentsRuntimeException("Must link a player to give money or XP!");
        }
        if (inventory == null) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsRuntimeException("Must specify an inventory to give to!");
            }
            inventory = Utilities.getEntryPlayer(scriptEntry).getInventory();
        }
        if (type == Type.MONEY) {
            BukkitImplDeprecations.giveTakeMoney.warn(scriptEntry);
        }
        ListTag leftoverSave = new ListTag();
        switch (type) {
            case MONEY:
                if (Depends.economy != null) {
                    Depends.economy.depositPlayer(Utilities.getEntryPlayer(scriptEntry).getOfflinePlayer(), quantity < 0 ? 1 : quantity);
                }
                else {
                    Debug.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                }
                break;
            case XP:
            case EXP:
            case EXPERIENCE:
                Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().giveExp((int) (quantity < 0 ? 1 : quantity));
                break;
            case __ITEM:
                boolean limited = !unlimit_stack_size;
                for (ItemTag item : items) {
                    ItemStack is = new ItemStack(item.getItemStack());
                    if (is.getType() == Material.AIR) {
                        Debug.echoError("Cannot give air!");
                        continue;
                    }
                    if (quantity >= 0) {
                        is.setAmount((int) quantity);
                    }
                    int slotId = SlotHelper.nameToIndexFor(slot, inventory.getInventory().getHolder());
                    if (slotId == -1) {
                        Debug.echoError(scriptEntry, "The input '" + slot + "' is not a valid slot!");
                        return;
                    }
                    List<ItemStack> leftovers = inventory.addWithLeftovers(slotId, allowed_slots, limited, is);
                    for (ItemStack extraItem : leftovers) {
                        leftoverSave.addObject(new ItemTag(extraItem));
                    }
                    if (!leftovers.isEmpty() && !ignore_leftovers) {
                        Debug.echoDebug(scriptEntry, "The inventory didn't have enough space, the rest of the items have been placed on the floor.");
                        for (ItemStack leftoverItem : leftovers) {
                            if (inventory.getLocation() == null) {
                                Debug.echoError("Cannot drop extras from failed give command - no inventory location.");
                                return;
                            }
                            inventory.getLocation().getWorld().dropItem(inventory.getLocation(), leftoverItem);
                        }
                    }
                }
                break;
        }
        scriptEntry.saveObject("leftover_items", leftoverSave);
    }
}
