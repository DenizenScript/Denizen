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
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.tags.core.EscapeTags;
import net.aufdemrand.denizen.utilities.SQLEscaper;
import net.aufdemrand.denizen.utilities.debugging.dB;

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
            this.element = TagManager.cleanOutputFully(string);
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
        this.element = TagManager.cleanOutputFully(string);
    }

    public double asDouble() {
        return Double.valueOf(element.replaceAll("%", ""));
    }

    public float asFloat() {
        return Float.valueOf(element.replaceAll("%", ""));
    }

    public int asInt() {
        try {
            return Integer.valueOf(element.replaceAll("(%)|(\\.\\d+)", ""));
        }
        catch (NumberFormatException ex) {
            dB.echoError("'" + element + "' is not a valid integer!");
            return 0;
        }
    }

    public long asLong() {
        try {
            return Long.valueOf(element.replaceAll("(%)|(\\.\\d+)", ""));
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
            if (Integer.valueOf(element.replaceAll("(%)|(\\.\\d+)", "")) != null)
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
            if (value.name().equalsIgnoreCase(element))
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
                if (!attribute.hasAlternative())
                    dB.echoError("'" + element + "' is not a valid Double.");
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
                if (!attribute.hasAlternative())
                    dB.echoError("'" + element + "' is not a valid Integer.");
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
                if (!attribute.hasAlternative())
                    dB.echoError("'" + element + "' is not a valid number.");
            }
        }

        // <--[tag]
        // @attribute <el@element.as_chunk>
        // @returns dCuboid
        // @group conversion
        // @description
        // Returns the element as a chunk. Note: the value must be a valid chunk.
        // -->
        if (attribute.startsWith("aschunk")
                || attribute.startsWith("as_chunk"))
            return HandleNull(element, dChunk.valueOf(element), "dChunk").getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.as_color>
        // @returns dCuboid
        // @group conversion
        // @description
        // Returns the element as a dColor. Note: the value must be a valid color.
        // -->
        if (attribute.startsWith("ascolor")
                || attribute.startsWith("as_color"))
            return HandleNull(element, dColor.valueOf(element), "dColor").getAttribute(attribute.fulfill(1));

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
        // @attribute <el@element.sql_escaped>
        // @returns Element
        // @group conversion
        // @description
        // Returns the element, escaped for safe use in SQL.
        // -->
        if (attribute.startsWith("sql_escaped"))
            return new Element(SQLEscaper.escapeSQL(element)).getAttribute(attribute.fulfill(1));

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
        // @attribute <el@element.contains_any_case_sensitive_text[<element>|...]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element contains any of a list of specified strings, case sensitive.
        // -->
        // <--[tag]
        // @attribute <el@element.contains_any_case_sensitive[<element>|...]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element contains any of a list of specified strings, case sensitive.
        // -->
        if (attribute.startsWith("contains_any_case_sensitive")) {
            dList list = dList.valueOf(attribute.getContext(1));
            for (String list_element: list) {
                if (element.contains(list_element)) {
                    return Element.TRUE.getAttribute(attribute.fulfill(1));
                }
            }
            return Element.FALSE.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.contains_any_text[<element>|...]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element contains any of a list of specified strings, case insensitive.
        // -->

        // <--[tag]
        // @attribute <el@element.contains_any[<element>|...]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element contains any of a list of specified strings, case insensitive.
        // -->
        if (attribute.startsWith("contains_any")) {
            dList list = dList.valueOf(attribute.getContext(1));
            String ellow = element.toLowerCase();
            for (String list_element: list) {
                if (ellow.contains(list_element.toLowerCase())) {
                    return Element.TRUE.getAttribute(attribute.fulfill(1));
                }
            }
            return Element.FALSE.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.contains_case_sensitive_text[<element>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element contains a specified string, case sensitive.
        // -->

        // <--[tag]
        // @attribute <el@element.contains_case_sensitive[<element>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element contains a specified string, case sensitive.
        // -->
        if (attribute.startsWith("contains_case_sensitive")) {
            String contains = attribute.getContext(1);
            if (element.contains(contains))
                return new Element("true").getAttribute(attribute.fulfill(1));
            else return new Element("false").getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.contains_text[<element>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element contains a specified string, case insensitive. Can use
        // regular expression by prefixing the string with regex:
        // -->

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
            return new Element(element.toLowerCase().endsWith(attribute.getContext(1).toLowerCase())).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <el@element.equals_case_sensitive[<element>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element matches another element, case-sensitive.
        // -->
        if (attribute.startsWith("equals_case_sensitive")
                && attribute.hasContext(1)) {
            return new Element(element.equals(attribute.getContext(1))).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.matches[<regex>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element matches a regex input.
        // -->
        if (attribute.startsWith("matches")
                && attribute.hasContext(1)) {
            return new Element(element.matches(attribute.getContext(1))).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.regex[<regex>].group[<group>]>
        // @returns Element
        // @group string checking
        // @description
        // Returns the specific group from a regex match.
        // Specify group 0 for the whole match.
        // For example, <el@val[hello5world].regex[.*(\d).*].group[1]> returns '5'.
        // -->
        if (attribute.startsWith("regex")
                && attribute.hasContext(1)
                && attribute.hasContext(2)) {
            String regex = attribute.getContext(1);
            Matcher m = Pattern.compile(regex).matcher(element);
            if (!m.matches()) {
                return Element.NULL.getAttribute(attribute.fulfill(2));
            }
            int group = new Element(attribute.getContext(2)).asInt();
            if (group < 0)
                group = 0;
            if (group > m.groupCount())
                group = m.groupCount();
            return new Element(m.group(group)).getAttribute(attribute.fulfill(2));
        }

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
        // @attribute <el@element.not>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns the opposite of the element
        // IE, true returns false and false returns true.
        // -->
        if (attribute.startsWith("not")) {
            return new Element(!element.equalsIgnoreCase("true"))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.and[<element>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether both the element and the second element are true.
        // -->
        if (attribute.startsWith("and")
                && attribute.hasContext(1)) {
            return new Element(element.equalsIgnoreCase("true") && attribute.getContext(1).equalsIgnoreCase("true"))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.or[<element>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether either the element or the second element are true.
        // -->
        if (attribute.startsWith("or")
                && attribute.hasContext(1)) {
            return new Element(element.equalsIgnoreCase("true") || attribute.getContext(1).equalsIgnoreCase("true"))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.xor[<element>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the element and the second element are true and false (exclusive or).
        // -->
        if (attribute.startsWith("xor")
                && attribute.hasContext(1)) {
            return new Element(element.equalsIgnoreCase("true") != attribute.getContext(1).equalsIgnoreCase("true"))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.equals_with_case[<element>]>
        // @returns Element(Boolean)
        // @group string checking
        // @description
        // Returns whether the two elements exactly match, counting casing.
        // -->
        if (attribute.startsWith("equals_with_case")
                && attribute.hasContext(1)) {
            return new Element(element.equals(attribute.getContext(1)))
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
            return new Element(element.toLowerCase().startsWith(attribute.getContext(1).toLowerCase())).getAttribute(attribute.fulfill(1));

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
            return new Element(element.toLowerCase().indexOf(attribute.getContext(1).toLowerCase()) + 1)
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
            return new Element(element.toLowerCase().lastIndexOf(attribute.getContext(1).toLowerCase()) + 1)
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
        // @attribute <el@element.after_last[<text>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the portion of an element after the last occurrence of a specified string.
        // EG, abcabc .after[b] returns c.
        // -->
        if (attribute.startsWith("after_last")
                && attribute.hasContext(1)) {
            String delimiter = attribute.getContext(1);
            if (element.toLowerCase().contains(delimiter.toLowerCase()))
                return new Element(element.substring
                        (element.toLowerCase().lastIndexOf(delimiter.toLowerCase()) + delimiter.length()))
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element("")
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.after[<text>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the portion of an element after the first occurrence of a specified string.
        // EG, HelloWorld .after[Hello] returns World.
        // -->
        if (attribute.startsWith("after")
                && attribute.hasContext(1)) {
            String delimiter = attribute.getContext(1);
            if (element.toLowerCase().contains(delimiter.toLowerCase()))
                return new Element(element.substring
                    (element.toLowerCase().indexOf(delimiter.toLowerCase()) + delimiter.length()))
                    .getAttribute(attribute.fulfill(1));
            else
                return new Element("")
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.before_last[<text>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the portion of an element before the last occurrence of a specified string.
        // EG, abcabc .before[b] returns abca.
        // -->
        if (attribute.startsWith("before_last")
                && attribute.hasContext(1)) {
            String delimiter = attribute.getContext(1);
            if (element.toLowerCase().contains(delimiter.toLowerCase()))
                return new Element(element.substring
                        (0, element.toLowerCase().lastIndexOf(delimiter.toLowerCase())))
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element(element)
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.before[<text>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the portion of an element before the first occurrence of specified string.
        // EG, abcd .before[c] returns ab.
        // -->
        if (attribute.startsWith("before")
                && attribute.hasContext(1)) {
            String delimiter = attribute.getContext(1);
            if (element.toLowerCase().contains(delimiter.toLowerCase()))
                return new Element(element.substring
                    (0, element.toLowerCase().indexOf(delimiter.toLowerCase())))
                    .getAttribute(attribute.fulfill(1));
            else
                return new Element(element)
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.replace[((first)regex:)<string>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the element with all instances of a string removed.
        // -->

        // <--[tag]
        // @attribute <el@element.replace[((first)regex:)<string>].with[<string>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the element with all instances of a string replaced with another.
        // Specify regex: at the start of the replace string to use Regex replacement.
        // Specify firstregex: at the start of the replace string to Regex 'replaceFirst'
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

            if (replace.startsWith("regex:"))
                return new Element(element.replaceAll(replace.substring("regex:".length()), replacement))
                        .getAttribute(attribute);
            if (replace.startsWith("firstregex:"))
                return new Element(element.replaceFirst(replace.substring("firstregex:".length()), replacement))
                        .getAttribute(attribute);
            else
                return new Element(element.replaceAll("(?i)" + Pattern.quote(replace), replacement))
                        .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <el@element.split[(regex:)<string>].limit[<#>]>
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
                return new dList(Arrays.asList(element.split("(?i)" + Pattern.quote(split_string), limit)))
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.split[(regex:)<string>]>
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
                return new dList(Arrays.asList(element.split("(?i)" + Pattern.quote(split_string))))
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.format_number>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns a number reformatted for easier reading.
        // EG, 1234567 will become 1,234,567.
        // -->
        if (attribute.startsWith("format_number")) {
            try {
                int decimal = element.indexOf('.');
                String shortelement;
                String afterdecimal;
                if (decimal != -1) {
                    shortelement = element.substring(0, decimal);
                    afterdecimal = element.substring(decimal);
                }
                else {
                    shortelement = element;
                    afterdecimal = "";
                }
                String intform = Long.valueOf(shortelement.replace("%", "")).toString();
                String negative = "";
                if (intform.startsWith("-")) {
                    negative = "-";
                    intform = intform.substring(1, intform.length());
                }
                for (int i = intform.length() - 3; i > 0; i -= 3) {
                    intform = intform.substring(0, i) + "," + intform.substring(i, intform.length());
                }
                return new Element(negative + intform + afterdecimal).getAttribute(attribute.fulfill(1));
            }
            catch (Exception ex) {
                dB.echoError(ex);
            }
        }

        // <--[tag]
        // @attribute <el@element.format[<script>]>
        // @returns Element
        // @group string manipulation
        // @description
        // Returns the text re-formatted according to a format script.
        // See <@link example using format scripts>.
        // -->
        if (attribute.startsWith("format")
                && attribute.hasContext(1)) {
            FormatScriptContainer format = ScriptRegistry.getScriptContainer(attribute.getContext(1));
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
        // @attribute <el@element.to_list>
        // @returns dList
        // @group string manipulation
        // @description
        // Returns a dList of each letter in the element.
        // -->
        if (attribute.startsWith("to_list")) {
            dList list = new dList();
            for (int i = 0; i < element.length(); i++) {
                list.add(String.valueOf(element.charAt(i)));
            }
            return list.getAttribute(attribute.fulfill(1));
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
        // @attribute <el@element.add_int[<#>]>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the element plus a number, using integer math.
        // -->
        if (attribute.startsWith("add_int")
                && attribute.hasContext(1)) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(asLong() + aH.getLongFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.div[<#>]>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the element divided by a number.
        // -->
        if (attribute.startsWith("div_int")
                && attribute.hasContext(1)) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(asLong() / aH.getLongFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.mul_int[<#>]>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the element multiplied by a number.
        // -->
        if (attribute.startsWith("mul_int")
                && attribute.hasContext(1)) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(asLong() * aH.getLongFrom(attribute.getContext(1)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <el@element.sub_int[<#>]>
        // @returns Element(Decimal)
        // @group math
        // @description
        // Returns the element minus a number.
        // -->
        if (attribute.startsWith("sub_int")
                && attribute.hasContext(1)) {
            if (!isDouble()) {
                dB.echoError("Element '" + element + "' is not a valid decimal number!");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
            return new Element(asLong() - aH.getLongFrom(attribute.getContext(1)))
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
            if (!attribute.hasAlternative())
                dB.echoDebug(attribute.getScriptEntry(), "Unfilled attributes '" + attribute.attributes.toString() +
                        "' for tag <" + attribute.getOrigin() + ">!");
            return "null";

        } else {
            return element;
        }
    }
}
