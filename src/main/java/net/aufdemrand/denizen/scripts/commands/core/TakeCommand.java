package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.milkbowl.vault.economy.Economy;

public class TakeCommand extends AbstractCommand{
	
	private enum TakeType { MONEY, ITEMINHAND, ITEM }

	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		
		TakeType takeType = null;
		int quantity = 1;
		ItemStack item = null;
		
		for (String arg : scriptEntry.getArguments()) {
			if (aH.matchesArg("MONEY, COINS", arg)) {
				takeType = TakeType.MONEY;
				dB.echoDebug("...taking MONEY");
			} 
			
			else if (aH.matchesArg("ITEMINHAND", arg)) {
				takeType = TakeType.ITEMINHAND;
				dB.echoDebug("...taking ITEMINHAND");
			}
			
			else if (aH.matchesQuantity(arg)) {
				quantity = aH.getIntegerFrom(arg);
			}
			
			else if (aH.matchesItem(arg)) {
				takeType = TakeType.ITEMINHAND;
				item = aH.getItemFrom(arg);
				dB.echoDebug("...taking " + item.getType());
			}
			
			else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
			
			scriptEntry.addObject("item", item);
			scriptEntry.addObject("takeType", takeType);
			scriptEntry.addObject("quantity", quantity);
		}
	}

	@Override
	public void execute(ScriptEntry scriptEntry)
			throws CommandExecutionException {
		switch ((TakeType)scriptEntry.getObject("takeType")) {

        case ITEMINHAND:
        	scriptEntry.getPlayer().setItemInHand(new ItemStack(0));
			dB.echoDebug("...item taken");
        	break;
        	
        case MONEY:
        	 try {
				 	RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration (Economy.class);
				 	if (provider != null && provider.getProvider() != null) {
				 		Economy economy = provider.getProvider();
						dB.echoDebug ("...taking " + scriptEntry.getObject("quantity") + " money.");
						economy.withdrawPlayer(scriptEntry.getPlayer().getName(), (Double)scriptEntry.getObject("quantity"));
				 	}
			 	} catch (NoClassDefFoundError e) {
					dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
			 	}
        	break;
        case ITEM:
        	((ItemStack)scriptEntry.getObject("item")).setAmount((Integer)scriptEntry.getObject("quantity"));
			if (!scriptEntry.getPlayer().getInventory().removeItem((ItemStack)scriptEntry.getObject("item")).isEmpty()) 
        	break;
		}
	}
}
