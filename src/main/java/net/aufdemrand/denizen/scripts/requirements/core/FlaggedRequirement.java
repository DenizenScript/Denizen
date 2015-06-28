package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;

import java.util.List;

public class FlaggedRequirement extends AbstractRequirement {

    /* FLAGGED TYPE FLAG:value
     * Example: FLAGGED PLAYER Cookies:3
     *
     * Arguments: [] - Required, () - Optional
     */

    private enum Type {GLOBAL, NPC, PLAYER}

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

        boolean outcome = false;
        String name = null;
        String value = "true";
        String index = "";
        Type type = Type.PLAYER;

        for (String arg : args) {

            if (aH.matchesArg("GLOBAL, NPC, DENIZEN, GLOBAL", arg))
                type = Type.valueOf(arg.toUpperCase().replace("DENIZEN", "NPC"));

            else if (arg.split(":", 2).length > 1) {
                String[] flagArgs = arg.split(":");
                value = flagArgs[1].toUpperCase();

                if (flagArgs[0].contains("[")) {
                    name = flagArgs[0].split("\\[", 2)[0].trim();
                    index = flagArgs[0].split("\\[", 2)[1].split("\\]", 2)[0].trim();
                }
                else {
                    name = flagArgs[0].toUpperCase();
                }
            }

            else
                name = arg.toUpperCase();
        }

        FlagManager flagMng = DenizenAPI.getCurrentInstance().flagManager();
        FlagManager.Flag flag = null;

        switch (type) {
            case NPC:
                flag = flagMng.getNPCFlag(context.getNPC().getId(), name);
                break;
            case PLAYER:
                flag = flagMng.getPlayerFlag(context.getPlayer(), name);
                break;
            case GLOBAL:
                flag = flagMng.getGlobalFlag(name);
                break;
        }

        if (index.length() == 0) {
            if (flag.getLast().asString().equalsIgnoreCase(value))
                outcome = true;
            else
                dB.echoDebug(context.getScriptContainer(), "... does not match '" + flag.getLast().asString() + "'.");
        }
        else if (index.matches("\\d+")) {
            if (flag.get(Integer.parseInt(index)).asString().equalsIgnoreCase(value))
                outcome = true;
            else
                dB.echoDebug(context.getScriptContainer(), "... does not match '" + flag.get(Integer.parseInt(index)).asString() + "'.");
        }

        return outcome;
    }
}
