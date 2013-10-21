package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TextTags implements Listener {

    public TextTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void foreignCharacterTags(ReplaceableTagEvent event) {

        if (!event.getName().startsWith("&")) return;
        Attribute attribute =
                new Attribute(event.raw_tag, event.getScriptEntry());

        // <--[tag]
        // @attribute <&auml>
        // @returns Element
        // @description
        // Returns an umlaut-a symbol: ä
        // -->
        if (event.getName().equals("&auml"))
            event.setReplaced(new Element("ä").getAttribute(attribute.fulfill(1)));

            // <--[tag]
            // @attribute <&Auml>
            // @returns Element
            // @description
            // Returns a capital umlaut-A symbol: Ä
            // -->
        else if (event.getName().equals("&Auml"))
            event.setReplaced(new Element("Ä").getAttribute(attribute.fulfill(1)));

            // <--[tag]
            // @attribute <&ouml>
            // @returns Element
            // @description
            // Returns an umlaut-o symbol: ö
            // -->
        else if (event.getName().equals("&ouml"))
            event.setReplaced(new Element("ö").getAttribute(attribute.fulfill(1)));

            // <--[tag]
            // @attribute <&Iuml>
            // @returns Element
            // @description
            // Returns a capital umlaut-O symbol: Ö
            // -->
        else if (event.getName().equals("&Ouml"))
            event.setReplaced(new Element("Ö").getAttribute(attribute.fulfill(1)));

            // <--[tag]
            // @attribute <&uuml>
            // @returns Element
            // @description
            // Returns an umlaut-u symbol: ü
            // -->
        else if (event.getName().equals("&uuml"))
            event.setReplaced(new Element("ü").getAttribute(attribute.fulfill(1)));

            // <--[tag]
            // @attribute <&Uuml>
            // @returns Element
            // @description
            // Returns a capital umlaut-U symbol: Ü
            // -->
        else if (event.getName().equals("&Uuml"))
            event.setReplaced(new Element("Ü").getAttribute(attribute.fulfill(1)));

    }

    // Thanks geckon :)
    final String[] code = {"0","1","2","3","4","5","6","7","8","9"
            ,"a","b","c","d","e","f","k","l","m","n","o","r"};

    // <--[tag]
    // @attribute <&0>
    // @returns ChatColor
    // @description
    // Makes the following characters Black.
    // -->
    
    // <--[tag]
    // @attribute <&1>
    // @returns ChatColor
    // @description
    // Makes the following characters Dark Blue.
    // -->
    
    // <--[tag]
    // @attribute <&2>
    // @returns ChatColor
    // @description
    // Makes the following characters Dark Green.
    // -->
    
    // <--[tag]
    // @attribute <&3>
    // @returns ChatColor
    // @description
    // Makes the following characters Dark Aqua.
    // -->
    
    // <--[tag]
    // @attribute <&4>
    // @returns ChatColor
    // @description
    // Makes the following characters Dark Red.
    // -->
    
    // <--[tag]
    // @attribute <&5>
    // @returns ChatColor
    // @description
    // Makes the following characters Dark Purple.
    // -->
    
    // <--[tag]
    // @attribute <&6>
    // @returns ChatColor
    // @description
    // Makes the following characters Gold.
    // -->
    
    // <--[tag]
    // @attribute <&7>
    // @returns ChatColor
    // @description
    // Makes the following characters Gray.
    // -->
    
    // <--[tag]
    // @attribute <&8>
    // @returns ChatColor
    // @description
    // Makes the following characters Dark Gray.
    // -->
    
    // <--[tag]
    // @attribute <&9>
    // @returns ChatColor
    // @description
    // Makes the following characters Blue.
    // -->
    
    // <--[tag]
    // @attribute <&a>
    // @returns ChatColor
    // @description
    // Makes the following characters Green.
    // -->
    
    // <--[tag]
    // @attribute <&b>
    // @returns ChatColor
    // @description
    // Makes the following characters Aqua.
    // -->
    
    // <--[tag]
    // @attribute <&c>
    // @returns ChatColor
    // @description
    // Makes the following characters Red.
    // -->
    
    // <--[tag]
    // @attribute <&d>
    // @returns ChatColor
    // @description
    // Makes the following characters Light Purple.
    // -->
    
    // <--[tag]
    // @attribute <&e>
    // @returns ChatColor
    // @description
    // Makes the following characters Yellow.
    // -->
    
    // <--[tag]
    // @attribute <&f>
    // @returns ChatColor
    // @description
    // Makes the following characters White.
    // -->
    
    // <--[tag]
    // @attribute <&k>
    // @returns ChatColor
    // @description
    // Makes the following characters obfuscated.
    // -->
    
    // <--[tag]
    // @attribute <&l>
    // @returns ChatColor
    // @description
    // Makes the following characters bolded.
    // -->
    
    // <--[tag]
    // @attribute <&m>
    // @returns ChatColor
    // @description
    // Makes the following characters strikethroughed.
    // -->
    
    // <--[tag]
    // @attribute <&n>
    // @returns ChatColor
    // @description
    // Makes the following characters underlined.
    // -->
    
    // <--[tag]
    // @attribute <&o>
    // @returns ChatColor
    // @description
    // Makes the following characters italicized.
    // -->
    
    // <--[tag]
    // @attribute <&r>
    // @returns ChatColor
    // @description
    // Resets the following characters to normal.
    // -->

    @EventHandler
    public void colorTags(ReplaceableTagEvent event) {
        Attribute attribute =
                new Attribute(event.raw_tag, event.getScriptEntry());
        int i = 0;
        for (ChatColor color : ChatColor.values()) {
            if (i > 22) break;
            if (event.matches(color.name()))
                event.setReplaced(new Element(color.toString()).getAttribute(attribute.fulfill(1)));
            else if (event.matches("&" + code[i]))
                event.setReplaced(new Element(ChatColor.getByChar(code[i]).toString()).getAttribute(attribute.fulfill(1)));
            i++;
        }
    }

}
