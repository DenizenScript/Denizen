package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizencore.objects.aH;

import java.util.List;

public class TimeRequirement extends AbstractRequirement {

    private enum Time {DAWN, DAY, DUSK, NIGHT}


    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

        boolean outcome = false;
        Time time = null;

        for (String arg : args) {
            if (aH.matchesArg("DAWN, DAY, DUSK, NIGHT", arg)) time = Time.valueOf(arg.toUpperCase());
        }

        long worldTime = context.getNPC().getEntity().getWorld().getTime();


        if (time.equals(Time.DAY) && (worldTime <= 12500))
            outcome = true;
        else if (time.equals(Time.NIGHT) && (worldTime >= 13500 && worldTime <= 23000))
            outcome = true;
        else if (time.equals(Time.DAWN) && (worldTime >= 23000))
            outcome = true;
        else if (time.equals(Time.DUSK) && worldTime >= 12500 && worldTime <= 13500)
            outcome = true;

        return outcome;
    }
}
