package net.aufdemrand.denizen.tags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.tags.core.ColorTags;
import net.aufdemrand.denizen.tags.core.PlayerTags;

/**
 * Calls a bukkit event for replaceable tags.
 * 
 * @author Jeremy Schroeder
 *
 */

public class TagManager {

    public Denizen denizen;

    public TagManager(Denizen denizen) {
        this.denizen = denizen;
    }

    public PlayerTags playerTags;
    public ColorTags colorTags;
    
    public void registerCoreTags() {
        playerTags = new PlayerTags(denizen);
        colorTags = new ColorTags(denizen);
    }
    
    public String tag(Player player, DenizenNPC npc, String arg, boolean instant) {
        if (arg == null) return null;
        // confirm there are/is a replaceable TAG(s), if not, return the arg.
        if (arg.indexOf('>') == -1 || arg.length() < 3) return arg;

        // Find location of the first tag
        int[] positions = locateTag(arg);
        if (positions == null) return arg;

        boolean changeBack = false;
        int failsafe = 0;
        do {
            // Just in case, do-loops make me nervous
            failsafe++;
            ReplaceableTagEvent event = null;
            if (positions == null) break;
            else {
                event = new ReplaceableTagEvent(player, npc, arg.substring(positions[0] + 1, positions[1]));
                if (event.isInstant() != instant) {
                    changeBack = true;
                    // Not the right type of tag, change out brackets so it doesn't get parsed again
                    // TODO: Find better way to do this ;)
                    arg = arg.substring(0, positions[0]) + "{" + event.getReplaceable() + "}" + arg.substring(positions[1] + 1, arg.length());
                } else {
                    // Call Event
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    arg = arg.substring(0, positions[0]) + event.getReplaceable() + arg.substring(positions[1] + 1, arg.length());
                }
            }
            // Find new TAG
            positions = locateTag(arg);
        } while (positions != null || failsafe < 25);
        // Change brackets back
        if (changeBack) arg = arg.replace("{", "<").replace("}", ">");
        // Return argument with replacements
        return arg;
    }

    private int[] locateTag(String arg) {
        // find first >
        int closePos = arg.indexOf('>');
        int openPos = -1;
        // find closest <
        for (int x = closePos; x >= 0; x-- ) {
            if (arg.charAt(x) == '<') {
                openPos = x;
                break;
            }
        }
        // no matching bracket, return null
        if (openPos == -1) return null;
        // return positions of the brackets
        int[] returnable = {openPos, closePos};
        return returnable;
    }
    
    public List<String> fillArguments(List<String> args, ScriptEntry scriptEntry) {
        List<String> filledArgs = new ArrayList<String>();
        if (args != null) {
            for (String argument : args) {
                filledArgs.add(tag(scriptEntry.getPlayer(), scriptEntry.getNPC(), argument, false));
            } 
        }
        return filledArgs;
    }

    public List<String> fillArguments(String[] args, Player player, DenizenNPC npc) {
        List<String> filledArgs = new ArrayList<String>();
        if (args != null) {
            for (String argument : args) {
                filledArgs.add(tag(player, npc, argument, false));
            } 
        }
        return filledArgs;
    }
}
