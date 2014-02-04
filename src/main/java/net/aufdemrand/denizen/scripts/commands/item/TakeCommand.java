package net.aufdemrand.denizen.scripts.commands.item;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;

/* TAKE [MONEY|ITEMINHAND|#(:#)|MATERIAL_TYPE(:#)] (QTY:#) */

/*
 * Arguments: [] - Required, () - Optional
 * [MONEY|ITEMINHAND|[#](:#)|[MATERIAL_TYPE](:#)] specifies what to take.
 *   [MONEY] takes money using your economy.
 *   [ITEMINHAND] takes from the item the Player has in their hand.
 *   [#](:#) takes the item with the specified item ID. Optional
 *     argument (:#) can specify a specific data value.
 *   [MATERIAL_TYPE](:#) takes the item with the specified
 *     bukkit MaterialType. Optional argument (:#) can specify
 *     a specific data value.
 * (QTY:#) specifies quantity. If not specified, assumed 'QTY:1'
 *
 */

public class TakeCommand extends AbstractCommand{

    private enum Type { MONEY, ITEMINHAND, ITEM, INVENTORY, BYDISPLAY, SLOT }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("type")
                    && arg.matches("money, coins"))
                scriptEntry.addObject("type", Type.MONEY);

            else if (!scriptEntry.hasObject("type")
                        && arg.matches("item_in_hand, iteminhand"))
                scriptEntry.addObject("type", Type.ITEMINHAND);

            else if (!scriptEntry.hasObject("qty")
                        && arg.matchesPrefix("q, qty, quantity")
                        && arg.matchesPrimitive(aH.PrimitiveType.Double))
                scriptEntry.addObject("qty", arg.asElement());

            else if (!scriptEntry.hasObject("items")
                    && arg.matchesPrefix("bydisplay")
                    && !scriptEntry.hasObject("type")) {
                scriptEntry.addObject("type", Type.BYDISPLAY);
                scriptEntry.addObject("displayname", arg.asElement());
            }

            else if (!scriptEntry.hasObject("items")
                        && !scriptEntry.hasObject("type")
                        && arg.matchesArgumentList(dItem.class))
                scriptEntry.addObject("items", dList.valueOf(arg.raw_value.replace("item:", "")).filter(dItem.class));

            else if (!scriptEntry.hasObject("slot")
                    && !scriptEntry.hasObject("type")
                    && arg.matchesPrefix("slot")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
                scriptEntry.addObject("type", Type.SLOT);
                scriptEntry.addObject("slot", arg.asElement());
            }

            else if (!scriptEntry.hasObject("inventory")
                        && arg.matchesPrefix("f, from")
                        && arg.matchesArgumentType(dInventory.class))
                scriptEntry.addObject("inventory", arg.asType(dInventory.class));

            else if (!scriptEntry.hasObject("type")
                        && arg.matches("inventory"))
                scriptEntry.addObject("type", Type.INVENTORY);

            else if (!scriptEntry.hasObject("inventory")
                        && arg.matches("npc"))
                scriptEntry.addObject("inventory", scriptEntry.getNPC().getDenizenEntity().getInventory());

        }

        scriptEntry.defaultObject("type", Type.ITEM)
                .defaultObject("inventory", (scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getInventory() : null))
                .defaultObject("qty", new Element(1));

        if (scriptEntry.getObject("type") == Type.ITEM && scriptEntry.getObject("items") == null)
            throw new InvalidArgumentsException("Must specify item/items!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dInventory inventory = (dInventory) scriptEntry.getObject("inventory");
        Element qty = scriptEntry.getElement("qty");
        Element displayname = scriptEntry.getElement("displayname");
        Element slot = scriptEntry.getElement("slot");
        Type type = (Type) scriptEntry.getObject("type");

        Object items_object = scriptEntry.getObject("items");
        List<dItem> items = null;

        if (items_object != null)
            items = (List<dItem>) items_object;

        dB.report(scriptEntry, getName(),
                aH.debugObj("Type", type.name())
                        + qty.debug()
                        + inventory.debug()
                        + (displayname != null ? displayname.debug(): "")
                        + aH.debugObj("Items", items)
                        + (slot != null ? slot.debug() : ""));

        switch (type) {

            case INVENTORY:
                inventory.clear();
                break;

            case ITEMINHAND:
                int inHandAmt = scriptEntry.getPlayer().getPlayerEntity().getItemInHand().getAmount();
                int theAmount = (int)qty.asDouble();
                ItemStack newHandItem = new ItemStack(0);
                if (theAmount > inHandAmt) {
                    dB.echoDebug(scriptEntry, "...player did not have enough of the item in hand, so Denizen just took as many as it could. To avoid this situation, use an IF <PLAYER.ITEM_IN_HAND.QTY>.");
                    scriptEntry.getPlayer().getPlayerEntity().setItemInHand(newHandItem);
                }
                else {

                    // amount is just right!
                    if (theAmount == inHandAmt) {
                        scriptEntry.getPlayer().getPlayerEntity().setItemInHand(newHandItem);
                    } else {
                        // amount is less than what's in hand, need to make a new itemstack of what's left...
                        newHandItem = new ItemStack(scriptEntry.getPlayer().getPlayerEntity().getItemInHand().getType(),
                                inHandAmt - theAmount, scriptEntry.getPlayer().getPlayerEntity().getItemInHand().getData().getData());
                        newHandItem.setItemMeta(scriptEntry.getPlayer().getPlayerEntity().getItemInHand().getItemMeta());
                        scriptEntry.getPlayer().getPlayerEntity().setItemInHand(newHandItem);
                        scriptEntry.getPlayer().getPlayerEntity().updateInventory();
                    }
                }
                break;

            case MONEY:
                if(Depends.economy != null) {
                    dB.echoDebug (scriptEntry, "...taking " + qty.asDouble() + " money.");
                    Depends.economy.withdrawPlayer(scriptEntry.getPlayer().getName(), qty.asDouble());
                } else {
                    dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                }
                break;

            case ITEM:
                for (dItem item : items) {
                    ItemStack is = item.getItemStack();
                    is.setAmount((int)qty.asDouble());

                    // Remove books with a certain title even if they
                    // are not identical to an item script, to allow
                    // books that update
                    if (is.getItemMeta() instanceof BookMeta) {
                        if (((BookMeta) is.getItemMeta()).hasTitle())
                            inventory.removeBook(is);
                    }
                    else if (!inventory.getInventory().removeItem(is).isEmpty())
                        dB.echoError("Inventory does not contain at least " + qty.asInt() + " of " + item.identify() +
                                "... Taking as much as possible...");
                }
                break;

            case BYDISPLAY:
                int found_items = 0;
                if (displayname == null) {
                    dB.echoError("Must specify a displayname!");
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

            case SLOT:
                inventory.setSlots(slot.asInt(), new ItemStack(Material.AIR));
                break;

        }
    }
}
