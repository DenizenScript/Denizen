package net.aufdemrand.denizen.events;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;

import java.util.List;

public abstract class BukkitScriptEvent extends ScriptEvent {

    public boolean runInCheck(ScriptContainer scriptContainer, String s, String lower, Location location) {
        return runInCheck(scriptContainer, s, lower, location, "in");
    }

    public boolean runInCheck(ScriptContainer scriptContainer, String s, String lower, Location location, String innote) {
        List<String> data = CoreUtilities.split(lower, ' ');

        int index;

        for (index = 0; index < data.size(); index++) {
            if (data.get(index).equals(innote)) {
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
        else if (dWorld.matches(it)) {
            return location.getWorld().getName().equalsIgnoreCase(it);
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

    public boolean tryLocation(dLocation location, String comparedto) {
        if (comparedto == null || comparedto.length() == 0) {
            dB.echoError("Null or empty location string to compare");
            return false;
        }
        if (comparedto.equals("notable")) {
            return true;
        }
        comparedto = "l@" + comparedto;
        dLocation loc = dLocation.valueOf(comparedto);
        if (loc == null) {
            dB.echoError("Invalid location in location comparison string: " + comparedto);
            return false;
        }
        return loc.getBlock().equals(location.getBlock());
    }

    public boolean runWithCheck(ScriptContainer scriptContainer, String s, String lower, dItem held) {
        String with = getSwitch(s, "with");
        if (with != null) {
            if (with.equalsIgnoreCase("item")) {
                return true;
            }
            dItem it = dItem.valueOf(with);
            if (it == null) {
                dB.echoError("Invalid WITH item in " + getName() + " for '" + s + "' in " + scriptContainer.getName());
                return false;
            }
            if (held == null || !tryItem(held, with)) {
                return false;
            }
        }
        return true;
    }

    public boolean tryItem(dItem item, String comparedto) {
        if (comparedto.equalsIgnoreCase("item")) {
            return true;
        }
        if (comparedto.length() == 0) {
            dB.echoError("tryItem missing item value when compared to " + item.identifyNoIdentifier());
            return false;
        }
        item = new dItem(item.getItemStack().clone());
        item.setAmount(1);
        if (item.identifyNoIdentifier().equalsIgnoreCase(comparedto)) {
            return true;
        }
        if (item.identifyMaterialNoIdentifier().equalsIgnoreCase(comparedto)) {
            return true;
        }
        if (item.identifySimpleNoIdentifier().equalsIgnoreCase(comparedto)) {
            return true;
        }
        item.setDurability((short) 0);
        if (item.identifyNoIdentifier().equalsIgnoreCase(comparedto)) {
            return true;
        }
        if (item.identifyMaterialNoIdentifier().equalsIgnoreCase(comparedto)) {
            return true;
        }
        return false;
    }

    public boolean tryMaterial(dMaterial mat, String comparedto) {
        if (comparedto == null || comparedto.length() == 0) {
            return false;
        }
        if (comparedto.equalsIgnoreCase("block") || comparedto.equalsIgnoreCase("material")) {
            return true;
        }
        if (mat.identifyNoIdentifier().equalsIgnoreCase(comparedto)) {
            return true;
        }
        if (mat.identifySimpleNoIdentifier().equalsIgnoreCase(comparedto)) {
            return true;
        }
        if (mat.identifyFullNoIdentifier().equalsIgnoreCase(comparedto)) {
            return true;
        }
        return false;
    }

    public boolean tryEntity(dEntity entity, String comparedto) {
        if (comparedto == null || comparedto.length() == 0 || entity == null) {
            return false;
        }
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("entity")) {
            return true;
        }
        if (comparedto.equals("npc")) {
            return entity.isCitizensNPC();
        }
        if (comparedto.equals("player")) {
            return entity.isPlayer();
        }
        if (entity.getEntityScript() != null && comparedto.equals(CoreUtilities.toLowerCase(entity.getEntityScript()))) {
            return true;
        }
        if (comparedto.equals(entity.getEntityType().getLowercaseName())) {
            return true;
        }
        return false;
    }

}
