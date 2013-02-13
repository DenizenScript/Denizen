package net.aufdemrand.denizen.tags;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.tags.core.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void registerCoreTags() {
        new PlayerTags(denizen);
        new OfflinePlayerTags(denizen);
        new ColorTags(denizen);
        new FlagTags(denizen);
        new BookmarkTags(denizen);
        new ConstantTags(denizen);
        new NPCTags(denizen);
        new AnchorTags(denizen);
        new ContextTags(denizen);
        new LocationTags(denizen);
        new SpecialCharacterTags(denizen);
    }

    public String tag(OfflinePlayer player, dNPC npc, String arg, boolean instant) {
        return tag(player, npc, arg, instant, null);
    }

    public String tag(OfflinePlayer player, dNPC npc, String arg, boolean instant, ScriptEntry scriptEntry) {
        if (arg == null) return null;
        // confirm there are/is a replaceable TAG(s), if not, return the arg.
        if (arg.indexOf('>') == -1 || arg.length() < 3) return arg;

        // Find location of the first tag
        int[] positions = locateTag(arg);
        if (positions == null) return arg;

        boolean changeBack = false;
        int failsafe = 0;
        do {
            // Just in case, do-loops make me nervous, but does implement a limit of 25 tags per argument.
            failsafe++;
            ReplaceableTagEvent event;
            if (positions == null) break;
            else {
                event = new ReplaceableTagEvent(player, npc, arg.substring(positions[0] + 1, positions[1]), scriptEntry);
                if (event.isInstant() != instant) {
                    changeBack = true;
                    // Not the right type of tag, change out brackets so it doesn't get parsed again
                    arg = arg.substring(0, positions[0]) + "{" + event.getReplaced() + "}" + arg.substring(positions[1] + 1, arg.length());
                } else {
                    // Call Event
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (!event.replaced() && event.getAlternative() != null) event.setReplaced(event.getAlternative());
                    arg = arg.substring(0, positions[0]) + event.getReplaced() + arg.substring(positions[1] + 1, arg.length());
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
    // Match all < > brackets that don't contain < > inside them
    private static Pattern tagRegex = Pattern.compile("<([^<>]+)>");

    private int[] locateTag(String arg) {
        // find tag brackets pattern
        Matcher tagMatcher = tagRegex.matcher(arg);
        if (tagMatcher.find())
            return new int[]{tagMatcher.start(), tagMatcher.end() - 1};
            // no matching brackets pattern, return null
        else return null;
    }

    public List<String> fillArguments(List<String> args, ScriptEntry scriptEntry) {
        return fillArguments(args, scriptEntry, false);
    }

    public List<String> fillArguments(List<String> args, ScriptEntry scriptEntry, boolean instant) {
        List<String> filledArgs = new ArrayList<String>();
        if (args != null) {
            for (String argument : args) {
                if (scriptEntry.getPlayer() == null && scriptEntry.getOfflinePlayer() != null)
                    filledArgs.add(tag(scriptEntry.getOfflinePlayer(), scriptEntry.getNPC(), argument, instant, scriptEntry));
                else
                    filledArgs.add(tag(scriptEntry.getPlayer(), scriptEntry.getNPC(), argument, instant, scriptEntry));
            }
        }
        return filledArgs;
    }

    public List<String> fillArguments(String[] args, Player player, dNPC npc) {
        List<String> filledArgs = new ArrayList<String>();
        if (args != null) {
            for (String argument : args) {
                filledArgs.add(tag(player, npc, argument, false));
            }
        }
        return filledArgs;
    }
}
