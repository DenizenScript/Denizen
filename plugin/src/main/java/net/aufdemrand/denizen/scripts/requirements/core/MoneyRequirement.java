package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.objects.aH;

import java.util.List;

public class MoneyRequirement extends AbstractRequirement {

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
        boolean outcome = true;

        if (Depends.economy != null) {
            double quantity = 0;
            double balance;

            for (String arg : args) {
                if (aH.matchesQuantity(arg) || aH.matchesInteger(arg) || aH.matchesDouble(arg)) {
                    quantity = aH.getDoubleFrom(arg);
                    dB.echoDebug(context.getScriptContainer(), "...quantity set to: " + quantity);
                }
                else {
                    throw new RequirementCheckException("Unknown argument '" + arg + "'!");
                }
            }

            balance = Depends.economy.getBalance(context.getPlayer().getName());
            dB.echoDebug(context.getScriptContainer(), "...player balance: " + balance);

            outcome = (balance >= quantity);
        }
        else {
            dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
        }

        return outcome;
    }
}
