package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.arguments.dItem;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

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

    enum GiveType { ITEM, MONEY, EXP }

    @Override
    public void parseArgs(ScriptEntry scriptEntry)
            throws InvalidArgumentsException {

        GiveType type = null;
        double amt = 1;
        dItem item = null;
        boolean engrave = false;

		/* Match arguments to expected variables */
        for (String thisArg : scriptEntry.getArguments()) {
            if (aH.matchesValueArg("QTY", thisArg, aH.ArgumentType.Double))
                amt = aH.getDoubleFrom(thisArg);

            else if (aH.matchesArg("MONEY", thisArg))
                type = GiveType.MONEY;

            else if (aH.matchesArg("XP", thisArg)
                    || aH.matchesArg("EXP", thisArg))
                type = GiveType.EXP;

            else if (aH.matchesArg("ENGRAVE", thisArg))
                engrave = true;

            else if (aH.matchesItem(thisArg) || aH.matchesItem("item:" + thisArg)) {
                item = aH.getItemFrom (thisArg);
                type = GiveType.ITEM;
            }

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, thisArg);
        }

        if (type == null)
            throw new InvalidArgumentsException("Must specify a type! Valid: MONEY, XP, or ITEM:...");

        if (type == GiveType.ITEM && item == null)
            throw new InvalidArgumentsException("Item was returned as null.");

        scriptEntry.addObject("type", type)
                .addObject("amt", amt)
                .addObject("item", item)
                .addObject("engrave", engrave);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        GiveType type = (GiveType) scriptEntry.getObject("type");
        Double amt = (Double) scriptEntry.getObject("amt");
        dItem item = (dItem) scriptEntry.getObject("item");
        Boolean engrave = (Boolean) scriptEntry.getObject("engrave");

        dB.report(getName(),
                aH.debugObj("Type", type.name())
                        + aH.debugObj("Amount", amt.toString())
                        + (item != null ? item.debug() : "")
                        + (engrave ? aH.debugObj("Engraved", "TRUE") : ""));

        switch (type) {

            case MONEY:
                if(Depends.economy != null)
                    Depends.economy.depositPlayer(scriptEntry.getPlayer().getName(), amt);
                else dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                break;

            case EXP:
                scriptEntry.getPlayer().giveExp(amt.intValue());
                break;

            case ITEM:
                ItemStack is = item.getItemStack();
                is.setAmount(amt.intValue());
                if(engrave) is = CustomNBT.addCustomNBT(item.getItemStack(), "owner", scriptEntry.getPlayer().getName());

                HashMap<Integer, ItemStack> leftovers = scriptEntry.getPlayer().getInventory().addItem(is);

                if (!leftovers.isEmpty()) {
                    dB.echoDebug ("'" + scriptEntry.getPlayer().getName() + "' did not have enough space in their inventory," +
                            " the rest of the items have been placed on the floor.");
                    for (Map.Entry<Integer, ItemStack> leftoverItem : leftovers.entrySet())
                        scriptEntry.getPlayer().getWorld().dropItem(scriptEntry.getPlayer().getLocation(), leftoverItem.getValue());
                }
                break;
        }
    }
}