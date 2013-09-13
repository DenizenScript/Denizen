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
