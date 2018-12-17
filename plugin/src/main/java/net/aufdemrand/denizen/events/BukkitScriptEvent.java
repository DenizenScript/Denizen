package net.aufdemrand.denizen.events;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;

import java.util.List;
import java.util.regex.Pattern;

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

        String it = CoreUtilities.getXthArg(index + 1, lower);
        if (it.equals("notable")) {
            String subit = CoreUtilities.getXthArg(index + 2, lower);
            if (subit.equals("cuboid")) {
                return dCuboid.getNotableCuboidsContaining(location).size() > 0;
            }
            else if (subit.equals("ellipsoid")) {
                return dEllipsoid.getNotableEllipsoidsContaining(location).size() > 0;
            }
            else {
                dB.echoError("Invalid event 'IN ...' check [" + getName() + "] ('in notable ???'): '" + s + "' for " + scriptContainer.getName());
                return false;
            }
        }
        else if (dWorld.matches(it)) {
            return CoreUtilities.toLowerCase(location.getWorld().getName()).equals(it);
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
        String with = getSwitch(lower, "with");
        if (with != null) {
            if (with.equals("item")) {
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

    private static final String ASTERISK_QUOTED = Pattern.quote("*");

    public String regexHandle(String input) {
        if (input.startsWith("regex:")) {
            return input.substring("regex:".length());
        }
        if (input.contains("*")) {
            return Pattern.quote(input).replace(ASTERISK_QUOTED, "(.*)");
        }
        return null;
    }

    public boolean equalityCheck(String input, String compared, String regexed) {
        input = CoreUtilities.toLowerCase(input);
        return input.equals(compared) || (regexed != null && input.matches(regexed));
    }

    public boolean tryItem(dItem item, String comparedto) {
        if (comparedto == null || comparedto.isEmpty() || item == null) {
            return false;
        }
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("item")) {
            return true;
        }
        if (comparedto.equals("potion") && CoreUtilities.toLowerCase(item.getItemStack().getType().name()).contains("potion")) {
            return true;
        }
        dMaterial quickOf = dMaterial.quickOfNamed(comparedto);
        if (quickOf != null) {
            dMaterial mat = item.getMaterial();
            if (quickOf.getMaterial() != mat.getMaterial()) {
                return false;
            }
            if (quickOf.equals(mat)) {
                return true;
            }
        }
        String regexd = regexHandle(comparedto);
        item = new dItem(item.getItemStack().clone());
        item.setAmount(1);
        if (equalityCheck(item.identifyNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(item.identifyMaterialNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(item.identifySimpleNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(item.identifyNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        item.setDurability((short) 0);
        if (equalityCheck(item.identifyMaterialNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        return false;
    }

    public boolean tryMaterial(dMaterial mat, String comparedto) {
        if (comparedto == null || comparedto.isEmpty() || mat == null) {
            return false;
        }
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("block") || comparedto.equals("material")) {
            return true;
        }
        dMaterial quickOf = dMaterial.quickOfNamed(comparedto);
        if (quickOf != null) {
            if (quickOf.getMaterial() != mat.getMaterial()) {
                return false;
            }
            if (quickOf.equals(mat)) {
                return true;
            }
        }
        String regexd = regexHandle(comparedto);
        if (equalityCheck(mat.realName(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(mat.identifyNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(mat.identifySimpleNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(mat.identifyFullNoIdentifier(), comparedto, regexd)) {
            return true;
        }
        return false;
    }

    public boolean tryEntity(dEntity entity, String comparedto) {
        if (comparedto == null || comparedto.isEmpty() || entity == null) {
            return false;
        }
        Entity bEntity = entity.getBukkitEntity();
        comparedto = CoreUtilities.toLowerCase(comparedto);
        if (comparedto.equals("entity")) {
            return true;
        }
        else if (comparedto.equals("npc")) {
            return entity.isCitizensNPC();
        }
        else if (comparedto.equals("player")) {
            return entity.isPlayer();
        }
        else if (comparedto.equals("vehicle")) {
            return bEntity instanceof Vehicle;
        }
        else if (comparedto.equals("projectile")) {
            return bEntity instanceof Projectile;
        }
        else if (comparedto.equals("hanging")) {
            return bEntity instanceof Hanging;
        }
        String regexd = regexHandle(comparedto);
        if (entity.getEntityScript() != null && equalityCheck(entity.getEntityScript(), comparedto, regexd)) {
            return true;
        }
        else if (equalityCheck(entity.getEntityType().getLowercaseName(), comparedto, regexd)) {
            return true;
        }
        return false;
    }

}
