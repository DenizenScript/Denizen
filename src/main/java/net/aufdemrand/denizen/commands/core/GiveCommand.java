package net.aufdemrand.denizen.commands.core;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Your command! 
 * This class is a template for a Command in Denizen.
 * 
 * @author You!
 */

public class GiveCommand extends AbstractCommand {

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

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 
		GiveType giveType = null;
		int theAmount = 1;
		ItemStack theItem = null;

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: GIVE [MONEY|#(:#)|MATERIAL_TYPE(:#)] (QTY:#)");

		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			// If argument is QTY: modifier
			if (aH.matchesQuantity(thisArg)) {
				theAmount = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...set quantity to '%s'.", thisArg);
			}

			// If the argument is MONEY
			else if (thisArg.toUpperCase().contains("MONEY")) {
				giveType = GiveType.MONEY;
				aH.echoDebug("...giving MONEY.");
			}

			// If the argument is XP
			else if (thisArg.toUpperCase().contains("HEROESEXP")
					|| thisArg.toUpperCase().contains("HEROES_EXP")) {
				giveType = GiveType.HEROESEXP;
				aH.echoDebug("...giving Heroes Quest EXP.");
			}

			// If the argument is XP
			else if (thisArg.toUpperCase().contains("XP")
					|| thisArg.toUpperCase().contains("EXP")) {
				giveType = GiveType.EXP;
				aH.echoDebug("...giving EXP.");
			}

			// If argument is an Item
			else if (aH.matchesItem(thisArg)) {
				theItem = aH.getItemModifier(thisArg);
				giveType = GiveType.ITEM;
				if (theItem != null)
					aH.echoDebug("...set item to be given to '%s'.", thisArg);
			}

			/* Can't match to anything */
			else aH.echoError("...unable to match '%s'!", thisArg);

		}	


		/* Execute the command, if all required variables are filled. */
		if (giveType != null) {

			switch (giveType) {

			case MONEY:
				if (plugin.economy != null) {
					double doubleAmount = Double.valueOf(theAmount);
					plugin.economy.depositPlayer(theEntry.getPlayer().getName(), doubleAmount);
				} else {
					aH.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");	
				}
				return true;

			case EXP:
				theEntry.getPlayer().giveExp(theAmount);
				return true;			

			case ITEM:
				theItem.setAmount(theAmount);
				HashMap<Integer, ItemStack> leftovers = theEntry.getPlayer().getInventory().addItem(theItem);

				if (!leftovers.isEmpty()) {
					if (plugin.debugMode)
						aH.echoDebug("...Player did not have enough space in their inventory, the rest of the items have been placed on the floor.");
					for (Entry<Integer, ItemStack> leftoverItem : leftovers.entrySet()) {
						theEntry.getPlayer().getWorld().dropItem(theEntry.getPlayer().getLocation(), leftoverItem.getValue());
					}
				}
				return true;

			case HEROESEXP:
				if (plugin.heroes != null) {
					Hero theHero = plugin.heroes.getCharacterManager().getHero(theEntry.getPlayer());
					theHero.gainExp(theAmount, ExperienceType.QUESTING, theEntry.getPlayer().getLocation());
				}
				else aH.echoError("Could not find Heroes!");
				return true;
			}
		}

		return false;
	}

}