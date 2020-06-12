package com.denizenscript.denizen.flags;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.core.FlagSmartEvent;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.OldEventManager;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.SlowWarning;
import com.denizenscript.denizencore.utilities.debugging.Warning;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.util.*;

public class FlagManager {

    public static Warning listFlagsTagWarning = new SlowWarning("The list_flags tag is meant for testing/debugging only. Do not use it in scripts (ignore this warning if using for testing reasons).");

    // Valid flag actions
    public enum Action {
        SET_VALUE, SET_BOOLEAN, INCREASE, DECREASE, MULTIPLY,
        DIVIDE, INSERT, REMOVE, SPLIT, SPLIT_NEW, DELETE
    }

    // Constructor
    private Denizen denizen;

    public FlagManager(Denizen denizen) {
        this.denizen = denizen;
    }

    // Static methods
    public static boolean playerHasFlag(PlayerTag player, String flagName) {
        if (player == null || flagName == null) {
            return false;
        }
        return DenizenAPI.getCurrentInstance().flagManager().getPlayerFlag(player, flagName).size() > 0;
    }

    public static boolean entityHasFlag(EntityTag entity, String flagName) {
        if (entity == null || flagName == null) {
            return false;
        }
        return DenizenAPI.getCurrentInstance().flagManager().getEntityFlag(entity, flagName).size() > 0;
    }

    public static boolean npcHasFlag(NPCTag npc, String flagName) {
        if (npc == null || flagName == null) {
            return false;
        }
        return DenizenAPI.getCurrentInstance().flagManager().getNPCFlag(npc.getId(), flagName).size() > 0;
    }

    public static boolean serverHasFlag(String flagName) {
        if (flagName == null) {
            return false;
        }
        return DenizenAPI.getCurrentInstance().flagManager().getGlobalFlag(flagName).size() > 0;
    }

    public static void clearNPCFlags(int npcid) {
        DenizenAPI.getCurrentInstance().getSaves().set("NPCs." + npcid, null);
    }

    public static void clearEntityFlags(EntityTag entity) {
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
    public Flag getPlayerFlag(PlayerTag player, String flagName) {
        if (player == null) {
            return new Flag("players.00.UNKNOWN.Flags." + flagName.toUpperCase(), flagName, "p@null");
        }
        return new Flag("Players." + player.getSaveName() + ".Flags." + flagName.toUpperCase(), flagName, player.identify());
    }

    public Flag getEntityFlag(EntityTag entity, String flagName) {
        if (entity == null) {
            return new Flag("Entities.00.UNKNOWN.Flags." + flagName.toUpperCase(), flagName, "e@null");
        }
        return new Flag("Entities." + entity.getSaveName() + ".Flags." + flagName.toUpperCase(), flagName, entity.identify());
    }

    /**
     * Returns a list of flag names currently attached to an NPC.
     */
    public Set<String> listNPCFlags(int npcid) {
        ConfigurationSection section = denizen.getSaves().getConfigurationSection("NPCs." + npcid + ".Flags");
        return section != null ? _filterExpirations(section.getValues(true).keySet()) : null;
    }

    public void shrinkGlobalFlags(Collection<String> set) {
        for (String str : new HashSet<>(set)) {
            if (!serverHasFlag(str)) {
                set.remove(str);
            }
        }
    }

    public void shrinkPlayerFlags(PlayerTag player, Collection<String> set) {
        for (String str : new HashSet<>(set)) {
            if (!playerHasFlag(player, str)) {
                set.remove(str);
            }
        }
    }

    public void shrinkEntityFlags(EntityTag entity, Collection<String> set) {
        for (String str : new HashSet<>(set)) {
            if (!entityHasFlag(entity, str)) {
                set.remove(str);
            }
        }
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
    public Set<String> listPlayerFlags(PlayerTag player) {
        ConfigurationSection section = denizen.getSaves().getConfigurationSection("Players." + player.getSaveName() + ".Flags");
        return section != null ? _filterExpirations(section.getValues(true).keySet()) : null;
    }

    public Set<String> listEntityFlags(EntityTag entity) {
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

    public class Flag {

        private Value value;
        private String flagPath;
        private String flagName;
        private String flagOwner;
        private long expiration = -1L;
        private boolean valid = true;

        Flag(String flagPath, String flagName, String flagOwner) {
            this.flagPath = flagPath;
            this.flagName = flagName;
            this.flagOwner = flagOwner;
            rebuild();
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
            return value.asList();
        }

        /**
         * Gets a specific value stored in a flag when given an index.
         */
        public Value get(int index) {
            checkExpired();
            return value.get(index);
        }

        /**
         * Clears all values from a flag, essentially making it null.
         */
        public void clear() {
            String OldOwner = flagOwner;
            String OldName = flagName;
            ObjectTag OldValue = FlagSmartEvent.isActive() ? (value.size() > 1
                    ? value.asList()
                    : value.size() == 1 ? new ElementTag(value.get(0).asString()) : new ElementTag("null")) : null;

            denizen.getSaves().set(flagPath, null);
            denizen.getSaves().set(flagPath + "-expiration", null);
            valid = false;
            rebuild();

            if (FlagSmartEvent.isActive()) {
                List<String> world_script_events = new ArrayList<>();

                Map<String, ObjectTag> context = new HashMap<>();
                PlayerTag player = null;
                if (PlayerTag.matches(OldOwner)) {
                    player = PlayerTag.valueOf(OldOwner, CoreUtilities.basicContext);
                }
                NPCTag npc = null;
                if (Depends.citizens != null && NPCTag.matches(OldOwner)) {
                    npc = NPCTag.valueOf(OldOwner, CoreUtilities.basicContext);
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

                context.put("owner", new ElementTag(OldOwner));
                context.put("name", new ElementTag(OldName));
                context.put("type", new ElementTag(type));
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
                value.values = null;
                value.size = 1;
                value.firstValue = (String) obj;
            }
            else if (size() == 0) {
                value.firstValue = (String) obj;
                value.size = 1;
            }
            else if (index > 0) {
                value.mustBeList();
                if (value.values.size() > index - 1) {
                    value.values.set(index - 1, (String) obj);

                    // Index higher than currently exists? Add the item to the end of the list.
                }
                else {
                    value.values.add((String) obj);
                    value.size++;
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
            value.mustBeList();
            value.values.add((String) obj);
            value.size++;
            valid = true;
            save();
            rebuild();
            return size();
        }

        /**
         * Splits a dScript list into values that are then added to the flag.
         * Returns the index of the last value added to the flag.
         */
        public int split(Object obj, TagContext context) {
            checkExpired();
            ListTag split = ListTag.valueOf(obj.toString(), context);
            if (split.size() > 0) {
                value.mustBeList();
                for (String val : split) {
                    if (val.length() > 0) {
                        value.values.add(val);
                        value.size++;
                    }
                }
                save();
                rebuild();
            }
            return size();
        }

        public int splitNew(Object obj, TagContext context) {
            checkExpired();
            ListTag split = ListTag.valueOf(obj.toString(), context);
            if (split.size() > 0) {
                value.mustBeList();
                value.values.clear();
                value.size = 0;
                for (String val : split) {
                    if (val.length() > 0) {
                        value.values.add(val);
                        value.size++;
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
            boolean isDouble = ArgumentHelper.matchesDouble((String) obj);
            value.mustBeList();
            if (index <= 0 && obj != null) {
                int x = 0;
                for (String val : value.values) {
                    if (val.equalsIgnoreCase(String.valueOf(obj))) {
                        value.values.remove(x);
                        value.size--;
                        break;
                    }
                    try {
                        if (isDouble && ArgumentHelper.matchesDouble(val) && Double.valueOf(val).equals(Double.valueOf((String) obj))) {
                            value.values.remove(x);
                            value.size--;
                            break;
                        }
                    }
                    catch (NumberFormatException e) {
                        // Ignore
                    }
                    x++;
                }
            }
            else if (index <= value.size()) {
                value.values.remove(index - 1);
                value.size--;
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

        /**
         * Saves the current values in this object to the Denizen saves.yml.
         * This is called internally when needed, but might be useful to call
         * if you are extending the usage of Flags yourself.
         */
        public void save() {
            String oldOwner = flagOwner;
            String oldName = flagName;
            ObjectTag oldValue = null;
            if (FlagSmartEvent.isActive()) {
                ListTag oldValueList = value.asList();
                oldValue = oldValueList.size() > 1 ? oldValueList
                        : oldValueList.size() == 1 ? new ElementTag(oldValueList.get(0)) : new ElementTag("null");
            }

            if (value.values != null) {
                denizen.getSaves().set(flagPath, value.values);
            }
            else {
                denizen.getSaves().set(flagPath, value.size == 0 ? null : value.firstValue);
            }
            denizen.getSaves().set(flagPath + "-expiration", (expiration > 0 ? expiration : null));

            if (FlagSmartEvent.isActive()) {
                List<String> world_script_events = new ArrayList<>();

                Map<String, ObjectTag> context = new HashMap<>();
                PlayerTag player = null;
                if (PlayerTag.matches(oldOwner)) {
                    player = PlayerTag.valueOf(oldOwner, CoreUtilities.basicContext);
                }
                NPCTag npc = null;
                if (Depends.citizens != null && NPCTag.matches(oldOwner)) {
                    npc = NPCTag.valueOf(oldOwner, CoreUtilities.basicContext);
                }
                EntityTag entity = null;
                if (EntityTag.matches(oldOwner)) {
                    entity = EntityTag.valueOf(oldOwner, CoreUtilities.basicContext);
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
                world_script_events.add(type + " flag " + oldName + " changed");

                context.put("owner", new ElementTag(oldOwner));
                context.put("name", new ElementTag(oldName));
                context.put("type", new ElementTag(type));
                context.put("old_value", oldValue);

                world_script_events.add("flag changed");

                OldEventManager.doEvents(world_script_events,
                        new BukkitScriptEntryData(player, npc), context);
            }

        }

        @Override
        public String toString() {
            return flagOwner + ":" + flagName;
        }

        /**
         * Removes flag if expiration is found to be up. This is called when an action
         * is done on the flag, such as get() or put(). If expired, the flag will be
         * erased before moving on.
         */
        public boolean checkExpired() {
            rebuild();
            if (denizen.getSaves().contains(flagPath + "-expiration")) {
                if (expiration > 1 && expiration < DenizenCore.currentTimeMillis) {
                    String oldOwner = flagOwner;
                    String oldName = flagName;
                    ObjectTag oldValue = FlagSmartEvent.isActive() ? (value.size() > 1
                            ? value.asList()
                            : value.size() == 1 ? new ElementTag(value.get(0).asString()) : new ElementTag("null")) : null;
                    denizen.getSaves().set(flagPath + "-expiration", null);
                    denizen.getSaves().set(flagPath, null);
                    valid = false;
                    rebuild();
                    //dB.log('\'' + flagName + "' has expired! " + flagPath);
                    if (FlagSmartEvent.isActive()) {
                        List<String> world_script_events = new ArrayList<>();

                        Map<String, ObjectTag> context = new HashMap<>();
                        PlayerTag player = null;
                        if (PlayerTag.matches(oldOwner)) {
                            player = PlayerTag.valueOf(oldOwner, CoreUtilities.basicContext);
                        }
                        NPCTag npc = null;
                        if (Depends.citizens != null && NPCTag.matches(oldOwner)) {
                            npc = NPCTag.valueOf(oldOwner, CoreUtilities.basicContext);
                        }
                        EntityTag entity = null;
                        if (EntityTag.matches(oldOwner)) {
                            entity = EntityTag.valueOf(oldOwner, CoreUtilities.basicContext);
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
                        world_script_events.add(type + " flag " + oldName + " expires");

                        context.put("owner", new ElementTag(oldOwner));
                        context.put("name", new ElementTag(oldName));
                        context.put("type", new ElementTag(type));
                        context.put("old_value", oldValue);

                        world_script_events.add("flag expires");

                        OldEventManager.doEvents(world_script_events,
                                new BukkitScriptEntryData(player, npc), context);
                    }
                    return true;
                }
            }
            return false;
        }

        public DurationTag expiration() {
            return new DurationTag((expiration - DenizenCore.currentTimeMillis) / 1000.0);
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
                timeString = days + "d ";
            }
            if (hours > 0) {
                timeString = timeString + hours + "h ";
            }
            if (minutes > 0 && days == 0) {
                timeString = timeString + minutes + "m ";
            }
            if (seconds > 0 && minutes < 10 && hours == 0 && days == 0) {
                timeString = timeString + seconds + "s";
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
            Object obj = denizen.getSaves().get(flagPath);
            if (obj instanceof Map || obj instanceof MemorySection) {
                valid = false;
                value = new Value();
            }
            else if (obj instanceof List) {
                ArrayList<String> val = new ArrayList<>(((List) obj).size());
                for (Object subObj : (List) obj) {
                    val.add(String.valueOf(subObj));
                }
                value = new Value(val);
            }
            else if (obj == null || obj.toString().length() == 0) {
                value = new Value();
            }
            else {
                value = new Value(obj.toString());
            }
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
        public void doAction(Action action, ElementTag value, Integer index, ScriptEntry entry) {

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
                    set(CoreUtilities.doubleToString(math(currentValue, value.asDouble(), action)), index);
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
                    split(val, entry.context);
                    break;

                case SPLIT_NEW:
                    splitNew(val, entry.context);
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

        private String firstValue;
        private List<String> values;
        private int index;
        private int size;

        public Value() {
            size = 0;
            index = 0;
        }

        public void mustBeList() {
            if (values == null) {
                values = new ArrayList<>();
                if (size != 0) {
                    values.add(firstValue);
                }
            }
        }

        public void fixSize() {
            if (values != null) {
                size = values.size();
            }
        }

        public Value(String oneValue) {
            this.firstValue = oneValue;
            size = 1;
            index = 1;
        }

        public Value(List<String> values) {
            this.values = values;
            if (values == null) {
                size = 0;
                index = 0;
            }
            else {
                size = values.size();
                index = values.size() - 1;
            }
        }

        private String getValue() {
            if (values == null) {
                if (size == 0) {
                    return "";
                }
                if (index == 0) {
                    return firstValue;
                }
                return "";
            }
            return values.get(index);
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
            return !getValue().equalsIgnoreCase("false");
        }

        /**
         * Retrieves a double value of the specified index. If value is not set,
         * or the value is not convertible to a Double, 0 is returned.
         */
        public double asDouble() {
            try {
                return Double.valueOf(getValue());
            }
            catch (NumberFormatException e) {
                return 0;
            }
        }

        /**
         * Returns an Integer value of the specified index. If the value has
         * decimal point information, it is rounded. If value is not set,
         * or the value is not convertible to a Double, 0 is returned.
         */
        public int asInteger() {
            try {
                return Double.valueOf(getValue()).intValue();
            }
            catch (NumberFormatException e) {
                return 0;
            }
        }

        /**
         * Returns a String value of the entirety of the values
         * contained as a comma-separated list. If the value doesn't
         * exist, "" is returned.
         */
        public String asCommaSeparatedList() {
            if (values == null) {
                if (size == 0) {
                    return "";
                }
                return firstValue;
            }
            return String.join(", ", values);
        }

        /**
         * Returns a String value of the entirety of the values
         * contained as a dScript list. If the value doesn't
         * exist, "" is returned.
         */
        public ListTag asList() {
            if (values == null) {
                ListTag toReturn = new ListTag();
                if (size != 0) {
                    toReturn.add(firstValue);
                }
                return toReturn;
            }
            return new ListTag(values);
        }

        public ListTag asList(String prefix) {
            if (values == null) {
                ListTag toReturn = new ListTag();
                toReturn.setPrefix(prefix);
                if (size != 0) {
                    toReturn.add(firstValue);
                }
                return toReturn;
            }
            return new ListTag(values, prefix);
        }

        /**
         * Returns a String value of the value in the specified index. If
         * the value doesn't exist, "" is returned.
         */
        public String asString() {
            return getValue();
        }

        /**
         * Returns an Integer value of the number of values
         * contained in a dScript list.
         */
        public int asSize() {
            return size;
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
            if (size == 0) {
                return true;
            }
            if (index >= size) {
                return true;
            }
            return getValue().equals("");
        }

        /**
         * Used internally. Returns the size of the current values list.
         */
        public int size() {
            return size;
        }
    }
}
