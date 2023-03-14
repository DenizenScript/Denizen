package com.denizenscript.denizen.objects;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

public class PluginTag implements ObjectTag, FlaggableObject {

    // <--[ObjectType]
    // @name PluginTag
    // @prefix pl
    // @base ElementTag
    // @implements FlaggableObject
    // @ExampleTagBase plugin[Denizen]
    // @ExampleValues Denizen
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
    public Object getJavaObject() {
        return plugin;
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

    public static void register() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <PluginTag.name>
        // @returns ElementTag
        // @description
        // Gets the name of this plugin.
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(object.plugin.getName());
        });

        // <--[tag]
        // @attribute <PluginTag.version>
        // @returns ElementTag
        // @description
        // Gets the version for the plugin specified.
        // -->
        tagProcessor.registerTag(ElementTag.class, "version", (attribute, object) -> {
            return new ElementTag(object.plugin.getDescription().getVersion());
        });

        // <--[tag]
        // @attribute <PluginTag.description>
        // @returns ElementTag
        // @description
        // Gets the description for the plugin specified.
        // -->
        tagProcessor.registerTag(ElementTag.class, "description", (attribute, object) -> {
            return new ElementTag(object.plugin.getDescription().getDescription());
        });

        // <--[tag]
        // @attribute <PluginTag.authors>
        // @returns ListTag
        // @description
        // Gets the list of authors for the plugin specified.
        // -->
        tagProcessor.registerTag(ListTag.class, "authors", (attribute, object) -> {
            return new ListTag(object.plugin.getDescription().getAuthors());
        });

        // <--[tag]
        // @attribute <PluginTag.depends>
        // @returns ListTag
        // @description
        // Gets the list of hard dependencies for the plugin specified.
        // -->
        tagProcessor.registerTag(ListTag.class, "depends", (attribute, object) -> {
            return new ListTag(object.plugin.getDescription().getDepend());
        });

        // <--[tag]
        // @attribute <PluginTag.soft_depends>
        // @returns ListTag
        // @description
        // Gets the list of soft dependencies for the plugin specified.
        // -->
        tagProcessor.registerTag(ListTag.class, "soft_depends", (attribute, object) -> {
            return new ListTag(object.plugin.getDescription().getSoftDepend());
        });

        // <--[tag]
        // @attribute <PluginTag.commands>
        // @returns MapTag(MapTag)
        // @description
        // Gets a map of commands registered this plugin registers by default.
        // Note that dynamically registered commands won't show up (for example, command scripts won't be listed under Denizen).
        // Map key is command name, map value is a sub-mapping with keys:
        // description (ElementTag), usage (ElementTag), permission (ElementTag), aliases (ListTag)
        // Not all keys will be present.
        // For example, <plugin[denizen].commands.get[ex]> will return a MapTag with:
        // [description=Executes a Denizen script command.;usage=/ex (-q) <Denizen script command> (arguments);permission=denizen.ex]
        // -->
        tagProcessor.registerTag(MapTag.class, "commands", (attribute, object) -> {
            Map<String, Map<String, Object>> commands = object.plugin.getDescription().getCommands();
            MapTag output = new MapTag();
            if (commands == null || commands.isEmpty()) {
                return output;
            }
            for (Map.Entry<String, Map<String, Object>> command : commands.entrySet()) {
                MapTag dataMap = new MapTag();
                if (command.getValue().containsKey("description")) {
                    dataMap.putObject("description", new ElementTag(command.getValue().get("description").toString(), true));
                }
                if (command.getValue().containsKey("usage")) {
                    dataMap.putObject("usage", new ElementTag(command.getValue().get("usage").toString(), true));
                }
                if (command.getValue().containsKey("permission")) {
                    dataMap.putObject("permission", new ElementTag(command.getValue().get("permission").toString(), true));
                }
                if (command.getValue().containsKey("aliases")) {
                    Object obj = command.getValue().get("aliases");
                    if (obj instanceof List) {
                        ListTag aliases = new ListTag();
                        for (Object entry : (List) obj) {
                            aliases.addObject(new ElementTag(String.valueOf(entry), true));
                        }
                        dataMap.putObject("aliases", aliases);
                    }
                }
                output.putObject(command.getKey(), dataMap);
            }
            return output;
        });
    }

    public static ObjectTagProcessor<PluginTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }
}
