package net.aufdemrand.denizen.scripts.commands.item;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
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

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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
                    && arg.matchesPrefix("q, qty, quantity")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double))
                scriptEntry.addObject("qty", arg.asElement());

            else if (!scriptEntry.hasObject("type")
                        && arg.matches("money, coins"))
                scriptEntry.addObject("type", Type.MONEY);

            else if (!scriptEntry.hasObject("type")
                        && arg.matches("xp, exp, experience"))
                scriptEntry.addObject("type", Type.EXP);

            else if (!scriptEntry.hasObject("engrave")
                        && arg.matches("engrave"))
                scriptEntry.addObject("engrave", Element.TRUE);

            else if (!scriptEntry.hasObject("items")
                        && !scriptEntry.hasObject("type")
                        && arg.matchesArgumentType(dItem.class))
                scriptEntry.addObject("items", dList.valueOf(arg.getValue()).filter(dItem.class));
            
            else if (!scriptEntry.hasObject("inventory")
                        && arg.matchesPrefix("t, to")
                        && arg.matchesArgumentType(dInventory.class))
                scriptEntry.addObject("inventory", arg.asType(dInventory.class));
            
        }

        scriptEntry.defaultObject("type", Type.ITEM)
                .defaultObject("engrave", Element.FALSE)
                .defaultObject("inventory", (scriptEntry.hasPlayer() ? new dInventory(scriptEntry.getPlayer().getPlayerEntity()) : null))
                .defaultObject("qty", new Element(1));
        
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element engrave = scriptEntry.getElement("engrave");
        dInventory inventory = (dInventory) scriptEntry.getObject("inventory");
        Element qty = scriptEntry.getElement("qty");
        Type type = (Type) scriptEntry.getObject("type");
        
        dList list_of_items = (dList) scriptEntry.getObject("items");
        Object items_object = null;
        List<dItem> items = null;
        
        if (list_of_items != null)
            items_object = list_of_items.filter(dItem.class);
        
        if (items_object != null)
            items = (List<dItem>) items_object;

        dB.report(getName(),
                aH.debugObj("Type", type.name())
                        + aH.debugObj("Quantity", qty.asDouble())
                        + engrave.debug()
                        + (items != null ? aH.debugObj("Items", items) : ""));

        switch (type) {

            case MONEY:
                if(Depends.economy != null)
                    Depends.economy.depositPlayer(scriptEntry.getPlayer().getName(), qty.asDouble());
                else 
                    dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                break;

            case EXP:
                scriptEntry.getPlayer().getPlayerEntity().giveExp(qty.asInt());
                break;

            case ITEM:
                for (dItem item : items) {
                    ItemStack is = item.getItemStack();
                    is.setAmount(qty.asInt());
                    if (engrave.asBoolean()) is = CustomNBT.addCustomNBT(item.getItemStack(), "owner", scriptEntry.getPlayer().getName());

                    HashMap<Integer, ItemStack> leftovers = inventory.addWithLeftovers(is);

                    if (!leftovers.isEmpty()) {
                        dB.echoDebug ("The inventory didn't have enough space, the rest of the items have been placed on the floor.");
                        for (ItemStack leftoverItem : leftovers.values())
                               inventory.getLocation().getWorld().dropItem(inventory.getLocation(), leftoverItem);
                    }
                }
                break;
        }
    }
}
