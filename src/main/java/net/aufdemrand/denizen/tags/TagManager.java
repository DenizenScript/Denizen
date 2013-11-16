package net.aufdemrand.denizen.tags;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.ObjectFetcher;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.tags.core.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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

public class TagManager implements Listener {

    public Denizen denizen;

    public TagManager(Denizen denizen) {
        this.denizen = denizen;

    }

    public void registerCoreTags() {
        new PlayerTags(denizen);
        new NPCTags(denizen);
        new LocationTags(denizen);

        new UtilTags(denizen);
        new ProcedureScriptTag(denizen);
        new ContextTags(denizen);
        new TextTags(denizen);

        // For compatibility
        new AnchorTags(denizen);
        new FlagTags(denizen);
        new ConstantTags(denizen);
        new NotableLocationTags(denizen);

        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void fetchObject(ReplaceableTagEvent event) {
        if (!event.getName().contains("@")) return;

        String object_type = event.getName().split("@")[0].toLowerCase();
        Class object_class = ObjectFetcher.getObjectClass(object_type);

        if (object_class == null) {
            dB.echoError("Invalid object type! Could not fetch '" + object_type + "'!");
            event.setReplaced("null");
            return;
        }

        dObject arg;
        try {

            if (!ObjectFetcher.checkMatch(object_class, event.hasNameContext() ? event.getName() + '[' + event.getNameContext() + ']'
                    : event.getName())) {
                dB.echoDebug(event.getScriptEntry(), "Returning null. '" + event.getName()
                        + "' is an invalid " + object_class.getSimpleName() + ".");
                event.setReplaced("null");
                return;
            }

            arg = ObjectFetcher.getObjectFrom(object_class, event.hasNameContext() ? event.getName() + '[' + event.getNameContext() + ']'
                            : event.getName());

            if (arg == null) {
                dB.echoError((event.hasNameContext() ? event.getName() + '[' + event.getNameContext() + ']'
                        : event.getName() + " is an invalid dObject!"));
                return;
            }

            Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());
            event.setReplaced(arg.getAttribute(attribute.fulfill(1)));
        } catch (Exception e) {
            dB.echoError("Uh oh! Report this to aufdemrand! Err: TagManagerObjectReflection");
            e.printStackTrace();
        }
    }


    public static String tag(dPlayer player, dNPC npc, String arg) {
        return tag(player, npc, arg, false, null);
    }

    public static String tag(dPlayer player, dNPC npc, String arg, boolean instant) {
        return tag(player, npc, arg, instant, null);
    }

    public static String tag(dPlayer player, dNPC npc, String arg, boolean instant, ScriptEntry scriptEntry) {
        if (arg == null) return null;

        // confirm there are/is a replaceable TAG(s), if not, return the arg.
        if (arg.indexOf('>') == -1 || arg.length() < 3) return arg;

        // Find location of the first tag
        int[] positions = locateTag(arg);
        if (positions == null) {
            // Un-escape \<, \>
            if (!instant) arg = arg.replace("\\<", "<").replace("\\>", ">");
            return arg;
        }

        int failsafe = 0;
        do {
            // Just in case, do-loops make me nervous, but does implement a limit of 25 tags per argument.
            failsafe++;
            ReplaceableTagEvent event;
            if (positions == null) break;
            else {
                event = new ReplaceableTagEvent(player, npc, arg.substring(positions[0] + 1, positions[1]), scriptEntry);
                if (event.isInstant() != instant) {
                    // Not the right type of tag, change out brackets so it doesn't get parsed again
                    arg = arg.substring(0, positions[0]) + String.valueOf((char)0x01)
                            + event.getReplaced().replace('|', (char)0x05) + String.valueOf((char)0x02) + arg.substring(positions[1] + 1, arg.length());
                } else {
                    // Call Event
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if ((!event.replaced() && event.getAlternative() != null)
                            || (event.getReplaced().equals("null") && event.getAlternative() != null))
                        event.setReplaced(event.getAlternative());
                    arg = arg.substring(0, positions[0]) + event.getReplaced().replace('|', (char)0x05) + arg.substring(positions[1] + 1, arg.length());
                }
            }
            // Find new TAG
            positions = locateTag(arg);
        } while (positions != null || failsafe < 50);

        // Return argument with replacements
        arg = arg.replace((char)0x01, '<').replace((char)0x02, '>').replace((char)0x05, '|');

        // Un-escape \< \>'s
        if (!instant) arg = arg.replace("\\<", "<").replace("\\>", ">");
        return arg;
    }

    // Match all < > brackets that don't contain < > inside them
    private static Pattern tagRegex = Pattern.compile("<([^<>]+)>");

    private static int[] locateTag(String arg) {
        // find escaped brackets
        arg = arg.replace("\\<", "  ")
            .replace("\\>", "  ");

        // find tag brackets pattern
        Matcher tagMatcher = tagRegex.matcher(arg);
        if (tagMatcher.find())
            return new int[]{tagMatcher.start(), tagMatcher.end() - 1};
            // no matching brackets pattern, return null
        else return null;
    }

    public static List<String> fillArguments(List<String> args, ScriptEntry scriptEntry) {
        return fillArguments(args, scriptEntry, false);
    }

    public static List<String> fillArguments(List<String> args, ScriptEntry scriptEntry, boolean instant) {
        List<String> filledArgs = new ArrayList<String>();

        int nested_level = 0;
        if (args != null) {
            for (String argument : args) {
                // Check nested level to avoid filling tags prematurely.
                if (argument.equals("{")) nested_level++;
                if (argument.equals("}")) nested_level--;
                // If this argument isn't nested, fill the tag.
                if (nested_level < 1) {
                    filledArgs.add(tag(scriptEntry.getPlayer(), scriptEntry.getNPC(), argument, instant, scriptEntry));
                }
                    else filledArgs.add(argument);
            }
        }
        return filledArgs;
    }

    public static List<String> fillArguments(String[] args, dPlayer player, dNPC npc) {
        List<String> filledArgs = new ArrayList<String>();
        if (args != null) {
            for (String argument : args) {
                filledArgs.add(tag(player, npc, argument, false));
            }
        }
        return filledArgs;
    }
}
