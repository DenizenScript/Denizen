package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.flags.FlagManager.Value;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;

public class FlagTags implements Listener {

    Denizen denizen;
    
    public FlagTags(Denizen denizen) {
        this.denizen = denizen;
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    private enum ReplaceType { LENGTH, SIZE, ASSTRING, ASINT, ASDOUBLE, ASLIST, ASMONEY, ASCSLIST }

    /**
     * Replaces FLAG TAGs. Called automatically by the dScript ScriptBuilder and Executer.
     * 
     * @param event 
     *      ReplaceableTagEvent
     */
    
    @EventHandler
    public void flagTag(ReplaceableTagEvent event) {
        if (!event.matches("FLAG")) return;

        // Replace <FLAG...> TAGs.
        String flagName = event.getValue().split(":").length > 1
                ? event.getValue().split(":")[0].toUpperCase() : event.getValue().toUpperCase();
        String flagFallback = event.getAlternative() != null ? event.getAlternative() : "EMPTY";
        int index = -1;
        ReplaceType replaceType = ReplaceType.ASSTRING;

        // Get format, if specified
        if (flagName.contains(".")) {
            try {
                int replaceTypeIndex = flagName.split("\\.").length - 1;
                replaceType = ReplaceType.valueOf(flagName.split("\\.")[replaceTypeIndex].replace("_", "").toUpperCase());
                flagName = flagName.replace("." + flagName.split("\\.")[replaceTypeIndex], "");
            } catch (Exception e) { e.printStackTrace(); replaceType = ReplaceType.ASSTRING; }
        }

        // Get index, if specified
        if (event.hasValueContext()) {
            try {
                index = Integer.valueOf(event.getValueContext());
            } catch (NumberFormatException e) { index = -1; }
        }

        // Check flag replacement type
        if (event.getType().toUpperCase().startsWith("G")) {
            if (denizen.flagManager().getGlobalFlag(flagName).get(index).isEmpty()) {
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' flag not found, using fallback!", flagName);
            } else {
                event.setReplaced(getReplaceable(denizen.flagManager().getGlobalFlag(flagName).get(index), replaceType));
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value '" + event.getReplaced() + "'.", flagName);
            }

        } else if (event.getType().toUpperCase().startsWith("D") || event.getType().toUpperCase().startsWith("N")) {
            if (denizen.flagManager().getNPCFlag(event.getNPC().getId(), flagName).get(index).isEmpty()) {
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' flag not found, using fallback!", flagName);
            } else {
                event.setReplaced(getReplaceable(denizen.flagManager().getNPCFlag(event.getNPC().getId(), flagName).get(index), replaceType));
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value '" + event.getReplaced() + "'.", flagName);
            }

        } else if (event.getType().toUpperCase().startsWith("P")) {
            // Separate name since subType context may specify a different (or offline) player
            String name = null;
            if (event.getPlayer() != null)
                name = event.getPlayer().getName();
            if (name == null && event.hasOfflinePlayer())
                name = event.getOfflinePlayer().getName();

            // No name? No flag replaceament!
            if (name == null) return;

            if (denizen.flagManager().getPlayerFlag(name, flagName).get(index).isEmpty()) {
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' flag not found, using fallback!", flagName);
            } else {
                event.setReplaced(getReplaceable(
                        denizen.flagManager().getPlayerFlag(name, flagName).get(index), replaceType));
                dB.echoDebug(ChatColor.YELLOW + "//REPLACED//" + ChatColor.WHITE + " '%s' with flag value '" + event.getReplaced() + "'.", flagName);

            }
        }               
    }

    private String getReplaceable(Value flag, ReplaceType replaceType) {
        switch (replaceType) {
        case ASINT:
            return String.valueOf(flag.asInteger());
        case ASDOUBLE:
            return String.valueOf(flag.asDouble());
        case ASSTRING:
            return flag.asString();
        case ASLIST:
            return String.valueOf(flag.asList());
        case ASCSLIST:
            return String.valueOf(flag.asCommaSeparatedList());
        case ASMONEY:
            DecimalFormat d = new DecimalFormat("0.00");
            return String.valueOf(d.format(flag.asDouble()));
        case LENGTH:
            return String.valueOf(flag.asString().length());
        case SIZE:
        	return String.valueOf(flag.asSize());
        }
        return null;
    }
    
}