package net.aufdemrand.denizen.objects;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.core.Comparable;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.core.EscapeTags;
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
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

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
            return new Element(value);
        }

        return new Element(string.toLowerCase().startsWith("el@") ? string.substring(3): string);
    }

    public static boolean matches(String string) {
        return string != null;
    }

    /**
     * Handle null dObjects appropriately for potentionally null tags.
     * Will show a dB error message and return Element.NULL for null objects.
     *
     * @param tag The input string that produced a potentially null object, for debugging.
     * @param object The potentially null object.
     * @param type The type of object expected, for debugging. (EG: 'dNPC')
     * @return The object or Element.NULL if the object is null.
     */
    public static dObject HandleNull(String tag, dObject object, String type) {
        if (object == null) {
            dB.echoError("'" + tag + "' is an invalid " + type + "!");
            return Element.NULL;
        }
        return object;
    }

    private final String element;

    public Element(String string) {
        this.prefix = "element";
        if (string == null)
            this.element = "null";
        else
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
        try {
            return Integer.valueOf(element.replaceAll("(el@)|%", ""));
        }
        catch (NumberFormatException ex) {
            dB.echoError("'" + element + "' is not a valid integer!");
            return 0;
        }
    }

    public boolean asBoolean() {
        return Boolean.valueOf(element.replaceAll("el@", ""));
    }

    public String asString() {
        return element;
    }

    public boolean isBoolean() {
        return (element != null && (element.equalsIgnoreCase("true") || element.equalsIgnoreCase("false")));
    }

    public boolean isDouble() {
        try {
            if (Double.valueOf(element) != null)
                return true;
        } catch (Exception e) {}
        return false;
    }

    public boolean isFloat() {
        try {
            if (Float.valueOf(element) != null)
                return true;
        } catch (Exception e) {}
        return false;
    }

    public boolean isInt() {
        try {
            if (Integer.valueOf(element) != null)
                return true;
        } catch (Exception e) {}
        return false;
    }

    public boolean isString() {
        return (element != null && !element.isEmpty());
    }

    public boolean matchesType(Class<? extends dObject> dClass) {
        return ObjectFetcher.checkMatch(dClass, element);
    }

    public <T extends dObject> T asType(Class<T> dClass) {
        return ObjectFetcher.getObjectFrom(dClass, element);
    }

    public boolean matchesEnum(Enum[] values) {
        for (Enum value : values)
            if (value.name().replace("_", "").equalsIgnoreCase(element.replace("_", "")))
                return true;

        return false;
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
    public String identifySimple() {
        return identify();
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
        // @group comparison
        // @description
        // Takes an operator, and compares the value of the element to the supplied
        // element. Returns the outcome of the comparable, either true or false. For
        // information on operators, see <@link language operator>.
        // Equivalent to <@link tag el@element.is[<operator>].than[<element>]>
        // -->

        // <--[tag]
        // @attribute <el@element.is[<operator>].than[<element>]>
        // @returns Element(Boolean)
        // @group comparison
        // @description
        // Takes an operator, and compares the value of the element to the supplied
        // element. Returns the outcome of the comparable, either true or false. For
        // information on operators, see <@link language operator>.
        // Equivalent to <@link tag el@element.is[<operator>].to[<element>]>
        // -->
        if (attribute.startsWith("is") && attribute.hasContext(1)
                && (attribute.startsWith("to", 2) || attribute.startsWith("than", 2)) && attribute.hasContext(2)) {

            // Use the Comparable object as implemented for the IF command. First, a new Comparable!
            Comparable com = new net.aufdemrand.denizen.scripts.commands.core.Comparable();

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

                // Comparable is the value of this element
                com.setComparable(element);
                // Compared_to is the value of the .to[] context.
                com.setComparedto(attribute.getContext(2));

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
        // @group conversion
        // @description
        // Returns the element as true/false.
        // -->
        if (attribute.startsWith("asboolean")
                || attribute.startsWith("as_boolean"))
            return new Element(element.equalsIgnoreCase("true") ||
                    element.equalsIgnoreCase("t") || element.equalsIgnoreCase("1"))
                    .getAttribute(attribute.fulfill(1));

        // TODO: Why does this exist? It just throws an error or makes no changes.
        if (attribute.startsWith("asdouble")
                || attribute.startsWith("as_double"))
            try { return new Element(Double.valueOf(element))
                    .getAttribute(attribute.fulfill(1)); }
            catch (NumberFormatException e) {
                dB.echoError("'" + element + "' is not a valid Double.");
                return new Element("null").getAttribute(attribute.fulfill(1));
            }

        // <--[tag]
        // @attribute <el@element.as_int>
        // @returns Element(Number)
        // @group conversion
        // @description
        // Returns the element as a number without a decimal. Rounds decimal values.
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
        // @attribute <el@element.as_money>
        // @returns Element(Decimal)
        // @group conversion
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
                dB.echoError("'" + element + "' is not a valid number.");
                return new Element("null").getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.as_cuboid>
        // @returns dCuboid
        // @group conversion
        // @description
        // Returns the element as a cuboid. Note: the value must be a valid cuboid.
        // -->
        if (attribute.startsWith("ascuboid")
                || attribute.startsWith("as_cuboid"))
            return HandleNull(element, dCuboid.valueOf(element), "dCuboid").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_entity>
        // @returns dEntity
        // @group conversion
        // @description
        // Returns the element as an entity. Note: the value must be a valid entity.
        // -->
        if (attribute.startsWith("asentity")
                || attribute.startsWith("as_entity"))
            return HandleNull(element, dEntity.valueOf(element), "dEntity").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_inventory>
        // @returns dInventory
        // @group conversion
        // @description
        // Returns the element as an inventory. Note: the value must be a valid inventory.
        // -->
        if (attribute.startsWith("asinventory")
                || attribute.startsWith("as_inventory"))
            return HandleNull(element, dInventory.valueOf(element), "dInventory").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_item>
        // @returns dItem
        // @group conversion
        // @description
        // Returns the element as an item. Additional attributes can be accessed by dItem.
        // Note: the value must be a valid item.
        // -->
        if (attribute.startsWith("asitem")
                || attribute.startsWith("as_item"))
            return HandleNull(element, dItem.valueOf(element), "dItem").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_list>
        // @returns dList
        // @group conversion
        // @description
        // Returns the element as a list.
        // -->
        if (attribute.startsWith("aslist")
                || attribute.startsWith("as_list"))
            return HandleNull(element, dList.valueOf(element), "dList").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_location>
        // @returns dLocation
        // @group conversion
        // @description
        // Returns the element as a location. Note: the value must be a valid location.
        // -->
        if (attribute.startsWith("aslocation")
                || attribute.startsWith("as_location"))
            return HandleNull(element, dLocation.valueOf(element), "dLocation").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_material>
        // @returns dMaterial
        // @group conversion
        // @description
        // Returns the element as a material. Note: the value must be a valid material.
        // -->
        if (attribute.startsWith("asmaterial")
                || attribute.startsWith("as_material"))
            return HandleNull(element, dMaterial.valueOf(element), "dMaterial").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_npc>
        // @returns dNPC
        // @group conversion
        // @description
        // Returns the element as an NPC. Note: the value must be a valid NPC.
        // -->
        if (attribute.startsWith("asnpc")
                || attribute.startsWith("as_npc"))
            return HandleNull(element, dNPC.valueOf(element), "dNPC").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_player>
        // @returns dPlayer
        // @group conversion
        // @description
        // Returns the element as a player. Note: the value must be a valid player. Can be online or offline.
        // -->
        if (attribute.startsWith("asplayer")
                || attribute.startsWith("as_player"))
            return HandleNull(element, dPlayer.valueOf(element), "dPlayer").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_plugin>
        // @returns dPlugin
        // @group conversion
        // @description
        // Returns the element as a plugin. Note: the value must be a valid plugin.
        // -->
        if (attribute.startsWith("asplugin")
                || attribute.startsWith("as_plugin"))
            return HandleNull(element, dPlugin.valueOf(element), "dPlugin").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_script>
        // @returns dScript
        // @group conversion
        // @description
        // Returns the element as a script. Note: the value must be a valid script.
        // -->
        if (attribute.startsWith("asscript")
                || attribute.startsWith("as_script"))
            return HandleNull(element, dScript.valueOf(element), "dScript").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_duration>
        // @returns Duration
        // @group conversion
        // @description
        // Returns the element as a duration.
        // -->
        if (attribute.startsWith("asduration")
                || attribute.startsWith("as_duration"))
            return HandleNull(element, Duration.valueOf(element), "Duration").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_world>
        // @returns dWorld
        // @group conversion
        // @description
        // Returns the element as a world.
        // -->
        if (attribute.startsWith("asworld")
                || attribute.startsWith("as_world"))
            return HandleNull(element, dWorld.valueOf(element), "dWorld").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_queue>
        // @returns dQueue
        // @group conversion
        // @description
        // Returns the element as a queue.
        // -->
        if (attribute.startsWith("asqueue")
                || attribute.startsWith("as_queue"))
            return HandleNull(element, ScriptQueue.valueOf(element), "dQueue").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.escaped>
        // @returns Element
        // @group conversion
        // @description
        // Returns the element, escaped for safe reuse.
        // Inverts <@link tag el@element.unescaped>
        // See <@link language property escaping>
        // -->
        if (attribute.startsWith("escaped"))
            return new Element(EscapeTags.Escape(element)).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.unescaped>
        // @returns Element
        // @group conversion
        // @description
        // Returns the element, unescaped.
        // Inverts <@link tag el@element.escaped>
        // See <@link language property escaping>
        // -->
        if (attribute.startsWith("unescaped"))
            return new Element(EscapeTags.unEscape(element)).getAttribute(attribute.fulfill(1));


        /////////////////////
        //   DEBUG ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <el@element.debug.log>
        // @returns Element
        // @group debug
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
        // @group debug
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
        // @group debug
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
        // @group debug
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
        // @attribute <el@element.contains[<element>]>
        // @returns Element(Boolean)
        // @group string checking
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
        // @attribute <el@element.ends_with[<element>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element ends with a specified string.
        // -->
        if (attribute.startsWith("ends_with") || attribute.startsWith("endswith"))
            return new Element(element.endsWith(attribute.getContext(1))).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.matches[<regex>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element matches a regex input.
        // -->
        // TODO: .group[#] and such
        if (attribute.startsWith("matches")
                && attribute.hasContext(1))
            return new Element(element.matches(attribute.getContext(1))).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.last_color>
        // @returns Element
        // @group string checking
        // @description
        // Returns the ChatColors used at the end of a string.
        // -->
        if (attribute.startsWith("last_color"))
            return new Element(ChatColor.getLastColors(element)).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.length>
        // @returns Element(Number)
        // @group string checking
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
        // @group string checking
        // @description
        // Returns whether the element starts with a specified string.
        // -->
        if (attribute.startsWith("starts_with") || attribute.startsWith("startswith"))
            return new Element(element.startsWith(attribute.getContext(1))).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.index_of[<string>]>
        // @returns Element(Number)
        // @group string checking
        // @description
        // Returns the index of the first occurrence of a specified string.
        // Returns -1 if the string never occurs within the element.
        // -->
        if (attribute.startsWith("index_of")
                && attribute.hasContext(1)) {
            return new Element(element.indexOf(attribute.getContext(1)) + 1)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.last_index_of[<string>]>
        // @returns Element(Number)
        // @group string checking
        // @description
        // Returns the index of the last occurrence of a specified string.
        // Returns -1 if the string never occurs within the element.
        // -->
        if (attribute.startsWith("last_index_of")
                && attribute.hasContext(1)) {
            return new Element(element.lastIndexOf(attribute.getContext(1)) + 1)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.char_at[<#>]>
        // @returns Element
        // @group string checking
        // @description
        // Returns the character at a specified index.
        // Returns null if the index is outside the range of the element.
        // -->
        if (attribute.startsWith("char_at")
                && attribute.hasContext(1)) {
            int index = attribute.getIntContext(1) - 1;
            if (index < 0 || index >= element.length())
                return Element.NULL.getAttribute(attribute.fulfill(1));
            else
                return new Element(String.valueOf(element.charAt(index)))
                        .getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   STRING MANIPULATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <el@element.after[<string>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the portion of an element after a specified string. ie. <el@helloWorld.after[hello]> returns 'World'.
        // -->
        if (attribute.startsWith("after")
                && attribute.hasContext(1)) {
            String delimiter = attribute.getContext(1);
            if (element.contains(delimiter))
                return new Element(element.substring
                    (element.indexOf(delimiter) + delimiter.length()))
                    .getAttribute(attribute.fulfill(1));
            else
                return new Element("")
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.before[<string>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the portion of an element before a specified string.
        // -->
        if (attribute.startsWith("before")
                && attribute.hasContext(1)) {
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
        // @group string manipulation
        // @description
        // Returns the element with all instances of a string removed.
        // -->

        // <--[tag]
        // @attribute <el@element.replace[<string>].with[<string>]>
        // @returns Element
        // @group string manipulation
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
        // @group string manipulation
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
        // @group string manipulation
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
        // @attribute <el@element.format[<script>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the text re-formatted according to a format script.
        // See <@link tutorial using format scripts>.
        // -->
        if (attribute.startsWith("format")
                && attribute.hasContext(1)) {
            FormatScriptContainer format = ScriptRegistry.getScriptContainerAs(attribute.getContext(1), FormatScriptContainer.class);
            if (format == null) {
                dB.echoError("Could not find format script matching '" + attribute.getContext(1) + "'");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            else {
                return new Element(format.getFormattedText(element,
                        attribute.getScriptEntry() != null ? attribute.getScriptEntry().getNPC(): null,
                        attribute.getScriptEntry() != null ? attribute.getScriptEntry().getPlayer(): null))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <el@element.strip_color>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the element with all color encoding stripped.
        // -->
        if (attribute.startsWith("strip_color"))
            return new Element(ChatColor.stripColor(element)).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.trim>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the value of an element minus any leading or trailing whitespace.
        // -->
        if (attribute.startsWith("trim"))
            return new Element(element.trim()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.to_uppercase>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the value of an element in all uppercase letters.
        // -->
        if (attribute.startsWith("to_uppercase") || attribute.startsWith("upper"))
            return new Element(element.toUpperCase()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.to_lowercase>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the value of an element in all lowercase letters.
        // -->
        if (attribute.startsWith("to_lowercase") || attribute.startsWith("lower"))
            return new Element(element.toLowerCase()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.to_titlecase>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns The Value Of An Element In Title Case.
        // -->
        if (attribute.startsWith("to_titlecase") || attribute.startsWith("totitlecase")) {
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
        // @group string manipulation
        // @description
        // Returns the portion of an element between two string indices.
        // If no second index is specified, it will return the portion of an
        // element after the specified index.
        // -->
        if (attribute.startsWith("substring")||attribute.startsWith("substr")) {            // substring[2,8]
            int beginning_index = new Element(attribute.getContext(1).split(",")[0]).asInt() - 1;
            int ending_index;
            if (attribute.getContext(1).split(",").length > 1)
                ending_index = new Element(attribute.getContext(1).split(",")[1]).asInt();
            else
                ending_index = element.length();
            if (beginning_index < 0) beginning_index = 0;
            if (beginning_index > element.length()) beginning_index = element.length();
            if (ending_index > element.length()) ending_index = element.length();
            if (ending_index < beginning_index) ending_index = beginning_index;
            return new Element(element.substring(beginning_index, ending_index))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.pad_left[<#>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the value of an element extended to reach a minimum specified length
        // by adding spaces to the left side.
        // -->
        if (attribute.startsWith("pad_left")
                && attribute.hasContext(1)) {
            String with = String.valueOf((char)0x00A0);
            int length = attribute.getIntContext(1);
            attribute = attribute.fulfill(1);
            // <--[tag]
            // @attribute <el@element.pad_left[<#>].with[<element>]>
            // @returns Element
            // @group string manipulation
            // @description
            // Returns the value of an element extended to reach a minimum specified length
            // by adding a specific symbol to the left side.
            // -->
            if (attribute.startsWith("with")
                    && attribute.hasContext(1)) {
                with = String.valueOf(attribute.getContext(1).charAt(0));
                attribute = attribute.fulfill(1);
            }
            String padded = element;
            while (padded.length() < length) {
                padded = with + padded;
            }
            return new Element(padded).getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <el@element.pad_right[<#>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the value of an element extended to reach a minimum specified length
        // by adding spaces to the right side.
        // -->
        if (attribute.startsWith("pad_right")
                && attribute.hasContext(1)) {
            String with = String.valueOf((char)0x00A0);
            int length = attribute.getIntContext(1);
            attribute = attribute.fulfill(1);
            // <--[tag]
            // @attribute <el@element.pad_right[<#>].with[<element>]>
            // @returns Element
            // @group string manipulation
            // @description
            // Returns the value of an element extended to reach a minimum specified length
            // by adding a specific symbol to the right side.
            // -->
            if (attribute.startsWith("with")
                    && attribute.hasContext(1)) {
                with = String.valueOf(attribute.getContext(1).charAt(0));
                attribute = attribute.fulfill(1);
            }
            StringBuilder padded = new StringBuilder(element);
            while (padded.length() < length) {
                padded.append(with);
            }
            return new Element(padded.toString()).getAttribute(attribute);
        }


        /////////////////////
        //   MATH ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <el@element.abs>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the absolute value of the element.
        // -->
        if (attribute.startsWith("abs")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.abs(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.add[<#>]>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the element plus a number.
        // -->
        if (attribute.startsWith("add")
                && attribute.hasContext(1)) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(asDouble() + aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.div[<#>]>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the element divided by a number.
        // -->
        if (attribute.startsWith("div")
                && attribute.hasContext(1)) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(asDouble() / aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.mod[<#>]>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the remainder of the element divided by a number.
        // -->
        if (attribute.startsWith("mod")
                && attribute.hasContext(1)) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(asDouble() % aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.mul[<#>]>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the element multiplied by a number.
        // -->
        if (attribute.startsWith("mul")
                && attribute.hasContext(1)) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(asDouble() * aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.sqrt>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the square root of the element.
        // -->
        if (attribute.startsWith("sqrt")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.sqrt(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.sub[<#>]>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the element minus a number.
        // -->
        if (attribute.startsWith("sub")
                && attribute.hasContext(1)) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(asDouble() - aH.getDoubleFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        // <--[tag]
        // @attribute <el@element.power[<#>]>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the element to the power of a number.
        // -->
        if (attribute.startsWith("power")
                && attribute.hasContext(1)) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.pow(asDouble(), aH.getDoubleFrom(attribute.getContext(1))))
                    .getAttribute(attribute.fulfill(1));
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        // <--[tag]
        // @attribute <el@element.asin>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the arc-sine of the element.
        // -->
        if (attribute.startsWith("asin")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.asin(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.acos>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the arc-cosine of the element.
        // -->
        if (attribute.startsWith("acos")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.acos(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.atan>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the arc-tangent of the element.
        // -->
        if (attribute.startsWith("atan")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.atan(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.cos>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the cosine of the element.
        // -->
        if (attribute.startsWith("cos")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.cos(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.sin>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the sine of the element.
        // -->
        if (attribute.startsWith("sin")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.sin(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.tan>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the tangent of the element.
        // -->
        if (attribute.startsWith("tan")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.tan(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.to_degrees>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Converts the element from radians to degrees.
        // -->
        if (attribute.startsWith("to_degrees")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.toDegrees(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.to_radians>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Converts the element from degrees to radians.
        // -->
        if (attribute.startsWith("to_radians")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(Math.toRadians(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.round_up>
        // @returns Element(Number)
        // @group math
        // @description
        // Rounds a decimal upward.
        // -->
        if (attribute.startsWith("round_up")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element((int)Math.ceil(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.round_down>
        // @returns Element(Number)
        // @group math
        // @description
        // Rounds a decimal downward.
        // -->
        if (attribute.startsWith("round_down")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element((int)Math.floor(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.round>
        // @returns Element(Number)
        // @group math
        // @description
        // Rounds a decimal.
        // -->
        if (attribute.startsWith("round")) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element((int)Math.round(asDouble()))
                    .getAttribute(attribute.fulfill(1));
        }

        // Unfilled attributes past this point probably means the tag is spelled
        // incorrectly. So instead of just passing through what's been resolved
        // so far, 'null' shall be returned with a debug message.

        if (attribute.attributes.size() > 0) {
            dB.echoDebug(attribute.getScriptEntry(), "Unfilled attributes '" + attribute.attributes.toString() +
                    "' for tag <" + attribute.getOrigin() + ">!");
            return "null";

        } else {
            return element;
        }
    }

}
