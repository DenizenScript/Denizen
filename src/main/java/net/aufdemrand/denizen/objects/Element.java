package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Element implements dObject {

    public final static Element TRUE = new Element(Boolean.TRUE);
    public final static Element FALSE = new Element(Boolean.FALSE);

    /**
     *
     * @param string  the string or dScript argument String
     * @return  a dScript dList
     *
     */
    @ObjectFetcher("el")
    public static dObject valueOf(String string) {
        if (string == null) return null;

        return new Element(string);
    }

    private String element;

    public Element(String string) {
        this.prefix = "element";
        this.element = string;
    }

    public Element(Integer integer) {
        this.prefix = "integer";
        this.element = String.valueOf(integer);
    }

    public Element(Double dbl) {
        this.prefix = "double";
        this.element = String.valueOf(dbl);
    }

    public Element(Boolean bool) {
        this.prefix = "boolean";
        this.element = String.valueOf(bool);
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
        return "Element";
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
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public String identify() {
        return element;
    }

    @Override
    public String toString() {
        return identify();
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
            return dList.valueOf(element).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("asentity")
                || attribute.startsWith("as_entity"))
            return dEntity.valueOf(element).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("aslocation")
                || attribute.startsWith("as_location"))
            return dLocation.valueOf(element).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("asplayer")
                || attribute.startsWith("as_player"))
            return dPlayer.valueOf(element).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("asnpc")
                || attribute.startsWith("as_npc"))
            return dNPC.valueOf(element).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("asitem")
                || attribute.startsWith("as_item"))
            return dItem.valueOf(element).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("asscript")
                || attribute.startsWith("as_script"))
            return dScript.valueOf(element).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("asduration")
                || attribute.startsWith("as_duration"))
            return Duration.valueOf(element).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("contains")) {
            String contains = attribute.getContext(1);

            if (contains.toLowerCase().startsWith("regex:")) {

                if (Pattern.compile(contains.substring(("regex:").length()), Pattern.CASE_INSENSITIVE).matcher(element).matches())
                    return new Element("true").getAttribute(attribute.fulfill(1));
                else return new Element("false").getAttribute(attribute.fulfill(1));
            }

            else if (element.toLowerCase().contains(contains.toLowerCase()))
                return new Element("true").getAttribute(attribute.fulfill(1));
            else return new Element("false").getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("substring")) {            // substring[2,8]
            int beginning_index = Integer.valueOf(attribute.getContext(1).split(",")[0]) - 1;
            int ending_index = Integer.valueOf(attribute.getContext(1).split(",")[1]) - 1;
            return new Element(String.valueOf(element.substring(beginning_index, ending_index)))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("last_color"))
            return new Element(String.valueOf(ChatColor.getLastColors(element))).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("strip_color"))
            return new Element(String.valueOf(ChatColor.stripColor(element))).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("split") && attribute.startsWith("limit", 2)) {
            String split_string = (attribute.hasContext(1) ? attribute.getContext(1) : " ");
            Integer limit = (attribute.hasContext(2) ? attribute.getIntContext(2) : 1);
            if (split_string.toLowerCase().startsWith("regex:"))
                return new dList(Arrays.asList(element.split(split_string.split(":", 2)[1], limit))).getAttribute(attribute.fulfill(1));
            else
                return new dList(Arrays.asList(StringUtils.split(element, split_string, limit))).getAttribute(attribute.fulfill(1));        }

        if (attribute.startsWith("split")) {
            String split_string = (attribute.hasContext(1) ? attribute.getContext(1) : " ");
            if (split_string.toLowerCase().startsWith("regex:"))
                return new dList(Arrays.asList(element.split(split_string.split(":", 2)[1]))).getAttribute(attribute.fulfill(1));
            else
                return new dList(Arrays.asList(StringUtils.split(element, split_string))).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("sqrt")) {
            return new Element(Math.sqrt(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("abs")) {
            return new Element(Math.abs(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("length")) {
            return new Element(element.length())
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

        // Unfilled attributes past this point probably means the tag is spelled
        // incorrectly. So instead of just passing through what's been resolved
        // so far, 'null' shall be returned with an error message.

        if (attribute.attributes.size() > 0)                    {
            dB.echoError("Unfilled attributes '" + attribute.attributes.toString() + "'" +
                    "for tag <" + attribute.getOrigin() + ">!");
            return "null";
        } else {
            dB.log("Filled tag <" + attribute.getOrigin() + "> with '" + element + "'.");
            return element;
        }
    }

}
