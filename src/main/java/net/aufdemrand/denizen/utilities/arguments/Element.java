package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class Element implements dScriptArgument {

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
    private String element;

    public Element(String string) {
        this.prefix = "element";
        this.element = string;
    }

    public Element(String prefix, String string) {
        if (prefix == null) this.prefix = "element";
        else this.prefix = prefix;
        this.element = string;
    }

    @Override
    public String getDefaultPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + element + "<G>'  ");
    }

    @Override
    public String as_dScriptArg() {
        return prefix + ":" + element;
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        if (attribute.startsWith("asint"))
            try { return new Element(String.valueOf(Integer.valueOf(element)))
                    .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) { dB.echoError("'" + element + "' is not a valid Integer."); }

        if (attribute.startsWith("asdouble"))
            try { return new Element(String.valueOf(Double.valueOf(element)))
                    .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) { dB.echoError("'" + element + "' is not a valid Double."); }

        if (attribute.startsWith("asmoney")) {
            try {
                DecimalFormat d = new DecimalFormat("0.00");
            return new Element(String.valueOf(d.format(Double.valueOf(element))))
                    .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) { dB.echoError("'" + element + "' is not a valid Money format."); }
        }

        if (attribute.startsWith("asboolean"))
            return new Element(Boolean.valueOf(element).toString())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("substring")) {            // suibstring[2|8]
            int beginning_index = Integer.valueOf(attribute.getContext(1).split("\\|")[0]) - 1;
            int ending_index = Integer.valueOf(attribute.getContext(1).split("\\|")[1]) - 1;
            return new Element(String.valueOf(element.substring(beginning_index, ending_index)))
                    .getAttribute(attribute.fulfill(1));
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

        return element;
    }

}
