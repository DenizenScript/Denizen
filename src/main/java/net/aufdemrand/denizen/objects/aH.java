package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The dScript Argument Helper will aide you in parsing and formatting arguments from a 
 * dScript argument string (such as those found in a ScriptEntry.getArguments() method).
 *
 * @author aufdemrand
 *
 */
public class aH {

    public enum PrimitiveType { Float, Double, Integer, Boolean, String, Word }

    final static Pattern floatPrimitive = Pattern.compile("^[-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?$");
    final static Pattern doublePrimitive = Pattern.compile("(-)?(?:(?:\\d+)|)(?:(?:\\.\\d+)|)");
    final static Pattern integerPrimitive = Pattern.compile("(-)?\\d+");
    final static Pattern booleanPrimitive = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
    final static Pattern wordPrimitive = Pattern.compile("\\w+");

    public static class Argument {
        public String raw_value;
        String prefix = null;
        String value;
        boolean has_prefix = false;

        // Construction
        public Argument(String string) {
            raw_value = string;
            string = string.trim();

            int first_colon = string.indexOf(":");
            int first_space = string.indexOf(" ");

            // dB.log("Constructing Argument: " + raw_value + " " + first_colon + "," + first_space);

            if ((first_space > -1 && first_space < first_colon) || first_colon == -1)  value = string;
            else {
                has_prefix = true;
                prefix = string.split(":")[0];
                value = string.split(":")[1];
            }

            // dB.log("Constructed Argument: " + prefix + ":" + value);
        }

        public boolean hasPrefix() {
            return has_prefix;
        }

        public boolean startsWith(String string) {
            return value.startsWith(string);
        }

        public boolean matches(String string) {
            return value.equalsIgnoreCase(string);
        }

        public void replaceValue(String string) {
            value = string;
        }

        public String getValue() {
            return value;
        }

        public boolean matchesEnum(Enum[] values) {
            for (Enum value : values)
                if (value.name().replace("_", "").equalsIgnoreCase(this.value.replace("_", "")))
                    return true;

            return false;
        }

        public boolean matchesPrefix(String values) {
            for (String value : values.split(","))
                if (value.trim().equalsIgnoreCase((prefix != null ? prefix : this.value)))
                    return true;

            return false;
        }

        public boolean matchesPrimitive(PrimitiveType argumentType) {
            if (value == null) return false;

            switch (argumentType) {
                case Word:
                    return wordPrimitive.matcher(value).matches();

                case Integer:
                    return integerPrimitive.matcher(value).matches();

                case Double:
                    return doublePrimitive.matcher(value).matches();

                case Float:
                    return floatPrimitive.matcher(value).matches();

                case Boolean:
                    return booleanPrimitive.matcher(value).matches();

                case String:
                    return true;
            }

            return false;
        }

        public boolean matchesArgumentType(Class<? extends dObject> clazz) {

            // dB.log("Calling matches: " + prefix + ":" + value + " " + clazz.getCanonicalName());

            try {
                return (Boolean) clazz.getMethod("matches", String.class).invoke(null, value);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        public Element asElement() {
            return new Element(prefix, value);
        }

        public <T extends dObject> T asType(Class<? extends dObject> clazz) {

            // dB.log("Calling asType: " + prefix + ":" + value + " " + clazz.getCanonicalName());

            dObject arg = null;
            try {
                arg = (dObject) clazz.getMethod("valueOf", String.class)
                        .invoke(null, value);

                dB.log("Cool! Created: " + clazz.cast(arg).debug());

                return (T) clazz.cast(arg).setPrefix(prefix);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    /**
     * Turns a list of string arguments (separated by buildArgs) into Argument
     * Objects for easy matching and dObject creation throughout Denizen.
     *
     * @param args  a list of string arguments
     * @return  a list of Arguments
     */
    public static List<Argument> interpret(List<String> args) {
        List<Argument> arg_list = new ArrayList<Argument>();
        for (String string : args)
            arg_list.add(new Argument(string.trim()));
        return arg_list;
    }


    /**
     * Builds an arguments array, recognizing items in quotes as a single item, but
     * otherwise splitting on a space.
     *
     * @param stringArgs  the line of arguments that need split
     * @return  an array of arguments
     *
     */
    public static String[] buildArgs(String stringArgs) {
        final Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

        if (stringArgs == null) return null;
        java.util.List<String> matchList = new ArrayList<String>();
        Matcher regexMatcher = regex.matcher(stringArgs);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null)
                matchList.add(regexMatcher.group(1));
            else if (regexMatcher.group(2) != null)
                matchList.add(regexMatcher.group(2));
            else
                matchList.add(regexMatcher.group());
        }

        if (dB.showScriptBuilder)
            dB.echoDebug(ChatColor.GRAY + "Args: " + Arrays.toString(matchList.toArray()));

        return matchList.toArray(new String[matchList.size()]);
    }





    /**
     * To be used with the dBuggers' .report to provide debug output for
     * objects that don't extend dObject.
     *
     * @param prefix  name/type/simple description of the object being reported
     * @param value  object being reported will report the value of toString()
     *
     * @return  color coded debug report
     */
    public static String debugObj(String prefix, Object value) {
        return "<G>" + prefix + "='<Y>" + (value != null ? value.toString() : "null") + "<G>'  ";
    }

    /**
     * To be used with the dBuggers' .report to provide debug output for
     * objects that may have some kind of id or type also associated with
     * the object.
     *
     * @param prefix  name/type/simple description of the object being reported
     * @param id  additional id/type of the object
     * @param value  object being reported will report the value of toString()
     *
     * @return  color coded debug report
     */
    public static String debugUniqueObj(String prefix, String id, Object value) {
        return "<G>" + prefix + "='<A>" + id + "<Y>(" + (value != null ? value.toString() : "null") + ")<G>'  ";
    }


    public enum ArgumentType {
        LivingEntity, Item, Boolean, Custom, Double, Float,
        Integer, String, Word, Location, Script, Duration
    }


    /**
     * <p>Used to determine if a argument string matches a non-valued custom argument.
     * If a dScript valued argument (such as TARGET:NAME) is passed, this method
     * will always return false. Also supports multiple argument names, separated by a
     * comma (,) character. This method will trim() each name specified.</p>
     *
     * <b>Example use of '<tt>aH.matchesArg("NOW, LATER", arg)</tt>':</b>
     * <ol>
     * <tt>arg = "NOW"</tt> will return true.<br>
     * <tt>arg = "NEVER"</tt> will return false.<br>
     * <tt>arg = "LATER:8PM"</tt> will return false.<br>
     * <tt>arg = "LATER"</tt> will return true.
     * </ol>
     *
     * @param names the valid argument names to match
     * @param string_arg the dScript argument string
     * @return true if matched, false if not
     *
     */
    public static boolean matchesArg(String names, String string_arg) {
        String[] parts = names.split(",");
        if (parts.length == 1) {
            if (string_arg.toUpperCase().equals(names.toUpperCase())) return true;
        } else {
            for (String string : parts)
                if (string_arg.split(":")[0].equalsIgnoreCase(string.trim())) return true;
        }
        return false;
    }


    /**
     * <p>Used to match a custom argument with a value. In practice, the standard
     * arguments should be used whenever possible to keep things consistent across
     * the entire 'dScript experience'. Should you need to use custom arguments,
     * however, this method provides some support. After all, while using standard
     * arguments is nice, you should never reach. Arguments should make as much
     * sense to the user/script writer as possible.</p>
     *
     * <b>Small code example:</b>
     * <ol>
     * <tt>0 if (aH.matchesValueArg("HARDNESS", arg, ArgumentType.Word))</tt><br>
     * <tt>1     try { </tt><br>
     * <tt>2        hardness = Hardness.valueOf(aH.getStringFrom(arg).toUpperCase());</tt><br>
     * <tt>3     } catch (Exception e) { </tt><br>
     * <tt>4		dB.echoError("Invalid HARDNESS!") </tt><br>
     * <tt>5 }</tt><br>
     * </ol>
     *
     * <p>Note: Like {@link #matchesArg(String, String)}, matchesValueArg(String)
     * supports multiple argument names, separated by a comma (,) character. This method
     * will trim() each name specified.</p>
     *
     * <p>Also requires a specified ArgumentType, which will filter the type of value
     * to match to. If anything should be excepted as the value, or you plan
     * on parsing the value yourself, use ArgumentType.Custom, otherwise use an
     * an appropriate ArgumentType. See: {@link ArgumentType}.</p>
     *
     * <b>Example use of '<tt>aH.matchesValueArg("TIME", arg, ArgumentType.Integer)</tt>':</b>
     * <ol>
     * <tt>arg = "TIME:60"</tt> will return true.<br>
     * <tt>arg = "90"</tt> will return false.<br>
     * <tt>arg = "TIME:8 o'clock"</tt> will return false.<br>
     * <tt>arg = "TIME:0"</tt> will return true.
     * </ol>
     *
     * @param names the desired name variations of the argument
     * @param string_arg the dScript argument string
     * @param type a valid ArgumentType, used for matching values
     * @return true if matched, false otherwise
     *
     */
    @Deprecated
    public static boolean matchesValueArg(String names, String string_arg, ArgumentType type) {
        if (string_arg == null) return false;

        int firstColonIndex = string_arg.indexOf(':');
        if (firstColonIndex == -1) return false;

        String[] commaParts = names.split(",");

        if (commaParts.length == 1) {
            if (!string_arg.substring(0,firstColonIndex).equalsIgnoreCase(names))
                return false;
        }

        else {
            boolean matched = false;
            for (String string : commaParts)
                if (string_arg.substring(0,firstColonIndex).equalsIgnoreCase(string.trim()))
                    matched = true;
            if (!matched) return false;
        }

        string_arg = string_arg.split(":", 2)[1];

        switch (type) {
            case Word:
                return wordPrimitive.matcher(string_arg).matches();

            case Integer:
                return integerPrimitive.matcher(string_arg).matches();

            case Double:
                return doublePrimitive.matcher(string_arg).matches();

            case Float:
                return floatPrimitive.matcher(string_arg).matches();

            case Boolean:
                return booleanPrimitive.matcher(string_arg).matches();

            case Location:
                return dLocation.matches(string_arg);

            case Script:
                // return dScript.matches(string_arg);
                return true;

            case Item:
                return dItem.matches(string_arg);

            case LivingEntity:
                return dEntity.matches(string_arg);

            case Duration:
                return Duration.matches(string_arg);

            case String:
                return true;

        }

        dB.echoError("While parsing '" + string_arg + "', Denizen has run into a problem. While the " +
                "prefix is correct, the value is not valid. Check documentation for valid value." +
                "Perhaps a replaceable tag has failed to fill in a value?");

        return false;
    }

    public static boolean getBooleanFrom(String arg) {
        return Boolean.valueOf(getStringFrom(arg));
    }

    public static double getDoubleFrom(String arg) {
        try {
            return Double.valueOf(getStringFrom(arg));
        } catch (NumberFormatException e) {
            return 0D;
        }
    }

    @Deprecated
    public static EntityType getEntityTypeFrom(String arg) {
        for (EntityType validEntity : EntityType.values())
            if (getStringFrom(arg).equalsIgnoreCase(validEntity.name()))
                return validEntity;

        // No match
        return null;
    }

    @Deprecated
    public static dEntity getEntityFrom(String arg) {
        arg = arg.replace("entity:", "");
        return dEntity.valueOf(arg);
    }

    public static float getFloatFrom(String arg) {
        try {
            return Float.valueOf(getStringFrom(arg));
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    public static int getIntegerFrom(String arg) {
        try {
            return Integer.valueOf(getStringFrom(arg));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Deprecated
    public static dItem getItemFrom(String arg) {
        arg = arg.replace("item:", "");
        dItem stack = dItem.valueOf(arg);
        return stack;
    }

    @Deprecated
    public static dList getListFrom(String arg) {
        return dList.valueOf(arg);
    }

    @Deprecated
    public static dLocation getLocationFrom(String arg) {
        arg = arg.replace("location:", "");
        return dLocation.valueOf(arg);
    }

    @Deprecated
    public static dScript getScriptFrom(String arg) {
        arg = arg.replace("script:", "");
        return dScript.valueOf(arg);
    }

    @Deprecated
    public static dPlayer getPlayerFrom(String arg) {
        return dPlayer.valueOf(arg);
    }

    @Deprecated
    public static dNPC getNPCFrom(String arg) {
        return dNPC.valueOf(arg);
    }

    @Deprecated
    public static ScriptQueue getQueueFrom(String arg) {
        arg = arg.replace("queue:", "");
        return ScriptQueue._getQueue(getStringFrom(arg).toUpperCase());
    }

    public static String getStringFrom(String arg) {
        String[] parts = arg.split(":", 2);
        return parts.length >=2 ? parts[1] : arg;
    }

    @Deprecated
    public static Duration getDurationFrom(String arg) {
        arg = arg.replace("duration:", "");
        return Duration.valueOf(arg);
    }

    public static boolean matchesDouble(String arg) {
        return doublePrimitive.matcher(arg).matches();
    }

    @Deprecated
    public static boolean matchesDuration(String arg) {
        arg = arg.replace("duration:", "");
        return Duration.matches(arg);
    }

    public static boolean matchesEntityType(String arg) {
        arg = arg.replace("duration:", "");

        // Check against valid EntityTypes using Bukkit's EntityType enum
        for (EntityType validEntity : EntityType.values())
            if (arg.equalsIgnoreCase(validEntity.name()))
                return true;
        return false;
    }

    public static boolean matchesInteger(String arg) {
        return integerPrimitive.matcher(arg).matches();
    }

    @Deprecated
    public static boolean matchesItem(String arg) {
        if (arg.length() > 5 && arg.toUpperCase().startsWith("ITEM:"))
            return true;
        // TODO: Other matches____ do some actual checks.
        return false;
    }

    @Deprecated
    public static boolean matchesContext(String arg) {
        if (arg.toUpperCase().startsWith("CONTEXT:")) return true;
        // TODO: Other matches____ do some actual checks, should this?.
        return false;
    }

    @Deprecated
    public static Map<String, String> getContextFrom(String arg) {
        Map<String, String> context = new HashMap<String, String>();
        int x = 1;
        for (String ctxt : aH.getListFrom(arg)) {
            context.put(String.valueOf(x), ctxt.trim());
            x++;
        }
        return context;
    }

    @Deprecated
    public static boolean matchesLocation(String arg) {
        if (arg.length() > 8 && arg.toUpperCase().startsWith("LOCATION:"))
            return true;
        return false;
    }

    @Deprecated
    public static boolean matchesQuantity(String arg) {
        if (arg.length() > 4 && arg.toUpperCase().startsWith("QTY:"))
            return true;
        return false;
    }

    @Deprecated
    public static boolean matchesQueue(String arg) {
        if (arg.length() > 6 && arg.toUpperCase().startsWith("QUEUE:"))
            return true;
        return false;
    }

    @Deprecated
    public static boolean matchesScript(String arg) {
        Matcher m = matchesScriptPtrn.matcher(arg);
        if (m.matches()) {
            if (ScriptRegistry.containsScript(m.group(1)))
                return true;
            else {
                dB.echoError("While parsing '" + arg + "', Denizen has run into a problem. This " +
                        "argument's format is correct, but Denizen couldn't locate a script " +
                        "named '" + m.group(1) + "'. Is it spelled correctly?");
            }
        }
        return false;
    }

    @Deprecated
    public static boolean matchesToggle(String arg) {
        final Pattern m = Pattern.compile("toggle:true|false|toggle");
        if (m.matcher(arg).matches()) return true;
        else if (arg.toUpperCase().startsWith("toggle:"))
            dB.echoError("While parsing '" + arg + "', Denizen has run into a problem. While the prefix is " +
                    "correct, the value is not valid. 'TOGGLE' requires a value of TRUE, FALSE, or TOGGLE. ");

        return false;
    }

    final static Pattern matchesTogglePtrn = Pattern.compile("toggle:true|false|toggle", Pattern.CASE_INSENSITIVE);
    final static Pattern matchesScriptPtrn = Pattern.compile("script:(.+)", Pattern.CASE_INSENSITIVE);










}
