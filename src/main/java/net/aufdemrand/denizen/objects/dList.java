package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;

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
                    if (FlagManager.npcHasFlag(aH.getNPCFrom(m.group(3)), m.group(4)))
                        return new dList(flag_manager.getNPCFlag(Integer.valueOf(m.group(3)), m.group(4)));
                }

            } catch (Exception e) {
                dB.echoDebug("Flag '" + m.group() + "' could not be found!");
                return null;
            }
        }

        // Use value of string, which will seperate values by the use of a pipe (|)
        return new dList(string.replaceFirst("li@", ""));
    }


    public static boolean matches(String arg) {

    	return true;
        /*
    	Matcher m;
        m = flag_by_id.matcher(arg);

        if (m.matches()) return true;

        if (arg.contains("|") || arg.startsWith("li@")) return true;

        return false;
        */
    }


    /////////////
    //   Constructors
    //////////


    public dList(String items) {
        addAll(Arrays.asList(items.split("\\|")));
    }

    public dList(List<String> items) {
        addAll(items);
    }
    
    public dList(List<String> items, String prefix) {
    	
    	for (String element : items) {
    		add(prefix + element);
    	}
    }

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
        if (flag != null) return true;
        else return false;
    }

    @Override
    public String getType() {
        return "dList";
    }
    
    // Return a list that includes only elements belonging to a certain class
    public List<dObject> filter(Class<? extends dObject> dClass) {
        
    	List<dObject> results = new ArrayList<dObject>();
    	
    	for (String element : this) {
    		
    		try {
				if ((Boolean) dClass.getMethod("matches", String.class).invoke(null, element)) {
					
					results.add((dObject) dClass.getMethod("valueOf", String.class).invoke(null, element));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
		return results;
    }
    
    @Override
    public String identify() {
        if (flag != null)
            return flag.toString();

        if (isEmpty()) return "li@";

        StringBuilder dScriptArg = new StringBuilder();
        dScriptArg.append("li@");
        for (String item : this)
            dScriptArg.append(item + "|");

        return dScriptArg.toString().substring(0, dScriptArg.length() - 1);
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        if (attribute.startsWith("ascslist")
                || attribute.startsWith("as_cslist")) {
            if (isEmpty()) return new Element("").getAttribute(attribute.fulfill(1));
            StringBuilder dScriptArg = new StringBuilder();
            for (String item : this)
                dScriptArg.append(item + ", ");
            return new Element(dScriptArg.toString().substring(0, dScriptArg.length() - 2))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("get")) {
            if (isEmpty()) return "null";
            int index = attribute.getIntContext(1);
            if (index > size()) return "null";
            String item;
            if (index > 0) item = get(index - 1);
            else item = get(0);
            if (attribute.getAttribute(2).startsWith("as")) {

            }
            else
                return new Element(item).getAttribute(attribute.fulfill(1));
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
        
        if (attribute.startsWith("random")) {
        	return new Element(this.get(new Random().nextInt(this.size())))
                    .getAttribute(attribute.fulfill(1));
        }

        // FLAG Specific Attributes

        if (attribute.startsWith("is_expired")) {
            if (flag == null) return new Element("false")
                    .getAttribute(attribute.fulfill(1));
            return new Element(flag.checkExpired())
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("expiration")) {
            if (flag == null) return Duration.ZERO
                    .getAttribute(attribute.fulfill(1));
            return flag.expiration()
                    .getAttribute(attribute.fulfill(1));
        }

        return (flag != null
                ? new Element(flag.getLast().asString()).getAttribute(attribute.fulfill(0))
                : new Element(identify()).getAttribute(attribute.fulfill(0)));
    }

}
