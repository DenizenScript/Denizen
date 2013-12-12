package net.aufdemrand.denizen.objects;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.objects.aH.Argument;
import net.aufdemrand.denizen.scripts.commands.core.*;
import net.aufdemrand.denizen.scripts.commands.core.Comparable;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

// <--[language]
// @name Element
// @group Object System
// @description
// Elements are simple objects that contain either a boolean (true/false),
// string, or number value. Their main usage is within the replaceable tag
// system, often times returned from the use of another tag that isn't returning
// a specific object type, such as a location or entity. For example,
// <player.name> or <li@item|item2|item3.as_cslist> will both return Elements.
//
// Pluses to the Element system is the ability to utilize its attributes that
// can provide a range of functionality that should be familiar from any other
// programming language, such as 'to_uppercase', 'split', 'replace', 'contains',
// as_int, any many more. See 'element' tags for more information.
//
// While information fetched from other tags resulting in an Element is often
// times automatically handled, it may be desirable to utilize element
// attributes from strings/numbers/etc. that aren't already an element object.
// To accomplish this, the object fetcher can be used to create a new element.
// Element has a constructor, el@val[element_value], that will allow the
// creation of a new element. For example: <el@val[This_is_a_test.].to_uppercase>
// will result in the value 'THIS_IS_A_TEST.' Note that while other objects often
// return their object identifier (el@, li@, e@, etc.), elements do not.

// -->


public class Element implements dObject {

    public final static Element TRUE = new Element(Boolean.TRUE);
    public final static Element FALSE = new Element(Boolean.FALSE);
    public final static Element SERVER = new Element("server");
    public final static Element NULL = new Element("null");

    final static Pattern VALUE_PATTERN =
            Pattern.compile("el@val(?:ue)?\\[([^\\[\\]]+)\\].*",
                    Pattern.CASE_INSENSITIVE);

    /**
     *
     * @param string  the string or dScript argument String
     * @return  a dScript dList
     *
     */
    @Fetchable("el")
    public static Element valueOf(String string) {
        if (string == null) return null;

        Matcher m = VALUE_PATTERN.matcher(string);

        // Allow construction of elements with el@val[<value>]
        if (m.matches()) {
            String value = m.group(1);
            Argument arg = Argument.valueOf(value);

            if (arg.matchesPrimitive(aH.PrimitiveType.Integer))
                return new Element(aH.getIntegerFrom(value));
            else if (arg.matchesPrimitive(aH.PrimitiveType.Double))
                return new Element(aH.getDoubleFrom(value));
            else return new Element(value);
        }

        return new Element(string);
    }

    public static boolean matches(String string) {
        return string != null;
    }

    private final String element;

    public Element(String string) {
        this.prefix = "element";
        this.element = string;
    }

    public Element(Boolean bool) {
        this.prefix = "boolean";
        this.element = String.valueOf(bool);
    }

    public Element(Integer integer) {
        this.prefix = "integer";
        this.element = String.valueOf(integer);
    }

    public Element(Byte byt) {
        this.prefix = "byte";
        this.element = String.valueOf(byt);
    }

    public Element(Short shrt) {
        this.prefix = "short";
        this.element = String.valueOf(shrt);
    }

    public Element(Long lng) {
        this.prefix = "long";
        this.element = String.valueOf(lng);
    }

    public Element(Double dbl) {
        this.prefix = "double";
        this.element = String.valueOf(dbl);
    }

    public Element(Float flt) {
        this.prefix = "float";
        this.element = String.valueOf(flt);
    }

    public Element(String prefix, String string) {
        if (prefix == null) this.prefix = "element";
        else this.prefix = prefix;
        this.element = string;
    }

    public double asDouble() {
        return Double.valueOf(element.replaceAll("(el@)|%", ""));
    }

    public float asFloat() {
        return Float.valueOf(element.replaceAll("(el@)|%", ""));
    }

    public int asInt() {
        return Integer.valueOf(element.replaceAll("(el@)|%", ""));
    }

    public boolean asBoolean() {
        return Boolean.valueOf(element.replaceAll("el@", ""));
    }

    public String asString() {
        return element;
    }

    public boolean matchesType(Class<? extends dObject> dClass) {
        return ObjectFetcher.checkMatch(dClass, element);
    }

    public <T extends dObject> T asType(Class<T> dClass) {
        return ObjectFetcher.getObjectFrom(dClass, element);
    }

    private String prefix;

    @Override
    public String getObjectType() {
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


        ////////////////////
        //   COMPARABLE ATTRIBUTES
        ////////////////

        // <--[tag]
        // @attribute <el@element.is[<operator>].to[<element>]>
        // @returns Element(Boolean)
        // @description
        // Takes an operator, and compares the value of the element to the supplied
        // element. Returns the outcome of the comparable, either true or false. For
        // information on operators, see <@link language operator>.
        // -->

        // <--[tag]
        // @attribute <el@element.is[<operator>].than[<element>]>
        // @returns Element(Boolean)
        // @description
        // Takes an operator, and compares the value of the element to the supplied
        // element. Returns the outcome of the comparable, either true or false. For
        // information on operators, see <@link language operator>.
        // -->
        if (attribute.startsWith("is") && attribute.hasContext(1)
                && (attribute.startsWith("to", 2) || attribute.startsWith("than", 2)) && attribute.hasContext(2)) {

            // Use the Comparable object as implemented for the IF command. First, a new Comparable!
            Comparable com = new net.aufdemrand.denizen.scripts.commands.core.Comparable();

            // Comparable is the value of this element
            com.setComparable(element);
            // Compared_to is the value of the .to[] context.
            com.setComparedto(attribute.getContext(2));

            // Check for negative logic
            String operator;
            if (attribute.getContext(1).startsWith("!")) {
                operator = attribute.getContext(1).substring(1);
                com.setNegativeLogic();
            } else operator = attribute.getContext(1);

            // Operator is the value of the .is[] context. Valid are Comparable.Operators, same
            // as used by the IF command.
            Comparable.Operator comparableOperator = null;
            try {
                comparableOperator = Comparable.Operator.valueOf(operator.replace("==", "EQUALS")
                        .replace(">=", "OR_MORE").replace("<=", "OR_LESS").replace("<", "LESS")
                        .replace(">", "MORE").replace("=", "EQUALS").toUpperCase());
            }
            catch (IllegalArgumentException e) { }

            if (comparableOperator != null) {
                com.setOperator(comparableOperator);

                return new Element(com.determineOutcome()).getAttribute(attribute.fulfill(2));
            }
            else {
                dB.echoError("Unknown operator '" + operator + "'.");
            }
        }


        /////////////////////
        //   CONVERSION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <el@element.as_boolean>
        // @returns Element(Boolean)
        // @description
        // Returns the element as true/false.
        // -->
        if (attribute.startsWith("asboolean")
                || attribute.startsWith("as_boolean"))
            return new Element(Boolean.valueOf(element).toString())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_double>
        // @returns Element(Decimal)
        // @description
        // Returns the element as a number with a decimal.
        // -->
        if (attribute.startsWith("asdouble")
                || attribute.startsWith("as_double"))
            try { return new Element(Double.valueOf(element))
                    .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Double.");
                return new Element("null").getAttribute(attribute.fulfill(1));
            }

        // <--[tag]
        // @attribute <el@element.as_duration>
        // @returns Duration
        // @description
        // Returns the element as a duration.
        // -->
        if (attribute.startsWith("asduration")
                || attribute.startsWith("as_duration"))
            return Duration.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_entity>
        // @returns dEntity
        // @description
        // Returns the element as an entity. Note: the value must be a valid entity.
        // -->
        if (attribute.startsWith("asentity")
                || attribute.startsWith("as_entity"))
            return dEntity.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.round_up>
        // @returns Element(Number)
        // @description
        // Rounds a decimal upward.
        // -->
        if (attribute.startsWith("round_up"))
            try {
                return new Element((int)Math.ceil(Double.valueOf(element)))
                        .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Integer.");
                return new Element("null").getAttribute(attribute.fulfill(1));
            }

        // <--[tag]
        // @attribute <el@element.round_down>
        // @returns Element(Number)
        // @description
        // Rounds a decimal downward.
        // -->
        if (attribute.startsWith("round_down"))
            try {
                return new Element((int)Math.floor(Double.valueOf(element)))
                        .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Integer.");
                return new Element("null").getAttribute(attribute.fulfill(1));
            }

        // <--[tag]
        // @attribute <el@element.as_int>
        // @returns Element(Number)
        // @description
        // Returns the element as a number without a decimal. Rounds double values.
        // -->
        if (attribute.startsWith("asint")
                || attribute.startsWith("as_int"))
            try {
                // Round the Double instead of just getting its
                // value as an Integer (which would incorrectly
                // turn 2.9 into 2)
                return new Element(Math.round(Double.valueOf(element)))
                        .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Integer.");
                return new Element("null").getAttribute(attribute.fulfill(1));
            }

        // <--[tag]
        // @attribute <el@element.as_item>
        // @returns dItem
        // @description
        // Returns the element as an item. Additional attributes can be accessed by dItem.
        // Note: the value must be a valid item.
        // -->
        if (attribute.startsWith("asitem")
                || attribute.startsWith("as_item"))
            return dItem.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_list>
        // @returns dList
        // @description
        // Returns the element as a list.
        // -->
        if (attribute.startsWith("aslist")
                || attribute.startsWith("as_list"))
            return dList.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_location>
        // @returns dLocation
        // @description
        // Returns the element as a location. Note: the value must be a valid location.
        // -->
        if (attribute.startsWith("aslocation")
                || attribute.startsWith("as_location"))
            return dLocation.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_money>
        // @returns Element(Decimal)
        // @description
        // Returns the element as a number with two decimal places.
        // -->
        if (attribute.startsWith("asmoney")
                || attribute.startsWith("as_money")) {
            try {
                DecimalFormat d = new DecimalFormat("0.00");
                return new Element(d.format(Double.valueOf(element)))
                        .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Money format.");
                return new Element("null").getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_npc>
        // @returns dNPC
        // @description
        // Returns the element as an NPC. Note: the value must be a valid NPC.
        // -->
        if (attribute.startsWith("asnpc")
                || attribute.startsWith("as_npc"))
            return dNPC.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_player>
        // @returns dPlayer
        // @description
        // Returns the element as a player. Note: the value must be a valid player. Can be online or offline.
        // -->
        if (attribute.startsWith("asplayer")
                || attribute.startsWith("as_player"))
            return dPlayer.valueOf(element).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_script>
        // @returns dScript
        // @description
        // Returns the element as a script. Note: the value must be a valid script.
        // -->
        if (attribute.startsWith("asscript")
                || attribute.startsWith("as_script"))
            return dScript.valueOf(element).getAttribute(attribute.fulfill(1));


        /////////////////////
        //   DEBUG ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <el@element.debug.log>
        // @returns Element
        // @description
        // Prints the Element's debug representation in the console and returns true.
        // -->
        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE)
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <el@element.debug.no_color>
        // @returns Element
        // @description
        // Returns a standard debug representation of the Element with colors stripped.
        // -->
        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <el@element.debug>
        // @returns Element
        // @description
        // Returns a standard debug representation of the Element.
        // -->
        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.prefix>
        // @returns Element
        // @description
        // Returns the prefix of the element.
        // -->
        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));


        /////////////////////
        //   STRING CHECKING ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <el@element.contains[<string>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the element contains a specified string, case insensitive. Can use
        // regular expression by prefixing the string with regex:
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

        // <--[tag]
        // @attribute <el@element.ends_with[<string>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the element ends with a specified string.
        // -->
        if (attribute.startsWith("ends_with") || attribute.startsWith("endswith"))
            return new Element(element.endsWith(attribute.getContext(1))).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.last_color>
        // @returns Element
        // @description
        // Returns the ChatColors used at the end of a string.
        // -->
        if (attribute.startsWith("last_color"))
            return new Element(ChatColor.getLastColors(element)).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.length>
        // @returns Element(Number)
        // @description
        // Returns the length of the element.
        // -->
        if (attribute.startsWith("length")) {
            return new Element(element.length())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.starts_with[<string>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the element starts with a specified string.
        // -->
        if (attribute.startsWith("starts_with") || attribute.startsWith("startswith"))
            return new Element(element.startsWith(attribute.getContext(1))).getAttribute(attribute.fulfill(1));


        /////////////////////
        //   STRING MANIPULATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <el@element.after[<string>]>
        // @returns Element
        // @description
        // Returns the portion of an element after a specified string. ie. <el@helloWorld.after[hello]> returns 'World'.
        // -->
        if (attribute.startsWith("after")) {
            String delimiter = attribute.getContext(1);
            if (element.contains(delimiter))
                return new Element(element.substring
                    (element.indexOf(delimiter) + delimiter.length()))
                    .getAttribute(attribute.fulfill(1));
            else
                return new Element(element)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.before[<string>]>
        // @returns Element
        // @description
        // Returns the portion of an element before a specified string.
        // -->
        if (attribute.startsWith("before")) {
            String delimiter = attribute.getContext(1);
            if (element.contains(delimiter))
                return new Element(element.substring
                    (0, element.indexOf(delimiter)))
                    .getAttribute(attribute.fulfill(1));
            else
                return new Element(element)
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.replace[<string>]>
        // @returns Element
        // @description
        // Returns the element with all instances of a string removed.
        // -->

        // <--[tag]
        // @attribute <el@element.replace[<string>].with[<string>]>
        // @returns Element
        // @description
        // Returns the element with all instances of a string replaced with another.
        // -->
        if (attribute.startsWith("replace")
                && attribute.hasContext(1)) {

            String replace = attribute.getContext(1);
            String replacement = "";
            attribute.fulfill(1);
            if (attribute.startsWith("with")) {
                if (attribute.hasContext(1)) {
                    replacement = attribute.getContext(1);
                    if (replacement == null)
                        replacement = "";
                    attribute.fulfill(1);
                }
            }

            return new Element(element.replace(replace, replacement))
                        .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <el@element.split[<string>].limit[<#>]>
        // @returns dList
        // @description
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

        // <--[tag]
        // @attribute <el@element.split[<string>]>
        // @returns dList
        // @description
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

        // <--[tag]
        // @attribute <el@element.strip_color>
        // @returns Element
        // @description
        // Returns the element with all color encoding stripped.
        // -->
        if (attribute.startsWith("strip_color"))
            return new Element(ChatColor.stripColor(element)).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.trim>
        // @returns Element
        // @description
        // Returns the value of an element minus any leading or trailing whitespace.
        // -->
        if (attribute.startsWith("trim"))
            return new Element(element.trim()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.upper>
        // @returns Element
        // @description
        // Returns the value of an element in all uppercase letters.
        // -->
        if (attribute.startsWith("upper"))
            return new Element(element.toUpperCase()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.lower>
        // @returns Element
        // @description
        // Returns the value of an element in all lowercase letters.
        // -->
        if (attribute.startsWith("lower"))
            return new Element(element.toLowerCase()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.totitlecase>
        // @returns Element
        // @description
        // Returns The Value Of An Element In Title Case.
        // -->
        if (attribute.startsWith("totitlecase")) {
            if (element.length() == 0) {
                return new Element("").getAttribute(attribute.fulfill(1));
            }
            StringBuilder TitleCase = new StringBuilder(element.length());
            String Upper = element.toUpperCase();
            String Lower = element.toLowerCase();
            TitleCase.append(Upper.charAt(0));
            for (int i = 1; i < element.length(); i++) {
                if (element.charAt(i - 1) == ' ')
                    TitleCase.append(Upper.charAt(i));
                else
                    TitleCase.append(Lower.charAt(i));
            }
            return new Element(TitleCase.toString()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.substring[<#>(,<#>)]>
        // @returns Element
        // @description
        // Returns the portion of an element between two string indices.
        // If no second index is specified, it will return the portion of an
        // element after the specified index.
        // -->
        if (attribute.startsWith("substring")||attribute.startsWith("substr")) {            // substring[2,8]
            int beginning_index = Integer.valueOf(attribute.getContext(1).split(",")[0]) - 1;
            int ending_index;
            if (attribute.getContext(1).split(",").length > 1)
                ending_index = Integer.valueOf(attribute.getContext(1).split(",")[1]);
            else
                ending_index = element.length();
            if (beginning_index < 0) beginning_index = 0;
            if (beginning_index > element.length()) beginning_index = element.length();
            if (ending_index > element.length()) ending_index = element.length();
            if (ending_index < beginning_index) ending_index = beginning_index;
            return new Element(element.substring(beginning_index, ending_index))
                    .getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   MATH ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <el@element.abs>
        // @returns Element(Decimal)
        // @description
        // Returns the absolute value of the element.
        // -->
        if (attribute.startsWith("abs")) {
            return new Element(Math.abs(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.add[<#>]>
        // @returns Element(Decimal)
        // @description
        // Returns the element plus a number.
        // -->
        if (attribute.startsWith("add")
                && attribute.hasContext(1)) {
            return new Element(asDouble() + aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.div[<#>]>
        // @returns Element(Decimal)
        // @description
        // Returns the element divided by a number.
        // -->
        if (attribute.startsWith("div")
                && attribute.hasContext(1)) {
            return new Element(asDouble() / aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.mod[<#>]>
        // @returns Element(Decimal)
        // @description
        // Returns the remainder of the element divided by a number.
        // -->
        if (attribute.startsWith("mod")
                && attribute.hasContext(1)) {
            return new Element(asDouble() % aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.mul[<#>]>
        // @returns Element(Decimal)
        // @description
        // Returns the element multiplied by a number.
        // -->
        if (attribute.startsWith("mul")
                && attribute.hasContext(1)) {
            return new Element(asDouble() * aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.sqrt>
        // @returns Element(Decimal)
        // @description
        // Returns the square root of the element.
        // -->
        if (attribute.startsWith("sqrt")) {
            return new Element(Math.sqrt(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.sub[<#>]>
        // @returns Element(Decimal)
        // @description
        // Returns the element minus a number.
        // -->
        if (attribute.startsWith("sub")
                && attribute.hasContext(1)) {
            return new Element(asDouble() - aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }


        // Unfilled attributes past this point probably means the tag is spelled
        // incorrectly. So instead of just passing through what's been resolved
        // so far, 'null' shall be returned with a debug message.

        if (attribute.attributes.size() > 0) {
            dB.echoDebug(attribute.getScriptEntry(), "Unfilled attributes '" + attribute.attributes.toString() + "'" +
                    "for tag <" + attribute.getOrigin() + ">!");
            return "null";
        } else {
            dB.echoDebug(attribute.getScriptEntry(), "Filled tag <" + attribute.getOrigin() + "> with '" +
                    element.replace((char)0x01, '<').replace((char)0x02, '>').replace(dList.internal_escape, "|") + "'.");
            return element;
        }
    }

}
