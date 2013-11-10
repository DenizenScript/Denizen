package net.aufdemrand.denizen.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.core.PlayerTags;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;

import org.bukkit.*;
import org.bukkit.entity.Player;

public class dPlayer implements dObject, Adjustable {


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

    // <--[language]
    // @name p@
    // @group Object Fetcher System
    // @description
    // p@ refers to the 'object identifier' of a dPlayer. The 'p@' is notation for Denizen's Object
    // Fetcher. The only valid constructor for a dPlayer is the name of the player the object should be
    // associated with. For example, to reference the player named 'mythan', use p@mythan. Player names
    // are case insensitive.
    // -->

    @Fetchable("p")
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
        // If passed null, of course it doesn't match!
        if (arg == null) return false;

        // If passed a identified object that starts with 'p@', return true
        // even if the player doesn't technically exist.
        if (arg.toLowerCase().startsWith("p@")) return true;

        // No identifier supplied? Let's check offlinePlayers. Return true if
        // a match is found.
        OfflinePlayer returnable = null;
        for (OfflinePlayer player : Bukkit.getOfflinePlayers())
            if (player.getName().equalsIgnoreCase(arg)) {
                returnable = player;
                break;
            }
        return returnable != null;
    }


    /////////////////////
    //   CONSTRUCTORS
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
        return getPlayerEntity() != null || getOfflinePlayer() != null;
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
        return getPlayerEntity() != null;
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
    public String getObjectType() {
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
        if (attribute == null)
            return "null";

        if (player_name == null)
            return Element.NULL.getAttribute(attribute);

        /////////////////////
        //   OFFLINE ATTRIBUTES
        /////////////////

        /////////////////////
        //   DEBUG ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.debug.log>
        // @returns Element(Boolean)
        // @description
        // Debugs the player in the log and returns true.
        // -->
        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE.toString())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.debug.no_color>
        // @returns Element
        // @description
        // Returns the player's debug with no color.
        // -->
        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.debug>
        // @returns Element
        // @description
        // Returns the player's debug.
        // -->
        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.prefix>
        // @returns Element
        // @description
        // Returns the dObject's prefix.
        // -->
        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));


        /////////////////////
        //   DENIZEN ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.chat_history_list>
        // @returns dList
        // @description
        // Returns a list of the last 10 things the player has said, less
        // if the player hasn't said all that much.
        // -->
        if (attribute.startsWith("chat_history_list"))
            return new dList(PlayerTags.playerChatHistory.get(player_name))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.chat_history[#]>
        // @returns Element
        // @description
        // returns the last thing the player said.
        // If a number is specified, returns an earlier thing the player said.
        // -->
        if (attribute.startsWith("chat_history")) {
            int x = 1;
            if (attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1)))
                x = attribute.getIntContext(1);
            // No playerchathistory? Return null.
            if (!PlayerTags.playerChatHistory.containsKey(player_name))
                return Element.NULL.getAttribute(attribute.fulfill(1));
            else
                return new Element(PlayerTags.playerChatHistory.get(player_name).get(x - 1))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.flag[flag_name]>
        // @returns Flag dList
        // @description
        // returns the specified flag from the player.
        // -->
        if (attribute.startsWith("flag")) {
            String flag_name;
            if (attribute.hasContext(1)) flag_name = attribute.getContext(1);
            else return Element.NULL.getAttribute(attribute.fulfill(1));
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
            else return Element.NULL.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.has_flag[flag_name]>
        // @returns Element(Boolean)
        // @description
        // returns true if the Player has the specified flag, otherwise returns false.
        // -->
        if (attribute.startsWith("has_flag")) {
            String flag_name;
            if (attribute.hasContext(1)) flag_name = attribute.getContext(1);
            else return Element.NULL.getAttribute(attribute.fulfill(1));
            return new Element(FlagManager.playerHasFlag(this, flag_name)).getAttribute(attribute.fulfill(1));
        }


        if (attribute.startsWith("current_step")) {
            String outcome = "null";
            if (attribute.hasContext(1)) {
                try {
                    outcome = DenizenAPI.getCurrentInstance().getSaves().getString("Players." + getName() + ".Scripts."
                            + dScript.valueOf(attribute.getContext(1)).getName() + ".Current Step");
                } catch (Exception e) {
                    outcome = "null";
                }
            }
            return new Element(outcome).getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   ECONOMY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.money>
        // @returns Element(Decimal)
        // @description
        // returns the amount of money the player has with the registered Economy system.
        // -->

        if (attribute.startsWith("money")) {
            if(Depends.economy != null) {

                // <--[tag]
                // @attribute <p@player.money.currency_singular>
                // @returns Element
                // @description
                // returns the name of a single piece of currency - EG: Dollar
                // (Only if supported by the registered Economy system.)
                // -->
                if (attribute.startsWith("money.currency_singular"))
                    return new Element(Depends.economy.currencyNameSingular())
                            .getAttribute(attribute.fulfill(2));

                // <--[tag]
                // @attribute <p@player.money.currency>
                // @returns Element
                // @description
                // returns the name of multiple pieces of currency - EG: Dollars
                // (Only if supported by the registered Economy system.)
                // -->
                if (attribute.startsWith("money.currency"))
                    return new Element(Depends.economy.currencyNamePlural())
                            .getAttribute(attribute.fulfill(2));

                return new Element(Depends.economy.getBalance(player_name))
                        .getAttribute(attribute.fulfill(1));

            } else {
                dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }
        }


        /////////////////////
        //   ENTITY LIST ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.list>
        // @returns dList(dPlayer)
        // @description
        // Returns all players that have ever played on the server, online or not.
        // ** NOTE: This tag is old. Please instead use <server.list_players> **
        // -->
        if (attribute.startsWith("list")) {
            List<String> players = new ArrayList<String>();

            // <--[tag]
            // @attribute <p@player.list.online>
            // @returns dList(dPlayer)
            // @description
            // Returns all online players.
            // **NOTE: This will only work if there is a player attached to the current script.
            // If you need it anywhere else, use <server.list_online_players>**
            // -->
            if (attribute.startsWith("list.online")) {
                for(Player player : Bukkit.getOnlinePlayers())
                    players.add(player.getName());
                return new dList(players).getAttribute(attribute.fulfill(2));
            }

            // <--[tag]
            // @attribute <p@player.list.offline>
            // @returns dList(dPlayer)
            // @description
            // Returns all players that have ever played on the server, but are not currently online.
            // ** NOTE: This tag is old. Please instead use <server.list_offline_players> **
            // -->
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


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        if (attribute.startsWith("name") && !isOnline())
            // This can be parsed later with more detail if the player is online, so only check for offline.
            return new Element(player_name).getAttribute(attribute.fulfill(1));


        /////////////////////
        //   LOCATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.bed_spawn>
        // @returns dLocation
        // @description
        // Returns the location of the player's bed spawn location, 'null' if
        // it doesn't exist.
        // -->
        if (attribute.startsWith("bed_spawn"))
            return new dLocation(getOfflinePlayer().getBedSpawnLocation())
                    .getAttribute(attribute.fulfill(2));


        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.first_played>
        // @returns Element(Number)
        // @description
        // returns the millisecond time of when the player first logged on to this server.
        // -->
        if (attribute.startsWith("first_played"))
            return new Element(getOfflinePlayer().getFirstPlayed())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.has_played_before>
        // @returns Element(Boolean)
        // @description
        // returns whether the player has played before.
        // -->
        if (attribute.startsWith("has_played_before"))
            return new Element(true)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.health.is_scaled>
        // @returns Element(Boolean)
        // @description
        // returns whether the player's health bar is currently being scaled.
        // -->
        if (attribute.startsWith("health.is_scaled"))
            return new Element(getPlayerEntity().isHealthScaled())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.health.scale>
        // @returns Element(Decimal)
        // @description
        // returns the current scale for the player's health bar
        // -->
        if (attribute.startsWith("health.scale"))
            return new Element(getPlayerEntity().getHealthScale())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.is_banned>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is banned.
        // -->
        if (attribute.startsWith("is_banned"))
            return new Element(getOfflinePlayer().isBanned())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_online>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is currently online.
        // -->
        if (attribute.startsWith("is_online"))
            return new Element(isOnline()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_op>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is a full server operator.
        // -->
        if (attribute.startsWith("is_op"))
            return new Element(getOfflinePlayer().isOp())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_whitelisted>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is whitelisted.
        // -->
        if (attribute.startsWith("is_whitelisted"))
            return new Element(getOfflinePlayer().isWhitelisted())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.last_played>
        // @returns Element(Number)
        // @description
        // returns the millisecond time of when the player
        // was last seen.
        // -->
        if (attribute.startsWith("last_played"))
            return new Element(getOfflinePlayer().getLastPlayed())
                    .getAttribute(attribute.fulfill(1));


        /////////////////////
        //   ONLINE ATTRIBUTES
        /////////////////

        // Player is required to be online after this point...
        if (!isOnline()) return new Element(identify()).getAttribute(attribute);


        /////////////////////
        //   CITIZENS ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.selected_npc>
        // @returns dNPC
        // @description
        // returns the dNPC that the player currently has selected with
        // '/npc select', null if no player selected.
        // -->
        if (attribute.startsWith("selected_npc")) {
            if (getPlayerEntity().hasMetadata("selected"))
                return getSelectedNPC()
                        .getAttribute(attribute.fulfill(1));
            else return Element.NULL.getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   CONVERSION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.entity>
        // @returns dEntity
        // @description
        // returns the dEntity object of the player.
        // (Note: This should never actually be needed. <p@player> is considered a valid dEntity by script commands.)
        // -->
        if (attribute.startsWith("entity") && !attribute.startsWith("entity_"))
            return new dEntity(getPlayerEntity())
                    .getAttribute(attribute.fulfill(1));


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.ip>
        // @returns Element
        // @description
        // returns the player's IP address.
        // -->
        if (attribute.startsWith("ip") ||
                attribute.startsWith("host_name"))
            return new Element(getPlayerEntity().getAddress().getHostName())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.name.display>
        // @returns Element
        // @description
        // returns the display name of the player, which may contain
        // prefixes and suffixes, colors, etc.
        // -->
        if (attribute.startsWith("name.display"))
            return new Element(getPlayerEntity().getDisplayName())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.name.list>
        // @returns Element
        // @description
        // returns the name of the player as shown in the player list.
        // -->
        if (attribute.startsWith("name.list"))
            return new Element(getPlayerEntity().getPlayerListName())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.name>
        // @returns Element
        // @description
        // returns the name of the player.
        // -->
        if (attribute.startsWith("name"))
            return new Element(player_name).getAttribute(attribute.fulfill(1));


        /////////////////////
        //   INVENTORY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.inventory>
        // @returns dInventory
        // @description
        // returns a dInventory of the player's current inventory.
        // -->
        if (attribute.startsWith("inventory"))
            return new dInventory(getPlayerEntity().getInventory())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.item_on_cursor>
        // @returns dItem
        // @description
        // returns a dItem that the player's cursor is on, if any. This includes
        // chest interfaces, inventories, and hotbars, etc.
        // -->
        if (attribute.startsWith("item_on_cursor"))
            return new dItem(getPlayerEntity().getItemOnCursor())
                    .getAttribute(attribute.fulfill(1));


        /////////////////////
        //   PERMISSION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.has_permission[permission.node]>
        // @returns Element(Boolean)
        // @description
        // returns whether the player has the specified node.
        // -->
        if (attribute.startsWith("permission")
                || attribute.startsWith("has_permission")) {
            if (Depends.permissions == null) {
                dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return null;
            }

            String permission = attribute.getContext(1);

            // <--[tag]
            // @attribute <p@player.has_permission[permission.node].global>
            // @returns Element(Boolean)
            // @description
            // returns whether the player has the specified node, regardless of world.
            // (Note: this may or may not be functional with your permissions system.)
            // -->

            // Non-world specific permission
            if (attribute.getAttribute(2).startsWith("global"))
                return new Element(Depends.permissions.has((World) null, player_name, permission))
                        .getAttribute(attribute.fulfill(2));

                // Permission in certain world
            else if (attribute.getAttribute(2).startsWith("world"))
                return new Element(Depends.permissions.has(attribute.getContext(2), player_name, permission))
                        .getAttribute(attribute.fulfill(2));

            // <--[tag]
            // @attribute <p@player.has_permission[permission.node].world>
            // @returns Element(Boolean)
            // @description
            // returns whether the player has the specified node in regards to the
            // player's current world.
            // (Note: This may or may not be functional with your permissions system.)
            // -->

            // Permission in current world
            return new Element(Depends.permissions.has(getPlayerEntity(), permission))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.in_group[group_name]>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is in the specified group.
        // -->
        if (attribute.startsWith("group")
                || attribute.startsWith("in_group")) {
            if (Depends.permissions == null) {
                dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return Element.NULL.getAttribute(attribute.fulfill(1));
            }

            String group = attribute.getContext(1);

            // <--[tag]
            // @attribute <p@player.in_group[group_name].global>
            // @returns Element(Boolean)
            // @description
            // returns whether the player has the group with no regard to the
            // player's current world.
            // (Note: This may or may not be functional with your permissions system.)
            // -->

            // Non-world specific permission
            if (attribute.getAttribute(2).startsWith("global"))
                return new Element(Depends.permissions.playerInGroup((World) null, player_name, group))
                        .getAttribute(attribute.fulfill(2));

                // Permission in certain world
            else if (attribute.getAttribute(2).startsWith("world"))
                return new Element(Depends.permissions.playerInGroup(attribute.getContext(2), player_name, group))
                        .getAttribute(attribute.fulfill(2));

            // <--[tag]
            // @attribute <p@player.in_group[group_name].world>
            // @returns Element(Boolean)
            // @description
            // returns whether the player has the group in regards to the
            // player's current world.
            // (Note: This may or may not be functional with your permissions system.)
            // -->

            // Permission in current world
            return new Element(Depends.permissions.playerInGroup(getPlayerEntity(), group))
                    .getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   LOCATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.compass.target>
        // @returns dLocation
        // @description
        // returns the location of the player's compass target.
        // -->
        if (attribute.startsWith("compass_target"))
            return new dLocation(getPlayerEntity().getCompassTarget())
                    .getAttribute(attribute.fulfill(2));


        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.allowed_flight>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is allowed to fly.
        // -->
        if (attribute.startsWith("allowed_flight"))
            return new Element(getPlayerEntity().getAllowFlight())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.fly_speed>
        // @returns Element(Decimal)
        // @description
        // returns the speed the player can fly at.
        // -->
        if (attribute.startsWith("fly_speed"))
            return new Element(getPlayerEntity().getFlySpeed())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.food_level.formatted>
        // @returns Element
        // @description
        // returns a 'formatted' value of the player's current food level.
        // May be 'starving', 'famished', 'parched, 'hungry' or 'healthy'.
        // -->
        if (attribute.startsWith("food_level.formatted")) {
            double maxHunger = getPlayerEntity().getMaxHealth();
            if (attribute.hasContext(2))
                maxHunger = attribute.getIntContext(2);
            if (getPlayerEntity().getFoodLevel() / maxHunger < .10)
                return new Element("starving").getAttribute(attribute.fulfill(2));
            else if (getPlayerEntity().getFoodLevel() / maxHunger < .40)
                return new Element("famished").getAttribute(attribute.fulfill(2));
            else if (getPlayerEntity().getFoodLevel() / maxHunger < .75)
                return new Element("parched").getAttribute(attribute.fulfill(2));
            else if (getPlayerEntity().getFoodLevel() / maxHunger < 1)
                return new Element("hungry").getAttribute(attribute.fulfill(2));

            else return new Element("healthy").getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.saturation>
        // @returns Element(Decimal)
        // @description
        // returns the current saturation of the player.
        // -->
        if (attribute.startsWith("saturation"))
            return new Element(getPlayerEntity().getSaturation())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.food_level>
        // @returns Element(Number)
        // @description
        // returns the current food level of the player.
        // -->
        if (attribute.startsWith("food_level"))
            return new Element(getPlayerEntity().getFoodLevel())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.oxygen.max>
        // @returns Element(Number)
        // @description
        // returns how much air the player can have.
        // -->
        if (attribute.startsWith("oxygen.max"))
            return new Element(getPlayerEntity().getMaximumAir())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.oxygen>
        // @returns Element(Number)
        // @description
        // returns how much air the player has.
        // -->
        if (attribute.startsWith("oxygen"))
            return new Element(getPlayerEntity().getRemainingAir())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.gamemode.id>
        // @returns Element(Number)
        // @description
        // returns the gamemode ID of the player. 0 = survival, 1 = creative, 2 = adventure
        // -->
        if (attribute.startsWith("gamemode.id"))
            return new Element(getPlayerEntity().getGameMode().getValue())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.gamemode>
        // @returns Element
        // @description
        // returns the name of the gamemode the player is currently set to.
        // -->
        if (attribute.startsWith("gamemode"))
            return new Element(getPlayerEntity().getGameMode().name())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_blocking>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is currently blocking.
        // -->
        if (attribute.startsWith("is_blocking"))
            return new Element(getPlayerEntity().isBlocking())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_flying>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is currently flying.
        // -->
        if (attribute.startsWith("is_flying"))
            return new Element(getPlayerEntity().isFlying())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_sleeping>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is currently sleeping.
        // -->
        if (attribute.startsWith("is_sleeping"))
            return new Element(getPlayerEntity().isSleeping())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_sneaking>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is currently sneaking.
        // -->
        if (attribute.startsWith("is_sneaking"))
            return new Element(getPlayerEntity().isSneaking())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_sprinting>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is currently sprinting.
        // -->
        if (attribute.startsWith("is_sprinting"))
            return new Element(getPlayerEntity().isSprinting())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.time_asleep>
        // @returns Duration
        // @description
        // returns the time the player has been asleep.
        // -->
        if (attribute.startsWith("time_asleep"))
            return new Duration(getPlayerEntity().getSleepTicks() / 20)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.time>
        // @returns Duration
        // @description
        // returns the time the player is currently experiencing. This time could differ from
        // the time that the rest of the world is currently experiencing if a 'time' or 'freeze_time'
        // mechanism is being used on the player.
        // -->
        if (attribute.startsWith("time"))
            return new Element(getPlayerEntity().getPlayerTime())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.walk_speed>
        // @returns Element(Decimal)
        // @description
        // returns the speed the player can walk at.
        // -->
        if (attribute.startsWith("walk_speed"))
            return new Element(getPlayerEntity().getWalkSpeed())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.weather>
        // @returns Element
        // @description
        // returns the type of weather the player is experiencing. This can be different
        // from the weather currently in the world that the player is residing in if
        // the weather is currently being forced onto the player.
        // -->
        if (attribute.startsWith("weather"))
            return new Element(getPlayerEntity().getPlayerWeather().name())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.xp.level>
        // @returns Element(Number)
        // @description
        // returns the number of XP levels the player has.
        // -->
        if (attribute.startsWith("xp.level"))
            return new Element(getPlayerEntity().getLevel())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.xp.to_next_level>
        // @returns Element(Number)
        // @description
        // returns the amount of XP needed to get to the next level.
        // -->
        if (attribute.startsWith("xp.to_next_level"))
            return new Element(getPlayerEntity().getExpToLevel())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.xp.total>
        // @returns Element(Number)
        // @description
        // returns the total amount of experience points.
        // -->
        if (attribute.startsWith("xp.total"))
            return new Element(getPlayerEntity().getTotalExperience())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.xp>
        // @returns Element(Decimal)
        // @description
        // returns the percentage of experience points to the next level.
        // -->
        if (attribute.startsWith("xp"))
            return new Element(getPlayerEntity().getExp() * 100)
                    .getAttribute(attribute.fulfill(1));

        return new dEntity(getPlayerEntity()).getAttribute(attribute);
    }


    @Override
    public void adjust(Mechanism mechanism, Element value) {


        // <--[mechanism]
        // @object dPlayer
        // @name level
        // @input Element(Number)
        // @description
        // Sets the level on the player. Does not affect the current progression
        // of experience towards next level.
        // @tags
        // <player.xp.level>
        // -->
        if (mechanism.matches("level")) {
            getPlayerEntity().setLevel(value.asInt());
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name award_achievement
        // @input Element
        // @description
        // Awards an achievement to the player. Valid achievements:
        // OPEN_INVENTORY, MINE_WOOD, BUILD_WORKBENCH, BUILD_PICKAXE, BUILD_FURNACE, ACQUIRE_IRON,
        // BUILD_HOE, MAKE_BREAD, BAKE_CAKE, BUILD_BETTER_PICKAXE, COOK_FISH, ON_A_RAIL, BUILD_SWORD,
        // KILL_ENEMY, KILL_COW, FLY_PIG, SNIPE_SKELETON, GET_DIAMONDS, NETHER_PORTAL, GHAST_RETURN,
        // GET_BLAZE_ROD, BREW_POTION, END_PORTAL, THE_END, ENCHANTMENTS, OVERKILL, BOOKCASE
        // @tags
        // None
        // -->
        if (mechanism.matches("award_achievement")) {
            getPlayerEntity().awardAchievement(Achievement.valueOf(value.asString().toUpperCase()));
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name health_scale
        // @input Element(Decimal)
        // @description
        // Sets the 'health scale' on the Player. Each heart equals '2'. The standard health scale is
        // 20, so for example, indicating a value of 40 will display double the amount of hearts
        // standard.
        // Player relogging will reset this mechanism.
        // @tags
        // <player.health.scale>
        // -->
        if (mechanism.matches("health_scale")) {
            getPlayerEntity().setHealthScale(value.asDouble());
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name scale_health
        // @input Element(Boolean)
        // @description
        // Enables or disables the health scale value. Disabling will result in the standard
        // amount of hearts being shown.
        // @tags
        // <player.health.is_scaled>
        // -->
        if (mechanism.matches("scale_health")) {
            getPlayerEntity().setHealthScaled(value.asBoolean());
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name texture_pack
        // @input Element
        // @description
        // Sets the current texture pack by specifying a valid URL to a texture pack.
        // @tags
        // None
        // -->
        if (mechanism.matches("texture_pack")) {
            getPlayerEntity().setTexturePack(value.asString());
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name saturation
        // @input Element(Decimal)
        // @description
        // Sets the current food saturation level of a player.
        // @tags
        // <player.saturation>
        // -->
        if (mechanism.matches("saturation")) {
            getPlayerEntity().setSaturation(value.asFloat());
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name food_level
        // @input Element(Number)
        // @description
        // Sets the current food level of a player. Typically, '20' is full.
        // @tags
        // <player.food_level>
        // -->
        if (mechanism.matches("food_level")) {
            getPlayerEntity().setFoodLevel(value.asInt());
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name bed_spawn_location
        // @input dLocation
        // @description
        // Sets the bed location that the player respawns at.
        // @tags
        // <player.bed_spawn>
        // -->
        if (mechanism.matches("bed_spawn_location")) {
            getPlayerEntity().setBedSpawnLocation(dLocation.valueOf(value.asString()));
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name fly_speed
        // @input Element(Decimal)
        // @description
        // Sets the fly speed of the player. Valid range is 0.0 to 1.0
        // @tags
        // <player.fly_speed>
        // -->
        if (mechanism.matches("fly_speed")) {
            getPlayerEntity().setFlySpeed(value.asFloat());
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name weather
        // @input Element
        // @description
        // Sets the weather condition for the player. This does NOT affect the weather
        // in the world, and will block any world weather changes until the 'reset_weather'
        // mechanism is used. Valid weather: CLEAR, DOWNFALL
        // @tags
        // <player.weather>
        // -->
        if (mechanism.matches("weather")) {
            getPlayerEntity().setPlayerWeather(WeatherType.valueOf(value.asString().toUpperCase()));
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name reset_weather
        // @input None
        // @description
        // Resets the weather on the Player to the conditions currently taking place in the Player's
        // current world.
        // @tags
        // <player.weather>
        // -->
        if (mechanism.matches("reset_weather")) {
            getPlayerEntity().resetPlayerWeather();
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name player_list_name
        // @input Element
        // @description
        // Sets the entry that is shown in the 'player list' that is shown when pressing tab.
        // @tags
        // <player.name.list>
        // -->
        if (mechanism.matches("player_list_name")) {
            getPlayerEntity().setPlayerListName(value.asString());
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name time
        // @input Element(Number)
        // @description
        // Sets the time of day the Player is currently experiencing. Setting this will cause the
        // player to have a different time than other Players in the world are experiencing though
        // time will continue to progress. Using the 'reset_time' mechanism, or relogging your player
        // will reset this mechanism to match the world's current time. Valid range is 0-28000
        // @tags
        // <player.time>
        // -->
        if (mechanism.matches("time")) {
            getPlayerEntity().setPlayerTime(value.asInt(), true);
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name freeze_time
        // @input Element(Number)
        // @description
        // Sets the time of day the Player is currently experiencing and freezes it there. Note:
        // there is a small 'twitch effect' when looking at the sky when time is frozen.
        // Setting this will cause the player to have a different time than other Players in
        // the world are experiencing. Using the 'reset_time' mechanism, or relogging your player
        // will reset this mechanism to match the world's current time. Valid range is 0-28000
        // @tags
        // <player.time>
        // -->
        if (mechanism.matches("freeze_time")) {
            if (value == null)
                getPlayerEntity().setPlayerTime(getPlayerEntity().getWorld().getTime(), false);
            else
                getPlayerEntity().setPlayerTime(value.asInt(), false);
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name reset_time
        // @input None
        // @description
        // Resets any altered time that has been applied to this player. Using this will make
        // the Player's time match the world's current time.
        // @tags
        // <player.time>
        // -->
        if (mechanism.matches("reset_time")) {
            getPlayerEntity().resetPlayerTime();
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name walk_speed
        // @input Element(Decimal)
        // @description
        // Sets the walk speed of the player. The standard value is '0.2'. Valid range is 0.0 to 1.0
        // @tags
        // <player.fly_speed>
        // -->
        if (mechanism.matches("walk_speed")) {
            getPlayerEntity().setWalkSpeed(value.asFloat());
            return;
        }

        // Pass along to dEntity mechanism handler if not already handled.
        Adjustable entity = new dEntity(getPlayerEntity());
        entity.adjust(mechanism, value);

    }
}
