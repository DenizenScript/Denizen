package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SpecialCharacterTags implements Listener {

    public SpecialCharacterTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

   @EventHandler
    public void specialCharacterTags(ReplaceableTagEvent event) {
    if (!event.getName().startsWith("&")) return;
       Attribute attribute =
               new Attribute(event.raw_tag, event.getScriptEntry());

       // <--[tag]
       // @attribute <&nl>
       // @returns Element
       // @description
       // Returns a newline symbol.
       // -->
       if (event.getName().equalsIgnoreCase("&nl"))
            event.setReplaced(new Element("\n").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&cm>
           // @returns Element
           // @description
           // Returns a comma symbol: ,
           // -->
       else if (event.getName().equalsIgnoreCase("&cm"))
           event.setReplaced(new Element(",").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&ss>
           // @returns Element
           // @description
           // Returns an internal coloring symbol: §
           // -->
       else if (event.getName().equalsIgnoreCase("&ss"))
           event.setReplaced(new Element("§").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&sq>
           // @returns Element
           // @description
           // Returns a single-quote symbol: '
           // -->
       else if (event.getName().equalsIgnoreCase("&sq"))
           event.setReplaced(new Element("'").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&dq>
           // @returns Element
           // @description
           // Returns a double-quote symbol: "
           // -->
       else if (event.getName().equalsIgnoreCase("&dq"))
           event.setReplaced(new Element("\"").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&co>
           // @returns Element
           // @description
           // Returns a colon symbol: :
           // -->
       else if (event.getName().equalsIgnoreCase("&co"))
           event.setReplaced(new Element(":").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&rb>
           // @returns Element
           // @description
           // Returns a right-bracket symbol: ]
           // -->
       else if (event.getName().equalsIgnoreCase("&rb"))
           event.setReplaced(new Element("]").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&lb>
           // @returns Element
           // @description
           // Returns a left-bracket symbol: [
           // -->
       else if (event.getName().equalsIgnoreCase("&lb"))
           event.setReplaced(new Element("[").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&rc>
           // @returns Element
           // @description
           // Returns a right-brace symbol: }
           // -->
       else if (event.getName().equalsIgnoreCase("&rc"))
           event.setReplaced(new Element("}").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&lc>
           // @returns Element
           // @description
           // Returns a left-brace symbol: {
           // -->
       else if (event.getName().equalsIgnoreCase("&lc"))
           event.setReplaced(new Element("{").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&ns>
           // @returns Element
           // @description
           // Returns a hash symbol: #
           // -->
       else if (event.getName().equalsIgnoreCase("&ns"))
           event.setReplaced(new Element("#").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&pc>
           // @returns Element
           // @description
           // Returns a percent symbol: %
           // -->
       else if (event.getName().equalsIgnoreCase("&pc"))
           event.setReplaced(new Element("%").getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&lt>
           // @returns Element
           // @description
           // Returns a less than symbol: <
           // -->
       else if (event.getName().equalsIgnoreCase("&lt"))
           event.setReplaced(new Element(String.valueOf((char)0x01)).getAttribute(attribute.fulfill(1)));

           // <--[tag]
           // @attribute <&gt>
           // @returns Element
           // @description
           // Returns a greater than symbol: >
           // -->
       else if (event.getName().equalsIgnoreCase("&gt"))
           event.setReplaced(new Element(String.valueOf((char)0x02)).getAttribute(attribute.fulfill(1)));

   }

}
