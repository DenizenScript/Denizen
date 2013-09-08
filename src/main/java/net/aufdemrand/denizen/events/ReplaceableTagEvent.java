package net.aufdemrand.denizen.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event that fires on the finding of a dScript replaced tag called
 * from argument creation (if a QUICKTAG '^') and upon execution. Replaceable
 * tags are enclosed in '< >'s.
 *
 * Tag Structure:
 * <^NAME[CONTEXT].TYPE[CONTEXT].SUBTYPE[CONTEXT].SPECIFIER[CONTEXT]:VALUE || FALLBACK VALUE>
 *
 * ^ - Optional. Specifies a QUICKTAG which is wasReplaced upon creation of arguments.
 *   Used in buildArgs(). If not used, replacement is done in Executer's execute()
 * FALLBACK VALUE is used internally to specify the replace value if nothing else
 *   is substituted. Must be in '( )'s.
 *
 * Examples:
 * <PLAYER.NAME>
 * <PLAYER.ITEM_IN_HAND.LORE[1] || None.>
 * <NPC.NAME.NICKNAME>
 * <^FLAG.D:FRIENDS>
 *
 * @author Jeremy Schroeder, David Cernat
 *
 */

public class ReplaceableTagEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private dPlayer player;
    private dNPC npc;

    private boolean instant = false;
    private boolean wasReplaced = false;

    private String baseContext = null;

    private String name = null;
    private String nameContext = null;

    private String type = null;
    private String typeContext = null;

    private String subType = null;
    private String subTypeContext = null;

    private String specifier = null;
    private String specifierContext = null;

    private String value = null;
    private String valueContext = null;

    private String alternative = null;
    private String replaced = null;

    private ScriptEntry scriptEntry = null;

    // Base context pattern that matches initial brackets in tag
    Pattern basecontextRegex = Pattern.compile("^( )?\\[.*?\\]");

    // Alternative text pattern that matches everything after ||
    Pattern alternativeRegex = Pattern.compile("\\|\\|.*");

    // Bracket pattern that matches brackets
    Pattern bracketRegex = Pattern.compile("\\[.*?\\]");

    // Value pattern that matches everything after the last : found
    // that isn't followed by ] without being followed by [ first,
    // and is therefore not between brackets
    Pattern valueRegex = Pattern.compile(":[^\\]]+(\\[([^\\[])+)?$");

    // Component pattern that matches groups of characters that are not
    // [] or . and that optionally contain [] and a . at the end
    Pattern componentRegex = Pattern.compile("[^\\[\\]\\.]+(\\[.*?\\])?(\\.)?");

    public String raw_tag;

    public ReplaceableTagEvent(dPlayer player, dNPC npc, String tag) {
        this(player, npc, tag, null);
    }

    public ReplaceableTagEvent(dPlayer player, dNPC npc, String tag, ScriptEntry scriptEntry) {

        // Add ScriptEntry if available
        this.scriptEntry = scriptEntry;

        this.player = player;

        this.replaced = tag;
        this.npc = npc;

        // check if tag is 'instant'
        if (tag.startsWith("!") || tag.startsWith("^"))
        {
            instant = true;
            tag = tag.substring(1);
        }

        // Get alternative text
        Matcher alternativeMatcher = alternativeRegex.matcher(tag);

        if (alternativeMatcher.find())
        {
            tag = tag.substring(0, alternativeMatcher.start()).trim(); // remove found alternative from tag
            alternative = alternativeMatcher.group()
                    .substring(2).trim(); // get rid of the || at the alternative's start
            // and any trailing spaces
        }

        // Alternatives are stripped, base context is stripped, let's remember the raw tag for
        // the attributer.
        raw_tag = tag;

        // Get value
        Matcher bracketMatcher = null;
        Matcher valueMatcher = valueRegex.matcher(tag);

        if (valueMatcher.find())
        {
            tag = tag.substring(0, valueMatcher.start()); // remove found value from tag

            value = valueMatcher.group().substring(1); // get rid of the : at the value's start
            bracketMatcher = bracketRegex.matcher(value);

            if (bracketMatcher.find())
            {
                valueContext = bracketMatcher.group().replace("[", "")
                        .replace("]", "");
                value = value.substring(0, bracketMatcher.start()) +
                        value.substring(bracketMatcher.end());
            }
        }

        // Get name, type, subType and specifier, and all their contexts
        String[] components = new String[4];
        String[] contexts = new String[4];
        String tagPart = null;
        int n = 0;

        Matcher componentMatcher = componentRegex.matcher(tag);

        while (componentMatcher.find() && n < 4)
        {
            tagPart = componentMatcher.group();
            bracketMatcher = bracketRegex.matcher(tagPart);

            if (bracketMatcher.find())
            {
                components[n] = tagPart.substring(0, bracketMatcher.start());
                contexts[n] = bracketMatcher.group().replace("[", "")
                        .replace("]", "");
            }
            else
                components[n] = tagPart.replace(".", "");

            n++;
        }

        name = components[0];
        nameContext = contexts[0];
        type = components[1];
        typeContext = contexts[1];
        subType = components[2];
        subTypeContext = contexts[2];
        specifier = components[3];
        specifierContext = contexts[3];
    }

    public String getName() {
        return name;
    }

    public String getNameContext() {
        return nameContext;
    }

    public boolean hasNameContext() {
        return nameContext != null;
    }

    public String getType() {
        return type;
    }

    public boolean hasType() {
        return type != null;
    }

    public String getTypeContext() {
        return typeContext;
    }

    public boolean hasTypeContext() {
        return typeContext != null;
    }

    public String getSubType() {
        return subType;
    }

    public boolean hasSubType() {
        return subType != null;
    }

    public String getSubTypeContext() {
        return subTypeContext;
    }

    public boolean hasSubTypeContext() {
        return subTypeContext != null;
    }

    public String getSpecifier() {
        return specifier;
    }

    public boolean hasSpecifier() {
        return specifier != null;
    }

    public String getSpecifierContext() {
        return specifierContext;
    }

    public boolean hasSpecifierContext() {
        return specifierContext != null;
    }

    public String getValue() {
        return value;
    }

    public boolean hasValue() {
        return value != null;
    }

    public String getValueContext() {
        return valueContext;
    }

    public boolean hasValueContext() {
        return valueContext != null;
    }

    public String getAlternative() {
        return alternative;
    }

    public boolean hasAlternative() {
        return alternative != null;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public dNPC getNPC() {
        return npc;
    }

    public dPlayer getPlayer() {
        return player;
    }

    public String getReplaced() {
        return replaced;
    }

    public boolean isInstant() {
        return instant;
    }

    public boolean matches(String tagName) {
        String[] tagNames = tagName.split(",");
        for (String string: tagNames)
            if (this.name.equalsIgnoreCase(string.trim())) return true;
        return false;
    }

    public boolean replaced() {
        return wasReplaced;
    }

    public void setReplaced(String string) {
        replaced = string;
        wasReplaced = true;
    }

    // TODO: Remove in 1.0
    @Deprecated
    private void parseContext() {
        dB.echoDebug("Using 'context' in this way has been deprecated, as it is now possible " +
                "to specify specific objects.");
    }

    public boolean hasScriptEntryAttached() {
        return scriptEntry != null;
    }

    public ScriptEntry getScriptEntry() {
        return scriptEntry;
    }

    @Override
    public String toString() {
        return  (instant ? "Instant=true," : "")
                + "Player=" + (player != null ? player.identify() : "null") + ", "
                + "NPC=" + (npc != null ? npc.getName() : "null") + ", "
                + "NAME=" + (nameContext != null ? name + "(" + nameContext + "), " : name + ", ")
                + (type != null ? (typeContext != null ? "TYPE=" + type + "(" + typeContext + "), " : "TYPE=" + type + ", ") : "" )
                + (subType != null ? (subTypeContext != null ? "SUBTYPE=" + subType + "(" + subTypeContext + "), " : "SUBTYPE=" + subType + ", ") : "")
                + (specifier != null ? (specifierContext != null ? "SPECIFIER=" + specifier + "(" + specifierContext + "), " : "SPECIFIER=" + specifier + ", ") : "")
                + (value != null ? (valueContext != null ? "VALUE=" + value + "(" + valueContext + "), " : "VALUE=" + value + ", ") : "")
                + (alternative != null ? "ALTERNATIVE=" + alternative + ", " : "");
    }

}
