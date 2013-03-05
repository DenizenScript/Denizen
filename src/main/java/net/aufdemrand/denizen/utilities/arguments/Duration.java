package net.aufdemrand.denizen.utilities.arguments;

import com.google.common.primitives.Ints;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Duration implements dScriptArgument {

    final static Pattern matchesDurationPtrn = Pattern.compile("(?:.+:|)(\\d+|\\.\\d+|\\d+\\.\\d+)(|t|m|s|h|d)", Pattern.CASE_INSENSITIVE);

    /**
     * Gets a Duration Object from a dScript argument. Durations must be a positive
     * number. Can specify the unit of time by using one of the following: T=ticks, M=minutes,
     * S=seconds, H=hours, D=days. Not using a unit will imply seconds. Examples: 10s, 50m, 1d, 50.
     * Can also include a prefix, though it will be ignored. Example: duration:10, time:30m.
     *
     * @param string  the dScript argument String
     * @return  a Script, or null if incorrectly formatted
     */
    public static Duration valueOf(String string) {
        if (string == null) return null;

        Matcher m = matchesDurationPtrn.matcher(string);
        if (m.matches()) {
            if (m.group().toUpperCase().endsWith("T"))
                // Matches TICKS, so 1 tick = .05 seconds
                return new Duration(Double.valueOf(m.group(1)) * 0.05);

            else if (m.group().toUpperCase().endsWith("D"))
                // Matches DAYS, so 1 day = 86400 seconds
                return new Duration(Double.valueOf(m.group(1)) * 86400);

            else if (m.group().toUpperCase().endsWith("M"))
                // Matches MINUTES, so 1 minute = 60 seconds
                return new Duration(Double.valueOf(m.group(1)) * 60);

            else if (m.group().toUpperCase().endsWith("H"))
                // Matches HOURS, so 1 hour = 3600 seconds
                return new Duration(Double.valueOf(m.group(1)) * 3600);

            else // seconds
                return new Duration(Double.valueOf(m.group(1)));
        }

        return null;
    }


    private double seconds;
    private String prefix = "Duration";

    /**
     * Creates a duration object when given number of seconds.
     *
     * @param seconds  number of seconds
     */
    public Duration(double seconds) {
        this.seconds = seconds;
        if (this.seconds < 0) this.seconds = 0;
    }

    /**
     * Creates a duration object when given number of seconds.
     *
     * @param seconds  number of seconds
     */
    public Duration(int seconds) {
        this.seconds = seconds;
        if (this.seconds < 0) this.seconds = 0;
    }

    /**
     * Creates a duration object when given number of Bukkit ticks.
     *
     * @param ticks  number of ticks
     */
    public Duration (long ticks) {
        this.seconds = ticks / 20;
        if (this.seconds < 0) this.seconds = 0;
    }

    /**
     * Gets the number of ticks of this duration.
     *
     * @return  number of ticks
     */
    public long getTicks() {
        return (long) (seconds * 20);
    }

    /**
     * Gets the number of ticks of this duration.
     *
     * @return  number of ticks
     */
    public int getTicksAsInt() {
        return Ints.checkedCast((long) (seconds * 20));
    }

    public long getMillis() {
        return (long) (seconds * 1000);
    }

    /**
     * Gets the number of seconds of this duration.
     *
     * @return  number of seconds
     */
    public double getSeconds() {
        return seconds;
    }

    /**
     * Gets the number of seconds as an integer value of the duration.
     *
     * @return  number of seconds rounded to the nearest second
     */
    public int getSecondsAsInt() {
        // Durations that are a fraction of a second
        // will return as 1 when using this method.
        if (seconds < 1 && seconds > 0) return 1;
        return Ints.checkedCast(Math.round(seconds));
    }

    @Override
    public String getDefaultPrefix() {
       return prefix;
    }

    @Override
    public String debug() {
        return "<G>" + prefix + "='<Y>" + seconds + " seconds<G>'  ";
    }

    @Override
    public String as_dScriptArg() {
        return prefix + ":" + seconds;
    }

    public String dScriptArgValue() {
        return String.valueOf(seconds);
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(String attribute) {

        if (attribute == null) return as_dScriptArg();

        // Desensitize the attribute for comparison
        attribute = attribute.toLowerCase();

        if (attribute.startsWith("in_seconds.asint")) {
            return String.valueOf(Double.valueOf(seconds).intValue());
        }

        // We're still working with JRE6, so no String 'switches'.
        if (attribute.startsWith("in_seconds")) {
            return String.valueOf(seconds);
        }





        return as_dScriptArg();
    }

}
