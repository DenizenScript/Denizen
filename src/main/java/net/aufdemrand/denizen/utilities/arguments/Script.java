package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.configuration.ConfigurationSection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Script implements dScriptArgument {

    final public static Pattern matchesScriptPtrn = Pattern.compile("(?:.+:|)(.+)", Pattern.CASE_INSENSITIVE);

    /**
     * Gets a Script Object from a dScript argument.
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

    private String prefix = "Script";
    private String type = null;
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

        if (DenizenAPI.getCurrentInstance().getScripts().contains(scriptName.toUpperCase() + ".TYPE")) {
            name = scriptName.toUpperCase();
            type = DenizenAPI.getCurrentInstance().getScripts().getString(scriptName.toUpperCase() + ".TYPE");
            valid = true;
        }
    }

    /**
     * Confirms that the script references a valid name and type in current loaded Scripts.
     *
     * @return  true if the script is valid, false if the script was not found, or the type is missing
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the type of the script, as defined by the TYPE: key.
     *
     * @return  the type of the script
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the name of the script.
     *
     * @return  script name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the contents of the script.
     *
     * @return  ConfigurationSection of the script contents
     */
    public ConfigurationSection getContents() {
        return DenizenAPI.getCurrentInstance().getScripts().getConfigurationSection(name);
    }

    @Override
    public String getDefaultPrefix() {
       return prefix;
    }

    @Override
    public String debug() {
        return "<G>" + prefix + "='<A>" + name + "<Y>(" + type + ")<G>'  ";
    }

    @Override
    public String dScriptArg() {
        return prefix + ":" + name;
    }

    @Override
    public String dScriptArgValue() {
        return name;
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }
}
