package net.aufdemrand.denizen.flags;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.commands.core.FlagCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.ArrayList;
import java.util.List;

public class FlagManager {

    private Denizen denizen;

    public FlagManager(Denizen denizen) {
        this.denizen = denizen;
    }
    
    /**
     * When given a FlagType and necessary information, this returns a Flag object.
     * When getting a FlagType.GLOBAL, targetName/npcid are not necessary and can be 
     * null. When getting a FlagType.PLAYER, npcid is not necessary and can be null. When
     * getting a FlagType.NPC, targetName is not necessary, as NPC flags
     * require an npcid instead. If it isn't obvious, flagType and flagName are required.
     * If this flag currently exists it will be populated with the current values. If 
     * the flag does NOT exist, it will be created with blank values.
     * 
     * If getting a known specific FlagType, getPlayerFlag(..), getDenizenFlag(..),
     * or getGlobalFlag(..) may be a cleaner method of retrieval.
     *   
     */
    public Flag getFlag(FlagCommand.Type flagType, String targetName, Integer npcid, String flagName) {
        if (flagType == FlagCommand.Type.GLOBAL)
            return new Flag("Global.Flags." + flagName.toUpperCase());
        else if (flagType == FlagCommand.Type.PLAYER)
            return new Flag("Players." + targetName + ".Flags." + flagName.toUpperCase());
        else if (flagType == FlagCommand.Type.NPC)
            return new Flag("NPCs." + npcid + ".Flags." + flagName.toUpperCase());

        return new Flag("Global.Flags.null");
    }

    /**
     * Returns a NPC Flag object. If this flag currently exists
     * it will be populated with the current values. If the flag does NOT exist,
     * it will be created with blank values.
     * 
     */
    public Flag getNPCFlag(int npcid, String flagName) {
        return new Flag("NPCs." + npcid + ".Flags." + flagName.toUpperCase());
    }

    /**
     * Returns a Global Flag object. If this flag currently exists
     * it will be populated with the current values. If the flag does NOT exist,
     * it will be created with blank values.
     * 
     */
    public Flag getGlobalFlag(String flagName) {
        return new Flag("Global.Flags." + flagName.toUpperCase()); 
    }

    /**
     * Returns a Flag Object tied to a Player. If this flag currently exists
     * it will be populated with the current values. If the flag does NOT exist,
     * it will be created with blank values.
     * 
     */
    public Flag getPlayerFlag(String playerName, String flagName) {
        return new Flag("Players." + playerName + ".Flags." + flagName.toUpperCase());
    }


    /**
     * Flag object contains methods for working with Flags and contain a list
     * of the values associated with said flag (if existing) and (optionally) an 
     * expiration (if existing).
     * 
     * Storage example in Denizen saves.yml:
     * 
     * 'FLAG_NAME':
     * - First Value
     * - Second Value
     * - Third Value
     * - ...
     * 'FLAG_NAME-expiration': 123456789
     * 
     * To work with multiple values in a flag, an index must be provided. Indexes
     * start at 1 and get higher as more items are added. Specifying an index of -1, or,
     * when possible, supplying NO index will result in retrieving/setting/etc the
     * item with the highest index. Also, note that when using a FLAG TAG in DSCRIPT,
     * ie. <FLAG.P:FLAG_NAME>, specifying no index will follow suit, that is, the 
     * value with the highest index will be referenced.
     * 
     */
    public class Flag {
        private Value value;
        private String flagPath;
        private long expiration = (long) -1;

        Flag(String flagPath) {
            this.flagPath = flagPath;
            rebuild();
        }

        /**
         * Returns whether or not a value currently exists in the Flag. The
         * provided value will check if it matches an existing value by means
         * of a String.equalsIgnoreCase as well as a Double match if the
         * provided value is a number.
         * 
         */
        public boolean contains(String stringValue) {
            checkExpired();
            for (String val : value.values) {
                if (val.equalsIgnoreCase(stringValue)) return true;
                try {
                    if (Double.valueOf(val).equals(Double.valueOf(stringValue))) return true;
                } catch (Exception e) { /* Not a valid number, continue. */ } 
            }

            return false;
        }
        
        /**
         * Gets all values currently stored in the flag.
         * 
         */
        public List<String> values() {
            checkExpired();
            return value.values;
        }

        /**
         * Gets a specific value stored in a flag when given an index.
         * 
         */
        public Value get(int index) {
            checkExpired();
            return value.get(index);
        }

        /**
         * Clears all values from a flag, essentially making it null.
         *
         */
        public void clear() {
            denizen.getSaves().set(flagPath, null);
            denizen.getSaves().set(flagPath + "-expiration", null);
            denizen.saveSaves();
            rebuild();
        }

        /**
         * Gets the first value stored in the Flag.
         * 
         */
        public Value getFirst() {
            checkExpired();
            return value.get(1);
        }

        /**
         * Gets the last value stored in the Flag.
         * 
         */
        public Value getLast() {
            checkExpired();
            return value.get(value.size());
        }

        /**
         * Sets the value of the most recent value added to the flag. This does
         * not create a new value unless the flag is currently empty of values. 
         * 
         */
        public void set(Object obj) {
            set(obj, -1);
        }

        /**
         * Sets a specific value in the flag. Adds the value to the flag if
         * the index doesn't exist. If the index is less than 0, it instead
         * works with the most recent value added to the flag. If the flag is
         * currently empty, the value is added.
         * 
         */
        public void set(Object obj, int index) {
            checkExpired();

            // No index? Work with last item in the Flag.
            if (index < 0) index = size();

            if (size() == 0) value.values.add((String) obj);
            else if (index > 0) {
                if (value.values.size() > index - 1) {
                    value.values.remove(index - 1);
                    value.values.add(index - 1, (String) obj);

                    // Index higher than currently exists? Add the item to the end of the list.
                } else value.values.add((String) obj);
            }
            save();
            rebuild();
        }

        /**
         * Adds a value to the end of the Flag's Values. This value will have an index
         * of size() + 1. Returns the index of the value added. This could change if
         * values are removed. 
         * 
         */
        public int add(Object obj) {
            checkExpired();
            value.values.add((String) obj);
            save();
            rebuild();
            return size();
        }

        /**
         * Removes a value from the Flag's current values. The first value that matches
         * (values are checked as Double and String.equalsIgnoreCase) is removed. If
         * no match, no removal is done.
         * 
         */
        public void remove(Object obj) {
            remove(obj, -1);
        }

        /**
         * Removes a value from the Flag's current values. If an index is specified,
         * that specific value is removed. If no index is specified (or -1 is
         * specified as the index), the first value that matches (values are
         * checked as Double and String.equalsIgnoreCase) is removed. If a positive 
         * index is specified that does not exist, no removal is done.
         * 
         */
        public void remove(Object obj, int index) {
            checkExpired();

            // No index? Match object and remove it.
            if (index < 0 && obj != null) {
                int x = 0;
                for (String val : value.values) {

                    // Evaluate as String
                    if (val.equalsIgnoreCase((String) obj)) {
                        value.values.remove(x);
                        return;
                    }

                    // Evaluate as number
                    try { 
                        if (Double.valueOf(val).equals(Double.valueOf((String) obj))) {
                            value.values.remove(x);
                            return;
                        }
                    } catch (Exception e) { /* Not a valid number, continue. */ } 

                    x++;
                }

                // Else, remove specified index
            } else if (index < size()) value.values.remove(index - 1);

            save();
            rebuild();
        }

        /**
         * Invalidates the current value/values in the Flag and replaces them with
         * the object provided. Could be an Integer, String, List<String>, etc. and
         * in theory, could be anything thats value is easily expressed as a String.
         * 
         */
        public void setEntireValue(Object obj) {
            denizen.getSaves().set(flagPath, obj);
            denizen.saveSaves();
            rebuild();
        }

        /**
         * Used to give an expiration time for a flag. This is the same format
         * as System.getCurrentTimeMillis(), which is the number of milliseconds
         * since Jan 1, 1960. As an example, to get a valid expiration for a
         * specific amount of seconds from the current time, use the code snippet
         * Flag.setExpiration(System.getCurrentTimeMillis() + (delay * 1000))
         * where 'delay' is the amount of seconds.
         * 
         */
        public void setExpiration(Long expiration) {
            this.expiration = expiration;
            save();
        }

        /**
         * Returns the number of items in the Flag. This directly corresponds
         * with the indexes, since Flag Indexes start with 1, unlike Java Lists
         * which start at 0.
         * 
         */
        public int size() {
            checkExpired();
            return value.size();
        }

        /**
         * Saves the current values in this object to the Denizen saves.yml.
         * This is called internally when needed, but might be useful to call
         * if you are extending the usage of Flags yourself.
         * 
         */
        public void save() {
            denizen.getSaves().set(flagPath, value.values);
            denizen.getSaves().set(flagPath + "-expiration", expiration);
            denizen.saveSaves();
        }

        /**
         * Returns a String value of the last item in a Flag Value. If there is only
         * a single item in the flag, it returns it. To return the value of another 
         * item in the Flag, use 'flag.get(index).asString()'.
         * 
         */
        @Override
        public String toString() {
            checkExpired();
            return value.get(value.size()).asString();
        }

        /**
         * Removes flag if expiration is found to be up. This is called when an action
         * is done on the flag, such as get() or put(). If expired, the flag will be 
         * erased before moving on.
         * 
         */
        private void checkExpired() {
            rebuild();
            if (denizen.getSaves().contains(flagPath + "-expiration")) 
                if (expiration > 1 && expiration < System.currentTimeMillis()) {
                    denizen.getSaves().set(flagPath + "-expiration", null);
                    denizen.getSaves().set(flagPath, null);
                    rebuild();
                    dB.echoDebug("// A FLAG has expired! " + flagPath);
                }
        }

        /**
         * Rebuilds the flag object with data from the saves.yml (in Memory)
         * to ensure that data is current if updated outside of the scope
         * of the plugin.
         * 
         */
        private Flag rebuild() {
            if (denizen.getSaves().contains(flagPath + "-expiration"))
                this.expiration = (denizen.getSaves().getLong(flagPath + "-expiration")); 
            List<String> cval = denizen.getSaves().getStringList(flagPath);
            if (cval == null) {
                cval = new ArrayList<String>();
                cval.add(denizen.getSaves().getString(flagPath, ""));
            }
            value = new Value(cval);
            return this;
        }
    }


    /**
     *  Value object that is in charge of holding values that belong to a flag.
     *  Also contains some methods for retrieving stored values as specific
     *  data types. Otherwise, this object is used internally and created/destroyed
     *  automatically when working with Flag objects.
     *  
     */
    public class Value {

        private List<String> values;
        private int index = -1;

        private Value(List<String> values) {
            this.values = values;
            if (this.values == null) {
                this.values = new ArrayList<String>();
            }
        }

        /**
         * Used internally to specify which value to work with, if multiple values
         * exist. If value is less than 0, value is set to the last value added.
         * 
         */
        private void adjustIndex() {
            // -1 = last object.
            if (index < 0)
                index = size() - 1;
        }

        /**
         * Retrieves a boolean of the value. If the value is set to ANYTHING except
         * 'FALSE' (equalsIgnoreCase), it will return true. Useful for determining
         * whether a value exists, as FALSE is also returned if the value is not set.
         * 
         */
        public boolean asBoolean() {
            adjustIndex();
            try {
                return !values.get(index).equalsIgnoreCase("FALSE");
            } catch (Exception e) { return false; } 
        }

        /**
         * Retrieves a double value of the specified index. If value is not set,
         * or the value is not convertible to a Double, 0 is returned.
         * 
         */
        public double asDouble() {
            adjustIndex();
            try {
                return Double.valueOf(values.get(index)).intValue();
            } catch (Exception e) { return 0; } 
        }

        /**
         * Returns an Integer value of the specified index. If the value has
         * decimal point information, it is rounded. If value is not set,
         * or the value is not convertible to a Double, 0 is returned.
         * 
         */
        public int asInteger() {
            adjustIndex();
            try {
                return Double.valueOf(values.get(index)).intValue();
            } catch (Exception e) { return 0; } 
        }

        /**
         * Returns a String value of the entirety of the values 
         * contained as a comma-separated list. If the value doesn't 
         * exist, "" is returned.
         * 
         */
        public String asCommaSeparatedList() {
            adjustIndex();
            String returnList = "";

            for (String string : values)
                returnList = returnList + string + ",";

            return returnList.substring(0, returnList.length() - 1);
        }

        /**
         * Returns a String value of the entirety of the values
         * contained as a dScript list. If the value doesn't
         * exist, "" is returned.
         *
         */
        public String asList() {
            adjustIndex();
            String returnList = "";

            for (String string : values)
                returnList = returnList + string + "|";

            return returnList.substring(0, returnList.length() - 1);
        }

        /**
         * Returns a String value of the value in the specified index. If
         * the value doesn't exist, "" is returned.
         * 
         */
        public String asString() {
            adjustIndex();
            try {
                return values.get(index);
            } catch (Exception e) { return ""; } 
        }
        
        

        /**
         * Returns an instance of the appropriate Object, as detected by this method.
         * Should check if instanceof Integer, Double, Boolean, List, or String.
         * 
         */
        public Object asAutoDetectedObject() {
            adjustIndex();
            String arg = values.get(index);

            try {
                // If an Integer
                if (aH.matchesInteger(arg))
                    return Integer.valueOf(aH.getIntegerFrom(arg));

                // If a Double
                else if (aH.matchesDouble(arg))
                    return Double.valueOf(aH.getDoubleFrom(arg));

                // If a Boolean
                else if (arg.equalsIgnoreCase("true")) return true;
                else if (arg.equalsIgnoreCase("false")) return false;

                // If a List<Object>
                else if (arg.contains("|")) {
                    List<String> toList = new ArrayList<String>();
                    for (String string : arg.split("|"))
                        toList.add(string);
                    return toList;
                }

                // Must be a String
                else return arg;
            } catch (Exception e) { return ""; }

        }

        /**
         * Used internally to specify the index. When using as API, you should
         * instead use the get(index) method in the Flag object.
         * 
         */
        private Value get(int i) {
            index = i - 1;
            adjustIndex();
            return this;
        }

        /**
         * Determines if the flag is empty.
         * 
         */
        public boolean isEmpty() {
            if (values.isEmpty()) return true;
            adjustIndex();
            if (this.size() < index + 1) return true;
            if (values.get(index).equals("")) return true;
            return false;
        }

        /**
         * Used internally. Returns the size of the current values list.
         * 
         */
        private int size() {
            return values.size();
        }
    }
    
}
