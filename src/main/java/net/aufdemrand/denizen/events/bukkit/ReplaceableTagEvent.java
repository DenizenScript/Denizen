package net.aufdemrand.denizen.events.bukkit;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.tags.Attribute;

import net.aufdemrand.denizen.tags.TagManager;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event that fires on the finding of a replaceable tag, as indicated by surrounding < >'s.
 */
public class ReplaceableTagEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final dPlayer player;
    private final dNPC npc;

    private boolean instant = false;
    private boolean wasReplaced = false;

    private String alternative = null;
    private boolean alternative_tagged = false;
    private String replaced = null;
    private String value = null;
    private boolean value_tagged = false;
    private Attribute core_attributes = null;

    private ScriptEntry scriptEntry = null;

    public String raw_tag;

    private dScript script;

    ////////////
    // Constructors

    public ReplaceableTagEvent(dPlayer player, dNPC npc, String tag) {
        this(player, npc, tag, null, null);
    }

    public ReplaceableTagEvent(dPlayer player, dNPC npc, String tag, ScriptEntry scriptEntry, dScript script) {

        // Reference ScriptEntry if available
        this.scriptEntry = scriptEntry;
        this.script = script;

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
        int alternativeLoc = locateAlternative(tag);

        if (alternativeLoc >= 0) {
            // get rid of the || at the alternative's start and any trailing spaces
            alternative = tag.substring(alternativeLoc + 2).trim();
            // remove found alternative from tag
            tag = tag.substring(0, alternativeLoc);
        }

        // Get value (if present)
        int valueLoc = locateValue(tag);

        if (valueLoc > 0) {
            value = tag.substring(valueLoc + 1);
            tag = tag.substring(0, valueLoc);
        }

        // Alternatives are stripped, value is stripped, let's remember the raw tag for the attributer.
        raw_tag = tag.trim();

        // Use Attributes system to get type/subtype/etc. etc. for 'static/legacy' tags.
        core_attributes = new Attribute(raw_tag, scriptEntry);
        core_attributes.setHadAlternative(hasAlternative());
    }

    private int locateValue(String tag) {
        int bracks = 0;
        int bracks2 = 0;
        for (int i = 0; i < tag.length(); i++) {
            char c = tag.charAt(i);
            if (c == '<')
                bracks++;
            else if (c == '>')
                bracks--;
            else if (bracks == 0 && c == '[')
                bracks2++;
            else if (bracks == 0 && c == ']')
                bracks2--;
            else if (c == ':' && bracks == 0 && bracks2 == 0) {
                return i;
            }
        }
        return -1;
    }

    private int locateAlternative(String tag) {
        int bracks = 0;
        int bracks2 = 0;
        boolean previousWasTarget = false;
        for (int i = 0; i < tag.length(); i++) {
            char c = tag.charAt(i);
            if (c == '<')
                bracks++;
            else if (c == '>')
                bracks--;
            else if (bracks == 0 && c == '[')
                bracks2++;
            else if (bracks == 0 && c == ']')
                bracks2--;
            else if (c == '|' && bracks == 0 && bracks2 == 0) {
                if (previousWasTarget) {
                    return i - 1;
                }
                else {
                    previousWasTarget = true;
                }
            }
            else
                previousWasTarget = false;
        }
        return -1;
    }


    // Matches method (checks first attribute (name) of the tag)

    // TODO: Remove in 1.0!
    public boolean matches(String tagName) {
        String[] tagNames = StringUtils.split(tagName, ',');
        String name = getName();
        for (String string: tagNames)
            if (name.equalsIgnoreCase(string.trim())) return true;
        return false;
    }

    public boolean matches(String... tagNames) {
        String name = getName();
        for (String string: tagNames)
            if (name.equalsIgnoreCase(string.trim())) return true;
        return false;
    }


    private String StripContext(String input) {
        if (input == null)
            return null;
        int index = input.indexOf('[');
        if (index < 0 || !input.endsWith("]"))
            return input;
        else
            return input.substring(0, index);
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
        if (value_tagged)
            return value;
        value_tagged = true;
        value = TagManager.cleanOutputFully(TagManager.tag(
                getPlayer(), getNPC(), value, false, getScriptEntry()));
        return value;
    }

    public boolean hasValue() {
        return value != null;
    }

    // Alternative

    public String getAlternative() {
        if (alternative_tagged)
            return alternative;
        alternative_tagged = true;
        alternative = TagManager.cleanOutputFully(TagManager.tag(
                getPlayer(), getNPC(), alternative, false, getScriptEntry()));
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

    public dScript getScript() {
        return script;
    }

    public boolean replaced() {
        return wasReplaced && replaced != null;
    }

    public void setReplaced(String string) {
        replaced = string;
        wasReplaced = string != null;
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

    @Override
    public String toString() {
        return core_attributes.toString() + (hasValue() ? ":" + value: "") + (hasAlternative() ? "||" + alternative: "");
    }
}
