package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.containers.core.ProcedureScriptContainer;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.core.EscapeTags;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.NaturalOrderComparator;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.regex.Pattern;

public class dList extends ArrayList<String> implements dObject {

    public final static char internal_escape_char = (char)0x05;
    public final static String internal_escape = String.valueOf(internal_escape_char);

    @Fetchable("li, fl")
    public static dList valueOf(String string) {
        if (string == null) return null;

        ///////
        // Match @object format

        if (string.startsWith("fl")) {
            FlagManager flag_manager = DenizenAPI.getCurrentInstance().flagManager();
            if (string.indexOf('[') == 3) {
                int cb = string.indexOf(']');
                if (cb > 4 && string.indexOf('@') == (cb + 1)) {
                    String owner = string.substring(3, cb - 1);
                    String flag = string.substring(cb + 1);
                    if (dPlayer.matches(owner)) {
                        dPlayer player = dPlayer.valueOf(owner);
                        if (FlagManager.playerHasFlag(player, flag))
                            return new dList(flag_manager.getPlayerFlag(player, flag));
                        else
                            dB.echoError("Player '" + owner + "' flag '" + flag + "' not found.");
                    }
                    else if (Depends.citizens != null && dNPC.matches(owner)) {
                        dNPC npc = dNPC.valueOf(owner);
                        if (FlagManager.npcHasFlag(npc, flag))
                            return new dList(flag_manager.getNPCFlag(npc.getId(), flag));
                        else
                            dB.echoError("NPC '" + owner + "' flag '" + flag + "' not found.");
                    }
                }
                else
                    dB.echoError("Invalid dFlag format: " + string);
            }
            else if (string.indexOf('@') == 3) {
                String flag = string.substring(3);
                if (FlagManager.serverHasFlag(flag))
                    return new dList(flag_manager.getGlobalFlag(flag));
                else
                    dB.echoError("Global flag '" + flag + "' not found.");
            }
            return null;
        }

        // Use value of string, which will separate values by the use of a pipe '|'
        return new dList(string.startsWith("li@") ? string.substring(3) : string);
    }


    public static boolean matches(String arg) {

        boolean flag = false;

        if (arg.startsWith("fl")) {
            if (arg.indexOf('[') == 3) {
                int cb = arg.indexOf(']');
                if (cb > 4 && arg.indexOf('@') == (cb + 1)) {
                    String owner = arg.substring(3, cb - 1);
                    flag = arg.substring(cb + 1).length() > 0 && (dPlayer.matches(owner)
                            || (Depends.citizens != null && dNPC.matches(owner)));
                }
            }
            else if (arg.indexOf('@') == 3) {
                flag = arg.substring(3).length() > 0;
            }
        }

        return flag || arg.contains("|") || arg.contains(internal_escape) || arg.startsWith("li@");
    }


    /////////////
    //   Constructors
    //////////

    // A list of dObjects
    public dList(Collection<? extends dObject> dObjectList) {
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

    // A Set<String> of items
    public dList(Set<String> items) {
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
     * Fetches a String Array copy of the dList,
     * with the same size as the dList.
     *
     * @return the array copy
     */
    public String[] toArray() {
        return toArray(size());
    }

    /**
     * Fetches a String Array copy of the dList.
     *
     * @param arraySize the size of the new array
     * @return the array copy
     */
    public String[] toArray(int arraySize) {
        List<String> list = new ArrayList<String>();

        for (String string : this) {
            list.add(string);
        }

        return list.toArray(new String[arraySize]);
    }


    // Returns if the list contains objects from the specified dClass
    // by using the matches() method.
    public boolean containsObjectsFrom(Class<? extends dObject> dClass) {

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
                            (entry != null ? ((BukkitScriptEntryData)entry.entryData).getPlayer(): null),
                            (entry != null ? ((BukkitScriptEntryData)entry.entryData).getNPC(): null));

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

        return results;
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
        // @attribute <li@list.get_sub_items[<#>]>
        // @returns dList
        // @description
        // returns a list of the specified sub items in the list, as split by the
        // forward-slash character (/).
        // EG, .get_sub_items[1] on a list of "one/alpha|two/beta" will return "one|two".
        // -->

        if (attribute.startsWith("get_sub_items")) {
            int index = -1;
            if (aH.matchesInteger(attribute.getContext(1)))
                index = attribute.getIntContext(1) - 1;
            attribute.fulfill(1);


            // <--[tag]
            // @attribute <li@list.get_sub_items[<#>].split_by[<element>]>
            // @returns dList
            // @description
            // returns a list of the specified sub item in the list, allowing you to specify a
            // character in which to split the sub items by. WARNING: When setting your own split
            // character, make note that it is CASE SENSITIVE.
            // EG, .get_sub_items[1].split_by[-] on a list of "one-alpha|two-beta" will return "one|two".
            // -->

            String split = "/";
            if (attribute.startsWith("split_by")) {
                if (attribute.hasContext(1) && attribute.getContext(1).length() > 0)
                    split = attribute.getContext(1);
                attribute.fulfill(1);
            }

            if (index < 0)
                return null;

            dList sub_list = new dList();

            for (String item : this) {
                String[] strings = item.split(Pattern.quote(split));
                if (strings.length > index)
                    sub_list.add(strings[index]);
                else sub_list.add("null");
            }

            return sub_list.getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <li@list.map_get[<element>]>
        // @returns dList
        // @description
        // Returns the sub-list split by the / symbol's
        // value for the matching input element.
        // TODO: Clarify
        // EG: li@one/a|two/b.map_get[one] returns a.
        // -->

        if (attribute.startsWith("map_get")
                && attribute.hasContext(1)) {
            String input = attribute.getContext(1);
            attribute.fulfill(1);


            // <--[tag]
            // @attribute <li@list.map_get[<element>].split_by[<element>]>
            // @returns dList
            // @description
            // Returns the sub-list split by the given symbol's
            // value for the matching input element.
            // TODO: Clarify
            // EG: li@one/a|two/b.map_get[one] returns a.
            // -->

            String split = "/";
            if (attribute.startsWith("split_by")) {
                if (attribute.hasContext(1) && attribute.getContext(1).length() > 0)
                    split = attribute.getContext(1);
                attribute.fulfill(1);
            }

            for (String item : this) {
                String[] strings = item.split(Pattern.quote(split), 2);
                if (strings.length > 1 && strings[0].equalsIgnoreCase(input)) {
                    return new Element(strings[1]).getAttribute(attribute);
                }
            }
        }

        // <--[tag]
        // @attribute <li@list.comma_separated>
        // @returns Element
        // @description
        // returns the list in a cleaner format, separated by commas.
        // EG, a list of "one|two|three" will return "one, two, three".
        // -->
        if (attribute.startsWith("comma_separated")
                || attribute.startsWith("ascslist")
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
        // @attribute <li@list.space_separated>
        // @returns Element
        // @description
        // returns the list in a cleaner format, separated by spaces.
        // EG, a list of "one|two|three" will return "one two three".
        // -->
        if (attribute.startsWith("space_separated")) {
            if (isEmpty()) return new Element("").getAttribute(attribute.fulfill(1));
            StringBuilder dScriptArg = new StringBuilder();
            for (String item : this) {
                dScriptArg.append(item);
                // Insert a space after each item
                dScriptArg.append(" ");
            }
            return new Element(dScriptArg.toString().substring(0, dScriptArg.length() - 1))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.unseparated>
        // @returns Element
        // @description
        // returns the list in a less clean format, separated by nothing.
        // EG, a list of "one|two|three" will return "onetwothree".
        // -->
        if (attribute.startsWith("unseparated")) {
            if (isEmpty()) return new Element("").getAttribute(attribute.fulfill(1));
            StringBuilder dScriptArg = new StringBuilder();
            for (String item : this) {
                dScriptArg.append(item);
            }
            return new Element(dScriptArg.toString())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.formatted>
        // @returns Element
        // @description
        // returns the list in a human-readable format.
        // EG, a list of "n@3|p@bob|potato" will return "GuardNPC, bob, and potato".
        // -->
        if (attribute.startsWith("formatted")) {
            if (isEmpty()) return new Element("").getAttribute(attribute.fulfill(1));
            StringBuilder dScriptArg = new StringBuilder();

            for (int n = 0; n < this.size(); n++) {
                if (get(n).startsWith("p@")) {
                    dPlayer gotten = dPlayer.valueOf(get(n));
                    if (gotten != null) {
                        dScriptArg.append(gotten.getName());
                    }
                    else {
                        dScriptArg.append(get(n).replaceAll("\\w+@", ""));
                    }
                }
                else if (get(n).startsWith("e@") || get(n).startsWith("n@")) {
                    dEntity gotten = dEntity.valueOf(get(n));
                    if (gotten != null) {
                        dScriptArg.append(gotten.getName());
                    }
                    else {
                        dScriptArg.append(get(n).replaceAll("\\w+@", ""));
                    }
                }
                else {
                    dScriptArg.append(get(n).replaceAll("\\w+@", ""));
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
        // EG, a list of "one|two|three" will return "3".
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
        // EG, a list of "one|two|three" will return "one two three".
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
        // @attribute <li@list.insert[...|...].at[<#>]>
        // @returns dList
        // @description
        // returns a new dList with the items specified inserted to the specified location.
        // EG, .insert[two|three].at[2] on a list of "one|four" will return "one|two|three|four".
        // -->
        if (attribute.startsWith("insert") &&
                attribute.hasContext(1)) {
            dList items = dList.valueOf(attribute.getContext(1));
            attribute = attribute.fulfill(1);
            if (attribute.startsWith("at")
                    && attribute.hasContext(1)) {
                dList result = new dList(this);
                int index = new Element(attribute.getContext(1)).asInt() - 1;
                if (index < 0)
                    index = 0;
                if (index > result.size())
                    index = result.size();
                for (int i = 0; i < items.size(); i++) {
                    result.add(index + i, items.get(i));
                }
                return result.getAttribute(attribute.fulfill(1));
            }
            else {
                dB.echoError("The tag li@list.insert[...] requires an at[#] tag follow it!");
                return null;
            }
        }

        // <--[tag]
        // @attribute <li@list.include[...|...]>
        // @returns dList
        // @description
        // returns a new dList including the items specified.
        // EG, .include[three|four] on a list of "one|two" will return "one|two|three|four".
        // -->
        if (attribute.startsWith("include") &&
                attribute.hasContext(1)) {
            dList list = new dList(this);
            list.addAll(dList.valueOf(attribute.getContext(1)));
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.exclude[...|...]>
        // @returns dList
        // @description
        // returns a new dList excluding the items specified.
        // EG, .exclude[two|four] on a list of "one|two|three|four" will return "one|three".
        // -->
        if (attribute.startsWith("exclude") &&
                attribute.hasContext(1)) {
            dList exclusions = dList.valueOf(attribute.getContext(1));
            // Create a new dList that will contain the exclusions
            dList list = new dList(this);
            // Iterate through
            for (String exclusion : exclusions) {
                for (int i = 0;i < list.size();i++) {
                    if (list.get(i).equalsIgnoreCase(exclusion)) {
                        list.remove(i--);
                    }
                }
            }

            // Return the modified list
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.remove[<#>|...]>
        // @returns dList
        // @description
        // returns a new dList excluding the items at the specified index.
        // EG, .remove[2] on a list of "one|two|three|four" will return "one|three|four".
        // -->
        if (attribute.startsWith("remove") &&
                attribute.hasContext(1)) {
            dList indices = dList.valueOf(attribute.getContext(1));
            dList list = new dList(this);
            for (String index: indices) {
                int remove = new Element(index).asInt() - 1;
                if (remove >= 0 && remove < list.size()) {
                    list.set(remove, "\0");
                }
            }
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals("\0"))
                    list.remove(i--);
            }
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.reverse>
        // @returns dList
        // @description
        // returns a copy of the list, with all items placed in opposite order.
        // EG, a list of "one|two|three" will become "three|two|one".
        // -->
        if (attribute.startsWith("reverse")) {
            dList list = new dList(this);
            Collections.reverse(list);
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.deduplicate>
        // @returns dList
        // @description
        // returns a copy of the list with any duplicate items removed.
        // EG, a list of "one|one|two|three" will become "one|two|three".
        // -->
        if (attribute.startsWith("deduplicate")) {
            dList list = new dList();
            int listSize = 0;
            int size = this.size();
            for (int i = 0; i < size; i++) {
                String entry = get(i);
                boolean duplicate = false;
                for (int x = 0; x < listSize; x++) {
                    if (get(x).equalsIgnoreCase(entry)) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) {
                    list.add(entry);
                    listSize++;
                }
            }
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.get[<#>|...]>
        // @returns dObject
        // @description
        // returns an element of the value specified by the supplied context.
        // EG, .get[1] on a list of "one|two" will return "one", and .get[2] will return "two"
        // Specify more than one index to get a list of results.
        // -->
        if (attribute.startsWith("get") &&
                attribute.hasContext(1)) {
            if (isEmpty()) return null;
            dList indices = dList.valueOf(attribute.getContext(1));
            if (indices.size() > 1) {
                dList results = new dList();
                for (String index: indices) {
                    int ind = aH.getIntegerFrom(index);
                    if (ind > 0 && ind <= size())
                        results.add(get(ind - 1));
                }
                return results.getAttribute(attribute.fulfill(1));
            }
            if (indices.size() > 0) {
                int index = aH.getIntegerFrom(indices.get(0));
                if (index > size()) return null;
                if (index < 1) index = 1;
                attribute = attribute.fulfill(1);

                // <--[tag]
                // @attribute <li@list.get[<#>].to[<#>]>
                // @returns dList
                // @description
                // returns all elements in the range from the first index to the second.
                // EG, .get[1].to[3] on a list of "one|two|three|four" will return "one|two|three"
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
                    return ObjectFetcher.pickObjectFor(item).getAttribute(attribute);
                }
            }
        }

        // <--[tag]
        // @attribute <li@list.find_all_partial[<element>]>
        // @returns dList(Element(Number))
        // @description
        // returns all the numbered locations of elements within a list,
        // or an empty list if the list does not contain that item.
        // EG, .find[two] on a list of "one|two|three|two" will return "2|4".
        // TODO: Take multiple inputs? Or a regex?
        // -->
        if (attribute.startsWith("find_all_partial") &&
                attribute.hasContext(1)) {
            dList positions = new dList();
            for (int i = 0; i < size(); i++) {
                if (get(i).toUpperCase().contains(attribute.getContext(1).toUpperCase()))
                    positions.add(String.valueOf(i + 1));
            }
            return positions.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.find_all[<element>]>
        // @returns dList(Element(Number))
        // @description
        // returns all the numbered locations of elements within a list,
        // or an empty list if the list does not contain that item.
        // EG, .find[two] on a list of "one|two|three|two" will return "2|4".
        // TODO: Take multiple inputs? Or a regex?
        // -->
        if (attribute.startsWith("find_all") &&
                attribute.hasContext(1)) {
            dList positions = new dList();
            for (int i = 0; i < size(); i++) {
                if (get(i).equalsIgnoreCase(attribute.getContext(1)))
                    positions.add(String.valueOf(i + 1));
            }
            return positions.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.find_partial[<element>]>
        // @returns Element(Number)
        // @description
        // returns the numbered location of the first partially matching element within a list,
        // or -1 if the list does not contain that item.
        // EG, .find[two] on a list of "one|two|three" will return "2".
        // TODO: Take multiple inputs? Or a regex?
        // -->
        if (attribute.startsWith("find_partial") &&
                attribute.hasContext(1)) {
            for (int i = 0; i < size(); i++) {
                if (get(i).toUpperCase().contains(attribute.getContext(1).toUpperCase()))
                    return new Element(i + 1).getAttribute(attribute.fulfill(1));
            }
            return new Element(-1).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.find[<element>]>
        // @returns Element(Number)
        // @description
        // returns the numbered location of an element within a list,
        // or -1 if the list does not contain that item.
        // EG, .find[two] on a list of "one|two|three" will return "2".
        // TODO: Take multiple inputs? Or a regex?
        // -->
        if (attribute.startsWith("find") &&
                attribute.hasContext(1)) {
            for (int i = 0; i < size(); i++) {
                if (get(i).equalsIgnoreCase(attribute.getContext(1)))
                    return new Element(i + 1).getAttribute(attribute.fulfill(1));
            }
            // TODO: This should be find_partial or something
            /*
            for (int i = 0; i < size(); i++) {
                if (get(i).toUpperCase().contains(attribute.getContext(1).toUpperCase()))
                    return new Element(i + 1).getAttribute(attribute.fulfill(1));
            }
            */
            return new Element(-1).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.count[<element>]>
        // @returns Element(Number)
        // @description
        // returns how many times in the sub-list occurs.
        // EG, a list of "one|two|two|three" .count[two] returns 2.
        // -->
        if (attribute.startsWith("count") &&
                attribute.hasContext(1)) {
            String element = attribute.getContext(1);
            int count = 0;
            for (int i = 0; i < size(); i++) {
                if (get(i).equalsIgnoreCase(element))
                    count++;
            }
            return new Element(count).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.first>
        // @returns Element
        // @description
        // returns the first element in the list.
        // If the list is empty, returns null instead.
        // EG, a list of "one|two|three" will return "one".
        // Effectively equivalent to .get[1]
        // -->
        if (attribute.startsWith("first")) {
            if (size() == 0)
                return null;
            else
                return new Element(get(0)).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.last>
        // @returns Element
        // @description
        // returns the last element in the list.
        // If the list is empty, returns null instead.
        // EG, a list of "one|two|three" will return "three".
        // Effectively equivalent to .get[999999]
        // -->
        if (attribute.startsWith("last")) {
            if (size() == 0)
                return null;
            else
                return new Element(get(size() - 1)).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.numerical>
        // @returns Element
        // @description
        // returns the list sorted to be in numerical order.
        // EG, a list of "3|2|1|10" will return "1|2|3|10".
        // -->
        if (attribute.startsWith("numerical")) {
            dList list = new dList(this);
            Collections.sort(list, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    double value = new Element(o1).asDouble() - new Element(o2).asDouble();
                    if (value == 0)
                        return 0;
                    else if (value > 0)
                        return 1;
                    else
                        return -1;
                }
            });
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.alphanumeric>
        // @returns Element
        // @description
        // returns the list sorted to be in alphabetical/numerical order.
        // EG, a list of "b|c|a10|a1" will return "a1|a10|b|c".
        // -->
        if (attribute.startsWith("alphanumeric")) {
            dList list = new dList(this);
            Collections.sort(list, new NaturalOrderComparator());
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.alphabetical>
        // @returns Element
        // @description
        // returns the list sorted to be in alphabetical order.
        // EG, a list of "c|d|q|a|g" will return "a|c|d|g|q".
        // -->
        if (attribute.startsWith("alphabetical")) {
            dList list = new dList(this);
            Collections.sort(list, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            });
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.sort[<procedure>]>
        // @returns Element
        // @description
        // returns a list sorted according to the return values of a procedure.
        // The <procedure> should link a procedure script that takes two definitions
        // each of which will be an item in the list, and returns -1, 0, or 1 based on
        // whether the second item should be added. EG, if a procedure with definitions
        // "one" and "two" returned 1, it would place "two" after "one". Note that this
        // uses some complex internal sorting code that could potentially throw errors
        // if the procedure does not return consistently - EG, if "one" and "two" returned
        // 1, but "two" and "one" returned 1 as well - obviously, "two" can not be both
        // before AND after "one"!
        // Note that the script should ALWAYS return -1, 0, or 1, or glitches will happen!
        // Note that if two inputs are exactly equal, the procedure should always return 0.
        // -->
        if (attribute.startsWith("sort")
                && attribute.hasContext(1)) {
            final ProcedureScriptContainer script = ScriptRegistry.getScriptContainer(attribute.getContext(1));
            if (script == null) {
                dB.echoError("'" + attribute.getContext(1) + "' is not a valid procedure script!");
                return getAttribute(attribute.fulfill(1));
            }
            final ScriptEntry entry = attribute.getScriptEntry();
            List<String> list = new ArrayList<String>(this);
            try {
                Collections.sort(list, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        List<ScriptEntry> entries = script.getBaseEntries(entry.entryData.clone());
                        if (entries.isEmpty()) {
                            return 0;
                        }
                        long id = DetermineCommand.getNewId();
                        ScriptBuilder.addObjectToEntries(entries, "ReqId", id);
                        InstantQueue queue = InstantQueue.getQueue(ScriptQueue.getNextId("DLIST_SORT"));
                        queue.addEntries(entries);
                        queue.setReqId(id);
                        int x = 1;
                        dList definitions = new dList();
                        definitions.add(o1);
                        definitions.add(o2);
                        String[] definition_names = null;
                        try { definition_names = script.getString("definitions").split("\\|"); } catch (Exception e) { /* IGNORE */ }
                        for (String definition : definitions) {
                            String name = definition_names != null && definition_names.length >= x ?
                                    definition_names[x - 1].trim() : String.valueOf(x);
                            queue.addDefinition(name, definition);
                            dB.echoDebug(entry, "Adding definition %" + name + "% as " + definition);
                            x++;
                        }
                        queue.start();
                        int res = 0;
                        if (DetermineCommand.hasOutcome(id))
                            res = new Element(DetermineCommand.getOutcome(id)).asInt();
                        if (res < 0)
                            return -1;
                        else if (res > 0)
                            return 1;
                        else
                            return 0;
                    }
                });
            }
            catch (Exception e) {
                dB.echoError("list.sort[...] tag failed - procedure returned unreasonable valid - internal error: " + e.getMessage());
            }
            return new dList(list).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.filter[<tag>]>
        // @returns dList
        // @description
        // returns a copy of the list with all its contents parsed through the given tag and only including ones that returned 'true'.
        // For example, a list of '1|2|3|4|5' .filter[is[or_more].than[3]] returns a list of '3|4|5'.
        // -->
        if (attribute.startsWith("filter")
                && attribute.hasContext(1)) {
            dList newlist = new dList();
            try {
                for (String str: this) {
                    if (ObjectFetcher.pickObjectFor(str).getAttribute(new Attribute(attribute.getContext(1),
                            attribute.getScriptEntry())).equalsIgnoreCase("true")) {
                        newlist.add(str);
                    }
                }
            }
            catch (Exception ex) {
                dB.echoError(ex);
            }
            return newlist.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.parse[<tag>]>
        // @returns dList
        // @description
        // returns a copy of the list with all its contents parsed through the given tag.
        // For example, a list of 'one|two' .parse[to_uppercase] returns a list of 'ONE|TWO'.
        // -->
        if (attribute.startsWith("parse")
                && attribute.hasContext(1)) {
            dList newlist = new dList();
            try {
                for (String str: this) {
                    newlist.add(ObjectFetcher.pickObjectFor(str).getAttribute(new Attribute(attribute.getContext(1),
                            attribute.getScriptEntry())));
                }
            }
            catch (Exception ex) {
                dB.echoError(ex);
            }
            return newlist.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.escape_contents>
        // @returns dList
        // @description
        // returns a copy of the list with all its contents escaped.
        // Inverts <@link tag li@list.unescape_contents>
        // See <@link language property escaping>
        // -->
        if (attribute.startsWith("escape_contents")) {
            dList escaped = new dList();
            for (String entry: this) {
                escaped.add(EscapeTags.Escape(entry));
            }
            return escaped.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.unescape_contents>
        // @returns dList
        // @description
        // returns a copy of the list with all its contents unescaped.
        // Inverts <@link tag li@list.escape_contents>
        // See <@link language property escaping>
        // -->
        if (attribute.startsWith("unescape_contents")) {
            dList escaped = new dList();
            for (String entry: this) {
                escaped.add(EscapeTags.unEscape(entry));
            }
            return escaped.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.contains_any_case_sensitive[<element>|...]>
        // @returns Element(Boolean)
        // @description
        // returns whether the list contains any of a list of given elements, case-sensitive.
        // -->
        if (attribute.matches("contains_any_case_sensitive")) {
            if (attribute.hasContext(1)) {
                dList list = dList.valueOf(attribute.getContext(1));
                boolean state = false;

                full_set:
                for (String element : this) {
                    for (String sub_element: list) {
                        if (element.equals(sub_element)) {
                            state = true;
                            break full_set;
                        }
                    }
                }

                return new Element(state).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <li@list.contains_any[<element>|...]>
        // @returns Element(Boolean)
        // @description
        // returns whether the list contains any of a list of given elements.
        // -->
        if (attribute.matches("contains_any")) {
            if (attribute.hasContext(1)) {
                dList list = dList.valueOf(attribute.getContext(1));
                boolean state = false;

                full_set:
                for (String element : this) {
                    for (String sub_element: list) {
                        if (element.equalsIgnoreCase(sub_element)) {
                            state = true;
                            break full_set;
                        }
                    }
                }

                return new Element(state).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <li@list.contains_case_sensitive[<element>]>
        // @returns Element(Boolean)
        // @description
        // returns whether the list contains a given element, case-sensitive.
        // -->
        if (attribute.matches("contains_case_sensitive")) {
            if (attribute.hasContext(1)) {
                boolean state = false;

                for (String element : this) {
                    if (element.equals(attribute.getContext(1))) {
                        state = true;
                        break;
                    }
                }

                return new Element(state).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <li@list.contains[<element>|...]>
        // @returns Element(Boolean)
        // @description
        // returns whether the list contains all of the given elements.
        // -->
        if (attribute.matches("contains")
                && attribute.hasContext(1)) {
            dList needed = dList.valueOf(attribute.getContext(1));
            int gotten = 0;

            for (String check: needed) {
                for (String element : this) {
                    if (element.equalsIgnoreCase(check)) {
                        gotten++;
                        break;
                    }
                }
            }

            return new Element(gotten == needed.size()).getAttribute(attribute.fulfill(1));
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

        if (attribute.startsWith("identify")) { // TODO: ???
            return new Element(identify())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.type>
        // @returns Element
        // @description
        // Always returns 'List' for dList objects. All objects fetchable by the Object Fetcher will return a the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("type")) {
            return new Element("List").getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <li@list.random[<#>]>
        // @returns Element
        // @description
        // Gets a random item in the list and returns it as an Element.
        // Optionally, add [<#>] to get a list of multiple randomly chosen elements.
        // EG, .random on a list of "one|two" could return EITHER "one" or "two" - different each time!
        // EG, .random[2] on a list of "one|two|three" could return "one|two", "two|three", OR "one|three" - different each time!
        // EG, .random[9999] on a list of "one|two|three" could return "one|two|three", "one|three|two", "two|one|three",
        // "two|three|one", "three|two|one", OR "three|one|two" - different each time!
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
                        int random = CoreUtilities.getRandom().nextInt(available.size());
                        toReturn.add(available.get(random));
                        available.remove(random);
                        times++;
                    }
                    return toReturn.getAttribute(attribute.fulfill(1));
                }
                else {
                    return ObjectFetcher.pickObjectFor(this.get(CoreUtilities.getRandom().nextInt(this.size())))
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
        // NOTE: Defined in UtilTags.java

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
