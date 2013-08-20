package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dList extends ArrayList<String> implements dObject {

    final static Pattern flag_by_id =
            Pattern.compile("(fl\\[((?:p@|n@)(.+?))\\]@|fl@)(.+)",
                    Pattern.CASE_INSENSITIVE);

    final static Pattern split_char = Pattern.compile("\\|");
    final static Pattern identifier = Pattern.compile("li@", Pattern.CASE_INSENSITIVE);
    
    @ObjectFetcher("li, fl")
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
                dB.echoDebug("Flag '" + m.group() + "' could not be found!");
                return null;
            }
        }

        // Use value of string, which will seperate values by the use of a pipe '|'
        return new dList(string.replaceFirst(identifier.pattern(), ""));
    }


    public static boolean matches(String arg) {

        Matcher m;
        m = flag_by_id.matcher(arg);

        return m.matches() || arg.contains("|") || arg.toLowerCase().startsWith("li@");
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
        if (items != null) addAll(Arrays.asList(split_char.split(items)));
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
    public String getType() {
        return "List";
    }
    
    public String[] toArray() {
        
        List<String> list = new ArrayList<String>();
        
        for (String string : this) {
            list.add(string);
        }
        
        return list.toArray(new String[list.size()]);
    }
    
    // Return a list that includes only elements belonging to a certain class
    public List<dObject> filter(Class<? extends dObject> dClass) {
        
        List<dObject> results = new ArrayList<dObject>();
        
        for (String element : this) {
            
            try {
                if ((Boolean) dClass.getMethod("matches", String.class).invoke(null, element)) {
                    
                    dObject object = (dObject) dClass.getMethod("valueOf", String.class).invoke(null, element); 
                    
                    // Only add the object if it is not null, thus filtering useless
                    // list items
                    
                    if (object != null) {
                        results.add(object);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (results.size() > 0) return results;
        else return null;
    }

    @Override
    public String toString() {
        return identify();
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
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        // <--
        // <li@list.as_cslist> -> Element
        // returns 'comma-separated' list of the contents of this dList.
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

        // <--
        // <li@list.size> -> Element
        // returns 'comma-separated' list of the contents of this dList.
        // -->
        if (attribute.startsWith("size"))
            return new Element(size()).getAttribute(attribute.fulfill(1));

        // <--
        // <li@list.is_empty> -> Element
        // returns 'comma-separated' list of the contents of this dList.
        // -->
        if (attribute.startsWith("is_empty"))
            return new Element(isEmpty()).getAttribute(attribute.fulfill(1));

        // <--
        // <li@list.as_string> -> Element
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

        // <--
        // <li@list.exclude[...|...]> -> dList
        // returns a new dList excluding the items specified.
        // -->
        if (attribute.startsWith("exclude")) {
            String[] exclusions = split_char.split(attribute.getContext(1));
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

        // <--
        // <li@list.get[#]> -> Element
        // returns an Element of the value specified by the supplied context.
        // -->
        if (attribute.startsWith("get")) {
            if (isEmpty()) return "null";
            int index = attribute.getIntContext(1);
            if (index > size()) return "null";
            String item;
            if (index > 0) item = get(index - 1);
            else item = get(0);

            return new Element(item).getAttribute(attribute.fulfill(1));
        }
        
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
            return new Element(getType())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <li@list.random> -> Element
        // gets a random item in the list and returns it as an Element.
        // -->
        if (attribute.startsWith("random")) {
            if (!this.isEmpty())
                return new Element(this.get(new Random().nextInt(this.size())))
                    .getAttribute(attribute.fulfill(1));
        }

        // FLAG Specific Attributes

        // Note: is_expired attribute is handled in player/npc/server
        // since expired flags return 'null'

        // <--
        // <fl@flag_name.is_expired> -> Element(boolean)
        // returns true of the flag is expired or does not exist, false if it
        // is not yet expired, or has no expiration.
        // -->

        // <--
        // <fl@flag_name.expiration> -> Duration
        // returns a Duration of the time remaining on the flag, if it
        // has an expiration.
        // -->
        if (flag != null && attribute.startsWith("expiration")) {
            return flag.expiration()
                    .getAttribute(attribute.fulfill(1));
        }

        // Need this attribute (for flags) since they return the last
        // element of the list, unless '.as_list' is specified.

        // <--
        // <fl@flag_name.as_list> -> dList
        // returns a dList containing the items in the flag
        // -->
        if (flag != null && (attribute.startsWith("as_list")
                || attribute.startsWith("aslist")))
            return new dList(this).getAttribute(attribute.fulfill(1));


        // If this is a flag, return the last element (this is how it has always worked...)
        // Use as_list to return a list representation of the flag.
        // If this is NOT a flag, but instead a normal dList, return an element
        // with dList's identify() value.

        return (flag != null
                ? new Element(flag.getLast().asString()).getAttribute(attribute.fulfill(0))
                : new Element(identify()).getAttribute(attribute.fulfill(0)));
    }

}
