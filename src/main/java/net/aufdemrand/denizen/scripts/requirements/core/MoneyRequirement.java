package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.milkbowl.vault.economy.Economy;

public class MoneyRequirement extends AbstractRequirement{

	private Double quantity;
	private Double balance;
	boolean outcome = false;
	
	@Override
	public boolean check(Player player, DenizenNPC npc, String scriptName,
			List<String> args) throws RequirementCheckException {
		for (String arg : args) {
			if (aH.matchesQuantity(arg)) {
				quantity = aH.getDoubleFrom(arg);
				dB.echoDebug("...quantity set to: " + quantity);
			} else throw new RequirementCheckException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
		try {
		 	RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration (Economy.class);
		 	if (provider != null && provider.getProvider() != null) {
		 		Economy economy = provider.getProvider();
		 		balance = economy.getBalance(player.getName());
				dB.echoDebug ("...player balance: " + balance);
		 	}
	 	} catch (NoClassDefFoundError e) {
			dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
	 	}
		if (balance >= quantity) outcome = true;
		else outcome = false;
		
		return outcome;
	}

}
