package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.core.PlayerTags;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class dPlayer implements dObject {


    /////////////////////
    //   STATIC METHODS
    /////////////////

    static Map<String, dPlayer> players = new HashMap<String, dPlayer>();

    public static dPlayer mirrorBukkitPlayer(OfflinePlayer player) {
        if (player == null) return null;
        if (players.containsKey(player.getName())) return players.get(player.getName());
        else return new dPlayer(player);
    }


    /////////////////////
    //   OBJECT FETCHER
    /////////////////

    @ObjectFetcher("p")
    public static dPlayer valueOf(String string) {
        if (string == null) return null;

        string = string.replace("p@", "");

        ////////
        // Match player name

        OfflinePlayer returnable = null;

        for (OfflinePlayer player : Bukkit.getOfflinePlayers())
            if (player.getName().equalsIgnoreCase(string)) {
                returnable = player;
                break;
            }

        if (returnable != null) {
            if (players.containsKey(returnable.getName())) return players.get(returnable.getName());
            else return new dPlayer(returnable);
        }

        else dB.echoError("Invalid Player! '" + string
                + "' could not be found. Has the player logged off?");

        return null;
    }


    public static boolean matches(String arg) {

        arg = arg.replace("p@", "");

        OfflinePlayer returnable = null;

        for (OfflinePlayer player : Bukkit.getOfflinePlayers())
            if (player.getName().equalsIgnoreCase(arg)) {
                returnable = player;
                break;
            }

        if (returnable != null) return true;

        return false;
    }


    /////////////////////
    //   STATIC CONSTRUCTORS
    /////////////////

    public dPlayer(OfflinePlayer player) {
        if (player == null) return;

        this.player_name = player.getName();

        // Keep in a map to avoid multiple instances of a dPlayer per player.
        players.put(this.player_name, this);
    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    String player_name = null;

    public boolean isValid() {
        if (player_name == null) return false;
        if (getPlayerEntity() == null && getOfflinePlayer() == null) return false;
        return true;
    }

    public Player getPlayerEntity() {
        if (player_name == null) return null;
        return Bukkit.getPlayer(player_name);
    }

    public OfflinePlayer getOfflinePlayer() {
        if (player_name == null) return null;
        return Bukkit.getOfflinePlayer(player_name);
    }

    public dEntity getDenizenEntity() {
        return new dEntity(getPlayerEntity());
    }

    public dNPC getSelectedNPC() {
        if (getPlayerEntity().hasMetadata("selected"))
            return dNPC.valueOf(getPlayerEntity().getMetadata("selected").get(0).asString());
        else return null;
    }

    public String getName() {
        return player_name;
    }

    public dLocation getLocation() {
        if (isOnline()) return new dLocation(getPlayerEntity().getLocation());
        else return null;
    }

    public dLocation getEyeLocation() {
        if (isOnline()) return new dLocation(getPlayerEntity().getEyeLocation());
        else return null;
    }

    public World getWorld() {
        if (isOnline()) return getPlayerEntity().getWorld();
        else return null;
    }

    public boolean isOnline() {
        if (player_name == null) return false;
        if (Bukkit.getPlayer(player_name) != null) return true;
        return false;
    }


    /////////////////////
    //   dObject Methods
    /////////////////

    private String prefix = "Player";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public dPlayer setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public String getType() {
        return "Player";
    }

    @Override
    public String identify() {
        return "p@" + player_name;
    }

    @Override
    public String toString() {
        return identify();
    }


    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return "null";

        if (player_name == null) return "null";
        
        // <--
        // <player> -> dPlayer
        // Returns the dPlayer of the player.
        // -->

        // <--
        // <player.entity> -> dEntity
        // returns the dEntity object of the player
        // -->
        if (attribute.startsWith("entity"))
            return new dEntity(getPlayerEntity())
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.has_played_before> -> Element(boolean)
        // returns true if the player has played before
        // -->
        if (attribute.startsWith("has_played_before"))
            return new Element(String.valueOf(getOfflinePlayer().hasPlayedBefore()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.is_op> -> Element(boolean)
        // returns true if the player has 'op status'
        // -->
        if (attribute.startsWith("is_op"))
            return new Element(String.valueOf(getOfflinePlayer().isOp()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.first_played> -> Element(number)
        // returns the 'System.currentTimeMillis()' of when the player
        // first logged on. Will return '0' if player has never played.
        // -->
        if (attribute.startsWith("first_played"))
            return new Element(String.valueOf(getOfflinePlayer().getFirstPlayed()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.last_played> -> Element(number)
        // returns the 'System.currentTimeMillis()' of when the player
        // was last seen. Will return '0' if player has never played.
        // -->
        if (attribute.startsWith("last_played"))
            return new Element(String.valueOf(getOfflinePlayer().getLastPlayed()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.is_banned> -> Element(boolean)
        // returns true if the player is banned
        // -->
        if (attribute.startsWith("is_banned"))
            return new Element(String.valueOf(getOfflinePlayer().isBanned()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.is_whitelisted> -> Element(boolean)
        // returns true if the player is whitelisted
        // -->
        if (attribute.startsWith("is_whitelisted"))
            return new Element(String.valueOf(getOfflinePlayer().isWhitelisted()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("name") && !isOnline())
            // This can be parsed later with more detail if the player is online, so only check for offline.
            return new Element(player_name).getAttribute(attribute.fulfill(1));

        // <--
        // <player.is_online> -> Element(boolean)
        // returns true if the player is currently online
        // -->
        if (attribute.startsWith("is_online"))
            return new Element(String.valueOf(isOnline())).getAttribute(attribute.fulfill(1));
        
        // Return player ip in format #.#.#.#
        if (attribute.startsWith("ip"))
            return getPlayerEntity().getAddress().getHostName();
        
        if (attribute.startsWith("list")) {
            List<String> players = new ArrayList<String>();
            if (attribute.startsWith("list.online")) {
                for(Player player : Bukkit.getOnlinePlayers())
                    players.add(player.getName());
                return new dList(players).getAttribute(attribute.fulfill(2));
            }
            else if (attribute.startsWith("list.offline")) {
                for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    if (!Bukkit.getOnlinePlayers().toString().contains(player.getName()))
                        players.add(player.getName());
                }
                return new dList(players).getAttribute(attribute.fulfill(2));
            }
            else {
                for(OfflinePlayer player : Bukkit.getOfflinePlayers())
                    players.add(player.getName());
                return new dList(players).getAttribute(attribute.fulfill(1));
            }
        }

        // <--
        // <player.chat_history_list> -> dList
        // Returns a list of the last 10 things the player has said, less
        // if the player hasn't said all that much.
        // -->
        if (attribute.startsWith("chat_history_list"))
            return new dList(PlayerTags.playerChatHistory.get(player_name))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.chat_history> -> Element
        // returns the last thing the player said.
        // -->
        if (attribute.startsWith("chat_history")) {
            int x = 1;
            if (attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1)))
                x = attribute.getIntContext(1);

            return new Element(PlayerTags.playerChatHistory.get(player_name).get(x - 1))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <player.bed_spawn> -> dLocation
        // Returns a dLocation of the player's bed spawn location, 'null' if
        // it doesn't exist.
        // -->
        if (attribute.startsWith("bed_spawn"))
            return new dLocation(getOfflinePlayer().getBedSpawnLocation())
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <player.money> -> Element(number)
        // returns the amount of money the player has with the registered
        // Economy system.
        // -->

        if (attribute.startsWith("money")) {
            if(Depends.economy != null) {

                // <--
                // <player.money.currency_singular> -> Element
                // returns the 'singular currency' string, if supported by the
                // registered Economy system.
                // -->
                if (attribute.startsWith("money.currency_singular"))
                    return new Element(Depends.economy.currencyNameSingular())
                            .getAttribute(attribute.fulfill(2));

                // <--
                // <player.money.currency> -> Element
                // returns the 'currency' string, if supported by the
                // registered Economy system.
                // -->
                if (attribute.startsWith("money.currency"))
                    return new Element(Depends.economy.currencyNamePlural())
                            .getAttribute(attribute.fulfill(2));

                return new Element(String.valueOf(Depends.economy.getBalance(player_name)))
                        .getAttribute(attribute.fulfill(1));

            } else {
                dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                return null;
            }
        }

        if (!isOnline()) return new Element(identify()).getAttribute(attribute);

        // Player is required to be online after this point...

        // <--
        // <player.xp.to_next_level> -> Element(number)
        // returns the amount of experience to the next level.
        // -->
        if (attribute.startsWith("xp.to_next_level"))
            return new Element(String.valueOf(getPlayerEntity().getExpToLevel()))
                    .getAttribute(attribute.fulfill(2)); 

        // <--
        // <player.xp.total> -> Element(number)
        // returns the total amount of experience points.
        // -->
        if (attribute.startsWith("xp.total"))
            return new Element(String.valueOf(getPlayerEntity().getTotalExperience()))
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <player.xp.level> -> Element(number)
        // returns the number of levels the player has.
        // -->
        if (attribute.startsWith("xp.level"))
            return new Element(getPlayerEntity().getLevel())
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <player.xp> -> Element(number)
        // returns the percentage of experience points to the next level.
        // -->
        if (attribute.startsWith("xp"))
            return new Element(String.valueOf(getPlayerEntity().getExp() * 100))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.equipment.boots> -> dItem
        // returns the item the player is wearing as boots, or null
        // if none.
        // -->
        if (attribute.startsWith("equipment.boots"))
            if (getPlayerEntity().getInventory().getBoots() != null)
                return new dItem(getPlayerEntity().getInventory().getBoots())
                        .getAttribute(attribute.fulfill(2));

        // <--
        // <player.equipment.chestplate> -> dItem
        // returns the item the player is wearing as a chestplate, or null
        // if none.
        // -->
        if (attribute.startsWith("equipment.chestplate"))
            if (getPlayerEntity().getInventory().getChestplate() != null)
                return new dItem(getPlayerEntity().getInventory().getChestplate())
                        .getAttribute(attribute.fulfill(2));

        // <--
        // <player.equipment.helmet> -> dItem
        // returns the item the player is wearing as a helmet, or null
        // if none.
        // -->
        if (attribute.startsWith("equipment.helmet"))
            if (getPlayerEntity().getInventory().getHelmet() != null)
                return new dItem(getPlayerEntity().getInventory().getHelmet())
                        .getAttribute(attribute.fulfill(2));

        // <--
        // <player.equipment.leggings> -> dItem
        // returns the item the player is wearing as leggings, or null
        // if none.
        // -->
        if (attribute.startsWith("equipment.leggings"))
            if (getPlayerEntity().getInventory().getLeggings() != null)
                return new dItem(getPlayerEntity().getInventory().getLeggings())
                        .getAttribute(attribute.fulfill(2));

        // <--
        // <player.equipment> -> dInventory
        // returns a dInventory containing the player's equipment
        // -->
        if (attribute.startsWith("equipment"))
            // The only way to return correct size for dInventory
            // created from equipment is to use a CRAFTING type
            // that has the expected 4 slots
            return new dInventory(InventoryType.CRAFTING).add(getPlayerEntity().getInventory().getArmorContents())
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.inventory> -> dInventory
        // returns a dInventory of the player's current inventory.
        // -->
        if (attribute.startsWith("inventory"))
            return new dInventory(getPlayerEntity().getInventory())
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.item_in_hand> -> dItem
        // returns the item the player is holding, or null
        // if none.
        // -->
        if (attribute.startsWith("item_in_hand"))
            return new dItem(getPlayerEntity().getItemInHand())
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.name.display> -> Element
        // returns the 'display name' of the player, which may contain
        // prefixes and suffixes/color, etc.
        // -->
        if (attribute.startsWith("name.display"))
            return new Element(getPlayerEntity().getDisplayName())
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <player.name.list> -> Element
        // returns the name of the player as shown in the 'player list'.
        // -->
        if (attribute.startsWith("name.list"))
            return new Element(getPlayerEntity().getPlayerListName())
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <player.name> -> Element
        // returns the name of the player.
        // -->
        if (attribute.startsWith("name"))
            return new Element(player_name).getAttribute(attribute.fulfill(1));

        // <--
        // <player.eyes> -> dLocation
        // returns a dLocation of the player's eyes.
        // -->
        if (attribute.startsWith("eyes"))
            return new dLocation(getEyeLocation())
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.compass.target> -> dLocation
        // returns a dLocation of the player's 'compass target'.
        // -->
        if (attribute.startsWith("compass_target"))
            return new dLocation(getPlayerEntity().getCompassTarget())
                    .getAttribute(attribute.fulfill(2));

        // <--
        // <player.food_level.formatted> -> Element
        // returns a 'formatted' value of the player's current food level.
        // May be 'starving', 'famished', 'parched, 'hungry' or 'healthy'
        // -->
        if (attribute.startsWith("food_level.formatted")) {
            double maxHunger = getPlayerEntity().getMaxHealth();
            if (attribute.hasContext(2))
                maxHunger = attribute.getIntContext(2);
            if ((float) getPlayerEntity().getFoodLevel() / maxHunger < .10)
                return new Element("starving").getAttribute(attribute.fulfill(2));
            else if ((float) getPlayerEntity().getFoodLevel() / maxHunger < .40)
                return new Element("famished").getAttribute(attribute.fulfill(2));
            else if ((float) getPlayerEntity().getFoodLevel() / maxHunger < .75)
                return new Element("parched").getAttribute(attribute.fulfill(2));
            else if ((float) getPlayerEntity().getFoodLevel() / maxHunger < 1)
                return new Element("hungry").getAttribute(attribute.fulfill(2));

            else return new Element("healthy").getAttribute(attribute.fulfill(2));
        }

        // <--
        // <player.food_level> -> Element(number)
        // returns the current food level of the player.
        // -->
        if (attribute.startsWith("food_level"))
            return new Element(String.valueOf(getPlayerEntity().getFoodLevel()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.has_permission[permission.node]> -> Element(boolean)
        // returns true if the player has the specified node, false otherwise
        // -->
        if (attribute.startsWith("permission")
                || attribute.startsWith("has_permission")) {
            if (Depends.permissions == null) {
                dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return null;
            }

            String permission = attribute.getContext(1);

            // <--
            // <player.has_permission[permission.node].global> -> Element(boolean)
            // returns true if the player has the specified node, regardless of world.
            // this may or may not be functional with your permissions system.
            // -->

            // Non-world specific permission
            if (attribute.getAttribute(2).startsWith("global"))
                return new Element(String.valueOf(Depends.permissions.has((World) null, player_name, permission)))
                        .getAttribute(attribute.fulfill(2));

                // Permission in certain world
            else if (attribute.getAttribute(2).startsWith("world"))
                return new Element(String.valueOf(Depends.permissions.has(attribute.getContext(2), player_name, permission)))
                        .getAttribute(attribute.fulfill(2));

            // <--
            // <player.has_permission[permission.node].world> -> Element(boolean)
            // returns true if the player has the specified node in regards to the
            // player's current world. This may or may not be functional with your
            // permissions system.
            // -->

            // Permission in current world
            return new Element(String.valueOf(Depends.permissions.has(getPlayerEntity(), permission)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <player.flag[flag_name]> -> Flag dList
        // returns 'flag dList' of the player's flag_name specified.
        // -->
        if (attribute.startsWith("flag")) {
            String flag_name;
            if (attribute.hasContext(1)) flag_name = attribute.getContext(1);
            else return "null";
            attribute.fulfill(1);
            if (attribute.startsWith("is_expired")
                    || attribute.startsWith("isexpired"))
                return new Element(!FlagManager.playerHasFlag(this, flag_name))
                        .getAttribute(attribute.fulfill(1));
            if (attribute.startsWith("size") && !FlagManager.playerHasFlag(this, flag_name))
                return new Element(0).getAttribute(attribute.fulfill(1));
            if (FlagManager.playerHasFlag(this, flag_name))
                return new dList(DenizenAPI.getCurrentInstance().flagManager()
                        .getPlayerFlag(getName(), flag_name))
                        .getAttribute(attribute);
            else return "null";
        }

        // <--
        // <player.in_group[group_name]> -> Element(boolean)
        // returns true if the player has the specified group, false otherwise
        // -->
        if (attribute.startsWith("group")
                || attribute.startsWith("in_group")) {
            if (Depends.permissions == null) {
                dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return "null";
            }

            String group = attribute.getContext(1);

            // <--
            // <player.in_group[group_name].global> -> Element(boolean)
            // returns true if the player has the group with no regard to the
            // player's current world. This may or may not be functional with your
            // permissions system.
            // -->

            // Non-world specific permission
            if (attribute.getAttribute(2).startsWith("global"))
                return new Element(String.valueOf(Depends.permissions.playerInGroup((World) null, player_name, group)))
                        .getAttribute(attribute.fulfill(2));

                // Permission in certain world
            else if (attribute.getAttribute(2).startsWith("world"))
                return new Element(String.valueOf(Depends.permissions.playerInGroup(attribute.getContext(2), player_name, group)))
                        .getAttribute(attribute.fulfill(2));

            // <--
            // <player.in_group[group_name].world> -> Element(boolean)
            // returns true if the player has the group in regards to the
            // player's current world. This may or may not be functional with your
            // permissions system.
            // -->

            // Permission in current world
            return new Element(String.valueOf(Depends.permissions.playerInGroup(getPlayerEntity(), group)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--
        // <player.is_flying> -> Element(boolean)
        // returns true if the player is currently flying, false otherwise
        // -->
        if (attribute.startsWith("is_flying"))
            return new Element(String.valueOf(getPlayerEntity().isFlying()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.is_sneaking> -> Element(boolean)
        // returns true if the player is currently sneaking, false otherwise
        // -->
        if (attribute.startsWith("is_sneaking"))
            return new Element(String.valueOf(getPlayerEntity().isSneaking()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.is_blocking> -> Element(boolean)
        // returns true if the player is currently blocking, false otherwise
        // -->
        if (attribute.startsWith("is_blocking"))
            return new Element(String.valueOf(getPlayerEntity().isBlocking()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.is_sleeping> -> Element(boolean)
        // returns true if the player is currently sleeping, false otherwise
        // -->
        if (attribute.startsWith("is_sleeping"))
            return new Element(String.valueOf(getPlayerEntity().isSleeping()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.is_sprinting> -> Element(boolean)
        // returns true if the player is currently sprinting, false otherwise
        // -->
        if (attribute.startsWith("is_sprinting"))
            return new Element(String.valueOf(getPlayerEntity().isSprinting()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.gamemode.id> -> Element(number)
        // returns 'gamemode id' of the player. 0 = survival, 1 = creative, 2 = adventure
        // -->
        if (attribute.startsWith("gamemode.id"))
            return new Element(String.valueOf(getPlayerEntity().getGameMode().getValue()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.gamemode> -> Element
        // returns the name of the gamemode the player is currently set to.
        // -->
        if (attribute.startsWith("gamemode"))
            return new Element(String.valueOf(getPlayerEntity().getGameMode().toString()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.item_on_cursor> -> dItem
        // returns a dItem that the player's cursor is on, if any. This includes
        // chest interfaces, inventories, and hotbars, etc.
        // -->
        if (attribute.startsWith("item_on_cursor"))
            return new dItem(getPlayerEntity().getItemOnCursor())
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.selected_npc> -> dNPC
        // returns the dNPC that the player currently has selected with
        // '/npc sel', null if no player selected.
        // -->
        if (attribute.startsWith("selected_npc")) {
            if (getPlayerEntity().hasMetadata("selected"))
                return dNPC.valueOf(getPlayerEntity().getMetadata("selected").get(0).asString())
                    .getAttribute(attribute.fulfill(1));
            else return "null";
        }

        // <--
        // <player.allowed_flight> -> Element(boolean)
        // returns true if the player is allowed to fly, and false otherwise
        // -->
        if (attribute.startsWith("allowed_flight"))
            return new Element(String.valueOf(getPlayerEntity().getAllowFlight()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.host_name> -> Element
        // returns the player's 'host name'.
        // -->
        if (attribute.startsWith("host_name"))
            return new Element(String.valueOf(getPlayerEntity().getAddress().getHostName()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.time_asleep> -> Duration
        // returns a Duration of the time the player has been asleep.
        // -->
        if (attribute.startsWith("time_asleep"))
            return new Duration(getPlayerEntity().getSleepTicks() / 20)
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.player_time> -> Element
        // returns the time, specific to the player
        // -->
        if (attribute.startsWith("player_time"))
            return new Element(String.valueOf(getPlayerEntity().getPlayerTime()))
                    .getAttribute(attribute.fulfill(1));

        // <--
        // <player.player_time_offset> -> Element
        // returns the player's 'offset' of time vs. the real time.
        // -->
        if (attribute.startsWith("player_time_offset"))
            return new Element(String.valueOf(getPlayerEntity().getPlayerTimeOffset()))
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

        return new dEntity(getPlayerEntity()).getAttribute(attribute.fulfill(0));
    }

}
