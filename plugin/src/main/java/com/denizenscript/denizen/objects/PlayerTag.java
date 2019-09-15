package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.nms.interfaces.AdvancementHelper;
import com.denizenscript.denizen.objects.properties.entity.EntityHealth;
import com.denizenscript.denizen.scripts.commands.player.SidebarCommand;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.entity.BossBarHelper;
import com.denizenscript.denizen.utilities.packets.ItemChangeMessage;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.nms.interfaces.PlayerHelper;
import com.denizenscript.denizen.tags.core.PlayerTagBase;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
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

public class PlayerTag implements ObjectTag, Adjustable, EntityFormObject {


    // <--[language]
    // @name PlayerTag
    // @group Object System
    // @description
    // A PlayerTag represents a player in the game.
    //
    // For format info, see <@link language p@>
    //
    // -->

    /////////////////////
    //   STATIC METHODS
    /////////////////


    public static PlayerTag mirrorBukkitPlayer(OfflinePlayer player) {
        if (player == null) {
            return null;
        }
        else {
            return new PlayerTag(player);
        }
    }

    static Map<String, UUID> playerNames = new HashMap<>();

    /**
     * Notes that the player exists, for easy PlayerTag valueOf handling.
     */
    public static void notePlayer(OfflinePlayer player) {
        if (player.getName() == null) {
            Debug.echoError("Null player " + player.toString());
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
    // p@ refers to the 'object identifier' of a PlayerTag. The 'p@' is notation for Denizen's Object
    // Fetcher. The only valid constructor for a PlayerTag is the UUID of the player the object should be
    // associated with.
    //
    // For general info, see <@link language PlayerTag>
    //
    // -->



    public static PlayerTag valueOf(String string) {
        return valueOf(string, null);
    }

    @Fetchable("p")
    public static PlayerTag valueOf(String string, TagContext context) {
        return valueOfInternal(string, context, true);
    }

    public static PlayerTag valueOfInternal(String string, boolean announce) {
        return valueOfInternal(string, null, announce);
    }

    public static String playerByNameMessage = Deprecations.playerByNameWarning.message;

    public static PlayerTag valueOfInternal(String string, TagContext context, boolean defaultAnnounce) {
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
                        return new PlayerTag(player);
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
                Deprecations.playerByNameWarning.message = playerByNameMessage + " Player named '" + player.getName() + "' has UUID: " + player.getUniqueId();
                Deprecations.playerByNameWarning.warn(context);
            }
            return new PlayerTag(player);
        }

        if (announce) {
            Debug.log("Minor: Invalid Player! '" + string + "' could not be found.");
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

    public PlayerTag(OfflinePlayer player) {
        offlinePlayer = player;
    }

    public PlayerTag(UUID uuid) {
        offlinePlayer = Bukkit.getOfflinePlayer(uuid);
    }

    public PlayerTag(Player player) {
        this((OfflinePlayer) player);
        if (EntityTag.isNPC(player)) {
            throw new IllegalStateException("NPCs are not allowed as PlayerTag objects!");
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
        return NMSHandler.getPlayerHelper().getOfflineData(getOfflinePlayer());
    }

    @Override
    public EntityTag getDenizenEntity() {
        return new EntityTag(getPlayerEntity());
    }

    public NPCTag getSelectedNPC() {
        if (Depends.citizens != null && CitizensAPI.hasImplementation()) {
            NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(getPlayerEntity());
            if (npc != null) {
                return NPCTag.mirrorCitizensNPC(npc);
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

    public LocationTag getLocation() {
        if (isOnline()) {
            return new LocationTag(getPlayerEntity().getLocation());
        }
        else {
            return new LocationTag(getNBTEditor().getLocation());
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

    public LocationTag getEyeLocation() {
        if (isOnline()) {
            return new LocationTag(getPlayerEntity().getEyeLocation());
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

    public InventoryTag getInventory() {
        if (isOnline()) {
            return InventoryTag.mirrorBukkitInventory(getPlayerEntity().getInventory());
        }
        else {
            return new InventoryTag(getNBTEditor());
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

    public InventoryTag getWorkbench() {
        if (isOnline()) {
            CraftingInventory workbench = getBukkitWorkbench();
            if (workbench != null) {
                return new InventoryTag(workbench, getPlayerEntity());
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

    public InventoryTag getEnderChest() {
        if (isOnline()) {
            return new InventoryTag(getPlayerEntity().getEnderChest(), getPlayerEntity());
        }
        else {
            return new InventoryTag(getNBTEditor(), true);
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
            Debug.echoError("Cannot set the maximum air of an offline player!");
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
        return NMSHandler.getPlayerHelper().hasChunkLoaded(getPlayerEntity(), chunk);
    }


    /////////////////////
    //   ObjectTag Methods
    /////////////////

    private String prefix = "Player";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public PlayerTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debuggable() {
        return "p@" + offlinePlayer.getUniqueId().toString() + "<GR> (" + offlinePlayer.getName() + ")";
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

    public static void registerTags() {

        /////////////////////
        //   OFFLINE ATTRIBUTES
        /////////////////

        // Defined in EntityTag
        registerTag("is_player", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(true).getObjectAttribute(attribute.fulfill(1));
            }
        });

        /////////////////////
        //   DENIZEN ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.chat_history_list>
        // @returns ListTag
        // @description
        // Returns a list of the last 10 things the player has said, less
        // if the player hasn't said all that much.
        // Works with offline players.
        // -->
        registerTag("chat_history_list", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ListTag(PlayerTagBase.playerChatHistory.get(((PlayerTag) object).getOfflinePlayer().getUniqueId()))
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.chat_history[#]>
        // @returns ElementTag
        // @description
        // Returns the last thing the player said.
        // If a number is specified, returns an earlier thing the player said.
        // Works with offline players.
        // -->
        registerTag("chat_history", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                int x = 1;
                if (attribute.hasContext(1) && ArgumentHelper.matchesInteger(attribute.getContext(1))) {
                    x = attribute.getIntContext(1);
                }
                // No playerchathistory? Return null.
                if (!PlayerTagBase.playerChatHistory.containsKey(((PlayerTag) object).getOfflinePlayer().getUniqueId())) {
                    return null;
                }
                List<String> messages = PlayerTagBase.playerChatHistory.get(((PlayerTag) object).getOfflinePlayer().getUniqueId());
                if (messages.size() < x || x < 1) {
                    return null;
                }
                return new ElementTag(messages.get(x - 1))
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.flag[<flag_name>]>
        // @returns Flag ListTag
        // @description
        // Returns the specified flag from the player.
        // Works with offline players.
        // -->
        registerTag("flag", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (!attribute.hasContext(1)) {
                    return null;
                }
                String flag_name = attribute.getContext(1);

                // <--[tag]
                // @attribute <PlayerTag.flag[<flag_name>].is_expired>
                // @returns ElementTag(Boolean)
                // @description
                // returns true if the flag is expired or does not exist, false if it is not yet expired or has no expiration.
                // Works with offline players.
                // -->
                if (attribute.getAttribute(2).equalsIgnoreCase("is_expired")
                        || attribute.startsWith("isexpired")) {
                    return new ElementTag(!FlagManager.playerHasFlag((PlayerTag) object, flag_name))
                            .getObjectAttribute(attribute.fulfill(2));
                }
                if (attribute.getAttribute(2).equalsIgnoreCase("size") && !FlagManager.playerHasFlag((PlayerTag) object, flag_name)) {
                    return new ElementTag(0).getObjectAttribute(attribute.fulfill(2));
                }
                if (FlagManager.playerHasFlag((PlayerTag) object, flag_name)) {
                    FlagManager.Flag flag = DenizenAPI.getCurrentInstance().flagManager()
                            .getPlayerFlag((PlayerTag) object, flag_name);

                    // <--[tag]
                    // @attribute <PlayerTag.flag[<flag_name>].expiration>
                    // @returns DurationTag
                    // @description
                    // Returns a DurationTag of the time remaining on the flag, if it has an expiration.
                    // Works with offline players.
                    // -->
                    if (attribute.getAttribute(2).equalsIgnoreCase("expiration")) {
                        return flag.expiration().getObjectAttribute(attribute.fulfill(2));
                    }

                    return new ListTag(flag.toString(), true, flag.values())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                return new ElementTag(((PlayerTag) object).identify()).getObjectAttribute(attribute);
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.has_flag[<flag_name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the Player has the specified flag, otherwise returns false.
        // Works with offline players.
        // -->
        registerTag("has_flag", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (!attribute.hasContext(1)) {
                    return null;
                }
                String flag_name = attribute.getContext(1);
                return new ElementTag(FlagManager.playerHasFlag((PlayerTag) object, flag_name)).getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.list_flags[(regex:)<search>]>
        // @returns ListTag
        // @description
        // Returns a list of a player's flag names, with an optional search for
        // names containing a certain pattern.
        // Works with offline players.
        // -->
        registerTag("list_flags", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                ListTag allFlags = new ListTag(DenizenAPI.getCurrentInstance().flagManager().listPlayerFlags((PlayerTag) object));
                ListTag searchFlags = null;
                if (!allFlags.isEmpty() && attribute.hasContext(1)) {
                    searchFlags = new ListTag();
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
                            Debug.echoError(e);
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
                    DenizenAPI.getCurrentInstance().flagManager().shrinkPlayerFlags((PlayerTag) object, searchFlags);
                }
                else {
                    DenizenAPI.getCurrentInstance().flagManager().shrinkPlayerFlags((PlayerTag) object, allFlags);
                }
                return searchFlags == null ? allFlags.getObjectAttribute(attribute.fulfill(1))
                        : searchFlags.getObjectAttribute(attribute.fulfill(1));
            }
        });


        registerTag("current_step", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                String outcome = "null";
                if (attribute.hasContext(1)) {
                    try {
                        outcome = DenizenAPI.getCurrentInstance().getSaves().getString("Players." + ((PlayerTag) object).getName() + ".Scripts."
                                + ScriptTag.valueOf(attribute.getContext(1)).getName() + ".Current Step");
                    }
                    catch (Exception e) {
                        outcome = "null";
                    }
                }
                return new ElementTag(outcome).getObjectAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   ECONOMY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.money>
        // @returns ElementTag(Decimal)
        // @plugin Vault
        // @description
        // Returns the amount of money the player has with the registered Economy system.
        // May work offline depending on economy provider.
        // @mechanism PlayerTag.money
        // -->

        registerTag("money", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (Depends.economy == null) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("No economy loaded! Have you installed Vault and a compatible economy plugin?");
                    }
                    return null;
                }

                attribute = attribute.fulfill(1);

                // <--[tag]
                // @attribute <PlayerTag.money.formatted>
                // @returns ElementTag
                // @plugin Vault
                // @description
                // Returns the formatted form of the player's money balance in the registered Economy system.
                // -->
                if (attribute.startsWith("formatted")) {
                    return new ElementTag(Depends.economy.format(Depends.economy.getBalance(((PlayerTag) object).getOfflinePlayer())))
                            .getObjectAttribute(attribute.fulfill(1));
                }

                if (attribute.startsWith("currency_singular")) {
                    Deprecations.oldEconomyTags.warn(attribute.getScriptEntry());
                    return new ElementTag(Depends.economy.currencyNameSingular())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                if (attribute.startsWith("currency")) {
                    Deprecations.oldEconomyTags.warn(attribute.getScriptEntry());
                    return new ElementTag(Depends.economy.currencyNamePlural())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                return new ElementTag(Depends.economy.getBalance(((PlayerTag) object).getOfflinePlayer()))
                        .getObjectAttribute(attribute);

            }
        });


        /////////////////////
        //   ENTITY LIST ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.target[(<entity>|...)]>
        // @returns EntityTag
        // @description
        // Returns the entity that the player is looking at, within a maximum range of 50 blocks,
        // or null if the player is not looking at an entity.
        // Optionally, specify a list of entities, entity types, or 'npc' to only count those targets.
        // -->

        registerTag("target", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                int range = 50;
                int attribs = 1;

                // <--[tag]
                // @attribute <PlayerTag.target[(<entity>|...)].within[(<#>)]>
                // @returns EntityTag
                // @description
                // Returns the living entity that the player is looking at within the specified range limit,
                // or null if the player is not looking at an entity.
                // Optionally, specify a list of entities, entity types, or 'npc' to only count those targets.
                // -->
                if (attribute.getAttribute(2).startsWith("within") &&
                        attribute.hasContext(2) &&
                        ArgumentHelper.matchesInteger(attribute.getContext(2))) {
                    attribs = 2;
                    range = attribute.getIntContext(2);
                }

                List<Entity> entities = ((PlayerTag) object).getPlayerEntity().getNearbyEntities(range, range, range);
                ArrayList<LivingEntity> possibleTargets = new ArrayList<>();
                if (!attribute.hasContext(1)) {
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity) {
                            possibleTargets.add((LivingEntity) entity);
                        }
                    }
                }
                else {
                    ListTag list = ListTag.getListFor(attribute.getContextObject(1));
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity) {
                            for (ObjectTag obj : list.objectForms) {
                                boolean valid = false;
                                EntityTag filterEntity = null;
                                if (obj instanceof EntityTag) {
                                    filterEntity = (EntityTag) obj;
                                }
                                else if (CoreUtilities.toLowerCase(obj.toString()).equals("npc")) {
                                    valid = EntityTag.isCitizensNPC(entity);
                                }
                                else {
                                    filterEntity = EntityTag.getEntityFor(obj, attribute.context);
                                    if (filterEntity == null) {
                                        Debug.echoError("Trying to filter 'player.target[...]' tag with invalid input: " + obj.toString());
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

                try {
                    NMSHandler.getChunkHelper().changeChunkServerThread(((PlayerTag) object).getWorld());
                    // Find the valid target
                    BlockIterator bi;
                    try {
                        bi = new BlockIterator(((PlayerTag) object).getPlayerEntity(), range);
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
                                    return new EntityTag(possibleTarget).getDenizenObject().getObjectAttribute(attribute.fulfill(attribs));
                                }
                            }
                        }
                    }
                }
                finally {
                    NMSHandler.getChunkHelper().restoreServerThread(((PlayerTag) object).getWorld());
                }
                return null;
            }
        });

        // workaround for <EntityTag.list_effects>
        registerTag("list_effects", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                ListTag effects = new ListTag();
                for (PotionEffect effect : ((PlayerTag) object).getPlayerEntity().getActivePotionEffects()) {
                    effects.add(effect.getType().getName() + "," + effect.getAmplifier() + "," + effect.getDuration() + "t");
                }
                return effects.getObjectAttribute(attribute.fulfill(1));
            }
        });

        registerTag("list", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                Debug.echoError("DO NOT USE PLAYER.LIST AS A TAG, please use <server.list_online_players> and related tags!");
                List<String> players = new ArrayList<>();

                if (attribute.startsWith("list.online")) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        players.add(player.getName());
                    }
                    return new ListTag(players).getObjectAttribute(attribute.fulfill(2));
                }
                else if (attribute.startsWith("list.offline")) {
                    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                        if (!player.isOnline()) {
                            players.add("p@" + player.getUniqueId().toString());
                        }
                    }
                    return new ListTag(players).getObjectAttribute(attribute.fulfill(2));
                }
                else {
                    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                        players.add("p@" + player.getUniqueId().toString());
                    }
                    return new ListTag(players).getObjectAttribute(attribute.fulfill(1));
                }
            }
        });


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Player' for PlayerTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag("Player").getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.save_name>
        // @returns ElementTag
        // @description
        // Returns the ID used to save the player in Denizen's saves.yml file.
        // Works with offline players.
        // -->
        registerTag("save_name", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getSaveName()).getObjectAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   LOCATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.bed_spawn>
        // @returns LocationTag
        // @description
        // Returns the location of the player's bed spawn location, null if
        // it doesn't exist.
        // Works with offline players.
        // @mechanism PlayerTag.bed_spawn_location
        // -->
        registerTag("bed_spawn", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                try {
                    NMSHandler.getChunkHelper().changeChunkServerThread(((PlayerTag) object).getWorld());
                    if (((PlayerTag) object).getOfflinePlayer().getBedSpawnLocation() == null) {
                        return null;
                    }
                    return new LocationTag(((PlayerTag) object).getOfflinePlayer().getBedSpawnLocation())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                finally {
                    NMSHandler.getChunkHelper().restoreServerThread(((PlayerTag) object).getWorld());
                }
            }
        });

        registerTag("location", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (((PlayerTag) object).isOnline()) {
                    return new EntityTag(((PlayerTag) object).getPlayerEntity()).getObjectAttribute(attribute);
                }
                return ((PlayerTag) object).getLocation().getObjectAttribute(attribute.fulfill(1));
            }
        });

        registerTag("world", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new WorldTag(((PlayerTag) object).getWorld()).getObjectAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.item_cooldown[<material>]>
        // @returns DurationTag
        // @description
        // Returns the cooldown duration remaining on player's material.
        // -->
        registerTag("item_cooldown", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                MaterialTag mat = new ElementTag(attribute.getContext(1)).asType(MaterialTag.class, attribute.context);
                if (mat != null) {
                    return new DurationTag((long) ((PlayerTag) object).getPlayerEntity().getCooldown(mat.getMaterial()))
                            .getObjectAttribute(attribute.fulfill(1));
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.first_played>
        // @returns DurationTag
        // @description
        // Returns the millisecond time of when the player first logged on to this server.
        // Works with offline players.
        // -->
        registerTag("first_played", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                attribute = attribute.fulfill(1);
                if (attribute.startsWith("milliseconds") || attribute.startsWith("in_milliseconds")) {
                    return new ElementTag(((PlayerTag) object).getOfflinePlayer().getFirstPlayed())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                return new DurationTag(((PlayerTag) object).getOfflinePlayer().getFirstPlayed() / 50)
                        .getObjectAttribute(attribute);
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.has_played_before>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player has played before.
        // Works with offline players.
        // Note: This will just always return true.
        // -->
        registerTag("has_played_before", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(true)
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.absorption_health>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the player's absorption health.
        // @mechanism PlayerTag.absorption_health
        // -->
        registerTag("absorption_health", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(NMSHandler.getPlayerHelper().getAbsorption(((PlayerTag) object).getPlayerEntity()))
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.health.is_scaled>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player's health bar is currently being scaled.
        // -->
        registerTag("health.is_scaled", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().isHealthScaled())
                        .getObjectAttribute(attribute.fulfill(2));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.health.scale>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the current scale for the player's health bar
        // -->
        registerTag("health.scale", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getHealthScale())
                        .getObjectAttribute(attribute.fulfill(2));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.exhaustion>
        // @returns ElementTag(Decimal)
        // @description
        // Returns how fast the food level drops (exhaustion).
        // -->
        registerTag("exhaustion", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getExhaustion())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // Handle EntityTag oxygen tags here to allow getting them when the player is offline
        registerTag("oxygen", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                attribute = attribute.fulfill(1);
                if (attribute.startsWith("max")) {
                    return new DurationTag((long) ((PlayerTag) object).getMaximumAir()).getObjectAttribute(attribute.fulfill(1));
                }
                return new DurationTag((long) ((PlayerTag) object).getRemainingAir()).getObjectAttribute(attribute);
            }
        });

        // Same with health tags
        registerTag("health", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                attribute = attribute.fulfill(1);
                if (attribute.startsWith("formatted")) {
                    return EntityHealth.getHealthFormatted(new EntityTag(((PlayerTag) object).getPlayerEntity()), attribute.fulfill(1));
                }
                if (attribute.startsWith("percentage")) {
                    double maxHealth = ((PlayerTag) object).getPlayerEntity().getMaxHealth();
                    if (attribute.hasContext(1)) {
                        maxHealth = attribute.getIntContext(1);
                    }
                    return new ElementTag((((PlayerTag) object).getPlayerEntity().getHealth() / maxHealth) * 100)
                            .getObjectAttribute(attribute.fulfill(1));
                }
                if (attribute.startsWith("max")) {
                    return new ElementTag(((PlayerTag) object).getMaxHealth()).getObjectAttribute(attribute.fulfill(1));
                }
                return new ElementTag(((PlayerTag) object).getHealth()).getObjectAttribute(attribute);
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.is_banned>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is banned.
        // -->
        registerTag("is_banned", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                BanEntry ban = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(((PlayerTag) object).getName());
                if (ban == null) {
                    return new ElementTag(false).getObjectAttribute(attribute.fulfill(1));
                }
                else if (ban.getExpiration() == null) {
                    return new ElementTag(true).getObjectAttribute(attribute.fulfill(1));
                }
                return new ElementTag(ban.getExpiration().after(new Date())).getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.is_online>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is currently online.
        // Works with offline players (returns false in that case).
        // -->
        registerTag("is_online", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).isOnline()).getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.is_op>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is a full server operator.
        // Works with offline players.
        // @mechanism PlayerTag.is_op
        // -->
        registerTag("is_op", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getOfflinePlayer().isOp())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.is_whitelisted>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is whitelisted.
        // Works with offline players.
        // @mechanism PlayerTag.is_whitelisted
        // -->
        registerTag("is_whitelisted", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getOfflinePlayer().isWhitelisted())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.last_played>
        // @returns DurationTag
        // @description
        // Returns the datestamp of when the player was last seen in duration.
        // Works with offline players.
        // -->
        registerTag("last_played", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                attribute = attribute.fulfill(1);
                if (attribute.startsWith("milliseconds") || attribute.startsWith("in_milliseconds")) {
                    if (((PlayerTag) object).isOnline()) {
                        return new ElementTag(System.currentTimeMillis())
                                .getObjectAttribute(attribute.fulfill(1));
                    }
                    return new ElementTag(((PlayerTag) object).getOfflinePlayer().getLastPlayed())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                if (((PlayerTag) object).isOnline()) {
                    return new DurationTag(System.currentTimeMillis() / 50)
                            .getObjectAttribute(attribute);
                }
                return new DurationTag(((PlayerTag) object).getOfflinePlayer().getLastPlayed() / 50)
                        .getObjectAttribute(attribute);
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.groups>
        // @returns ListTag
        // @description
        // Returns a list of all groups the player is in.
        // May work with offline players, depending on permission plugin.
        // -->
        registerTag("groups", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (Depends.permissions == null) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                    }
                    return null;
                }
                ListTag list = new ListTag();
                // TODO: optionally specify world
                for (String group : Depends.permissions.getGroups()) {
                    if (Depends.permissions.playerInGroup(null, ((PlayerTag) object).offlinePlayer, group)) {
                        list.add(group);
                    }
                }
                return list.getObjectAttribute(attribute.fulfill(1));
            }
        });

        registerTag("ban_info", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                attribute.fulfill(1);
                BanEntry ban = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(((PlayerTag) object).getName());
                if (ban == null || (ban.getExpiration() != null && ban.getExpiration().before(new Date()))) {
                    return null;
                }

                // <--[tag]
                // @attribute <PlayerTag.ban_info.expiration>
                // @returns DurationTag
                // @description
                // Returns the expiration of the player's ban, if they are banned.
                // Potentially can be null.
                // -->
                if (attribute.startsWith("expiration") && ban.getExpiration() != null) {
                    return new DurationTag(ban.getExpiration().getTime() / 50)
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <PlayerTag.ban_info.reason>
                // @returns ElementTag
                // @description
                // Returns the reason for the player's ban, if they are banned.
                // -->
                else if (attribute.startsWith("reason")) {
                    return new ElementTag(ban.getReason())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <PlayerTag.ban_info.created>
                // @returns DurationTag
                // @description
                // Returns when the player's ban was created, if they are banned.
                // -->
                else if (attribute.startsWith("created")) {
                    return new DurationTag(ban.getCreated().getTime() / 50)
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <PlayerTag.ban_info.source>
                // @returns ElementTag
                // @description
                // Returns the source of the player's ban, if they are banned.
                // -->
                else if (attribute.startsWith("source")) {
                    return new ElementTag(ban.getSource())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                return null;
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.in_group[<group_name>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is in the specified group.
        // This requires an online player - if the player may be offline, consider using
        // <@link tag PlayerTag.in_group[group_name].global>.
        // -->
        registerTag("in_group", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (Depends.permissions == null) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                    }
                    return null;
                }

                String group = attribute.getContext(1);

                // <--[tag]
                // @attribute <PlayerTag.in_group[<group_name>].global>
                // @returns ElementTag(Boolean)
                // @description
                // Returns whether the player has the group with no regard to the
                // player's current world.
                // (Works with offline players)
                // (Note: This may or may not be functional with your permissions system.)
                // -->

                // Non-world specific permission
                if (attribute.getAttribute(2).startsWith("global")) {
                    return new ElementTag(Depends.permissions.playerInGroup((World) null, ((PlayerTag) object).getName(), group)) // TODO: Vault UUID support?
                            .getObjectAttribute(attribute.fulfill(2));
                }

                // <--[tag]
                // @attribute <PlayerTag.in_group[<group_name>].world[<world>]>
                // @returns ElementTag(Boolean)
                // @description
                // Returns whether the player has the group in regards to a specific world.
                // (Works with offline players)
                // (Note: This may or may not be functional with your permissions system.)
                // -->

                // Permission in certain world
                else if (attribute.getAttribute(2).startsWith("world")) {
                    return new ElementTag(Depends.permissions.playerInGroup(attribute.getContext(2), ((PlayerTag) object).getName(), group)) // TODO: Vault UUID support?
                            .getObjectAttribute(attribute.fulfill(2));
                }

                // Permission in current world
                else if (((PlayerTag) object).isOnline()) {
                    return new ElementTag(Depends.permissions.playerInGroup(((PlayerTag) object).getPlayerEntity(), group))
                            .getObjectAttribute(attribute.fulfill(1));
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.has_permission[permission.node]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player has the specified node.
        // (Requires the player to be online)
        // -->
        registerTag("has_permission", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                String permission = attribute.getContext(1);

                // <--[tag]
                // @attribute <PlayerTag.has_permission[permission.node].global>
                // @returns ElementTag(Boolean)
                // @description
                // Returns whether the player has the specified node, regardless of world.
                // (Works with offline players)
                // (Note: this may or may not be functional with your permissions system.)
                // -->

                // Non-world specific permission
                if (attribute.getAttribute(2).startsWith("global")) {
                    if (Depends.permissions == null) {
                        if (!attribute.hasAlternative()) {
                            Debug.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                        }
                        return null;
                    }

                    return new ElementTag(Depends.permissions.has((World) null, ((PlayerTag) object).getName(), permission)) // TODO: Vault UUID support?
                            .getObjectAttribute(attribute.fulfill(2));
                }

                // <--[tag]
                // @attribute <PlayerTag.has_permission[permission.node].world[<world name>]>
                // @returns ElementTag(Boolean)
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
                            Debug.echoError("No permission system loaded! Have you installed Vault and a compatible permissions plugin?");
                        }
                        return null;
                    }

                    return new ElementTag(Depends.permissions.has(attribute.getContext(2), ((PlayerTag) object).getName(), permission)) // TODO: Vault UUID support?
                            .getObjectAttribute(attribute.fulfill(2));
                }

                // Permission in current world
                else if (((PlayerTag) object).isOnline()) {
                    return new ElementTag(((PlayerTag) object).getPlayerEntity().hasPermission(permission))
                            .getObjectAttribute(attribute.fulfill(1));
                }
                return null;
            }
        });
        registerTag("permission", tagProcessor.registeredObjectTags.get("has_permission"));

        // <--[tag]
        // @attribute <PlayerTag.uuid>
        // @returns ElementTag
        // @description
        // Returns the UUID of the player.
        // Works with offline players.
        // -->
        registerTag("uuid", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getOfflinePlayer().getUniqueId().toString()).getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.name>
        // @returns ElementTag
        // @description
        // Returns the name of the player.
        // Works with offline players.
        // -->
        registerTag("name", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                attribute = attribute.fulfill(1);
                // <--[tag]
                // @attribute <PlayerTag.name.list>
                // @returns ElementTag
                // @description
                // Returns the name of the player as shown in the player list.
                // -->
                if (attribute.startsWith("list") && ((PlayerTag) object).isOnline()) {
                    return new ElementTag(((PlayerTag) object).getPlayerEntity().getPlayerListName())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                // <--[tag]
                // @attribute <PlayerTag.name.display>
                // @returns ElementTag
                // @description
                // Returns the display name of the player, which may contain
                // prefixes and suffixes, colors, etc.
                // -->
                if (attribute.startsWith("display") && ((PlayerTag) object).isOnline()) {
                    return new ElementTag(((PlayerTag) object).getPlayerEntity().getDisplayName())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                return new ElementTag(((PlayerTag) object).getName()).getObjectAttribute(attribute);
            }
        });

        /////////////////////
        //   INVENTORY ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.inventory>
        // @returns InventoryTag
        // @description
        // Returns a InventoryTag of the player's current inventory.
        // Works with offline players.
        // -->
        registerTag("inventory", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return ((PlayerTag) object).getInventory().getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.enderchest>
        // @returns InventoryTag
        // @description
        // Gets the player's enderchest inventory.
        // Works with offline players.
        // -->
        registerTag("enderchest", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return ((PlayerTag) object).getEnderChest().getObjectAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   ONLINE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.open_inventory>
        // @returns InventoryTag
        // @description
        // Gets the inventory the player currently has open. If the player has no open
        // inventory, this returns the player's inventory.
        // -->
        registerOnlineOnlyTag("open_inventory", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return InventoryTag.mirrorBukkitInventory(((PlayerTag) object).getPlayerEntity().getOpenInventory().getTopInventory())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.selected_trade_index>
        // @returns ElementTag(Number)
        // @description
        // Returns the index of the trade the player is currently viewing, if any.
        // -->
        registerOnlineOnlyTag("selected_trade_index", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (((PlayerTag) object).getPlayerEntity().getOpenInventory().getTopInventory() instanceof MerchantInventory) {
                    return new ElementTag(((MerchantInventory) ((PlayerTag) object).getPlayerEntity().getOpenInventory().getTopInventory())
                            .getSelectedRecipeIndex() + 1).getObjectAttribute(attribute.fulfill(1));
                }
                return null;
            }
        });

        // This is almost completely broke and only works if the player has placed items in the trade slots.
        // [tag]
        // @attribute <PlayerTag.selected_trade>
        // @returns TradeTag
        // @description
        // Returns the trade the player is currently viewing, if any.
        //
        /*
        registerOnlineOnlyTag("selected_trade", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                Inventory playerInventory = getPlayerEntity().getOpenInventory().getTopInventory();
            if (playerInventory instanceof MerchantInventory
                    && ((MerchantInventory) playerInventory).getSelectedRecipe() != null) {
                return new TradeTag(((MerchantInventory) playerInventory).getSelectedRecipe()).getObjectAttribute(attribute.fulfill(1));
            }
            }
        });
        */

        // <--[tag]
        // @attribute <PlayerTag.item_on_cursor>
        // @returns ItemTag
        // @description
        // Returns the item on the player's cursor, if any. This includes
        // chest interfaces, inventories, and hotbars, etc.
        // -->
        registerOnlineOnlyTag("item_on_cursor", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ItemTag(((PlayerTag) object).getPlayerEntity().getItemOnCursor())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.item_in_hand.slot>
        // @returns ElementTag(Number)
        // @description
        // Returns the slot location of the player's selected item.
        // -->
        registerOnlineOnlyTag("item_in_hand.slot", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getInventory().getHeldItemSlot() + 1)
                        .getObjectAttribute(attribute.fulfill(2));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.sidebar.lines>
        // @returns ListTag
        // @description
        // Returns the current lines set on the player's Sidebar via the Sidebar command.
        // -->
        registerOnlineOnlyTag("sidebar.lines", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                Sidebar sidebar = SidebarCommand.getSidebar((PlayerTag) object);
                if (sidebar == null) {
                    return null;
                }
                return new ListTag(sidebar.getLinesText()).getObjectAttribute(attribute.fulfill(2));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.sidebar.title>
        // @returns ElementTag
        // @description
        // Returns the current title set on the player's Sidebar via the Sidebar command.
        // -->
        registerOnlineOnlyTag("sidebar.title", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                Sidebar sidebar = SidebarCommand.getSidebar((PlayerTag) object);
                if (sidebar == null) {
                    return null;
                }
                return new ElementTag(sidebar.getTitle()).getObjectAttribute(attribute.fulfill(2));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.sidebar.scores>
        // @returns ListTag
        // @description
        // Returns the current scores set on the player's Sidebar via the Sidebar command,
        // in the same order as <@link tag PlayerTag.sidebar.lines>.
        // -->
        registerOnlineOnlyTag("sidebar.scores", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                Sidebar sidebar = SidebarCommand.getSidebar((PlayerTag) object);
                if (sidebar == null) {
                    return null;
                }
                ListTag scores = new ListTag();
                for (int score : sidebar.getScores()) {
                    scores.add(String.valueOf(score));
                }
                return scores.getObjectAttribute(attribute.fulfill(2));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.skin_blob>
        // @returns ElementTag
        // @description
        // Returns the player's current skin blob.
        // @mechanism PlayerTag.skin_blob
        // -->
        registerOnlineOnlyTag("skin_blob", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(NMSHandler.getInstance().getProfileEditor().getPlayerSkinBlob(((PlayerTag) object).getPlayerEntity()))
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        registerOnlineOnlyTag("attack_cooldown", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                attribute.fulfill(1);

                // <--[tag]
                // @attribute <PlayerTag.attack_cooldown.duration>
                // @returns DurationTag
                // @description
                // Returns the amount of time that passed since the start of the attack cooldown.
                // -->
                if (attribute.startsWith("duration")) {
                    return new DurationTag((long) NMSHandler.getPlayerHelper()
                            .ticksPassedDuringCooldown(((PlayerTag) object).getPlayerEntity())).getObjectAttribute(attribute.fulfill(1));
                }


                // <--[tag]
                // @attribute <PlayerTag.attack_cooldown.max_duration>
                // @returns DurationTag
                // @description
                // Returns the maximum amount of time that can pass before the player's main hand has returned
                // to its original place after the cooldown has ended.
                // NOTE: This is slightly inaccurate and may not necessarily match with the actual attack
                // cooldown progress.
                // -->
                else if (attribute.startsWith("max_duration")) {
                    return new DurationTag((long) NMSHandler.getPlayerHelper()
                            .getMaxAttackCooldownTicks(((PlayerTag) object).getPlayerEntity())).getObjectAttribute(attribute.fulfill(1));
                }


                // <--[tag]
                // @attribute <PlayerTag.attack_cooldown.percent>
                // @returns ElementTag(Decimal)
                // @description
                // Returns the progress of the attack cooldown. 0 means that the attack cooldown has just
                // started, while 100 means that the attack cooldown has finished.
                // NOTE: This may not match exactly with the clientside attack cooldown indicator.
                // -->
                else if (attribute.startsWith("percent")) {
                    return new ElementTag(NMSHandler.getPlayerHelper()
                            .getAttackCooldownPercent(((PlayerTag) object).getPlayerEntity()) * 100).getObjectAttribute(attribute.fulfill(1));
                }

                Debug.echoError("The tag 'player.attack_cooldown...' must be followed by a sub-tag.");

                return null;
            }
        });


        /////////////////////
        //   CITIZENS ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.selected_npc>
        // @returns NPCTag
        // @description
        // Returns the NPCTag that the player currently has selected with
        // '/npc select', null if no player selected.
        // -->
        registerOnlineOnlyTag("selected_npc", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (((PlayerTag) object).getPlayerEntity().hasMetadata("selected")) {
                    return ((PlayerTag) object).getSelectedNPC().getObjectAttribute(attribute.fulfill(1));
                }
                return null;
            }
        });


        /////////////////////
        //   CONVERSION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.entity>
        // @returns EntityTag
        // @description
        // Returns the EntityTag object of the player.
        // (Note: This should never actually be needed. <PlayerTag> is considered a valid EntityTag by script commands.)
        // -->
        registerOnlineOnlyTag("entity", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new EntityTag(((PlayerTag) object).getPlayerEntity())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   IDENTIFICATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.ip>
        // @returns ElementTag
        // @description
        // Returns the player's IP address host name.
        // -->
        registerOnlineOnlyTag("ip", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                attribute = attribute.fulfill(1);
                // <--[tag]
                // @attribute <PlayerTag.ip.address_only>
                // @returns ElementTag
                // @description
                // Returns the player's IP address.
                // -->
                if (attribute.startsWith("address_only")) {
                    return new ElementTag(((PlayerTag) object).getPlayerEntity().getAddress().toString())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                String host = ((PlayerTag) object).getPlayerEntity().getAddress().getHostName();
                // <--[tag]
                // @attribute <PlayerTag.ip.address>
                // @returns ElementTag
                // @description
                // Returns the player's IP address.
                // -->
                if (attribute.startsWith("address")) {
                    return new ElementTag(((PlayerTag) object).getPlayerEntity().getAddress().toString())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                return new ElementTag(host)
                        .getObjectAttribute(attribute);
            }
        });
        registerOnlineOnlyTag("host_name", tagProcessor.registeredObjectTags.get("ip"));

        // <--[tag]
        // @attribute <PlayerTag.nameplate>
        // @returns ElementTag
        // @description
        // Returns the displayed text in the nameplate of the player.
        // -->
        registerOnlineOnlyTag("nameplate", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(NMSHandler.getInstance().getProfileEditor().getPlayerName(((PlayerTag) object).getPlayerEntity()))
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        /////////////////////
        //   LOCATION ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.compass_target>
        // @returns LocationTag
        // @description
        // Returns the location of the player's compass target.
        // -->
        registerOnlineOnlyTag("compass_target", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                Location target = ((PlayerTag) object).getPlayerEntity().getCompassTarget();
                if (target != null) {
                    return new LocationTag(target).getObjectAttribute(attribute.fulfill(1));
                }
                return null;
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.chunk_loaded[<chunk>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player has the chunk loaded on their client.
        // -->
        registerOnlineOnlyTag("chunk_loaded", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (!attribute.hasContext(1)) {
                    return null;
                }
                ChunkTag chunk = ChunkTag.valueOf(attribute.getContext(1));
                if (chunk == null) {
                    return null;
                }
                return new ElementTag(chunk.isLoadedSafe() && ((PlayerTag) object).hasChunkLoaded(chunk.getChunkForTag(attribute))).getObjectAttribute(attribute.fulfill(1));
            }
        });


        /////////////////////
        //   STATE ATTRIBUTES
        /////////////////

        // <--[tag]
        // @attribute <PlayerTag.can_fly>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is allowed to fly.
        // @mechanism PlayerTag.can_fly
        // -->
        registerOnlineOnlyTag("can_fly", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getAllowFlight())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });
        registerOnlineOnlyTag("allowed_flight", tagProcessor.registeredObjectTags.get("can_fly"));

        // <--[tag]
        // @attribute <PlayerTag.fly_speed>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the speed the player can fly at.
        // -->
        registerOnlineOnlyTag("fly_speed", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getFlySpeed())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.food_level.formatted>
        // @returns ElementTag
        // @description
        // Returns a 'formatted' value of the player's current food level.
        // May be 'starving', 'famished', 'parched, 'hungry' or 'healthy'.
        // -->
        registerOnlineOnlyTag("food_level.formatted", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                double maxHunger = ((PlayerTag) object).getPlayerEntity().getMaxHealth();
                if (attribute.hasContext(2)) {
                    maxHunger = attribute.getIntContext(2);
                }
                int foodLevel = ((PlayerTag) object).getFoodLevel();
                if (foodLevel / maxHunger < .10) {
                    return new ElementTag("starving").getObjectAttribute(attribute.fulfill(2));
                }
                else if (foodLevel / maxHunger < .40) {
                    return new ElementTag("famished").getObjectAttribute(attribute.fulfill(2));
                }
                else if (foodLevel / maxHunger < .75) {
                    return new ElementTag("parched").getObjectAttribute(attribute.fulfill(2));
                }
                else if (foodLevel / maxHunger < 1) {
                    return new ElementTag("hungry").getObjectAttribute(attribute.fulfill(2));
                }
                else {
                    return new ElementTag("healthy").getObjectAttribute(attribute.fulfill(2));
                }
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.saturation>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the current saturation of the player.
        // -->
        registerOnlineOnlyTag("saturation", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getSaturation())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.food_level>
        // @returns ElementTag(Number)
        // @description
        // Returns the current food level of the player.
        // -->
        registerOnlineOnlyTag("food_level", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getFoodLevel())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.gamemode>
        // @returns ElementTag
        // @description
        // Returns the name of the gamemode the player is currently set to.
        // -->
        registerOnlineOnlyTag("gamemode", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                attribute = attribute.fulfill(1);
                // <--[tag]
                // @attribute <PlayerTag.gamemode.id>
                // @returns ElementTag(Number)
                // @description
                // Returns the gamemode ID of the player. 0 = survival, 1 = creative, 2 = adventure, 3 = spectator
                // -->
                if (attribute.startsWith("id")) {
                    return new ElementTag(((PlayerTag) object).getPlayerEntity().getGameMode().getValue())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getGameMode().name())
                        .getObjectAttribute(attribute);
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.is_blocking>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is currently blocking.
        // -->
        registerOnlineOnlyTag("is_blocking", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().isBlocking())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.ping>
        // @returns ElementTag(Number)
        // @description
        // Returns the player's current ping.
        // -->
        registerOnlineOnlyTag("ping", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(NMSHandler.getPlayerHelper().getPing(((PlayerTag) object).getPlayerEntity()))
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.is_flying>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is currently flying.
        // -->
        registerOnlineOnlyTag("is_flying", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().isFlying())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.is_sleeping>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is currently sleeping.
        // -->
        registerOnlineOnlyTag("is_sleeping", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().isSleeping())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.is_sneaking>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is currently sneaking.
        // -->
        registerOnlineOnlyTag("is_sneaking", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().isSneaking())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.is_sprinting>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player is currently sprinting.
        // -->
        registerOnlineOnlyTag("is_sprinting", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().isSprinting())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.has_advancement[<advancement>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the player has completed the specified advancement.
        // -->
        registerOnlineOnlyTag("has_advancement", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (!attribute.hasContext(1)) {
                    return null;
                }
                Advancement adv = AdvancementHelper.getAdvancement(attribute.getContext(1));
                if (adv == null) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("Advancement '" + attribute.getContext(1) + "' does not exist.");
                    }
                    return null;
                }
                AdvancementProgress progress = ((PlayerTag) object).getPlayerEntity().getAdvancementProgress(adv);
                return new ElementTag(progress.isDone()).getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.list_advancements>
        // @returns ListTag
        // @description
        // Returns a list of the names of all advancements the player has completed.
        // -->
        registerOnlineOnlyTag("list_advancements", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                ListTag list = new ListTag();
                Bukkit.advancementIterator().forEachRemaining((adv) -> {
                    if (((PlayerTag) object).getPlayerEntity().getAdvancementProgress(adv).isDone()) {
                        list.add(adv.getKey().toString());
                    }
                });
                return list.getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.statistic[<statistic>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the player's current value for the specified statistic.
        // Valid statistics: <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Statistic.html>
        // -->
        registerOnlineOnlyTag("statistic", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (!attribute.hasContext(1)) {
                    return null;
                }
                Statistic statistic = Statistic.valueOf(attribute.getContext(1).toUpperCase());
                if (statistic == null) {
                    return null;
                }

                // <--[tag]
                // @attribute <PlayerTag.statistic[<statistic>].qualifier[<material>/<entity>]>
                // @returns ElementTag(Number)
                // @description
                // Returns the player's current value for the specified statistic, with the
                // specified qualifier, which can be either an entity or material.
                // Valid statistics: <@link url https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Statistic.html>
                // -->
                if (attribute.getAttribute(2).startsWith("qualifier")) {
                    ObjectTag obj = ObjectFetcher.pickObjectFor(attribute.getContext(2), attribute.context);
                    try {
                        if (obj instanceof MaterialTag) {
                            return new ElementTag(((PlayerTag) object).getPlayerEntity().getStatistic(statistic, ((MaterialTag) obj).getMaterial()))
                                    .getObjectAttribute(attribute.fulfill(2));
                        }
                        else if (obj instanceof EntityTag) {
                            return new ElementTag(((PlayerTag) object).getPlayerEntity().getStatistic(statistic, ((EntityTag) obj).getBukkitEntityType()))
                                    .getObjectAttribute(attribute.fulfill(2));
                        }
                        else {
                            return null;
                        }
                    }
                    catch (Exception e) {
                        Debug.echoError("Invalid statistic: " + statistic + " for this player!");
                        return null;
                    }
                }
                try {
                    return new ElementTag(((PlayerTag) object).getPlayerEntity().getStatistic(statistic)).getObjectAttribute(attribute.fulfill(1));
                }
                catch (Exception e) {
                    Debug.echoError("Invalid statistic: " + statistic + " for this player!");
                    return null;
                }
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.time_asleep>
        // @returns DurationTag
        // @description
        // Returns the time the player has been asleep.
        // -->
        registerOnlineOnlyTag("time_asleep", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new DurationTag(((PlayerTag) object).getPlayerEntity().getSleepTicks() / 20)
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.time>
        // @returns ElementTag(Number)
        // @description
        // Returns the time the player is currently experiencing. This time could differ from
        // the time that the rest of the world is currently experiencing if a 'time' or 'freeze_time'
        // mechanism is being used on the player.
        // -->
        registerOnlineOnlyTag("time", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getPlayerTime())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.walk_speed>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the speed the player can walk at.
        // -->
        registerOnlineOnlyTag("walk_speed", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getWalkSpeed())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.weather>
        // @returns ElementTag
        // @mechanism PlayerTag.weather
        // @description
        // Returns the type of weather the player is experiencing. This will be different
        // from the weather currently in the world that the player is residing in if
        // the weather is currently being forced onto the player.
        // Returns null if the player does not currently have any forced weather.
        // -->
        registerOnlineOnlyTag("weather", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (((PlayerTag) object).getPlayerEntity().getPlayerWeather() != null) {
                    return new ElementTag(((PlayerTag) object).getPlayerEntity().getPlayerWeather().name())
                            .getObjectAttribute(attribute.fulfill(1));
                }
                else {
                    return null;
                }
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.xp.level>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of XP levels the player has.
        // -->
        registerOnlineOnlyTag("xp.level", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getLevel())
                        .getObjectAttribute(attribute.fulfill(2));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.xp.to_next_level>
        // @returns ElementTag(Number)
        // @description
        // Returns the amount of XP needed to get to the next level.
        // -->
        registerOnlineOnlyTag("xp.to_next_level", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getExpToLevel())
                        .getObjectAttribute(attribute.fulfill(2));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.xp.total>
        // @returns ElementTag(Number)
        // @description
        // Returns the total amount of experience points.
        // -->
        registerOnlineOnlyTag("xp.total", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getTotalExperience())
                        .getObjectAttribute(attribute.fulfill(2));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.xp>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the percentage of experience points to the next level.
        // -->
        registerOnlineOnlyTag("xp", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((PlayerTag) object).getPlayerEntity().getExp() * 100)
                        .getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.chat_prefix>
        // @returns ElementTag
        // @plugin Vault
        // @description
        // Returns the player's chat prefix.
        // NOTE: May work with offline players.
        // Requires a Vault-compatible chat plugin.
        // @mechanism PlayerTag.chat_prefix
        // -->
        registerTag("chat_prefix", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (Depends.chat == null) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("'chat_prefix' tag unavailable: Vault and a chat plugin are required.");
                    }
                    return null;
                }
                String prefix = Depends.chat.getPlayerPrefix(((PlayerTag) object).getWorld().getName(), ((PlayerTag) object).getOfflinePlayer());
                if (prefix == null) {
                    return null;
                }
                return new ElementTag(prefix).getObjectAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <PlayerTag.chat_suffix>
        // @returns ElementTag
        // @plugin Vault
        // @description
        // Returns the player's chat suffix.
        // NOTE: May work with offline players.
        // Requires a Vault-compatible chat plugin.
        // @mechanism PlayerTag.chat_suffix
        // -->
        registerTag("chat_suffix", new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (Depends.chat == null) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("'chat_suffix' tag unavailable: Vault and a chat plugin are required.");
                    }
                    return null;
                }
                String suffix = Depends.chat.getPlayerSuffix(((PlayerTag) object).getWorld().getName(), ((PlayerTag) object).getOfflinePlayer());
                if (suffix == null) {
                    return null;
                }
                return new ElementTag(suffix).getObjectAttribute(attribute.fulfill(1));
            }
        });
    }

    public static ObjectTagProcessor tagProcessor = new ObjectTagProcessor();

    public static void registerOnlineOnlyTag(String name, TagRunnable.ObjectForm runnable) {
        TagRunnable.ObjectForm newRunnable = new TagRunnable.ObjectForm() {
            @Override
            public ObjectTag run(Attribute attribute, ObjectTag object) {
                if (!((PlayerTag) object).isOnline()) {
                    if (!attribute.hasAlternative()) {
                        Debug.echoError("Player is not online, but tag '" + attribute.getAttributeWithoutContext(1) + "' requires the player be online, for player: " + object.debuggable());
                    }
                    return null;
                }
                return runnable.run(attribute, object);
            }
        };
        newRunnable.name = runnable.name;
        registerTag(name, newRunnable);
    }

    public static void registerTag(String name, TagRunnable.ObjectForm runnable) {
        tagProcessor.registerTag(name, runnable);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    @Override
    public ObjectTag getNextObjectTypeDown() {
        if (isOnline()) {
            return new EntityTag(getPlayerEntity());
        }
        return new ElementTag(identify());
    }

    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to a player!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object PlayerTag
        // @name respawn
        // @input None
        // @description
        // Forces the player to respawn if they are on the death screen.
        // -->
        if (mechanism.matches("respawn")) {
            NMSHandler.getPacketHelper().respawn(getPlayerEntity());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name vision
        // @input Element
        // @description
        // Changes the player's vision to the provided entity type. Valid types:
        // ENDERMAN, CAVE_SPIDER, SPIDER, CREEPER
        // Provide no value to reset the player's vision.
        // -->
        if (mechanism.matches("vision")) {
            if (mechanism.hasValue() && mechanism.requireEnum(false, EntityType.values())) {
                NMSHandler.getPacketHelper().setVision(getPlayerEntity(), EntityType.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else {
                NMSHandler.getPacketHelper().forceSpectate(getPlayerEntity(), getPlayerEntity());
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name level
        // @input Element(Number)
        // @description
        // Sets the level on the player. Does not affect the current progression
        // of experience towards next level.
        // @tags
        // <PlayerTag.xp.level>
        // -->
        if (mechanism.matches("level") && mechanism.requireInteger()) {
            setLevel(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name item_slot
        // @input Element(Number)
        // @description
        // Sets the inventory slot that the player has selected.
        // Works with offline players.
        // @tags
        // <PlayerTag.item_in_hand.slot>
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
        // @object PlayerTag
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
                Debug.echoError("Invalid input! Must be in the form PROPERTY,VALUE");
            }
            else {
                try {
                    getPlayerEntity().setWindowProperty(InventoryView.Property.valueOf(split[0].toUpperCase()), Integer.parseInt(split[1]));
                }
                catch (NumberFormatException e) {
                    Debug.echoError("Input value must be a number!");
                }
                catch (IllegalArgumentException e) {
                    Debug.echoError("Must specify a valid window property!");
                }
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name item_on_cursor
        // @input ItemTag
        // @description
        // Sets the item on the player's cursor. This includes
        // chest interfaces, inventories, and hotbars, etc.
        // @tags
        // <PlayerTag.item_on_cursor>
        // -->
        if (mechanism.matches("item_on_cursor") && mechanism.requireObject(ItemTag.class)) {
            getPlayerEntity().setItemOnCursor(mechanism.valueAsType(ItemTag.class).getItemStack());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name award_advancement
        // @input Element
        // @description
        // Awards an achievement to the player.
        // @tags
        // <PlayerTag.has_advancement[<name>]>
        // -->
        if (mechanism.matches("award_advancement")) {
            Advancement adv = AdvancementHelper.getAdvancement(mechanism.getValue().asString());
            if (adv == null) {
                if (mechanism.shouldDebug()) {
                    Debug.echoError("Advancement '" + mechanism.getValue().asString() + "' does not exist.");
                }
                return;
            }
            AdvancementProgress prog = getPlayerEntity().getAdvancementProgress(adv);
            for (String criteria : prog.getRemainingCriteria()) {
                prog.awardCriteria(criteria);
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name absorption_health
        // @input Element(Decimal)
        // @description
        // Sets the player's absorption health.
        // @tags
        // <PlayerTag.absorption_health>
        // -->
        if (mechanism.matches("absorption_health") && mechanism.requireFloat()) {
            NMSHandler.getPlayerHelper().setAbsorption(getPlayerEntity(), mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name fake_absorption_health
        // @input Element(Decimal)
        // @description
        // Shows the player fake absorption health that persists on damage.
        // -->
        if (mechanism.matches("fake_absorption_health") && mechanism.requireFloat()) {
            NMSHandler.getPacketHelper().setFakeAbsorption(getPlayerEntity(), mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name health_scale
        // @input Element(Decimal)
        // @description
        // Sets the 'health scale' on the Player. Each heart equals '2'. The standard health scale is
        // 20, so for example, indicating a value of 40 will display double the amount of hearts
        // standard.
        // Player relogging will reset this mechanism.
        // @tags
        // <PlayerTag.health.scale>
        // -->
        if (mechanism.matches("health_scale") && mechanism.requireDouble()) {
            getPlayerEntity().setHealthScale(mechanism.getValue().asDouble());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name scale_health
        // @input Element(Boolean)
        // @description
        // Enables or disables the health scale mechanism.getValue(). Disabling will result in the standard
        // amount of hearts being shown.
        // @tags
        // <PlayerTag.health.is_scaled>
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
        // @object PlayerTag
        // @name redo_attack_cooldown
        // @input None
        // @description
        // Forces the player to wait for the full attack cooldown duration for the item in their hand.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <PlayerTag.attack_cooldown.time_passed>
        // <PlayerTag.attack_cooldown.max_duration>
        // <PlayerTag.attack_cooldown.percent_done>
        // -->
        if (mechanism.matches("redo_attack_cooldown")) {
            NMSHandler.getPlayerHelper().setAttackCooldown(getPlayerEntity(), 0);
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name reset_attack_cooldown
        // @input None
        // @description
        // Ends the player's attack cooldown.
        // NOTE: This will do nothing if the player's attack speed attribute is set to 0.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <PlayerTag.attack_cooldown.time_passed>
        // <PlayerTag.attack_cooldown.max_duration>
        // <PlayerTag.attack_cooldown.percent_done>
        // -->
        if (mechanism.matches("reset_attack_cooldown")) {
            PlayerHelper playerHelper = NMSHandler.getPlayerHelper();
            playerHelper.setAttackCooldown(getPlayerEntity(), Math.round(playerHelper.getMaxAttackCooldownTicks(getPlayerEntity())));
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name attack_cooldown_percent
        // @input Element(Decimal)
        // @description
        // Sets the progress of the player's attack cooldown. Takes a decimal from 0 to 1.
        // 0 means the cooldown has just begun, while 1 means the cooldown has been completed.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <PlayerTag.attack_cooldown.time_passed>
        // <PlayerTag.attack_cooldown.max_duration>
        // <PlayerTag.attack_cooldown.percent_done>
        // -->
        if (mechanism.matches("attack_cooldown_percent") && mechanism.requireFloat()) {
            float percent = mechanism.getValue().asFloat();
            System.out.println(percent + " >> " + (percent >= 0 && percent <= 1));
            if (percent >= 0 && percent <= 1) {
                PlayerHelper playerHelper = NMSHandler.getPlayerHelper();
                playerHelper.setAttackCooldown(getPlayerEntity(),
                        Math.round(playerHelper.getMaxAttackCooldownTicks(getPlayerEntity()) * mechanism.getValue().asFloat()));
            }
            else {
                Debug.echoError("Invalid percentage! \"" + percent + "\" is not between 0 and 1!");
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name attack_cooldown
        // @input Duration
        // @description
        // Sets the player's time since their last attack. If the time is greater than the max duration of their
        // attack cooldown, then the cooldown is considered finished.
        // NOTE: The clientside attack cooldown indicator will not reflect this change!
        // @tags
        // <PlayerTag.attack_cooldown.time_passed>
        // <PlayerTag.attack_cooldown.max_duration>
        // <PlayerTag.attack_cooldown.percent_done>
        // -->
        if (mechanism.matches("attack_cooldown") && mechanism.requireObject(DurationTag.class)) {
            NMSHandler.getPlayerHelper().setAttackCooldown(getPlayerEntity(),
                    mechanism.getValue().asType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object PlayerTag
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
        // @object PlayerTag
        // @name saturation
        // @input Element(Decimal)
        // @description
        // Sets the current food saturation level of a player.
        // @tags
        // <PlayerTag.saturation>
        // -->
        if (mechanism.matches("saturation") && mechanism.requireFloat()) {
            getPlayerEntity().setSaturation(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object PlayerTag
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
                Debug.echoError("No map found for ID " + mechanism.getValue().asInt() + "!");
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name food_level
        // @input Element(Number)
        // @description
        // Sets the current food level of a player. Typically, '20' is full.
        // @tags
        // <PlayerTag.food_level>
        // -->
        if (mechanism.matches("food_level") && mechanism.requireInteger()) {
            setFoodLevel(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name bed_spawn_location
        // @input LocationTag
        // @description
        // Sets the bed location that the player respawns at.
        // @tags
        // <PlayerTag.bed_spawn>
        // -->
        if (mechanism.matches("bed_spawn_location") && mechanism.requireObject(LocationTag.class)) {
            setBedSpawnLocation(mechanism.valueAsType(LocationTag.class));
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name can_fly
        // @input Element(Boolean)
        // @description
        // Sets whether the player is allowed to fly.
        // @tags
        // <PlayerTag.can_fly>
        // -->
        if (mechanism.matches("can_fly") && mechanism.requireBoolean()) {
            getPlayerEntity().setAllowFlight(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name fly_speed
        // @input Element(Decimal)
        // @description
        // Sets the fly speed of the player. Valid range is 0.0 to 1.0
        // @tags
        // <PlayerTag.fly_speed>
        // -->
        if (mechanism.matches("fly_speed") && mechanism.requireFloat()) {
            setFlySpeed(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name flying
        // @input Element(Boolean)
        // @description
        // Sets whether the player is flying.
        // @tags
        // <PlayerTag.is_flying>
        // -->
        if (mechanism.matches("flying") && mechanism.requireBoolean()) {
            getPlayerEntity().setFlying(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name sprinting
        // @input Element(Boolean)
        // @description
        // Sets whether the player is sprinting.
        // @tags
        // <PlayerTag.is_sprinting>
        // -->
        if (mechanism.matches("sprinting") && mechanism.requireBoolean()) {
            getPlayerEntity().setSprinting(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name gamemode
        // @input Element
        // @description
        // Sets the game mode of the player.
        // Valid gamemodes are survival, creative, adventure, and spectator.
        // @tags
        // <PlayerTag.gamemode>
        // <PlayerTag.gamemode.id>
        // -->
        if (mechanism.matches("gamemode") && mechanism.requireEnum(false, GameMode.values())) {
            setGameMode(GameMode.valueOf(mechanism.getValue().asString().toUpperCase()));
        }

        // <--[mechanism]
        // @object PlayerTag
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
        // @object PlayerTag
        // @name weather
        // @input Element
        // @description
        // Sets the weather condition for the player. This does NOT affect the weather
        // in the world, and will block any world weather changes until the 'reset_weather'
        // mechanism is used. Valid weather: CLEAR, DOWNFALL
        // @tags
        // <PlayerTag.weather>
        // -->
        if (mechanism.matches("weather") && mechanism.requireEnum(false, WeatherType.values())) {
            getPlayerEntity().setPlayerWeather(WeatherType.valueOf(mechanism.getValue().asString().toUpperCase()));
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name reset_weather
        // @input None
        // @description
        // Resets the weather on the Player to the conditions currently taking place in the Player's
        // current world.
        // @tags
        // <PlayerTag.weather>
        // -->
        if (mechanism.matches("reset_weather")) {
            getPlayerEntity().resetPlayerWeather();
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name player_list_name
        // @input Element
        // @description
        // Sets the entry that is shown in the 'player list' that is shown when pressing tab.
        // @tags
        // <PlayerTag.name.list>
        // -->
        if (mechanism.matches("player_list_name")) {
            getPlayerEntity().setPlayerListName(mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name display_name
        // @input Element
        // @description
        // Sets the name displayed for the player when chatting.
        // @tags
        // <PlayerTag.name.display>
        // -->
        if (mechanism.matches("display_name")) {
            getPlayerEntity().setDisplayName(mechanism.getValue().asString());
            return;
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name show_workbench
        // @input LocationTag
        // @description
        // Shows the player a workbench GUI corresponding to a given location.
        // @tags
        // None
        // -->
        if (mechanism.matches("show_workbench") && mechanism.requireObject(LocationTag.class)) {
            getPlayerEntity().openWorkbench(mechanism.valueAsType(LocationTag.class), true);
            return;
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name location
        // @input LocationTag
        // @description
        // If the player is online, teleports the player to a given location.
        // Otherwise, sets the player's next spawn location.
        // @tags
        // <PlayerTag.location>
        // -->
        if (mechanism.matches("location") && mechanism.requireObject(LocationTag.class)) {
            setLocation(mechanism.valueAsType(LocationTag.class));
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name time
        // @input Element(Number)
        // @description
        // Sets the time of day the Player is currently experiencing. Setting this will cause the
        // player to have a different time than other Players in the world are experiencing though
        // time will continue to progress. Using the 'reset_time' mechanism, or relogging your player
        // will reset this mechanism to match the world's current time. Valid range is 0-24000.
        // The value is relative to the current world time, and will continue moving at the same rate as current world time moves.
        // @tags
        // <PlayerTag.time>
        // -->
        if (mechanism.matches("time") && mechanism.requireInteger()) {
            getPlayerEntity().setPlayerTime(mechanism.getValue().asInt(), true);
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name freeze_time
        // @input Element(Number)
        // @description
        // Sets the time of day the Player is currently experiencing and freezes it there. Note:
        // there is a small 'twitch effect' when looking at the sky when time is frozen.
        // Setting this will cause the player to have a different time than other Players in
        // the world are experiencing. Using the 'reset_time' mechanism, or relogging your player
        // will reset this mechanism to match the world's current time. Valid range is 0-24000.
        // @tags
        // <PlayerTag.time>
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
        // @object PlayerTag
        // @name reset_time
        // @input None
        // @description
        // Resets any altered time that has been applied to this player. Using this will make
        // the Player's time match the world's current time.
        // @tags
        // <PlayerTag.time>
        // -->
        if (mechanism.matches("reset_time")) {
            getPlayerEntity().resetPlayerTime();
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name walk_speed
        // @input Element(Decimal)
        // @description
        // Sets the walk speed of the player. The standard value is '0.2'. Valid range is 0.0 to 1.0
        // @tags
        // <PlayerTag.walk_speed>
        // -->
        if (mechanism.matches("walk_speed") && mechanism.requireFloat()) {
            getPlayerEntity().setWalkSpeed(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name exhaustion
        // @input Element(Decimal)
        // @description
        // Sets the exhaustion level of a player.
        // @tags
        // <PlayerTag.exhaustion>
        // -->
        if (mechanism.matches("exhaustion") && mechanism.requireFloat()) {
            getPlayerEntity().setExhaustion(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name show_entity
        // @input EntityTag
        // @description
        // Shows the player a previously hidden entity.
        // -->
        if (mechanism.matches("show_entity") && mechanism.requireObject(EntityTag.class)) {
            NMSHandler.getEntityHelper().unhideEntity(getPlayerEntity(), mechanism.valueAsType(EntityTag.class).getBukkitEntity());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name hide_entity
        // @input EntityTag(|Element(Boolean))
        // @description
        // Hides an entity from the player. You can optionally also specify a boolean to determine
        // whether the entity should be kept in the tab list (players only).
        // -->
        if (mechanism.matches("hide_entity")) {
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + ListTag.internal_escape + "]", 2);
                if (split.length > 0 && new ElementTag(split[0]).matchesType(EntityTag.class)) {
                    EntityTag entity = mechanism.valueAsType(EntityTag.class);
                    if (!entity.isSpawned()) {
                        Debug.echoError("Can't hide the unspawned entity '" + split[0] + "'!");
                    }
                    else if (split.length > 1 && new ElementTag(split[1]).isBoolean()) {
                        NMSHandler.getEntityHelper().hideEntity(getPlayerEntity(), entity.getBukkitEntity(),
                                new ElementTag(split[1]).asBoolean());
                    }
                    else {
                        NMSHandler.getEntityHelper().hideEntity(getPlayerEntity(), entity.getBukkitEntity(), false);
                    }
                }
                else {
                    Debug.echoError("'" + split[0] + "' is not a valid entity!");
                }
            }
            else {
                Debug.echoError("Must specify an entity to hide!");
            }
        }

        // <--[mechanism]
        // @object PlayerTag
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
                String[] split = mechanism.getValue().asString().split("[\\|" + ListTag.internal_escape + "]", 2);
                if (split.length == 2 && new ElementTag(split[0]).isDouble()) {
                    BossBarHelper.showSimpleBossBar(getPlayerEntity(), split[1], new ElementTag(split[0]).asDouble() * (1.0 / 200.0));
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
        // @object PlayerTag
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
                String[] split = mechanism.getValue().asString().split("[\\|" + ListTag.internal_escape + "]", 2);
                if (split.length > 0 && new ElementTag(split[0]).isFloat()) {
                    if (split.length > 1 && new ElementTag(split[1]).isInt()) {
                        NMSHandler.getPacketHelper().showExperience(getPlayerEntity(),
                                new ElementTag(split[0]).asFloat(), new ElementTag(split[1]).asInt());
                    }
                    else {
                        NMSHandler.getPacketHelper().showExperience(getPlayerEntity(),
                                new ElementTag(split[0]).asFloat(), getPlayerEntity().getLevel());
                    }
                }
                else {
                    Debug.echoError("'" + split[0] + "' is not a valid decimal number!");
                }
            }
            else {
                NMSHandler.getPacketHelper().resetExperience(getPlayerEntity());
            }
        }

        // <--[mechanism]
        // @object PlayerTag
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
                String[] split = mechanism.getValue().asString().split("[\\|" + ListTag.internal_escape + "]", 3);
                if (split.length > 0 && new ElementTag(split[0]).isFloat()) {
                    if (split.length > 1 && new ElementTag(split[1]).isInt()) {
                        if (split.length > 2 && new ElementTag(split[2]).isFloat()) {
                            NMSHandler.getPacketHelper().showHealth(getPlayerEntity(), new ElementTag(split[0]).asFloat(),
                                    new ElementTag(split[1]).asInt(), new ElementTag(split[2]).asFloat());
                        }
                        else {
                            NMSHandler.getPacketHelper().showHealth(getPlayerEntity(), new ElementTag(split[0]).asFloat(),
                                    new ElementTag(split[1]).asInt(), getPlayerEntity().getSaturation());
                        }
                    }
                    else {
                        NMSHandler.getPacketHelper().showHealth(getPlayerEntity(), new ElementTag(split[0]).asFloat(),
                                getPlayerEntity().getFoodLevel(), getPlayerEntity().getSaturation());
                    }
                }
                else {
                    Debug.echoError("'" + split[0] + "' is not a valid decimal number!");
                }
            }
            else {
                NMSHandler.getPacketHelper().resetHealth(getPlayerEntity());
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name fake_equipment
        // @input EntityTag(|Element|dItem)
        // @description
        // Shows the player fake equipment on the specified living entity, which has
        // no real non-visual effects, in the form Entity|Slot|Item, where the slot
        // can be one of the following: HAND, OFF_HAND, BOOTS, LEGS, CHEST, HEAD
        // Optionally, exclude the slot and item to stop showing the fake equipment,
        // if any, on the specified entity.
        // - adjust <player> fake_equipment:e@123|chest|diamond_chestplate
        // - adjust <player> fake_equipment:<player>|head|jack_o_lantern
        // -->
        if (mechanism.matches("fake_equipment")) {
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + ListTag.internal_escape + "]", 3);
                if (split.length > 0 && new ElementTag(split[0]).matchesType(EntityTag.class)) {
                    String slot = split.length > 1 ? split[1].toUpperCase() : null;
                    if (split.length > 1 && (new ElementTag(slot).matchesEnum(EquipmentSlot.values())
                            || slot.equals("MAIN_HAND") || slot.equals("BOOTS"))) {
                        if (split.length > 2 && new ElementTag(split[2]).matchesType(ItemTag.class)) {
                            if (slot.equals("MAIN_HAND")) {
                                slot = "HAND";
                            }
                            else if (slot.equals("BOOTS")) {
                                slot = "FEET";
                            }
                            NMSHandler.getPacketHelper().showEquipment(getPlayerEntity(),
                                    new ElementTag(split[0]).asType(EntityTag.class, mechanism.context).getLivingEntity(),
                                    EquipmentSlot.valueOf(slot),
                                    new ElementTag(split[2]).asType(ItemTag.class, mechanism.context).getItemStack());
                        }
                        else if (split.length > 2) {
                            Debug.echoError("'" + split[2] + "' is not a valid ItemTag!");
                        }
                    }
                    else if (split.length > 1) {
                        Debug.echoError("'" + split[1] + "' is not a valid slot; must be HAND, OFF_HAND, BOOTS, LEGS, CHEST, or HEAD!");
                    }
                    else {
                        NMSHandler.getPacketHelper().resetEquipment(getPlayerEntity(),
                                new ElementTag(split[0]).asType(EntityTag.class, mechanism.context).getLivingEntity());
                    }
                }
                else {
                    Debug.echoError("'" + split[0] + "' is not a valid EntityTag!");
                }
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name fov_multiplier
        // @input Element(Decimal)
        // @description
        // Sets the player's field of view multiplier.
        // Leave input empty to reset.
        // Note: Values outside a (-1, 1) range will have little effect on the player's fov.
        // -->
        if (mechanism.matches("fov_multiplier")) {
            if (mechanism.hasValue() && mechanism.requireFloat()) {
                NMSHandler.getPacketHelper().setFieldOfView(getPlayerEntity(), mechanism.getValue().asFloat());
            }
            else {
                NMSHandler.getPacketHelper().setFieldOfView(getPlayerEntity(), Float.NaN);
            }
        }

        // <--[mechanism]
        // @object PlayerTag
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
        // @object PlayerTag
        // @name show_endcredits
        // @input None
        // @description
        // Shows the player the end credits.
        // -->
        if (mechanism.matches("show_endcredits")) {
            NMSHandler.getPlayerHelper().showEndCredits(getPlayerEntity());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name show_demo
        // @input None
        // @description
        // Shows the player the demo screen.
        // -->
        if (mechanism.matches("show_demo")) {
            NMSHandler.getPacketHelper().showDemoScreen(getPlayerEntity());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name spectate
        // @input EntityTag
        // @description
        // Forces the player to spectate from the entity's point of view.
        // Note: They cannot cancel the spectating without a re-log -- you
        // must make them spectate themselves to cancel the effect.
        // (i.e. - adjust <player> "spectate:<player>")
        // -->
        if (mechanism.matches("spectate") && mechanism.requireObject(EntityTag.class)) {
            NMSHandler.getPacketHelper().forceSpectate(getPlayerEntity(), mechanism.valueAsType(EntityTag.class).getBukkitEntity());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name open_book
        // @input None
        // @description
        // Forces the player to open the written book in their hand.
        // The book can safely be removed from the player's hand
        // without the player closing the book.
        // -->
        if (mechanism.matches("open_book")) {
            NMSHandler.getPacketHelper().openBook(getPlayerEntity(), EquipmentSlot.HAND);
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name open_offhand_book
        // @input None
        // @description
        // Forces the player to open the written book in their offhand.
        // The book can safely be removed from the player's offhand
        // without the player closing the book.
        // -->
        if (mechanism.matches("open_offhand_book")) {
            NMSHandler.getPacketHelper().openBook(getPlayerEntity(), EquipmentSlot.OFF_HAND);
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name show_book
        // @input ItemTag
        // @description
        // Displays a book to a player.
        // -->
        if (mechanism.matches("show_book")
                && mechanism.requireObject(ItemTag.class)) {
            ItemTag book = mechanism.valueAsType(ItemTag.class);
            if (!book.getItemStack().hasItemMeta() || !(book.getItemStack().getItemMeta() instanceof BookMeta)) {
                Debug.echoError("show_book mechanism must have a book as input.");
                return;
            }
            NMSHandler.getPacketHelper().showEquipment(getPlayerEntity(), getPlayerEntity(),
                    EquipmentSlot.OFF_HAND, book.getItemStack());
            NMSHandler.getPacketHelper().openBook(getPlayerEntity(), EquipmentSlot.OFF_HAND);
            NMSHandler.getPacketHelper().showEquipment(getPlayerEntity(), getPlayerEntity(),
                    EquipmentSlot.OFF_HAND, getPlayerEntity().getEquipment().getItemInOffHand());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name edit_sign
        // @input LocationTag
        // @description
        // Allows the player to edit an existing sign. To create a
        // sign, see <@link command Sign>.
        // -->
        if (mechanism.matches("edit_sign") && mechanism.requireObject(LocationTag.class)) {
            if (!NMSHandler.getPacketHelper().showSignEditor(getPlayerEntity(), mechanism.valueAsType(LocationTag.class))) {
                Debug.echoError("Can't edit non-sign materials!");
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name tab_list_info
        // @input Element(|Element)
        // @description
        // Show the player some text in the header and footer area
        // in their tab list.
        // - adjust <player> tab_list_info:<header>|<footer>
        // -->
        if (mechanism.matches("tab_list_info")) {
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + ListTag.internal_escape + "]", 2);
                if (split.length > 0) {
                    String header = split[0];
                    String footer = "";
                    if (split.length > 1) {
                        footer = split[1];
                    }
                    NMSHandler.getPacketHelper().showTabListHeaderFooter(getPlayerEntity(), header, footer);
                }
                else {
                    Debug.echoError("Must specify a header and footer to show!");
                }
            }
            else {
                NMSHandler.getPacketHelper().resetTabListHeaderFooter(getPlayerEntity());
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name sign_update
        // @input LocationTag|dList
        // @description
        // Shows the player fake lines on a sign.
        // -->
        if (mechanism.matches("sign_update")) {
            if (!mechanism.getValue().asString().isEmpty()) {
                String[] split = mechanism.getValue().asString().split("[\\|" + ListTag.internal_escape + "]", 2);
                if (LocationTag.matches(split[0]) && split.length > 1) {
                    ListTag lines = ListTag.valueOf(split[1]);
                    getPlayerEntity().sendSignChange(LocationTag.valueOf(split[0]), lines.toArray(4));
                }
                else {
                    Debug.echoError("Must specify a valid location and at least one sign line!");
                }
            }
            else {
                Debug.echoError("Must specify a valid location and at least one sign line!");
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name banner_update
        // @input LocationTag|Element(|dList)
        // @description
        // Shows the player a fake base color and, optionally, patterns on a banner. Input must be
        // in the form: "LOCATION|BASE_COLOR(|COLOR/PATTERN|...)"
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // For the list of possible patterns, see <@link url http://bit.ly/1MqRn7T>.
        // -->
        if (mechanism.matches("banner_update")) {
            if (mechanism.getValue().asString().length() > 0) {
                String[] split = mechanism.getValue().asString().split("[\\|" + ListTag.internal_escape + "]");
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
                            Debug.echoError("Could not apply pattern to banner: " + string);
                        }
                    }
                }
                if (LocationTag.matches(split[0]) && split.length > 1) {
                    LocationTag location = LocationTag.valueOf(split[0]);
                    DyeColor base;
                    try {
                        base = DyeColor.valueOf(split[1].toUpperCase());
                    }
                    catch (Exception e) {
                        Debug.echoError("Could not apply base color to banner: " + split[1]);
                        return;
                    }
                    NMSHandler.getPacketHelper().showBannerUpdate(getPlayerEntity(), location, base, patterns);
                }
                else {
                    Debug.echoError("Must specify a valid location and a base color!");
                }
            }
        }

        // <--[mechanism]
        // @object PlayerTag
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
                    Debug.echoError("Invalid SoundCategory. Must specify a valid name.");
                }
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name action_bar
        // @input Element
        // @description
        // Sends the player text in the action bar.
        // -->
        if (mechanism.matches("action_bar")) {
            NMSHandler.getPacketHelper().sendActionBarMessage(getPlayerEntity(), mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name update_advancements
        // @input None
        // @description
        // Updates the player's client-side advancements to match their server data.
        // -->
        if (mechanism.matches("update_advancements")) {
            NMSHandler.getAdvancementHelper().update(getPlayerEntity());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name name
        // @input Element
        // @description
        // Changes the name on this player's nameplate.
        // -->
        if (mechanism.matches("name")) {
            String name = mechanism.getValue().asString();
            if (name.length() > 16) {
                Debug.echoError("Must specify a name with no more than 16 characters.");
            }
            else {
                NMSHandler.getInstance().getProfileEditor().setPlayerName(getPlayerEntity(), mechanism.getValue().asString());
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name skin
        // @input Element
        // @description
        // Changes the skin of the player to the skin of the given
        // player name.
        // -->
        if (mechanism.matches("skin")) {
            String name = mechanism.getValue().asString();
            if (name.length() > 16) {
                Debug.echoError("Must specify a name with no more than 16 characters.");
            }
            else {
                NMSHandler.getInstance().getProfileEditor().setPlayerSkin(getPlayerEntity(), mechanism.getValue().asString());
            }
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name skin_blob
        // @input Element
        // @description
        // Changes the skin of the player to the specified blob.
        // -->
        if (mechanism.matches("skin_blob")) {
            NMSHandler.getInstance().getProfileEditor().setPlayerSkinBlob(getPlayerEntity(), mechanism.getValue().asString());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name is_whitelisted
        // @input Element(Boolean)
        // @description
        // Changes whether the player is whitelisted or not.
        // @tags
        // <PlayerTag.is_whitelisted>
        // -->
        if (mechanism.matches("is_whitelisted") && mechanism.requireBoolean()) {
            getPlayerEntity().setWhitelisted(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name is_op
        // @input Element(Boolean)
        // @description
        // Changes whether the player is a server operator or not.
        // @tags
        // <PlayerTag.is_op>
        // -->
        if (mechanism.matches("is_op") && mechanism.requireBoolean()) {
            getOfflinePlayer().setOp(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name money
        // @input Element(Number)
        // @plugin Vault
        // @description
        // Set the amount of money a player has with the linked economy system (through Vault).
        // (Only if supported by the registered Economy system.)
        // @tags
        // <PlayerTag.money>
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
            // @object PlayerTag
            // @name chat_prefix
            // @input Element
            // @plugin Vault
            // @description
            // Set the player's chat prefix.
            // Requires a Vault-compatible chat plugin.
            // @tags
            // <PlayerTag.chat_prefix>
            // -->
            if (mechanism.matches("chat_prefix")) {
                Depends.chat.setPlayerPrefix(getPlayerEntity(), mechanism.getValue().asString());
            }

            // <--[mechanism]
            // @object PlayerTag
            // @name chat_suffix
            // @input Element
            // @plugin Vault
            // @description
            // Set the player's chat suffix.
            // Requires a Vault-compatible chat plugin.
            // @tags
            // <PlayerTag.chat_suffix>
            // -->
            if (mechanism.matches("chat_suffix")) {
                Depends.chat.setPlayerSuffix(getPlayerEntity(), mechanism.getValue().asString());
            }
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);

        // Pass along to EntityTag mechanism handler if not already handled.
        if (!mechanism.fulfilled()) {
            if (isOnline()) {
                new EntityTag(getPlayerEntity()).adjust(mechanism);
            }
        }

    }
}
