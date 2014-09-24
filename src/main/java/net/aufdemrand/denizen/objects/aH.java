package net.aufdemrand.denizen.objects;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;


/**
 * The dScript Argument Helper will aid you in parsing and formatting arguments from a
 * dScript argument string (such as those found in a ScriptEntry.getArguments() method).
 *
 * @author aufdemrand
 *
 */
public class aH {


    ////////////////////
    // Patterns and Enumerations
    /////////////////

    public enum PrimitiveType { Float, Double, Integer, Boolean, String, Word, Percentage }

    final static Pattern floatPrimitive =
            Pattern.compile("^[-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?$");

    // <--[language]
    // @name number
    // @description
    // Many arguments in Denizen require the use of a 'number', or 'double'. Sometimes referred to as #.# or <number>,
    // this kind of hint can generally be filled with any reasonable positive or negative number with or without a
    // decimal point. Numbers can be verified with the 'if' commands' 'matches' functionality.
    // For example: - if <number> matches double ... will return true if <number> is a valid number.
    //
    // Denizen uses the regular expression pattern -?(?:\d+)?(\.\d+)? for number matching.
    // -->
    final static Pattern doublePrimitive =
            Pattern.compile("-?(?:\\d+)?(\\.\\d+)?");

    // <--[language]
    // @name percentage
    // @description
    // Promotes the usage of a 'percentage' format to be used in applicable arguments. The 'percentage' in Denizen is
    // much like the 'number', except arguments which utilize percentages instead of numbers can also include a %.
    // Percentage arguments can generally be filled with any reasonable positive or negative number with or without a
    // decimal point and/or percentage sign. Argument hints and other usages will typically refer to a percentage as
    // #.#% or <percentage>. Percentages can be verified with the 'if' commands' 'matches' functionality.
    // For example: - if <percentage> matches percentage ... will return true if <percentage> is a valid percentage.
    //
    // Denizen uses the regular expression pattern -?(?:\d+)?(\.\d+)?(%)? for percentage matching.
    // -->
    final static Pattern percentagePrimitive =
            Pattern.compile("-?(?:\\d+)?(\\.\\d+)?(%)?");

    final static Pattern integerPrimitive =
            Pattern.compile("(-)?\\d+");

    final static Pattern booleanPrimitive =
            Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

    final static Pattern wordPrimitive =
            Pattern.compile("\\w+");


    ////////////////////
    // Argument Object
    //////////////////

    public static class Argument {

        public String raw_value;
        String prefix = null;
        String lower_prefix = null;
        String value;
        String lower_value;
        boolean has_prefix = false;

        // Construction
        public Argument(String string) {
            raw_value = string;
            string = string.trim();

            int first_colon = string.indexOf(':');
            int first_space = string.indexOf(' ');

            if ((first_space > -1 && first_space < first_colon) || first_colon == -1)  {
                value = string;
                lower_value = string.toLowerCase();
            }
            else {
                has_prefix = true;
                String[] split = StringUtils.split(string, ":", 2);
                prefix = split[0];
                lower_prefix = prefix.toLowerCase();
                if (split.length == 2)
                    value = split[1];
                else
                    value = "";
                lower_value = value.toLowerCase();
            }

        }


        public static Argument valueOf(String string) {
            return new Argument(string);
        }


        public boolean startsWith(String string) {
            return lower_value.startsWith(string.toLowerCase());
        }


        public boolean hasPrefix() {
            return has_prefix;
        }


        public Argument getPrefix() {
            if (prefix == null)
                return null;
            return valueOf(prefix);
        }


        public boolean matches(String values) {
            for (String value : StringUtils.split(values, ',')) {
                if (value.trim().toLowerCase().equals(lower_value))
                    return true;
            }
            return false;
        }

        public boolean matches(String... values) {
            for (String value : values) {
                if (value.toLowerCase().equals(lower_value))
                    return true;
            }
            return false;
        }


        public void replaceValue(String string) {
            value = string;
            lower_value = value.toLowerCase();
        }


        public String getValue() {
            return value;
        }


        public boolean matchesEnum(Enum<?>[] values) {
            for (Enum<?> value : values)
                if (value.name().replace("_", "").equalsIgnoreCase(this.value.replace("_", "")))
                    return true;

            return false;
        }


        // Check if this argument matches a dList of Enum values
        public boolean matchesEnumList(Enum<?>[] values) {
            dList list = dList.valueOf(this.value);

            for (String string : list) {
                for (Enum<?> value : values)
                    if (value.name().replace("_", "").equalsIgnoreCase(string.replace("_", "")))
                        return true;
            }

            return false;
        }


        public boolean matchesPrefix(String values) {
            if (!hasPrefix()) return false;
            for (String value : StringUtils.split(values, ',')) {
                if (value.trim().toLowerCase().equals(lower_prefix))
                    return true;
            }
            return false;
        }

        public boolean matchesPrefix(String... values) {
            if (!hasPrefix()) return false;
            for (String value : values) {
                if (value.toLowerCase().equals(lower_prefix))
                    return true;
            }
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

                case Percentage:
                    return percentagePrimitive.matcher(value).matches();

                case String:
                    return true;
            }

            return false;
        }


        // Check if this argument matches a certain dObject type
        public boolean matchesArgumentType(Class<? extends dObject> dClass) {
            return ObjectFetcher.checkMatch(dClass, value);
        }


        // Check if this argument matches any of multiple dObject types
        public boolean matchesArgumentTypes(Class<? extends dObject>... dClasses) {

            for (Class<? extends dObject> c : dClasses) {
                if (matchesArgumentType(c)) {
                    return true;
                }
            }

            return false;
        }


        // Check if this argument matches a dList of a certain dObject
        public boolean matchesArgumentList(Class<? extends dObject> dClass) {

            dList list = new dList(this.value.replace("li@", ""));

            return list.containsObjectsFrom(dClass) || list.isEmpty();
        }


        public Element asElement() {
            return new Element(prefix, value);
        }


        public <T extends dObject> T asType(Class<T> clazz) {
            dObject arg = ObjectFetcher.getObjectFrom(clazz, value);
            if (arg != null) {
                arg.setPrefix(prefix);
                return clazz.cast(arg);
            }
            return null;
        }


        public void reportUnhandled() {
            dB.echoError('\'' + raw_value + "' is an unknown argument!");
        }


        @Override
        public String toString() {
            return raw_value;
        }
    }

    /////////////////
    // Static Methods
    ///////////////


    /**
     * Turns a list of string arguments (separated by buildArgs) into Argument
     * Objects for easy matching and dObject creation throughout Denizen.
     *
     * @param args  a list of string arguments
     * @return  a list of Arguments
     */
    public static List<Argument> interpret(List<String> args) {
        List<Argument> arg_list = new ArrayList<Argument>();
        for (String string : args) {
            arg_list.add(new Argument(string.trim()));
        }
        return arg_list;
    }

    private static final Pattern argsRegex = Pattern.compile("[^\\s\"'¨]+|\"([^\"]*)\"|'([^']*)'|¨([^¨]*)¨");


    /**
     * Builds an arguments array, recognizing items in quotes as a single item, but
     * otherwise splitting on a space.
     *
     * @param stringArgs  the line of arguments that need split
     * @return  an array of arguments
     *
     */
    public static String[] buildArgs(String stringArgs) {
        if (stringArgs == null) return null;
        java.util.List<String> matchList = new ArrayList<String>();
        Matcher regexMatcher = argsRegex.matcher(stringArgs);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                matchList.add(regexMatcher.group(2));
            } else {
                matchList.add(regexMatcher.group());
            }
        }

        if (dB.showScriptBuilder)
            dB.log(ChatColor.GRAY + "Constructed args: " + Arrays.toString(matchList.toArray()));

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

    public static <T extends dObject> String debugList(String prefix, Collection<T> objects) {
        if (objects == null)
            return debugObj(prefix, null);
        StringBuilder sb = new StringBuilder();
        for (dObject obj: objects) {
            String output = obj.debug();
            sb.append(output.substring((obj.getPrefix() + "='<A>").length(), output.length() - 6)).append(", ");
        }
        if (sb.length() == 0)
            return debugObj(prefix, sb);
        else
            return debugObj(prefix, "[" + sb.substring(0, sb.length() - 2) + "]");
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
     * <tt>4        dB.echoError("Invalid HARDNESS!") </tt><br>
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

            case Custom:
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
        arg = arg.toLowerCase().replace("entity:", "");
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
        arg = arg.toLowerCase().replace("item:", "");
        return dItem.valueOf(arg);
    }

    @Deprecated
    public static dList getListFrom(String arg) {
        return dList.valueOf(aH.getStringFrom(arg));
    }

    @Deprecated
    public static dLocation getLocationFrom(String arg) {
        arg = arg.toLowerCase().replace("location:", "");
        return dLocation.valueOf(arg);
    }

    public static long getLongFrom(String arg) {
        try {
            return Long.valueOf(getStringFrom(arg));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Deprecated
    public static dScript getScriptFrom(String arg) {
        arg = arg.toLowerCase().replace("script:", "");
        return dScript.valueOf(arg);
    }

    @Deprecated
    public static dPlayer getPlayerFrom(String arg) {
        return dPlayer.valueOf(aH.getStringFrom(arg));
    }

    @Deprecated
    public static dNPC getNPCFrom(String arg) {
        return dNPC.valueOf(aH.getStringFrom(arg));
    }

    public static String getStringFrom(String arg) {
        String[] parts = arg.split(":", 2);
        return parts.length >=2 ? parts[1] : arg;
    }

    @Deprecated
    public static Duration getDurationFrom(String arg) {
        arg = arg.toLowerCase().replace("duration:", "").replace("delay:", "");
        return Duration.valueOf(arg);
    }

    public static boolean matchesDouble(String arg) {
        return doublePrimitive.matcher(arg).matches();
    }

    @Deprecated
    public static boolean matchesDuration(String arg) {
        arg = arg.toLowerCase().replace("duration:", "").replace("delay:", "");
        return Duration.matches(arg);
    }

    public static boolean matchesEntityType(String arg) {
        arg = arg.toLowerCase().replace("entity:", "");

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
        return false;
    }

    @Deprecated
    public static boolean matchesContext(String arg) {
        if (arg.toUpperCase().startsWith("CONTEXT:") ||
                arg.toUpperCase().startsWith("DEFINE:")) return true;
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
        return arg.toUpperCase().startsWith("LOCATION:");
    }

    @Deprecated
    public static boolean matchesQuantity(String arg) {
        return arg.toUpperCase().startsWith("QTY:");
    }

    @Deprecated
    public static boolean matchesQueue(String arg) {
        return arg.toUpperCase().startsWith("QUEUE:");
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
    public static boolean matchesState(String arg) {
        final Pattern m = Pattern.compile("(state|toggle):(true|false|toggle)");
        if (m.matcher(arg).matches()) return true;
        else if (arg.toUpperCase().startsWith("(state|toggle):"))
            dB.echoError("While parsing '" + arg + "', Denizen has run into a problem. While the prefix is " +
                    "correct, the value is not valid. 'STATE' requires a value of TRUE, FALSE, or TOGGLE. ");

        return false;
    }

    final static Pattern matchesScriptPtrn = Pattern.compile("script:(.+)", Pattern.CASE_INSENSITIVE);
}
