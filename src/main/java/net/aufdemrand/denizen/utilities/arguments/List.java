package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.persistence.Id;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class List extends ArrayList<String> implements dScriptArgument {

    public static List valueOf(String string) {
        if (string == null) return null;

        String prefix = null;
        // Strip prefix (ie. targets:...)
        if (string.split(":").length > 1) {
            prefix = string.split(":", 2)[0];
            string = string.split(":", 2)[1];
        }

        return new List(prefix, string);
    }

    private String prefix;

    public List(String prefix, String items) {
        if (prefix == null) this.prefix = "list";
        else this.prefix = prefix;
        addAll(Arrays.asList(items.split("\\|")));
    }

    public List(String prefix, java.util.List<String> items) {
        if (prefix == null) this.prefix = "list";
        else this.prefix = prefix;
        addAll(items);
    }

    public List(java.util.List<String> items) {
        this.prefix = "list";
        addAll(items);
    }


    @Override
    public String getDefaultPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + this.toString() + "<G>'  ");
    }

    @Override
    public String as_dScriptArg() {
        if (isEmpty()) return null;
        StringBuilder dScriptArg = new StringBuilder();
        dScriptArg.append(prefix + ":");
        for (String item : this)
            dScriptArg.append(item + "|");

        return dScriptArg.toString().substring(0, dScriptArg.length() - 1);
    }

    public String dScriptArgValue() {
        StringBuilder dScriptArg = new StringBuilder();
        for (String item : this)
            dScriptArg.append(item + "|");
        return dScriptArg.toString().substring(0, dScriptArg.length() - 1);
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        // Desensitize the attribute for comparison

        if (attribute.startsWith(".ascslist")) {
            StringBuilder dScriptArg = new StringBuilder();
            for (String item : this)
                dScriptArg.append(item + ", ");
            return new Element(dScriptArg.toString().substring(0, dScriptArg.length() - 2))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith(".get")) {
            int index = attribute.getIntContext(1);
            if (index > size()) return null;
            String item = get(index - 1);
            return new Element(item).getAttribute(attribute.fulfill(1));
        }

        return new Element(dScriptArgValue()).getAttribute(attribute);
    }

}
