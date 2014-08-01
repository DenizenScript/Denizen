package net.aufdemrand.denizen.objects;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.ChatColor;

import com.google.common.primitives.Ints;


/**
 * Durations are a convenient way to get a 'unit of time' within Denizen.
 *
 * @version 1.0
 * @author Jeremy Schroeder
 *
 */
public class Duration implements dObject {

    // <--[language]
    // @name Duration
    // @group Object System
    // @description
    // Durations are a unified and convenient way to get a 'unit of time' throughout Denizen.
    // Many commands and features that require a duration can be satisfied by specifying a number
    // and unit of time, especially command arguments that are prefixed 'duration:', etc. The d@
    // object fetcher notation can also be used, and is encouraged. The unit of time can be specified
    // by using one of the following: T=ticks, M=minutes, S=seconds, H=hours, D=days, W = Weeks.
    // Not using a unit will imply seconds. Examples: d@10s, d@50m, d@1d, d@20.
    //
    // Specifying a range of duration will result in a randomly selected duration that is
    // in between the range specified. The smaller value should be first. Examples:
    // d@10s-25s, d@1m-2m.
    //
    // See 'd@duration' tags for an explanation of duration attributes.
    // -->

    // <--[language]
    // @name Tick
    // @description
    // A 'tick' is usually referred to as 1/20th of a second, the speed at which CraftBukkit updates
    // its various server events.
    // -->


    /////////////////////
    //   STATIC METHODS AND FIELDS
    /////////////////

    // Use regex pattern matching to easily determine if a string
    // value is a valid Duration.
    final static Pattern match =
            Pattern.compile("(?:d@)?(\\d+.\\d+|.\\d+|\\d+)(t|m|s|h|d|w|)" +
                    // Optional 'high-range' for random durations.
                    "(?:(?:-\\d+.\\d+|.\\d+|\\d+)(?:t|m|s|h|d|w|))?",
                    Pattern.CASE_INSENSITIVE);


    // Define a 'ZERO' Duration
    final public static Duration ZERO = new Duration(0);


    /////////////////////
    //   OBJECT FETCHER
    /////////////////

    // <--[language]
    // @name d@
    // @group Object Fetcher System
    // @description
    // d@ refers to the 'object identifier' of a 'Duration'. The 'd@' is notation for Denizen's Object
    // Fetcher. Durations must be a positive number or range of numbers followed optionally by
    // a unit of time, and prefixed by d@. Examples: d@3s, d@1d, d@10s-20s.
    //
    // See also 'Duration'
    // -->

    /**
     * Gets a Duration Object from a dScript argument. Durations must be a positive
     * number. Can specify the unit of time by using one of the following: T=ticks, M=minutes,
     * S=seconds, H=hours, D=days. Not using a unit will imply seconds. Examples: 10s, 50m, 1d, 50.
     *
     * @param string  the Argument value.
     * @return  a Duration, or null if incorrectly formatted.
     */
    @Fetchable("d")
    public static Duration valueOf(String string) {
        if (string == null) return null;

        string = string.replace("d@", "");

        // Pick a duration between a high and low number if there is a '-' present.
        String[] split = string.split("-", 2);
        if (split.length == 2
                && Duration.matches(split[0])
                && Duration.matches(split[1])) {
            Duration low = Duration.valueOf(split[0]);
            Duration high = Duration.valueOf(split[1]);

            // Make sure 'low' and 'high' returned valid Durations,
            // and that 'low' is less time than 'high'.
            if (low != null && high != null
                    && low.getSecondsAsInt() < high.getSecondsAsInt()) {
                int seconds = Utilities.getRandom()
                        .nextInt((high.getSecondsAsInt() - low.getSecondsAsInt() + 1))
                                + low.getSecondsAsInt();
                // dB.log("Getting random duration between " + low.identify()
                //        + " and " + high.identify() + "... " + seconds + "s");

                return new Duration(seconds);

            } else return null;
        }

        // Standard Duration. Check the type and create new Duration object accordingly.
        Matcher m = match.matcher(string);
        if (m.matches()) {
            if (m.group().toLowerCase().endsWith("t"))
                // Matches TICKS, so 1 tick = .05 seconds
                return new Duration(Double.valueOf(m.group(1)) * 0.05);

            else if (m.group().toLowerCase().endsWith("d"))
                // Matches DAYS, so 1 day = 86400 seconds
                return new Duration(Double.valueOf(m.group(1)) * 86400);

            else if (m.group().toLowerCase().endsWith("w"))
                // Matches WEEKS, so 1 week = 604800 seconds
                return new Duration(Double.valueOf(m.group(1)) * 604800);

            else if (m.group().toLowerCase().endsWith("m"))
                // Matches MINUTES, so 1 minute = 60 seconds
                return new Duration(Double.valueOf(m.group(1)) * 60);

            else if (m.group().toLowerCase().endsWith("h"))
                // Matches HOURS, so 1 hour = 3600 seconds
                return new Duration(Double.valueOf(m.group(1)) * 3600);

            else // seconds
                return new Duration(Double.valueOf(m.group(1)));
        }

        return null;
    }


    /**
     * Checks to see if the string is a valid Duration.
     *
     * @param string  the String to match.
     * @return  true if valid.
     */
    public static boolean matches(String string) {
        Matcher m = match.matcher(string);
        return m.matches();
    }


    /////////////////////
    //   CONSTRUCTORS
    /////////////////

    /**
     * Creates a duration object when given number of seconds.
     *
     * @param seconds  the number of seconds.
     */
    public Duration(double seconds) {
        this.seconds = seconds;
        if (this.seconds < 0) this.seconds = 0;
    }

    /**
     * Creates a duration object when given number of seconds.
     *
     * @param seconds  the number of seconds.
     */
    public Duration(int seconds) {
        this.seconds = seconds;
        if (this.seconds < 0) this.seconds = 0;
    }

    /**
     * Creates a duration object when given number of Bukkit ticks.
     *
     * @param ticks  the number of ticks.
     */
    public Duration (long ticks) {
        this.seconds = ticks / 20;
        if (this.seconds < 0) this.seconds = 0;
    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////


    // The amount of seconds in the duration.
    private double seconds;


    // Duration's default dObject prefix.
    private String prefix = "Duration";


    /**
     * Gets the number of ticks of this duration. There are 20 ticks
     * per second.
     *
     * @return  the number of ticks.
     */
    public long getTicks() {
        return (long) (seconds * 20);
    }


    /**
     * Gets the number of ticks of this duration as an integer. There are
     * 20 per second.
     *
     * @return  the number of ticks.
     */
    public int getTicksAsInt() {
        return Ints.checkedCast((long) (seconds * 20));
    }


    /**
     * Gets the number of milliseconds in this duration.
     *
     * @return  the number of milliseconds.
     */
    public long getMillis() {
        Double millis = seconds * 1000;
        return millis.longValue();
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
        return round(seconds);
    }

    private int round(double d){
        double dAbs = Math.abs(d);
        int i = (int) dAbs;
        double result = dAbs - i;
        if(result<0.5){
            return d<0 ? -i : i;
        }else{
            return d<0 ? -(i+1) : i+1;
        }
    }


    /////////////////////
    //   dObject Methods
    /////////////////

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return ChatColor.DARK_GRAY +  prefix + "='"
                + ChatColor.YELLOW + identify()
                + ChatColor.DARK_GRAY + "'  ";
    }

    @Override
    public boolean isUnique() {
        // Durations are not unique, cannot be saved or persisted.
        return false;
    }

    @Override
    public String getObjectType() {
        return "duration";
    }

    /**
     * Return the value of this Duration. This will also return a
     * valid String that can be re-interpreted with Duration.valueOf()
     * thus acting as a form of 'serialization/deserialization'.
     *
     * @return  a valid String-form Duration.
     */
    @Override
    public String identify() {
        return getTicks() + "t";
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    /**
     * Acts just like identify().
     *
     * @return  a valid String-form Duration.
     */
    @Override
    public String toString() {
        return identify();
    }

    @Override
    public dObject setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }


    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;


        /////////////////////
        //   CONVERSION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <d@duration.in_weeks>
        // @returns Element(Decimal)
        // @description
        // returns the number of days in the Duration.
        // -->
        if (attribute.startsWith("in_weeks") || attribute.startsWith("weeks"))
            return new Element(seconds / 604800)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <d@duration.in_days>
        // @returns Element(Decimal)
        // @description
        // returns the number of days in the Duration.
        // -->
        if (attribute.startsWith("in_days") || attribute.startsWith("days"))
            return new Element(seconds / 86400)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <d@duration.in_hours>
        // @returns Element(Decimal)
        // @description
        // returns the number of hours in the Duration.
        // -->
        if (attribute.startsWith("in_hours") || attribute.startsWith("hours"))
            return new Element(seconds / 3600)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <d@duration.in_minutes>
        // @returns Element(Decimal)
        // @description
        // returns the number of minutes in the Duration.
        // -->
        if (attribute.startsWith("in_minutes") || attribute.startsWith("minutes"))
            return new Element(seconds / 60)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <d@duration.in_seconds>
        // @returns Element(Decimal)
        // @description
        // returns the number of seconds in the Duration.
        // -->
        if (attribute.startsWith("in_seconds") || attribute.startsWith("seconds"))
            return new Element(seconds)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <d@duration.in_milliseconds>
        // @returns Element(Decimal)
        // @description
        // returns the number of milliseconds in the Duration.
        // -->
        if (attribute.startsWith("in_milliseconds") || attribute.startsWith("milliseconds"))
            return new Element(seconds * 1000)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <d@duration.in_ticks>
        // @returns Element(Number)
        // @description
        // returns the number of ticks in the Duration. (20t/second)
        // -->
        if (attribute.startsWith("in_ticks") || attribute.startsWith("ticks"))
            return new Element(getTicks())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <d@duration.time>
        // @returns Element(Number)
        // @description
        // returns the date-time specified by the duration object.
        // -->
        if (attribute.startsWith("time")) {

            Date currentDate = new Date(getTicks() * 50);
            SimpleDateFormat format = new SimpleDateFormat();

            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <d@duration.time.year>
            // @returns Element(Number)
            // @description
            // Returns the current year of the time specified by the duration object.
            // -->
            if (attribute.startsWith("year"))
                return new Element(currentDate.getYear()).getAttribute(attribute.fulfill(1));

                // <--[tag]
                // @attribute <d@duration.time.month>
                // @returns Element(Number)
                // @description
                // Returns the current month of the time specified by the duration object.
                // -->
            else if (attribute.startsWith("month"))
                return new Element(currentDate.getMonth() + 1).getAttribute(attribute.fulfill(1));

                // <--[tag]
                // @attribute <d@duration.time.day>
                // @returns Element(Number)
                // @description
                // Returns the current day of the time specified by the duration object.
                // -->
            else if (attribute.startsWith("day"))
                return new Element(currentDate.getDate()).getAttribute(attribute.fulfill(1));

                // <--[tag]
                // @attribute <d@duration.time.hour>
                // @returns Element(Number)
                // @description
                // Returns the current hour of the time specified by the duration object.
                // -->
            else if (attribute.startsWith("hour"))
                return new Element(currentDate.getHours()).getAttribute(attribute.fulfill(1));

                // <--[tag]
                // @attribute <d@duration.time.minute>
                // @returns Element(Number)
                // @description
                // Returns the current minute of the time specified by the duration object.
                // -->
            else if (attribute.startsWith("minute"))
                return new Element(currentDate.getMinutes()).getAttribute(attribute.fulfill(1));

                // <--[tag]
                // @attribute <d@duration.time.second>
                // @returns Element(Number)
                // @description
                // Returns the current second of the time specified by the duration object.
                // -->
            else if (attribute.startsWith("second"))
                return new Element(currentDate.getSeconds()).getAttribute(attribute.fulfill(1));

            else {
                format.applyPattern("EEE, d MMM yyyy HH:mm:ss");
                return new Element(format.format(currentDate))
                        .getAttribute(attribute);
            }
        }

        /////////////////////
        //   DEBUG ATTRIBUTES
        /////////////////

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


        /////////////////////
        //   FORMAT ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <d@duration.formatted>
        // @returns Element
        // @description
        // returns the value of the duration in an easily readable
        // format like 2h 30m, where minutes are only shown if there
        // is less than a day left and seconds are only shown if
        // there are less than 10 minutes left.
        // -->
        if (attribute.startsWith("formatted") || attribute.startsWith("value")) {

            // Make sure you don't change these longs into doubles
            // and break the code

            long seconds = (long) this.seconds;
            long days = seconds / 86400;
            long hours = (seconds - days * 86400) / 3600;
            long minutes = (seconds - days * 86400 - hours * 3600) / 60;
            seconds = seconds - days * 86400 - hours * 3600 - minutes * 60;

            String timeString = "";

            if (days > 0)
                timeString = String.valueOf(days) + "d ";
            if (hours > 0)
                timeString = timeString + String.valueOf(hours) + "h ";
            if (minutes > 0 && days == 0)
                timeString = timeString + String.valueOf(minutes) + "m ";
            if (seconds > 0 && minutes < 10 && hours == 0 && days == 0)
                timeString = timeString + String.valueOf(seconds) + "s";

            if (timeString.isEmpty())
                timeString = "forever";

            return new Element(timeString.trim())
                        .getAttribute(attribute.fulfill(1));
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }
}
