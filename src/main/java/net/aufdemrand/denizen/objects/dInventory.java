package net.aufdemrand.denizen.objects;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import net.aufdemrand.denizen.objects.notable.Notable;
import net.aufdemrand.denizen.objects.notable.NotableManager;
import net.aufdemrand.denizen.objects.notable.Note;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;

public class dInventory implements dObject, Notable {
    
    ///////////////////
    //    NOTABLE METHODS
    ////////////////

    public boolean isUnique() {
        return (NotableManager.isSaved(this));
    }
    
    @Note("inventory")
    public String getSaveString() {
        return identify();
    }

    public void makeUnique(String id) {
        NotableManager.saveAs(this, id);
    }

    public void forget() {
        NotableManager.remove(this);
    }
    
    /////////////////////
    //   PATTERNS
    /////////////////
    
    final static Pattern inventory_by_type = Pattern.compile("(in@)(npc|player|entity|location|equipment)(\\[)(.+?)(\\])", Pattern.CASE_INSENSITIVE);
    final static Pattern inventory_by_saved = Pattern.compile("(in@)(.+)");
    
    //////////////////
    //    OBJECT FETCHER
    ////////////////
    
    /**
     * 
     * Gets a dInventory from a string format.
     *
     * @param string
     *          The inventory in string form. (in@player[playerName], in@notableName, etc.)
     * @return 
     *          The dInventory value. If the string is incorrectly formatted or
     *          the specified inventory is invalid, this is null.
     *
     */
    @ObjectFetcher("in")
    public static dInventory valueOf(String string) {
        
        if (string == null) return null;
        
        Matcher m = inventory_by_type.matcher(string);
        
        if (m.matches()) {

            // Set the type of the inventory holder
            String t = m.group(2);
            // Set the name/id/location of the inventory holder
            String h = m.group(4);
            
            if (t.equalsIgnoreCase("npc")) {
                // Check if the NPC ID specified is valid
                if (dNPC.matches((h.startsWith("n@") ? h : "n@" + h)) 
                && (dNPC.valueOf((h.startsWith("n@") ? h : "n@" + h)).getEntity() instanceof Player 
                || dNPC.valueOf((h.startsWith("n@") ? h : "n@" + h)).getEntity() instanceof Horse))
                    return new dInventory(dNPC.valueOf(h).getEntity());
            }
            else if (t.equalsIgnoreCase("player")) {
                // Check if the player name specified is valid
                if (dPlayer.matches(h))
                    return new dInventory(dPlayer.valueOf(h).getPlayerEntity());
            }
            else if (t.equalsIgnoreCase("entity")) {
                // Check if the entity ID specified is valid and the entity is living
                if (dEntity.matches((h.startsWith("e@") ? h : "e@" + h))
                       && dEntity.valueOf((h.startsWith("e@") ? h : "e@" + h)).isLivingEntity())
                    return new dInventory(dEntity.valueOf((h.startsWith("e@") ? h : "e@" + h)).getLivingEntity());
            }
            else if (t.equalsIgnoreCase("location")) {
                // Check if the location specified is valid and the block is a container
                if (dLocation.matches(h)) {
                    BlockState block = dLocation.valueOf(h).getBlock().getState();
                    if (block instanceof InventoryHolder)
                        return new dInventory(block);
                }
            }
            else if (t.equalsIgnoreCase("equipment")) {
                // Match the entity given in the brackets
                if (dNPC.matches(h)) {
                    if (CitizensAPI.getNPCRegistry().getById(Integer.valueOf(h.substring(2))) instanceof Player)
                        return new dInventory(InventoryType.CRAFTING, t, h).add(CitizensAPI.getNPCRegistry()
                                .getById(Integer.valueOf(h.substring(2))).getBukkitEntity().getEquipment().getArmorContents());
                }
                else if (dPlayer.matches(h) && Bukkit.getPlayer(h.substring(2)).isOnline()) {
                    return new dInventory(InventoryType.CRAFTING, t, h).add(Bukkit.getPlayer(h.substring(2))
                            .getEquipment().getArmorContents());
                }
                else if (dEntity.matches(h) && dEntity.valueOf(h).isLivingEntity() 
                            && dEntity.valueOf(h).isSpawned()) {
                    return new dInventory(InventoryType.CRAFTING, t, h).add(dEntity.valueOf(h).getLivingEntity()
                            .getEquipment().getArmorContents());
                }
            }
            
            // If the dInventory is invalid, alert the user and return null
            dB.echoError("Value of dInventory returning null. Invalid " + t + " specified: " + h);
            return null;
            
        }
        
        // Match in@notableName for Notable dInventories
        m = inventory_by_saved.matcher(string);
        
        if (m.matches()) {
            
            if (NotableManager.isType(m.group(2), dInventory.class))
                return (dInventory) NotableManager.getSavedObject(m.group(2));
            
            dB.echoError("Value of dInventory returning null. Invalid notable specified: " + m.group(2));
            return null;
            
        }
        
        dB.echoError("Value of dInventory returning null. Invalid dInventory specified: " + string);
        return null;
        
    }
    
    /**
     * Determine whether a string is a valid inventory.
     *
     * @param arg  the arg string
     * @return  true if matched, otherwise false
     *
     */
    public static boolean matches(String arg) {

        if (valueOf(arg) != null)
            return true;
        
        return false;

    }
    
    
    ///////////////
    //   Constructors
    /////////////
    
    String holderType = null;
    String holderIdentifier = null;

    public dInventory(Inventory inventory) {
        if (inventory.getHolder() != null) {
            
            InventoryHolder holder = inventory.getHolder();
            
            if (!(holder instanceof LivingEntity)) {
                if (holder instanceof DoubleChest)
                    holderIdentifier = ((DoubleChest) holder).getLocation().toString();
                else if (holder instanceof BlockState)
                    holderIdentifier = ((BlockState) holder).getLocation().toString();
                holderType = "location";
            }
            else if (holder instanceof Player) {
                // Check if it's an NPC... currently, only Player NPCs can have inventories
                if (CitizensAPI.getNPCRegistry().isNPC((LivingEntity) holder)) {
                    holderType = "npc";
                    holderIdentifier = String.valueOf(CitizensAPI.getNPCRegistry().getNPC((LivingEntity) holder).getId());
                }
                else {
                    holderType = "player";
                    holderIdentifier = ((Player) holder).getName();
                }
            }
            else if (holder instanceof Horse) {
                holderType = "entity";
                holderIdentifier = String.valueOf(((Horse) holder).getEntityId());
            }
            else if (holder instanceof HopperMinecart) {
                holderType = "entity";
                holderIdentifier = String.valueOf(((HopperMinecart) holder).getEntityId());
            }
            else if (holder instanceof StorageMinecart) {
                holderType = "entity";
                holderIdentifier = String.valueOf(((StorageMinecart) holder).getEntityId());
            }
            else {
                if (CitizensAPI.getNPCRegistry().isNPC((LivingEntity) holder)) {
                    // Return, because only Players and Player NPCs can have inventories right now.
                    // Uncomment this when Denizen implements inventories for all!
                    // holderType = "npc";
                    // holderIdentifier = String.valueOf(((NPC) holder).getId());
                    
                    dB.echoError("Only Player-type NPCs can have inventories.");
                    return;
                }
                else {
                    holderType = "entity";
                    holderIdentifier = String.valueOf(((LivingEntity) holder).getEntityId());
                }
            }
            
        }
        this.inventory = inventory;
    }
    
    public dInventory(InventoryType type) {
        inventory = Bukkit.getServer().createInventory(null, type);
    }
    
    public dInventory(InventoryType type, String id, String identifier) {
        inventory = Bukkit.getServer().createInventory(null, type);
        holderType = id;
        holderIdentifier = identifier;
    }
    
    public dInventory(InventoryHolder holder) {
        if (!(holder instanceof LivingEntity)) {
            if (holder instanceof DoubleChest)
                holderIdentifier = ((DoubleChest) holder).getLocation().toString();
            else if (holder instanceof BlockState)
                holderIdentifier = ((BlockState) holder).getLocation().toString();
            holderType = "location";
        }
        else if (holder instanceof Player) {
            // Check if it's an NPC... currently, only Player NPCs can have inventories
            if (CitizensAPI.getNPCRegistry().isNPC((LivingEntity) holder)) {
                holderType = "npc";
                holderIdentifier = String.valueOf(CitizensAPI.getNPCRegistry().getNPC((LivingEntity) holder).getId());
            }
            else {
                holderType = "player";
                holderIdentifier = ((Player) holder).getName();
            }
        }
        else if (holder instanceof Horse) {
            holderType = "entity";
            holderIdentifier = String.valueOf(((Horse) holder).getEntityId());
        }
        else if (holder instanceof HopperMinecart) {
            holderType = "entity";
            holderIdentifier = String.valueOf(((HopperMinecart) holder).getEntityId());
        }
        else if (holder instanceof StorageMinecart) {
            holderType = "entity";
            holderIdentifier = String.valueOf(((StorageMinecart) holder).getEntityId());
        }
        else {
            if (CitizensAPI.getNPCRegistry().isNPC((LivingEntity) holder)) {
                // Return, because only Players and Player NPCs can have inventories right now.
                // Uncomment this when Denizen implements inventories for all!
                // holderType = "npc";
                // holderIdentifier = String.valueOf(((NPC) holder).getId());
                
                dB.echoError("Only Player-type NPCs can have inventories.");
                return;
            }
            else {
                holderType = "entity";
                holderIdentifier = String.valueOf(((LivingEntity) holder).getEntityId());
            }
        }
    }
    
    public dInventory(Player player) {
        this.inventory = player.getInventory();
        holderType = "player";
        holderIdentifier = player.getName();
    }
    
    public dInventory(BlockState state) {
        if (state instanceof InventoryHolder) {
            this.inventory = ((InventoryHolder) state).getInventory();
            holderType = "location";
            holderIdentifier = state.getLocation().toString();
        }
    }
    
    public dInventory(LivingEntity entity) {
        if (entity instanceof InventoryHolder) {
            this.inventory = ((InventoryHolder) entity).getInventory();
            holderType = "entity";
            holderIdentifier = String.valueOf(entity.getEntityId());
        }
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
     * Add an array of items to this inventory
     * and return the result
     *
     * @param items  The array of items
     * @return  The resulting dInventory
     *
     */
    
    public dInventory add(ItemStack[] items) {
        
        if (inventory == null || items == null) return this;
        
        for (ItemStack item : items) {
            
            if (item != null) inventory.addItem(item);
        }
        
        return this;
    }
        
    /**
     * Count the number or quantities of stacks that
     * match an item in an inventory.
     *
     * @param item  The item (can be null)
     * @param stacks  Whether stacks should be counted
     *                   instead of item quantities
     * @return  The number of stacks or quantity of items
     *
     */
    
    public int count(ItemStack item, boolean stacks)
    {
        if (inventory == null) return 0;
        
        int qty = 0;
        
        for (ItemStack invStack : inventory)
        {
            // If ItemStacks are empty here, they are null
            if (invStack != null)
            {
                // If item is null, include all items in the
                // inventory
                
                if (item == null || invStack.isSimilar(item)) {
                    
                    // If stacks is true, only count the number
                    // of stacks
                    //
                    // Otherwise, count the quantities of stacks
                    
                    if (stacks == true) qty++;
                    else qty = qty + invStack.getAmount();
                }
            }
        }
        
        return qty;
    }
    
    /**
     * Keep only the items from a certain array
     * in this inventory, removing all others
     *
     * @param items  The array of items
     * @return  The resulting dInventory
     *
     */
    
    public dInventory keep(ItemStack[] items) {
        
        if (inventory == null || items == null) return this;
        
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
                if (keep == false) {
                    
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
     * @param items  The array of items
     * @return  The resulting dInventory
     *
     */
    
    public dInventory exclude(ItemStack[] items) {
        
        if (inventory == null || items == null) return this;
        
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
     * @param item  The array of items
     * @return  The resulting dInventory
     *
     */
    
    public dInventory fill(ItemStack[] items) {
        
        if (inventory == null || items == null) return this;
        
        int oldCount = this.count(null, false);
        int newCount = -1;
        
        while (oldCount != newCount) {
            
            oldCount = newCount;
            newCount = this.add(items).count(null, false);
        }
        
        return this;
    }
    
    /**
     * Return the dLocation of this inventory's
     * holder
     *
     * @return  The holder's dLocation
     *
     */
    
    public dLocation getLocation() {
        
        if (inventory != null) {
            
            InventoryHolder holder = inventory.getHolder();
            
            if (holder instanceof BlockState) {
                return new dLocation(((BlockState) holder).getLocation());
            }
            else if (holder instanceof Player) {
                
                return new dLocation(((Player) holder).getLocation());
            }
        }
        
        return null;
    }
    
    /**
     * Remove an array of items from this inventory,
     * and return the result
     *
     * @param items  The array of items
     * @return  The resulting dInventory
     *
     */
    
    public dInventory remove(ItemStack[] items) {
        
        if (inventory == null || items == null) return this;
        
        for (ItemStack item : items) {
            
            if (item != null) inventory.removeItem(item);
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
     * @param   items  The itemStack of the book
     * @return  The resulting dInventory
     *
     */
    
    public dInventory removeBook(ItemStack book) {
        
        if (inventory == null || book == null) return this;
        
        // We have to manually keep track of the quantity
        // we are removing, because we are not relying on
        // Bukkit methods to find matching itemStacks
        int qty = book.getAmount();
        
        // Store the book's meta information in a variable
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        
        for (ItemStack invStack : inventory) {

            if (qty == 0) break;
            
            if (invStack != null && invStack.getItemMeta() instanceof BookMeta) {
                        
                BookMeta invMeta = (BookMeta) invStack.getItemMeta();
                        
                if (invMeta.getAuthor().equalsIgnoreCase(bookMeta.getAuthor())
                    && invMeta.getTitle().equalsIgnoreCase(bookMeta.getTitle())) {

                    // Make sure we don't remove more books than we
                    // need to
                    if (qty - invStack.getAmount() < 0) {
                        
                        invStack.setAmount((qty - invStack.getAmount()) * -1);
                    }
                    else {
                        
                        inventory.removeItem(invStack);
                        
                        // Update the quantity we still have to remove
                        qty = qty - invStack.getAmount();
                    }
                }
            }
            
        }
        
        return this;
    }
    
    /**
     * Replace another inventory with this one,
     * cropping it if necessary so that it fits.
     *
     * @param destination  The destination inventory
     *
     */
    
    public void replace(dInventory destination) {
        
        if (inventory == null || destination == null) return;
        
        // If the destination is smaller than our current inventory,
        // add as many items as possible
        
        if (destination.getSize() < this.getSize()) {

            destination.clear();
            destination.add(this.getContents());
        }
        else {
        
            destination.setContents(this.getContents());
        }
    }
    
    public void clear() {
        if (inventory != null) inventory.clear();
    }
    
    public ItemStack[] getContents() {
        if (inventory != null) return inventory.getContents();
        else return new ItemStack[0];
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
    
    ////////////////////////
    //  dObject Methods
    /////////////////////

    private String prefix = "Inventory";
    
    public String getType() {
        return "Inventory";
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public dInventory setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String debug() {
        return null;
    }

    public String identify() {
        return "in@" + (holderType.equals("notable") ? holderIdentifier : (holderType + "[" + holderIdentifier + "]"));
    }

    public String getAttribute(Attribute attribute) {
        
        if (attribute == null) return null;

        // <--
        // <inventory.contains[<item>].qty[<#>]> -> Element(Number)
        // Check if the inventory contains a certain quantity (1 by default) of an item
        // and return true or false
        // -->
        if (attribute.startsWith("contains")) {
            if (attribute.hasContext(1) && dItem.matches(attribute.getContext(1))) {
                
                int qty = 1;
                
                if (attribute.getAttribute(2).startsWith("qty") &&
                    attribute.hasContext(2) &&
                    aH.matchesInteger(attribute.getContext(2))) {
                    
                    qty = attribute.getIntContext(2);
                }
                
                return new Element(getInventory().containsAtLeast
                        (dItem.valueOf(attribute.getContext(1)).getItemStack(), qty))
                        .getAttribute(attribute.fulfill(qty == 1 ? 1 : 2));
            }
        }
        
        // <--
        // <inventory.location> -> dLocation
        // Returns the location of this inventory's holder.
        // -->
        if (attribute.startsWith("location")) {
            
            return new dLocation(getLocation())
                    .getAttribute(attribute.fulfill(1));
        }
        
        // <--
        // <inventory.qty[<item>]> -> Element(Number)
        // Returns the combined quantity of itemstacks that match an item if
        // one if specified, or the combined quantity of all itemstacks
        // if one is not
        // -->
        if (attribute.startsWith("qty"))
            if (attribute.hasContext(1) && dItem.matches(attribute.getContext(1)))
                return new Element(String.valueOf(count
                    (dItem.valueOf(attribute.getContext(1)).getItemStack(), false)))
                    .getAttribute(attribute.fulfill(1));
            else
                return new Element(String.valueOf(count(null, false)))
                    .getAttribute(attribute.fulfill(1));
        
        // <--
        // <inventory.size> -> Element(Number)
        // Return the number of slots in the inventory
        // -->
        if (attribute.startsWith("size"))
            return new Element(String.valueOf(getSize()))
                    .getAttribute(attribute.fulfill(1));
        
        // <--
        // <inventory.stacks> -> Element(Number)
        // Returns the number of itemstacks that match an item if one is
        // specified, or the number of all itemstacks if one is not
        // -->
        if (attribute.startsWith("stacks"))
            if (attribute.hasContext(1) && dItem.matches(attribute.getContext(1)))
                return new Element(String.valueOf(count
                    (dItem.valueOf(attribute.getContext(1)).getItemStack(), true)))
                    .getAttribute(attribute.fulfill(1));
            else
                return new Element(String.valueOf(count(null, true)))
                    .getAttribute(attribute.fulfill(1));
        
        // <--
        // <inventory.type> -> Element
        // Returns the type of the inventory (e.g. "PLAYER", "CRAFTING", "HORSE")
        // -->
        if (attribute.startsWith("type"))
            return new Element(getInventory().getType().name())
                    .getAttribute(attribute.fulfill(1));
        
        // <--
        // <inventory.equipment> -> dInventory(Equipment)
        // Returns the equipment of an inventory. If the inventory has no
        // equipment (Generally, if it's not alive), this returns null.
        // -->
        if (attribute.startsWith("equipment")) {
            if (getInventory() instanceof PlayerInventory) {
                String identifier = null;
                if (CitizensAPI.getNPCRegistry().isNPC((LivingEntity) getInventory().getHolder())) {
                    if (inventory.getHolder() instanceof Player)
                        identifier = "n@" + CitizensAPI.getNPCRegistry().getNPC((LivingEntity) getInventory().getHolder()).getId();
                    else return new Element("null")
                                .getAttribute(attribute.fulfill(1));
                }
                else if (inventory.getHolder() instanceof Player)
                    identifier = "p@" + ((Player) getInventory().getHolder()).getName();
                else if (inventory.getHolder() instanceof LivingEntity)
                    identifier = "e@" + ((Player) getInventory().getHolder()).getEntityId();
                else return new Element("null")
                        .getAttribute(attribute.fulfill(1));
                
                return new dInventory(InventoryType.CRAFTING, "equipment", identifier)
                        .add(((PlayerInventory) getInventory()).getArmorContents())
                        .getAttribute(attribute.fulfill(1));
            }
            else if (getInventory() instanceof HorseInventory) {
                return new dInventory(InventoryType.CRAFTING, "equipment",
                    (getInventory().getHolder() != null ? "e@" + String.valueOf(((LivingEntity) getInventory().getHolder()).getEntityId())
                     : getInventory().getName()))
                        .getAttribute(attribute.fulfill(1));
            }
        }
        
        // <--
        // <inventory.list_contents> -> dList(dItem)
        // Returns a list of all items in the inventory.
        // -->
        if (attribute.startsWith("list_contents")) {
            ArrayList<dItem> items = new ArrayList<dItem>();
            for (ItemStack item : getContents()) {
                if (item != null && item.getType() != Material.AIR)
                    items.add(new dItem(item));
            }
            return new dList(items).getAttribute(attribute.fulfill(1));
        }
        
        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
