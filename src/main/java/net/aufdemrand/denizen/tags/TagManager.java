package net.aufdemrand.denizen.tags;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.tags.core.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Calls a bukkit event for replaceable tags.
 *
 * @author Jeremy Schroeder
 *
 */

public class TagManager implements Listener {

    public static class TagContext {
        public final dPlayer player;
        public final dNPC npc;
        public final boolean instant;
        public final ScriptEntry entry;
        public final boolean debug;
        public TagContext(dPlayer player, dNPC npc, boolean instant, ScriptEntry entry, boolean debug) {
            this.player = player;
            this.npc = npc;
            this.instant = instant;
            this.entry = entry;
            this.debug = debug;
        }
    }

    public Denizen denizen;

    public TagManager(Denizen denizen) {
        this.denizen = denizen;

    }

    public void registerCoreTags() {
        // Objects
        new CuboidTags(denizen);
        new EntityTags(denizen);
        new ListTags(denizen);
        new LocationTags(denizen);
        new NPCTags(denizen);
        new PlayerTags(denizen);
        new QueueTags(denizen);
        new ScriptTags(denizen);

        // Utilities
        new UtilTags(denizen);
        new ProcedureScriptTag(denizen);
        new ContextTags(denizen);
        new TextTags(denizen);
        new EscapeTags(denizen);
        new DefinitionTags(denizen);
        new ParseTags(denizen);

        // For compatibility
        new AnchorTags(denizen);
        new FlagTags(denizen);
        new ConstantTags(denizen);
        new NotableLocationTags(denizen);

        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    // INTERNAL MAPPING NOTE:
    // 0x01: <
    // 0x02: >
    // 0x04: Exclusively For Utilities.talkToNPC()
    // 0x05: |
    // 0x2011: ;

    /**
     * Cleans escaped symbols generated within Tag Manager so that
     * they can be parsed now.
     *
     * @param input the potentially escaped input string.
     * @return the cleaned output string.
     */
    public static String cleanOutput(String input) {
        if (input == null) return null;
        return input.replace((char)0x01, '<')
                    .replace((char)0x02, '>')
                    .replace((char)0x03, '[')
                    .replace((char)0x06, ']')
                    /*.replace((char)0x2011, ';')*/
                    .replace(dList.internal_escape_char, '|');
    }

    /**
     * Cleans any potential internal escape characters (secret characters
     * used to hold the place of symbols that might get parsed weirdly
     * like > or | ) back into their proper form. Use this function
     * when outputting information that is going to be read by a
     * person.
     *
     * @param input the potentially escaped input string.
     * @return the cleaned output string.
     */
    public static String cleanOutputFully(String input) {
        if (input == null) return null;
        return input.replace((char)0x01, '<')
                    .replace((char)0x02, '>')
                    .replace((char)0x2011, ';')
                    .replace(dList.internal_escape_char, '|')
                    .replace((char)0x00A0, ' ')
                    .replace((char)0x03, '[')
                    .replace((char)0x06, ']');
    }

    public static String escapeOutput(String input) {
        if (input == null) return null;
        return input.replace('|', dList.internal_escape_char)
                    .replace('<', (char)0x01)
                    .replace('>', (char)0x02)
                    .replace('[', (char)0x03)
                    .replace(']', (char)0x06);
    }

    @EventHandler
    public void fetchObject(ReplaceableTagEvent event) {
        if (!event.getName().contains("@")) return;

        String object_type = StringUtils.split(event.getName(), '@')[0].toLowerCase();
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
                dB.echoError(((event.hasNameContext() ? event.getName() + '[' + event.getNameContext() + ']'
                        : event.getName()) + " is an invalid dObject!"));
                return;
            }

            Attribute attribute = event.getAttributes();
            event.setReplaced(arg.getAttribute(attribute.fulfill(1)));
        } catch (Exception e) {
            dB.echoError("Uh oh! Report this to the Denizen developers! Err: TagManagerObjectReflection");
            dB.echoError(e);
        }
    }

    public static String readSingleTag(String str, TagContext context) {
        ReplaceableTagEvent event = new ReplaceableTagEvent(context.player, context.npc, str, context.entry);
        if (event.isInstant() != context.instant) {
            // Not the right type of tag, escape the brackets so it doesn't get parsed again
            return String.valueOf((char)0x01) + str + String.valueOf((char)0x02);
        } else {
            // Call Event
            Bukkit.getServer().getPluginManager().callEvent(event);
            if ((!event.replaced() && event.getAlternative() != null)
                    || (event.getReplaced().equals("null") && event.hasAlternative()))
                event.setReplaced(event.getAlternative());
            if (context.debug)
                dB.echoDebug(context.entry, "Filled tag <" + event.toString() + "> with '" +
                        event.getReplaced() + "'.");
            if (!event.replaced())
                dB.echoError(context.entry != null ? context.entry.getResidingQueue(): null, "Tag '" + event.getReplaced() + "' is invalid!");
            return escapeOutput(event.getReplaced());
        }
    }


    public static String tag(dPlayer player, dNPC npc, String arg) {
        return tag(player, npc, arg, false, null);
    }

    public static String tag(dPlayer player, dNPC npc, String arg, boolean instant) {
        return tag(player, npc, arg, instant, null);
    }

    public static String tag(dPlayer player, dNPC npc, String arg, boolean instant, ScriptEntry scriptEntry) {
        try {
            return tag(player, npc, arg, instant, scriptEntry, dB.shouldDebug(scriptEntry));
        }
        catch (Exception e) {
            dB.echoError(e);
            return null;
        }
    }


    public static String tag(dPlayer player, dNPC npc, String arg, boolean instant, ScriptEntry scriptEntry, boolean debug) {
        return tag(arg, new TagContext(player, npc, instant, scriptEntry, debug));
    }

    public static String tag(String arg, TagContext context) {
        if (arg == null) return null;

        // confirm there are/is a replaceable TAG(s), if not, return the arg.
        if (arg.indexOf('>') == -1 || arg.length() < 3) return cleanOutput(arg);

        // Parse \escaping down to internal escaping.
        if (!context.instant) arg = arg.replace("\\<", String.valueOf((char)0x01)).replace("\\>", String.valueOf((char)0x02));

        // Find location of the first tag
        int[] positions = locateTag(arg);
        if (positions == null) {
            return cleanOutput(arg);
        }

        int failsafe = 0;
        do {
            // Just in case, do-loops make me nervous, but does implement a limit of 25 tags per argument.
            failsafe++;
            if (positions == null) break;
            else {
                String oriarg = arg.substring(positions[0] + 1, positions[1]);
                String replaced = readSingleTag(oriarg, context);
                arg = arg.substring(0, positions[0]) + replaced + arg.substring(positions[1] + 1, arg.length());
            }
            // Find new tag
            positions = locateTag(arg);
        } while (positions != null || failsafe < 50);

        return cleanOutput(arg);
    }

    private static int[] locateTag(String arg) {
        int first = arg.indexOf('<');
        if (first == -1)
            return null;
        // Handle "<-" for the flag command
        if (first + 1 < arg.length() && (arg.charAt(first + 1) == '-')) {
            return locateTag(arg.substring(0, first) + (char)0x01 + arg.substring(first + 1));
        }
        int len = arg.length();
        int bracks = 0;
        int second = -1;
        for (int i = first + 1; i < len; i++) {
            if (arg.charAt(i) == '<') {
                bracks++;
            }
            else if (arg.charAt(i) == '>') {
                bracks--;
                if (bracks == -1) {
                    second = i;
                    break;
                }
            }
        }
        if (first > -1 && second > first)
            return new int[]{first, second};
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
