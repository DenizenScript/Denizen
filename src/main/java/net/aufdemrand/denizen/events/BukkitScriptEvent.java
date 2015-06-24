package net.aufdemrand.denizen.events;

import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEllipsoid;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;

import java.util.List;

public abstract class BukkitScriptEvent extends ScriptEvent {

    public boolean runInCheck(ScriptContainer scriptContainer, String s, String lower, Location location) {

        List<String> data = CoreUtilities.split(lower, ' ');

        int index;

        for (index = 0; index < data.size(); index++) {
            if (data.get(index).equals("in")) {
                break;
            }
        }
        if (index >= data.size()) {
            // No 'in ...' specified
            return true;
        }

        String it = CoreUtilities.getXthArg(index + 1, s);
        if (it.equalsIgnoreCase("notable")) {
            String subit = CoreUtilities.getXthArg(index + 2, lower);
            if (subit.equalsIgnoreCase("cuboid")) {
                return dCuboid.getNotableCuboidsContaining(location).size() > 0;
            }
            else if (subit.equalsIgnoreCase("ellipsoid")) {
                return dEllipsoid.getNotableEllipsoidsContaining(location).size() > 0;
            }
            else {
                dB.echoError("Invalid event 'IN ...' check [" + getName() + "] ('in notable ???'): '" + s + "' for " + scriptContainer.getName());
                return false;
            }
        }
        else if (dCuboid.matches(it)) {
            dCuboid cuboid = dCuboid.valueOf(it);
            return cuboid.isInsideCuboid(location);
        }
        else if (dEllipsoid.matches(it)) {
            dEllipsoid ellipsoid = dEllipsoid.valueOf(it);
            return ellipsoid.contains(location);
        }
        else {
            dB.echoError("Invalid event 'IN ...' check [" + getName() + "] ('in ???'): '" + s + "' for " + scriptContainer.getName());
            return false;
        }
    }
}
