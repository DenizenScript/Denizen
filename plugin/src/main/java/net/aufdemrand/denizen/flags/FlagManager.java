package net.aufdemrand.denizen.flags;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.core.FlagSmartEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.DenizenCore;
import net.aufdemrand.denizencore.events.OldEventManager;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

// <--[language]
// @name flags
// @group flag system
// @description
// Flags are a feature that is implemented by Denizen to allow the dynamic and persistent storage of information
// to NPC and player objects as well as the server on a global scale. Whether you need to store a quest variable
// on a player, or you are keeping track of the number of people logging into your server, flags can help.
//
// First, a couple facts about flags:
// 1) They are persistent, that is - they survive a server restart, as long as '/denizen save' or a proper shutdown
// has happened since the flag was made.
// 2) They are stored in flags.yml whenever '/denizen save' is used. Otherwise, since writing to the disk is
// 'expensive' as far as performance is concerned, they are stored in memory.
// 3) Flags can be attached to players, NPCs, or the server. This means both 'aufdemrand' and 'davidcernat' can have
// a flag by the same name, but contain separate values if stored to their player-objects.
// 4) Flags can be booleans, single-item values, or multiple-item arrays, and storing information inside them is
// smart and reliable.
// 5) You can set expirations on any flag, to set the maximum life of a flag. This is great for cooldowns and
// temporary variables.
// 6) Flags have world script events, so you can tell when a flag is changing, and react to those changes.
//
// Here's the basics:
//
// Since the range of information needing to be stored can vary, flags offer a flexible way to handle many situations.
// Flags in their simplest form are a true/false method of storage, with tags available to check if a flag exists. But
// they can also store a piece of information, in a 'key/value' relationship. Name the flag, set the value. This can
// serve a dual purpose, since 1) you can prove it exists, using it as a boolean, and 2) another piece of information
// can be stored. It's like saying, 'this flag exists, and stored with it is a chunk of information'.
//
// The elements stored in flags also have the ability to do perform functions if they are numbers. Easily create
// a counter with flags, simply by using the flag command to use the '+' feature. Also available is '-' to
// decrease, '*' to multiply, and '/' to divide the values.
//
// Flags can act as arrays of information as well, with the ability to add additional elements to a single flag. Flags
// with multiple values use a 'smart-index' with the ability to get/remove information via a number, or by its value.
// Included with the flag command is an easy way to add and remove elements from the array, see the flag command for
// more information.
//
// Flags are modified by using the flag command, and easily read by using a replaceable tag. Here are some examples
// of some snippets of actual code:
//
// Setting a boolean flag to a player, and retrieving it:
// We'll use the player linked to the script by specifying <player> in the flag command. The next argument
// is 'stoneskin', shown here without quotes since it's just one word. This will be the name of the flag.
// Second command, 'narrate', as used below will show the player "p@player_name has flag 'stoneskin'? true". The 'true'
// is retrieved by using the player attribute 'has_flag[flag_name]' to check. If this flag did not exist, it would
// return false. To see a list of flag tags, see 'A list of flag-specific tags'.
// <code>
// - flag <player> stoneskin
// - narrate "<player.name> has flag 'stoneskin'? <player.has_flag[stoneskin]>"
// </code>
//
//
// Using flags as counters:
// Definitions are nice and light, but sometimes they just don't do the job. For example, using flags in foreach loops
// is a great idea, and really easy to do.
// <code>
// # initiate a loop through each player that has logged onto the server
// # Inside the loop, check if the player's flag 'completed' contains in it an element named 'beginners tutorial'.
// # If it does, increment the server flag 'completes_counter' by one, and give it 10 seconds to live.
// - foreach <server.list_players> {
//     - if <def[value].flag[completed].as_list> contains 'beginners tutorial'
//       flag server completes_counter:++ duration:10s
//   }
// # Now show the number of players who had the element in their 'completed' flag.
// - narrate "The number of players who have completed the beginner's tutorial is<&co> <server.flag[completes_counter]>"
// </code>
//
//
// Using flags as object storage:
// Flags can hold fetchable objects as well. Let's say players can be friends with NPCs. Why not store the friends
// on the NPC with a list of player objects?
// <code>
// - flag <npc> friends:->:<player>
// - foreach <npc.flag[friends].as_list> {
//     - chat t:<def[value]> 'You are my friend!'
//   }
// </code>
//
// Need to store a location? Store it in a flag!
// <code>
// - flag <player> home_location:<player.location.simple>
// - narrate "Your home location is now '<player.flag[home_location].as_location.simple>'!"
// </code>
//
//
// Flags are good for lots of other things too! Check out the flag command and 'fl@flag' tags
// for more specific information.
// -->


public class FlagManager {

    // Valid flag actions
    public static enum Action {
        SET_VALUE, SET_BOOLEAN, INCREASE, DECREASE, MULTIPLY,
        DIVIDE, INSERT, REMOVE, SPLIT, SPLIT_NEW, DELETE
    }


    // Constructor
    private Denizen denizen;

    public FlagManager(Denizen denizen) {
        this.denizen = denizen;
    }

    // Static methods
    public static boolean playerHasFlag(dPlayer player, String flagName) {
        if (player == null || flagName == null) {
            return false;
        }
        return DenizenAPI.getCurrentInstance().flagManager()
                .getPlayerFlag(player, flagName).size() > 0;
    }

    public static boolean entityHasFlag(dEntity entity, String flagName) {
        if (entity == null || flagName == null) {
            return false;
        }
        return DenizenAPI.getCurrentInstance().flagManager()
                .getEntityFlag(entity, flagName).size() > 0;
    }

    public static boolean npcHasFlag(dNPC npc, String flagName) {
        if (npc == null || flagName == null) {
            return false;
        }
        return DenizenAPI.getCurrentInstance().flagManager()
                .getNPCFlag(npc.getId(), flagName).size() > 0;
    }

    public static boolean serverHasFlag(String flagName) {
        if (flagName == null) {
            return false;
        }
        return DenizenAPI.getCurrentInstance().flagManager()
                .getGlobalFlag(flagName).size() > 0;
    }

    public static void clearNPCFlags(int npcid) {
        DenizenAPI.getCurrentInstance().getSaves().set("NPCs." + npcid, null);
    }

    public static void clearEntityFlags(dEntity entity) {
        DenizenAPI.getCurrentInstance().getSaves().set("Entities." + entity.getSaveName(), null);
    }

    /**
     * Returns a NPC Flag object. If this flag currently exists
     * it will be populated with the current values. If the flag does NOT exist,
     * it will be created with blank values.
     */
    public Flag getNPCFlag(int npcid, String flagName) {
        return new Flag("NPCs." + npcid + ".Flags." + flagName.toUpperCase(), flagName, "n@" + npcid);
    }

    /**
     * Returns a Global Flag object. If this flag currently exists
     * it will be populated with the current values. If the flag does NOT exist,
     * it will be created with blank values.
     */
    public Flag getGlobalFlag(String flagName) {
        return new Flag("Global.Flags." + flagName.toUpperCase(), flagName, "SERVER");
    }

    /**
     * Returns a Flag Object tied to a Player. If this flag currently exists
     * it will be populated with the current values. If the flag does NOT exist,
     * it will be created with blank values.
     */
    public Flag getPlayerFlag(dPlayer player, String flagName) {
        if (player == null) {
            return new Flag("players.00.UNKNOWN.Flags." + flagName.toUpperCase(), flagName, "p@null");
        }
        return new Flag("Players." + player.getSaveName() + ".Flags." + flagName.toUpperCase(), flagName, player.identify());
    }

    public Flag getEntityFlag(dEntity entity, String flagName) {
        if (entity == null) {
            return new Flag("Entities.00.UNKNOWN.Flags." + flagName.toUpperCase(), flagName, "p@null");
        }
        return new Flag("Entities." + entity.getSaveName() + ".Flags." + flagName.toUpperCase(), flagName, entity.identify());
    }

    public Flag getPlayerFlag(UUID player, String flagName) {
        if (player == null) {
            return new Flag("players.00.UNKNOWN.Flags." + flagName.toUpperCase(), flagName, "p@null");
        }
        String baseID = player.toString().toUpperCase().replace("-", "");
        return new Flag("Players." + baseID.substring(0, 2) + "." + baseID + ".Flags." + flagName.toUpperCase(), flagName, "p@" + player.toString());
    }

    /**
     * Returns a list of flag names currently attached to an NPC.
     */
    public Set<String> listNPCFlags(int npcid) {
        ConfigurationSection section = denizen.getSaves().getConfigurationSection("NPCs." + npcid + ".Flags");
        return section != null ? _filterExpirations(section.getValues(true).keySet()) : null;
    }

    /**
     * Returns a list of flag names currently attached to the server.
     */
    public Set<String> listGlobalFlags() {
        ConfigurationSection section = denizen.getSaves().getConfigurationSection("Global.Flags");
        return section != null ? _filterExpirations(section.getValues(true).keySet()) : null;
    }

    /**
     * Returns a list of flag names currently attached to a player.
     */
    public Set<String> listPlayerFlags(dPlayer player) {
        ConfigurationSection section = denizen.getSaves().getConfigurationSection("Players." + player.getSaveName() + ".Flags");
        return section != null ? _filterExpirations(section.getValues(true).keySet()) : null;
    }

    public Set<String> listEntityFlags(dEntity entity) {
        ConfigurationSection section = denizen.getSaves().getConfigurationSection("Entities." + entity.getSaveName() + ".Flags");
        return section != null ? _filterExpirations(section.getValues(true).keySet()) : null;
    }

    public Set<String> _filterExpirations(Set<String> flagKeys) {
        for (Iterator<String> iter = flagKeys.iterator(); iter.hasNext(); ) {
            if (iter.next().endsWith("-expiration")) {
                iter.remove();
            }
        }
        return flagKeys;
    }


    /**
     * Flag object contains methods for working with Flags and contain a list
     * of the values associated with said flag (if existing) and (optionally) an
     * expiration (if existing).
     * <p/>
     * Storage example in Denizen saves.yml:
     * <p/>
     * 'FLAG_NAME':
     * - First Value
     * - Second Value
     * - Third Value
     * - ...
     * 'FLAG_NAME-expiration': 123456789
     * <p/>
     * To work with multiple values in a flag, an index must be provided. Indexes
     * start at 1 and get higher as more items are added. Specifying an index of -1, or,
     * when possible, supplying NO index will result in retrieving/setting/etc the
     * item with the highest index. Also, note that when using a FLAG TAG in DSCRIPT,
     * ie. <FLAG.P:FLAG_NAME>, specifying no index will follow suit, that is, the
     * value with the highest index will be referenced.
     */
    public class Flag {

        private Value value;
        private String flagPath;
        private String flagName;
        private String flagOwner;
        private long expiration = (long) -1;
        private boolean valid = true;

        Flag(String flagPath, String flagName, String flagOwner) {
            this.flagPath = flagPath;
            this.flagName = flagName;
            this.flagOwner = flagOwner;
            rebuild();
        }

        /**
         * Returns whether or not a value currently exists in the Flag. The
         * provided value will check if it matches an existing value by means
         * of a String.equalsIgnoreCase as well as a Double match if the
         * provided value is a number.
         */
        public boolean contains(String stringValue) {
            if (checkExpired()) {
                return false;
            }
            for (String val : value.values) {
                if (val.equalsIgnoreCase(stringValue)) {
                    return true;
                }
                try {
                    if (Double.valueOf(val).equals(Double.valueOf(stringValue))) {
                        return true;
                    }
                }
                catch (NumberFormatException e) { /* Not a valid number, continue. */ }
            }

            return false;
        }

        /**
         * Gets whether the flag is still valid.
         */
        public boolean StillValid() {
            return valid;
        }

        /**
         * Gets all values currently stored in the flag.
         */
        public List<String> values() {
            checkExpired();
            return value.values;
        }

        /**
         * Gets a specific value stored in a flag when given an index.
         */
        public Value get(int index) {
            checkExpired();
            return value.get(index);
        }

        // <--[event]
        // @Events
        // flag cleared
        // player flag cleared
        // player flag <flagname> cleared
        // npc flag cleared
        // npc flag <flagname> cleared
        // server flag cleared
        // server flag <flagname> cleared
        //
        // @Warning This event will fire rapidly and not exactly when you might expect it to fire.
        //
        // @Triggers when a flag is cleared
        // @Context
        // <context.owner> returns an Element of the flag owner's object.
        // <context.name> returns an Element of the flag name.
        // <context.type> returns an Element of the flag type.
        // <context.old_value> returns an Element of the flag's previous value.
        //
        // -->

        /**
         * Clears all values from a flag, essentially making it null.
         */
        public void clear() {
            String OldOwner = flagOwner;
            String OldName = flagName;
            dObject OldValue = value.size() > 1
                    ? new dList(denizen.getSaves().getStringList(flagPath))
                    : value.size() == 1 ? new Element(value.get(0).asString()) : Element.valueOf("null");

            denizen.getSaves().set(flagPath, null);
            denizen.getSaves().set(flagPath + "-expiration", null);
            valid = false;
            rebuild();

            if (FlagSmartEvent.IsActive()) {
                List<String> world_script_events = new ArrayList<String>();

                Map<String, dObject> context = new HashMap<String, dObject>();
                dPlayer player = null;
                if (dPlayer.matches(OldOwner)) {
                    player = dPlayer.valueOf(OldOwner);
                }
                dNPC npc = null;
                if (Depends.citizens != null && dNPC.matches(OldOwner)) {
                    npc = dNPC.valueOf(OldOwner);
                }

                String type;

                if (player != null) {
                    type = "player";
                }
                else if (npc != null) {
                    type = "npc";
                }
                else {
                    type = "server";
                }
                world_script_events.add(type + " flag cleared");
                world_script_events.add(type + " flag " + OldName + " cleared");

                context.put("owner", new Element(OldOwner));
                context.put("name", new Element(OldName));
                context.put("type", new Element(type));
                context.put("old_value", OldValue);

                world_script_events.add("flag cleared");

                OldEventManager.doEvents(world_script_events,
                        new BukkitScriptEntryData(player, npc), context);
            }
        }

        /**
         * Gets the first value stored in the Flag.
         */
        public Value getFirst() {
            checkExpired();
            return value.get(1);
        }

        /**
         * Gets the last value stored in the Flag.
         */
        public Value getLast() {
            checkExpired();
            return value.get(value.size());
        }

        /**
         * Sets the value of the most recent value added to the flag. This does
         * not create a new value unless the flag is currently empty of values.
         */
        public void set(Object obj) {
            set(obj, -1);
        }

        /**
         * Sets a specific value in the flag. Adds the value to the flag if
         * the index doesn't exist. If the index is less than 0, it instead
         * clears the flag and works as if setting a blank flag. If the flag is
         * currently empty, the value is added.
         */
        public void set(Object obj, int index) {
            checkExpired();

            // No index? Clear the flag and set the whole thing.
            if (index < 0) {
                value.values.clear();
                value.values.add((String) obj);
            }
            else if (size() == 0) {
                value.values.add((String) obj);
            }
            else if (index > 0) {
                if (value.values.size() > index - 1) {
                    value.values.remove(index - 1);
                    value.values.add(index - 1, (String) obj);

                    // Index higher than currently exists? Add the item to the end of the list.
                }
                else {
                    value.values.add((String) obj);
                }
            }
            valid = true;
            save();
            rebuild();
        }

        /**
         * Adds a value to the end of the Flag's Values. This value will have an index
         * of size() + 1. Returns the index of the value added. This could change if
         * values are removed.
         */
        public int add(Object obj) {
            checkExpired();
            value.values.add((String) obj);
            valid = true;
            save();
            rebuild();
            return size();
        }

        /**
         * Splits a dScript list into values that are then added to the flag.
         * Returns the index of the last value added to the flag.
         */
        public int split(Object obj) {
            checkExpired();
            dList split = dList.valueOf(obj.toString());
            if (split.size() > 0) {
                for (String val : split) {
                    if (val.length() > 0) {
                        value.values.add(val);
                    }
                }
                save();
                rebuild();
            }
            return size();
        }

        public int splitNew(Object obj) {
            checkExpired();
            dList split = dList.valueOf(obj.toString());
            if (split.size() > 0) {
                value.values.clear();
                for (String val : split) {
                    if (val.length() > 0) {
                        value.values.add(val);
                    }
                }
                save();
                rebuild();
            }
            else {
                clear();
            }
            return size();
        }

        /**
         * Removes a value from the Flag's current values. The first value that matches
         * (values are checked as Double and String.equalsIgnoreCase) is removed. If
         * no match, no removal is done.
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
         */
        public void remove(Object obj, int index) {
            checkExpired();

            // No index? Match object and remove it.
            if (index <= 0 && obj != null) {
                int x = 0;
                for (String val : value.values) {

                    // Evaluate as String
                    if (val.equalsIgnoreCase(String.valueOf(obj))) {

                        value.values.remove(x);
                        break;
                    }

                    // Evaluate as number
                    try {
                        if (Double.valueOf(val).equals(Double.valueOf((String) obj))) {
                            value.values.remove(x);
                            break;
                        }
                    }
                    catch (Exception e) { /* Not a valid number, continue. */ }

                    x++;
                }

                // Else, remove specified index
            }
            else if (index <= size()) {
                value.values.remove(index - 1);
            }

            valid = true;
            save();
            rebuild();
        }

        /**
         * Used to give an expiration time for a flag. This is the same format
         * as System.getCurrentTimeMillis(), which is the number of milliseconds
         * since Jan 1, 1960. As an example, to get a valid expiration for a
         * specific amount of seconds from the current time, use the code snippet
         * Flag.setExpiration(System.getCurrentTimeMillis() + (delay * 1000))
         * where 'delay' is the amount of seconds.
         */
        public void setExpiration(Long expiration) {
            valid = true;
            this.expiration = expiration;
            save();
        }

        /**
         * Returns the number of items in the Flag. This directly corresponds
         * with the indexes, since Flag Indexes start with 1, unlike Java Lists
         * which start at 0.
         */
        public int size() {
            checkExpired();
            return value.size();
        }

        // <--[event]
        // @Events
        // flag changed
        // player flag changed
        // player flag <flagname> changed
        // npc flag changed
        // npc flag <flagname> changed
        // server flag changed
        // server flag <flagname> changed
        // entity flag changed
        // entity flag <flagname> changed
        //
        // @Warning This event will fire rapidly and not exactly when you might expect it to fire.
        //
        // @Triggers when a flag is changed
        // @Context
        // <context.owner> returns an Element of the flag owner's object.
        // <context.name> returns an Element of the flag name.
        // <context.type> returns an Element of the flag type.
        // <context.old_value> returns an Element of the flag's previous value.
        //
        // -->

        /**
         * Saves the current values in this object to the Denizen saves.yml.
         * This is called internally when needed, but might be useful to call
         * if you are extending the usage of Flags yourself.
         */
        public void save() {
            String OldOwner = flagOwner;
            String OldName = flagName;
            List<String> oldValueList = denizen.getSaves().getStringList(flagPath);
            dObject OldValue = oldValueList.size() > 1 ? new dList(oldValueList)
                    : oldValueList.size() == 1 ? new Element(oldValueList.get(0)) : Element.valueOf("null");

            denizen.getSaves().set(flagPath, value.values);
            denizen.getSaves().set(flagPath + "-expiration", (expiration > 0 ? expiration : null));
            rebuild();

            if (FlagSmartEvent.IsActive()) {
                List<String> world_script_events = new ArrayList<String>();

                Map<String, dObject> context = new HashMap<String, dObject>();
                dPlayer player = null;
                if (dPlayer.matches(OldOwner)) {
                    player = dPlayer.valueOf(OldOwner);
                }
                dNPC npc = null;
                if (Depends.citizens != null && dNPC.matches(OldOwner)) {
                    npc = dNPC.valueOf(OldOwner);
                }
                dEntity entity = null;
                if (dEntity.matches(OldOwner)) {
                    entity = dEntity.valueOf(OldOwner);
                }

                String type;

                if (player != null) {
                    type = "player";
                }
                else if (npc != null) {
                    type = "npc";
                }
                else if (entity != null) {
                    type = "entity";
                }
                else {
                    type = "server";
                }
                world_script_events.add(type + " flag changed");
                world_script_events.add(type + " flag " + OldName + " changed");

                context.put("owner", Element.valueOf(OldOwner));
                context.put("name", Element.valueOf(OldName));
                context.put("type", Element.valueOf(type));
                context.put("old_value", OldValue);

                world_script_events.add("flag changed");

                OldEventManager.doEvents(world_script_events,
                        new BukkitScriptEntryData(player, npc), context);
            }

        }

        /**
         * Returns a String value of the last item in a Flag Value. If there is only
         * a single item in the flag, it returns it. To return the value of another
         * item in the Flag, use 'flag.get(index).asString()'.
         */
        @Override
        public String toString() {
            checkExpired();
            // Possibly use reflection to check whether dList or dElement is calling this?
            // If dList, return fl@..., if dElement, return f@...
            return (flagOwner.equalsIgnoreCase("SERVER") ? "fl@" + flagName : "fl[" + flagOwner + "]@" + flagName);
        }

        // <--[event]
        // @Events
        // flag expires
        // player flag expires
        // player flag <flagname> expires
        // npc flag expires
        // npc flag <flagname> expires
        // server flag expires
        // server flag <flagname> expires
        // entity flag expires
        // entity flag <flagname> expires
        //
        // @Warning This event will fire rapidly and not exactly when you might expect it to fire.
        //
        // @Triggers when a flag expires
        // @Context
        // <context.owner> returns an Element of the flag owner's object.
        // <context.name> returns an Element of the flag name.
        // <context.type> returns an Element of the flag type.
        // <context.old_value> returns an Element of the flag's previous value.
        //
        // -->

        /**
         * Removes flag if expiration is found to be up. This is called when an action
         * is done on the flag, such as get() or put(). If expired, the flag will be
         * erased before moving on.
         */
        public boolean checkExpired() {
            rebuild();
            if (denizen.getSaves().contains(flagPath + "-expiration")) {
                if (expiration > 1 && expiration < DenizenCore.currentTimeMillis) {
                    String OldOwner = flagOwner;
                    String OldName = flagName;
                    dObject OldValue = value.size() > 1
                            ? new dList(denizen.getSaves().getStringList(flagPath))
                            : value.size() == 1 ? new Element(value.get(0).asString()) : Element.valueOf("null");
                    denizen.getSaves().set(flagPath + "-expiration", null);
                    denizen.getSaves().set(flagPath, null);
                    valid = false;
                    rebuild();
                    //dB.log('\'' + flagName + "' has expired! " + flagPath);
                    if (FlagSmartEvent.IsActive()) {
                        List<String> world_script_events = new ArrayList<String>();

                        Map<String, dObject> context = new HashMap<String, dObject>();
                        dPlayer player = null;
                        if (dPlayer.matches(OldOwner)) {
                            player = dPlayer.valueOf(OldOwner);
                        }
                        dNPC npc = null;
                        if (Depends.citizens != null && dNPC.matches(OldOwner)) {
                            npc = dNPC.valueOf(OldOwner);
                        }
                        dEntity entity = null;
                        if (dEntity.matches(OldOwner)) {
                            entity = dEntity.valueOf(OldOwner);
                        }

                        String type;

                        if (player != null) {
                            type = "player";
                        }
                        else if (npc != null) {
                            type = "npc";
                        }
                        else if (entity != null) {
                            type = "entity";
                        }
                        else {
                            type = "server";
                        }
                        world_script_events.add(type + " flag expires");
                        world_script_events.add(type + " flag " + OldName + " expires");

                        context.put("owner", Element.valueOf(OldOwner));
                        context.put("name", Element.valueOf(OldName));
                        context.put("type", Element.valueOf(type));
                        context.put("old_value", OldValue);

                        world_script_events.add("flag expires");

                        OldEventManager.doEvents(world_script_events,
                                new BukkitScriptEntryData(player, npc), context);
                    }
                    return true;
                }
            }
            return false;
        }

        public Duration expiration() {
            return new Duration((double) (expiration - DenizenCore.currentTimeMillis) / 1000);
        }

        /**
         * Returns the time left before the flag will expire. Minutes are only shown
         * if there is less than a day left, and seconds are only shown if there are
         * less than 10 minutes left.
         */
        @Deprecated
        public String expirationTime() {
            rebuild();

            long seconds = (expiration - DenizenCore.currentTimeMillis) / 1000;

            long days = seconds / 86400;
            long hours = (seconds - days * 86400) / 3600;
            long minutes = (seconds - days * 86400 - hours * 3600) / 60;
            seconds = seconds - days * 86400 - hours * 3600 - minutes * 60;

            String timeString = "";

            if (days > 0) {
                timeString = String.valueOf(days) + "d ";
            }
            if (hours > 0) {
                timeString = timeString + String.valueOf(hours) + "h ";
            }
            if (minutes > 0 && days == 0) {
                timeString = timeString + String.valueOf(minutes) + "m ";
            }
            if (seconds > 0 && minutes < 10 && hours == 0 && days == 0) {
                timeString = timeString + String.valueOf(seconds) + "s";
            }

            return timeString.trim();
        }

        /**
         * Rebuilds the flag object with data from the saves.yml (in Memory)
         * to ensure that data is current if updated outside of the scope
         * of the plugin.
         */
        public Flag rebuild() {
            if (denizen.getSaves().contains(flagPath + "-expiration")) {
                this.expiration = (denizen.getSaves().getLong(flagPath + "-expiration"));
            }
            List<String> cval = denizen.getSaves().getStringList(flagPath);
            if (cval == null) {
                cval = new ArrayList<String>();
                cval.add(denizen.getSaves().getString(flagPath, ""));
            }
            value = new Value(cval);
            return this;
        }

        /**
         * Determines if the flag is empty.
         */
        public boolean isEmpty() {
            return value.isEmpty();
        }

        /**
         * Performs an action on the flag.
         *
         * @param action a valid Action enum
         * @param value  the value specified for the action
         * @param index  the flag index, null if none
         */
        public void doAction(Action action, Element value, Integer index) {

            String val = (value != null ? value.asString() : null);

            if (index == null) {
                index = -1;
            }

            if (action == null) {
                return;
            }

            // Do flagAction
            switch (action) {
                case INCREASE:
                case DECREASE:
                case MULTIPLY:
                case DIVIDE:
                    double currentValue = get(index).asDouble();
                    set(CoreUtilities.doubleToString(math(currentValue, Double.valueOf(value.asString()), action)), index);
                    break;

                case SET_BOOLEAN:
                    set("true", index);
                    break;

                case SET_VALUE:
                    set(val, index);
                    break;

                case INSERT:
                    add(val);
                    break;

                case REMOVE:
                    remove(val, index);
                    break;

                case SPLIT:
                    split(val);
                    break;

                case SPLIT_NEW:
                    splitNew(val);
                    break;

                case DELETE:
                    clear();
                    break;
            }
        }

        private double math(double currentValue, double value, Action flagAction) {
            switch (flagAction) {
                case INCREASE:
                    return currentValue + value;

                case DECREASE:
                    return currentValue - value;

                case MULTIPLY:
                    return currentValue * value;

                case DIVIDE:
                    return currentValue / value;

                default:
                    break;
            }

            return 0;
        }

    }


    /**
     * Value object that is in charge of holding values that belong to a flag.
     * Also contains some methods for retrieving stored values as specific
     * data types. Otherwise, this object is used internally and created/destroyed
     * automatically when working with Flag objects.
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
         */
        private void adjustIndex() {
            // -1 = last object.
            if (index < 0) {
                index = size() - 1;
            }
        }

        /**
         * Retrieves a boolean of the value. If the value is set to ANYTHING except
         * 'FALSE' (equalsIgnoreCase), it will return true. Useful for determining
         * whether a value exists, as FALSE is also returned if the value is not set.
         */
        public boolean asBoolean() {
            adjustIndex();
            try {
                return !values.get(index).equalsIgnoreCase("FALSE");
            }
            catch (Exception e) {
                return false;
            }
        }

        /**
         * Retrieves a double value of the specified index. If value is not set,
         * or the value is not convertible to a Double, 0 is returned.
         */
        public double asDouble() {
            adjustIndex();
            try {
                return Double.valueOf(values.get(index));
            }
            catch (Exception e) {
                return 0;
            }
        }

        /**
         * Returns an Integer value of the specified index. If the value has
         * decimal point information, it is rounded. If value is not set,
         * or the value is not convertible to a Double, 0 is returned.
         */
        public int asInteger() {
            adjustIndex();
            try {
                return Double.valueOf(values.get(index)).intValue();
            }
            catch (Exception e) {
                return 0;
            }
        }

        /**
         * Returns a String value of the entirety of the values
         * contained as a comma-separated list. If the value doesn't
         * exist, "" is returned.
         */
        public String asCommaSeparatedList() {
            adjustIndex();
            String returnList = "";

            for (String string : values) {
                returnList = returnList + string + ", ";
            }

            return returnList.substring(0, returnList.length() - 2);
        }

        /**
         * Returns a String value of the entirety of the values
         * contained as a dScript list. If the value doesn't
         * exist, "" is returned.
         */
        public dList asList() {
            adjustIndex();
            return new dList(values);
        }

        /**
         * Returns a String value of the entirety of the values
         * contained as a dScript list, with a prefix added to
         * the start of each value. If the value doesn't
         * exist, "" is returned.
         */
        public dList asList(String prefix) {
            adjustIndex();
            return new dList(values, prefix);
        }

        /**
         * Returns a String value of the value in the specified index. If
         * the value doesn't exist, "" is returned.
         */
        public String asString() {
            adjustIndex();
            try {
                return values.get(index);
            }
            catch (Exception e) {
                return "";
            }
        }

        /**
         * Returns an Integer value of the number of values
         * contained in a dScript list.
         */
        public int asSize() {
            adjustIndex();
            return values.size();
        }

        /**
         * Returns an instance of the appropriate Object, as detected by this method.
         * Should check if instanceof Integer, Double, Boolean, List, or String.
         */
        public Object asAutoDetectedObject() {
            adjustIndex();
            String arg = values.get(index);

            try {
                // If an Integer
                if (aH.matchesInteger(arg)) {
                    return aH.getIntegerFrom(arg);
                }

                // If a Double
                else if (aH.matchesDouble(arg)) {
                    return aH.getDoubleFrom(arg);
                }

                // If a Boolean
                else if (arg.equalsIgnoreCase("true")) {
                    return true;
                }
                else if (arg.equalsIgnoreCase("false")) {
                    return false;
                }

                // If a List<Object>
                else if (arg.contains("|")) {
                    List<String> toList = new ArrayList<String>();
                    return new dList(toList);
                }

                // Must be a String
                else {
                    return arg;
                }
            }
            catch (Exception e) {
                return "";
            }

        }

        /**
         * Used internally to specify the index. When using as API, you should
         * instead use the get(index) method in the Flag object.
         */
        private Value get(int i) {
            index = i - 1;
            adjustIndex();
            return this;
        }

        /**
         * Determines if the flag is empty.
         */
        public boolean isEmpty() {
            if (values.isEmpty()) {
                return true;
            }
            adjustIndex();
            if (this.size() < index + 1) {
                return true;
            }
            return values.get(index).equals("");
        }

        /**
         * Used internally. Returns the size of the current values list.
         */
        private int size() {
            return values.size();
        }
    }
}
