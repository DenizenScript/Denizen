package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.core.CooldownCommand;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dScript implements dObject {

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

    private ScriptContainer container;
    private String prefix = "Container";
    private String name = null;
    private boolean valid = false;

    /**
     * Creates a script object from a script name. If the script is valid, {@link #isValid()} will retrun true.
     *
     * @param scriptName
     */
    public dScript(String scriptName) {
        // Required for tests
        if (DenizenAPI.getCurrentInstance() == null) return;
        if (ScriptRegistry.getScriptContainer(scriptName) != null) {
            container = ScriptRegistry.getScriptContainer(scriptName);
            name = scriptName.toUpperCase();
            valid = true;
        }
    }

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
        return (container != null ? container.getType() : "invalid");
    }

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

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return "<G>" + prefix + "='<A>" + name + "<Y>(" + getType() + ")<G>'  ";
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public dObject setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <s@script.container_type>
        // @returns Element
        // @description
        // Returns the container type of a dScript.
        // -->
        if (attribute.startsWith("container_type"))
            return new Element(container.getType())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <s@script.cooled_down[<player>]>
        // @returns Element(Boolean)
        // @description
        // Returns true if the script has been cooled down for the
        // player (defaults to current). Otherwise, returns false.
        // -->
        if (attribute.startsWith("cooled_down")) {
            dPlayer player = (attribute.hasContext(1) ? dPlayer.valueOf(attribute.getContext(1))
                    : attribute.getScriptEntry().getPlayer());
            return new Element(container.checkCooldown(player))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <s@script.requirements[<player>].check[<path>]>
        // @returns Element
        // @description
        // Returns true if the player specified (defaults to current) has the
        // requirement. Otherwise, returns false.
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

        return new Element(identify()).getAttribute(attribute);
    }


}
