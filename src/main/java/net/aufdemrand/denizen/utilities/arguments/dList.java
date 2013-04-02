package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;

public class dList extends ArrayList<String> implements dScriptArgument {

    public static dList valueOf(String string) {
        if (string == null) return null;

        String prefix = null;
        // Strip prefix (ie. targets:...)
        String[] parts = string.split(":", 2);
        if (parts.length > 1) {
            prefix = parts[0];
            string = parts[1];
        }

        return new dList(prefix, string);
    }

    private String prefix;

    public dList(String prefix, String items) {
        if (prefix == null) this.prefix = "list";
        else this.prefix = prefix;
        addAll(Arrays.asList(items.split("\\|")));
    }

    public dList(String prefix, java.util.List<String> items) {
        if (prefix == null) this.prefix = "list";
        else this.prefix = prefix;
        addAll(items);
    }

    public dList(java.util.List<String> items) {
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
        if (isEmpty()) return "";
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

        if (attribute.startsWith("ascslist")) {
            StringBuilder dScriptArg = new StringBuilder(); 
            for (String item : this)
                dScriptArg.append(item + ", ");
            return new Element(dScriptArg.toString().substring(0, dScriptArg.length() - 2))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("get")) {
            int index = attribute.getIntContext(1);
            if (index > size()) return null;
            String item;
            if (index > 0) item = get(index - 1);
            else item = get(0);
            return new Element(item).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE.toString())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        return new Element(dScriptArgValue()).getAttribute(attribute);
    }

}
