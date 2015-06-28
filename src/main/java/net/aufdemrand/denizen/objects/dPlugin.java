package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Fetchable;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class dPlugin implements dObject {


    //////////////////
    //    Object Fetcher
    ////////////////


    public static dPlugin valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a dPlugin from a string format.
     *
     * @param string The plugin in string form. (pl@PluginName)
     * @return The dPlugin value. If the string is incorrectly formatted or
     * the specified plugin is invalid, this is null.
     */
    @Fetchable("pl")
    public static dPlugin valueOf(String string, TagContext context) {

        if (string == null) return null;

        string = string.toLowerCase().replace("pl@", "");

        try {
            // Attempt to match from plugin list, as PluginManager#getPlugin is case sensitive
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                if (string.equalsIgnoreCase(plugin.getName()))
                    return new dPlugin(plugin);
            }
        }
        catch (Exception e) {
            dB.echoError("Invalid plugin name specified, or plugin is not enabled: " + string);
        }

        return null;

    }

    public static boolean matches(String arg) {
        if (arg.toLowerCase().startsWith("pl@"))
            return true;
        for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            if (arg.equalsIgnoreCase(plugin.getName()))
                return true;
        }
        return false;
    }


    /////////////////////
    //   Constructors
    //////////////////

    public dPlugin(Plugin plugin) {
        this.plugin = plugin;
    }


    /////////////////////
    //   Instance Fields/Methods
    /////////////////

    private Plugin plugin;

    public Plugin getPlugin() {
        return plugin;
    }


    /////////////////////
    //  dObject Methods
    ///////////////////

    private String prefix = "Plugin";

    @Override
    public String getObjectType() {
        return "Plugin";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return String.format("<G>%s='<Y>%s<G>'  ", prefix, identify());
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String identify() {
        return "pl@" + plugin.getName();
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public dPlugin setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }


    /////////////////
    // Attributes
    /////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <pl@plugin.name>
        // @returns Element
        // @description
        // Gets the name of this plugin.
        // -->
        if (attribute.startsWith("name")) {
            return new Element(plugin.getName())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <pl@plugin.version>
        // @returns Element
        // @description
        // Gets the version for the plugin specified.
        // -->
        if (attribute.startsWith("version")) {
            return new Element(plugin.getDescription().getVersion())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <pl@plugin.description>
        // @returns Element
        // @description
        // Gets the description for the plugin specified.
        // -->
        if (attribute.startsWith("description")) {
            return new Element(plugin.getDescription().getDescription())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <pl@plugin.authors>
        // @returns dList
        // @description
        // Gets the list of authors for the plugin specified.
        // -->
        if (attribute.startsWith("authors")) {
            return new dList(plugin.getDescription().getAuthors())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <pl@plugin.type>
        // @returns Element
        // @description
        // Always returns 'Plugin' for dPlugin objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("type")) {
            return new Element("Plugin").getAttribute(attribute.fulfill(1));
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new Element(identify()).getAttribute(attribute);

    }
}
