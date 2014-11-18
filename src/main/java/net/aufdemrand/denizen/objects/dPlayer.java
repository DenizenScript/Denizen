package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.objects.properties.PropertyParser;
import net.aufdemrand.denizen.scripts.commands.core.FailCommand;
import net.aufdemrand.denizen.scripts.commands.core.FinishCommand;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.core.PlayerTags;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.nbt.ImprovedOfflinePlayer;
import net.aufdemrand.denizen.utilities.packets.BossHealthBar;
import net.aufdemrand.denizen.utilities.packets.EntityEquipment;
import net.aufdemrand.denizen.utilities.packets.ItemChangeMessage;
import net.aufdemrand.denizen.utilities.packets.PlayerBars;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.util.BlockIterator;

import java.util.*;
import java.util.regex.Pattern;

public class dPlayer implements dObject, Adjustable {


    /////////////////////
    //   STATIC METHODS
    /////////////////


    public static dPlayer mirrorBukkitPlayer(OfflinePlayer player) {
        if (player == null) return null;
        else return new dPlayer(player);
    }

    static Map<String, UUID> playerNames = new HashMap<String, UUID>();

    /**
     * Notes that the player exists, for easy dPlayer valueOf handling.
     */
    public static void notePlayer(OfflinePlayer player) {
        if (player.getName() == null) {
            dB.echoError("Null player " + player.toString());
            return;
        }
        if (!playerNames.containsKey(player.getName().toLowerCase())) {
            playerNames.put(player.getName().toLowerCase(), player.getUniqueId());
        }
    }

    public static boolean isNoted(OfflinePlayer player) {
        return playerNames.containsValue(player.getUniqueId());
    }

    public static Map<String, UUID> getAllPlayers() {
        return playerNames;
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
        return valueOfInternal(string, true);
    }


    static dPlayer valueOfInternal(String string, boolean announce) {
        if (string == null) return null;

        string = string.replace("p@", "").replace("P@", "");

        // Match as a UUID

        if (string.indexOf('-') >= 0) {
            try {
                UUID uuid = UUID.fromString(string);
                if (uuid != null) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    if (player != null) {
                        return new dPlayer(player);
                    }
                }
            }
            catch (IllegalArgumentException e) {
                // Nothing
            }
        }

        // Match as a player name
        if (playerNames.containsKey(string.toLowerCase())) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerNames.get(string.toLowerCase()));
            return new dPlayer(player);
        }

        if (announce)
            dB.echoError("Invalid Player! '" + string + "' could not be found.");

        return null;
    }


    public static boolean matches(String arg) {
        // If passed null, of course it doesn't match!
        if (arg == null) return false;

        // If passed a identified object that starts with 'p@', return true
        // even if the player doesn't technically exist.
        if (arg.toLowerCase().startsWith("p@")) return true;

        arg = arg.replace("p@", "").replace("P@", "");
        if (arg.indexOf('-') >= 0) {
            try {
                UUID uuid = UUID.fromString(arg);
                if (uuid != null) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    if (player != null) {
                        return true;
                    }
                }
            }
            catch (IllegalArgumentException e) {
                // Nothing
            }
        }
        return false;
    }

    public static boolean playerNameIsValid(String name) {
        return playerNames.containsKey(name.toLowerCase());
    }


    /////////////////////
    //   CONSTRUCTORS
    /////////////////

    public dPlayer(OfflinePlayer player) {
        offlinePlayer = player;
    }

    public dPlayer(UUID uuid) {
        offlinePlayer = Bukkit.getOfflinePlayer(uuid);
    }

    public dPlayer(Player player) {
        this((OfflinePlayer)player);
        if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC(player))
            NPCPlayer = player;
    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    OfflinePlayer offlinePlayer = null;

    Player NPCPlayer = null;

    public boolean isValid() {
        return getPlayerEntity() != null || getOfflinePlayer() != null;
    }

    public Player getPlayerEntity() {
        if (NPCPlayer != null) return NPCPlayer;
        if (offlinePlayer == null) return null;
        return Bukkit.getPlayer(offlinePlayer.getUniqueId());
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public ImprovedOfflinePlayer getNBTEditor() {
        return new ImprovedOfflinePlayer(getOfflinePlayer());
    }

    public dEntity getDenizenEntity() {
        return new dEntity(getPlayerEntity());
    }

    public dNPC getSelectedNPC() {
        if (Depends.citizens != null) {
            NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(getPlayerEntity());
            if (npc != null)
                return dNPC.mirrorCitizensNPC(npc);
        }
        return null;
    }

    public String getName() {
        if (offlinePlayer == null)
            return null;
        return offlinePlayer.getName();
    }

    public String getSaveName() {
        if (offlinePlayer == null)
            return "00.UNKNOWN";
        String baseID = offlinePlayer.getUniqueId().toString().toUpperCase().replace("-", "");
        return baseID.substring(0, 2) + "." + baseID;
    }

    public dLocation getLocation() {
        if (isOnline()) return new dLocation(getPlayerEntity().getLocation());
        else return new dLocation(getNBTEditor().getLocation());
    }

    public int getRemainingAir() {
        if (isOnline())
            return getPlayerEntity().getRemainingAir();
        else
            return getNBTEditor().getRemainingAir();
    }

    public int getMaximumAir() {
        if (isOnline())
            return getPlayerEntity().getMaximumAir();
        else
            return 300;
    }

    public dLocation getEyeLocation() {
        if (isOnline()) return new dLocation(getPlayerEntity().getEyeLocation());
        else return null;
    }

    public PlayerInventory getBukkitInventory() {
        if (isOnline()) return getPlayerEntity().getInventory();
        else return getNBTEditor().getInventory();
    }

    public dInventory getInventory() {
        if (isOnline()) return new dInventory(getPlayerEntity().getInventory());
        else return new dInventory(getNBTEditor());
    }

    public CraftingInventory getBukkitWorkbench() {
        if (isOnline()) {
            if (getPlayerEntity().getOpenInventory().getType() == InventoryType.WORKBENCH
                    || getPlayerEntity().getOpenInventory().getType() == InventoryType.CRAFTING)
                return (CraftingInventory) getPlayerEntity().getOpenInventory().getTopInventory();
        }
        return null;
    }

    public dInventory getWorkbench() {
        if (isOnline()) {
            CraftingInventory workbench = getBukkitWorkbench();
            if (workbench != null)
                return new dInventory(workbench, getPlayerEntity());
        }
        return null;
    }

    public Inventory getBukkitEnderChest() {
        if (isOnline()) return getPlayerEntity().getEnderChest();
        else return getNBTEditor().getEnderChest();
    }

    public dInventory getEnderChest() {
        if (isOnline()) return new dInventory(getPlayerEntity().getEnderChest(), getPlayerEntity());
        else return new dInventory(getNBTEditor(), true);
    }

    public World getWorld() {
        if (isOnline()) return getPlayerEntity().getWorld();
        else return null;
    }

    public void decrementStatistic(Statistic statistic, int amount) {
        if (isOnline()) getPlayerEntity().decrementStatistic(statistic, amount);
        else {}// TODO: write to JSON?
    }

    public void decrementStatistic(Statistic statistic, EntityType entity, int amount) {
        if (isOnline() && statistic.getType() == Statistic.Type.ENTITY)
            getPlayerEntity().decrementStatistic(statistic, entity, amount);
        else {}// TODO: write to JSON?
    }

    public void decrementStatistic(Statistic statistic, Material material, int amount) {
        if (isOnline() && (statistic.getType() == Statistic.Type.BLOCK
                || statistic.getType() == Statistic.Type.ITEM))
            getPlayerEntity().decrementStatistic(statistic, material, amount);
        else {}// TODO: write to JSON?
    }

    public void incrementStatistic(Statistic statistic, int amount) {
        if (isOnline()) getPlayerEntity().incrementStatistic(statistic, amount);
        else {}// TODO: write to JSON?
    }

    public void incrementStatistic(Statistic statistic, EntityType entity, int amount) {
        if (isOnline() && statistic.getType() == Statistic.Type.ENTITY)
            getPlayerEntity().incrementStatistic(statistic, entity, amount);
        else {}// TODO: write to JSON?
    }

    public void incrementStatistic(Statistic statistic, Material material, int amount) {
        if (isOnline() && (statistic.getType() == Statistic.Type.BLOCK
                || statistic.getType() == Statistic.Type.ITEM))
            getPlayerEntity().incrementStatistic(statistic, material, amount);
        else {}// TODO: write to JSON?
    }

    public void setStatistic(Statistic statistic, int amount) {
        if (isOnline()) getPlayerEntity().setStatistic(statistic, amount);
        else {}// TODO: write to JSON?
    }

    public void setStatistic(Statistic statistic, EntityType entity, int amount) {
        if (isOnline() && statistic.getType() == Statistic.Type.ENTITY)
            getPlayerEntity().setStatistic(statistic, entity, amount);
        else {}// TODO: write to JSON?
    }

    public void setStatistic(Statistic statistic, Material material, int amount) {
        if (isOnline() && (statistic.getType() == Statistic.Type.BLOCK
                || statistic.getType() == Statistic.Type.ITEM))
            getPlayerEntity().setStatistic(statistic, material, amount);
        else {}// TODO: write to JSON?
    }

    public boolean isOnline() {
        return getPlayerEntity() != null;
    }

    public void setBedSpawnLocation(Location location) {
        if (isOnline())
            getPlayerEntity().setBedSpawnLocation(location);
        else
            getNBTEditor().setBedSpawnLocation(location, getNBTEditor().isSpawnForced());
    }

    public void setLocation(Location location) {
        if (isOnline())
            getPlayerEntity().teleport(location);
        else
            getNBTEditor().setLocation(location);
    }

    public void setMaximumAir(int air) {
        if (isOnline())
            getPlayerEntity().setMaximumAir(air);
        else
            dB.echoError("Cannot set the maximum air of an offline player!");
    }

    public void setRemainingAir(int air) {
        if (isOnline())
            getPlayerEntity().setRemainingAir(air);
        else
            getNBTEditor().setRemainingAir(air);
    }

    public void setLevel(int level) {
        if (isOnline())
            getPlayerEntity().setLevel(level);
        else
            getNBTEditor().setLevel(level);
    }

    public void setFlySpeed(float speed) {
        if (isOnline())
            getPlayerEntity().setFlySpeed(speed);
        else
            getNBTEditor().setFlySpeed(speed);
    }

    public void setGameMode(GameMode mode) {
        if (isOnline())
            getPlayerEntity().setGameMode(mode);
        else
            getNBTEditor().setGameMode(mode);
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
        return (prefix + "='<A>" + identifySimple() + "<G>'  ");
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
        return "p@" + offlinePlayer.getUniqueId().toString();
    }

    @Override
    public String identifySimple() {
        return "p@" + offlinePlayer.getName();
    }

    @Override
    public String toString() {
        return identify();
    }


    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null)
            return "null";

        if (offlinePlayer == null)
            return null;

        /////////////////////
        //   OFFLINE ATTRIBUTES
        /////////////////

        // Defined in dEntity
        if (attribute.startsWith("is_player")) {
            return Element.TRUE.getAttribute(attribute.fulfill(1));
        }

        /////////////////////
        //   DEBUG ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.debug.log>
        // @returns Element(Boolean)
        // @description
        // Debugs the player in the log and returns true.
        // Works with offline players.
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
        // Works with offline players.
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
        // Works with offline players.
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
        // Works with offline players.
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
        // Works with offline players.
        // -->
        if (attribute.startsWith("chat_history_list"))
            return new dList(PlayerTags.playerChatHistory.get(getName())) // TODO: UUID?
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.chat_history[#]>
        // @returns Element
        // @description
        // returns the last thing the player said.
        // If a number is specified, returns an earlier thing the player said.
        // Works with offline players.
        // -->
        if (attribute.startsWith("chat_history")) {
            int x = 1;
            if (attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1)))
                x = attribute.getIntContext(1);
            // No playerchathistory? Return null.
            if (!PlayerTags.playerChatHistory.containsKey(getName())) // TODO: UUID?
                return null;
            else
                return new Element(PlayerTags.playerChatHistory.get(getName()).get(x - 1)) // TODO: UUID?
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.flag[<flag_name>]>
        // @returns Flag dList
        // @description
        // returns the specified flag from the player.
        // Works with offline players.
        // -->
        if (attribute.startsWith("flag")) {
            String flag_name;
            if (attribute.hasContext(1)) flag_name = attribute.getContext(1);
            else return null;
            if (attribute.getAttribute(2).equalsIgnoreCase("is_expired")
                    || attribute.startsWith("isexpired"))
                return new Element(!FlagManager.playerHasFlag(this, flag_name))
                        .getAttribute(attribute.fulfill(2));
            if (attribute.getAttribute(2).equalsIgnoreCase("size") && !FlagManager.playerHasFlag(this, flag_name))
                return new Element(0).getAttribute(attribute.fulfill(2));
            if (FlagManager.playerHasFlag(this, flag_name))
                return new dList(DenizenAPI.getCurrentInstance().flagManager()
                        .getPlayerFlag(this, flag_name))
                        .getAttribute(attribute.fulfill(1));
            return new Element(identify()).getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <p@player.has_flag[<flag_name>]>
        // @returns Element(Boolean)
        // @description
        // returns true if the Player has the specified flag, otherwise returns false.
        // Works with offline players.
        // -->
        if (attribute.startsWith("has_flag")) {
            String flag_name;
            if (attribute.hasContext(1)) flag_name = attribute.getContext(1);
            else return null;
            return new Element(FlagManager.playerHasFlag(this, flag_name)).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.list_flags[(regex:)<search>]>
        // @returns dList
        // @description
        // Returns a list of a player's flag names, with an optional search for
        // names containing a certain pattern.
        // Works with offline players.
        // -->
        if (attribute.startsWith("list_flags")) {
            dList allFlags = new dList(DenizenAPI.getCurrentInstance().flagManager().listPlayerFlags(this));
            dList searchFlags = null;
            if (!allFlags.isEmpty() && attribute.hasContext(1)) {
                searchFlags = new dList();
                String search = attribute.getContext(1).toLowerCase();
                if (search.startsWith("regex:")) {
                    try {
                        Pattern pattern = Pattern.compile(search.substring(6));
                        for (String flag : allFlags)
                            if (pattern.matcher(flag).matches())
                                searchFlags.add(flag);
                    } catch (Exception e) {
                        dB.echoError(e);
                    }
                }
                else {
                    for (String flag : allFlags)
                        if (flag.toLowerCase().contains(search))
                            searchFlags.add(flag);
                }
            }
            return searchFlags == null ? allFlags.getAttribute(attribute.fulfill(1))
                    : searchFlags.getAttribute(attribute.fulfill(1));
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
        // May work offline depending on economy plugin.
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

                return new Element(Depends.economy.getBalance(getName())) // TODO: Vault UUID support?
                        .getAttribute(attribute.fulfill(1));

            } else {
                if (!attribute.hasAlternative())
                    dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                return null;
            }
        }


        /////////////////////
        //   ENTITY LIST ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.target[(<entity>|...)]>
        // @returns dEntity
        // @description
        // Returns the entity that the player is looking at, within a maximum range of 50 blocks,
        // or null if the player is not looking at an entity.
        // Optionally, specify a list of entities, entity types, or 'npc' to only count those targets.
        // -->

        if (attribute.startsWith("target")) {
            int range = 50;
            int attribs = 1;

            // <--[tag]
            // @attribute <p@player.target[(<entity>|...)].within[(<#>)]>
            // @returns dEntity
            // @description
            // Returns the entity that the player is looking at within the specified range limit,
            // or null if the player is not looking at an entity.
            // Optionally, specify a list of entities, entity types, or 'npc' to only count those targets.
            // -->
            if (attribute.getAttribute(2).startsWith("within") &&
                    attribute.hasContext(2) &&
                    aH.matchesInteger(attribute.getContext(2))) {
                attribs = 2;
                range = attribute.getIntContext(2);
            }

            List<Entity> entities = getPlayerEntity().getNearbyEntities(range, range, range);
            ArrayList<LivingEntity> possibleTargets = new ArrayList<LivingEntity>();
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity) {

                    // if we have a context for entity types, check the entity
                    if (attribute.hasContext(1)) {
                        String context = attribute.getContext(1);
                        if (context.toLowerCase().startsWith("li@"))
                            context = context.substring(3);
                        for (String ent: context.split("\\|")) {
                            boolean valid = false;

                            if (ent.equalsIgnoreCase("npc") && Depends.citizens != null
                                    && CitizensAPI.getNPCRegistry().isNPC(entity)) {
                                valid = true;
                            }
                            else if (dEntity.matches(ent)) {
                                // only accept generic entities that are not NPCs
                                if (dEntity.valueOf(ent).isGeneric()) {
                                    if (Depends.citizens == null || !CitizensAPI.getNPCRegistry().isNPC(entity)) {
                                        valid = true;
                                    }
                                }
                                else {
                                    valid = true;
                                }
                            }
                            if (valid) possibleTargets.add((LivingEntity) entity);
                        }
                    } else { // no entity type specified
                        possibleTargets.add((LivingEntity) entity);
                        entity.getType();
                    }
                }
            }

            // Find the valid target
            BlockIterator bi;
            try {
                bi = new BlockIterator(getPlayerEntity(), range);
            }
            catch (IllegalStateException e) {
                return null;
            }
            Block b;
            Location l;
            int bx, by, bz;
            double ex, ey, ez;

            // Loop through player's line of sight
            while (bi.hasNext()) {
                b = bi.next();
                bx = b.getX();
                by = b.getY();
                bz = b.getZ();

                if (b.getType() != Material.AIR) {
                    // Line of sight is broken
                    break;
                }
                else {
                    // Check for entities near this block in the line of sight
                    for (LivingEntity possibleTarget : possibleTargets) {
                        l = possibleTarget.getLocation();
                        ex = l.getX();
                        ey = l.getY();
                        ez = l.getZ();

                        if ((bx - .50 <= ex && ex <= bx + 1.50) &&
                                (bz - .50 <= ez && ez <= bz + 1.50) &&
                                (by - 1 <= ey && ey <= by + 2.5)) {
                            // Entity is close enough, so return it
                            return new dEntity(possibleTarget).getAttribute(attribute.fulfill(attribs));
                        }
                    }
                }
            }
            return null;
        }

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
                    if (!player.isOnline())
                        players.add("p@" + player.getUniqueId().toString());
                }
                return new dList(players).getAttribute(attribute.fulfill(2));
            }
            else {
                for(OfflinePlayer player : Bukkit.getOfflinePlayers())
                    players.add("p@" + player.getUniqueId().toString());
                return new dList(players).getAttribute(attribute.fulfill(1));
            }
        }


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        if (attribute.startsWith("name") && !isOnline())
            // This can be parsed later with more detail if the player is online, so only check for offline.
            return new Element(getName()).getAttribute(attribute.fulfill(1));

        else if (attribute.startsWith("uuid") && !isOnline())
            // This can be parsed later with more detail if the player is online, so only check for offline.
            return new Element(offlinePlayer.getUniqueId().toString()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.save_name>
        // @returns Element
        // @description
        // returns the ID used to save the player in Denizen's saves.yml file.
        // Works with offline players.
        // -->
        if (attribute.startsWith("save_name"))
            return new Element(getSaveName()).getAttribute(attribute.fulfill(1));


        /////////////////////
        //   LOCATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.bed_spawn>
        // @returns dLocation
        // @description
        // Returns the location of the player's bed spawn location, 'null' if
        // it doesn't exist.
        // Works with offline players.
        // -->
        if (attribute.startsWith("bed_spawn"))
            return new dLocation(getOfflinePlayer().getBedSpawnLocation())
                    .getAttribute(attribute.fulfill(2));

        // If online, let dEntity handle location tags since there are more options
        // for online Players

        if (attribute.startsWith("location") && !isOnline()) {
            return getLocation().getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.first_played>
        // @returns Duration
        // @description
        // returns the millisecond time of when the player first logged on to this server.
        // Works with offline players.
        // -->
        if (attribute.startsWith("first_played")) {
            attribute = attribute.fulfill(1);
            if (attribute.startsWith("milliseconds") || attribute.startsWith("in_milliseconds"))
                return new Element(getOfflinePlayer().getFirstPlayed())
                        .getAttribute(attribute.fulfill(1));
            else
                return new Duration(getOfflinePlayer().getFirstPlayed() / 50)
                        .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <p@player.has_played_before>
        // @returns Element(Boolean)
        // @description
        // returns whether the player has played before.
        // Works with offline players.
        // Note: This will just always return true.
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

        // Handle dEntity oxygen tags here to allow getting them when the player is offline
        if (attribute.startsWith("oxygen.max"))
            return new Duration((long) getMaximumAir()).getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("oxygen"))
            return new Duration((long) getRemainingAir()).getAttribute(attribute.fulfill(1));

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
        // Works with offline players (returns false in that case).
        // -->
        if (attribute.startsWith("is_online"))
            return new Element(isOnline()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_op>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is a full server operator.
        // Works with offline players.
        // -->
        if (attribute.startsWith("is_op"))
            return new Element(getOfflinePlayer().isOp())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.is_whitelisted>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is whitelisted.
        // Works with offline players.
        // -->
        if (attribute.startsWith("is_whitelisted"))
            return new Element(getOfflinePlayer().isWhitelisted())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.last_played>
        // @returns Duration
        // @description
        // returns the millisecond time of when the player was last seen.
        // Works with offline players.
        // -->
        if (attribute.startsWith("last_played")) {
            attribute = attribute.fulfill(1);
            if (attribute.startsWith("milliseconds") || attribute.startsWith("in_milliseconds"))
                return new Element(getOfflinePlayer().getLastPlayed())
                        .getAttribute(attribute.fulfill(1));
            else
                return new Duration(getOfflinePlayer().getLastPlayed() / 50)
                        .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <p@player.groups>
        // @returns dList
        // @description
        // returns a list of all groups the player is in.
        // May work with offline players, depending on permission plugin.
        // -->
        if (attribute.startsWith("groups")) {
            if (Depends.permissions == null) {
                if (!attribute.hasAlternative())
                    dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return null;
            }
            dList list = new dList();
            for (String group: Depends.permissions.getGroups()) {
                if (Depends.permissions.playerInGroup(null, offlinePlayer, group)) {
                    list.add(group);
                }
            }
            return list.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.in_group[<group_name>]>
        // @returns Element(Boolean)
        // @description
        // returns whether the player is in the specified group (requires the player to be online)
        // -->
        if (attribute.startsWith("in_group")) {
            if (Depends.permissions == null) {
                if (!attribute.hasAlternative())
                    dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return null;
            }

            String group = attribute.getContext(1);

            // <--[tag]
            // @attribute <p@player.in_group[<group_name>].global>
            // @returns Element(Boolean)
            // @description
            // returns whether the player has the group with no regard to the
            // player's current world.
            // (Works with offline players)
            // (Note: This may or may not be functional with your permissions system.)
            // -->

            // Non-world specific permission
            if (attribute.getAttribute(2).startsWith("global"))
                return new Element(Depends.permissions.playerInGroup((World) null, getName(), group)) // TODO: Vault UUID support?
                        .getAttribute(attribute.fulfill(2));

                // <--[tag]
                // @attribute <p@player.in_group[<group_name>].world>
                // @returns Element(Boolean)
                // @description
                // returns whether the player has the group in regards to the
                // player's current world.
                // (Works with offline players)
                // (Note: This may or may not be functional with your permissions system.)
                // -->

                // Permission in certain world
            else if (attribute.getAttribute(2).startsWith("world"))
                return new Element(Depends.permissions.playerInGroup(attribute.getContext(2), getName(), group)) // TODO: Vault UUID support?
                        .getAttribute(attribute.fulfill(2));

            // Permission in current world
            else if (isOnline())
                return new Element(Depends.permissions.playerInGroup(getPlayerEntity(), group))
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.has_permission[permission.node]>
        // @returns Element(Boolean)
        // @description
        // returns whether the player has the specified node.
        // (Requires the player to be online)
        // -->
        if (attribute.startsWith("permission")
                || attribute.startsWith("has_permission")) {

            String permission = attribute.getContext(1);

            if (Depends.permissions == null) {
                if (!attribute.hasAlternative())
                    dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                return null;
            }

            // <--[tag]
            // @attribute <p@player.has_permission[permission.node].global>
            // @returns Element(Boolean)
            // @description
            // returns whether the player has the specified node, regardless of world.
            // (Works with offline players)
            // (Note: this may or may not be functional with your permissions system.)
            // -->

            // Non-world specific permission
            if (attribute.getAttribute(2).startsWith("global"))
                return new Element(Depends.permissions.has((World) null, getName(), permission)) // TODO: Vault UUID support?
                        .getAttribute(attribute.fulfill(2));

                // <--[tag]
                // @attribute <p@player.has_permission[permission.node].world>
                // @returns Element(Boolean)
                // @description
                // returns whether the player has the specified node in regards to the
                // player's current world.
                // (Works with offline players)
                // (Note: This may or may not be functional with your permissions system.)
                // -->

                // Permission in certain world
            else if (attribute.getAttribute(2).startsWith("world"))
                return new Element(Depends.permissions.has(attribute.getContext(2), getName(), permission)) // TODO: Vault UUID support?
                        .getAttribute(attribute.fulfill(2));

            // Permission in current world
            else if (isOnline())
                return new Element(getPlayerEntity().hasPermission(permission))
                        .getAttribute(attribute.fulfill(1));
        }

        /////////////////////
        //   INVENTORY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.inventory>
        // @returns dInventory
        // @description
        // returns a dInventory of the player's current inventory.
        // Works with offline players.
        // -->
        if (attribute.startsWith("inventory")) {
            return getInventory().getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.enderchest>
        // @returns dInventory
        // @description
        // Gets the player's enderchest inventory.
        // Works with offline players.
        // -->
        if (attribute.startsWith("enderchest"))
            return getEnderChest().getAttribute(attribute.fulfill(1));


        /////////////////////
        //   ONLINE ATTRIBUTES
        /////////////////

        // Player is required to be online after this point...
        if (!isOnline()) return new Element(identify()).getAttribute(attribute);

        // <--[tag]
        // @attribute <p@player.open_inventory>
        // @returns dInventory
        // @description
        // Gets the inventory the player currently has open. If the player has no open
        // inventory, this returns the player's inventory.
        // -->
        if (attribute.startsWith("open_inventory"))
            return new dInventory(getPlayerEntity().getOpenInventory().getTopInventory())
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

        // <--[tag]
        // @attribute <p@player.item_in_hand.slot>
        // @returns Element(Number)
        // @description
        // returns the slot location of the player's selected item.
        // -->
        if (attribute.startsWith("item_in_hand.slot")) {
            return new Element(getPlayerEntity().getInventory().getHeldItemSlot() + 1)
                    .getAttribute(attribute.fulfill(2));
        }


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
            else return null;
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
        // returns the player's IP address host name.
        // -->
        if (attribute.startsWith("ip") ||
                attribute.startsWith("host_name")) {
            attribute = attribute.fulfill(1);
            // <--[tag]
            // @attribute <p@player.ip.address>
            // @returns Element
            // @description
            // returns the player's IP address.
            // -->
            if (attribute.startsWith("address"))
                return new Element(getPlayerEntity().getAddress().toString())
                        .getAttribute(attribute.fulfill(1));

            return new Element(getPlayerEntity().getAddress().getHostName())
                    .getAttribute(attribute);
        }

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
            return new Element(getName()).getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <p@player.has_finished[<script>]>
        // @returns Element(Boolean)
        // @description
        // returns whether the player has finished the specified script.
        // -->
        if (attribute.startsWith("has_finished")) {
            dScript script = dScript.valueOf(attribute.getContext(1));
            if (script == null) return Element.FALSE.getAttribute(attribute.fulfill(1));

            return new Element(FinishCommand.getScriptCompletes(getName(), script.getName()) > 0)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.has_failed[<script>]>
        // @returns Element(Boolean)
        // @description
        // returns whether the player has failed the specified script.
        // -->
        if (attribute.startsWith("has_failed")) {
            dScript script = dScript.valueOf(attribute.getContext(1));
            if (script == null) return Element.FALSE.getAttribute(attribute.fulfill(1));

            return new Element(FailCommand.getScriptFails(getName(), script.getName()) > 0)
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
        // @mechanism dPlayer.can_fly
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
        // @attribute <p@player.gamemode>
        // @returns Element
        // @description
        // returns the name of the gamemode the player is currently set to.
        // -->
        if (attribute.startsWith("gamemode")) {
            attribute = attribute.fulfill(1);
            // <--[tag]
            // @attribute <p@player.gamemode.id>
            // @returns Element(Number)
            // @description
            // returns the gamemode ID of the player. 0 = survival, 1 = creative, 2 = adventure
            // -->
            if (attribute.startsWith("id"))
                return new Element(getPlayerEntity().getGameMode().getValue())
                        .getAttribute(attribute.fulfill(1));
            return new Element(getPlayerEntity().getGameMode().name())
                    .getAttribute(attribute);
        }

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
        // @attribute <p@player.statistic[<statistic>]>
        // @returns Element(Number)
        // @description
        // Returns the player's current value for the specified statistic.
        // -->
        if (attribute.startsWith("statistic")) {
            Statistic statistic = Statistic.valueOf(attribute.getContext(1).toUpperCase());

            // <--[tag]
            // @attribute <p@player.statistic[<statistic>].qualifier[<material>/<entity>]>
            // @returns Element(Number)
            // @description
            // Returns the player's current value for the specified statistic, with the
            // specified qualifier, which can be either an entity or material.
            // -->
            if (attribute.getAttribute(2).startsWith("qualifier")) {
                if (statistic == null) return null;
                dObject obj = ObjectFetcher.pickObjectFor(attribute.getContext(2));
                if (obj instanceof dMaterial)
                    return new Element(getPlayerEntity().getStatistic(statistic, ((dMaterial) obj).getMaterial()))
                            .getAttribute(attribute.fulfill(2));
                else if (obj instanceof dEntity)
                    return new Element(getPlayerEntity().getStatistic(statistic, ((dEntity) obj).getEntityType()))
                            .getAttribute(attribute.fulfill(2));
                else
                    return null;
            }

            if (statistic == null) return null;
            return new Element(getPlayerEntity().getStatistic(statistic)).getAttribute(attribute.fulfill(1));
        }

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
        // returns the type of weather the player is experiencing. This will be different
        // from the weather currently in the world that the player is residing in if
        // the weather is currently being forced onto the player.
        // Returns null if the player does not currently have any forced weather.
        // -->
        if (attribute.startsWith("weather")) {
            if (getPlayerEntity().getPlayerWeather() != null)
                return new Element(getPlayerEntity().getPlayerWeather().name())
                        .getAttribute(attribute.fulfill(1));
            else
                return null;
        }

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

        // <--[tag]
        // @attribute <p@player.type>
        // @returns Element
        // @description
        // Always returns 'Player' for dPlayer objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("type")) {
            return new Element("Player").getAttribute(attribute.fulfill(1));
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) return returned;
        }

        return new dEntity(getPlayerEntity()).getAttribute(attribute);
    }


    public void applyProperty(Mechanism mechanism) {
        dB.echoError("Cannot apply properties to a player!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        Element value = mechanism.getValue();

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
        if (mechanism.matches("level") && mechanism.requireInteger()) {
            setLevel(value.asInt());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name item_slot
        // @input Element(Number)
        // @description
        // Sets the inventory slot that the player has selected.
        // @tags
        // <player.item_in_hand.slot>
        // -->
        if (mechanism.matches("item_slot") && mechanism.requireInteger()) {
            getPlayerEntity().getInventory().setHeldItemSlot(mechanism.getValue().asInt() - 1);
        }

        // <--[mechanism]
        // @object dPlayer
        // @name award_achievement
        // @input Element
        // @description
        // Awards an achievement to the player. Valid achievements:
        // ACQUIRE_IRON, BAKE_CAKE, BOOKCASE, BREED_COW, BREW_POTION, BUILD_BETTER_PICKAXE,
        // BUILD_FURNACE, BUILD_HOE, BUILD_PICKAXE, BUILD_SWORD, BUILD_WORKBENCH, COOK_FISH,
        // DIAMONDS_TO_YOU, ENCHANTMENTS, END_PORTAL, EXPLORE_ALL_BIOMES, FLY_PIG, FULL_BEACON,
        // GET_BLAZE_ROD, GET_DIAMONDS, GHAST_RETURN, KILL_COW, KILL_ENEMY, KILL_WITHER,
        // MAKE_BREAD, MINE_WOOD, NETHER_PORTAL, ON_A_RAIL, OPEN_INVENTORY, OVERKILL,
        // SNIPE_SKELETON, SPAWN_WITHER, THE_END
        // @tags
        // None
        // -->
        // TODO: Player achievement/statistics tags.
        if (mechanism.matches("award_achievement")&& mechanism.requireEnum(false, Achievement.values())) {
            getPlayerEntity().awardAchievement(Achievement.valueOf(value.asString().toUpperCase()));
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
        if (mechanism.matches("health_scale") && mechanism.requireDouble()) {
            getPlayerEntity().setHealthScale(value.asDouble());
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
        if (mechanism.matches("scale_health") && mechanism.requireBoolean()) {
            getPlayerEntity().setHealthScaled(value.asBoolean());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name resource_pack
        // @input Element
        // @description
        // Sets the current resource pack by specifying a valid URL to a resource pack.
        // @tags
        // None
        // -->
        if (mechanism.matches("resource_pack") || mechanism.matches("texture_pack")) {
            getPlayerEntity().setResourcePack(value.asString());
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
        if (mechanism.matches("saturation") && mechanism.requireFloat()) {
            getPlayerEntity().setSaturation(value.asFloat());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name send_map
        // @input Element(Number)
        // @description
        // Forces a player to receive the entirety of the specified map ID instantly.
        // @tags
        // None
        // -->
        if (mechanism.matches("send_map") && mechanism.requireInteger()) {
            MapView map = Bukkit.getServer().getMap((short) value.asInt());
            if (map != null)
                getPlayerEntity().sendMap(map);
            else
                dB.echoError("No map found for ID " + value.asInt() + "!");
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
        if (mechanism.matches("food_level") && mechanism.requireInteger()) {
            getPlayerEntity().setFoodLevel(value.asInt());
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
        if (mechanism.matches("bed_spawn_location") && mechanism.requireObject(dLocation.class)) {
            setBedSpawnLocation(value.asType(dLocation.class));
        }

        // <--[mechanism]
        // @object dPlayer
        // @name can_fly
        // @input Element(Boolean)
        // @description
        // Sets whether the player is allowed to fly.
        // @tags
        // <player.allowed_flight>
        // -->
        if (mechanism.matches("can_fly") && mechanism.requireBoolean()) {
            getPlayerEntity().setAllowFlight(value.asBoolean());
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
        if (mechanism.matches("fly_speed") && mechanism.requireFloat()) {
            setFlySpeed(value.asFloat());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name gamemode
        // @input Element
        // @description
        // Sets the game mode of the player.
        // Valid gamemodes are survival, creative, and adventure.
        // @tags
        // <p@player.gamemode>
        // <p@player.gamemode.id>
        // -->
        if (mechanism.matches("gamemode") && mechanism.requireEnum(false, GameMode.values())) {
            setGameMode(GameMode.valueOf(value.asString().toUpperCase()));
        }

        // <--[mechanism]
        // @object dPlayer
        // @name kick
        // @input Element
        // @description
        // Kicks the player, with the specified message.
        // @tags
        // None
        // -->
        if (mechanism.matches("kick")) {
            getPlayerEntity().kickPlayer(mechanism.getValue().asString());
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
        if (mechanism.matches("weather") && mechanism.requireEnum(false, WeatherType.values())) {
            getPlayerEntity().setPlayerWeather(WeatherType.valueOf(value.asString().toUpperCase()));
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
        }

        // <--[mechanism]
        // @object dPlayer
        // @name display_name
        // @input Element
        // @description
        // Sets the name displayed for the player when chatting.
        // @tags
        // <player.name.display>
        // -->
        if (mechanism.matches("display_name")) {
            getPlayerEntity().setDisplayName(value.asString());
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name show_workbench
        // @input dLocation
        // @description
        // Shows the player a workbench GUI corresponding to a given location.
        // @tags
        // None
        // -->
        if (mechanism.matches("show_workbench") && mechanism.requireObject(dLocation.class)) {
            getPlayerEntity().openWorkbench(mechanism.getValue().asType(dLocation.class), true);
            return;
        }

        // <--[mechanism]
        // @object dPlayer
        // @name location
        // @input dLocation
        // @description
        // If the player is online, teleports the player to a given location.
        // Otherwise, sets the player's next spawn location.
        // @tags
        // <player.location>
        // -->
        if (mechanism.matches("location") && mechanism.requireObject(dLocation.class)) {
            setLocation(value.asType(dLocation.class));
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
        if (mechanism.matches("time") && mechanism.requireInteger()) {
            getPlayerEntity().setPlayerTime(value.asInt(), true);
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
            if (mechanism.requireInteger("Invalid integer specified. Assuming current world time."))
                getPlayerEntity().setPlayerTime(value.asInt(), false);
            else
                getPlayerEntity().setPlayerTime(getPlayerEntity().getWorld().getTime(), false);
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
        }

        // <--[mechanism]
        // @object dPlayer
        // @name walk_speed
        // @input Element(Decimal)
        // @description
        // Sets the walk speed of the player. The standard value is '0.2'. Valid range is 0.0 to 1.0
        // @tags
        // <player.walk_speed>
        // -->
        if (mechanism.matches("walk_speed") && mechanism.requireFloat()) {
            getPlayerEntity().setWalkSpeed(value.asFloat());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name show_entity
        // @input dEntity
        // @description
        // Shows the player an entity. (Must be a player or player NPC).
        // -->
        if (mechanism.matches("show_entity") && mechanism.requireObject(dEntity.class)) {
            getPlayerEntity().showPlayer((Player)value.asType(dEntity.class).getLivingEntity());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name hide_entity
        // @input dEntity
        // @description
        // Hides an entity from the player. (Must be a player or player NPC).
        // -->
        if (mechanism.matches("hide_entity") && mechanism.requireObject(dEntity.class)) {
            getPlayerEntity().hidePlayer((Player)value.asType(dEntity.class).getLivingEntity());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name show_boss_bar
        // @input (Element(Number)|)Element
        // @description
        // Shows the player a boss health bar with the specified text as a name.
        // Use with no input value to remove the bar.
        // Optionally, precede the text with a number indicating the health value. EG:
        // - adjust <player> show_boss_bar:Hello
        // - adjust <player> show_boss_bar:100|Hello
        // @tags
        // None
        // -->
        if (mechanism.matches("show_boss_bar")) {
            if (value.asString().length() > 0) {
                String[] split = value.asString().split("[\\|" + dList.internal_escape + "]", 2);
                if (split.length == 2 && new Element(split[0]).isInt()) {
                    BossHealthBar.displayTextBar(split[1], getPlayerEntity(), new Element(split[0]).asInt());
                }
                else {
                    BossHealthBar.displayTextBar(value.asString(), getPlayerEntity(), 200);
                }
            }
            else {
                BossHealthBar.removeTextBar(getPlayerEntity());
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name fake_experience
        // @input Element(Decimal)(|Element(Number))
        // @description
        // Shows the player a fake experience bar, with a number between 0.0 and 1.0
        // to specify how far along the bar is.
        // Use with no input value to reset to the player's normal experience.
        // Optionally, you can specify a fake experience level.
        // - adjust <player> fake_experience:0.5|5
        // @tags
        // None
        // -->
        if (mechanism.matches("fake_experience")) {
            if (value.asString().length() > 0) {
                String[] split = value.asString().split("[\\|" + dList.internal_escape + "]", 2);
                if (split.length > 0 && new Element(split[0]).isFloat()) {
                    if (split.length > 1 && new Element(split[1]).isInt()) {
                        PlayerBars.showExperience(getPlayerEntity(),
                                new Element(split[0]).asFloat(), new Element(split[1]).asInt());
                    }
                    else
                        PlayerBars.showExperience(getPlayerEntity(),
                                new Element(split[0]).asFloat(), getPlayerEntity().getLevel());
                }
                else {
                    dB.echoError("'" + split[0] + "' is not a valid decimal number!");
                }
            }
            else {
                PlayerBars.resetExperience(getPlayerEntity());
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name fake_health
        // @input Element(Decimal)(|Element(Number)(|Element(Decimal)))
        // @description
        // Shows the player a fake health bar, with a number between 0 and 20,
        // where 1 is half of a heart.
        // Use with no input value to reset to the player's normal health.
        // Optionally, you can specify a fake food level, between 0 and 20.
        // You can also optionally specify a food saturation level between 0 and 10.
        // - adjust <player> fake_health:1
        // - adjust <player> fake_health:10|15
        // - adjust <player> fake_health:<player.health>|3|0
        // @tags
        // None
        // -->
        if (mechanism.matches("fake_health")) {
            if (value.asString().length() > 0) {
                String[] split = value.asString().split("[\\|" + dList.internal_escape + "]", 3);
                if (split.length > 0 && new Element(split[0]).isFloat()) {
                    if (split.length > 1 && new Element(split[1]).isInt()) {
                        if (split.length > 2 && new Element(split[2]).isFloat())
                            PlayerBars.showHealth(getPlayerEntity(), new Element(split[0]).asFloat(),
                                    new Element(split[1]).asInt(), new Element(split[2]).asFloat());
                        else
                            PlayerBars.showHealth(getPlayerEntity(), new Element(split[0]).asFloat(),
                                    new Element(split[1]).asInt(), getPlayerEntity().getSaturation());
                    }
                    else {
                        PlayerBars.showHealth(getPlayerEntity(), new Element(split[0]).asFloat(),
                                getPlayerEntity().getFoodLevel(), getPlayerEntity().getSaturation());
                    }
                }
                else {
                    dB.echoError("'" + split[0] + "' is not a valid decimal number!");
                }
            }
            else {
                PlayerBars.resetHealth(getPlayerEntity());
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name fake_equipment
        // @input dEntity(|Element|dItem)
        // @description
        // Shows the player fake equipment on the specified living entity, which has
        // no real non-visual effects, in the form Entity|Slot|Item, where the slot
        // can be one of the following: HAND, BOOTS, LEGS, CHEST, HEAD
        // Optionally, exclude the slot and item to stop showing the fake equipment,
        // if any, on the specified entity.
        // - adjust <player> fake_equipment:e@123|chest|i@diamond_chestplate
        // - adjust <player> fake_equipment:<player>|head|i@jack_o_lantern
        // -->
        if (mechanism.matches("fake_equipment")) {
            if (value.asString().length() > 0) {
                String[] split = value.asString().split("[\\|" + dList.internal_escape + "]", 3);
                if (split.length > 0 && new Element(split[0]).matchesType(dEntity.class)) {
                    if (split.length > 1 && new Element(split[1]).matchesEnum(EntityEquipment.EquipmentSlots.values())) {
                        if (split.length > 2 && new Element(split[2]).matchesType(dItem.class)) {
                            EntityEquipment.showEquipment(getPlayerEntity(),
                                    new Element(split[0]).asType(dEntity.class).getLivingEntity(),
                                    EntityEquipment.EquipmentSlots.valueOf(new Element(split[1]).asString().toUpperCase()),
                                    new Element(split[2]).asType(dItem.class).getItemStack());
                        }
                        else if (split.length > 2) {
                            dB.echoError("'" + split[2] + "' is not a valid dItem!");
                        }
                    }
                    else if (split.length > 1) {
                        dB.echoError("'" + split[1] + "' is not a valid slot; must be HAND, BOOTS, LEGS, CHEST, or HEAD!");
                    }
                    else {
                        EntityEquipment.resetEquipment(getPlayerEntity(),
                                new Element(split[0]).asType(dEntity.class).getLivingEntity());
                    }
                }
                else {
                    dB.echoError("'" + split[0] + "' is not a valid dEntity!");
                }
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name item_message
        // @input Element
        // @description
        // Shows the player an item message as if the item they are carrying had
        // changed names to the specified Element.
        // -->
        if (mechanism.matches("item_message")) {
            ItemChangeMessage.sendMessage(getPlayerEntity(), value.asString());
        }

        // Iterate through this object's properties' mechanisms
        for (Property property : PropertyParser.getProperties(this)) {
            property.adjust(mechanism);
            if (mechanism.fulfilled())
                break;
        }

        // Pass along to dEntity mechanism handler if not already handled.
        if (!mechanism.fulfilled()) {
            Adjustable entity = new dEntity(getPlayerEntity());
            entity.adjust(mechanism);
        }

    }
}
