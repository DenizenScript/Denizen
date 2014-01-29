package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dList extends ArrayList<String> implements dObject {

    final static Pattern flag_by_id =
            Pattern.compile("(fl\\[((?:p@|n@)(.+?))\\]@|fl@)(.+)",
                    Pattern.CASE_INSENSITIVE);

    public final static char internal_escape_char = (char)0x05;
    public final static String internal_escape = String.valueOf(internal_escape_char);
    final static Pattern identifier = Pattern.compile("li@", Pattern.CASE_INSENSITIVE);

    @Fetchable("li, fl")
    public static dList valueOf(String string) {
        if (string == null) return null;

        ///////
        // Match @object format

        // Make sure string matches what this interpreter can accept.
        Matcher m;
        m = flag_by_id.matcher(string);

        if (m.matches()) {
            FlagManager flag_manager = DenizenAPI.getCurrentInstance().flagManager();

            try {
                // Global
                if (m.group(1).equalsIgnoreCase("fl@")) {
                    if (FlagManager.serverHasFlag(m.group(4)))
                        return new dList(flag_manager.getGlobalFlag(m.group(4)));

                } else if (m.group(2).toLowerCase().startsWith("p@")) {
                    if (FlagManager.playerHasFlag(dPlayer.valueOf(m.group(3)), m.group(4)))
                        return new dList(flag_manager.getPlayerFlag(m.group(3), m.group(4)));

                } else if (m.group(2).toLowerCase().startsWith("n@")) {
                    if (FlagManager.npcHasFlag(dNPC.valueOf(m.group(3)), m.group(4)))
                        return new dList(flag_manager.getNPCFlag(Integer.valueOf(m.group(3)), m.group(4)));
                }

            } catch (Exception e) {
                dB.echoError("Flag '" + m.group() + "' could not be found!");
                return null;
            }
        }

        // Use value of string, which will separate values by the use of a pipe '|'
        return new dList(string.replaceFirst(identifier.pattern(), ""));
    }


    public static boolean matches(String arg) {

        Matcher m;
        m = flag_by_id.matcher(arg);

        return m.matches() || arg.contains("|") || arg.contains(internal_escape) || arg.toLowerCase().startsWith("li@");
    }


    /////////////
    //   Constructors
    //////////

    // A list of dObjects
    public dList(ArrayList<? extends dObject> dObjectList) {
        for (dObject obj : dObjectList)
            add(obj.identify());
    }

    // Empty dList
    public dList() { }

    // A string of items, split by '|'
    public dList(String items) {
        if (items != null && items.length() > 0) {
            // Count brackets
            int brackets = 0;
            // Record start position
            int start = 0;
            // Loop through characters
            for (int i = 0; i < items.length(); i++) {
                char chr = items.charAt(i);
                // Count brackets
                if (chr == '[') {
                    brackets++;
                }
                else if (chr == ']') {
                    if (brackets > 0) brackets--;
                }
                // Separate if an un-bracketed pipe is found
                else if ((brackets == 0) && (chr == '|' || chr == internal_escape_char)) {
                    add(items.substring(start, i));
                    start = i + 1;
                }
            }
            // If there is an item waiting, add it too
            if (start < items.length()) {
                add(items.substring(start, items.length()));
            }
        }
    }

    // A List<String> of items
    public dList(List<String> items) {
        if (items != null) addAll(items);
    }

    // A List<String> of items, with a prefix
    public dList(List<String> items, String prefix) {
        for (String element : items) {
            add(prefix + element);
        }
    }

    // A Flag
    public dList(FlagManager.Flag flag) {
        this.flag = flag;
        addAll(flag.values());
    }


    /////////////
    //   Instance Fields/Methods
    //////////

    private FlagManager.Flag flag = null;


    /**
     * Adds a list of dObjects to a dList by forcing each to 'identify()'.
     *
     * @param dObjects the List of dObjects
     * @return a dList
     */
    public dList addObjects(List<dObject> dObjects) {
        for (dObject obj : dObjects) {
            add(obj.identify());
        }

        return this;
    }


    /**
     * Fetches a String Array copy of the dList.
     *
     * @return the array copy
     */
    public String[] toArray() {
        List<String> list = new ArrayList<String>();

        for (String string : this) {
            list.add(string);
        }

        return list.toArray(new String[list.size()]);
    }


    // Returns if the list contains objects from the specified dClass
    // by using the matches() method.
    public boolean containsObjectsFrom(Class<? extends dObject> dClass) {
        List<dObject> results = new ArrayList<dObject>();

        // Iterate through elements until one matches() the dClass
        for (String element : this)
            if (ObjectFetcher.checkMatch(dClass, element))
                return true;

        return false;
    }


    /**
     *  Return a new list that includes only strings that match the values of an Enum array
     *
     * @param values  the Enum's value
     * @return  a filtered list
     */
    public List<String> filter(Enum[] values) {
        List<String> list = new ArrayList<String>();

        for (String string : this) {
            for (Enum value : values)
                if (value.name().equalsIgnoreCase(string))
                    list.add(string);
        }

        if (!list.isEmpty())
            return list;
        else return null;
    }


    // Return a list that includes only elements belonging to a certain class
    public <T extends dObject> List<T> filter(Class<T> dClass) {
        return filter(dClass, null);
    }


    public <T extends dObject> List<T> filter(Class<T> dClass, ScriptEntry entry) {
        List<T> results = new ArrayList<T>();

        for (String element : this) {

            try {
                if (ObjectFetcher.checkMatch(dClass, element)) {

                    T object = ObjectFetcher.getObjectFrom(dClass, element,
                            (entry != null ? entry.getPlayer(): null),
                            (entry != null ? entry.getNPC(): null));

                    // Only add the object if it is not null, thus filtering useless
                    // list items

                    if (object != null) {
                        results.add(object);
                    }
                }
            } catch (Exception e) {
                dB.echoError(e);
            }
        }

        if (results.size() > 0) return results;
        else return null;
    }


    @Override
    public String toString() {
        return identify();
    }




    //////////////////////////////
    //    DSCRIPT ARGUMENT METHODS
    /////////////////////////


    private String prefix = "List";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public dList setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debug() {
        return "<G>" + prefix + "='<Y>" + identify() + "<G>'  ";
    }

    @Override
    public boolean isUnique() {
        return flag != null;
    }

    @Override
    public String getObjectType() {
        return "List";
    }

    @Override
    public String identify() {
        if (flag != null)
            return flag.toString();

        if (isEmpty()) return "li@";

        StringBuilder dScriptArg = new StringBuilder();
        dScriptArg.append("li@");
        for (String item : this) {
            dScriptArg.append(item);
            // Items are separated by the | character in dLists
            dScriptArg.append('|');
        }

        return dScriptArg.toString().substring(0, dScriptArg.length() - 1);
    }


    @Override
    public String identifySimple() {
        return identify();
    }


    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        // <--[tag]
        // @attribute <li@list.get_sub_items[#]>
        // @returns dList
        // @description
        // returns a list of the specified sub items in the list, as split by the
        // forward-slash character (/).
        // -->

        if (attribute.startsWith("get_sub_items")) {
            int index = -1;
            if (aH.matchesInteger(attribute.getContext(1)))
                index = attribute.getIntContext(1) - 1;
            attribute.fulfill(1);


            // <--[tag]
            // @attribute <li@list.get_sub_items[#].split_by[<element>]>
            // @returns dList
            // @description
            // returns a list of the specified sub item in the list, allowing you to specify a
            // character in which to split the sub items by. WARNING: When setting your own split
            // character, make note that it is CASE SENSITIVE.
            // -->

            char split = '/';
            if (attribute.startsWith("split_by")) {
                if (attribute.hasContext(1) && attribute.getContext(1).length() > 0)
                    split = attribute.getContext(1).charAt(0);
                attribute.fulfill(1);
            }

            if (index < 0)
                return Element.NULL.getAttribute(attribute);

            dList sub_list = new dList();

            for (String item : this) {
                String[] strings = StringUtils.split(item, split);
                if (strings.length >= index)
                    sub_list.add(strings[index]);
                else sub_list.add("null");
            }

            return sub_list.getAttribute(attribute);
        }


        // <--[tag]
        // @attribute <li@list.as_cslist>
        // @returns Element
        // @description
        // returns the list in a cleaner format, separated by commas.
        // -->
        if (attribute.startsWith("ascslist")
                || attribute.startsWith("as_cslist")) {
            if (isEmpty()) return new Element("").getAttribute(attribute.fulfill(1));
            StringBuilder dScriptArg = new StringBuilder();
            for (String item : this) {
                dScriptArg.append(item);
                // Insert a comma and space after each item
                dScriptArg.append(", ");
            }
            return new Element(dScriptArg.toString().substring(0, dScriptArg.length() - 2))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.formatted>
        // @returns Element
        // @description
        // returns the list in a human-readable format.
        // EG: li@n@3|p@bob|potato returns 'GuardNPC, bob, and potato'
        // -->
        if (attribute.startsWith("formatted")) {
            if (isEmpty()) return new Element("").getAttribute(attribute.fulfill(1));
            StringBuilder dScriptArg = new StringBuilder();

            for (int n = 0; n < this.size(); n++) {

                if (dNPC.matches(get(n))) {
                    dNPC gotten = dNPC.valueOf(get(n));
                    if (gotten != null) {
                        dScriptArg.append(gotten.getName());
                    }
                    else {
                        dScriptArg.append(get(n).replaceAll("\\w\\w?@", ""));
                    }
                }
                else {
                    dScriptArg.append(get(n).replaceAll("\\w\\w?@", ""));
                }

                if (n == this.size() - 2) {
                    dScriptArg.append(n == 0 ? " and ": ", and ");
                }
                else {
                    dScriptArg.append(", ");
                }
            }

            return new Element(dScriptArg.toString().substring(0, dScriptArg.length() - 2))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.size>
        // @returns Element(Number)
        // @description
        // returns the size of the list.
        // -->
        if (attribute.startsWith("size"))
            return new Element(size()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <li@list.is_empty>
        // @returns Element(Boolean)
        // @description
        // returns whether the list is empty.
        // -->
        if (attribute.startsWith("is_empty"))
            return new Element(isEmpty()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <li@list.as_string>
        // @returns Element
        // @description
        // returns each item in the list as a single 'String'.
        // -->
        if (attribute.startsWith("asstring")
                || attribute.startsWith("as_string")) {
            if (isEmpty()) return new Element("").getAttribute(attribute.fulfill(1));
            StringBuilder dScriptArg = new StringBuilder();
            for (String item : this) {
                dScriptArg.append(item);
                // Insert space between items.
                dScriptArg.append(' ');
            }
            return new Element(dScriptArg.toString().substring(0, dScriptArg.length() - 1))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.exclude[...|...]>
        // @returns dList
        // @description
        // returns a new dList excluding the items specified.
        // -->
        if (attribute.startsWith("exclude") &&
                attribute.hasContext(1)) {
            dList exclusions = new dList(attribute.getContext(1));
            // Create a new dList that will contain the exclusions
            dList list = new dList(this);
            // Iterate through
            for (String exclusion : exclusions)
                for (int i = 0;i < list.size();i++)
                    if (list.get(i).equalsIgnoreCase(exclusion))
                        list.remove(i--);

            // Return the modified list
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.get[#]>
        // @returns Element
        // @description
        // returns an element of the value specified by the supplied context.
        // -->
        if (attribute.startsWith("get") &&
                attribute.hasContext(1)) {
            if (isEmpty()) return "null";
            int index = attribute.getIntContext(1);
            if (index > size()) return "null";
            if (index < 1) index = 1;
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <li@list.get[#].to[#]>
            // @returns dList
            // @description
            // returns all elements in the range from the first index to the second.
            // -->
            if (attribute.startsWith("to") &&
                    attribute.hasContext(1)) {
                int index2 = attribute.getIntContext(1);
                if (index2 > size()) index2 = size();
                if (index2 < 1) index2 = 1;
                String item = "";
                for (int i = index; i <= index2; i++) {
                    item += get(i - 1) + (i < index2 ? "|": "");
                }
                return new dList(item).getAttribute(attribute.fulfill(1));
            }
            else {
                String item;
                item = get(index - 1);
                return new Element(item).getAttribute(attribute);
            }
        }

        // <--[tag]
        // @attribute <li@list.find[<element>]>
        // @returns Element(Number)
        // @description
        // returns the numbered location of an element within a list,
        // or -1 if the list does not contain that item.
        // -->
        if (attribute.startsWith("find") &&
                attribute.hasContext(1)) {
            for (int i = 0; i < size(); i++) {
                if (get(i).equalsIgnoreCase(attribute.getContext(1)))
                    return new Element(i + 1).getAttribute(attribute.fulfill(1));
            }
            for (int i = 0; i < size(); i++) {
                if (get(i).toUpperCase().contains(attribute.getContext(1).toUpperCase()))
                    return new Element(i + 1).getAttribute(attribute.fulfill(1));
            }
            return new Element(-1).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.last>
        // @returns Element
        // @description
        // returns the last element in the list.
        // -->
        if (attribute.startsWith("last")) {
            return new Element(get(size() - 1)).getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("contains")) {
            if (attribute.hasContext(1)) {
                boolean state = false;

                for (String element : this) {
                    if (element.equalsIgnoreCase(attribute.getContext(1))) {
                        state = true;
                        break;
                    }
                }

                return new Element(state).getAttribute(attribute.fulfill(1));
            }
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

        if (attribute.startsWith("identify")) {
            return new Element(identify())
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("type")) {
            return new Element(getObjectType())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.random[<#>]>
        // @returns Element
        // @description
        // Gets a random item in the list and returns it as an Element.
        // Optionally, add [#] to get a list of multiple randomly chosen elements.
        // -->
        if (attribute.startsWith("random")) {
            if (!this.isEmpty()) {
                if (attribute.hasContext(1)) {
                    int count = Integer.valueOf(attribute.getContext(1));
                    int times = 0;
                    ArrayList<String> available = new ArrayList<String>();
                    available.addAll(this);
                    dList toReturn = new dList();
                    while (!available.isEmpty() && times < count) {
                        int random = Utilities.getRandom().nextInt(available.size());
                        toReturn.add(available.get(random));
                        available.remove(random);
                        times++;
                    }
                    return toReturn.getAttribute(attribute.fulfill(1));
                }
                else {
                    return new Element(this.get(Utilities.getRandom().nextInt(this.size())))
                            .getAttribute(attribute.fulfill(1));
                }
            }
        }

        // FLAG Specific Attributes

        // Note: is_expired attribute is handled in player/npc/server
        // since expired flags return 'null'

        // <--[tag]
        // @attribute <fl@flag_name.is_expired>
        // @returns Element(Boolean)
        // @description
        // returns true of the flag is expired or does not exist, false if it
        // is not yet expired, or has no expiration.
        // -->

        // <--[tag]
        // @attribute <fl@flag_name.expiration>
        // @returns Duration
        // @description
        // returns a Duration of the time remaining on the flag, if it
        // has an expiration.
        // -->
        if (flag != null && attribute.startsWith("expiration")) {
            return flag.expiration()
                    .getAttribute(attribute.fulfill(1));
        }

        // Need this attribute (for flags) since they return the last
        // element of the list, unless '.as_list' is specified.

        // <--[tag]
        // @attribute <fl@flag_name.as_list>
        // @returns dList
        // @description
        // returns a dList containing the items in the flag.
        // -->
        if (flag != null && (attribute.startsWith("as_list")
                || attribute.startsWith("aslist")))
            return new dList(this).getAttribute(attribute.fulfill(1));


        // If this is a flag, return the last element (this is how it has always worked...)
        // Use as_list to return a list representation of the flag.
        // If this is NOT a flag, but instead a normal dList, return an element
        // with dList's identify() value.

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return (flag != null
                ? new Element(flag.getLast().asString()).getAttribute(attribute)
                : new Element(identify()).getAttribute(attribute));
    }



}
