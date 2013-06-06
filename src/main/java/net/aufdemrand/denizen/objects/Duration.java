package net.aufdemrand.denizen.objects;

import com.google.common.primitives.Ints;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Duration implements dObject {

    final static Pattern match =
            Pattern.compile("(\\d+.\\d+|.\\d+|\\d+)(t|m|s|h|d|)",
                    Pattern.CASE_INSENSITIVE);

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

        Matcher m = match.matcher(string);
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


    public static boolean matches(String string) {
        Matcher m = match.matcher(string);
        if (m.matches()) return true;

        return false;
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
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return ChatColor.DARK_GRAY +  prefix + "='" + ChatColor.YELLOW + seconds + " seconds" + ChatColor.DARK_GRAY + "'  ";
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getType() {
        return "duration";
    }

    @Override
    public String identify() {
        double seconds = getTicks() / 20;
        double days = seconds / 86400;
        double hours = (seconds - days * 86400) / 3600;
        double minutes = (seconds - days * 86400 - hours * 3600) / 60;
        seconds = seconds - days * 86400 - hours * 3600 - minutes * 60;

        String timeString = "";

        if (days > 0)
            timeString = String.valueOf(days) + "d ";
        else if (hours > 0)
            timeString = timeString + String.valueOf(hours) + "h ";
        else if (minutes > 0 && days == 0)
            timeString = timeString + String.valueOf(minutes) + "m ";
        else if (seconds > 0 && minutes < 10 && hours == 0 && days == 0)
            timeString = timeString + String.valueOf(seconds) + "s";

        return timeString;
    }

    @Override
    public dObject setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        if (attribute.startsWith("in_seconds"))
            return new Element(String.valueOf(seconds))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("in_hours"))
            return new Element(String.valueOf(seconds / 1800))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("in_minutes"))
            return new Element(String.valueOf(seconds / 60))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("in_ticks"))
            return new Element(String.valueOf(getTicksAsInt()))
                    .getAttribute(attribute.fulfill(1));

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

        if (attribute.startsWith("value")) {
            if (seconds % 43200 == 0)
                return new Element(seconds / 86400 + "d")
                        .getAttribute(attribute.fulfill(1));
            else if (seconds % 1800 == 0)
                return new Element(seconds / 3600 + "h")
                        .getAttribute(attribute.fulfill(1));
            else if (seconds % 30 == 0)
                return new Element(seconds / 60 + "m")
                        .getAttribute(attribute.fulfill(1));
            else return new Element(seconds + "s")
                        .getAttribute(attribute.fulfill(1));
        }

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }


}
