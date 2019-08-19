package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.scripts.containers.core.InventoryScriptContainer;
import com.denizenscript.denizen.scripts.containers.core.InventoryScriptHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;
import java.util.regex.Matcher;

public class InventoryTag implements ObjectTag, Notable, Adjustable {

    // <--[language]
    // @name InventoryTag
    // @group Object System
    // @description
    // A InventoryTag represents an inventory, generically or attached to some in-the-world object.
    //
    // Inventories can be generically designed using inventory script containers,
    // and can be modified using the inventory command.
    //
    // For format info, see <@link language in@>
    //
    // -->

    // <--[language]
    // @name in@
    // @group Object Fetcher System
    // @description
    // in@ refers to the 'object identifier' of a InventoryTag. The 'in@' is notation for Denizen's Object
    // Fetcher. The constructor for a InventoryTag is a the classification type of inventory to use. All other data is specified through properties.
    //
    // Valid inventory type classifications:
    // "npc", "player", "crafting", "enderchest", "workbench", "entity", "location", "generic"
    //
    // For general info, see <@link language InventoryTag>
    //
    // -->

    public static InventoryTag mirrorBukkitInventory(Inventory inventory) {
        // Scripts have priority over notables
        if (InventoryScriptHelper.tempInventoryScripts.containsKey(inventory)) {
            return new InventoryTag(inventory).setIdentifiers("script",
                    InventoryScriptHelper.tempInventoryScripts.get(inventory));
        }
        // Use the map to get notable inventories
        String title = NMSHandler.getInstance().getTitle(inventory);
        if (InventoryScriptHelper.notableInventories.containsKey(title)) {
            return InventoryScriptHelper.notableInventories.get(title);
        }
        // Iterate through offline player inventories
        for (Map.Entry<UUID, PlayerInventory> inv : ImprovedOfflinePlayer.offlineInventories.entrySet()) {
            if (inv.getValue().equals(inventory)) {
                return new InventoryTag(NMSHandler.getPlayerHelper().getOfflineData(inv.getKey()));
            }
        }
        // Iterate through offline player enderchests
        for (Map.Entry<UUID, Inventory> inv : ImprovedOfflinePlayer.offlineEnderChests.entrySet()) {
            if (inv.getValue().equals(inventory)) {
                return new InventoryTag(NMSHandler.getPlayerHelper().getOfflineData(inv.getKey()), true);
            }
        }

        return new InventoryTag(inventory);
    }

    /////////////////////
    //   STATIC FIELDS
    /////////////////

    // The maximum number of slots a Bukkit inventory can have
    public final static int maxSlots = 54;

    // All of the inventory id types we use
    public final static String[] idTypes = {"npc", "player", "crafting", "enderchest", "workbench", "entity", "location", "generic"};


    /////////////////////
    //   NOTABLE METHODS
    /////////////////

    public boolean isUnique() {
        return NotableManager.isSaved(this);
    }

    @Note("Inventories")
    public String getSaveObject() {
        return "in@" + idType + PropertyParser.getPropertiesString(this);
    }

    public String notableColors = null;

    // in 1.13, we use "&1.&2.&3.", below that we can just use "&1&2&3"
    public static int inventoryNameNotableRequired = NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13) ? 9 : 6;
    public static int inventoryNameNotableMax = 32 - inventoryNameNotableRequired;

    public void makeUnique(String id) {
        String title = NMSHandler.getInstance().getTitle(inventory);
        if (title == null || title.startsWith("container.")) {
            title = inventory.getType().getDefaultTitle();
        }
        // You can only have 32 characters in an inventory title... So let's make sure we have at least 3 colors...
        // which brings notable inventory title lengths down to 26... TODO: document this/fix if possible in later version
        if (title.length() > inventoryNameNotableMax) {
            title = title.substring(0, title.charAt(inventoryNameNotableMax - 1) == '§' ? (inventoryNameNotableMax - 1) : inventoryNameNotableMax);
        }
        String colors;
        int x = 0;
        while (true) {
            x++;
            if (x > 5000) {
                Debug.echoError("Inventory note failed - too many notes already!");
                return;
            }
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                colors = Utilities.generateRandomColorsWithDots(3);
            }
            else {
                colors = Utilities.generateRandomColors(3);
            }
            if (!InventoryScriptHelper.notableInventories.containsKey(title + colors)) {
                notableColors = colors;
                ItemStack[] contents = inventory.getContents();
                if (getInventoryType() == InventoryType.CHEST) {
                    inventory = Bukkit.getServer().createInventory(null, inventory.getSize(), title + colors);
                }
                else {
                    inventory = Bukkit.getServer().createInventory(null, inventory.getType(), title + colors);
                }
                inventory.setContents(contents);
                InventoryScriptHelper.notableInventories.put(title + colors, this);
                break;
            }
        }
        idType = null;
        idHolder = null;
        loadIdentifiers();
        NotableManager.saveAs(this, id);
    }

    public void forget() {
        notableColors = null;
        NotableManager.remove(idHolder);
    }

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    @Fetchable("in")
    public static InventoryTag valueOf(String string, TagContext context) {
        if (context == null) {
            return valueOf(string, null, null);
        }
        else {
            return valueOf(string, ((BukkitTagContext) context).player, ((BukkitTagContext) context).npc, !context.debug);
        }
    }

    public static InventoryTag valueOf(String string, PlayerTag player, NPCTag npc) {
        return valueOf(string, player, npc, false);
    }

    public static InventoryTag valueOf(String string, PlayerTag player, NPCTag npc, boolean silent) {

        if (string == null) {
            return null;
        }

        ///////
        // Handle objects with properties through the object fetcher
        Matcher describedMatcher = ObjectFetcher.DESCRIBED_PATTERN.matcher(string);
        if (describedMatcher.matches()) {
            return ObjectFetcher.getObjectFrom(InventoryTag.class, string,
                    new BukkitTagContext(player, npc, false, null, false, null));
        }

        if (string.startsWith("in@")) {
            string = string.substring("in@".length());
        }

        if (ScriptRegistry.containsScript(string, InventoryScriptContainer.class)) {
            return ScriptRegistry.getScriptContainerAs(string, InventoryScriptContainer.class)
                    .getInventoryFrom(player, npc);
        }

        if (NotableManager.isSaved(string) && NotableManager.isType(string, InventoryTag.class)) {
            return (InventoryTag) NotableManager.getSavedObject(string);
        }

        for (String idType : idTypes) {
            if (string.equalsIgnoreCase(idType)) {
                return new InventoryTag(string);
            }
        }

        if (!silent) {
            Debug.echoError("Value of InventoryTag returning null. Invalid InventoryTag specified: " + string);
        }
        return null;
    }

    /**
     * Determine whether a string is a valid inventory.
     *
     * @param arg the arg string
     * @return true if matched, otherwise false
     */
    public static boolean matches(String arg) {

        if (CoreUtilities.toLowerCase(arg).startsWith("in@")) {
            return true;
        }

        String tid = arg;
        if (arg.contains("[")) {
            tid = arg.substring(0, arg.indexOf('['));
        }
        if (new ElementTag(tid).matchesEnum(InventoryType.values())) {
            return true;
        }

        if (ScriptRegistry.containsScript(tid, InventoryScriptContainer.class)) {
            return true;
        }

        if (NotableManager.isSaved(tid) && NotableManager.isType(tid, InventoryTag.class)) {
            return true;
        }

        for (String idType : idTypes) {
            if (tid.equalsIgnoreCase(idType)) {
                return true;
            }
        }

        return false;
    }


    ///////////////
    //   Constructors
    /////////////

    String idType = null;
    String idHolder = null;

    public String scriptName = null;

    public InventoryTag(Inventory inventory) {
        this.inventory = inventory;
        loadIdentifiers();
    }

    public InventoryTag(Inventory inventory, InventoryHolder holder) {
        this.inventory = inventory;
        loadIdentifiers(holder);
    }

    public InventoryTag(InventoryHolder holder) {
        inventory = holder.getInventory();
        loadIdentifiers();
    }

    public InventoryTag(ItemStack[] items) {
        inventory = Bukkit.getServer().createInventory(null, (int) Math.ceil(items.length / 9.0) * 9);
        setContents(items);
        loadIdentifiers();
    }

    public InventoryTag(ImprovedOfflinePlayer offlinePlayer) {
        this(offlinePlayer, false);
    }

    public InventoryTag(ImprovedOfflinePlayer offlinePlayer, boolean isEnderChest) {
        inventory = isEnderChest ? offlinePlayer.getEnderChest() : offlinePlayer.getInventory();
        setIdentifiers(isEnderChest ? "enderchest" : "player", "p@" + offlinePlayer.getUniqueId());
    }

    public InventoryTag(int size, String title) {
        if (size <= 0 || size % 9 != 0) {
            Debug.echoError("InventorySize must be multiple of 9, and greater than 0.");
            return;
        }
        inventory = Bukkit.getServer().createInventory(null, size, title);
        loadIdentifiers();
    }

    public InventoryTag(InventoryType type) {
        inventory = Bukkit.getServer().createInventory(null, type);
        loadIdentifiers();
    }

    public InventoryTag(int size) {
        this(size, "Chest");
    }

    public InventoryTag(String idType) {
        this.idType = CoreUtilities.toLowerCase(idType);
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Associated with Bukkit Inventory

    private Inventory inventory = null;

    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Changes the inventory to a new inventory, possibly of a different
     * type, size, and with different contents.
     * NOTE: SHOULD ONLY BE USED IN CASES WHERE THERE
     * ARE NO OTHER OPTIONS.
     *
     * @param inventory The new inventory
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
        loadIdentifiers();
    }

    public void setInventory(Inventory inventory, PlayerTag player) {
        this.inventory = inventory;
        this.idHolder = player.identify();
    }

    public void setTitle(String title) {
        if (!(getIdType().equals("generic") || getIdType().equals("script")) || title == null) {
            return;
        }
        if (inventory != null && NMSHandler.getInstance().getTitle(inventory).equals(title)) {
            return;
        }
        if (inventory == null) {
            inventory = Bukkit.getServer().createInventory(null, maxSlots, title);
            loadIdentifiers();
            return;
        }
        else if (notableColors != null) {
            title += notableColors;
            InventoryScriptHelper.notableInventories.remove(NMSHandler.getInstance().getTitle(inventory));
            InventoryScriptHelper.notableInventories.put(title, this);
        }
        ItemStack[] contents = inventory.getContents();
        if (inventory.getType() == InventoryType.CHEST) {
            inventory = Bukkit.getServer().createInventory(null, inventory.getSize(), title);
        }
        else {
            inventory = Bukkit.getServer().createInventory(null, inventory.getType(), title);
        }
        inventory.setContents(contents);
        loadIdentifiers();
    }

    public boolean containsItem(ItemTag item, int amount) {
        if (item == null) {
            return false;
        }
        item = new ItemTag(item.getItemStack().clone());
        item.setAmount(1);
        String myItem = CoreUtilities.toLowerCase(item.getFullString());
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack is = inventory.getItem(i);
            if (is == null || item.getMaterial().getMaterial() != is.getType()) {
                continue;
            }
            is = is.clone();
            int count = is.getAmount();
            is.setAmount(1);
            String newItem = CoreUtilities.toLowerCase(new ItemTag(is).getFullString());
            if (myItem.equals(newItem)) {
                if (count <= amount) {
                    amount -= count;
                    if (amount == 0) {
                        return true;
                    }
                }
                else if (count > amount) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeItem(ItemTag item, int amount) {
        if (item == null) {
            return false;
        }
        item.setAmount(1);
        String myItem = CoreUtilities.toLowerCase(item.getFullString());
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack is = inventory.getItem(i);
            if (is == null) {
                continue;
            }
            is = is.clone();
            int count = is.getAmount();
            is.setAmount(1);
            // Note: this double-parsing is intentional, as part of a hotfix for a larger issue
            String newItem = CoreUtilities.toLowerCase(ItemTag.valueOf(new ItemTag(is).getFullString(), false).getFullString());
            if (myItem.equals(newItem)) {
                if (count <= amount) {
                    inventory.setItem(i, null);
                    amount -= count;
                    if (amount == 0) {
                        return true;
                    }
                }
                else if (count > amount) {
                    is.setAmount(count - amount);
                    inventory.setItem(i, is);
                    return true;
                }
            }
        }
        return false;
    }

    public void setSize(int size) {
        if (!getIdType().equals("generic")) {
            return;
        }
        else if (size <= 0 || size % 9 != 0) {
            Debug.echoError("InventorySize must be multiple of 9, and greater than 0.");
            return;
        }
        else if (inventory == null) {
            inventory = Bukkit.getServer().createInventory(null, size, "Chest");
            loadIdentifiers();
            return;
        }
        int oldSize = inventory.getSize();
        ItemStack[] oldContents = inventory.getContents();
        ItemStack[] newContents = new ItemStack[size];
        if (oldSize > size) {
            for (int i = 0; i < size; i++) { // TODO: Why is this a manual copy?
                newContents[i] = oldContents[i];
            }
        }
        else {
            newContents = oldContents;
        }
        String title = NMSHandler.getInstance().getTitle(inventory);
        inventory = Bukkit.getServer().createInventory(null, size, (title != null ? title : inventory.getType().getDefaultTitle()));
        inventory.setContents(newContents);
        loadIdentifiers();
    }

    private void loadIdentifiers() {
        loadIdentifiers(inventory.getHolder());
    }

    private void loadIdentifiers(final InventoryHolder holder) {
        if (inventory == null) {
            return;
        }

        if (holder != null) {
            if (holder instanceof NPCTag) {
                idType = "npc";
                idHolder = ((NPCTag) holder).identify();
                return;
            }
            else if (holder instanceof Player) {
                if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC((Player) holder)) {
                    idType = "npc";
                    idHolder = (NPCTag.fromEntity((Player) holder)).identify();
                    return;
                }
                if (inventory.getType() == InventoryType.CRAFTING) {
                    idType = "crafting";
                }
                if (inventory.getType() == InventoryType.ENDER_CHEST) {
                    idType = "enderchest";
                }
                else if (inventory.getType() == InventoryType.WORKBENCH) {
                    idType = "workbench";
                }
                else {
                    idType = "player";
                }
                idHolder = new PlayerTag((Player) holder).identify();
                return;
            }
            else if (holder instanceof Entity) {
                idType = "entity";
                idHolder = new EntityTag((Entity) holder).identify();
                return;
            }
            else {
                idType = "location";
                try {
                    idHolder = getLocation(holder).identify();
                }
                catch (NullPointerException e) {
                    idHolder = "null";
                }
                return;
            }
        }
        else if (getIdType().equals("player")) {
            // Iterate through offline player inventories
            for (Map.Entry<UUID, PlayerInventory> inv : ImprovedOfflinePlayer.offlineInventories.entrySet()) { // TODO: Less weird lookup?
                if (inv.getValue().equals(inventory)) {
                    idHolder = new PlayerTag(inv.getKey()).identify();
                    return;
                }
            }
        }
        else if (getIdType().equals("enderchest")) {
            // Iterate through offline player enderchests
            for (Map.Entry<UUID, Inventory> inv : ImprovedOfflinePlayer.offlineEnderChests.entrySet()) { // TODO: Less weird lookup?
                if (inv.getValue().equals(inventory)) {
                    idHolder = new PlayerTag(inv.getKey()).identify();
                    return;
                }
            }
        }
        else if (getIdType().equals("script")) {
            if (InventoryScriptHelper.tempInventoryScripts.containsKey(inventory)) {
                idHolder = InventoryScriptHelper.tempInventoryScripts.get(inventory);
                return;
            }
        }
        idType = "generic";
        idHolder = getInventory().getType().name();
    }

    public InventoryTag setIdentifiers(String type, String holder) {
        idType = type;
        idHolder = holder;
        return this;
    }

    /**
     * Generally shouldn't be used.
     */
    public void setIdType(String type) {
        idType = type;
    }

    public String getIdType() {
        return idType == null ? "" : idType;
    }

    public String getIdHolder() {
        return idHolder == null ? "" : idHolder;
    }

    /**
     * Return the LocationTag of this inventory's
     * holder
     *
     * @return The holder's LocationTag
     */

    public LocationTag getLocation() {
        return getLocation(inventory.getHolder());
    }

    public LocationTag getLocation(InventoryHolder holder) {
        if (inventory != null && holder != null) {
            if (holder instanceof BlockState) {
                return new LocationTag(((BlockState) holder).getLocation());
            }
            else if (holder instanceof DoubleChest) {
                return new LocationTag(((DoubleChest) holder).getLocation());
            }
            else if (holder instanceof Entity) {
                return new LocationTag(((Entity) holder).getLocation());
            }
            else if (holder instanceof NPCTag) {
                NPCTag npc = (NPCTag) holder;
                if (npc.getLocation() == null) {
                    return new LocationTag(((NPCTag) holder).getCitizen().getStoredLocation());
                }
                return npc.getLocation();
            }
        }

        return null;
    }

    public ItemStack[] getContents() {
        if (inventory != null) {
            return inventory.getContents();
        }
        else {
            return new ItemStack[0];
        }
    }

    public ItemStack[] getStorageContents() {
        if (inventory != null) {
            return inventory.getStorageContents();
        }
        else {
            return new ItemStack[0];
        }
    }

    public ListTag getEquipment() {
        ItemStack[] equipment = null;
        if (inventory instanceof PlayerInventory) {
            equipment = ((PlayerInventory) inventory).getArmorContents();
        }
        else if (inventory instanceof HorseInventory) {
            equipment = new ItemStack[] {((HorseInventory) inventory).getSaddle(), ((HorseInventory) inventory).getArmor()};
        }
        if (equipment == null) {
            return null;
        }
        ListTag equipmentList = new ListTag();
        for (ItemStack item : equipment) {
            equipmentList.add(new ItemTag(item).identify());
        }
        return equipmentList;
    }

    public InventoryType getInventoryType() {
        return inventory.getType();
    }

    public int getSize() {
        return inventory.getSize();
    }

    public void remove(ItemStack item) {
        inventory.remove(item);
    }

    public void setContents(ItemStack[] contents) {
        inventory.setContents(contents);
    }

    public void setContents(ListTag list, TagContext context) {
        int size;
        if (inventory == null) {
            size = (int) Math.ceil(list.size() / 9.0) * 9;
            if (size == 0) {
                size = 9;
            }
            inventory = Bukkit.getServer().createInventory(null, size);
            loadIdentifiers();
        }
        else {
            size = inventory.getSize();
        }
        ItemStack[] contents = new ItemStack[size];
        int filled = 0;
        for (ItemTag item : list.filter(ItemTag.class, context)) {
            contents[filled] = item.getItemStack();
            filled++;
        }
        final ItemStack air = new ItemStack(Material.AIR);
        while (filled < size) {
            contents[filled] = air;
            filled++;
        }
        inventory.setContents(contents);
        if (Depends.citizens != null && NPCTag.matches(idHolder)) { // TODO: Directly store holder
            NPCTag.valueOf(idHolder).getInventoryTrait().setContents(contents);
        }
    }

    public boolean update() {
        if (getIdType().equals("player")) {
            PlayerTag.valueOf(idHolder).getPlayerEntity().updateInventory();
            return true;
        }
        return false;
    }

    public int firstPartial(int startSlot, ItemStack item) {
        ItemStack[] inventory = getContents();
        if (item == null) {
            return -1;
        }
        for (int i = startSlot; i < inventory.length; i++) {
            ItemStack item1 = inventory[i];
            if (item1 != null && item1.getAmount() < item.getMaxStackSize() && item1.isSimilar(item)) {
                return i;
            }
        }
        return -1;
    }

    public int firstEmpty(int startSlot) {
        ItemStack[] inventory = getStorageContents();
        for (int i = startSlot; i < inventory.length; i++) {
            if (inventory[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /////////////////////
    //   INVENTORY MANIPULATION
    /////////////////

    // Somewhat simplified version of CraftBukkit's current code
    public InventoryTag add(int slot, ItemStack... items) {
        if (inventory == null || items == null) {
            return this;
        }

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) {
                continue;
            }
            int amount = item.getAmount();
            int max = item.getMaxStackSize();
            while (true) {
                // Do we already have a stack of it?
                int firstPartial = firstPartial(slot, item);

                // Drat! no partial stack
                if (firstPartial == -1) {
                    // Find a free spot!
                    int firstFree = firstEmpty(slot);

                    if (firstFree == -1) {
                        // No space at all!
                        break;
                    }
                    else {
                        // More than a single stack!
                        if (amount > max) {
                            ItemStack clone = item.clone();
                            clone.setAmount(max);
                            inventory.setItem(firstFree, clone);
                            item.setAmount(amount -= max);
                        }
                        else {
                            // Just store it
                            inventory.setItem(firstFree, item);
                            break;
                        }
                    }
                }
                else {
                    // So, apparently it might only partially fit, well lets do just that
                    ItemStack partialItem = inventory.getItem(firstPartial);

                    int partialAmount = partialItem.getAmount();
                    int total = amount + partialAmount;

                    // Check if it fully fits
                    if (total <= max) {
                        partialItem.setAmount(total);
                        break;
                    }

                    // It fits partially
                    partialItem.setAmount(max);
                    item.setAmount(amount = total - max);
                }
            }
        }

        return this;
    }

    public List<ItemStack> addWithLeftovers(int slot, boolean keepMaxStackSize, ItemStack... items) {
        if (inventory == null || items == null) {
            return null;
        }

        List<ItemStack> leftovers = new ArrayList<>();

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) {
                continue;
            }
            int amount = item.getAmount();
            int max;
            if (keepMaxStackSize) {
                max = item.getMaxStackSize();
            }
            else {
                max = 64;
            }
            while (true) {
                // Do we already have a stack of it?
                int firstPartial = firstPartial(slot, item);

                // Drat! no partial stack
                if (firstPartial == -1) {
                    // Find a free spot!
                    int firstFree = firstEmpty(slot);

                    if (firstFree == -1) {
                        // No space at all!
                        leftovers.add(item);
                        break;
                    }
                    else {
                        // More than a single stack!
                        if (amount > max) {
                            ItemStack clone = item.clone();
                            clone.setAmount(max);
                            inventory.setItem(firstFree, clone);
                            item.setAmount(amount -= max);
                        }
                        else {
                            // Just store it
                            inventory.setItem(firstFree, item);
                            break;
                        }
                    }
                }
                else {
                    // So, apparently it might only partially fit, well lets do just that
                    ItemStack partialItem = inventory.getItem(firstPartial);

                    int partialAmount = partialItem.getAmount();
                    int total = amount + partialAmount;

                    // Check if it fully fits
                    if (total <= max) {
                        partialItem.setAmount(total);
                        break;
                    }

                    // It fits partially
                    partialItem.setAmount(max);
                    item.setAmount(amount = total - max);
                }
            }
        }

        return leftovers;
    }

    public List<ItemStack> setWithLeftovers(int slot, ItemStack... items) {
        if (inventory == null || items == null) {
            return null;
        }

        List<ItemStack> leftovers = new ArrayList<>();

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            try {
                inventory.setItem(i + slot, item);
            }
            catch (Exception e) {
                leftovers.add(i + slot, item);
            }
        }

        return leftovers;
    }

    public int countByMaterial(Material material) {
        if (inventory == null) {
            return 0;
        }
        int qty = 0;
        for (ItemStack invStack : inventory) {
            if (invStack != null) {
                if (invStack.getType() == material) {
                    qty += invStack.getAmount();
                }
            }
        }
        return qty;
    }

    public int countByScriptName(String scriptName) {
        if (inventory == null) {
            return 0;
        }
        int qty = 0;
        for (ItemStack invStack : inventory) {
            if (invStack != null) {
                ItemTag item = new ItemTag(invStack);
                if (item.isItemscript() && item.getScriptName().equalsIgnoreCase(scriptName)) {
                    qty += invStack.getAmount();
                }
            }
        }
        return qty;
    }

    /**
     * Count the number or quantities of stacks that
     * match an item in an inventory.
     *
     * @param item   The item (can be null)
     * @param stacks Whether stacks should be counted
     *               instead of item quantities
     * @return The number of stacks or quantity of items
     */
    public int count(ItemStack item, boolean stacks) {
        if (inventory == null) {
            return 0;
        }

        int qty = 0;

        for (ItemStack invStack : inventory) {
            // If ItemStacks are empty here, they are null
            if (invStack != null) {
                // If item is null, include all items in the inventory
                if (item == null || invStack.isSimilar(item)) {
                    // If stacks is true, only count the number of stacks
                    // Otherwise, count the quantities of stacks
                    qty += (stacks ? 1 : invStack.getAmount());
                }
            }
        }

        return qty;
    }

    /**
     * Keep only the items from a certain array
     * in this inventory, removing all others
     *
     * @param items The array of items
     * @return The resulting InventoryTag
     */

    public InventoryTag keep(ItemStack[] items) {

        if (inventory == null || items == null) {
            return this;
        }

        for (ItemStack invStack : inventory) {

            if (invStack != null) {

                boolean keep = false;

                // See if the item array contains
                // this inventory item
                for (ItemStack item : items) {

                    if (invStack.isSimilar(item)) {

                        keep = true;
                        break;
                    }
                }

                // If the item array did not contain
                // this inventory item, remove it
                // from the inventory
                if (!keep) {

                    this.remove(invStack);
                }
            }
        }

        return this;
    }

    /**
     * Exclude an array of items from this
     * inventory by removing them over and over
     * until they are completely gone
     *
     * @param items The array of items
     * @return The resulting InventoryTag
     */

    public InventoryTag exclude(ItemStack[] items) {

        if (inventory == null || items == null) {
            return this;
        }

        int oldCount = this.count(null, false);
        int newCount = -1;

        while (oldCount != newCount) {

            oldCount = newCount;
            newCount = this.remove(items).count(null, false);
        }

        return this;
    }

    /**
     * Fill an inventory with an array of items by
     * continuing to add the items to it over and
     * over until there is no more room
     *
     * @param items The array of items
     * @return The resulting InventoryTag
     */

    public InventoryTag fill(ItemStack[] items) {

        if (inventory == null || items == null) {
            return this;
        }

        int oldCount = this.count(null, false);
        int newCount = -1;

        while (oldCount != newCount) {

            oldCount = newCount;
            newCount = this.add(0, items).count(null, false);
        }

        return this;
    }

    /**
     * Remove an array of items from this inventory,
     * and return the result
     *
     * @param items The array of items
     * @return The resulting InventoryTag
     */

    public InventoryTag remove(ItemStack[] items) {

        if (inventory == null || items == null) {
            return this;
        }

        for (ItemStack item : items) {
            if (item != null) {
                inventory.removeItem(item.clone());
            }
        }

        return this;
    }

    /**
     * Remove a book from this inventory, comparing
     * only its title and author with books in the
     * inventory, but ignoring its text, thus having
     * Denizen support for updatable quest journals
     * and their like
     *
     * @param title    The title of the book
     * @param author   The author of the book
     * @param quantity The number of books to remove
     * @return The resulting InventoryTag
     */

    public InventoryTag removeBook(String title, String author, int quantity) {

        if (inventory == null || (title == null && author == null)) {
            return this;
        }

        for (ItemStack invStack : inventory) {

            if (quantity == 0) {
                break;
            }

            if (invStack != null && invStack.getItemMeta() instanceof BookMeta) {

                BookMeta invMeta = (BookMeta) invStack.getItemMeta();

                String invTitle = invMeta.getTitle();
                String invAuthor = invMeta.getAuthor();

                if ((invTitle == null && title != null) || (invAuthor == null && author != null)) {
                    continue;
                }
                else if (invTitle == null || invAuthor == null) {
                    continue;
                }

                if (equalOrNull(invAuthor, author) && equalOrNull(invTitle, title)) {
                    // Make sure we don't remove more books than we need to
                    // TODO: WTF is this logic???
                    if (quantity - invStack.getAmount() < 0) {
                        invStack.setAmount((quantity - invStack.getAmount()) * -1);
                    }
                    else {
                        inventory.removeItem(invStack);
                        // Update the quantity we still have to remove
                        quantity -= invStack.getAmount();
                    }
                }
            }
        }

        return this;
    }

    private static boolean equalOrNull(String a, String b) {
        return b == null || a == null || a.equalsIgnoreCase(b);
    }

    /**
     * Replace another inventory with this one,
     * cropping it if necessary so that it fits.
     *
     * @param destination The destination inventory
     */

    public void replace(InventoryTag destination) {

        if (inventory == null || destination == null) {
            return;
        }

        // If the destination is smaller than our current inventory,
        // add as many items as possible

        if (destination.getSize() < this.getSize()) {

            destination.clear();
            destination.add(0, this.getContents());
        }
        else {

            destination.setContents(this.getContents());
        }
    }

    public InventoryTag setSlots(int slot, ItemStack... items) {
        return setSlots(slot, items, items.length);
    }

    /**
     * Set items in an inventory, starting with a specified slot
     *
     * @param slot  The slot to start from
     * @param items The items to add
     * @return The resulting InventoryTag
     */
    public InventoryTag setSlots(int slot, ItemStack[] items, int c) {

        if (inventory == null || items == null) {
            return this;
        }

        for (int i = 0; i < c; i++) {
            if (i >= items.length || items[i] == null) {
                inventory.setItem(slot + i, new ItemStack(Material.AIR));
            }
            ItemStack item = items[i];
            if (slot + i < 0 || slot + i >= inventory.getSize()) {
                break;
            }
            inventory.setItem(slot + i, item);
        }
        if (Depends.citizens != null && NPCTag.matches(idHolder)) { // TODO: Directly store holder
            NPCTag.valueOf(idHolder).getInventoryTrait().setContents(inventory.getContents());
        }
        return this;

    }

    public void clear() {
        if (inventory != null) {
            inventory.clear();
        }
    }

    ////////////////////////
    //  ObjectTag Methods
    /////////////////////

    private String prefix = getObjectType();

    @Override
    public String getObjectType() {
        return "Inventory";
    }


    @Override
    public String getPrefix() {
        return prefix;
    }


    @Override
    public InventoryTag setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String identify() {
        if (isUnique()) {
            return "in@" + NotableManager.getSavedId(this);
        }
        else {
            return "in@" + (getIdType().equals("script") ? idHolder
                    : (idType + PropertyParser.getPropertiesString(this)));
        }
    }


    public String bestName() {
        if (isUnique()) {
            return NotableManager.getSavedId(this);
        }
        else {
            return (getIdType().equals("script") || getIdType().equals("notable")
                    ? idHolder : (idType));
        }
    }


    @Override
    public String identifySimple() {
        if (isUnique()) {
            return "in@" + NotableManager.getSavedId(this);
        }
        else {
            return "in@" + (getIdType().equals("script") || getIdType().equals("notable")
                    ? idHolder : (idType + "[" + idHolder + "]"));
        }
    }


    @Override
    public String toString() {
        return identify();
    }

    ////////////////////////
    //  Attributes
    /////////////////////


    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <InventoryTag.empty_slots>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of empty slots in an inventory.
        // -->
        if (attribute.startsWith("empty_slots")) {
            InventoryTag dummyInv;
            if (inventory.getType() == InventoryType.PLAYER) {
                dummyInv = new InventoryTag(Bukkit.createInventory(null, InventoryType.CHEST));
                ItemStack[] contents = getStorageContents();
                dummyInv.setSize(contents.length);
                if (contents.length != dummyInv.getSize()) {
                    contents = Arrays.copyOf(contents, dummyInv.getSize());
                }
                dummyInv.setContents(contents);
            }
            else {
                dummyInv = new InventoryTag(inventory);
            }
            int full = dummyInv.count(null, true);
            return new ElementTag(dummyInv.getSize() - full).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.can_fit[<item>|...]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the inventory can fit an item.
        // -->
        if (attribute.startsWith("can_fit") && attribute.hasContext(1)) {
            List<ItemTag> items = ListTag.valueOf(attribute.getContext(1)).filter(ItemTag.class, attribute.getScriptEntry());
            if (items == null || items.isEmpty()) {
                return null;
            }
            int attribs = 1;

            InventoryType type = inventory.getType();
            InventoryTag dummyInv = new InventoryTag(Bukkit.createInventory(null, type == InventoryType.PLAYER ? InventoryType.CHEST : type, NMSHandler.getInstance().getTitle(inventory)));
            ItemStack[] contents = getStorageContents();
            if (dummyInv.getInventoryType() == InventoryType.CHEST) {
                dummyInv.setSize(contents.length);
            }
            if (contents.length != dummyInv.getSize()) {
                contents = Arrays.copyOf(contents, dummyInv.getSize());
            }
            dummyInv.setContents(contents);

            // <--[tag]
            // @attribute <InventoryTag.can_fit[<item>].count>
            // @returns ElementTag(Number)
            // @description
            // Returns the total count of how many times an item can fit into an inventory.
            // -->
            if (attribute.getAttribute(2).startsWith("count")) {
                attribs = 2;
                ItemStack toAdd = items.get(0).getItemStack().clone();
                int totalCount = 64 * 64 * 4; // Technically nothing stops us from ridiculous numbers in an ItemStack amount.
                toAdd.setAmount(totalCount);
                List<ItemStack> leftovers = dummyInv.addWithLeftovers(0, true, toAdd);
                int result = 0;
                if (leftovers.size() > 0) {
                    result += leftovers.get(0).getAmount();
                }
                return new ElementTag(totalCount - result).getAttribute(attribute.fulfill(attribs));
            }

            // <--[tag]
            // @attribute <InventoryTag.can_fit[<item>].quantity[<#>]>
            // @returns ElementTag(Boolean)
            // @description
            // Returns whether the inventory can fit a certain quantity of an item.
            // -->
            if ((attribute.getAttribute(2).startsWith("quantity") || attribute.getAttribute(2).startsWith("qty")) &&
                    attribute.hasContext(2) &&
                    ArgumentHelper.matchesInteger(attribute.getContext(2))) {
                int qty = attribute.getIntContext(2);
                attribs = 2;
                items.get(0).setAmount(qty);
            }

            // NOTE: Could just also convert items to an array and pass it all in at once...
            for (ItemTag itm : items) {
                List<ItemStack> leftovers = dummyInv.addWithLeftovers(0, true, itm.getItemStack());
                if (!leftovers.isEmpty()) {
                    return new ElementTag(false).getAttribute(attribute.fulfill(attribs));
                }
            }
            return new ElementTag(true).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <InventoryTag.include[<item>]>
        // @returns InventoryTag
        // @description
        // Returns the InventoryTag with an item added.
        // -->
        if (attribute.startsWith("include") && attribute.hasContext(1)
                && ItemTag.matches(attribute.getContext(1))) {
            ItemTag item = ItemTag.valueOf(attribute.getContext(1), attribute.context);
            if (item == null) {
                return null;
            }
            int attribs = 1;
            int qty = 1;

            InventoryTag dummyInv = new InventoryTag(Bukkit.createInventory(null, inventory.getType(), NMSHandler.getInstance().getTitle(inventory)));
            if (inventory.getType() == InventoryType.CHEST) {
                dummyInv.setSize(inventory.getSize());
            }
            dummyInv.setContents(getContents());

            // <--[tag]
            // @attribute <InventoryTag.include[<item>].quantity[<#>]>
            // @returns InventoryTag
            // @description
            // Returns the InventoryTag with a certain quantity of an item added.
            // -->
            if ((attribute.getAttribute(2).startsWith("quantity") || attribute.getAttribute(2).startsWith("qty")) &&
                    attribute.hasContext(2) && ArgumentHelper.matchesInteger(attribute.getContext(2))) {
                qty = attribute.getIntContext(2);
                attribs = 2;
            }
            item.setAmount(qty);
            dummyInv.add(0, item.getItemStack());
            return dummyInv.getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <InventoryTag.is_empty>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the inventory is empty.
        // -->
        if (attribute.startsWith("is_empty")) {
            boolean empty = true;
            for (ItemStack item : getStorageContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    empty = false;
                    break;
                }
            }
            return new ElementTag(empty).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.is_full>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the inventory is completely full.
        // -->
        if (attribute.startsWith("is_full")) {
            boolean full = true;

            for (ItemStack item : getStorageContents()) {
                if ((item == null) ||
                        (item.getType() == Material.AIR) ||
                        (item.getAmount() < item.getMaxStackSize())) {
                    full = false;
                    break;
                }
            }
            return new ElementTag(full).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.contains.display[(strict:)<element>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the inventory contains an item with the specified display
        // name. Use 'strict:' in front of the search element to ensure the display
        // name is EXACTLY the search element, otherwise the searching will only
        // check if the search element is contained in the display name.
        // -->
        if (attribute.startsWith("contains.display") && attribute.hasContext(2)) {
            String search_string = attribute.getContext(2);
            boolean strict = false;
            if (CoreUtilities.toLowerCase(search_string).startsWith("strict:") && search_string.length() > 7) {
                strict = true;
                search_string = search_string.substring(7);
            }
            if (search_string.length() == 0) {
                return null;
            }
            int qty = 1;
            int attribs = 2;

            // <--[tag]
            // @attribute <InventoryTag.contains.display[(strict:)<element>].quantity[<#>]>
            // @returns ElementTag(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of an item with the
            // specified display name. Use 'strict:' in front of the search element to ensure
            // the display name is EXACTLY the search element, otherwise the searching will only
            // check if the search element is contained in the display name.
            // -->
            if ((attribute.getAttribute(3).startsWith("quantity") || attribute.getAttribute(3).startsWith("qty")) &&
                    attribute.hasContext(3) &&
                    ArgumentHelper.matchesInteger(attribute.getContext(3))) {
                qty = attribute.getIntContext(3);
                attribs = 3;
            }

            int found_items = 0;

            if (strict) {
                for (ItemStack item : getContents()) {
                    if (item != null && item.getType() == Material.WRITTEN_BOOK
                            && ((BookMeta) item.getItemMeta()).getTitle().equalsIgnoreCase(search_string)) {
                        found_items += item.getAmount();
                        if (found_items >= qty) {
                            break;
                        }
                    }
                    else if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                            item.getItemMeta().getDisplayName().equalsIgnoreCase(search_string)) {
                        found_items += item.getAmount();
                        if (found_items >= qty) {
                            break;
                        }
                    }
                }
            }
            else {
                for (ItemStack item : getContents()) {
                    if (item != null && item.getType() == Material.WRITTEN_BOOK
                            && CoreUtilities.toLowerCase(((BookMeta) item.getItemMeta()).getTitle())
                            .contains(CoreUtilities.toLowerCase(search_string))) {
                        found_items += item.getAmount();
                        if (found_items >= qty) {
                            break;
                        }
                    }
                    else if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                            CoreUtilities.toLowerCase(item.getItemMeta().getDisplayName())
                                    .contains(CoreUtilities.toLowerCase(search_string))) {
                        found_items += item.getAmount();
                        if (found_items >= qty) {
                            break;
                        }
                    }
                }
            }

            return new ElementTag(found_items >= qty).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <InventoryTag.contains.lore[(strict:)<element>|...]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the inventory contains an item with the specified lore.
        // Use 'strict:' in front of the search elements to ensure all lore lines
        // are EXACTLY the search elements, otherwise the searching will only
        // check if the search elements are contained in the lore.
        // -->
        if (attribute.startsWith("contains.lore") && attribute.hasContext(2)) {
            String search_string = attribute.getContext(2);
            boolean strict = false;
            if (CoreUtilities.toLowerCase(search_string).startsWith("strict:")) {
                strict = true;
                search_string = search_string.substring(7);
            }
            if (search_string.length() == 0) {
                return null;
            }
            ListTag lore = ListTag.valueOf(search_string);
            int qty = 1;
            int attribs = 2;

            // <--[tag]
            // @attribute <InventoryTag.contains.lore[(strict:)<element>|...].quantity[<#>]>
            // @returns ElementTag(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of an item
            // with the specified lore. Use 'strict:' in front of the search elements
            // to ensure all lore lines are EXACTLY the search elements, otherwise the
            // searching will only check if the search elements are contained in the lore.
            // -->
            if ((attribute.getAttribute(3).startsWith("quantity") || attribute.getAttribute(3).startsWith("qty")) &&
                    attribute.hasContext(3) &&
                    ArgumentHelper.matchesInteger(attribute.getContext(3))) {
                qty = attribute.getIntContext(3);
                attribs = 3;
            }

            int found_items = 0;

            if (strict) {
                strict_items:
                for (ItemStack item : getContents()) {
                    if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                        List<String> item_lore = item.getItemMeta().getLore();
                        if (lore.size() != item_lore.size()) {
                            continue;
                        }
                        for (int i = 0; i < item_lore.size(); i++) {
                            if (lore.get(i).equalsIgnoreCase(item_lore.get(i))) {
                                if (i == lore.size()) {
                                    found_items += item.getAmount();
                                    if (found_items >= qty) {
                                        break strict_items;
                                    }
                                }
                            }
                            else {
                                continue strict_items;
                            }
                        }
                    }
                }
            }
            else {
                for (ItemStack item : getContents()) {
                    if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                        List<String> item_lore = item.getItemMeta().getLore();
                        int loreCount = 0;
                        lines:
                        for (String line : lore) {
                            for (String item_line : item_lore) {
                                if (CoreUtilities.toLowerCase(item_line).contains(CoreUtilities.toLowerCase(line))) {
                                    loreCount++;
                                    continue lines;
                                }
                            }
                        }
                        if (loreCount == lore.size()) {
                            found_items += item.getAmount();
                            if (found_items >= qty) {
                                break;
                            }
                        }
                    }
                }
            }

            return new ElementTag(found_items >= qty).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <InventoryTag.contains.scriptname[<scriptname>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the inventory contains an item with the specified scriptname.
        // -->
        if (attribute.startsWith("contains.scriptname") && attribute.hasContext(2)) {
            String scrName = attribute.getContext(2);
            int qty = 1;
            int attribs = 2;

            // <--[tag]
            // @attribute <InventoryTag.contains.scriptname[<scriptname>].quantity[<#>]>
            // @returns ElementTag(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of an item with the specified scriptname.
            // -->
            if ((attribute.getAttribute(3).startsWith("quantity") || attribute.getAttribute(3).startsWith("qty")) &&
                    attribute.hasContext(3) &&
                    ArgumentHelper.matchesInteger(attribute.getContext(3))) {
                qty = attribute.getIntContext(3);
                attribs = 3;
            }

            int found_items = 0;

            for (ItemStack item : getContents()) {
                if (item != null && scrName.equalsIgnoreCase(new ItemTag(item).getScriptName())) {
                    found_items += item.getAmount();
                    if (found_items >= qty) {
                        break;
                    }
                }
            }

            return new ElementTag(found_items >= qty).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <InventoryTag.contains.nbt[<key>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the inventory contains an item with the specified key.
        // -->
        if (attribute.startsWith("contains.nbt") && attribute.hasContext(2)) {
            String keyName = attribute.getContext(2);
            int qty = 1;
            int attribs = 2;

            // <--[tag]
            // @attribute <InventoryTag.contains.nbt[<key>].quantity[<#>]>
            // @returns ElementTag(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of an item with the specified key.
            // -->
            if ((attribute.getAttribute(3).startsWith("quantity") || attribute.getAttribute(3).startsWith("qty")) &&
                    attribute.hasContext(3) &&
                    ArgumentHelper.matchesInteger(attribute.getContext(3))) {
                qty = attribute.getIntContext(3);
                attribs = 3;
            }

            int found_items = 0;

            for (ItemStack item : getContents()) {
                if (CustomNBT.hasCustomNBT(item, keyName, CustomNBT.KEY_DENIZEN)) {
                    found_items += item.getAmount();
                    if (found_items >= qty) {
                        break;
                    }
                }
            }

            return new ElementTag(found_items >= qty).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <InventoryTag.contains.material[<material>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the inventory contains an item with the specified material.
        // -->
        if (attribute.startsWith("contains.material") && attribute.hasContext(2) &&
                MaterialTag.matches(attribute.getContext(2))) {
            MaterialTag material = MaterialTag.valueOf(attribute.getContext(2));
            int qty = 1;
            int attribs = 2;

            // <--[tag]
            // @attribute <InventoryTag.contains.material[<material>].quantity[<#>]>
            // @returns ElementTag(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of an item with the
            // specified material.
            // -->
            if ((attribute.getAttribute(3).startsWith("quantity") || attribute.getAttribute(3).startsWith("qty")) &&
                    attribute.hasContext(3) &&
                    ArgumentHelper.matchesInteger(attribute.getContext(3))) {
                qty = attribute.getIntContext(3);
                attribs = 3;
            }

            int found_items = 0;

            for (ItemStack item : getContents()) {
                if (item != null && item.getType() == material.getMaterial()) {
                    found_items += item.getAmount();
                    if (found_items >= qty) {
                        break;
                    }
                }
            }

            return new ElementTag(found_items >= qty).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <InventoryTag.contains_any[<item>|...]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the inventory contains any of the specified items.
        // -->
        if (attribute.startsWith("contains_any") && attribute.hasContext(1)) {
            ListTag list = ListTag.valueOf(attribute.getContext(1));
            if (list.isEmpty()) {
                return null;
            }
            int qty = 1;
            int attribs = 1;

            // <--[tag]
            // @attribute <InventoryTag.contains_any[<item>|...].quantity[<#>]>
            // @returns ElementTag(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of any of the specified items.
            // -->
            if ((attribute.getAttribute(2).startsWith("quantity") || attribute.getAttribute(2).startsWith("qty"))
                    && attribute.hasContext(2) && ArgumentHelper.matchesInteger(attribute.getContext(2))) {
                qty = attribute.getIntContext(2);
                attribs = 2;
            }
            List<ItemTag> contains = list.filter(ItemTag.class, attribute.getScriptEntry());
            if (!contains.isEmpty()) {
                for (ItemTag item : contains) {
                    if (containsItem(item, qty)) {
                        return new ElementTag(true).getAttribute(attribute.fulfill(attribs));
                    }
                }
            }
            return new ElementTag(false).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <InventoryTag.contains[<item>|...]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the inventory contains all of the specified items.
        // -->
        if (attribute.startsWith("contains") && attribute.hasContext(1)) {
            ListTag list = ListTag.valueOf(attribute.getContext(1));
            if (list.isEmpty()) {
                return null;
            }
            int qty = 1;
            int attribs = 1;

            // <--[tag]
            // @attribute <InventoryTag.contains[<item>|...].quantity[<#>]>
            // @returns ElementTag(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of all of the specified items.
            // -->
            if ((attribute.getAttribute(2).startsWith("quantity") || attribute.getAttribute(2).startsWith("qty"))
                    && attribute.hasContext(2) && ArgumentHelper.matchesInteger(attribute.getContext(2))) {
                qty = attribute.getIntContext(2);
                attribs = 2;
            }
            List<ItemTag> contains = list.filter(ItemTag.class, attribute.getScriptEntry());
            if (contains.size() == list.size()) {
                for (ItemTag item : contains) {
                    if (!containsItem(item, qty)) {
                        return new ElementTag(false).getAttribute(attribute.fulfill(attribs));
                    }
                }
                return new ElementTag(true).getAttribute(attribute.fulfill(attribs));
            }
            return new ElementTag(false).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <InventoryTag.first_empty>
        // @returns ElementTag(Number)
        // @description
        // Returns the location of the first empty slot.
        // Returns -1 if the inventory is full.
        // -->
        if (attribute.startsWith("first_empty")) {
            int val = firstEmpty(0);
            return new ElementTag(val >= 0 ? (val + 1) : -1).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.find.material[<material>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the location of the first slot that contains the material.
        // Returns -1 if there's no match.
        // -->
        if (attribute.startsWith("find.material")
                && attribute.hasContext(2)
                && MaterialTag.matches(attribute.getContext(2))) {
            MaterialTag material = MaterialTag.valueOf(attribute.getContext(2));
            if (material == null) {
                return null;
            }
            int slot = -1;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) != null && inventory.getItem(i).getType() == material.getMaterial()) {
                    slot = i + 1;
                    break;
                }
            }
            return new ElementTag(slot).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <InventoryTag.find.scriptname[<item>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the location of the first slot that contains the item
        // with the specified script name.
        // Returns -1 if there's no match.
        // -->
        if (attribute.startsWith("find.scriptname")
                && attribute.hasContext(2)
                && ItemTag.matches(attribute.getContext(2))) {
            String scrname = ItemTag.valueOf(attribute.getContext(2), attribute.context).getScriptName();
            if (scrname == null) {
                return null;
            }
            int slot = -1;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) != null
                        && scrname.equalsIgnoreCase(new ItemTag(inventory.getItem(i)).getScriptName())) {
                    slot = i + 1;
                    break;
                }
            }
            return new ElementTag(slot).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <InventoryTag.find_imperfect[<item>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the location of the first slot that contains the item.
        // Returns -1 if there's no match.
        // Will match item script to item script, even if one is edited.
        // -->
        if (attribute.startsWith("find_imperfect")
                && attribute.hasContext(1)
                && ItemTag.matches(attribute.getContext(1))) {
            ItemTag item = ItemTag.valueOf(attribute.getContext(1), attribute.context);
            item.setAmount(1);
            int slot = -1;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) != null) {
                    ItemTag compare_to = new ItemTag(inventory.getItem(i).clone());
                    compare_to.setAmount(1);
                    if (item.identify().equalsIgnoreCase(compare_to.identify())
                            || item.getScriptName().equalsIgnoreCase(compare_to.getScriptName())) {
                        slot = i + 1;
                        break;
                    }
                }
            }
            return new ElementTag(slot).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.find[<item>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the location of the first slot that contains the item.
        // Returns -1 if there's no match.
        // -->
        if (attribute.startsWith("find")
                && attribute.hasContext(1)
                && ItemTag.matches(attribute.getContext(1))) {
            ItemTag item = ItemTag.valueOf(attribute.getContext(1), attribute.context);
            item.setAmount(1);
            int slot = -1;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) != null) {
                    ItemTag compare_to = new ItemTag(inventory.getItem(i).clone());
                    compare_to.setAmount(1);
                    if (item.getFullString().equalsIgnoreCase(compare_to.getFullString())) {
                        slot = i + 1;
                        break;
                    }
                }
            }
            return new ElementTag(slot).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.id_type>
        // @returns ElementTag
        // @description
        // Returns Denizen's type ID for this inventory. (player, location, etc.)
        // -->
        if (attribute.startsWith("id_type")) {
            return new ElementTag(idType).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.notable_name>
        // @returns ElementTag
        // @description
        // Gets the name of a Notable InventoryTag. If the inventory isn't noted,
        // this is null.
        // -->
        if (attribute.startsWith("notable_name")) {
            String notname = NotableManager.getSavedId(this);
            if (notname == null) {
                return null;
            }
            return new ElementTag(notname).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.location>
        // @returns LocationTag
        // @description
        // Returns the location of this inventory's holder.
        // -->
        if (attribute.startsWith("location")) {
            LocationTag location = getLocation();
            if (location == null) {
                return null;
            }
            return location.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.quantity.scriptname[<script>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the combined quantity of itemstacks that have the specified script name.
        // -->
        if (attribute.startsWith("quantity.scriptname") && attribute.hasContext(2)) {
            return new ElementTag(countByScriptName(attribute.getContext(2)))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <InventoryTag.quantity.material[<material>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the combined quantity of itemstacks that have the specified material.
        // -->
        if (attribute.startsWith("quantity.material")
                && attribute.hasContext(2) && MaterialTag.matches(attribute.getContext(2))) {
            return new ElementTag(countByMaterial(MaterialTag.valueOf(attribute.getContext(2)).getMaterial()))
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <InventoryTag.quantity[<item>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the combined quantity of itemstacks that match an item if
        // one is specified, or the combined quantity of all itemstacks
        // if one is not.
        // -->
        if (attribute.startsWith("quantity") || attribute.startsWith("qty")) {
            if (attribute.hasContext(1) && ItemTag.matches(attribute.getContext(1))) {
                return new ElementTag(count // TODO: Handle no-script-entry cases
                        (ItemTag.valueOf(attribute.getContext(1), attribute.context).getItemStack(), false))
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return new ElementTag(count(null, false))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <InventoryTag.stacks[<item>]>
        // @returns ElementTag(Number)
        // @description
        // Returns the number of itemstacks that match an item if one is
        // specified, or the number of all itemstacks if one is not.
        // -->
        if (attribute.startsWith("stacks")) {
            if (attribute.hasContext(1) && ItemTag.matches(attribute.getContext(1))) {
                return new ElementTag(count // TODO: Handle no-script-entry cases
                        (ItemTag.valueOf(attribute.getContext(1), attribute.context).getItemStack(), true))
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return new ElementTag(count(null, true))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <InventoryTag.slot[<#>]>
        // @returns ItemTag
        // @description
        // Returns the item in the specified slot.
        // -->
        if (attribute.startsWith("slot")
                && attribute.hasContext(1)
                && ArgumentHelper.matchesInteger(attribute.getContext(1))) {
            int slot = new ElementTag(attribute.getContext(1)).asInt() - 1;
            if (slot < 0) {
                slot = 0;
            }
            else if (slot > getInventory().getSize() - 1) {
                slot = getInventory().getSize() - 1;
            }
            return new ItemTag(getInventory().getItem(slot))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.inventory_type>
        // @returns ElementTag
        // @description
        // Returns the type of the inventory (e.g. "PLAYER", "CRAFTING", "HORSE").
        // -->
        if (attribute.startsWith("inventory_type")) {
            return new ElementTag(inventory instanceof HorseInventory ? "HORSE" : getInventory().getType().name())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.equipment>
        // @returns ListTag(ItemTag)
        // @description
        // Returns the equipment of an inventory.
        // -->
        if (attribute.startsWith("equipment")) {
            ListTag equipment = getEquipment();
            if (equipment == null) {
                return null;
            }
            return equipment.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.matrix>
        // @returns ListTag(ItemTag)
        // @description
        // Returns the ItemTags currently in a crafting inventory's matrix.
        // -->
        if (attribute.startsWith("matrix") && inventory instanceof CraftingInventory) {
            ListTag recipeList = new ListTag();
            for (ItemStack item : ((CraftingInventory) inventory).getMatrix()) {
                if (item != null) {
                    recipeList.add(new ItemTag(item).identify());
                }
                else {
                    recipeList.add(new ItemTag(Material.AIR).identify());
                }
            }
            return recipeList.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.result>
        // @returns ItemTag
        // @description
        // Returns the ItemTag currently in the result section of a crafting inventory.
        // -->
        if (attribute.startsWith("result") && inventory instanceof CraftingInventory) {
            ItemStack result = ((CraftingInventory) inventory).getResult();
            if (result == null) {
                return null;
            }
            return new ItemTag(result).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.anvil_repair_cost>
        // @returns Element(Number)
        // @mechanism anvil_repair_cost
        // @description
        // Returns the current repair cost on an anvil.
        // -->
        if (attribute.startsWith("anvil_repair_cost") && inventory instanceof AnvilInventory) {
            return new ElementTag(((AnvilInventory) inventory).getRepairCost()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.anvil_max_repair_cost>
        // @returns Element(Number)
        // @mechanism anvil_max_repair_cost
        // @description
        // Returns the maximum repair cost on an anvil.
        // -->
        if (attribute.startsWith("anvil_max_repair_cost") && inventory instanceof AnvilInventory) {
            return new ElementTag(((AnvilInventory) inventory).getMaximumRepairCost()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.anvil_rename_text>
        // @returns Element
        // @description
        // Returns the current entered renaming text on an anvil.
        // -->
        if (attribute.startsWith("anvil_rename_text") && inventory instanceof AnvilInventory) {
            return new ElementTag(((AnvilInventory) inventory).getRenameText()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <InventoryTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Inventory' for InventoryTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("type")) {
            return new ElementTag("Inventory").getAttribute(attribute.fulfill(1));
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new ElementTag(identify()).getAttribute(attribute);
    }

    private ArrayList<Mechanism> mechanisms = new ArrayList<>();

    public void applyProperty(Mechanism mechanism) {
        if (NotableManager.isExactSavedObject(this)) {
            Debug.echoError("Cannot apply properties to noted objects.");
            return;
        }
        if (idType == null) {
            mechanisms.add(mechanism);
        }
        else if (idType.equals("generic") || mechanism.matches("holder")) {
            adjust(mechanism);
        }
        else if (!(idType.equals("location") && mechanism.matches("title"))) {
            Debug.echoError("Cannot apply properties to non-generic inventory!");
        }
    }

    @Override
    public void adjust(Mechanism mechanism) {

        CoreUtilities.autoPropertyMechanism(this, mechanism);

        // <--[mechanism]
        // @object InventoryTag
        // @name matrix
        // @input ListTag(ItemTag)
        // @description
        // Sets the items in the matrix slots of this crafting inventory.
        // @tags
        // <InventoryTag.matrix>
        // -->
        if (mechanism.matches("matrix") && mechanism.requireObject(ListTag.class)) {
            if (!(inventory instanceof CraftingInventory)) {
                Debug.echoError("Inventory is not a crafting inventory, cannot set matrix.");
                return;
            }
            CraftingInventory craftingInventory = (CraftingInventory) inventory;
            List<ItemTag> items = mechanism.valueAsType(ListTag.class).filter(ItemTag.class, mechanism.context);
            ItemStack[] itemStacks = new ItemStack[9];
            for (int i = 0; i < 9 && i < items.size(); i++) {
                itemStacks[i] = items.get(i).getItemStack();
            }
            craftingInventory.setMatrix(itemStacks);
            ((Player) inventory.getHolder()).updateInventory();
        }

        // <--[mechanism]
        // @object InventoryTag
        // @name result
        // @input ItemTag
        // @description
        // Sets the item in the result slot of this crafting inventory.
        // @tags
        // <InventoryTag.result>
        // -->
        if (mechanism.matches("result") && mechanism.requireObject(ItemTag.class)) {
            if (!(inventory instanceof CraftingInventory)) {
                Debug.echoError("Inventory is not a crafting inventory, cannot set result.");
                return;
            }
            CraftingInventory craftingInventory = (CraftingInventory) inventory;
            craftingInventory.setResult(mechanism.valueAsType(ItemTag.class).getItemStack());
            ((Player) inventory.getHolder()).updateInventory();
        }

        // <--[mechanism]
        // @object InventoryTag
        // @name anvil_max_repair_cost
        // @input Element(Number)
        // @description
        // Sets the maximum repair cost of an anvil.
        // @tags
        // <InventoryTag.anvil_max_repair_cost>
        // -->
        if (mechanism.matches("anvil_max_repair_cost") && mechanism.requireInteger()) {
            if (!(inventory instanceof AnvilInventory)) {
                Debug.echoError("Inventory is not an anvil, cannot set max repair cost.");
                return;
            }
            ((AnvilInventory) inventory).setMaximumRepairCost(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object InventoryTag
        // @name anvil_repair_cost
        // @input Element(Number)
        // @description
        // Sets the current repair cost of an anvil.
        // @tags
        // <InventoryTag.anvil_repair_cost>
        // -->
        if (mechanism.matches("anvil_repair_cost") && mechanism.requireInteger()) {
            if (!(inventory instanceof AnvilInventory)) {
                Debug.echoError("Inventory is not an anvil, cannot set repair cost.");
                return;
            }
            ((AnvilInventory) inventory).setRepairCost(mechanism.getValue().asInt());
        }
    }
}
