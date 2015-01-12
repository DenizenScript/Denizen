package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/* GIVE [MONEY|#(:#)|MATERIAL_TYPE(:#)] (QTY:#) */

/*
 * Arguments: [] - Required, () - Optional
 * [MONEY|[#](:#)|[MATERIAL_TYPE](:#)] specifies what to give.
 *   [MONEY] gives money using your economy.
 *   [#](:#) gives the item with the specified item ID. Optional
 *     argument (:#) can specify a specific data value.
 *   [MATERIAL_TYPE](:#) gives the item with the specified
 *     bukkit MaterialType. Optional argument (:#) can specify
 *     a specific data value.
 * (QTY:#) specifies quantity. If not specified, assumed 'QTY:1'
 *
 */

public class GiveCommand  extends AbstractCommand {

    enum Type { ITEM, MONEY, EXP }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        /* Match arguments to expected variables */
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrefix("q", "qty", "quantity")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("qty", arg.asElement());
                scriptEntry.addObject("set_quantity",  new Element(true));
            }

            else if (!scriptEntry.hasObject("type")
                        && arg.matches("money", "coins"))
                scriptEntry.addObject("type", Type.MONEY);

            else if (!scriptEntry.hasObject("type")
                        && arg.matches("xp", "exp", "experience"))
                scriptEntry.addObject("type", Type.EXP);

            else if (!scriptEntry.hasObject("engrave")
                        && arg.matches("engrave"))
                scriptEntry.addObject("engrave",  new Element(true));

            else if (!scriptEntry.hasObject("unlimit_stack_size")
                    && arg.matches("unlimit_stack_size"))
                scriptEntry.addObject("unlimit_stack_size", new Element(true));

            else if (!scriptEntry.hasObject("items")
                        && !scriptEntry.hasObject("type")
                        && arg.matchesArgumentList(dItem.class)) {
                scriptEntry.addObject("items", dList.valueOf(arg.raw_value.replace("item:", "")).filter(dItem.class, scriptEntry));
            }

            else if (!scriptEntry.hasObject("inventory")
                        && arg.matchesPrefix("t", "to")
                        && arg.matchesArgumentType(dInventory.class))
                scriptEntry.addObject("inventory", arg.asType(dInventory.class));

            else if (!scriptEntry.hasObject("slot")
                    && arg.matchesPrefix("slot")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer))
                scriptEntry.addObject("slot", arg.asElement());

            else
                arg.reportUnhandled();

        }

        scriptEntry.defaultObject("type", Type.ITEM)
                .defaultObject("engrave",  new Element(false))
                .defaultObject("unlimit_stack_size", new Element(false))
                .defaultObject("qty", new Element(1))
                .defaultObject("slot", new Element(1));

        Type type = (Type) scriptEntry.getObject("type");

        if (type != Type.MONEY && scriptEntry.getObject("inventory") == null)
            scriptEntry.addObject("inventory", ((BukkitScriptEntryData)scriptEntry.entryData).hasPlayer() ? ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getInventory(): null);

        if (!scriptEntry.hasObject("inventory") && type != Type.MONEY)
            throw new InvalidArgumentsException("Must specify an inventory to give to!");

        if (type == Type.ITEM && scriptEntry.getObject("items") == null)
            throw new InvalidArgumentsException("Must specify item/items!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element engrave = scriptEntry.getElement("engrave");
        Element unlimit_stack_size = scriptEntry.getElement("unlimit_stack_size");
        dInventory inventory = (dInventory) scriptEntry.getObject("inventory");
        Element qty = scriptEntry.getElement("qty");
        Type type = (Type) scriptEntry.getObject("type");
        Element slot = scriptEntry.getElement("slot");

        Object items_object = scriptEntry.getObject("items");
        List<dItem> items = null;

        if (items_object != null)
            items = (List<dItem>) items_object;

        dB.report(scriptEntry, getName(),
                aH.debugObj("Type", type.name())
                        + (inventory != null ? inventory.debug(): "")
                        + aH.debugObj("Quantity", qty.asDouble())
                        + engrave.debug()
                        + unlimit_stack_size.debug()
                        + (items != null ? aH.debugObj("Items", items) : "")
                        + slot.debug());

        switch (type) {

            case MONEY:
                if(Depends.economy != null)
                    Depends.economy.depositPlayer(((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getName(), qty.asDouble());
                else
                    dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                break;

            case EXP:
                ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getPlayerEntity().giveExp(qty.asInt());
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
                    if (set_quantity)
                        is.setAmount(qty.asInt());
                    if (engrave.asBoolean()) is = CustomNBT.addCustomNBT(item.getItemStack(), "owner", ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getName());

                    List<ItemStack> leftovers = inventory.addWithLeftovers(slot.asInt()-1, limited, is);

                    if (!leftovers.isEmpty()) {
                        dB.echoDebug (scriptEntry, "The inventory didn't have enough space, the rest of the items have been placed on the floor.");
                        for (ItemStack leftoverItem : leftovers)
                            inventory.getLocation().getWorld().dropItem(inventory.getLocation(), leftoverItem);
                    }
                }
                break;
        }
    }
}
