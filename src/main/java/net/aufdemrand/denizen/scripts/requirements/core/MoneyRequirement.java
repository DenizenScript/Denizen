package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.Depends;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;

public class MoneyRequirement extends AbstractRequirement{

	private Double quantity;
	private Double balance;
	boolean outcome = false;

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
		for (String arg : args) {
			if (aH.matchesQuantity(arg) || aH.matchesInteger(arg) || aH.matchesDouble(arg)) {
				quantity = aH.getDoubleFrom(arg);
				dB.echoDebug("...quantity set to: " + quantity);
			} else throw new RequirementCheckException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}
		
		if(Depends.economy != null) {
			Economy economy = Depends.economy;
			balance = economy.getBalance(context.getPlayer().getName());
			dB.echoDebug ("...player balance: " + balance);
		} else {
			dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
		}
		
		if (balance >= quantity) outcome = true;
		else outcome = false;
		
		return outcome;
	}

}
