package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;

import java.util.ArrayList;
import java.util.Arrays;

public class Element extends ArrayList<String> implements dScriptArgument {

    /**
     *
     * @param string  the string or dScript argument String
     * @return  a dScript List
     *
     */
    public static Element valueOf(String string) {
        if (string == null) return null;

        String prefix = null;
        // Strip prefix (ie. targets:...)
        if (string.split(":").length > 1) {
            prefix = string.split(":", 2)[0];
            string = string.split(":", 2)[1];
        }

        return new Element(prefix, string);
    }

    private String prefix;

    public Element(String prefix, String items) {
        if (prefix == null) this.prefix = "list";
        else this.prefix = prefix;
        addAll(Arrays.asList(items.split("\\|")));
    }

    public Element(String prefix, java.util.List<String> items) {
        if (prefix == null) this.prefix = "list";
        else this.prefix = prefix;
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

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(String attribute) {

        if (attribute == null) return null;

        // Desensitize the attribute for comparison
        attribute = attribute.toLowerCase();

        if (attribute.startsWith(".aslist")) {
            StringBuilder dScriptArg = new StringBuilder();
            for (String item : this)
                dScriptArg.append(item + "|");
            return dScriptArg.toString().substring(0, dScriptArg.length() - 1);
        }

        if (attribute.startsWith(".ascslist")) {
            StringBuilder dScriptArg = new StringBuilder();
            for (String item : this)
                dScriptArg.append(item + ", ");
            return dScriptArg.toString().substring(0, dScriptArg.length() - 2);
        }

        if (attribute.startsWith(".get[")) {
            int index = Integer.valueOf(attribute.split("\\[")[1].split("\\]")[0]);
            if (index > size()) return null;

            String item = get(index - 1);

            attribute = "." + attribute.split("\\.", 2)[1];



        }


        return null;
    }

}
