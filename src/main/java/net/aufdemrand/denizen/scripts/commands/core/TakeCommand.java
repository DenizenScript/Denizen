package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
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

    private enum TakeType { MONEY, ITEMINHAND, ITEM, INVENTORY }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        TakeType takeType = null;
        double quantity = 1;
        ItemStack item = null;
        boolean npc = false;

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("MONEY, COINS", arg))
                takeType = TakeType.MONEY;

            else if (aH.matchesArg("ITEM_IN_HAND", arg))
                takeType = TakeType.ITEMINHAND;

            else if (aH.matchesArg("INVENTORY", arg))
                takeType = TakeType.INVENTORY;

            else if (aH.matchesArg("NPC", arg))
                npc = true;

            else if (aH.matchesValueArg("QTY", arg, aH.ArgumentType.Double))
                quantity = aH.getDoubleFrom(arg);

            else if (aH.matchesItem(arg) || aH.matchesItem("item:" + arg)) {
                takeType = TakeType.ITEM;
                item = aH.getItemFrom(arg).getItemStack();
            }

            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        scriptEntry.addObject("item", item)
                .addObject("takeType", takeType)
                .addObject("quantity", quantity)
                .addObject("npc", npc);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        boolean npc = (Boolean) scriptEntry.getObject("npc");
        TakeType type = (TakeType) scriptEntry.getObject("takeType");
        double quantity = (Double) scriptEntry.getObject("quantity");

        dB.report(getName(),
                aH.debugObj("Type", type.name())
                        + aH.debugObj("Quantity", String.valueOf(quantity))
                        + ((type == TakeType.INVENTORY && npc)
                        ? aH.debugObj("NPC", "true") : ""));

        switch (type) {

            case INVENTORY:

                if (npc)
                    scriptEntry.getNPC().getEntity().getEquipment().clear();
                else // Player
                    scriptEntry.getPlayer().getInventory().clear();
                break;

            case ITEMINHAND:
                int inHandAmt = scriptEntry.getPlayer().getItemInHand().getAmount();
                int theAmount = ((Double) scriptEntry.getObject("quantity")).intValue();
                ItemStack newHandItem = new ItemStack(0);
                if (theAmount > inHandAmt) {
                    dB.echoDebug("...player did not have enough of the item in hand, so Denizen just took as many as it could. To avoid this situation, use an IF <PLAYER.ITEM_IN_HAND.QTY>.");
                    scriptEntry.getPlayer().setItemInHand(newHandItem);
                }
                else {

                    // amount is just right!
                    if (theAmount == inHandAmt) {
                        scriptEntry.getPlayer().setItemInHand(newHandItem);
                    } else {
                        // amount is less than what's in hand, need to make a new itemstack of what's left...
                        newHandItem = new ItemStack(scriptEntry.getPlayer().getItemInHand().getType(),
                                inHandAmt - theAmount, scriptEntry.getPlayer().getItemInHand().getData().getData());
                        newHandItem.setItemMeta(scriptEntry.getPlayer().getItemInHand().getItemMeta());
                        scriptEntry.getPlayer().setItemInHand(newHandItem);
                        scriptEntry.getPlayer().updateInventory();
                    }
                }
                break;

            case MONEY:
                if(Depends.economy != null) {
                    double amount = (Double) scriptEntry.getObject("quantity");
                    dB.echoDebug ("...taking " + amount + " money.");
                    Depends.economy.withdrawPlayer(scriptEntry.getPlayer().getName(), amount);
                } else {
                    dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                }
                break;

            case ITEM:
                ((ItemStack) scriptEntry.getObject("item")).setAmount(((Double) scriptEntry.getObject("quantity")).intValue());
                if (!scriptEntry.getPlayer().getInventory().removeItem((ItemStack)scriptEntry.getObject("item")).isEmpty())
                    dB.echoDebug("The Player did not have enough " + ((ItemStack) scriptEntry.getObject("item")).getType().toString()
                            + " on hand, so Denizen took as much as possible. To avoid this situation, use an IF or REQUIREMENT to check.");
                break;
        }
    }
}
