package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PluginTag implements ObjectTag, FlaggableObject {

    // <--[ObjectType]
    // @name PluginTag
    // @prefix pl
    // @base ElementTag
    // @implements FlaggableObject
    // @format
    // The identity format for plugins is the plugin's registered name.
    // For example, 'pl@Denizen'.
    //
    // @description
    // A PluginTag represents a Bukkit plugin on the server.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the server saves file, under special sub-key "__plugins"
    //
    // -->

    //////////////////
    //    Object Fetcher
    ////////////////

    @Deprecated
    public static PluginTag valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a PluginTag from a string format.
     *
     * @param string The plugin in string form. (pl@PluginName)
     * @return The PluginTag value. If the string is incorrectly formatted or
     * the specified plugin is invalid, this is null.
     */
    @Fetchable("pl")
    public static PluginTag valueOf(String string, TagContext context) {

        if (string == null) {
            return null;
        }

        string = CoreUtilities.toLowerCase(string).replace("pl@", "");

        try {
            // Attempt to match from plugin list, as PluginManager#getPlugin is case sensitive
            for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
                if (string.equalsIgnoreCase(plugin.getName())) {
                    return new PluginTag(plugin);
                }
            }
        }
        catch (Exception e) {
            Debug.echoError("Invalid plugin name specified, or plugin is not enabled: " + string);
        }

        return null;

    }

    public static boolean matches(String arg) {
        if (CoreUtilities.toLowerCase(arg).startsWith("pl@")) {
            return true;
        }
        for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            if (arg.equalsIgnoreCase(plugin.getName())) {
                return true;
            }
        }
        return false;
    }

    /////////////////////
    //   Constructors
    //////////////////

    public PluginTag(Plugin plugin) {
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
    //  ObjectTag Methods
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
    public String toString() {
        return identify();
    }

    @Override
    public PluginTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(DenizenCore.serverFlagMap, "__plugins." + plugin.getName().replace(".", "&dot"));
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <PluginTag.name>
        // @returns ElementTag
        // @description
        // Gets the name of this plugin.
        // -->
        registerTag("name", (attribute, object) -> {
            return new ElementTag(object.plugin.getName());
        });

        // <--[tag]
        // @attribute <PluginTag.version>
        // @returns ElementTag
        // @description
        // Gets the version for the plugin specified.
        // -->
        registerTag("version", (attribute, object) -> {
            return new ElementTag(object.plugin.getDescription().getVersion());
        });

        // <--[tag]
        // @attribute <PluginTag.description>
        // @returns ElementTag
        // @description
        // Gets the description for the plugin specified.
        // -->
        registerTag("description", (attribute, object) -> {
            return new ElementTag(object.plugin.getDescription().getDescription());
        });

        // <--[tag]
        // @attribute <PluginTag.authors>
        // @returns ListTag
        // @description
        // Gets the list of authors for the plugin specified.
        // -->
        registerTag("authors", (attribute, object) -> {
            return new ListTag(object.plugin.getDescription().getAuthors());
        });
    }

    public static ObjectTagProcessor<PluginTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<PluginTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }
}
