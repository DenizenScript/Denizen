package net.aufdemrand.denizen.events;

import net.aufdemrand.denizen.npc.DenizenNPC;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * Bukkit event that fires on the finding of a dScript replaceable tag called
 * from argument creation (if a QUICKTAG '^') and upon execution. Replaceable
 *  tags are enclosed in '< >'s.
 * 
 * Tag Structure:
 * <^NAME.TYPE:VALUE[INDEX](FALLBACK VALUE)>
 * 
 * ^ - Optional. Specifies a QUICKTAG which is replaced upon creation of arguments.
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
    private boolean replaced = false;
    
    private String name;
    private String replaceable;
    
    private String value = null;
    private String type = null;
    private String index = null;
    private String fallback = null;
    
    public ReplaceableTagEvent(Player player, DenizenNPC npc, String tag) {
        if (tag.split("\\(").length > 1) {
            fallback = tag.split("\\(", 2)[1];
            tag = tag.split("\\(", 2)[0];
        }
        this.player = player;
        this.npc = npc;
        name = tag.split(":", 2)[0];
        if (name.contains(".")) {
            type = name.split("\\.", 2)[1];
            name = name.split("\\.", 2)[0];
        }
        if (tag.split(":").length > 1)
            value = tag.split(":", 2)[1];
        if (name.startsWith("^")) {
            instant = true;
            name = name.substring(1);
        }
        if (value != null && value.contains("[")) {
            index = value.split("\\[")[1].replace("]", "");
            name = value.split("\\[")[0];
        }
        replaceable = tag;
    }
    
    public String getFallback() {
        return fallback;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getIndex() {
        return index != null ? index : "";
    }

    public String getName() {
        return name;
    }

    public DenizenNPC getNPC() {
        return npc;
    }

    public Player getPlayer() {
        return player;
    }

    public String getReplaceable() {
        return replaceable;
    }

    public String getType() {
        return type != null ? type : "";
    }

    public String getValue() {
        return value != null ? value : "";
    }

    public boolean isInstant() {
        return instant;
    }

    public boolean matches(String tagName) {
        if (!this.name.equalsIgnoreCase(tagName)) return false;
        return true;
    }
    
    public boolean replaced() {
        return replaced;
    }

    public void setReplaceable(String string) {
        replaceable = string;
        replaced = true;
    }

    @Override
    public String toString() {
        return instant ? "Instant Tag " : "Tag '" + name + "." + type + "' with value '" + value + "'";
    }
}