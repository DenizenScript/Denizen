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
 *
 * @author Jeremy Schroeder
 *
 */

public class Attribute {

    String attribute;

    public Attribute(String attribute) {
        this.attribute = attribute.toLowerCase();
    }

    public boolean startsWith(String string) {
        string = string.toLowerCase();
        if (attribute.startsWith(string)) return true;
        return false;
    }

    public Attribute fulfill(int attributes) {
        if (attribute.split("\\.").length >= attributes)
            attribute = "";
        else attribute = attribute.split("\\.", attributes + 1)[attributes];
        return this;
    }

    public boolean hasContext(int attribute) {
        if (getAttribute(attribute).contains("[")) return true;
        return false;
    }

    public String getContext(int attribute) {
        if (hasContext(attribute))
            return getAttribute(attribute).split("\\[", 2)[1].replace("]", "");
        return null;
    }

    public int getIntContext(int attribute) {
        try {
        if (hasContext(attribute))
            return Integer.valueOf(getAttribute(attribute).split("\\[", 2)[1].replace("]", ""));
        } catch (Exception e) { }

        return 0;
    }

    private String getAttribute(int num) {
        int size = attribute.split("\\.").length;
        if (num > size) return "";
        else return attribute.split("\\.")[num - 1];
    }

}
