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
    public static Element valueOf(String string) {
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
        return Double.valueOf(element.replace("%", ""));
    }

    public float asFloat() {
        return Float.valueOf(element.replace("%", ""));
    }

    public int asInt() {
        return Integer.valueOf(element.replace("%", ""));
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

        // <--
        // <element.asint> -> Element(Number)
        // Returns the element as a number without a decimal.
        // -->
        if (attribute.startsWith("asint")
                || attribute.startsWith("as_int"))
            try {
                // Round the Double instead of just getting its
                // value as an Integer (which would incorrectly
                // turn 2.9 into 2)
                return new Element(String.valueOf
                        (Math.round(Double.valueOf(element))))
                        .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Integer.");
                return null;
            }

        // <--
        // <element.asdouble> -> Element(Number)
        // Returns the element as a number with a decimal.
        // -->
        if (attribute.startsWith("asdouble")
                || attribute.startsWith("as_double"))
            try { return new Element(String.valueOf(Double.valueOf(element)))
                    .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Double.");
                return null;
            }

        // <--
        // <element.asmoney> -> Element(Number)
        // Returns the element as a number with two decimal places.
        // -->
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

        // <--
        // <element.asboolean> -> Element(Boolean)
        // Returns the element as true/false.
        // -->
        if (attribute.startsWith("asboolean")
                || attribute.startsWith("as_boolean"))
            return new Element(Boolean.valueOf(element).toString())
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <element.aslist> -> dList
        // Returns the element as a list.
        // -->
        if (attribute.startsWith("aslist")
                || attribute.startsWith("as_list"))
            return dList.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--
        // <element.asentity> -> dEntity
        // Returns the element as an entity.
        // -->
        if (attribute.startsWith("asentity")
                || attribute.startsWith("as_entity"))
            return dEntity.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--
        // <element.aslocation> -> dLocation
        // Returns the element as a location.
        // -->
        if (attribute.startsWith("aslocation")
                || attribute.startsWith("as_location"))
            return dLocation.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--
        // <element.asplayer> -> dPlayer
        // Returns the element as a player.
        // -->
        if (attribute.startsWith("asplayer")
                || attribute.startsWith("as_player"))
            return dPlayer.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--
        // <element.asnpc> -> dNPC
        // Returns the element as an NPC.
        // -->
        if (attribute.startsWith("asnpc")
                || attribute.startsWith("as_npc"))
            return dNPC.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--
        // <element.asitem> -> dItem
        // Returns the element as an item.
        // -->
        if (attribute.startsWith("asitem")
                || attribute.startsWith("as_item"))
            return dItem.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--
        // <element.asscript> -> dScript
        // Returns the element as a script.
        // -->
        if (attribute.startsWith("asscript")
                || attribute.startsWith("as_script"))
            return dScript.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--
        // <element.asentity> -> Duration
        // Returns the element as a duration.
        // -->
        if (attribute.startsWith("asduration")
                || attribute.startsWith("as_duration"))
            return Duration.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--
        // <element.contains[<string>]> -> Element(Boolean)
        // Returns whether the element contains a specified string.
        // -->
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

        // <--
        // <element.after[<string>]> -> Element
        // Returns the portion of an element after a specified string.
        // -->
        // Get the substring after a certain text
        if (attribute.startsWith("after")) {
            String delimiter = attribute.getContext(1);
            return new Element(String.valueOf(element.substring
                    (element.indexOf(delimiter) + delimiter.length())))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.before[<string>]> -> Element
        // Returns the portion of an element before a specified string.
        // -->
        // Get the substring before a certain text
        if (attribute.startsWith("before")) {
            String delimiter = attribute.getContext(1);
            return new Element(String.valueOf(element.substring
                    (0, element.indexOf(delimiter))))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.substring[<#>(,<#>)]> -> Element
        // Returns the portion of an element between two string indices.
        // If no second index is specified, it will return the portion of an
        // element after the specified index.
        // -->
        if (attribute.startsWith("substring")||attribute.startsWith("substr")) {            // substring[2,8]
            int beginning_index = Integer.valueOf(attribute.getContext(1).split(",")[0]) - 1;
            int ending_index;
            if (attribute.getContext(1).split(",").length > 1)
                ending_index = Integer.valueOf(attribute.getContext(1).split(",")[1]) - 1;
            else
                ending_index = element.length();
            return new Element(String.valueOf(element.substring(beginning_index, ending_index)))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("last_color"))
            return new Element(String.valueOf(ChatColor.getLastColors(element))).getAttribute(attribute.fulfill(1));

        // <--
        // <element.strip_color> -> Element
        // Returns the element with all color encoding stripped.
        // -->
        if (attribute.startsWith("strip_color"))
            return new Element(String.valueOf(ChatColor.stripColor(element))).getAttribute(attribute.fulfill(1));

        // <--
        // <element.startswith[<string>]> -> Element(Boolean)
        // Returns whether the element starts with a specified string.
        // -->
        if (attribute.startsWith("starts_with") || attribute.startsWith("startswith"))
            return new Element(String.valueOf(element.startsWith(attribute.getContext(1)))).getAttribute(attribute.fulfill(1));

        // <--
        // <element.endswith[<string>]> -> Element(Boolean)
        // Returns whether the element ends with a specified string.
        // -->
        if (attribute.startsWith("ends_with") || attribute.startsWith("endswith"))
            return new Element(String.valueOf(element.endsWith(attribute.getContext(1)))).getAttribute(attribute.fulfill(1));

        // <--
        // <element.split[<string>].limit[<#>]> -> dList
        // Returns a list of portions of this element, split by the specified string,
        // and capped at the specified number of max list items.
        // -->
        if (attribute.startsWith("split") && attribute.startsWith("limit", 2)) {
            String split_string = (attribute.hasContext(1) ? attribute.getContext(1) : " ");
            Integer limit = (attribute.hasContext(2) ? attribute.getIntContext(2) : 1);
            if (split_string.toLowerCase().startsWith("regex:"))
                return new dList(Arrays.asList(element.split(split_string.split(":", 2)[1], limit)))
                        .getAttribute(attribute.fulfill(1));
            else
                return new dList(Arrays.asList(StringUtils.split(element, split_string, limit)))
                        .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.split[<string>]> -> dList
        // Returns a list of portions of this element, split by the specified string.
        // -->
        if (attribute.startsWith("split")) {
            String split_string = (attribute.hasContext(1) ? attribute.getContext(1) : " ");
            if (split_string.toLowerCase().startsWith("regex:"))
                return new dList(Arrays.asList(element.split(split_string.split(":", 2)[1])))
                        .getAttribute(attribute.fulfill(1));
            else
                return new dList(Arrays.asList(StringUtils.split(element, split_string)))
                        .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.sqrt> -> Element(Number)
        // Returns the square root of the element.
        // -->
        if (attribute.startsWith("sqrt")) {
            return new Element(Math.sqrt(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.abs> -> Element(Number)
        // Returns the absolute value of the element.
        // -->
        if (attribute.startsWith("abs")) {
            return new Element(Math.abs(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.mul[<#>]> -> Element(Number)
        // Returns the element multiplied by a number.
        // -->
        if (attribute.startsWith("mul")
                && attribute.hasContext(1)) {
            return new Element(asDouble() * aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.sub[<#>]> -> Element(Number)
        // Returns the element minus a number.
        // -->
        if (attribute.startsWith("sub")
                && attribute.hasContext(1)) {
            return new Element(asDouble() - aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.add[<#>]> -> Element(Number)
        // Returns the element plus a number.
        // -->
        if (attribute.startsWith("add")
                && attribute.hasContext(1)) {
            return new Element(asDouble() + aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.div[<#>]> -> Element(Number)
        // Returns the element divided by a number.
        // -->
        if (attribute.startsWith("div")
                && attribute.hasContext(1)) {
            return new Element(asDouble() / aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.mod[<#>]> -> Element(Number)
        // Returns the remainder of the element divided by a number.
        // -->
        if (attribute.startsWith("mod")
                && attribute.hasContext(1)) {
            return new Element(asDouble() % aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.replace[<string>]> -> Element
        // Returns the element with all instances of a string removed.
        // -->

        // <--
        // <element.replace[<string>].with[<string>]> -> Element
        // Returns the element with all instances of a string replaced with another.
        // -->
        if (attribute.startsWith("replace")
                && attribute.hasContext(1)) {

            String replace = attribute.getContext(1);
            String replacement = "";
            if (attribute.startsWith("with", 2)) {
                if (attribute.hasContext(2)) replacement = attribute.getContext(2);
                attribute.fulfill(1);
            }

            return new Element(element.replace(replace, replacement))
                        .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.length> -> Element(Number)
        // Returns the length of the element.
        // -->
        if (attribute.startsWith("length")) {
            return new Element(element.length())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <element.prefix> -> Element
        // Returns the prefix of the element.
        // -->
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
