package net.aufdemrand.denizen.events;

import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event that fires on the finding of a dScript replaced tag called
 * from argument creation (if a QUICKTAG '^') and upon execution. Replaceable
 *  tags are enclosed in '< >'s.
 *
 * Tag Structure:
 * <^NAME.TYPE:VALUE[INDEX](FALLBACK VALUE)>
 *
 * ^ - Optional. Specifies a QUICKTAG which is wasReplaced upon creation of arguments.
 *   Used in buildArgs(). If not used, replacement is done in Executer's execute() 
 * FALLBACK VALUE is used internally to specify the replace value if nothing else
 *   is substituted. Must be in '( )'s. 
 *
 * Examples:
 * <PLAYER.NAME>
 * <PLAYER.ITEM_IN_HAND:LORE[1](None.)>
 * <NPC.NAME:NICKNAME>
 * <^FLAG.D:FRIENDS(None.)>
 *
 * @author Jeremy Schroeder
 *
 */

public class ReplaceableTagEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private DenizenNPC npc;

    private boolean instant = false;
    private boolean wasReplaced = false;

    private String baseContext = null;

    private String name = null;
    private String nameContext = null;

    private String type = null;
    private String typeContext = null;

    private String subType = null;
    private String subTypeContext = null;

    private String value = null;
    private String valueContext = null;

    private String alternative = null;
    private String replaced = null;


    public ReplaceableTagEvent(Player player, DenizenNPC npc, String tag) {

        // TODO: Use REGEX and MATCHER/GROUPS to simplify this code (might be faster?)

        this.replaced = tag;
        this.player = player;
        this.npc = npc;

        // check if tag is 'instant'
        if (tag.startsWith("!") || tag.startsWith("^")) {
            instant = true;
            tag = tag.substring(1);
        }

        // check if tag has base context
        if (tag.startsWith("[") || tag.startsWith(" [")) {
            baseContext = tag.split("\\]", 2)[0].split("\\[", 2)[1].trim();
            dB.log(baseContext);
            parseContext();
            tag = tag.split("\\]", 2)[1];
        }

        // check if tag has an alternative text
        if (tag.contains("||")) {
            alternative = tag.split("\\|\\|")[1].trim();
            dB.log(alternative);
            tag = tag.split("\\|\\|", 2)[0];
        }

        // Get value and context
        if (tag.contains(":")) {
            String inQuestion = tag.split(":", 2)[1];
            if (inQuestion.contains("[")) {
                // Get index
                value = inQuestion.split("\\[", 2)[0].trim();
                valueContext = inQuestion.split("\\[", 2)[1].split("\\]", 2)[0].trim();
            } else
                value = inQuestion.trim();

            tag = tag.split(":", 2)[0];
        }

        // Get tag name/type/subtype and index
        if (tag.contains(".")) {

            // Get name
            String inQuestion = tag.split("\\.", 2)[0];
            if (inQuestion.contains("[")) {
                // Get index
                name = inQuestion.split("\\[", 2)[0].trim();
                nameContext = inQuestion.split("\\[", 2)[1].split("\\]", 2)[0].trim();
            } else
                name = inQuestion.trim();

            tag = tag.split("\\.", 2)[1];

            // Get type
            if (tag.contains(".")) {
                // Subtype with type, must split
                inQuestion = tag.split("\\.", 2)[0];
                if (inQuestion.contains("[")) {
                    // Get index
                    type = inQuestion.split("\\[", 2)[0].trim();
                    typeContext = inQuestion.split("\\[", 2)[1].split("\\]", 2)[0].trim();
                } else
                    type = inQuestion.trim();
                tag = tag.split("\\.")[1];

                if (tag.contains("[")) {
                    // Get index
                    subType = inQuestion.split("\\[", 2)[0].trim();
                    subTypeContext = inQuestion.split("\\[", 2)[1].split("\\]", 2)[0].trim();
                } else
                    subType = inQuestion.trim();
                tag = tag.split("\\.")[1];

                // No subtype, just get type, and possible context
            } else {
                if (tag.contains("[")) {
                    // Get index
                    type = tag.split("\\[", 2)[0].trim();
                    typeContext = tag.split("\\[", 2)[1].split("\\]", 2)[0].trim();
                } else
                    type = tag.trim();
            }

        } else name = tag;

    }

    public String getName() {
        return name;
    }

    public String getNameContext() {
        return nameContext;
    }

    public boolean hasNameContext() {
        return nameContext == null ? false : true;
    }

    public String getType() {
        return type;
    }

    public boolean hasType() {
        return type == null ? false : true;
    }

    public String getTypeContext() {
        return typeContext;
    }

    public boolean hasTypeContext() {
        return typeContext == null ? false : true;
    }

    public String getSubType() {
        return subType;
    }

    public boolean hasSubType() {
        return subType == null ? false : true;
    }

    public String getSubTypeContext() {
        return subTypeContext;
    }

    public boolean hasSubTypeContext() {
        return subTypeContext == null ? false : true;
    }

    public String getValue() {
        return value;
    }

    public boolean hasValue() {
        return value == null ? false : true;
    }

    public String getValueContext() {
        return valueContext;
    }

    public boolean hasValueContext() {
        return valueContext == null ? false : true;
    }

    public String getAlternative() {
        return alternative;
    }

    public boolean hasAlternative() {
        return alternative == null ? false : true;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public DenizenNPC getNPC() {
        return npc;
    }

    public Player getPlayer() {
        return player;
    }

    public String getReplaced() {
        return replaced;
    }

    public boolean isInstant() {
        return instant;
    }

    public boolean matches(String tagName) {
        return this.name.equalsIgnoreCase(tagName);
    }

    public boolean replaced() {
        return wasReplaced;
    }

    public void setReplaced(String string) {
        replaced = string;
        wasReplaced = true;
    }

    private void parseContext() {
        if (baseContext == null || baseContext.length() == 1) return;
        LivingEntity entity = null;
        for (String context : baseContext.split("\\|")) {
            entity = aH.getLivingEntityFrom(context);
            if (entity != null) {
                if (CitizensAPI.getNPCRegistry().isNPC(entity))
                    npc = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(entity));

                else if (entity instanceof Player) {
                    player = (Player) entity;
                }
            }
        }
    }

    @Override
    public String toString() {
        return  (instant ? "Instant=true," : "")
                + "Player=" + (player != null ? player.getName() : "null") + ", "
                + "NPC=" + (npc != null ? npc.getName() : "null") + ", "
                + "NAME=" + (nameContext != null ? name + "(" + nameContext + "), " : name + ", ")
                + (type != null ? (typeContext != null ? "TYPE=" + type + "(" + typeContext + "), " : "TYPE=" + type + ", ") : "" )
                + (subType != null ? (subTypeContext != null ? "SUBTYPE=" + subType + "(" + subTypeContext + "), " : "SUBTYPE=" + subType + ", ") : "")
                + (value != null ? (valueContext != null ? "VALUE=" + value + "(" + valueContext + "), " : "VALUE=" + value + ", ") : "")
                + (alternative != null ? "ALTERNATIVE=" + alternative + ", " : "");
    }

}