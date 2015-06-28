package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.objects.aH;
import org.bukkit.World;

import java.util.List;

public class InGroupRequirement extends AbstractRequirement {

    @Override
    public void onEnable() {
        // nothing to do here
    }

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

        if (context.getPlayer() != null) {
            if (Depends.permissions != null) {
                boolean outcome = false;
                boolean global = false;

                for (String arg : args) {
                    if (aH.matchesArg("GLOBAL", arg))
                        global = true;
                    else {
                        if (global) {
                            if (Depends.permissions.playerInGroup((World) null, context.getPlayer().getName(), arg)) {
                                dB.echoDebug(context.getScriptContainer(), "...player is in global group: " + arg);
                                outcome = true;
                            }
                            else
                                dB.echoDebug(context.getScriptContainer(), "...player is not in global group: " + arg + "!");
                        }
                        else {
                            if (Depends.permissions.playerInGroup(context.getPlayer().getPlayerEntity(), arg)) {
                                dB.echoDebug(context.getScriptContainer(), "...player is in group: " + arg);
                                outcome = true;
                            }
                            else {
                                dB.echoDebug(context.getScriptContainer(), "...player is not in group: " + arg + "!");
                            }
                        }

                    }
                }

                return outcome;
            }

            dB.echoDebug(context.getScriptContainer(), "...no permission plugin found, assume as FALSE!");
        }

        return false;
    }
}
