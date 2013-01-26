package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Depends;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

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

enum GiveType { ITEM, MONEY, EXP, HEROESEXP }

public class GiveCommand  extends AbstractCommand {
	Player player;
	GiveType giveType;
	int theAmount;
	ItemStack theItem;

	@Override
	public void onEnable() {
		//nothing to do here
	}

	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		/* Initialize variables */ 
	
		if (scriptEntry.getArguments () == null) {
			throw new InvalidArgumentsException ("...Usage: GIVE [MONEY|#(:#)|MATERIAL_TYPE(:#)] (QTY:#)");
		}
		
		player = scriptEntry.getPlayer();
		giveType = null;
		theAmount = 1;
		theItem = null;
	
		/* Match arguments to expected variables */
		for (String thisArg : scriptEntry.getArguments()) {
			if (aH.matchesQuantity(thisArg)) {
				theAmount = aH.getIntegerFrom(thisArg);
				dB.echoDebug ("...set quantity to '%s'.", thisArg);
			} 
			
			else if (aH.matchesArg("MONEY", thisArg)) {
				this.giveType = GiveType.MONEY;
				dB.echoDebug ("...giving MONEY.");
			} 
			
			else if (aH.matchesArg("HEROESEXP", thisArg)
					|| aH.matchesArg("HEROES_EXP", thisArg)) {
				this.giveType = GiveType.HEROESEXP;
				dB.echoDebug("...giving Heroes Quest EXP.");
			} 
			
			else if (aH.matchesArg("XP", thisArg)
					|| aH.matchesArg("EXP", thisArg)) {
				this.giveType = GiveType.EXP;
				dB.echoDebug ("...giving EXP.");
			} 
			
			else if (aH.matchesItem(thisArg)) {
				theItem = aH.getItemFrom (thisArg);
				this.giveType = GiveType.ITEM;
				if (theItem != null) {
					dB.echoDebug ("...set item to be given to '%s'.", thisArg);
				}
			}
			
			else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, thisArg);
		}			
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		dB.echoDebug ("execute (" + scriptEntry.toString () + ")");
		if (giveType != null) {
			switch (giveType) {

			case MONEY:
				if(Depends.economy != null) {
					Economy economy = Depends.economy;
					double doubleAmount = (double) theAmount;
					dB.echoDebug ("Giving player " + theAmount + " money.");
					economy.depositPlayer(player.getName(), doubleAmount);
				} else {
					dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
				}
				break;

			case EXP:
				player.giveExp(theAmount);
				dB.echoDebug("..giving player " + theAmount + " EXP");
				break;

			case ITEM:
				theItem.setAmount(theAmount);
				dB.echoDebug("..giving player " + theAmount + " of " + theItem);
				HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(theItem);

				if (!leftovers.isEmpty()) {
					dB.echoDebug ("...Player did not have enough space in their inventory, the rest of the items have been placed on the floor.");
					for (Map.Entry<Integer, ItemStack> leftoverItem : leftovers.entrySet()) {
						player.getWorld().dropItem(player.getLocation(), leftoverItem.getValue());
					}
				}
				break;
			}
		}
	}
}