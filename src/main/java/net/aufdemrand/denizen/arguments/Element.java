package net.aufdemrand.denizen.arguments;

import net.aufdemrand.denizen.interfaces.dScriptArgument;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.util.Arrays;

public class Element implements dScriptArgument {

    /**
     *
     * @param string  the string or dScript argument String
     * @return  a dScript dList
     *
     */
    @ObjectFetcher("el")
    public static dScriptArgument valueOf(String string) {
        if (string == null) return null;

        String prefix = null;
        // Strip prefix (ie. targets:...)
        if (string.split(":").length > 1) {
            prefix = string.split(":", 2)[0];
            string = string.split(":", 2)[1];
        }

        return new Element(prefix, string);
    }




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

    public double asDouble() {
        return Double.valueOf(element);
    }

    public float asFloat() {
        return Float.valueOf(element);
    }

    public int asInt() {
        return Integer.valueOf(element);
    }

    public boolean asBoolean() {
        return Boolean.valueOf(element);
    }

    public String asString() {
        return element;
    }




    private String prefix;

    @Override
    public String getType() {
        return "element";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public String identify() {
        return element;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        if (attribute.startsWith("asint")
                || attribute.startsWith("as_int"))
            try { return new Element(String.valueOf(Integer.valueOf(element)))
                    .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Integer.");
                return null;
            }

        if (attribute.startsWith("asdouble")
                || attribute.startsWith("as_double"))
            try { return new Element(String.valueOf(Double.valueOf(element)))
                    .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Double.");
                return null;
            }

        if (attribute.startsWith("asmoney")
                || attribute.startsWith("as_money")) {
            try {
                DecimalFormat d = new DecimalFormat("0.00");
            return new Element(String.valueOf(d.format(Double.valueOf(element))))
                    .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Money format.");
                return null;
            }
        }

        if (attribute.startsWith("asboolean")
                || attribute.startsWith("as_boolean"))
            return new Element(Boolean.valueOf(element).toString())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("aslist")
                || attribute.startsWith("as_list"))
            return new dList(element).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("substring")) {            // substring[2,8]
            int beginning_index = Integer.valueOf(attribute.getContext(1).split(",")[0]) - 1;
            int ending_index = Integer.valueOf(attribute.getContext(1).split(",")[1]) - 1;
            return new Element(String.valueOf(element.substring(beginning_index, ending_index)))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("last_color"))
            return new Element(String.valueOf(ChatColor.getLastColors(element))).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("split") && attribute.startsWith("limit", 2)) {
            String split_string = (attribute.hasContext(1) ? attribute.getContext(1) : " ");
            Integer limit = (attribute.hasContext(2) ? attribute.getIntContext(2) : 1);
            if (split_string.toUpperCase().startsWith("regex:"))
                return new dList(Arrays.asList(element.split(split_string.split(":", 2)[1], limit))).getAttribute(attribute.fulfill(1));
            else
                return new dList(Arrays.asList(StringUtils.split(element, split_string, limit))).getAttribute(attribute.fulfill(1));        }

        if (attribute.startsWith("split")) {
            String split_string = (attribute.hasContext(1) ? attribute.getContext(1) : " ");
            if (split_string.toUpperCase().startsWith("regex:"))
                return new dList(Arrays.asList(element.split(split_string.split(":", 2)[1]))).getAttribute(attribute.fulfill(1));
            else
                return new dList(Arrays.asList(StringUtils.split(element, split_string))).getAttribute(attribute.fulfill(1));
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
