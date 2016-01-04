package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.objects.aH;
import org.bukkit.World;

import java.util.List;

public class PermissionRequirement extends AbstractRequirement {

    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {
        if (context.getPlayer() != null) {
            if (Depends.permissions != null) {
                boolean outcome = false;
                boolean global = false;

                for (String arg : args) {
                    if (aH.matchesArg("GLOBAL", arg)) {
                        global = true;
                    }
                    else {
                        if (global) {
                            if (Depends.permissions.has((World) null, context.getPlayer().getName(), arg)) {
                                dB.echoDebug(context.getScriptContainer(), "...player has global permission: " + arg);
                                outcome = true;
                            }
                            else {
                                dB.echoDebug(context.getScriptContainer(), "...player does not have global permission: " + arg);
                            }
                        }
                        else {
                            if (Depends.permissions.has(context.getPlayer().getPlayerEntity(), arg)) {
                                dB.echoDebug(context.getScriptContainer(), "...player has permission: " + arg);
                                outcome = true;
                            }
                            else {
                                dB.echoDebug(context.getScriptContainer(), "...player does not have permission: " + arg + "!");
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
