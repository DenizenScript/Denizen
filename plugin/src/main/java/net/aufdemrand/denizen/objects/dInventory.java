package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.nms.abstracts.ImprovedOfflinePlayer;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.scripts.containers.core.InventoryScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.InventoryScriptHelper;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.aH.Argument;
import net.aufdemrand.denizencore.objects.aH.PrimitiveType;
import net.aufdemrand.denizencore.objects.notable.Notable;
import net.aufdemrand.denizencore.objects.notable.Note;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dInventory implements dObject, Notable, Adjustable {

    public static dInventory mirrorBukkitInventory(Inventory inventory) {
        // Scripts have priority over notables
        if (InventoryScriptHelper.tempInventoryScripts.containsKey(inventory)) {
            return new dInventory(inventory).setIdentifiers("script",
                    InventoryScriptHelper.tempInventoryScripts.get(inventory));
        }
        // Use the map to get notable inventories
        if (InventoryScriptHelper.notableInventories.containsKey(inventory.getTitle())) {
            return InventoryScriptHelper.notableInventories.get(inventory.getTitle());
        }
        // Iterate through offline player inventories
        for (Map.Entry<UUID, PlayerInventory> inv : ImprovedOfflinePlayer.offlineInventories.entrySet()) {
            if (inv.getValue().equals(inventory)) {
                return new dInventory(NMSHandler.getInstance().getPlayerHelper().getOfflineData(inv.getKey()));
            }
        }
        // Iterate through offline player enderchests
        for (Map.Entry<UUID, Inventory> inv : ImprovedOfflinePlayer.offlineEnderChests.entrySet()) {
            if (inv.getValue().equals(inventory)) {
                return new dInventory(NMSHandler.getInstance().getPlayerHelper().getOfflineData(inv.getKey()), true);
            }
        }

        return new dInventory(inventory);
    }

    /////////////////////
    //   PATTERNS
    /////////////////

    final static Pattern inventory_by_type = Pattern.compile("(in@)(npc|player|enderchest|workbench|entity|location|generic)\\[(.+?)\\]", Pattern.CASE_INSENSITIVE);
    final static Pattern inventory_by_script = Pattern.compile("(in@)(.+)", Pattern.CASE_INSENSITIVE);


    /////////////////////
    //   STATIC FIELDS
    /////////////////

    // The maximum number of slots a Bukkit inventory can have
    public final static int maxSlots = 54;

    // All of the inventory id types we use
    public final static String[] idTypes = {"npc", "player", "enderchest", "workbench", "entity", "location", "generic"};


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

    public void makeUnique(String id) {
        String title = inventory.getTitle();
        if (title == null || title.startsWith("container.")) {
            title = inventory.getType().getDefaultTitle();
        }
        // You can only have 32 characters in an inventory title... So let's make sure we have at least 3 colors...
        // which brings notable inventory title lengths down to 26... TODO: document this/fix if possible in later version
        if (title.length() > 26) {
            title = title.substring(0, title.charAt(25) == '§' ? 25 : 26);
        }
        String colors;
        while (true) {
            colors = Utilities.generateRandomColors(3);
            if (!InventoryScriptHelper.notableInventories.containsKey(title + colors)) {
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
        NotableManager.remove(idHolder);
    }

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    @Fetchable("in")
    public static dInventory valueOf(String string, TagContext context) {
        if (context == null) {
            return valueOf(string, null, null);
        }
        else {
            return valueOf(string, ((BukkitTagContext) context).player, ((BukkitTagContext) context).npc);
        }
    }

    /**
     * Gets a dInventory from a string format.
     *
     * @param string The inventory in string form. (in@player[playerName], in@scriptName, etc.)
     * @return The dInventory value. If the string is incorrectly formatted or
     * the specified inventory is invalid, this is null.
     */
    public static dInventory valueOf(String string, dPlayer player, dNPC npc) {

        if (string == null) {
            return null;
        }

        ///////
        // Handle objects with properties through the object fetcher
        Matcher m = ObjectFetcher.DESCRIBED_PATTERN.matcher(string);
        if (m.matches()) {
            return ObjectFetcher.getObjectFrom(dInventory.class, string,
                    new BukkitTagContext(player, npc, false, null, false, null));
        }

        // Match in@scriptName for Inventory Scripts, as well as in@notableName
        m = inventory_by_script.matcher(string);

        if (m.matches()) {
            if (ScriptRegistry.containsScript(m.group(2), InventoryScriptContainer.class)) {
                return ScriptRegistry.getScriptContainerAs(m.group(2), InventoryScriptContainer.class)
                        .getInventoryFrom(player, npc);
            }

            if (NotableManager.isSaved(m.group(2)) && NotableManager.isType(m.group(2), dInventory.class)) {
                return (dInventory) NotableManager.getSavedObject(m.group(2));
            }

            for (String idType : idTypes) {
                if (m.group(2).equalsIgnoreCase(idType)) {
                    return new dInventory(m.group(2));
                }
            }
        }

        m = inventory_by_type.matcher(string);

        if (m.matches()) {

            // Set the type of the inventory holder
            String type = CoreUtilities.toLowerCase(m.group(2));
            // Set the name/id/location of the inventory holder
            String holder = m.group(3);

            if (type.equals("generic")) {
                Argument arg = Argument.valueOf(holder);
                if (arg.matchesEnum(InventoryType.values())) {
                    return new dInventory(InventoryType.valueOf(holder.toUpperCase()));
                }
                else if (arg.matchesPrimitive(PrimitiveType.Integer)) {
                    return new dInventory(arg.asElement().asInt());
                }
                else {
                    dB.echoError("That type of inventory does not exist!");
                }
            }
            else if (type.equals("npc")) {
                if (dNPC.matches(holder)) {
                    return dNPC.valueOf(holder).getDenizenInventory();
                }
            }
            else if (type.equals("player")) {
                if (dPlayer.matches(holder)) {
                    return dPlayer.valueOf(holder).getInventory();
                }
            }
            else if (type.equals("workbench")) {
                if (dPlayer.matches(holder)) {
                    dInventory workbench = dPlayer.valueOf(holder).getWorkbench();
                    if (workbench != null) {
                        dB.echoError("Value of dInventory returning null (" + string + ")." +
                                " Specified player does not have an open workbench.");
                    }
                    else {
                        return workbench;
                    }
                }
            }
            else if (type.equals("enderchest")) {
                if (dPlayer.matches(holder)) {
                    return dPlayer.valueOf(holder).getEnderChest();
                }
            }
            else if (type.equals("entity")) {
                if (dEntity.matches(holder)) {
                    return dEntity.valueOf(holder).getInventory();
                }
            }
            else if (type.equals("location")) {
                if (dLocation.matches(holder)) {
                    return dLocation.valueOf(holder).getInventory();
                }
            }

            // If the dInventory is invalid, alert the user and return null
            dB.echoError("Value of dInventory returning null. Invalid " +
                    type + " specified: " + holder);
            return null;
        }

        dB.echoError("Value of dInventory returning null. Invalid dInventory specified: " + string);
        return null;
    }

    /**
     * Determine whether a string is a valid inventory.
     *
     * @param arg the arg string
     * @return true if matched, otherwise false
     */
    public static boolean matches(String arg) {

        // Every single dInventory should have the in@ prefix. No exceptions.
        return CoreUtilities.toLowerCase(arg).startsWith("in@");

    }


    ///////////////
    //   Constructors
    /////////////

    String idType = null;
    String idHolder = null;

    public dInventory(Inventory inventory) {
        this.inventory = inventory;
        loadIdentifiers();
    }

    public dInventory(Inventory inventory, InventoryHolder holder) {
        this.inventory = inventory;
        loadIdentifiers(holder);
    }

    public dInventory(InventoryHolder holder) {
        inventory = holder.getInventory();
        loadIdentifiers();
    }

    public dInventory(ItemStack[] items) {
        inventory = Bukkit.getServer().createInventory(null, (int) Math.ceil(items.length / 9) * 9);
        setContents(items);
        loadIdentifiers();
    }

    public dInventory(ImprovedOfflinePlayer offlinePlayer) {
        this(offlinePlayer, false);
    }

    public dInventory(ImprovedOfflinePlayer offlinePlayer, boolean isEnderChest) {
        inventory = isEnderChest ? offlinePlayer.getEnderChest() : offlinePlayer.getInventory();
        setIdentifiers(isEnderChest ? "enderchest" : "player", "p@" + offlinePlayer.getUniqueId());
    }

    public dInventory(int size, String title) {
        if (size <= 0 || size % 9 != 0) {
            dB.echoError("InventorySize must be multiple of 9, and greater than 0.");
            return;
        }
        inventory = Bukkit.getServer().createInventory(null, size, title);
        loadIdentifiers();
    }

    public dInventory(InventoryType type) {
        inventory = Bukkit.getServer().createInventory(null, type);
        loadIdentifiers();
    }

    public dInventory(int size) {
        this(size, "Chest");
    }

    public dInventory(String idType) {
        this.idType = CoreUtilities.toLowerCase(idType);
        for (Mechanism mechanism : mechanisms) {
            adjust(mechanism);
        }
        mechanisms.clear();
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

    public void setInventory(Inventory inventory, dPlayer player) {
        this.inventory = inventory;
        this.idHolder = player.identify();
    }

    public void setTitle(String title) {
        if (!(getIdType().equals("generic") || getIdType().equals("script")) || title == null) {
            return;
        }
        else if (inventory == null) {
            inventory = Bukkit.getServer().createInventory(null, maxSlots, title);
            loadIdentifiers();
            return;
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

    public boolean containsItem(dItem item, int amount) {
        if (item == null) {
            return false;
        }
        item = new dItem(item.getItemStack().clone());
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
            String newItem = CoreUtilities.toLowerCase(new dItem(is).getFullString());
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

    public boolean removeItem(dItem item, int amount) {
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
            String newItem = CoreUtilities.toLowerCase(dItem.valueOf(new dItem(is).getFullString()).getFullString());
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
            dB.echoError("InventorySize must be multiple of 9, and greater than 0.");
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
            for (int i = 0; i < size; i++) // TODO: Why is this a manual copy?
            {
                newContents[i] = oldContents[i];
            }
        }
        else {
            newContents = oldContents;
        }
        String title = inventory.getTitle();
        inventory = Bukkit.getServer().createInventory(null, size,
                (title != null ? title : inventory.getType().getDefaultTitle()));
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
            if (holder instanceof dNPC) {
                idType = "npc";
                idHolder = ((dNPC) holder).identify();
                return;
            }
            else if (holder instanceof Player) {
                if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC((Player) holder)) {
                    idType = "npc";
                    idHolder = (dNPC.fromEntity((Player) holder)).identify();
                    return;
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
                idHolder = new dPlayer((Player) holder).identify();
                return;
            }
            else if (holder instanceof Entity) {
                idType = "entity";
                idHolder = new dEntity((Entity) holder).identify();
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
            for (Map.Entry<UUID, PlayerInventory> inv : ImprovedOfflinePlayer.offlineInventories.entrySet()) {
                if (inv.getValue().equals(inventory)) {
                    idHolder = new dPlayer(inv.getKey()).identify();
                    return;
                }
            }
        }
        else if (getIdType().equals("enderchest")) {
            // Iterate through offline player enderchests
            for (Map.Entry<UUID, Inventory> inv : ImprovedOfflinePlayer.offlineEnderChests.entrySet()) {
                if (inv.getValue().equals(inventory)) {
                    idHolder = new dPlayer(inv.getKey()).identify();
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

    public dInventory setIdentifiers(String type, String holder) {
        idType = type;
        idHolder = holder;
        return this;
    }

    public String getIdType() {
        return idType == null ? "" : idType;
    }

    public String getIdHolder() {
        return idHolder == null ? "" : idHolder;
    }

    /**
     * Return the dLocation of this inventory's
     * holder
     *
     * @return The holder's dLocation
     */

    public dLocation getLocation() {
        return getLocation(inventory.getHolder());
    }

    public dLocation getLocation(InventoryHolder holder) {
        if (inventory != null && holder != null) {
            if (holder instanceof BlockState) {
                return new dLocation(((BlockState) holder).getLocation());
            }
            else if (holder instanceof DoubleChest) {
                return new dLocation(((DoubleChest) holder).getLocation());
            }
            else if (holder instanceof Entity) {
                return new dLocation(((Entity) holder).getLocation());
            }
            else if (holder instanceof dNPC) {
                dNPC npc = (dNPC) holder;
                if (npc.getLocation() == null) {
                    return new dLocation(((dNPC) holder).getCitizen().getStoredLocation());
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
            return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_9_R2) ? inventory.getStorageContents() : inventory.getContents();
        }
        else {
            return new ItemStack[0];
        }
    }

    public dList getEquipment() {
        ItemStack[] equipment = null;
        if (inventory instanceof PlayerInventory) {
            equipment = ((PlayerInventory) inventory).getArmorContents();
        }
        else if (inventory instanceof HorseInventory) {
            equipment = new ItemStack[]{((HorseInventory) inventory).getSaddle(), ((HorseInventory) inventory).getArmor()};
        }
        if (equipment == null) {
            return null;
        }
        dList equipmentList = new dList();
        for (ItemStack item : equipment) {
            equipmentList.add(new dItem(item).identify());
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

    public void setContents(dList list) {
        int size;
        if (inventory == null) {
            size = (int) Math.ceil(list.size() / 9) * 9;
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
        for (dItem item : list.filter(dItem.class)) {
            contents[filled] = item.getItemStack();
            filled++;
        }
        final ItemStack air = new ItemStack(Material.AIR);
        while (filled < size) {
            contents[filled] = air;
            filled++;
        }
        inventory.setContents(contents);
        if (Depends.citizens != null && dNPC.matches(idHolder)) { // TODO: Directly store holder
            dNPC.valueOf(idHolder).getInventoryTrait().setContents(contents);
        }
    }

    public boolean update() {
        if (getIdType().equals("player")) {
            dPlayer.valueOf(idHolder).getPlayerEntity().updateInventory();
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
        ItemStack[] inventory = getContents();
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
    public dInventory add(int slot, ItemStack... items) {
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

        List<ItemStack> leftovers = new ArrayList<ItemStack>();

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

        List<ItemStack> leftovers = new ArrayList<ItemStack>();

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
                // If item is null, include all items in the
                // inventory

                if (item == null || invStack.isSimilar(item)) {

                    // If stacks is true, only count the number
                    // of stacks
                    //
                    // Otherwise, count the quantities of stacks

                    if (stacks) {
                        qty++;
                    }
                    else {
                        qty = qty + invStack.getAmount();
                    }
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
     * @return The resulting dInventory
     */

    public dInventory keep(ItemStack[] items) {

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
     * @return The resulting dInventory
     */

    public dInventory exclude(ItemStack[] items) {

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
     * @return The resulting dInventory
     */

    public dInventory fill(ItemStack[] items) {

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
     * @return The resulting dInventory
     */

    public dInventory remove(ItemStack[] items) {

        if (inventory == null || items == null) {
            return this;
        }

        for (ItemStack item : items) {
            if (item != null) {
                inventory.removeItem(item);
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
     * @return The resulting dInventory
     */

    public dInventory removeBook(String title, String author, int quantity) {

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

    public void replace(dInventory destination) {

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
    public dInventory setSlots(int slot, ItemStack... items) {
        return setSlots(slot, items, items.length);
    }

    /**
     * Set items in an inventory, starting with a specified slot
     *
     * @param slot  The slot to start from
     * @param items The items to add
     * @return The resulting dInventory
     */
    public dInventory setSlots(int slot, ItemStack[] items, int c) {

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
        if (Depends.citizens != null && dNPC.matches(idHolder)) { // TODO: Directly store holder
            dNPC.valueOf(idHolder).getInventoryTrait().setContents(inventory.getContents());
        }
        return this;

    }

    public void clear() {
        if (inventory != null) {
            inventory.clear();
        }
    }

    ////////////////////////
    //  dObject Methods
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
    public dInventory setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }


    @Override
    public String debug() {
        return "<G>" + prefix + "='<Y>" + identify() + "<G>'  ";
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


    @Override
    public String identifySimple() {
        if (isUnique()) {
            return "in@" + NotableManager.getSavedId(this);
        }
        else {
            return "in@" + (getIdType().equals("script") || getIdType().equals("notable")
                    ? idHolder : (idType + "[" + idHolder + ']'));
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
        // @attribute <in@inventory.empty_slots>
        // @returns Element(Integer)
        // @description
        // Returns the number of empty slots in an inventory.
        // -->
        if (attribute.startsWith("empty_slots")) {
            dInventory dummyInv;
            if (inventory.getType() == InventoryType.PLAYER) {
                dummyInv = new dInventory(Bukkit.createInventory(null, InventoryType.CHEST));
                ItemStack[] contents = getStorageContents();
                dummyInv.setSize(contents.length);
                if (contents.length != dummyInv.getSize()) {
                    contents = Arrays.copyOf(contents, dummyInv.getSize());
                }
                dummyInv.setContents(contents);
            }
            else {
                dummyInv = new dInventory(inventory);
            }
            int full = dummyInv.count(null, true);
            return new Element(dummyInv.getSize() - full).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.can_fit[<item>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the inventory can fit an item.
        // -->
        if (attribute.startsWith("can_fit") && attribute.hasContext(1) && dItem.matches(attribute.getContext(1))) {
            dItem item = dItem.valueOf(attribute.getContext(1));
            if (item == null) {
                return null;
            }
            int attribs = 1;
            int qty = 1;

            InventoryType type = inventory.getType();
            dInventory dummyInv = new dInventory(Bukkit.createInventory(null, type == InventoryType.PLAYER ? InventoryType.CHEST : type, inventory.getTitle()));
            ItemStack[] contents = getStorageContents();
            if (dummyInv.getInventoryType() == InventoryType.CHEST) {
                dummyInv.setSize(contents.length);
            }
            if (contents.length != dummyInv.getSize()) {
                contents = Arrays.copyOf(contents, dummyInv.getSize());
            }
            dummyInv.setContents(contents);

            // <--[tag]
            // @attribute <in@inventory.can_fit[<item>].quantity[<#>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the inventory can fit a certain quantity of an item.
            // -->
            if ((attribute.getAttribute(2).startsWith("quantity") || attribute.getAttribute(2).startsWith("qty")) &&
                    attribute.hasContext(2) &&
                    aH.matchesInteger(attribute.getContext(2))) {
                qty = attribute.getIntContext(2);
                attribs = 2;
            }
            item.setAmount(qty);

            List<ItemStack> leftovers = dummyInv.addWithLeftovers(0, true, item.getItemStack());
            return new Element(leftovers.isEmpty()).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <in@inventory.include[<item>]>
        // @returns dInventory
        // @description
        // Returns the dInventory with an item added.
        // -->
        if (attribute.startsWith("include") && attribute.hasContext(1)
                && dItem.matches(attribute.getContext(1))) {
            dItem item = dItem.valueOf(attribute.getContext(1));
            if (item == null) {
                return null;
            }
            int attribs = 1;
            int qty = 1;

            dInventory dummyInv = new dInventory(Bukkit.createInventory(null, inventory.getType(), inventory.getTitle()));
            if (inventory.getType() == InventoryType.CHEST) {
                dummyInv.setSize(inventory.getSize());
            }
            dummyInv.setContents(getContents());

            // <--[tag]
            // @attribute <in@inventory.include[<item>].quantity[<#>]>
            // @returns dInventory
            // @description
            // Returns the dInventory with a certain quantity of an item added.
            // -->
            if ((attribute.getAttribute(2).startsWith("quantity") || attribute.getAttribute(2).startsWith("qty")) &&
                    attribute.hasContext(2) && aH.matchesInteger(attribute.getContext(2))) {
                qty = attribute.getIntContext(2);
                attribs = 2;
            }
            item.setAmount(qty);
            dummyInv.add(0, item.getItemStack());
            return dummyInv.getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <in@inventory.is_empty>
        // @returns Element(Boolean)
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
            return new Element(empty).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.is_full>
        // @returns Element(Boolean)
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
            return new Element(full).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.contains.display[(strict:)<element>]>
        // @returns Element(Boolean)
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
            // @attribute <in@inventory.contains.display[(strict:)<element>].quantity[<#>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of an item with the
            // specified display name. Use 'strict:' in front of the search element to ensure
            // the display name is EXACTLY the search element, otherwise the searching will only
            // check if the search element is contained in the display name.
            // -->
            if ((attribute.getAttribute(3).startsWith("quantity") || attribute.getAttribute(3).startsWith("qty")) &&
                    attribute.hasContext(3) &&
                    aH.matchesInteger(attribute.getContext(3))) {
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

            return new Element(found_items >= qty).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <in@inventory.contains.lore[(strict:)<element>|...]>
        // @returns Element(Boolean)
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
            dList lore = dList.valueOf(search_string);
            int qty = 1;
            int attribs = 2;

            // <--[tag]
            // @attribute <in@inventory.contains.lore[(strict:)<element>|...].quantity[<#>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of an item
            // with the specified lore. Use 'strict:' in front of the search elements
            // to ensure all lore lines are EXACTLY the search elements, otherwise the
            // searching will only check if the search elements are contained in the lore.
            // -->
            if ((attribute.getAttribute(3).startsWith("quantity") || attribute.getAttribute(3).startsWith("qty")) &&
                    attribute.hasContext(3) &&
                    aH.matchesInteger(attribute.getContext(3))) {
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

            return new Element(found_items >= qty).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <in@inventory.contains.scriptname[<material>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the inventory contains an item with the specified scriptname.
        // -->
        if (attribute.startsWith("contains.scriptname") && attribute.hasContext(2) &&
                dMaterial.matches(attribute.getContext(2))) {
            String scrName = attribute.getContext(2);
            int qty = 1;
            int attribs = 2;

            // <--[tag]
            // @attribute <in@inventory.contains.scriptname[<material>].quantity[<#>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of an item with the specified scriptname.
            // -->
            if ((attribute.getAttribute(3).startsWith("quantity") || attribute.getAttribute(3).startsWith("qty")) &&
                    attribute.hasContext(3) &&
                    aH.matchesInteger(attribute.getContext(3))) {
                qty = attribute.getIntContext(3);
                attribs = 3;
            }

            int found_items = 0;

            for (ItemStack item : getContents()) {
                if (item != null && scrName.equals(new dItem(item).getScriptName())) {
                    found_items += item.getAmount();
                    if (found_items >= qty) {
                        break;
                    }
                }
            }

            return new Element(found_items >= qty).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <in@inventory.contains.material[<material>]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the inventory contains an item with the specified material.
        // -->
        if (attribute.startsWith("contains.material") && attribute.hasContext(2) &&
                dMaterial.matches(attribute.getContext(2))) {
            dMaterial material = dMaterial.valueOf(attribute.getContext(2));
            int qty = 1;
            int attribs = 2;

            // <--[tag]
            // @attribute <in@inventory.contains.material[<material>].quantity[<#>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of an item with the
            // specified material.
            // -->
            if ((attribute.getAttribute(3).startsWith("quantity") || attribute.getAttribute(3).startsWith("qty")) &&
                    attribute.hasContext(3) &&
                    aH.matchesInteger(attribute.getContext(3))) {
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

            return new Element(found_items >= qty).getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <in@inventory.contains_any[<item>|...]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the inventory contains any of the specified items.
        // -->
        if (attribute.startsWith("contains_any") && attribute.hasContext(1)) {
            dList list = dList.valueOf(attribute.getContext(1));
            if (list.isEmpty()) {
                return null;
            }
            int qty = 1;
            int attribs = 1;

            // <--[tag]
            // @attribute <in@inventory.contains_any[<item>|...].quantity[<#>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of any of the specified items.
            // -->
            if ((attribute.getAttribute(2).startsWith("quantity") || attribute.getAttribute(2).startsWith("qty"))
                    && attribute.hasContext(2) && aH.matchesInteger(attribute.getContext(2))) {
                qty = attribute.getIntContext(2);
                attribs = 2;
            }
            List<dItem> contains = list.filter(dItem.class, attribute.getScriptEntry());
            if (!contains.isEmpty()) {
                for (dItem item : contains) {
                    if (containsItem(item, qty)) {
                        return Element.TRUE.getAttribute(attribute.fulfill(attribs));
                    }
                }
            }
            return Element.FALSE.getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <in@inventory.contains[<item>|...]>
        // @returns Element(Boolean)
        // @description
        // Returns whether the inventory contains all of the specified items.
        // -->
        if (attribute.startsWith("contains") && attribute.hasContext(1)) {
            dList list = dList.valueOf(attribute.getContext(1));
            if (list.isEmpty()) {
                return null;
            }
            int qty = 1;
            int attribs = 1;

            // <--[tag]
            // @attribute <in@inventory.contains[<item>|...].quantity[<#>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the inventory contains a certain quantity of all of the specified items.
            // -->
            if ((attribute.getAttribute(2).startsWith("quantity") || attribute.getAttribute(2).startsWith("qty"))
                    && attribute.hasContext(2) && aH.matchesInteger(attribute.getContext(2))) {
                qty = attribute.getIntContext(2);
                attribs = 2;
            }
            // TODO: Fix logic
            List<dItem> contains = list.filter(dItem.class, attribute.getScriptEntry());
            if (!contains.isEmpty()) {
                for (dItem item : contains) {
                    if (containsItem(item, qty)) {
                        return Element.TRUE.getAttribute(attribute.fulfill(attribs));
                    }
                }
            }
            return Element.FALSE.getAttribute(attribute.fulfill(attribs));
        }

        // <--[tag]
        // @attribute <in@inventory.first_empty>
        // @returns Element(Number)
        // @description
        // Returns the location of the first empty slot.
        // Returns -1 if the inventory is full.
        // -->
        if (attribute.startsWith("first_empty")) {
            return new Element(firstEmpty(0)).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.find.material[<material>]>
        // @returns Element(Number)
        // @description
        // Returns the location of the first slot that contains the material.
        // Returns -1 if there's no match.
        // -->
        if (attribute.startsWith("find.material")
                && attribute.hasContext(2)
                && dMaterial.matches(attribute.getContext(2))) {
            dMaterial material = dMaterial.valueOf(attribute.getContext(2));
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
            return new Element(slot).getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <in@inventory.find_imperfect[<item>]>
        // @returns Element(Number)
        // @description
        // Returns the location of the first slot that contains the item.
        // Returns -1 if there's no match.
        // Will match item script to item script, even if one is edited.
        // -->
        if (attribute.startsWith("find_imperfect")
                && attribute.hasContext(1)
                && dItem.matches(attribute.getContext(1))) {
            dItem item = dItem.valueOf(attribute.getContext(1),
                    attribute.getScriptEntry() != null ? ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer() : null,
                    attribute.getScriptEntry() != null ? ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getNPC() : null);
            item.setAmount(1);
            int slot = -1;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) != null) {
                    dItem compare_to = new dItem(inventory.getItem(i).clone());
                    compare_to.setAmount(1);
                    if (item.identify().equalsIgnoreCase(compare_to.identify())) {
                        slot = i + 1;
                        break;
                    }
                }
            }
            return new Element(slot).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.find[<item>]>
        // @returns Element(Number)
        // @description
        // Returns the location of the first slot that contains the item.
        // Returns -1 if there's no match.
        // -->
        if (attribute.startsWith("find")
                && attribute.hasContext(1)
                && dItem.matches(attribute.getContext(1))) {
            dItem item = dItem.valueOf(attribute.getContext(1),
                    attribute.getScriptEntry() != null ? ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer() : null,
                    attribute.getScriptEntry() != null ? ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getNPC() : null);
            item.setAmount(1);
            int slot = -1;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) != null) {
                    dItem compare_to = new dItem(inventory.getItem(i).clone());
                    compare_to.setAmount(1);
                    if (item.getFullString().equalsIgnoreCase(compare_to.getFullString())) {
                        slot = i + 1;
                        break;
                    }
                }
            }
            return new Element(slot).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.id_type>
        // @returns Element
        // @description
        // Returns Denizen's type ID for this inventory. (player, location, etc.)
        // -->
        if (attribute.startsWith("id_type")) {
            return new Element(idType).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.notable_name>
        // @returns Element
        // @description
        // Gets the name of a Notable dInventory. If the inventory isn't noted,
        // this is null.
        // -->
        if (attribute.startsWith("notable_name")) {
            String notname = NotableManager.getSavedId(this);
            if (notname == null) {
                return null;
            }
            return new Element(notname).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.location>
        // @returns dLocation
        // @description
        // Returns the location of this inventory's holder.
        // -->
        if (attribute.startsWith("location")) {
            dLocation location = getLocation();
            if (location == null) {
                return null;
            }
            return location.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.quantity[<item>]>
        // @returns Element(Number)
        // @description
        // Returns the combined quantity of itemstacks that match an item if
        // one is specified, or the combined quantity of all itemstacks
        // if one is not.
        // -->
        if (attribute.startsWith("quantity") || attribute.startsWith("qty")) {
            if (attribute.hasContext(1) && dItem.matches(attribute.getContext(1))) {
                return new Element(count // TODO: Handle no-script-entry cases
                        (dItem.valueOf(attribute.getContext(1),
                                ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer(),
                                ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getNPC()).getItemStack(), false))
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return new Element(count(null, false))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <in@inventory.stacks[<item>]>
        // @returns Element(Number)
        // @description
        // Returns the number of itemstacks that match an item if one is
        // specified, or the number of all itemstacks if one is not.
        // -->
        if (attribute.startsWith("stacks")) {
            if (attribute.hasContext(1) && dItem.matches(attribute.getContext(1))) {
                return new Element(count // TODO: Handle no-script-entry cases
                        (dItem.valueOf(attribute.getContext(1),
                                ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getPlayer(),
                                ((BukkitScriptEntryData) attribute.getScriptEntry().entryData).getNPC()).getItemStack(), true))
                        .getAttribute(attribute.fulfill(1));
            }
            else {
                return new Element(count(null, true))
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <in@inventory.slot[<#>]>
        // @returns dItem
        // @description
        // Returns the item in the specified slot.
        // -->
        if (attribute.startsWith("slot")
                && attribute.hasContext(1)
                && aH.matchesInteger(attribute.getContext(1))) {
            int slot = new Element(attribute.getContext(1)).asInt() - 1;
            if (slot < 0) {
                slot = 0;
            }
            else if (slot > getInventory().getSize() - 1) {
                slot = getInventory().getSize() - 1;
            }
            return new dItem(getInventory().getItem(slot))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.inventory_type>
        // @returns Element
        // @description
        // Returns the type of the inventory (e.g. "PLAYER", "CRAFTING", "HORSE").
        // -->
        if (attribute.startsWith("inventory_type")) {
            return new Element(inventory instanceof HorseInventory ? "HORSE" : getInventory().getType().name())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <in@inventory.equipment>
        // @returns dList(dItem)
        // @description
        // Returns the equipment of an inventory.
        // -->
        if (attribute.startsWith("equipment")) {
            dList equipment = getEquipment();
            if (equipment == null) {
                return null;
            }
            return equipment.getAttribute(attribute.fulfill(1));
        }

        if (inventory instanceof CraftingInventory) {
            CraftingInventory craftingInventory = (CraftingInventory) inventory;

            // <--[tag]
            // @attribute <in@inventory.matrix>
            // @returns dList(dItem)
            // @description
            // Returns the dItems currently in a crafting inventory's matrix.
            // -->
            if (attribute.startsWith("matrix")) {
                dList recipeList = new dList();
                for (ItemStack item : craftingInventory.getMatrix()) {
                    if (item != null) {
                        recipeList.add(new dItem(item).identify());
                    }
                    else {
                        recipeList.add(new dItem(Material.AIR).identify());
                    }
                }
                return recipeList.getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <in@inventory.result>
            // @returns dItem
            // @description
            // Returns the dItem currently in the result section of a crafting inventory.
            // -->
            if (attribute.startsWith("result")) {
                ItemStack result = craftingInventory.getResult();
                if (result == null) {
                    return null;
                }
                return new dItem(result).getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <in@inventory.type>
        // @returns Element
        // @description
        // Always returns 'Inventory' for dInventory objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        if (attribute.startsWith("type")) {
            return new Element("Inventory").getAttribute(attribute.fulfill(1));
        }

        // Iterate through this object's properties' attributes
        for (Property property : PropertyParser.getProperties(this)) {
            String returned = property.getAttribute(attribute);
            if (returned != null) {
                return returned;
            }
        }

        return new Element(identify()).getAttribute(attribute);
    }

    private ArrayList<Mechanism> mechanisms = new ArrayList<Mechanism>();

    public void applyProperty(Mechanism mechanism) {
        if (idType == null) {
            mechanisms.add(mechanism);
        }
        else if (idType.equals("generic") || mechanism.matches("holder")) {
            adjust(mechanism);
        }
        else if (!(idType.equals("location") && mechanism.matches("title"))) {
            dB.echoError("Cannot apply properties to non-generic inventory!");
        }
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // Iterate through this object's properties' mechanisms
        for (Property property : PropertyParser.getProperties(this)) {
            property.adjust(mechanism);
            if (mechanism.fulfilled()) {
                break;
            }
        }

        if (inventory instanceof CraftingInventory) {
            CraftingInventory craftingInventory = (CraftingInventory) inventory;

            // <--[mechanism]
            // @object dInventory
            // @name matrix
            // @input dList(dItem)
            // @description
            // Sets the items in the matrix slots of this crafting inventory.
            // @tags
            // <in@inventory.matrix>
            // -->
            if (mechanism.matches("matrix") && mechanism.requireObject(dList.class)) {
                List<dItem> items = mechanism.getValue().asType(dList.class).filter(dItem.class);
                ItemStack[] itemStacks = new ItemStack[9];
                for (int i = 0; i < 9 && i < items.size(); i++) {
                    itemStacks[i] = items.get(i).getItemStack();
                }
                craftingInventory.setMatrix(itemStacks);
                ((Player) inventory.getHolder()).updateInventory();
            }

            // <--[mechanism]
            // @object dInventory
            // @name result
            // @input dItem
            // @description
            // Sets the item in the result slot of this crafting inventory.
            // @tags
            // <in@inventory.result>
            // -->
            if (mechanism.matches("result") && mechanism.requireObject(dItem.class)) {
                craftingInventory.setResult(mechanism.getValue().asType(dItem.class).getItemStack());
                ((Player) inventory.getHolder()).updateInventory();
            }
        }

        if (!mechanism.fulfilled()) {
            mechanism.reportInvalid();
        }

    }
}
