package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.commands.core.FailCommand;
import net.aufdemrand.denizen.scripts.commands.core.FinishCommand;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.aH.ArgumentType;

import java.util.List;

public class ScriptRequirement extends AbstractRequirement {

    private enum Type {FINISHED, FAILED, STEP}

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

        boolean outcome = false;

        Type type = null;
        String step = null;
        int quantity = 1;
        boolean exactly = false;
        String checkScript = null;

        for (String thisArg : args) {

            if (aH.matchesArg("FINISHED, FAILED", thisArg)) {
                try {
                    type = Type.valueOf(aH.getStringFrom(thisArg).toUpperCase());
                }
                catch (Exception e) {
                    dB.echoError("Invalid check type. Valid: FINISHED, FAILED, STEP.");
                }
            }

            else if (aH.matchesScript(thisArg)) {
                checkScript = aH.getStringFrom(thisArg).toUpperCase();
            }

            else if (aH.matchesValueArg("STEP", thisArg, ArgumentType.String)) {
                step = aH.getStringFrom(thisArg).toUpperCase();
            }

            else if (aH.matchesQuantity(thisArg)) {
                quantity = aH.getIntegerFrom(thisArg);
            }


            else if (aH.matchesArg("EXACTLY", thisArg))
            //im pretty confident this was missing from the original requirement
            {
                exactly = true;
            }

            else {
                dB.echoError("Could not match argument '" + thisArg + "'");
            }
        }

        if (type != null && checkScript != null) {

            switch (type) {

                case FINISHED:
                    int finishes = FinishCommand.getScriptCompletes(context.getPlayer().getName(), checkScript);
                    if (exactly) {
                        if (quantity == finishes) {
                            outcome = true;
                        }
                    }
                    else {
                        if (finishes >= quantity) {
                            outcome = true;
                        }
                    }
                    break;

                case FAILED:
                    int fails = FailCommand.getScriptFails(context.getPlayer().getName(), checkScript);
                    if (exactly) {
                        if (quantity == fails) {
                            outcome = true;
                        }
                    }
                    else {
                        if (fails >= quantity) {
                            outcome = true;
                        }
                    }
                    break;

                case STEP:
                    // TODO: reimplement
                    break;
            }
        }

        return outcome;
    }
}
