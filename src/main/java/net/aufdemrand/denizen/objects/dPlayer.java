package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.core.PlayerTags;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class dPlayer implements dObject {


    /////////////////////
    //   STATIC METHODS
    /////////////////

    static Map<String, dPlayer> players = new HashMap<String, dPlayer>();

    public static dPlayer mirrorBukkitPlayer(Player player) {
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
        this.player_name = player.getName();

        // Keep in a map to avoid multiple instances of a dPlayer per player.
        players.put(this.player_name, this);
    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    String player_name;

    public Player getPlayerEntity() {
        return Bukkit.getPlayer(player_name);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(player_name);
    }

    public String getName() {
        return player_name;
    }

    public dLocation getLocation() {
        if (isOnline()) return new dLocation(getPlayerEntity().getLocation());
        else return null;
    }

    public boolean isOnline() {
        if (Bukkit.getPlayer(player_name) != null) return true;
        return false;
    }


    /////////////////////
    //   DSCRIPTARGUMENT METHODS
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
        return "player";
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
        if (attribute == null) return null;

        dB.log("getAttribute: " + getType() + " ---> " + attribute.attributes.toString());

        if (attribute.startsWith("has_played_before"))
            return new Element(String.valueOf(getOfflinePlayer().hasPlayedBefore()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_op"))
            return new Element(String.valueOf(getOfflinePlayer().isOp()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("first_played"))
            return new Element(String.valueOf(getOfflinePlayer().getFirstPlayed()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("last_played"))
            return new Element(String.valueOf(getOfflinePlayer().getLastPlayed()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_banned"))
            return new Element(String.valueOf(getOfflinePlayer().isBanned()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_whitelisted"))
            return new Element(String.valueOf(getOfflinePlayer().isWhitelisted()))
                    .getAttribute(attribute.fulfill(1));

        // This can be parsed later with more detail if the player is online, so only check for offline.
        if (attribute.startsWith("name") && !isOnline())
            return new Element(player_name).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_online"))
            return new Element(String.valueOf(isOnline())).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("chat_history_list"))
            return new dList(PlayerTags.playerChatHistory.get(player_name))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("chat_history")) {
            int x = 1;
            if (attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1)))
                x = attribute.getIntContext(1);
            return new Element(PlayerTags.playerChatHistory.get(player_name).get(x - 1))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("location.bed_spawn"))
            return new dLocation(getOfflinePlayer().getBedSpawnLocation())
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("money")) {
            if(Depends.economy != null) {

                if (attribute.startsWith("money.currency_singular"))
                    return new Element(Depends.economy.currencyNameSingular())
                            .getAttribute(attribute.fulfill(2));

                if (attribute.startsWith("money.currency_plural"))
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


        if (attribute.startsWith("xp.to_next_level"))
            return new Element(String.valueOf(getPlayerEntity().getExpToLevel()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("xp.total"))
            return new Element(String.valueOf(getPlayerEntity().getTotalExperience()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("xp.level"))
            return new Element(String.valueOf(getPlayerEntity().getLevel()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("xp"))
            return new Element(String.valueOf(getPlayerEntity().getExp() * 100))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("item_in_hand"))
            return new dItem(getPlayerEntity().getItemInHand())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("name.display"))
            return new Element(getPlayerEntity().getDisplayName())
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("name.list"))
            return new Element(getPlayerEntity().getPlayerListName())
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("name"))
            return new Element(player_name).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("location.cursor_on")) {
            int range = attribute.getIntContext(2);
            if (range < 1) range = 50;
            return new dLocation(getPlayerEntity().getTargetBlock(null, range).getLocation())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("location.standing_on"))
            return new dLocation(getPlayerEntity().getLocation().add(0, -1, 0))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("location.compass_target"))
            return new dLocation(getPlayerEntity().getCompassTarget())
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("location"))
            return new dLocation(getPlayerEntity().getLocation())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("health.formatted")) {
            int maxHealth = getPlayerEntity().getMaxHealth();
            if (attribute.hasContext(2))
                maxHealth = attribute.getIntContext(2);
            if ((float) getPlayerEntity().getHealth() / maxHealth < .10)
                return new Element("dying").getAttribute(attribute.fulfill(2));
            else if ((float) getPlayerEntity().getHealth() / maxHealth < .40)
                return new Element("seriously wounded").getAttribute(attribute.fulfill(2));
            else if ((float) getPlayerEntity().getHealth() / maxHealth < .75)
                return new Element("injured").getAttribute(attribute.fulfill(2));
            else if ((float) getPlayerEntity().getHealth() / maxHealth < 1)
                return new Element("scraped").getAttribute(attribute.fulfill(2));

            else return new Element("healthy").getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("health.percentage")) {
            int maxHealth = getPlayerEntity().getMaxHealth();
            if (attribute.hasContext(2))
                maxHealth = attribute.getIntContext(2);
            return new Element(String.valueOf(((float) getPlayerEntity().getHealth() / maxHealth) * 100))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("health"))
            return new Element(String.valueOf(getPlayerEntity().getHealth()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("food_level.formatted")) {
            int maxHunger = getPlayerEntity().getMaxHealth();
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

        if (attribute.startsWith("food_level"))
            return new Element(String.valueOf(getPlayerEntity().getFoodLevel()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("permission")) {
            if (Depends.permissions == null) {
                dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return null;
            }

            String permission = attribute.getContext(1);

            // Non-world specific permission
            if (attribute.startsWith("permission.global"))
                return new Element(String.valueOf(Depends.permissions.has((World) null, player_name, permission)))
                        .getAttribute(attribute.fulfill(2));

            // Permission in current world
            return new Element(String.valueOf(Depends.permissions.has(getPlayerEntity(), permission)))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("group")) {
            if (Depends.permissions == null) {
                dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return null;
            }

            String group = attribute.getContext(1);

            // Non-world specific permission
            if (attribute.startsWith("group.global"))
                return new Element(String.valueOf(Depends.permissions.playerInGroup((World) null, player_name, group)))
                        .getAttribute(attribute.fulfill(2));

            // Permission in current world
            return new Element(String.valueOf(Depends.permissions.playerInGroup(getPlayerEntity(), group)))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("is_flying"))
            return new Element(String.valueOf(getPlayerEntity().isFlying()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_sneaking"))
            return new Element(String.valueOf(getPlayerEntity().isSneaking()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_blocking"))
            return new Element(String.valueOf(getPlayerEntity().isBlocking()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_sleeping"))
            return new Element(String.valueOf(getPlayerEntity().isSleeping()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_sprinting"))
            return new Element(String.valueOf(getPlayerEntity().isSprinting()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_inside_vehicle"))
            return new Element(String.valueOf(getPlayerEntity().isInsideVehicle()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("gamemode.id"))
            return new Element(String.valueOf(getPlayerEntity().getGameMode().getValue()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("gamemode"))
            return new Element(String.valueOf(getPlayerEntity().getGameMode().toString()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("item_on_cursor"))
            return new dItem(getPlayerEntity().getItemOnCursor())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("killer"))
            return new dPlayer(getPlayerEntity().getKiller())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("last_damage_cause"))
            return new Element(String.valueOf(getPlayerEntity().getLastDamageCause().getCause().toString()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("last_damage"))
            return new Element(String.valueOf(getPlayerEntity().getLastDamage()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("time_lived"))
            return new Duration(getPlayerEntity().getTicksLived() / 20)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("allowed_flight"))
            return new Element(String.valueOf(getPlayerEntity().getAllowFlight()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("host_name"))
            return new Element(String.valueOf(getPlayerEntity().getAddress().getHostName()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("can_pickup_items"))
            return new Element(String.valueOf(getPlayerEntity().getCanPickupItems()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("entity_id"))
            return new Element(String.valueOf(getPlayerEntity().getEntityId()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("fall_distance"))
            return new Element(String.valueOf(getPlayerEntity().getFallDistance()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("uuid"))
            return new Element(String.valueOf(getPlayerEntity().getUniqueId().toString()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("time_asleep"))
            return new Duration(getPlayerEntity().getSleepTicks() / 20)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("player_time"))
            return new Element(String.valueOf(getPlayerEntity().getPlayerTime()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("player_time_offset"))
            return new Element(String.valueOf(getPlayerEntity().getPlayerTimeOffset()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("has_effect")) {
            // Add later
        }

        if (attribute.startsWith("equipment")) {
            // Add later
        }

        if (attribute.startsWith("world")) {
            // Add world dScriptArg
        }

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

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
