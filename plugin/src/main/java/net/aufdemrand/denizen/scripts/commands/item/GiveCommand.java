package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.inventory.SlotHelper;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;


public class GiveCommand extends AbstractCommand {

    // <--[command]
    // @Name Give
    // @Syntax give [money/xp/<item>|...] (quantity:<#>) (unlimit_stack_size) (to:<inventory>) (slot:<slot>)
    // @Required 1
    // @Short Gives the player an item, xp, or money.
    // @Group item
    //
    // @Description
    // Gives the linked player or inventory items, xp, or money.
    // Optionally specify a slot to put the items into. If the slot is already filled, the next available slot will be used.
    // If the player's inventory is full, the items will be dropped on the ground at the inventory's location.
    // Specifying "unlimit_stack_size" will allow an item to stack up to 64. This is useful for stacking items
    // with a max stack size that is less than 64 (for example, most weapon and armor items have a stack size of 1).
    // If an economy is registered, specifying money instead of a item will give money to the player's economy.
    //
    // @Tags
    // <p@player.money>
    //
    // @Usage
    // Use to give money to the player.
    // - give money quantity:10
    //
    // @Usage
    // Use to give XP to the player.
    // - give xp quantity:10
    //
    // @Usage
    // Use to give an item to the player.
    // - give i@iron_sword
    //
    // @Usage
    // Use to give an item and place it in a specific slot if possible.
    // - give WATCH slot:5
    // -->

    enum Type {ITEM, MONEY, EXP}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        /* Match arguments to expected variables */
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrefix("q", "qty", "quantity")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("qty", arg.asElement());
                scriptEntry.addObject("set_quantity", new Element(true));
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("money", "coins")) {
                scriptEntry.addObject("type", Type.MONEY);
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("xp", "exp", "experience")) {
                scriptEntry.addObject("type", Type.EXP);
            }
            else if (!scriptEntry.hasObject("engrave")
                    && arg.matches("engrave")) {
                scriptEntry.addObject("engrave", new Element(true));
            }
            else if (!scriptEntry.hasObject("unlimit_stack_size")
                    && arg.matches("unlimit_stack_size")) {
                scriptEntry.addObject("unlimit_stack_size", new Element(true));
            }
            else if (!scriptEntry.hasObject("items")
                    && !scriptEntry.hasObject("type")
                    && (arg.matchesArgumentList(dItem.class) || arg.startsWith("item:"))) {
                scriptEntry.addObject("items", dList.valueOf(arg.raw_value.startsWith("item:") ?
                        arg.raw_value.substring("item:".length()) : arg.raw_value).filter(dItem.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("inventory")
                    && arg.matchesPrefix("t", "to")
                    && arg.matchesArgumentType(dInventory.class)) {
                scriptEntry.addObject("inventory", arg.asType(dInventory.class));
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
                .defaultObject("engrave", new Element(false))
                .defaultObject("unlimit_stack_size", new Element(false))
                .defaultObject("qty", new Element(1))
                .defaultObject("slot", new Element(1));

        Type type = (Type) scriptEntry.getObject("type");

        if (type != Type.MONEY && scriptEntry.getObject("inventory") == null) {
            scriptEntry.addObject("inventory", Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).getInventory() : null);
        }

        if (!scriptEntry.hasObject("inventory") && type != Type.MONEY) {
            throw new InvalidArgumentsException("Must specify an inventory to give to!");
        }

        if (type == Type.ITEM && scriptEntry.getObject("items") == null) {
            throw new InvalidArgumentsException("Must specify item/items!");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element engrave = scriptEntry.getElement("engrave");
        Element unlimit_stack_size = scriptEntry.getElement("unlimit_stack_size");
        dInventory inventory = (dInventory) scriptEntry.getObject("inventory");
        Element qty = scriptEntry.getElement("qty");
        Type type = (Type) scriptEntry.getObject("type");
        Element slot = scriptEntry.getElement("slot");

        Object items_object = scriptEntry.getObject("items");
        List<dItem> items = null;

        if (items_object != null) {
            items = (List<dItem>) items_object;
        }

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    aH.debugObj("Type", type.name())
                            + (inventory != null ? inventory.debug() : "")
                            + aH.debugObj("Quantity", qty.asDouble())
                            + engrave.debug()
                            + unlimit_stack_size.debug()
                            + (items != null ? aH.debugObj("Items", items) : "")
                            + slot.debug());

        }

        switch (type) {

            case MONEY:
                if (Depends.economy != null) {
                    Depends.economy.depositPlayer(Utilities.getEntryPlayer(scriptEntry).getOfflinePlayer(), qty.asDouble());
                }
                else {
                    dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                }
                break;

            case EXP:
                Utilities.getEntryPlayer(scriptEntry).getPlayerEntity().giveExp(qty.asInt());
                break;

            case ITEM:
                boolean set_quantity = scriptEntry.hasObject("set_quantity");
                boolean limited = !unlimit_stack_size.asBoolean();
                for (dItem item : items) {
                    ItemStack is = item.getItemStack();
                    if (is.getType() == Material.AIR) {
                        dB.echoError("Cannot give air!");
                        continue;
                    }
                    if (set_quantity) {
                        is.setAmount(qty.asInt());
                    }
                    // TODO: Should engrave be kept?
                    if (engrave.asBoolean()) {
                        is = CustomNBT.addCustomNBT(item.getItemStack(), "owner", Utilities.getEntryPlayer(scriptEntry).getName(), CustomNBT.KEY_DENIZEN);
                    }
                    int slotId = SlotHelper.nameToIndex(slot.asString());
                    if (slotId == -1) {
                        dB.echoError(scriptEntry.getResidingQueue(), "The input '" + slot.asString() + "' is not a valid slot!");
                        return;
                    }

                    List<ItemStack> leftovers = inventory.addWithLeftovers(slotId, limited, is);

                    if (!leftovers.isEmpty()) {
                        dB.echoDebug(scriptEntry, "The inventory didn't have enough space, the rest of the items have been placed on the floor.");
                        for (ItemStack leftoverItem : leftovers) {
                            inventory.getLocation().getWorld().dropItem(inventory.getLocation(), leftoverItem);
                        }
                    }
                }
                break;
        }
    }
}
