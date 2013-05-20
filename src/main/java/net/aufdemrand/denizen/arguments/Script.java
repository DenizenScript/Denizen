package net.aufdemrand.denizen.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Script implements dScriptArgument {

    final public static Pattern matchesScriptPtrn = Pattern.compile("(?:.+:|)(.+)", Pattern.CASE_INSENSITIVE);

    /**
     * Gets a Script Argument Object from a dScript argument.
     *
     * @param string  the dScript argument String
     * @return  a Script, or null if incorrectly formatted
     */
    public static Script valueOf(String string) {

        Matcher m = matchesScriptPtrn.matcher(string);
        if (m.matches()) {
            Script script = new Script(m.group(1));
            // Make sure it's valid.
            if (script.isValid()) return script;
        }
        return null;
    }

    private ScriptContainer container;
    private String prefix = "Script";
    private String name = null;
    private boolean valid = false;

    /**
     * Creates a script object from a script name. If the script is valid, {@link #isValid()} will retrun true.
     *
     * @param scriptName
     */
    public Script (String scriptName) {
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
    public String getDefaultPrefix() {
       return prefix;
    }

    @Override
    public String debug() {
        return "<G>" + prefix + "='<A>" + name + "<Y>(" + getType() + ")<G>'  ";
    }

    @Override
    public String as_dScriptArg() {
        return prefix + ":" + name;
    }

    public String dScriptArgValue() {
        return name;
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return as_dScriptArg();

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }


}
