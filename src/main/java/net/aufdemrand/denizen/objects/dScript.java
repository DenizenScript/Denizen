package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.core.CooldownCommand;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    // @description
    // 1) A dObject that represents a script container. dScripts contain all information inside the script, and can be
    // used in a variety of commands that require script arguments. For example, run and inject will 'execute'
    // script entries inside of a script container when given a matching dScript object.
    //
    // dScripts also provide a way to access attributes accessed by the replaceable tag system by using the object
    // fetcher or any other entry point to a dScript object. dScript objects have the object prefix 's'.
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
    // @description
    // s@ refers to the object type of a dScript. The 's@' is notation for Denizen's Object
    // Fetcher. The only valid constructor for a dScript is the name of the script container that it should be
    // associated with. For example, if my script container is called 'cool_script', the dScript object for that script
    // would be able to be referenced (fetched) with s@cool_script.
    // -->


    final public static Pattern CONTAINER_PATTERN = Pattern.compile("(s@|)(.+)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Gets a dContainer Object from a dScript argument.
     *
     * @param string  the dScript argument String
     * @return  a Script, or null if incorrectly formatted
     */
    @ObjectFetcher("s")
    public static dScript valueOf(String string) {

        Matcher m = CONTAINER_PATTERN.matcher(string);
        if (m.matches()) {
            dScript script = new dScript(m.group(2));
            // Make sure it's valid.
            if (script.isValid()) return script;
        }
        return null;
    }

    public static boolean matches(String string) {

        Matcher m = CONTAINER_PATTERN.matcher(string);
        if (m.matches()) {
            dScript script = new dScript(m.group(2));
            // Make sure it's valid.
            if (script.isValid()) return true;
        }
        return false;
    }



    //////////////////
    // Constructor
    ////////////////


    /**
     * Creates a script object from a script name. If the script is valid, {@link #isValid()} will retrun true.
     *
     * @param scriptName
     */
    public dScript(String scriptName) {
        if (ScriptRegistry.getScriptContainer(scriptName) != null) {
            container = ScriptRegistry.getScriptContainer(scriptName);
            name = scriptName.toUpperCase();
            valid = true;
        }
    }



    ///////////////////////
    // Instance fields and methods
    /////////////////////

    private ScriptContainer container;
    private String prefix = "Container";
    private String name = null;
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
        return "<G>" + prefix + "='<A>" + name + "<Y>(" + getType() + ")<G>'  ";
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    public String colorless_debug() {
        return prefix + "='" + name + "(" + getType() + ")'";
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
        if (attribute.startsWith("type"))
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
            return CooldownCommand.getCooldownDuration((player != null ? player.getName() : null), container.getName())
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
            return new Element(colorless_debug()).getAttribute(attribute.fulfill(1));
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



        return new Element(identify()).getAttribute(attribute);
    }


}
