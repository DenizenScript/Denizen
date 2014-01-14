package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EscapeTags implements Listener {

    public EscapeTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    // <--[language]
    // @name Property Escaping
    // @group Useful Lists
    // @description
    // Some item properties (and corresponding mechanisms) need to escape their
    // text output/input to prevent players using them to cheat the system
    // (EG, if a player set the display name of an item to:
    //      'name;enchantments=damage_all,3', he would get a free enchantment!)
    // This are the escape codes used to prevent that:
    //
    // | = &pipe
    // < = &lt
    // > = &gt
    // newline = &nl
    // & = &amp
    // ; = &sc
    // [ = &lb
    // ] = &rb
    //
    // These symbols are automatically used by the internal system, if you are
    // writing your own property string and need to escape some symbols, you
    // can just directly type them in, EG: i@stick[display_name=&ltStick&gt]
    // -->

    /**
     * A quick function to escape book Strings.
     * This is just to prevent tag reading errors.
     *
     * @param input the unescaped data.
     * @return the escaped data.
     */
    public static String Escape(String input) {
        return TagManager.CleanOutputFully(input)
                .replace("&", "&amp").replace("|", "&pipe")
                .replace(">", "&gt").replace("<", "&lt")
                .replace("\n", "&nl").replace(";", "&sc")
                .replace("[", "&lb").replace("]", "&rb");
    }

    /**
     * A quick function to reverse a book string escaping.
     * This is just to prevent tag reading errors.
     *
     * @param input the escaped data.
     * @return the unescaped data.
     */
    public static String unEscape(String input) {
        return TagManager.CleanOutputFully(input)
                .replace("&pipe;", "|").replace("&nl;", "\n")
                .replace("&gt;", ">").replace("&lt;", "<")
                .replace("&amp;", "&").replace("&sc;", ";")
                        // TODO: Remove the above outdated escapes and keep only the below
                .replace("&pipe", "|").replace("&nl", "\n")
                .replace("&gt", ">").replace("&lt", "<")
                .replace("&amp", "&").replace("&sc", ";")
                .replace("&lb", "[").replace("&rb", "]");
    }

    @EventHandler
    public void escapeTags(ReplaceableTagEvent event) {
        // <--[tag]
        // @attribute <escape:<text to escape>>
        // @returns Direct text output
        // @description
        // Returns the text simply escaped to prevent tagging conflicts.
        // See <@link language Property Escaping>
        // -->
        if (event.matches("escape")) {
            if (!event.hasValue()) {
                dB.echoError("Escape tag '" + event.raw_tag + "' does not have a value!");
                return;
            }
            event.setReplaced(Escape(event.getValue()));
        }
        // <--[tag]
        // @attribute <unescape:<escaped text>>
        // @returns Direct text output
        // @description
        // Returns the text with escaping removed.
        // See <@link language Property Escaping>
        // -->
        else if (event.matches("unescape")) {
            if (!event.hasValue()) {
                dB.echoError("Escape tag '" + event.raw_tag + "' does not have a value!");
                return;
            }
            event.setReplaced(unEscape(event.getValue()));
        }
    }
}
