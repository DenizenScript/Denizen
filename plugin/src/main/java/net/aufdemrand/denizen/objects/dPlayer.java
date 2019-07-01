package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.abstracts.ImprovedOfflinePlayer;
import net.aufdemrand.denizen.nms.abstracts.Sidebar;
import net.aufdemrand.denizen.nms.interfaces.PlayerHelper;
import net.aufdemrand.denizen.objects.properties.entity.EntityHealth;
import net.aufdemrand.denizen.scripts.commands.player.SidebarCommand;
import net.aufdemrand.denizen.tags.core.PlayerTags;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.entity.BossBarHelper;
import net.aufdemrand.denizen.utilities.packets.ItemChangeMessage;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.aufdemrand.denizencore.utilities.debugging.SlowWarning;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class dPlayer implements dObject, Adjustable, EntityFormObject {


    // <--[language]
    // @name dPlayer
    // @group Object System
    // @description
    // A dPlayer represents a player in the game.
    //
    // For format info, see <@link language p@>
    //
    // -->

    /////////////////////
    //   STATIC METHODS
    /////////////////


    public static dPlayer mirrorBukkitPlayer(OfflinePlayer player) {
        if (player == null) {
            return null;
        }
        else {
            return new dPlayer(player);
        }
    }

    static Map<String, UUID> playerNames = new HashMap<>();

    /**
     * Notes that the player exists, for easy dPlayer valueOf handling.
     */
    public static void notePlayer(OfflinePlayer player) {
        if (player.getName() == null) {
            dB.echoError("Null player " + player.toString());
            return;
        }
        if (!playerNames.containsKey(CoreUtilities.toLowerCase(player.getName()))) {
            playerNames.put(CoreUtilities.toLowerCase(player.getName()), player.getUniqueId());
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
    // Fetcher. The only valid constructor for a dPlayer is the UUID of the player the object should be
    // associated with.
    //
    // For general info, see <@link language dPlayer>
    //
    // -->


    public static dPlayer valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("p")
    public static dPlayer valueOf(String string, TagContext context) {
        return valueOfInternal(string, context, true);
    }

    public static SlowWarning playerByNameWarning = new SlowWarning("");

    public static dPlayer valueOfInternal(String string, boolean announce) {
        return valueOfInternal(string, null, announce);
    }

    public static dPlayer valueOfInternal(String string, TagContext context, boolean defaultAnnounce) {
        if (string == null) {
            return null;
        }
        boolean announce = context == null ? defaultAnnounce : context.debug;

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
        if (playerNames.containsKey(CoreUtilities.toLowerCase(string))) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerNames.get(CoreUtilities.toLowerCase(string)));
            if (announce) {
                playerByNameWarning.message = "Warning: loading player by name - use the UUID instead" +
                        " (or use tag server.match_player)! Player named '" + player.getName() + "' has UUID: " + player.getUniqueId();
                playerByNameWarning.warn(context == null ? null : context.entry);
            }
            return new dPlayer(player);
        }

        if (announce) {
            dB.log("Minor: Invalid Player! '" + string + "' could not be found.");
        }

        return null;
    }


    public static boolean matches(String arg) {
        // If passed null, of course it doesn't match!
        if (arg == null) {
            return false;
        }

        // If passed a identified object that starts with 'p@', return true
        // even if the player doesn't technically exist.
        if (CoreUtilities.toLowerCase(arg).startsWith("p@")) {
            return true;
        }

        arg = arg.replace("p@", "").replace("P@", "");
        if (arg.indexOf('-') >= 0) {
            try {
                UUID uuid = UUID.fromString(arg);
                if (uuid != null) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    if (player != null && player.hasPlayedBefore()) {
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
        return playerNames.containsKey(CoreUtilities.toLowerCase(name));
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
        this((OfflinePlayer) player);
        if (dEntity.isNPC(player)) {
            throw new IllegalStateException("NPCs are not allowed as dPlayer objects!");
        }
    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    OfflinePlayer offlinePlayer = null;

    public boolean isValid() {
        return getPlayerEntity() != null || getOfflinePlayer() != null;
    }

    public Player getPlayerEntity() {
        if (offlinePlayer == null) {
            return null;
        }
        return Bukkit.getPlayer(offlinePlayer.getUniqueId());
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public ImprovedOfflinePlayer getNBTEditor() {
        return NMSHandler.getInstance().getPlayerHelper().getOfflineData(getOfflinePlayer());
    }

    @Override
    public dEntity getDenizenEntity() {
        return new dEntity(getPlayerEntity());
    }

    public dNPC getSelectedNPC() {
        if (Depends.citizens != null && CitizensAPI.hasImplementation()) {
            NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(getPlayerEntity());
            if (npc != null) {
                return dNPC.mirrorCitizensNPC(npc);
            }
        }
        return null;
    }

    public String getName() {
        if (offlinePlayer == null) {
            return null;
        }
        return offlinePlayer.getName();
    }

    public String getSaveName() {
        if (offlinePlayer == null) {
            return "00.UNKNOWN";
        }
        String baseID = offlinePlayer.getUniqueId().toString().toUpperCase().replace("-", "");
        return baseID.substring(0, 2) + "." + baseID;
    }

    public dLocation getLocation() {
        if (isOnline()) {
            return new dLocation(getPlayerEntity().getLocation());
        }
        else {
            return new dLocation(getNBTEditor().getLocation());
        }
    }

    public int getRemainingAir() {
        if (isOnline()) {
            return getPlayerEntity().getRemainingAir();
        }
        else {
            return getNBTEditor().getRemainingAir();
        }
    }

    public int getMaximumAir() {
        if (isOnline()) {
            return getPlayerEntity().getMaximumAir();
        }
        else {
            return 300;
        }
    }

    public double getHealth() {
        if (isOnline()) {
            return getPlayerEntity().getHealth();
        }
        else {
            return getNBTEditor().getHealthFloat();
        }
    }

    public double getMaxHealth() {
        if (isOnline()) {
            return getPlayerEntity().getMaxHealth();
        }
        else {
            return getNBTEditor().getMaxHealth();
        }
    }

    public int getFoodLevel() {
        if (isOnline()) {
            return getPlayerEntity().getFoodLevel();
        }
        else {
            return getNBTEditor().getFoodLevel();
        }
    }

    public dLocation getEyeLocation() {
        if (isOnline()) {
            return new dLocation(getPlayerEntity().getEyeLocation());
        }
        else {
            return null;
        }
    }

    public PlayerInventory getBukkitInventory() {
        if (isOnline()) {
            return getPlayerEntity().getInventory();
        }
        else {
            return getNBTEditor().getInventory();
        }
    }

    public dInventory getInventory() {
        if (isOnline()) {
            return dInventory.mirrorBukkitInventory(getPlayerEntity().getInventory());
        }
        else {
            return new dInventory(getNBTEditor());
        }
    }

    public CraftingInventory getBukkitWorkbench() {
        if (isOnline()) {
            if (getPlayerEntity().getOpenInventory().getType() == InventoryType.WORKBENCH
                    || getPlayerEntity().getOpenInventory().getType() == InventoryType.CRAFTING) {
                return (CraftingInventory) getPlayerEntity().getOpenInventory().getTopInventory();
            }
        }
        return null;
    }

    public dInventory getWorkbench() {
        if (isOnline()) {
            CraftingInventory workbench = getBukkitWorkbench();
            if (workbench != null) {
                return new dInventory(workbench, getPlayerEntity());
            }
        }
        return null;
    }

    public Inventory getBukkitEnderChest() {
        if (isOnline()) {
            return getPlayerEntity().getEnderChest();
        }
        else {
            return getNBTEditor().getEnderChest();
        }
    }

    public dInventory getEnderChest() {
        if (isOnline()) {
            return new dInventory(getPlayerEntity().getEnderChest(), getPlayerEntity());
        }
        else {
            return new dInventory(getNBTEditor(), true);
        }
    }

    public World getWorld() {
        if (isOnline()) {
            return getPlayerEntity().getWorld();
        }
        else {
            return getLocation().getWorld();
        }
    }

    public void decrementStatistic(Statistic statistic, int amount) {
        if (isOnline()) {
            getPlayerEntity().decrementStatistic(statistic, amount);
        }
        else {
        }// TODO: write to JSON?
    }

    public void decrementStatistic(Statistic statistic, EntityType entity, int amount) {
        if (isOnline() && statistic.getType() == Statistic.Type.ENTITY) {
            getPlayerEntity().decrementStatistic(statistic, entity, amount);
        }
        else {
        }// TODO: write to JSON?
    }

    public void decrementStatistic(Statistic statistic, Material material, int amount) {
        if (isOnline() && (statistic.getType() == Statistic.Type.BLOCK
                || statistic.getType() == Statistic.Type.ITEM)) {
            getPlayerEntity().decrementStatistic(statistic, material, amount);
        }
        else {
        }// TODO: write to JSON?
    }

    public void incrementStatistic(Statistic statistic, int amount) {
        if (isOnline()) {
            getPlayerEntity().incrementStatistic(statistic, amount);
        }
        else {
        }// TODO: write to JSON?
    }

    public void incrementStatistic(Statistic statistic, EntityType entity, int amount) {
        if (isOnline() && statistic.getType() == Statistic.Type.ENTITY) {
            getPlayerEntity().incrementStatistic(statistic, entity, amount);
        }
        else {
        }// TODO: write to JSON?
    }

    public void incrementStatistic(Statistic statistic, Material material, int amount) {
        if (isOnline() && (statistic.getType() == Statistic.Type.BLOCK
                || statistic.getType() == Statistic.Type.ITEM)) {
            getPlayerEntity().incrementStatistic(statistic, material, amount);
        }
        else {
        }// TODO: write to JSON?
    }

    public void setStatistic(Statistic statistic, int amount) {
        if (isOnline()) {
            getPlayerEntity().setStatistic(statistic, amount);
        }
        else {
        }// TODO: write to JSON?
    }

    public void setStatistic(Statistic statistic, EntityType entity, int amount) {
        if (isOnline() && statistic.getType() == Statistic.Type.ENTITY) {
            getPlayerEntity().setStatistic(statistic, entity, amount);
        }
        else {
        }// TODO: write to JSON?
    }

    public void setStatistic(Statistic statistic, Material material, int amount) {
        if (isOnline() && (statistic.getType() == Statistic.Type.BLOCK
                || statistic.getType() == Statistic.Type.ITEM)) {
            getPlayerEntity().setStatistic(statistic, material, amount);
        }
        else {
        }// TODO: write to JSON?
    }

    public boolean isOnline() {
        return getPlayerEntity() != null;
    }

    public void setBedSpawnLocation(Location location) {
        if (isOnline()) {
            getPlayerEntity().setBedSpawnLocation(location);
        }
        else {
            getNBTEditor().setBedSpawnLocation(location, getNBTEditor().isSpawnForced());
        }
    }

    public void setLocation(Location location) {
        if (isOnline()) {
            getPlayerEntity().teleport(location);
        }
        else {
            getNBTEditor().setLocation(location);
        }
    }

    public void setMaximumAir(int air) {
        if (isOnline()) {
            getPlayerEntity().setMaximumAir(air);
        }
        else {
            dB.echoError("Cannot set the maximum air of an offline player!");
        }
    }

    public void setRemainingAir(int air) {
        if (isOnline()) {
            getPlayerEntity().setRemainingAir(air);
        }
        else {
            getNBTEditor().setRemainingAir(air);
        }
    }

    public void setHealth(double health) {
        if (isOnline()) {
            getPlayerEntity().setHealth(health);
        }
        else {
            getNBTEditor().setHealthFloat((float) health);
        }
    }

    public void setMaxHealth(double maxHealth) {
        if (isOnline()) {
            getPlayerEntity().setMaxHealth(maxHealth);
        }
        else {
            getNBTEditor().setMaxHealth(maxHealth);
        }
    }

    public void setFoodLevel(int foodLevel) {
        if (isOnline()) {
            getPlayerEntity().setFoodLevel(foodLevel);
        }
        else {
            getNBTEditor().setFoodLevel(foodLevel);
        }
    }

    public void setLevel(int level) {
        if (isOnline()) {
            getPlayerEntity().setLevel(level);
        }
        else {
            getNBTEditor().setLevel(level);
        }
    }

    public void setFlySpeed(float speed) {
        if (isOnline()) {
            getPlayerEntity().setFlySpeed(speed);
        }
        else {
            getNBTEditor().setFlySpeed(speed);
        }
    }

    public void setGameMode(GameMode mode) {
        if (isOnline()) {
            getPlayerEntity().setGameMode(mode);
        }
        else {
            getNBTEditor().setGameMode(mode);
        }
    }

    public boolean hasChunkLoaded(Chunk chunk) {
        return NMSHandler.getInstance().getPlayerHelper().hasChunkLoaded(getPlayerEntity(), chunk);
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
        if (attribute == null) {
            return null;
        }


        if (offlinePlayer == null) {
            return null;
        }

        /////////////////////
        //   OFFLINE ATTRIBUTES
        /////////////////

        // Defined in dEntity
        if (attribute.startsWith("is_player")) {
            return new Element(true).getAttribute(attribute.fulfill(1));
        }

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
        if (attribute.startsWith("chat_history_list")) {
            return new dList(PlayerTags.playerChatHistory.get(getPlayerEntity().getUniqueId()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.chat_history[#]>
        // @returns Element
        // @description
        // Returns the last thing the player said.
        // If a number is specified, returns an earlier thing the player said.
        // Works with offline players.
        // -->
        if (attribute.startsWith("chat_history")) {
            int x = 1;
            if (attribute.hasContext(1) && aH.matchesInteger(attribute.getContext(1))) {
                x = attribute.getIntContext(1);
            }
            // No playerchathistory? Return null.
            if (!PlayerTags.playerChatHistory.containsKey(getPlayerEntity().getUniqueId())) {
                return null;
            }
            List<String> messages = PlayerTags.playerChatHistory.get(getPlayerEntity().getUniqueId());
            if (messages.size() < x || x < 1) {
                return null;
            }
            return new Element(messages.get(x - 1))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.flag[<flag_name>]>
        // @returns Flag dList
        // @description
        // Returns the specified flag from the player.
        // Works with offline players.
        // -->
        if (attribute.startsWith("flag") && attribute.hasContext(1)) {
            String flag_name = attribute.getContext(1);
            if (attribute.getAttribute(2).equalsIgnoreCase("is_expired")
                    || attribute.startsWith("isexpired")) {
                return new Element(!FlagManager.playerHasFlag(this, flag_name))
                        .getAttribute(attribute.fulfill(2));
            }
            if (attribute.getAttribute(2).equalsIgnoreCase("size") && !FlagManager.playerHasFlag(this, flag_name)) {
                return new Element(0).getAttribute(attribute.fulfill(2));
            }
            if (FlagManager.playerHasFlag(this, flag_name)) {
                FlagManager.Flag flag = DenizenAPI.getCurrentInstance().flagManager()
                        .getPlayerFlag(this, flag_name);
                return new dList(flag.toString(), true, flag.values())
                        .getAttribute(attribute.fulfill(1));
            }
            return new Element(identify()).getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <p@player.has_flag[<flag_name>]>
        // @returns Element(Boolean)
        // @description
        // Returns true if the Player has the specified flag, otherwise returns false.
        // Works with offline players.
        // -->
        if (attribute.startsWith("has_flag") && attribute.hasContext(1)) {
            String flag_name = attribute.getContext(1);
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
                String search = attribute.getContext(1);
                if (search.startsWith("regex:")) {
                    try {
                        Pattern pattern = Pattern.compile(search.substring(6), Pattern.CASE_INSENSITIVE);
                        for (String flag : allFlags) {
                            if (pattern.matcher(flag).matches()) {
                                searchFlags.add(flag);
                            }
                        }
                    }
                    catch (Exception e) {
                        dB.echoError(e);
                    }
                }
                else {
                    search = CoreUtilities.toLowerCase(search);
                    for (String flag : allFlags) {
                        if (CoreUtilities.toLowerCase(flag).contains(search)) {
                            searchFlags.add(flag);
                        }
                    }
                }
                DenizenAPI.getCurrentInstance().flagManager().shrinkPlayerFlags(this, searchFlags);
            }
            else {
                DenizenAPI.getCurrentInstance().flagManager().shrinkPlayerFlags(this, allFlags);
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
                }
                catch (Exception e) {
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
        // @plugin Vault
        // @description
        // Returns the amount of money the player has with the registered Economy system.
        // May work offline depending on economy provider.
        // @mechanism dPlayer.money
        // -->

        if (attribute.startsWith("money")) {
            if (Depends.economy != null) {

                // <--[tag]
                // @attribute <p@player.money.formatted>
                // @returns Element
                // @plugin Vault
                // @description
                // Returns the formatted form of the player's money balance in the registered Economy system.
                // -->
                if (attribute.startsWith("money.formatted")) {
                    return new Element(Depends.economy.format(Depends.economy.getBalance(getOfflinePlayer())))
                            .getAttribute(attribute.fulfill(2));
                }

                // <--[tag]
                // @attribute <p@player.money.currency_singular>
                // @returns Element
                // @plugin Vault
                // @description
                // Returns the name of a single piece of currency - For example: Dollar
                // (Only if supported by the registered Economy system.)
                // -->
                if (attribute.startsWith("money.currency_singular")) {
                    return new Element(Depends.economy.currencyNameSingular())
                            .getAttribute(attribute.fulfill(2));
                }

                // <--[tag]
                // @attribute <p@player.money.currency>
                // @returns Element
                // @plugin Vault
                // @description
                // Returns the name of multiple pieces of currency - For example: Dollars
                // (Only if supported by the registered Economy system.)
                // -->
                if (attribute.startsWith("money.currency")) {
                    return new Element(Depends.economy.currencyNamePlural())
                            .getAttribute(attribute.fulfill(2));
                }

                return new Element(Depends.economy.getBalance(getOfflinePlayer()))
                        .getAttribute(attribute.fulfill(1));

            }
            else {
                if (!attribute.hasAlternative()) {
                    dB.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                }
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
            // Returns the living entity that the player is looking at within the specified range limit,
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
            ArrayList<LivingEntity> possibleTargets = new ArrayList<>();
            if (!attribute.hasContext(1)) {
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity) {
                        possibleTargets.add((LivingEntity) entity);
                    }
                }
            }
            else {
                dList list = dList.getListFor(attribute.getContextObject(1));
                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity) {
                        for (dObject obj : list.objectForms) {
                            boolean valid = false;
                            dEntity filterEntity = null;
                            if (obj instanceof dEntity) {
                                filterEntity = (dEntity) obj;
                            }
                            else if (CoreUtilities.toLowerCase(obj.toString()).equals("npc")) {
                                valid = dEntity.isCitizensNPC(entity);
                            }
                            else {
                                filterEntity = dEntity.getEntityFor(obj, attribute.context);
                                if (filterEntity == null) {
                                    dB.echoError("Trying to filter 'player.target[...]' tag with invalid input: " + obj.toString());
                                    continue;
                                }
                            }
                            if (!valid && filterEntity != null) {
                                if (filterEntity.isGeneric()) {
                                    valid = filterEntity.getBukkitEntityType().equals(entity.getType());
                                }
                                else {
                                    valid = filterEntity.getUUID().equals(entity.getUniqueId());
                                }
                            }
                            if (valid) {
                                possibleTargets.add((LivingEntity) entity);
                                break;
                            }
                        }
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

                if (b.getType().isSolid()) {
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
                            return new dEntity(possibleTarget).getDenizenObject().getAttribute(attribute.fulfill(attribs));
                        }
                    }
                }
            }
            return null;
        }

        // workaround for <e@entity.list_effects>
        if (attribute.startsWith("list_effects")) {
            dList effects = new dList();
            for (PotionEffect effect : getPlayerEntity().getActivePotionEffects()) {
                effects.add(effect.getType().getName() + "," + effect.getAmplifier() + "," + effect.getDuration() + "t");
            }
            return effects.getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("list")) {
            dB.echoError("DO NOT USE PLAYER.LIST AS A TAG, please use <server.list_online_players> and related tags!");
            List<String> players = new ArrayList<>();

            if (attribute.startsWith("list.online")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    players.add(player.getName());
                }
                return new dList(players).getAttribute(attribute.fulfill(2));
            }
            else if (attribute.startsWith("list.offline")) {
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    if (!player.isOnline()) {
                        players.add("p@" + player.getUniqueId().toString());
                    }
                }
                return new dList(players).getAttribute(attribute.fulfill(2));
            }
            else {
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    players.add("p@" + player.getUniqueId().toString());
                }
                return new dList(players).getAttribute(attribute.fulfill(1));
            }
        }


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        if (attribute.startsWith("name") && !isOnline())
        // This can be parsed later with more detail if the player is online, so only check for offline.
        {
            return new Element(getName()).getAttribute(attribute.fulfill(1));
        }
        else if (attribute.startsWith("uuid") && !isOnline())
        // This can be parsed later with more detail if the player is online, so only check for offline.
        {
            return new Element(offlinePlayer.getUniqueId().toString()).getAttribute(attribute.fulfill(1));
        }

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

        // <--[tag]
        // @attribute <p@player.save_name>
        // @returns Element
        // @description
        // Returns the ID used to save the player in Denizen's saves.yml file.
        // Works with offline players.
        // -->
        if (attribute.startsWith("save_name")) {
            return new Element(getSaveName()).getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   LOCATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.bed_spawn>
        // @returns dLocation
        // @description
        // Returns the location of the player's bed spawn location, null if
        // it doesn't exist.
        // Works with offline players.
        // @mechanism dPlayer.bed_spawn_location
        // -->
        if (attribute.startsWith("bed_spawn")) {
            if (getOfflinePlayer().getBedSpawnLocation() == null) {
                return null;
            }
            return new dLocation(getOfflinePlayer().getBedSpawnLocation())
                    .getAttribute(attribute.fulfill(1));
        }

        // If online, let dEntity handle location tags since there are more options
        // for online Players

        if (attribute.startsWith("location") && !isOnline()) {
            return getLocation().getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("world") && !isOnline()) {
            return new dWorld(getWorld()).getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.item_cooldown[<material>]>
        // @returns Duration
        // @description
        // Returns the cooldown duration remaining on player's material.
        // -->
        if (attribute.startsWith("item_cooldown")) {
            dMaterial mat = new Element(attribute.getContext(1)).asType(dMaterial.class, attribute.context);
            if (mat != null) {
                return new Duration((long) getPlayerEntity().getCooldown(mat.getMaterial()))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <p@player.first_played>
        // @returns Duration
        // @description
        // Returns the millisecond time of when the player first logged on to this server.
        // Works with offline players.
        // -->
        if (attribute.startsWith("first_played")) {
            attribute = attribute.fulfill(1);
            if (attribute.startsWith("milliseconds") || attribute.startsWith("in_milliseconds")) {
                return new Element(getOfflinePlayer().getFirstPlayed())
                        .getAttribute(attribute.fulfill(1));
            }
            return new Duration(getOfflinePlayer().getFirstPlayed() / 50)
                    .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <p@player.has_played_before>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player has played before.
        // Works with offline players.
        // Note: This will just always return true.
        // -->
        if (attribute.startsWith("has_played_before")) {
            return new Element(true)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.absorption_health>
        // @returns Element(Decimal)
        // @description
        // Returns the player's absorption health.
        // @mechanism dPlayer.absorption_health
        // -->
        if (attribute.startsWith("absorption_health")) {
            return new Element(NMSHandler.getInstance().getPlayerHelper().getAbsorption(getPlayerEntity()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.health.is_scaled>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player's health bar is currently being scaled.
        // -->
        if (attribute.startsWith("health.is_scaled")) {
            return new Element(getPlayerEntity().isHealthScaled())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.health.scale>
        // @returns Element(Decimal)
        // @description
        // Returns the current scale for the player's health bar
        // -->
        if (attribute.startsWith("health.scale")) {
            return new Element(getPlayerEntity().getHealthScale())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.exhaustion>
        // @returns Element(Decimal)
        // @description
        // Returns how fast the food level drops (exhaustion).
        // -->
        if (attribute.startsWith("exhaustion")) {
            return new Element(getPlayerEntity().getExhaustion())
                    .getAttribute(attribute.fulfill(1));
        }

        // Handle dEntity oxygen tags here to allow getting them when the player is offline
        if (attribute.startsWith("oxygen.max")) {
            return new Duration((long) getMaximumAir()).getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("oxygen")) {
            return new Duration((long) getRemainingAir()).getAttribute(attribute.fulfill(1));
        }

        // Same with health tags
        if (attribute.startsWith("health.formatted")) {
            return EntityHealth.getHealthFormatted(new dEntity(getPlayerEntity()), attribute);
        }

        if (attribute.startsWith("health.percentage")) {
            double maxHealth = getPlayerEntity().getMaxHealth();
            if (attribute.hasContext(2)) {
                maxHealth = attribute.getIntContext(2);
            }
            return new Element((getPlayerEntity().getHealth() / maxHealth) * 100)
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("health.max")) {
            return new Element(getMaxHealth()).getAttribute(attribute.fulfill(2));
        }

        if (attribute.matches("health")) {
            return new Element(getHealth()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.is_banned>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is banned.
        // -->
        if (attribute.startsWith("is_banned")) {
            BanEntry ban = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(getName());
            if (ban == null) {
                return new Element(false).getAttribute(attribute.fulfill(1));
            }
            else if (ban.getExpiration() == null) {
                return new Element(true).getAttribute(attribute.fulfill(1));
            }
            return new Element(ban.getExpiration().after(new Date())).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.is_online>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is currently online.
        // Works with offline players (returns false in that case).
        // -->
        if (attribute.startsWith("is_online")) {
            return new Element(isOnline()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.is_op>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is a full server operator.
        // Works with offline players.
        // @mechanism dPlayer.is_op
        // -->
        if (attribute.startsWith("is_op")) {
            return new Element(getOfflinePlayer().isOp())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.is_whitelisted>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is whitelisted.
        // Works with offline players.
        // @mechanism dPlayer.is_whitelisted
        // -->
        if (attribute.startsWith("is_whitelisted")) {
            return new Element(getOfflinePlayer().isWhitelisted())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.last_played>
        // @returns Duration
        // @description
        // Returns the datestamp of when the player was last seen in duration.
        // Works with offline players.
        // -->
        if (attribute.startsWith("last_played")) {
            attribute = attribute.fulfill(1);
            if (attribute.startsWith("milliseconds") || attribute.startsWith("in_milliseconds")) {
                if (isOnline()) {
                    return new Element(System.currentTimeMillis())
                            .getAttribute(attribute.fulfill(1));
                }
                return new Element(getOfflinePlayer().getLastPlayed())
                        .getAttribute(attribute.fulfill(1));
            }
            if (isOnline()) {
                return new Duration(System.currentTimeMillis() / 50)
                        .getAttribute(attribute);
            }
            return new Duration(getOfflinePlayer().getLastPlayed() / 50)
                    .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <p@player.groups>
        // @returns dList
        // @description
        // Returns a list of all groups the player is in.
        // May work with offline players, depending on permission plugin.
        // -->
        if (attribute.startsWith("groups")) {
            if (Depends.permissions == null) {
                if (!attribute.hasAlternative()) {
                    dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                }
                return null;
            }
            dList list = new dList();
            // TODO: optionally specify world
            for (String group : Depends.permissions.getGroups()) {
                if (Depends.permissions.playerInGroup(null, offlinePlayer, group)) {
                    list.add(group);
                }
            }
            return list.getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("ban_info")) {
            attribute.fulfill(1);
            BanEntry ban = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(getName());
            if (ban == null || (ban.getExpiration() != null && ban.getExpiration().before(new Date()))) {
                return null;
            }

            // <--[tag]
            // @attribute <p@player.ban_info.expiration>
            // @returns Duration
            // @description
            // Returns the expiration of the player's ban, if they are banned.
            // Potentially can be null.
            // -->
            if (attribute.startsWith("expiration") && ban.getExpiration() != null) {
                return new Duration(ban.getExpiration().getTime() / 50)
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <p@player.ban_info.reason>
            // @returns Element
            // @description
            // Returns the reason for the player's ban, if they are banned.
            // -->
            else if (attribute.startsWith("reason")) {
                return new Element(ban.getReason())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <p@player.ban_info.created>
            // @returns Duration
            // @description
            // Returns when the player's ban was created, if they are banned.
            // -->
            else if (attribute.startsWith("created")) {
                return new Duration(ban.getCreated().getTime() / 50)
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <p@player.ban_info.source>
            // @returns Element
            // @description
            // Returns the source of the player's ban, if they are banned.
            // -->
            else if (attribute.startsWith("source")) {
                return new Element(ban.getSource())
                        .getAttribute(attribute.fulfill(1));
            }

            return null;
        }

        // <--[tag]
        // @attribute <p@player.in_group[<group_name>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is in the specified group.
        // This requires an online player - if the player may be offline, consider using
        // <@link tag p@player.in_group[group_name].global>.
        // -->
        if (attribute.startsWith("in_group")) {
            if (Depends.permissions == null) {
                if (!attribute.hasAlternative()) {
                    dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                }
                return null;
            }

            String group = attribute.getContext(1);

            // <--[tag]
            // @attribute <p@player.in_group[<group_name>].global>
            // @returns Element(Boolean)
            // @description
            // Returns whether the player has the group with no regard to the
            // player's current world.
            // (Works with offline players)
            // (Note: This may or may not be functional with your permissions system.)
            // -->

            // Non-world specific permission
            if (attribute.getAttribute(2).startsWith("global")) {
                return new Element(Depends.permissions.playerInGroup((World) null, getName(), group)) // TODO: Vault UUID support?
                        .getAttribute(attribute.fulfill(2));
            }

            // <--[tag]
            // @attribute <p@player.in_group[<group_name>].world[<world>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the player has the group in regards to a specific world.
            // (Works with offline players)
            // (Note: This may or may not be functional with your permissions system.)
            // -->

            // Permission in certain world
            else if (attribute.getAttribute(2).startsWith("world")) {
                return new Element(Depends.permissions.playerInGroup(attribute.getContext(2), getName(), group)) // TODO: Vault UUID support?
                        .getAttribute(attribute.fulfill(2));
            }

            // Permission in current world
            else if (isOnline()) {
                return new Element(Depends.permissions.playerInGroup(getPlayerEntity(), group))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <p@player.has_permission[permission.node]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player has the specified node.
        // (Requires the player to be online)
        // -->
        if (attribute.startsWith("permission")
                || attribute.startsWith("has_permission")) {

            String permission = attribute.getContext(1);

            // <--[tag]
            // @attribute <p@player.has_permission[permission.node].global>
            // @returns Element(Boolean)
            // @description
            // Returns whether the player has the specified node, regardless of world.
            // (Works with offline players)
            // (Note: this may or may not be functional with your permissions system.)
            // -->

            // Non-world specific permission
            if (attribute.getAttribute(2).startsWith("global")) {
                if (Depends.permissions == null) {
                    if (!attribute.hasAlternative()) {
                        dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                    }
                    return null;
                }

                return new Element(Depends.permissions.has((World) null, getName(), permission)) // TODO: Vault UUID support?
                        .getAttribute(attribute.fulfill(2));
            }

            // <--[tag]
            // @attribute <p@player.has_permission[permission.node].world[<world name>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the player has the specified node in regards to the
            // specified world.
            // (Works with offline players)
            // (Note: This may or may not be functional with your permissions system.)
            // -->

            // Permission in certain world
            else if (attribute.getAttribute(2).startsWith("world")) {
                if (Depends.permissions == null) {
                    if (!attribute.hasAlternative()) {
                        dB.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                    }
                    return null;
                }

                return new Element(Depends.permissions.has(attribute.getContext(2), getName(), permission)) // TODO: Vault UUID support?
                        .getAttribute(attribute.fulfill(2));
            }

            // Permission in current world
            else if (isOnline()) {
                return new Element(getPlayerEntity().hasPermission(permission))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        /////////////////////
        //   INVENTORY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.inventory>
        // @returns dInventory
        // @description
        // Returns a dInventory of the player's current inventory.
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
        if (attribute.startsWith("enderchest")) {
            return getEnderChest().getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   ONLINE ATTRIBUTES
        /////////////////

        // Player is required to be online after this point...
        if (!isOnline()) {

            String returned = CoreUtilities.autoPropertyTag(this, attribute);
            if (returned != null) {
                return returned;
            }

            return new Element(identify()).getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <p@player.open_inventory>
        // @returns dInventory
        // @description
        // Gets the inventory the player currently has open. If the player has no open
        // inventory, this returns the player's inventory.
        // -->
        if (attribute.startsWith("open_inventory")) {
            return dInventory.mirrorBukkitInventory(getPlayerEntity().getOpenInventory().getTopInventory())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.selected_trade_index>
        // @returns Element(Number)
        // @description
        // Returns the index of the trade the player is currently viewing, if any.
        // -->
        if (attribute.startsWith("selected_trade_index")) {
            if (getPlayerEntity().getOpenInventory().getTopInventory() instanceof MerchantInventory) {
                return new Element(((MerchantInventory) getPlayerEntity().getOpenInventory().getTopInventory())
                        .getSelectedRecipeIndex() + 1).getAttribute(attribute.fulfill(1));
            }
        }

        // This is almost completely broke and only works if the player has placed items in the trade slots.
        // [tag]
        // @attribute <p@player.selected_trade>
        // @returns dTrade
        // @description
        // Returns the trade the player is currently viewing, if any.
        //
        /*
        if (attribute.startsWith("selected_trade")) {
            Inventory playerInventory = getPlayerEntity().getOpenInventory().getTopInventory();
            if (playerInventory instanceof MerchantInventory
                    && ((MerchantInventory) playerInventory).getSelectedRecipe() != null) {
                return new dTrade(((MerchantInventory) playerInventory).getSelectedRecipe()).getAttribute(attribute.fulfill(1));
            }
        }
        */

        // <--[tag]
        // @attribute <p@player.item_on_cursor>
        // @returns dItem
        // @description
        // Returns the item on the player's cursor, if any. This includes
        // chest interfaces, inventories, and hotbars, etc.
        // -->
        if (attribute.startsWith("item_on_cursor")) {
            return new dItem(getPlayerEntity().getItemOnCursor())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.item_in_hand.slot>
        // @returns Element(Number)
        // @description
        // Returns the slot location of the player's selected item.
        // -->
        if (attribute.startsWith("item_in_hand.slot")) {
            return new Element(getPlayerEntity().getInventory().getHeldItemSlot() + 1)
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.sidebar.lines>
        // @returns dList
        // @description
        // Returns the current lines set on the player's Sidebar via the Sidebar command.
        // -->
        if (attribute.startsWith("sidebar.lines")) {
            Sidebar sidebar = SidebarCommand.getSidebar(this);
            if (sidebar == null) {
                return null;
            }
            return new dList(sidebar.getLines()).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.sidebar.title>
        // @returns Element
        // @description
        // Returns the current title set on the player's Sidebar via the Sidebar command.
        // -->
        if (attribute.startsWith("sidebar.title")) {
            Sidebar sidebar = SidebarCommand.getSidebar(this);
            if (sidebar == null) {
                return null;
            }
            return new Element(sidebar.getTitle()).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.sidebar.scores>
        // @returns dList
        // @description
        // Returns the current scores set on the player's Sidebar via the Sidebar command,
        // in the same order as <@link tag p@player.sidebar.lines>.
        // -->
        if (attribute.startsWith("sidebar.scores")) {
            Sidebar sidebar = SidebarCommand.getSidebar(this);
            if (sidebar == null) {
                return null;
            }
            dList scores = new dList();
            for (int score : sidebar.getScores()) {
                scores.add(String.valueOf(score));
            }
            return scores.getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.sidebar.start>
        // @returns Element(Number)
        // @description
        // Returns the current start score set on the player's Sidebar via the Sidebar command.
        // -->
        if (attribute.startsWith("sidebar.start")) {
            Sidebar sidebar = SidebarCommand.getSidebar(this);
            if (sidebar == null) {
                return null;
            }
            return new Element(sidebar.getStart()).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.sidebar.increment>
        // @returns Element(Number)
        // @description
        // Returns the current score increment set on the player's Sidebar via the Sidebar command.
        // -->
        if (attribute.startsWith("sidebar.increment")) {
            Sidebar sidebar = SidebarCommand.getSidebar(this);
            if (sidebar == null) {
                return null;
            }
            return new Element(sidebar.getIncrement()).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.skin_blob>
        // @returns Element
        // @description
        // Returns the player's current skin blob.
        // @mechanism dPlayer.skin_blob
        // -->
        if (attribute.startsWith("skin_blob")) {
            return new Element(NMSHandler.getInstance().getProfileEditor().getPlayerSkinBlob(getPlayerEntity()))
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("attack_cooldown")) {
            attribute.fulfill(1);

            // <--[tag]
            // @attribute <p@player.attack_cooldown.duration>
            // @returns Duration
            // @description
            // Returns the amount of time that passed since the start of the attack cooldown.
            // -->
            if (attribute.startsWith("duration")) {
                return new Duration((long) NMSHandler.getInstance().getPlayerHelper()
                        .ticksPassedDuringCooldown(getPlayerEntity())).getAttribute(attribute.fulfill(1));
            }


            // <--[tag]
            // @attribute <p@player.attack_cooldown.max_duration>
            // @returns Duration
            // @description
            // Returns the maximum amount of time that can pass before the player's main hand has returned
            // to its original place after the cooldown has ended.
            // NOTE: This is slightly inaccurate and may not necessarily match with the actual attack
            // cooldown progress.
            // -->
            else if (attribute.startsWith("max_duration")) {
                return new Duration((long) NMSHandler.getInstance().getPlayerHelper()
                        .getMaxAttackCooldownTicks(getPlayerEntity())).getAttribute(attribute.fulfill(1));
            }


            // <--[tag]
            // @attribute <p@player.attack_cooldown.percent>
            // @returns Element(Decimal)
            // @description
            // Returns the progress of the attack cooldown. 0 means that the attack cooldown has just
            // started, while 100 means that the attack cooldown has finished.
            // NOTE: This may not match exactly with the clientside attack cooldown indicator.
            // -->
            else if (attribute.startsWith("percent")) {
                return new Element(NMSHandler.getInstance().getPlayerHelper()
                        .getAttackCooldownPercent(getPlayerEntity()) * 100).getAttribute(attribute.fulfill(1));
            }

            dB.echoError("The tag 'player.attack_cooldown...' must be followed by a sub-tag.");

            return null;
        }


        /////////////////////
        //   CITIZENS ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.selected_npc>
        // @returns dNPC
        // @description
        // Returns the dNPC that the player currently has selected with
        // '/npc select', null if no player selected.
        // -->
        if (attribute.startsWith("selected_npc")) {
            if (getPlayerEntity().hasMetadata("selected")) {
                return getSelectedNPC().getAttribute(attribute.fulfill(1));
            }
        }


        /////////////////////
        //   CONVERSION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.entity>
        // @returns dEntity
        // @description
        // Returns the dEntity object of the player.
        // (Note: This should never actually be needed. <p@player> is considered a valid dEntity by script commands.)
        // -->
        if (attribute.startsWith("entity") && !attribute.startsWith("entity_")) {
            return new dEntity(getPlayerEntity())
                    .getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.ip>
        // @returns Element
        // @description
        // Returns the player's IP address host name.
        // -->
        if (attribute.startsWith("ip") ||
                attribute.startsWith("host_name")) {
            attribute = attribute.fulfill(1);
            // <--[tag]
            // @attribute <p@player.ip.address_only>
            // @returns Element
            // @description
            // Returns the player's IP address.
            // -->
            if (attribute.startsWith("address_only")) {
                return new Element(getPlayerEntity().getAddress().toString())
                        .getAttribute(attribute.fulfill(1));
            }
            String host = getPlayerEntity().getAddress().getHostName();
            // <--[tag]
            // @attribute <p@player.ip.address>
            // @returns Element
            // @description
            // Returns the player's IP address.
            // -->
            if (attribute.startsWith("address")) {
                return new Element(getPlayerEntity().getAddress().toString())
                        .getAttribute(attribute.fulfill(1));
            }
            return new Element(host)
                    .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <p@player.name.display>
        // @returns Element
        // @description
        // Returns the display name of the player, which may contain
        // prefixes and suffixes, colors, etc.
        // -->
        if (attribute.startsWith("name.display")) {
            return new Element(getPlayerEntity().getDisplayName())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.name.list>
        // @returns Element
        // @description
        // Returns the name of the player as shown in the player list.
        // -->
        if (attribute.startsWith("name.list")) {
            return new Element(getPlayerEntity().getPlayerListName())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.nameplate>
        // @returns Element
        // @description
        // Returns the displayed text in the nameplate of the player.
        // -->
        if (attribute.startsWith("nameplate")) {
            return new Element(NMSHandler.getInstance().getProfileEditor().getPlayerName(getPlayerEntity()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.name>
        // @returns Element
        // @description
        // Returns the name of the player.
        // -->
        if (attribute.startsWith("name")) {
            return new Element(getName()).getAttribute(attribute.fulfill(1));
        }

        /////////////////////
        //   LOCATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.compass_target>
        // @returns dLocation
        // @description
        // Returns the location of the player's compass target.
        // -->
        if (attribute.startsWith("compass_target")) {
            Location target = getPlayerEntity().getCompassTarget();
            if (target != null) {
                return new dLocation(target).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <p@player.chunk_loaded[<chunk>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player has the chunk loaded on their client.
        // -->
        if (attribute.startsWith("chunk_loaded") && attribute.hasContext(1)) {
            dChunk chunk = dChunk.valueOf(attribute.getContext(1));
            if (chunk == null) {
                return null;
            }
            return new Element(hasChunkLoaded(chunk.getChunk())).getAttribute(attribute.fulfill(1));
        }


        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <p@player.can_fly>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is allowed to fly.
        // @mechanism dPlayer.can_fly
        // -->
        if (attribute.startsWith("can_fly") || attribute.startsWith("allowed_flight")) {
            return new Element(getPlayerEntity().getAllowFlight())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.fly_speed>
        // @returns Element(Decimal)
        // @description
        // Returns the speed the player can fly at.
        // -->
        if (attribute.startsWith("fly_speed")) {
            return new Element(getPlayerEntity().getFlySpeed())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.food_level.formatted>
        // @returns Element
        // @description
        // Returns a 'formatted' value of the player's current food level.
        // May be 'starving', 'famished', 'parched, 'hungry' or 'healthy'.
        // -->
        if (attribute.startsWith("food_level.formatted")) {
            double maxHunger = getPlayerEntity().getMaxHealth();
            if (attribute.hasContext(2)) {
                maxHunger = attribute.getIntContext(2);
            }
            int foodLevel = getFoodLevel();
            if (foodLevel / maxHunger < .10) {
                return new Element("starving").getAttribute(attribute.fulfill(2));
            }
            else if (foodLevel / maxHunger < .40) {
                return new Element("famished").getAttribute(attribute.fulfill(2));
            }
            else if (foodLevel / maxHunger < .75) {
                return new Element("parched").getAttribute(attribute.fulfill(2));
            }
            else if (foodLevel / maxHunger < 1) {
                return new Element("hungry").getAttribute(attribute.fulfill(2));
            }
            else {
                return new Element("healthy").getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <p@player.saturation>
        // @returns Element(Decimal)
        // @description
        // Returns the current saturation of the player.
        // -->
        if (attribute.startsWith("saturation")) {
            return new Element(getPlayerEntity().getSaturation())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.food_level>
        // @returns Element(Number)
        // @description
        // Returns the current food level of the player.
        // -->
        if (attribute.startsWith("food_level")) {
            return new Element(getFoodLevel())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.gamemode>
        // @returns Element
        // @description
        // Returns the name of the gamemode the player is currently set to.
        // -->
        if (attribute.startsWith("gamemode")) {
            attribute = attribute.fulfill(1);
            // <--[tag]
            // @attribute <p@player.gamemode.id>
            // @returns Element(Number)
            // @description
            // Returns the gamemode ID of the player. 0 = survival, 1 = creative, 2 = adventure, 3 = spectator
            // -->
            if (attribute.startsWith("id")) {
                return new Element(getPlayerEntity().getGameMode().getValue())
                        .getAttribute(attribute.fulfill(1));
            }
            return new Element(getPlayerEntity().getGameMode().name())
                    .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <p@player.is_blocking>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is currently blocking.
        // -->
        if (attribute.startsWith("is_blocking")) {
            return new Element(getPlayerEntity().isBlocking())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.ping>
        // @returns Element(Number)
        // @description
        // Returns the player's current ping.
        // -->
        if (attribute.startsWith("ping")) {
            return new Element(NMSHandler.getInstance().getPlayerHelper().getPing(getPlayerEntity()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.is_flying>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is currently flying.
        // -->
        if (attribute.startsWith("is_flying")) {
            return new Element(getPlayerEntity().isFlying())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.is_sleeping>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is currently sleeping.
        // -->
        if (attribute.startsWith("is_sleeping")) {
            return new Element(getPlayerEntity().isSleeping())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.is_sneaking>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is currently sneaking.
        // -->
        if (attribute.startsWith("is_sneaking")) {
            return new Element(getPlayerEntity().isSneaking())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.is_sprinting>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player is currently sprinting.
        // -->
        if (attribute.startsWith("is_sprinting")) {
            return new Element(getPlayerEntity().isSprinting())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.has_achievement[<achievement>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the player has the specified achievement.
        // -->
        if (attribute.startsWith("has_achievement")) {
            Achievement ach = Achievement.valueOf(attribute.getContext(1).toUpperCase());
            return new Element(getPlayerEntity().hasAchievement(ach)).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.statistic[<statistic>]>
        // @returns Element(Number)
        // @description
        // Returns the player's current value for the specified statistic.
        // -->
        if (attribute.startsWith("statistic")) {
            Statistic statistic = Statistic.valueOf(attribute.getContext(1).toUpperCase());
            if (statistic == null) {
                return null;
            }

            // <--[tag]
            // @attribute <p@player.statistic[<statistic>].qualifier[<material>/<entity>]>
            // @returns Element(Number)
            // @description
            // Returns the player's current value for the specified statistic, with the
            // specified qualifier, which can be either an entity or material.
            // -->
            if (attribute.getAttribute(2).startsWith("qualifier")) {
                dObject obj = ObjectFetcher.pickObjectFor(attribute.getContext(2), attribute.context);
                try {
                    if (obj instanceof dMaterial) {
                        return new Element(getPlayerEntity().getStatistic(statistic, ((dMaterial) obj).getMaterial()))
                                .getAttribute(attribute.fulfill(2));
                    }
                    else if (obj instanceof dEntity) {
                        return new Element(getPlayerEntity().getStatistic(statistic, ((dEntity) obj).getBukkitEntityType()))
                                .getAttribute(attribute.fulfill(2));
                    }
                    else {
                        return null;
                    }
                }
                catch (Exception e) {
                    dB.echoError("Invalid statistic: " + statistic + " for this player!");
                    return null;
                }
            }
            try {
                return new Element(getPlayerEntity().getStatistic(statistic)).getAttribute(attribute.fulfill(1));
            }
            catch (Exception e) {
                dB.echoError("Invalid statistic: " + statistic + " for this player!");
                return null;
            }
        }

        // <--[tag]
        // @attribute <p@player.time_asleep>
        // @returns Duration
        // @description
        // Returns the time the player has been asleep.
        // -->
        if (attribute.startsWith("time_asleep")) {
            return new Duration(getPlayerEntity().getSleepTicks() / 20)
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.time>
        // @returns Element(Number)
        // @description
        // Returns the time the player is currently experiencing. This time could differ from
        // the time that the rest of the world is currently experiencing if a 'time' or 'freeze_time'
        // mechanism is being used on the player.
        // -->
        if (attribute.startsWith("time")) {
            return new Element(getPlayerEntity().getPlayerTime())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.walk_speed>
        // @returns Element(Decimal)
        // @description
        // Returns the speed the player can walk at.
        // -->
        if (attribute.startsWith("walk_speed")) {
            return new Element(getPlayerEntity().getWalkSpeed())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <p@player.weather>
        // @returns Element
        // @mechanism dPlayer.weather
        // @description
        // Returns the type of weather the player is experiencing. This will be different
        // from the weather currently in the world that the player is residing in if
        // the weather is currently being forced onto the player.
        // Returns null if the player does not currently have any forced weather.
        // -->
        if (attribute.startsWith("weather")) {
            if (getPlayerEntity().getPlayerWeather() != null) {
                return new Element(getPlayerEntity().getPlayerWeather().name())
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }

        // <--[tag]
        // @attribute <p@player.xp.level>
        // @returns Element(Number)
        // @description
        // Returns the number of XP levels the player has.
        // -->
        if (attribute.startsWith("xp.level")) {
            return new Element(getPlayerEntity().getLevel())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.xp.to_next_level>
        // @returns Element(Number)
        // @description
        // Returns the amount of XP needed to get to the next level.
        // -->
        if (attribute.startsWith("xp.to_next_level")) {
            return new Element(getPlayerEntity().getExpToLevel())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.xp.total>
        // @returns Element(Number)
        // @description
        // Returns the total amount of experience points.
        // -->
        if (attribute.startsWith("xp.total")) {
            return new Element(getPlayerEntity().getTotalExperience())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <p@player.xp>
        // @returns Element(Decimal)
        // @description
        // Returns the percentage of experience points to the next level.
        // -->
        if (attribute.startsWith("xp")) {
            return new Element(getPlayerEntity().getExp() * 100)
                    .getAttribute(attribute.fulfill(1));
        }

        if (Depends.chat != null) {

            // <--[tag]
            // @attribute <p@player.chat_prefix>
            // @returns Element
            // @plugin Vault
            // @description
            // Returns the player's chat prefix.
            // NOTE: May work with offline players.
            // Requires a Vault-compatible chat plugin.
            // @mechanism dPlayer.chat_prefix
            // -->
            if (attribute.startsWith("chat_prefix")) {
                String prefix = Depends.chat.getPlayerPrefix(getWorld().getName(), getOfflinePlayer());
                if (prefix == null) {
                    return null;
                }
                return new Element(prefix).getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <p@player.chat_suffix>
            // @returns Element
            // @plugin Vault
            // @description
            // Returns the player's chat suffix.
            // NOTE: May work with offline players.
            // Requires a Vault-compatible chat plugin.
            // @mechanism dPlayer.chat_suffix
            // -->
            else if (attribute.startsWith("chat_suffix")) {
                String suffix = Depends.chat.getPlayerSuffix(getWorld().getName(), getOfflinePlayer());
                if (suffix == null) {
                    return null;
                }
                return new Element(suffix).getAttribute(attribute.fulfill(1));
            }
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new dEntity(getPlayerEntity()).getAttribute(attribute);
    }


    public void applyProperty(Mechanism mechanism) {
        dB.echoError("Cannot apply properties to a player!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dPlayer
        // @name respawn
        // @input None
        // @description
        // Forces the player to respawn if they are on the death screen.
        // -->
        if (mechanism.matches("respawn")) {
            NMSHandler.getInstance().getPacketHelper().respawn(getPlayerEntity());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name vision
        // @input Element
        // @description
        // Changes the player's vision to the provided entity type. Valid types:
        // ENDERMAN, CAVE_SPIDER, SPIDER, CREEPER
        // Provide no value to reset the player's vision.
        // -->
        if (mechanism.matches("vision")) {
            if (mechanism.hasValue() && mechanism.requireEnum(false, EntityType.values())) {
                NMSHandler.getInstance().getPacketHelper().setVision(getPlayerEntity(), EntityType.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else {
                NMSHandler.getInstance().getPacketHelper().forceSpectate(getPlayerEntity(), getPlayerEntity());
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name level
        // @input Element(Number)
        // @description
        // Sets the level on the player. Does not affect the current progression
        // of experience towards next level.
        // @tags
        // <p@player.xp.level>
        // -->
        if (mechanism.matches("level") && mechanism.requireInteger()) {
            setLevel(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name item_slot
        // @input Element(Number)
        // @description
        // Sets the inventory slot that the player has selected.
        // Works with offline players.
        // @tags
        // <p@player.item_in_hand.slot>
        // -->
        if (mechanism.matches("item_slot") && mechanism.requireInteger()) {
            if (isOnline()) {
                getPlayerEntity().getInventory().setHeldItemSlot(mechanism.getValue().asInt() - 1);
            }
            else {
                getNBTEditor().setItemInHand(mechanism.getValue().asInt() - 1);
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name window_property
        // @input Element
        // @description
        // Sets various properties of a window the player has open, such as the open page in a lectern.
        // Input is of the form PROPERTY,VALUE where the value is a number.
        // Note that any adjusted window properties are entirely clientside.
        // Valid properties: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/InventoryView.Property.html>
        // -->
        if (mechanism.matches("window_property")) {
            String[] split = mechanism.getValue().asString().split(",", 2);
            if (split.length != 2) {
                dB.echoError("Invalid input! Must be in the form PROPERTY,VALUE");
            }
            else {
                try {
                    getPlayerEntity().setWindowProperty(InventoryView.Property.valueOf(split[0].toUpperCase()), Integer.parseInt(split[1]));
                }
                catch (NumberFormatException e) {
                    dB.echoError("Input value must be a number!");
                }
                catch (IllegalArgumentException e) {
                    dB.echoError("Must specify a valid window property!");
                }
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name item_on_cursor
        // @input dItem
        // @description
        // Sets the item on the player's cursor. This includes
        // chest interfaces, inventories, and hotbars, etc.
        // @tags
        // <p@player.item_on_cursor>
        // -->
        if (mechanism.matches("item_on_cursor") && mechanism.requireObject(dItem.class)) {
            getPlayerEntity().setItemOnCursor(mechanism.valueAsType(dItem.class).getItemStack());
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
        // TODO: Player achievement tags.
        if (mechanism.matches("award_achievement") && mechanism.requireEnum(false, Achievement.values())) {
            getPlayerEntity().awardAchievement(Achievement.valueOf(mechanism.getValue().asString().toUpperCase()));
        }

        // <--[mechanism]
        // @object dPlayer
        // @name absorption_health
        // @input Element(Decimal)
        // @description
        // Sets the player's absorption health.
        // @tags
        // <p@player.absorption_health>
        // -->
        if (mechanism.matches("absorption_health") && mechanism.requireFloat()) {
            NMSHandler.getInstance().getPlayerHelper().setAbsorption(getPlayerEntity(), mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name fake_absorption_health
        // @input Element(Decimal)
        // @description
        // Shows the player fake absorption health that persists on damage.
        // -->
        if (mechanism.matches("fake_absorption_health") && mechanism.requireFloat()) {
            NMSHandler.getInstance().getPacketHelper().setFakeAbsorption(getPlayerEntity(), mechanism.getValue().asFloat());
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
        // <p@player.health.scale>
        // -->
        if (mechanism.matches("health_scale") && mechanism.requireDouble()) {
            getPlayerEntity().setHealthScale(mechanism.getValue().asDouble());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name scale_health
        // @input Element(Boolean)
        // @description
        // Enables or disables the health scale mechanism.getValue(). Disabling will result in the standard
        // amount of hearts being shown.
        // @tags
        // <p@player.health.is_scaled>
        // -->
        if (mechanism.matches("scale_health") && mechanism.requireBoolean()) {
            getPlayerEntity().setHealthScaled(mechanism.getValue().asBoolean());
        }

        // Allow offline editing of health values
        if (mechanism.matches("max_health") && mechanism.requireDouble()) {
            setMaxHealth(mechanism.getValue().asDouble());
        }

        if (mechanism.matches("health") && mechanism.requireDouble()) {
            setHealth(mechanism.getValue().asDouble());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name redo_attack_cooldown
        // @input None
        // @description
        // Forces the player to wait for the full attack cooldown duration for the item in their hand.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <p@player.attack_cooldown.time_passed>
        // <p@player.attack_cooldown.max_duration>
        // <p@player.attack_cooldown.percent_done>
        // -->
        if (mechanism.matches("redo_attack_cooldown")) {
            NMSHandler.getInstance().getPlayerHelper().setAttackCooldown(getPlayerEntity(), 0);
        }

        // <--[mechanism]
        // @object dPlayer
        // @name reset_attack_cooldown
        // @input None
        // @description
        // Ends the player's attack cooldown.
        // NOTE: This will do nothing if the player's attack speed attribute is set to 0.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <p@player.attack_cooldown.time_passed>
        // <p@player.attack_cooldown.max_duration>
        // <p@player.attack_cooldown.percent_done>
        // -->
        if (mechanism.matches("reset_attack_cooldown")) {
            PlayerHelper playerHelper = NMSHandler.getInstance().getPlayerHelper();
            playerHelper.setAttackCooldown(getPlayerEntity(), Math.round(playerHelper.getMaxAttackCooldownTicks(getPlayerEntity())));
        }

        // <--[mechanism]
        // @object dPlayer
        // @name attack_cooldown_percent
        // @input Element(Decimal)
        // @description
        // Sets the progress of the player's attack cooldown. Takes a decimal from 0 to 1.
        // 0 means the cooldown has just begun, while 1 means the cooldown has been completed.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <p@player.attack_cooldown.time_passed>
        // <p@player.attack_cooldown.max_duration>
        // <p@player.attack_cooldown.percent_done>
        // -->
        if (mechanism.matches("attack_cooldown_percent") && mechanism.requireFloat()) {
            float percent = mechanism.getValue().asFloat();
            System.out.println(percent + " >> " + (percent >= 0 && percent <= 1));
            if (percent >= 0 && percent <= 1) {
                PlayerHelper playerHelper = NMSHandler.getInstance().getPlayerHelper();
                playerHelper.setAttackCooldown(getPlayerEntity(),
                        Math.round(playerHelper.getMaxAttackCooldownTicks(getPlayerEntity()) * mechanism.getValue().asFloat()));
            }
            else {
                dB.echoError("Invalid percentage! \"" + percent + "\" is not between 0 and 1!");
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name attack_cooldown
        // @input Duration
        // @description
        // Sets the player's time since their last attack. If the time is greater than the max duration of their
        // attack cooldown, then the cooldown is considered finished.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <p@player.attack_cooldown.time_passed>
        // <p@player.attack_cooldown.max_duration>
        // <p@player.attack_cooldown.percent_done>
        // -->
        if (mechanism.matches("attack_cooldown") && mechanism.requireObject(Duration.class)) {
            NMSHandler.getInstance().getPlayerHelper().setAttackCooldown(getPlayerEntity(),
                    mechanism.getValue().asType(Duration.class).getTicksAsInt());
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
            getPlayerEntity().setResourcePack(mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name saturation
        // @input Element(Decimal)
        // @description
        // Sets the current food saturation level of a player.
        // @tags
        // <p@player.saturation>
        // -->
        if (mechanism.matches("saturation") && mechanism.requireFloat()) {
            getPlayerEntity().setSaturation(mechanism.getValue().asFloat());
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
            MapView map = Bukkit.getServer().getMap((short) mechanism.getValue().asInt());
            if (map != null) {
                getPlayerEntity().sendMap(map);
            }
            else {
                dB.echoError("No map found for ID " + mechanism.getValue().asInt() + "!");
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name food_level
        // @input Element(Number)
        // @description
        // Sets the current food level of a player. Typically, '20' is full.
        // @tags
        // <p@player.food_level>
        // -->
        if (mechanism.matches("food_level") && mechanism.requireInteger()) {
            setFoodLevel(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name bed_spawn_location
        // @input dLocation
        // @description
        // Sets the bed location that the player respawns at.
        // @tags
        // <p@player.bed_spawn>
        // -->
        if (mechanism.matches("bed_spawn_location") && mechanism.requireObject(dLocation.class)) {
            setBedSpawnLocation(mechanism.valueAsType(dLocation.class));
        }

        // <--[mechanism]
        // @object dPlayer
        // @name can_fly
        // @input Element(Boolean)
        // @description
        // Sets whether the player is allowed to fly.
        // @tags
        // <p@player.can_fly>
        // -->
        if (mechanism.matches("can_fly") && mechanism.requireBoolean()) {
            getPlayerEntity().setAllowFlight(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name fly_speed
        // @input Element(Decimal)
        // @description
        // Sets the fly speed of the player. Valid range is 0.0 to 1.0
        // @tags
        // <p@player.fly_speed>
        // -->
        if (mechanism.matches("fly_speed") && mechanism.requireFloat()) {
            setFlySpeed(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name flying
        // @input Element(Boolean)
        // @description
        // Sets whether the player is flying.
        // @tags
        // <p@player.is_flying>
        // -->
        if (mechanism.matches("flying") && mechanism.requireBoolean()) {
            getPlayerEntity().setFlying(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name sprinting
        // @input Element(Boolean)
        // @description
        // Sets whether the player is sprinting.
        // @tags
        // <p@player.is_sprinting>
        // -->
        if (mechanism.matches("sprinting") && mechanism.requireBoolean()) {
            getPlayerEntity().setSprinting(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name gamemode
        // @input Element
        // @description
        // Sets the game mode of the player.
        // Valid gamemodes are survival, creative, adventure, and spectator.
        // @tags
        // <p@player.gamemode>
        // <p@player.gamemode.id>
        // -->
        if (mechanism.matches("gamemode") && mechanism.requireEnum(false, GameMode.values())) {
            setGameMode(GameMode.valueOf(mechanism.getValue().asString().toUpperCase()));
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
        // <p@player.weather>
        // -->
        if (mechanism.matches("weather") && mechanism.requireEnum(false, WeatherType.values())) {
            getPlayerEntity().setPlayerWeather(WeatherType.valueOf(mechanism.getValue().asString().toUpperCase()));
        }

        // <--[mechanism]
        // @object dPlayer
        // @name reset_weather
        // @input None
        // @description
        // Resets the weather on the Player to the conditions currently taking place in the Player's
        // current world.
        // @tags
        // <p@player.weather>
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
        // <p@player.name.list>
        // -->
        if (mechanism.matches("player_list_name")) {
            getPlayerEntity().setPlayerListName(mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name display_name
        // @input Element
        // @description
        // Sets the name displayed for the player when chatting.
        // @tags
        // <p@player.name.display>
        // -->
        if (mechanism.matches("display_name")) {
            getPlayerEntity().setDisplayName(mechanism.getValue().asString());
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
            getPlayerEntity().openWorkbench(mechanism.valueAsType(dLocation.class), true);
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
        // <p@player.location>
        // -->
        if (mechanism.matches("location") && mechanism.requireObject(dLocation.class)) {
            setLocation(mechanism.valueAsType(dLocation.class));
        }

        // <--[mechanism]
        // @object dPlayer
        // @name time
        // @input Element(Number)
        // @description
        // Sets the time of day the Player is currently experiencing. Setting this will cause the
        // player to have a different time than other Players in the world are experiencing though
        // time will continue to progress. Using the 'reset_time' mechanism, or relogging your player
        // will reset this mechanism to match the world's current time. Valid range is 0-24000.
        // The value is relative to the current world time, and will continue moving at the same rate as current world time moves.
        // @tags
        // <p@player.time>
        // -->
        if (mechanism.matches("time") && mechanism.requireInteger()) {
            getPlayerEntity().setPlayerTime(mechanism.getValue().asInt(), true);
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
        // will reset this mechanism to match the world's current time. Valid range is 0-24000.
        // @tags
        // <p@player.time>
        // -->
        if (mechanism.matches("freeze_time")) {
            if (mechanism.requireInteger("Invalid integer specified. Assuming current world time.")) {
                getPlayerEntity().setPlayerTime(mechanism.getValue().asInt(), false);
            }
            else {
                getPlayerEntity().setPlayerTime(getPlayerEntity().getWorld().getTime(), false);
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name reset_time
        // @input None
        // @description
        // Resets any altered time that has been applied to this player. Using this will make
        // the Player's time match the world's current time.
        // @tags
        // <p@player.time>
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
        // <p@player.walk_speed>
        // -->
        if (mechanism.matches("walk_speed") && mechanism.requireFloat()) {
            getPlayerEntity().setWalkSpeed(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name exhaustion
        // @input Element(Decimal)
        // @description
        // Sets the exhaustion level of a player.
        // @tags
        // <p@player.exhaustion>
        // -->
        if (mechanism.matches("exhaustion") && mechanism.requireFloat()) {
            getPlayerEntity().setExhaustion(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name show_entity
        // @input dEntity
        // @description
        // Shows the player a previously hidden entity.
        // -->
        if (mechanism.matches("show_entity") && mechanism.requireObject(dEntity.class)) {
            NMSHandler.getInstance().getEntityHelper().unhideEntity(getPlayerEntity(), mechanism.valueAsType(dEntity.class).getBukkitEntity());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name hide_entity
        // @input dEntity(|Element(Boolean))
        // @description
        // Hides an entity from the player. You can optionally also specify a boolean to determine
        // whether the entity should be kept in the tab list (players only).
        // -->
        if (mechanism.matches("hide_entity")) {
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + dList.internal_escape + "]", 2);
                if (split.length > 0 && new Element(split[0]).matchesType(dEntity.class)) {
                    dEntity entity = mechanism.valueAsType(dEntity.class);
                    if (!entity.isSpawned()) {
                        dB.echoError("Can't hide the unspawned entity '" + split[0] + "'!");
                    }
                    else if (split.length > 1 && new Element(split[1]).isBoolean()) {
                        NMSHandler.getInstance().getEntityHelper().hideEntity(getPlayerEntity(), entity.getBukkitEntity(),
                                new Element(split[1]).asBoolean());
                    }
                    else {
                        NMSHandler.getInstance().getEntityHelper().hideEntity(getPlayerEntity(), entity.getBukkitEntity(), false);
                    }
                }
                else {
                    dB.echoError("'" + split[0] + "' is not a valid entity!");
                }
            }
            else {
                dB.echoError("Must specify an entity to hide!");
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name show_boss_bar
        // @input (Element(Number)|)Element
        // @description
        // Shows the player a boss health bar with the specified text as a name.
        // Use with no input value to remove the bar.
        // Optionally, precede the text with a number indicating the health value
        // based on an arbitrary scale of 0 to 200. For example:
        // - adjust <player> show_boss_bar:Hello
        // - adjust <player> show_boss_bar:100|Hello
        // NOTE: This has been replaced by <@link command bossbar>!
        // @tags
        // None
        // -->
        if (mechanism.matches("show_boss_bar")) {
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + dList.internal_escape + "]", 2);
                if (split.length == 2 && new Element(split[0]).isDouble()) {
                    BossBarHelper.showSimpleBossBar(getPlayerEntity(), split[1], new Element(split[0]).asDouble() * (1.0 / 200.0));
                }
                else {
                    BossBarHelper.showSimpleBossBar(getPlayerEntity(), split[0], 1.0);
                }
            }
            else {
                BossBarHelper.removeSimpleBossBar(getPlayerEntity());
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
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + dList.internal_escape + "]", 2);
                if (split.length > 0 && new Element(split[0]).isFloat()) {
                    if (split.length > 1 && new Element(split[1]).isInt()) {
                        NMSHandler.getInstance().getPacketHelper().showExperience(getPlayerEntity(),
                                new Element(split[0]).asFloat(), new Element(split[1]).asInt());
                    }
                    else {
                        NMSHandler.getInstance().getPacketHelper().showExperience(getPlayerEntity(),
                                new Element(split[0]).asFloat(), getPlayerEntity().getLevel());
                    }
                }
                else {
                    dB.echoError("'" + split[0] + "' is not a valid decimal number!");
                }
            }
            else {
                NMSHandler.getInstance().getPacketHelper().resetExperience(getPlayerEntity());
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
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + dList.internal_escape + "]", 3);
                if (split.length > 0 && new Element(split[0]).isFloat()) {
                    if (split.length > 1 && new Element(split[1]).isInt()) {
                        if (split.length > 2 && new Element(split[2]).isFloat()) {
                            NMSHandler.getInstance().getPacketHelper().showHealth(getPlayerEntity(), new Element(split[0]).asFloat(),
                                    new Element(split[1]).asInt(), new Element(split[2]).asFloat());
                        }
                        else {
                            NMSHandler.getInstance().getPacketHelper().showHealth(getPlayerEntity(), new Element(split[0]).asFloat(),
                                    new Element(split[1]).asInt(), getPlayerEntity().getSaturation());
                        }
                    }
                    else {
                        NMSHandler.getInstance().getPacketHelper().showHealth(getPlayerEntity(), new Element(split[0]).asFloat(),
                                getPlayerEntity().getFoodLevel(), getPlayerEntity().getSaturation());
                    }
                }
                else {
                    dB.echoError("'" + split[0] + "' is not a valid decimal number!");
                }
            }
            else {
                NMSHandler.getInstance().getPacketHelper().resetHealth(getPlayerEntity());
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name fake_equipment
        // @input dEntity(|Element|dItem)
        // @description
        // Shows the player fake equipment on the specified living entity, which has
        // no real non-visual effects, in the form Entity|Slot|Item, where the slot
        // can be one of the following: HAND, OFF_HAND, BOOTS, LEGS, CHEST, HEAD
        // Optionally, exclude the slot and item to stop showing the fake equipment,
        // if any, on the specified entity.
        // - adjust <player> fake_equipment:e@123|chest|i@diamond_chestplate
        // - adjust <player> fake_equipment:<player>|head|i@jack_o_lantern
        // -->
        if (mechanism.matches("fake_equipment")) {
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + dList.internal_escape + "]", 3);
                if (split.length > 0 && new Element(split[0]).matchesType(dEntity.class)) {
                    String slot = split.length > 1 ? split[1].toUpperCase() : null;
                    if (split.length > 1 && (new Element(slot).matchesEnum(EquipmentSlot.values())
                            || slot.equals("MAIN_HAND") || slot.equals("BOOTS"))) {
                        if (split.length > 2 && new Element(split[2]).matchesType(dItem.class)) {
                            if (slot.equals("MAIN_HAND")) {
                                slot = "HAND";
                            }
                            else if (slot.equals("BOOTS")) {
                                slot = "FEET";
                            }
                            NMSHandler.getInstance().getPacketHelper().showEquipment(getPlayerEntity(),
                                    new Element(split[0]).asType(dEntity.class, mechanism.context).getLivingEntity(),
                                    EquipmentSlot.valueOf(slot),
                                    new Element(split[2]).asType(dItem.class, mechanism.context).getItemStack());
                        }
                        else if (split.length > 2) {
                            dB.echoError("'" + split[2] + "' is not a valid dItem!");
                        }
                    }
                    else if (split.length > 1) {
                        dB.echoError("'" + split[1] + "' is not a valid slot; must be HAND, OFF_HAND, BOOTS, LEGS, CHEST, or HEAD!");
                    }
                    else {
                        NMSHandler.getInstance().getPacketHelper().resetEquipment(getPlayerEntity(),
                                new Element(split[0]).asType(dEntity.class, mechanism.context).getLivingEntity());
                    }
                }
                else {
                    dB.echoError("'" + split[0] + "' is not a valid dEntity!");
                }
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name fov_multiplier
        // @input Element(Decimal)
        // @description
        // Sets the player's field of view multiplier.
        // Leave input empty to reset.
        // Note: Values outside a (-1, 1) range will have little effect on the player's fov.
        // -->
        if (mechanism.matches("fov_multiplier")) {
            if (mechanism.hasValue() && mechanism.requireFloat()) {
                NMSHandler.getInstance().getPacketHelper().setFieldOfView(getPlayerEntity(), mechanism.getValue().asFloat());
            }
            else {
                NMSHandler.getInstance().getPacketHelper().setFieldOfView(getPlayerEntity(), Float.NaN);
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
            ItemChangeMessage.sendMessage(getPlayerEntity(), mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name show_endcredits
        // @input None
        // @description
        // Shows the player the end credits.
        // -->
        if (mechanism.matches("show_endcredits")) {
            NMSHandler.getInstance().getPlayerHelper().showEndCredits(getPlayerEntity());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name show_demo
        // @input None
        // @description
        // Shows the player the demo screen.
        // -->
        if (mechanism.matches("show_demo")) {
            NMSHandler.getInstance().getPacketHelper().showDemoScreen(getPlayerEntity());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name spectate
        // @input dEntity
        // @description
        // Forces the player to spectate from the entity's point of view.
        // Note: They cannot cancel the spectating without a re-log -- you
        // must make them spectate themselves to cancel the effect.
        // (i.e. - adjust <player> "spectate:<player>")
        // -->
        if (mechanism.matches("spectate") && mechanism.requireObject(dEntity.class)) {
            NMSHandler.getInstance().getPacketHelper().forceSpectate(getPlayerEntity(), mechanism.valueAsType(dEntity.class).getBukkitEntity());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name open_book
        // @input None
        // @description
        // Forces the player to open the written book in their hand.
        // The book can safely be removed from the player's hand
        // without the player closing the book.
        // -->
        if (mechanism.matches("open_book")) {
            NMSHandler.getInstance().getPacketHelper().openBook(getPlayerEntity(), EquipmentSlot.HAND);
        }

        // <--[mechanism]
        // @object dPlayer
        // @name open_offhand_book
        // @input None
        // @description
        // Forces the player to open the written book in their offhand.
        // The book can safely be removed from the player's offhand
        // without the player closing the book.
        // -->
        if (mechanism.matches("open_offhand_book")) {
            NMSHandler.getInstance().getPacketHelper().openBook(getPlayerEntity(), EquipmentSlot.OFF_HAND);
        }

        // <--[mechanism]
        // @object dPlayer
        // @name show_book
        // @input dItem
        // @description
        // Displays a book to a player.
        // -->
        if (mechanism.matches("show_book")
                && mechanism.requireObject(dItem.class)) {
            dItem book = mechanism.valueAsType(dItem.class);
            if (!book.getItemStack().hasItemMeta() || !(book.getItemStack().getItemMeta() instanceof BookMeta)) {
                dB.echoError("show_book mechanism must have a book as input.");
                return;
            }
            NMSHandler.getInstance().getPacketHelper().showEquipment(getPlayerEntity(), getPlayerEntity(),
                    EquipmentSlot.OFF_HAND, book.getItemStack());
            NMSHandler.getInstance().getPacketHelper().openBook(getPlayerEntity(), EquipmentSlot.OFF_HAND);
            NMSHandler.getInstance().getPacketHelper().showEquipment(getPlayerEntity(), getPlayerEntity(),
                    EquipmentSlot.OFF_HAND, getPlayerEntity().getEquipment().getItemInOffHand());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name edit_sign
        // @input dLocation
        // @description
        // Allows the player to edit an existing sign. To create a
        // sign, see <@link command Sign>.
        // -->
        if (mechanism.matches("edit_sign") && mechanism.requireObject(dLocation.class)) {
            if (!NMSHandler.getInstance().getPacketHelper().showSignEditor(getPlayerEntity(), mechanism.valueAsType(dLocation.class))) {
                dB.echoError("Can't edit non-sign materials!");
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name tab_list_info
        // @input Element(|Element)
        // @description
        // Show the player some text in the header and footer area
        // in their tab list.
        // - adjust <player> tab_list_info:<header>|<footer>
        // -->
        if (mechanism.matches("tab_list_info")) {
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + dList.internal_escape + "]", 2);
                if (split.length > 0) {
                    String header = split[0];
                    String footer = "";
                    if (split.length > 1) {
                        footer = split[1];
                    }
                    NMSHandler.getInstance().getPacketHelper().showTabListHeaderFooter(getPlayerEntity(), header, footer);
                }
                else {
                    dB.echoError("Must specify a header and footer to show!");
                }
            }
            else {
                NMSHandler.getInstance().getPacketHelper().resetTabListHeaderFooter(getPlayerEntity());
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name sign_update
        // @input dLocation|dList
        // @description
        // Shows the player fake lines on a sign.
        // -->
        if (mechanism.matches("sign_update")) {
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + dList.internal_escape + "]", 2);
                if (dLocation.matches(split[0]) && split.length > 1) {
                    dList lines = dList.valueOf(split[1]);
                    getPlayerEntity().sendSignChange(dLocation.valueOf(split[0]), lines.toArray(4));
                }
                else {
                    dB.echoError("Must specify a valid location and at least one sign line!");
                }
            }
            else {
                dB.echoError("Must specify a valid location and at least one sign line!");
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name banner_update
        // @input dLocation|Element(|dList)
        // @description
        // Shows the player a fake base color and, optionally, patterns on a banner. Input must be
        // in the form: "LOCATION|BASE_COLOR(|COLOR/PATTERN|...)"
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // For the list of possible patterns, see <@link url http://bit.ly/1MqRn7T>.
        // -->
        if (mechanism.matches("banner_update")) {
            if (mechanism.getValue().asString().length() > 0) {
                String[] split = mechanism.getValue().asString().split("[\\|" + dList.internal_escape + "]");
                List<org.bukkit.block.banner.Pattern> patterns = new ArrayList<>();
                if (split.length > 2) {
                    List<String> splitList;
                    for (int i = 2; i < split.length; i++) {
                        String string = split[i];
                        try {
                            splitList = CoreUtilities.split(string, '/', 2);
                            patterns.add(new org.bukkit.block.banner.Pattern(DyeColor.valueOf(splitList.get(0).toUpperCase()),
                                    PatternType.valueOf(splitList.get(1).toUpperCase())));
                        }
                        catch (Exception e) {
                            dB.echoError("Could not apply pattern to banner: " + string);
                        }
                    }
                }
                if (dLocation.matches(split[0]) && split.length > 1) {
                    dLocation location = dLocation.valueOf(split[0]);
                    DyeColor base;
                    try {
                        base = DyeColor.valueOf(split[1].toUpperCase());
                    }
                    catch (Exception e) {
                        dB.echoError("Could not apply base color to banner: " + split[1]);
                        return;
                    }
                    NMSHandler.getInstance().getPacketHelper().showBannerUpdate(getPlayerEntity(), location, base, patterns);
                }
                else {
                    dB.echoError("Must specify a valid location and a base color!");
                }
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name stop_sound
        // @input Element
        // @description
        // Stops all sounds of the specified type for the player.
        // Valid types are AMBIENT, BLOCKS, HOSTILE, MASTER, MUSIC,
        // NEUTRAL, PLAYERS, RECORDS, VOICE, and WEATHER
        // If no sound type is specified, all types will be stopped.
        // -->
        if (mechanism.matches("stop_sound")) {
            if (!mechanism.hasValue()) {
                getPlayerEntity().stopSound("");
            }
            else {
                try {
                    getPlayerEntity().stopSound("", SoundCategory.valueOf(mechanism.getValue().asString().toUpperCase()));
                }
                catch (Exception e) {
                    dB.echoError("Invalid SoundCategory. Must specify a valid name.");
                }
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name action_bar
        // @input Element
        // @description
        // Sends the player text in the action bar.
        // -->
        if (mechanism.matches("action_bar")) {
            NMSHandler.getInstance().getPacketHelper().sendActionBarMessage(getPlayerEntity(), mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name update_advancements
        // @input None
        // @description
        // Updates the player's client-side advancements to match their server data.
        // -->
        if (mechanism.matches("update_advancements")) {
            NMSHandler.getInstance().getAdvancementHelper().update(getPlayerEntity());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name name
        // @input Element
        // @description
        // Changes the name on this player's nameplate.
        // -->
        if (mechanism.matches("name")) {
            String name = mechanism.getValue().asString();
            if (name.length() > 16) {
                dB.echoError("Must specify a name with no more than 16 characters.");
            }
            else {
                NMSHandler.getInstance().getProfileEditor().setPlayerName(getPlayerEntity(), mechanism.getValue().asString());
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name skin
        // @input Element
        // @description
        // Changes the skin of the player to the skin of the given
        // player name.
        // -->
        if (mechanism.matches("skin")) {
            String name = mechanism.getValue().asString();
            if (name.length() > 16) {
                dB.echoError("Must specify a name with no more than 16 characters.");
            }
            else {
                NMSHandler.getInstance().getProfileEditor().setPlayerSkin(getPlayerEntity(), mechanism.getValue().asString());
            }
        }

        // <--[mechanism]
        // @object dPlayer
        // @name skin_blob
        // @input Element
        // @description
        // Changes the skin of the player to the specified blob.
        // -->
        if (mechanism.matches("skin_blob")) {
            NMSHandler.getInstance().getProfileEditor().setPlayerSkinBlob(getPlayerEntity(), mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name is_whitelisted
        // @input Element(Boolean)
        // @description
        // Changes whether the player is whitelisted or not.
        // @tags
        // <p@player.is_whitelisted>
        // -->
        if (mechanism.matches("is_whitelisted") && mechanism.requireBoolean()) {
            getPlayerEntity().setWhitelisted(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name is_op
        // @input Element(Boolean)
        // @description
        // Changes whether the player is a server operator or not.
        // @tags
        // <p@player.is_op>
        // -->
        if (mechanism.matches("is_op") && mechanism.requireBoolean()) {
            getOfflinePlayer().setOp(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object dPlayer
        // @name money
        // @input Element(Number)
        // @plugin Vault
        // @description
        // Set the amount of money a player has with the linked economy system (through Vault).
        // (Only if supported by the registered Economy system.)
        // @tags
        // <p@player.money>
        // -->
        if (mechanism.matches("money") && mechanism.requireDouble() && Depends.economy != null) {
            double bal = Depends.economy.getBalance(getOfflinePlayer());
            double goal = mechanism.getValue().asDouble();
            if (goal > bal) {
                Depends.economy.depositPlayer(getOfflinePlayer(), goal - bal);
            }
            else if (bal > goal) {
                Depends.economy.withdrawPlayer(getOfflinePlayer(), bal - goal);
            }
        }

        if (Depends.chat != null) {
            // <--[mechanism]
            // @object dPlayer
            // @name chat_prefix
            // @input Element
            // @plugin Vault
            // @description
            // Set the player's chat prefix.
            // Requires a Vault-compatible chat plugin.
            // @tags
            // <p@player.chat_prefix>
            // -->
            if (mechanism.matches("chat_prefix")) {
                Depends.chat.setPlayerPrefix(getPlayerEntity(), mechanism.getValue().asString());
            }

            // <--[mechanism]
            // @object dPlayer
            // @name chat_suffix
            // @input Element
            // @plugin Vault
            // @description
            // Set the player's chat suffix.
            // Requires a Vault-compatible chat plugin.
            // @tags
            // <p@player.chat_suffix>
            // -->
            if (mechanism.matches("chat_suffix")) {
                Depends.chat.setPlayerSuffix(getPlayerEntity(), mechanism.getValue().asString());
            }
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);

        // Pass along to dEntity mechanism handler if not already handled.
        if (!mechanism.fulfilled()) {
            if (isOnline()) {
                new dEntity(getPlayerEntity()).adjust(mechanism);
            }
        }

    }
}
