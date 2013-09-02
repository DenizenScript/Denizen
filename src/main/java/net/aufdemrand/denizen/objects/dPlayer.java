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

        return returnable != null;
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
        return (!(getPlayerEntity() == null && getOfflinePlayer() == null));
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
        return Bukkit.getPlayer(player_name) != null;
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

        // <--[tag]
        // @attribute <p@player.entity>
        // @returns dEntity
        // @description
        // returns the dEntity object of the player
        // -->
        if (attribute.startsWith("entity"))
            return new dEntity(getPlayerEntity())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.has_played_before>
        // @returns Element(boolean)
        // @description
        // returns true if the player has played before
        // -->
        if (attribute.startsWith("has_played_before"))
            return new Element(String.valueOf(getOfflinePlayer().hasPlayedBefore()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_op>
        // @returns Element(boolean)
        // @description
        // returns true if the player has 'op status'
        // -->
        if (attribute.startsWith("is_op"))
            return new Element(String.valueOf(getOfflinePlayer().isOp()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.first_played>
        // @returns Element(number)
        // @description
        // returns the 'System.currentTimeMillis()' of when the player
        // first logged on. Will return '0' if player has never played.
        // -->
        if (attribute.startsWith("first_played"))
            return new Element(String.valueOf(getOfflinePlayer().getFirstPlayed()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.last_played>
        // @returns Element(number)
        // @description
        // returns the 'System.currentTimeMillis()' of when the player
        // was last seen. Will return '0' if player has never played.
        // -->
        if (attribute.startsWith("last_played"))
            return new Element(String.valueOf(getOfflinePlayer().getLastPlayed()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_banned>
        // @returns Element(boolean)
        // @description
        // returns true if the player is banned
        // -->
        if (attribute.startsWith("is_banned"))
            return new Element(String.valueOf(getOfflinePlayer().isBanned()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_whitelisted>
        // @returns Element(boolean)
        // @description
        // returns true if the player is whitelisted
        // -->
        if (attribute.startsWith("is_whitelisted"))
            return new Element(String.valueOf(getOfflinePlayer().isWhitelisted()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("name") && !isOnline())
            // This can be parsed later with more detail if the player is online, so only check for offline.
            return new Element(player_name).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_online>
        // @returns Element(boolean)
        // @description
        // returns true if the player is currently online
        // -->
        if (attribute.startsWith("is_online"))
            return new Element(String.valueOf(isOnline())).getAttribute(attribute.fulfill(1));
        
        // <--[tag]
        // @attribute <p@player.ip>
        // @returns Element
        // @description
        // Returns the player's IP address.
        // -->
        if (attribute.startsWith("ip") && isOnline())
            return getPlayerEntity().getAddress().getHostName();
        
        // <--[tag]
        // @attribute <p@player.list>
        // @returns dList(dPlayer)
        // @description
        // Returns all players that have ever played on the server, online or not.
        // **NOTE: This will only work if there is a player attached to the current script.
        // If you need it anywhere else, use <server.list_players>**
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
            // Returns all offline players.
            // **NOTE: This will only work if there is a player attached to the current script.
            // If you need it anywhere else, use <server.list_offline_players>**
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
        // @attribute <p@player.chat_history>
        // @returns Element
        // @description
        // returns the last thing the player said.
        // -->
        if (attribute.startsWith("chat_history")) {
            int x = 1;
            if (attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1)))
                x = attribute.getIntContext(1);
            // No playerchathistory? Return null.
            if (!PlayerTags.playerChatHistory.containsKey(player_name)) return "null";
            else return new Element(PlayerTags.playerChatHistory.get(player_name).get(x - 1))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.bed_spawn>
        // @returns dLocation
        // @description
        // Returns a dLocation of the player's bed spawn location, 'null' if
        // it doesn't exist.
        // -->
        if (attribute.startsWith("bed_spawn"))
            return new dLocation(getOfflinePlayer().getBedSpawnLocation())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.money>
        // @returns Element(number)
        // @description
        // returns the amount of money the player has with the registered
        // Economy system.
        // -->

        if (attribute.startsWith("money")) {
            if(Depends.economy != null) {

                // <--[tag]
                // @attribute <p@player.money.currency_singular>
                // @returns Element
                // @description
                // returns the 'singular currency' string, if supported by the
                // registered Economy system.
                // -->
                if (attribute.startsWith("money.currency_singular"))
                    return new Element(Depends.economy.currencyNameSingular())
                            .getAttribute(attribute.fulfill(2));

                // <--[tag]
                // @attribute <p@player.money.currency>
                // @returns Element
                // @description
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


        // <--[tag]
        // @attribute <p@player.xp.to_next_level>
        // @returns Element(number)
        // @description
        // returns the amount of experience to the next level.
        // -->
        if (attribute.startsWith("xp.to_next_level") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().getExpToLevel()))
                    .getAttribute(attribute.fulfill(2)); 

        // <--[tag]
        // @attribute <p@player.xp.total>
        // @returns Element(number)
        // @description
        // returns the total amount of experience points.
        // -->
        if (attribute.startsWith("xp.total") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().getTotalExperience()))
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.xp.level>
        // @returns Element(number)
        // @description
        // returns the number of levels the player has.
        // -->
        if (attribute.startsWith("xp.level") && isOnline())
            return new Element(getPlayerEntity().getLevel())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.xp>
        // @returns Element(number)
        // @description
        // returns the percentage of experience points to the next level.
        // -->
        if (attribute.startsWith("xp") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().getExp() * 100))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.equipment.boots>
        // @returns dItem
        // @description
        // returns the item the player is wearing as boots, or null
        // if none.
        // -->
        if (attribute.startsWith("equipment.boots") && isOnline())
            if (getPlayerEntity().getInventory().getBoots() != null)
                return new dItem(getPlayerEntity().getInventory().getBoots())
                        .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.equipment.chestplate>
        // @returns dItem
        // @description
        // returns the item the player is wearing as a chestplate, or null
        // if none.
        // -->
        if (attribute.startsWith("equipment.chestplate") && isOnline())
            if (getPlayerEntity().getInventory().getChestplate() != null)
                return new dItem(getPlayerEntity().getInventory().getChestplate())
                        .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.equipment.helmet>
        // @returns dItem
        // @description
        // returns the item the player is wearing as a helmet, or null
        // if none.
        // -->
        if (attribute.startsWith("equipment.helmet") && isOnline())
            if (getPlayerEntity().getInventory().getHelmet() != null)
                return new dItem(getPlayerEntity().getInventory().getHelmet())
                        .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.equipment.leggings>
        // @returns dItem
        // @description
        // returns the item the player is wearing as leggings, or null
        // if none.
        // -->
        if (attribute.startsWith("equipment.leggings") && isOnline())
            if (getPlayerEntity().getInventory().getLeggings() != null)
                return new dItem(getPlayerEntity().getInventory().getLeggings())
                        .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.equipment>
        // @returns dInventory
        // @description
        // returns a dInventory containing the player's equipment
        // -->
        if (attribute.startsWith("equipment") && isOnline())
            // The only way to return correct size for dInventory
            // created from equipment is to use a CRAFTING type
            // that has the expected 4 slots
            return new dInventory(InventoryType.CRAFTING).add(getPlayerEntity().getInventory().getArmorContents())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.inventory>
        // @returns dInventory
        // @description
        // returns a dInventory of the player's current inventory.
        // -->
        if (attribute.startsWith("inventory") && isOnline())
            return new dInventory(getPlayerEntity().getInventory())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.item_in_hand>
        // @returns dItem
        // @description
        // returns the item the player is holding, or null
        // if none.
        // -->
        if (attribute.startsWith("item_in_hand") && isOnline())
            return new dItem(getPlayerEntity().getItemInHand())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.name.display>
        // @returns Element
        // @description
        // returns the 'display name' of the player, which may contain
        // prefixes and suffixes/color, etc.
        // -->
        if (attribute.startsWith("name.display") && isOnline())
            return new Element(getPlayerEntity().getDisplayName())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.name.list>
        // @returns Element
        // @description
        // returns the name of the player as shown in the 'player list'.
        // -->
        if (attribute.startsWith("name.list") && isOnline())
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

        // <--[tag]
        // @attribute <p@player.compass.target>
        // @returns dLocation
        // @description
        // returns a dLocation of the player's 'compass target'.
        // -->
        if (attribute.startsWith("compass_target") && isOnline())
            return new dLocation(getPlayerEntity().getCompassTarget())
                    .getAttribute(attribute.fulfill(2));

        // <--[tag]
        // @attribute <p@player.food_level.formatted>
        // @returns Element
        // @description
        // returns a 'formatted' value of the player's current food level.
        // May be 'starving', 'famished', 'parched, 'hungry' or 'healthy'
        // -->
        if (attribute.startsWith("food_level.formatted") && isOnline()) {
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

        // <--[tag]
        // @attribute <p@player.food_level>
        // @returns Element(number)
        // @description
        // returns the current food level of the player.
        // -->
        if (attribute.startsWith("food_level") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().getFoodLevel()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.has_permission[permission.node]>
        // @returns Element(boolean)
        // @description
        // returns true if the player has the specified node, false otherwise
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
            // @returns Element(boolean)
            // @description
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

            // <--[tag]
            // @attribute <p@player.has_permission[permission.node].world>
            // @returns Element(boolean)
            // @description
            // returns true if the player has the specified node in regards to the
            // player's current world. This may or may not be functional with your
            // permissions system.
            // -->

            // Permission in current world
            return new Element(String.valueOf(Depends.permissions.has(getPlayerEntity(), permission)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.flag[flag_name]>
        // @returns Flag dList
        // @description
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

        // <--[tag]
        // @attribute <p@player.in_group[group_name]>
        // @returns Element(boolean)
        // @description
        // returns true if the player has the specified group, false otherwise
        // -->
        if (attribute.startsWith("group")
                || attribute.startsWith("in_group")) {
            if (Depends.permissions == null) {
                dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return "null";
            }

            String group = attribute.getContext(1);

            // <--[tag]
            // @attribute <p@player.in_group[group_name].global>
            // @returns Element(boolean)
            // @description
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

            // <--[tag]
            // @attribute <p@player.in_group[group_name].world>
            // @returns Element(boolean)
            // @description
            // returns true if the player has the group in regards to the
            // player's current world. This may or may not be functional with your
            // permissions system.
            // -->

            // Permission in current world
            return new Element(String.valueOf(Depends.permissions.playerInGroup(getPlayerEntity(), group)))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.is_flying>
        // @returns Element(boolean)
        // @description
        // returns true if the player is currently flying, false otherwise
        // -->
        if (attribute.startsWith("is_flying") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().isFlying()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_sneaking>
        // @returns Element(boolean)
        // @description
        // returns true if the player is currently sneaking, false otherwise
        // -->
        if (attribute.startsWith("is_sneaking") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().isSneaking()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_blocking>
        // @returns Element(boolean)
        // @description
        // returns true if the player is currently blocking, false otherwise
        // -->
        if (attribute.startsWith("is_blocking") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().isBlocking()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_sleeping>
        // @returns Element(boolean)
        // @description
        // returns true if the player is currently sleeping, false otherwise
        // -->
        if (attribute.startsWith("is_sleeping") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().isSleeping()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_sprinting>
        // @returns Element(boolean)
        // @description
        // returns true if the player is currently sprinting, false otherwise
        // -->
        if (attribute.startsWith("is_sprinting") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().isSprinting()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.gamemode.id>
        // @returns Element(number)
        // @description
        // returns 'gamemode id' of the player. 0 = survival, 1 = creative, 2 = adventure
        // -->
        if (attribute.startsWith("gamemode.id") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().getGameMode().getValue()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.gamemode>
        // @returns Element
        // @description
        // returns the name of the gamemode the player is currently set to.
        // -->
        if (attribute.startsWith("gamemode") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().getGameMode().toString()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.item_on_cursor>
        // @returns dItem
        // @description
        // returns a dItem that the player's cursor is on, if any. This includes
        // chest interfaces, inventories, and hotbars, etc.
        // -->
        if (attribute.startsWith("item_on_cursor") && isOnline())
            return new dItem(getPlayerEntity().getItemOnCursor())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.selected_npc>
        // @returns dNPC
        // @description
        // returns the dNPC that the player currently has selected with
        // '/npc sel', null if no player selected.
        // -->
        if (attribute.startsWith("selected_npc") && isOnline()) {
            if (getPlayerEntity().hasMetadata("selected"))
                return dNPC.valueOf(getPlayerEntity().getMetadata("selected").get(0).asString())
                    .getAttribute(attribute.fulfill(1));
            else return "null";
        }

        // <--[tag]
        // @attribute <p@player.allowed_flight>
        // @returns Element(boolean)
        // @description
        // returns true if the player is allowed to fly, and false otherwise
        // -->
        if (attribute.startsWith("allowed_flight") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().getAllowFlight()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.host_name>
        // @returns Element
        // @description
        // returns the player's 'host name'.
        // -->
        if (attribute.startsWith("host_name") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().getAddress().getHostName()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.time_asleep>
        // @returns Duration
        // @description
        // returns a Duration of the time the player has been asleep.
        // -->
        if (attribute.startsWith("time_asleep") && isOnline())
            return new Duration(getPlayerEntity().getSleepTicks() / 20)
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.player_time>
        // @returns Element
        // @description
        // returns the time, specific to the player
        // -->
        if (attribute.startsWith("player_time") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().getPlayerTime()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.player_time_offset>
        // @returns Element
        // @description
        // returns the player's 'offset' of time vs. the real time.
        // -->
        if (attribute.startsWith("player_time_offset") && isOnline())
            return new Element(String.valueOf(getPlayerEntity().getPlayerTimeOffset()))
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.prefix>
        // @returns Element
        // @description
        // Returns the dObject's prefix.
        // -->
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

        if (!isOnline())
            return new Element(identify()).getAttribute(attribute);
        else
            return new dEntity(getPlayerEntity()).getAttribute(attribute);
    }

}
