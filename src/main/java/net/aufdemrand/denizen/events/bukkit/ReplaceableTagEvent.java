package net.aufdemrand.denizen.events.bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.tags.Attribute;

import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event that fires on the finding of a replaceable tag, as indicated by surrounding < >'s.
 *
 * @author Jeremy Schroeder
 *
 * @version 1.0
 *
 */

public class ReplaceableTagEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final dPlayer player;
    private final dNPC npc;

    private boolean instant = false;
    private boolean wasReplaced = false;

    private String alternative = null;
    private String replaced = null;
    private String value = null;
    private Attribute core_attributes = null;

    private ScriptEntry scriptEntry = null;

    // Alternative text pattern that matches everything after ||
    private static Pattern alternativeRegex = Pattern.compile("\\|\\|(.*)", Pattern.DOTALL | Pattern.MULTILINE);

    public String raw_tag;

    ////////////
    // Constructors

    public ReplaceableTagEvent(dPlayer player, dNPC npc, String tag) { this(player, npc, tag, null); }

    public ReplaceableTagEvent(dPlayer player, dNPC npc, String tag, ScriptEntry scriptEntry) {

        // Reference ScriptEntry if available
        this.scriptEntry = scriptEntry;

        // Reference player/npc
        this.player = player;
        this.npc = npc;

        // If tag is not replaced, return the tag
        // TODO: Possibly make this return "null" ... might break some
        // scripts using tags incorrectly, but makes more sense overall
        this.replaced = tag;

        // Check if tag is 'instant'
        if (tag.length() > 0) {
            char start = tag.charAt(0);
            if (start == '!' || start == '^') {
                instant = true;
                tag = tag.substring(1);
            }
        }

        // Get alternative text
        Matcher alternativeMatcher = alternativeRegex.matcher(tag);

        if (alternativeMatcher.find()) {
            // remove found alternative from tag
            tag = tag.substring(0, alternativeMatcher.start()).trim();
            // get rid of the || at the alternative's start and any trailing spaces
            alternative = alternativeMatcher.group(1).trim();
        }

        // Get value (if present)

        if (tag.indexOf(':') > 0) {
            int x1 = -1;
            int braced = 0;

            for (int x = 0; x < tag.length(); x++) {
                Character chr = tag.charAt(x);

                if (chr == '[')
                    braced++;

                else if (chr == ']') {
                    if (braced > 0) braced--;
                }

                else if (chr == ':' && braced == 0 && x != tag.length() - 1 && x > 0) {
                    x1 = x;
                    break;
                }
            }

            if (x1 > -1) {
                value = tag.substring(x1 + 1);
                tag = tag.substring(0, x1);
            }
        }

        // Alternatives are stripped, value is stripped, let's remember the raw tag for the attributer.
        raw_tag = tag;

        // Use Attributes system to get type/subtype/etc. etc. for 'static/legacy' tags.
        core_attributes = new Attribute(raw_tag, scriptEntry);
        core_attributes.setHadAlternative(hasAlternative());
    }


    // Matches method (checks first attribute (name) of the tag)

    public boolean matches(String tagName) {
        String[] tagNames = tagName.split(",");
        String name = getName();
        for (String string: tagNames)
            if (name.equalsIgnoreCase(string.trim())) return true;
        return false;
    }


    private String StripContext(String input) {
        if (input == null)
            return null;
        else
            return Attribute.CONTEXT_PATTERN.matcher(input).replaceAll("");
    }

    ////////
    // Replaceable Tag 'Parts'
    // <name.type.subtype.specifier:value>

    // Name

    public String getName() {
        return StripContext(core_attributes.getAttribute(1));
    }

    public String getNameContext() {
        return core_attributes.getContext(1);
    }

    public boolean hasNameContext() {
        return core_attributes.hasContext(1);
    }

    // Type

    public String getType() {
        return StripContext(core_attributes.getAttribute(2));
    }

    public boolean hasType() {
        return core_attributes.getAttribute(2).length() > 0;
    }

    public String getTypeContext() {
        return core_attributes.getContext(2);
    }

    public boolean hasTypeContext() {
        return core_attributes.hasContext(2);
    }

    // Subtype

    public String getSubType() {
        return StripContext(core_attributes.getAttribute(3));
    }

    public boolean hasSubType() {
        return core_attributes.getAttribute(3).length() > 0;
    }

    public String getSubTypeContext() {
        return core_attributes.getContext(3);
    }

    public boolean hasSubTypeContext() {
        return core_attributes.hasContext(3);
    }

    // Specifier

    public String getSpecifier() {
        return StripContext(core_attributes.getAttribute(4));
    }

    public boolean hasSpecifier() {
        return core_attributes.getAttribute(4).length() > 0;
    }

    public String getSpecifierContext() {
        return core_attributes.getContext(4);
    }

    public boolean hasSpecifierContext() {
        return core_attributes.hasContext(4);
    }

    // Value

    public String getValue() {
        return value;
    }

    public boolean hasValue() {
        return value != null;
    }

    // Alternative

    public String getAlternative() {
        return alternative;
    }

    public boolean hasAlternative() {
        return alternative != null;
    }

    // Other internal mechanics

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

    public boolean replaced() {
        return wasReplaced;
    }

    public void setReplaced(String string) {
        replaced = string;
        wasReplaced = true;
    }

    public boolean hasScriptEntryAttached() {
        return scriptEntry != null;
    }

    public ScriptEntry getScriptEntry() {
        return scriptEntry;
    }


    /**
     * Gets an Attribute object for easy parsing/reading
     * of the different tag attributes.
     *
     * @return attributes
     */

    public Attribute getAttributes() {
        return core_attributes;
    }
}
