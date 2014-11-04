package net.aufdemrand.denizen.objects;

import java.util.List;

import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.core.CooldownCommand;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptHelper;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;
import org.json.JSONObject;

public class dScript implements dObject {

    // <--[language]
    // @name Script
    // @description
    // A somewhat vague term used to describe a collection of script entries and other script parts.
    //
    // For example, 'Hey, check out this script I just wrote!', probably refers to a collection of script entries
    // that make up some kind of script container. Perhaps it is a NPC Assignment Script Container that provides
    // waypoint functionality, or a world script that implements and keeps track of a new player stat. 'Script' can
    // refer to a single container, as well as a collection of containers that share a common theme.
    //
    // Scripts that contain a collection of containers are typically kept to a single file. Multiple containers are
    // permitted inside a single file, but it should be noted that container names are stored on a global level. That
    // is, naming scripts should be done with care to avoid duplicate script names.
    //
    // -->

    // <--[language]
    // @name dScript
    // @group Object System
    // @description
    // 1) A dObject that represents a script container. dScripts contain all information inside the script, and can be
    // used in a variety of commands that require script arguments. For example, run and inject will 'execute'
    // script entries inside of a script container when given a matching dScript object.
    //
    // dScripts also provide a way to access attributes accessed by the replaceable tag system by using the object
    // fetcher or any other entry point to a dScript object. dScript objects have the object identifier of 's@'.
    // For example: s@script_name
    //
    // 2) The overall 'scripting language' that Denizen implements is referred to as 'dScripting', or 'dScript'.
    // dScripts use YAML + Denizen's Scripting API to parse scripts that are stored as .yml or .dscript files. Scripts
    // go in the .../plugins/Denizen/scripts folder.
    //
    // -->

    ///////////////
    // Object Fetcher
    /////////////

    // <--[language]
    // @name s@
    // @group Object Fetcher System
    // @description
    // s@ refers to the 'object identifier' of a dScript. The 's@' is notation for Denizen's Object
    // Fetcher. The only valid constructor for a dScript is the name of the script container that it should be
    // associated with. For example, if my script container is called 'cool_script', the dScript object for that script
    // would be able to be referenced (fetched) with s@cool_script.
    // -->


    /**
     * Gets a dContainer Object from a dScript argument.
     *
     * @param string  the dScript argument String
     * @return  a Script, or null if incorrectly formatted
     */
    @Fetchable("s")
    public static dScript valueOf(String string) {

        if (string.startsWith("s@"))
            string = string.substring(2);

        dScript script = new dScript(string);
        // Make sure it's valid.
        if (script.isValid())
            return script;
        else
            return null;
    }


    public static boolean matches(String string) {

        if (string.toLowerCase().startsWith("s@")) return true;

        dScript script = new dScript(string);
        // Make sure it's valid.
        return script.isValid();
    }

    //////////////////
    // Constructor
    ////////////////


    /**
     * Creates a script object from a script name. If the script is valid, {@link #isValid()} will return true.
     *
     * @param scriptName the name of the script
     */
    public dScript(String scriptName) {
        if (ScriptRegistry.getScriptContainer(scriptName) != null) {
            container = ScriptRegistry.getScriptContainer(scriptName);
            name = scriptName.toUpperCase();
            valid = true;
        }
    }

    public dScript(ScriptContainer container) {
        this.container = container;
        name = container.getName().toUpperCase();
        valid = true;
    }

    ///////////////////////
    // Instance fields and methods
    /////////////////////

    // Keep track of the corresponding ScriptContainer
    private ScriptContainer container;

    // Make the default prefix "Container"
    private String prefix = "Container";


    private boolean valid = false;

    /**
     * Confirms that the script references a valid name and type in current loaded ScriptsContainers.
     *
     * @return  true if the script is valid, false if the script was not found, or the type is missing
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the type of the ScriptContainer, as defined by the TYPE: key.
     *
     * @return  the type of the Script Container
     */
    public String getType() {
        return (container != null ? container.getContainerType() : "invalid");
    }


    private String name = null;

    /**
     * Gets the name of the ScriptContainer.
     *
     * @return  script name
     */
    public String getName() {
        return name;
    }


    /**
     * Gets the contents of the scriptContainer.
     *
     * @return  ConfigurationSection of the script contents
     */
    public ScriptContainer getContainer() {
        return container;
    }

    ///////////////
    // dObject Methods
    ////////////

    @Override
    public String getObjectType() {
        return "Container";
    }

    @Override
    public String identify() {
        return "s@" + name;
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
    public String getPrefix() {
        return prefix;
    }

    @Override
    public dObject setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debug() {
        return String.format("<G>%s='<A>%s<Y>(%s)<G>'  ", prefix, name, getType());
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <s@script.container_type>
        // @returns Element
        // @description
        // Returns the type of script container that is associated with this dScript object. For example: 'task', or
        // 'world'.
        // -->
        if (attribute.startsWith("container_type") || attribute.startsWith("type"))
            return new Element(container.getContainerType())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <s@script.cooled_down[<player>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the script is currently cooled down for the player. Any global
        // cooldown present on the script will also be taken into account. Not specifying a player will result in
        // using the attached player available in the script entry. Not having a valid player will result in 'null'.
        // -->
        if (attribute.startsWith("cooled_down")) {
            dPlayer player = (attribute.hasContext(1) ? dPlayer.valueOf(attribute.getContext(1))
                    : attribute.getScriptEntry().getPlayer());
            if (player != null && player.isValid())
                return new Element(container.checkCooldown(player))
                        .getAttribute(attribute.fulfill(1));
            else return "null";
        }

        // <--[tag]
        // @attribute <s@script.requirements[<player>].check[<path>]>
        // @returns Element
        // @description
        // Returns whether the player specified (defaults to current) has the requirement.
        // -->
        if (attribute.startsWith("requirements.check")) {
            dPlayer player = (attribute.hasContext(1) ? dPlayer.valueOf(attribute.getContext(1))
                    : attribute.getScriptEntry().getPlayer());
            if (attribute.hasContext(2))
                return new Element(container.checkRequirements(player,
                        attribute.getScriptEntry().getNPC(),
                        attribute.getContext(2)))
                        .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <s@script.cooldown[<player>]>
        // @returns Duration
        // @description
        // Returns the time left for the player to cooldown for the script.
        // -->
        if (attribute.startsWith("cooldown")) {
            dPlayer player = (attribute.hasContext(1) ? dPlayer.valueOf(attribute.getContext(1))
                    : attribute.getScriptEntry().getPlayer());
            return CooldownCommand.getCooldownDuration(player, name)
                    .getAttribute(attribute.fulfill(1));

        }

        // <--[tag]
        // @attribute <s@script.name>
        // @returns Element
        // @description
        // Returns the name of the script container.
        // -->
        if (attribute.startsWith("name")) {
            return new Element(name)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.relative_filename>
        // @returns Element
        // @description
        // Returns the filename that contains the script, relative to the denizen/ folder.
        // -->
        if (attribute.startsWith("relative_filename")) {
            return new Element(container.getFileName().replace(DenizenAPI.getCurrentInstance().getDataFolder().getAbsolutePath(), "").replace("\\", "/"))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.filename>
        // @returns Element
        // @description
        // Returns the filename that contains the script.
        // -->
        if (attribute.startsWith("filename")) {
            return new Element(container.getFileName().replace("\\", "/"))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.constant[<constant_name>]>
        // @returns Element or dList
        // @description
        // Returns the value of the constant as either an Element or dList.
        // -->
        if (attribute.startsWith("cons")) {
            if (!attribute.hasContext(1)) return null;

            YamlConfiguration section = getContainer().getConfigurationSection("constants");
            if (section == null) return null;
            Object obj = section.get(attribute.getContext(1).toUpperCase());
            if (obj == null) return null;

            if (obj instanceof List) {
                dList list = new dList();
                for (Object each : (List<Object>) obj)
                    list.add(TagManager.tag(attribute.getScriptEntry() == null ? null: attribute.getScriptEntry().getPlayer(),
                            attribute.getScriptEntry() == null ? null: attribute.getScriptEntry().getNPC(), each.toString(), false, attribute.getScriptEntry()));
                return list.getAttribute(attribute.fulfill(1));

            }
            else return new Element(TagManager.tag(attribute.getScriptEntry() == null ? null: attribute.getScriptEntry().getPlayer(),
                    attribute.getScriptEntry() == null ? null: attribute.getScriptEntry().getNPC(), obj.toString(), false, attribute.getScriptEntry()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.yaml_key[<constant_name>]>
        // @returns Element or dList
        // @description
        // Returns the value of the script's YAML as either an Element or dList.
        // -->
        if (attribute.startsWith("yaml_key")
                && attribute.hasContext(1)) {
            ScriptContainer container = getContainer();
            if (container == null) {
                dB.echoError("Missing script container?!");
                return new Element(identify()).getAttribute(attribute);
            }
            YamlConfiguration section = container.getConfigurationSection("");
            if (section == null) {
                dB.echoError("Missing YAML section?!");
                return new Element(identify()).getAttribute(attribute);
            }
            Object obj = section.get(attribute.getContext(1).toUpperCase());
            if (obj == null) return null;

            if (obj instanceof List) {
                dList list = new dList();
                for (Object each : (List<Object>) obj)
                    list.add(TagManager.tag(attribute.getScriptEntry() == null ? null: attribute.getScriptEntry().getPlayer(),
                            attribute.getScriptEntry() == null ? null: attribute.getScriptEntry().getNPC(), each.toString(), false, attribute.getScriptEntry()));
                return list.getAttribute(attribute.fulfill(1));

            }
            else return new Element(TagManager.tag(attribute.getScriptEntry() == null ? null: attribute.getScriptEntry().getPlayer(),
                    attribute.getScriptEntry() == null ? null: attribute.getScriptEntry().getNPC(), obj.toString(), false, attribute.getScriptEntry()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.list_keys[<constant_name>]>
        // @returns dList
        // @description
        // Returns a list of all keys within a script.
        // -->
        if (attribute.startsWith("list_keys")) {
            return new dList(getContainer().getConfigurationSection(attribute.hasContext(1) ? attribute.getContext(1): "").getKeys(false))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.list_deep_keys[<constant_name>]>
        // @returns dList
        // @description
        // Returns a list of all keys within a script, searching recursively.
        // -->
        if (attribute.startsWith("list_deep_keys")) {
            return new dList(getContainer().getConfigurationSection(attribute.hasContext(1) ? attribute.getContext(1): "").getKeys(true))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.step[<player>]>
        // @returns Element
        // @description
        // Returns the name of a script step that the player is currently on.
        // -->
        if (attribute.startsWith("step")) {
            dPlayer player = (attribute.hasContext(1) ? dPlayer.valueOf(attribute.getContext(1))
                    : attribute.getScriptEntry().getPlayer());

            if (player != null && player.isValid())
                return new Element(InteractScriptHelper.getCurrentStep(player, container.getName()))
                        .getAttribute(attribute.fulfill(1));
        }


        /////////////////
        // dObject attributes
        ///////////////

        // <--[tag]
        // @attribute <s@script.debug>
        // @returns Element
        // @description
        // Returns the debug entry for this object. This contains the prefix, the name of the dScript object, and the
        // type of ScriptContainer is held within. All objects fetchable by the Object Fetcher will return a valid
        // debug entry for the object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("debug")) {
            return new Element(debug()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.prefix>
        // @returns Element
        // @description
        // Returns the prefix for this object. By default this will return 'Script', however certain situations will
        // return a finer scope. All objects fetchable by the Object Fetcher will return a valid prefix for the object
        // that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("prefix")) {
            return new Element(prefix).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.object_type>
        // @returns Element
        // @description
        // Always returns 'Script' for dScript objects. All objects fetchable by the Object Fetcher will return a the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("object_type")) {
            return new Element(getObjectType()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.to_json>
        // @returns Element
        // @description
        // Converts the YAML Script Container to a JSON array.
        // Best used with 'yaml data' type scripts.
        // -->
        if (attribute.startsWith("to_json")) {
            JSONObject jsobj = new JSONObject(container.getConfigurationSection("").getMap());
            jsobj.remove("TYPE");
            return new Element(jsobj.toString()).getAttribute(attribute.fulfill(1));
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }
}
