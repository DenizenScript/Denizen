package net.aufdemrand.denizen.objects;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.aufdemrand.denizen.tags.Attribute;

public class dInventory implements dObject {

    //final static Pattern inventoryPattern = Pattern.compile("(\\w+)");
    
    //////////////////
    //    OBJECT FETCHER
    ////////////////
    
    /**
     * Gets an Inventory Object from a string form.
     *
     * @param string  the string
     * @return  an Inventory, or null if incorrectly formatted
     *
     */
    @ObjectFetcher("in")
    public static dInventory valueOf(String string) {
                
        // No match
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

        return false;

    }
    
    
    ///////////////
    //   Constructors
    /////////////

    public dInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    public dInventory(InventoryType type) {
    	
    	inventory = Bukkit.getServer().createInventory(null, type);
    }
    
    public dInventory(InventoryHolder holder) {
        this.inventory = holder.getInventory();
    }
    
    public dInventory(Player player) {
        this.inventory = player.getInventory();
    }
    
    public dInventory(BlockState state) {
    	
    	if (state instanceof InventoryHolder) {
    		this.inventory = ((InventoryHolder) state).getInventory();
    	}
    }
    
    public dInventory(LivingEntity entity) {
    	
    	if (entity instanceof InventoryHolder) {
    		this.inventory = ((InventoryHolder) entity).getInventory();
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
     * 				  instead of item quantities
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
     * @param items  The itemStack of the book
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
    
    
    
    //////////////////////////////
    //  DSCRIPT ARGUMENT METHODS
    /////////////////////////

    private String prefix = "Inventory";
    
    @Override
    public String getType() {
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
        return null;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String identify() {
        return null;
    }

    @Override
    public String getAttribute(Attribute attribute) {
        
        if (attribute == null) return null;

        // Check if the inventory contains a certain quantity (1 by default) of an item
        // and return true or false
        
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
                		.getAttribute(attribute.fulfill(1));
            }
        }
        
        // Get the location of this inventory's holder
        
        if (attribute.startsWith("location")) {
        	
        	return new dLocation(getLocation())
                	.getAttribute(attribute.fulfill(1));
        }
        
        // Get the combined quantity of itemstacks that match an item if
        // one if specified, or the combined quantity of all itemstacks
        // if one is not
        
        if (attribute.startsWith("qty"))
            if (attribute.hasContext(1) && dItem.matches(attribute.getContext(1)))
            	return new Element(String.valueOf(count
            		(dItem.valueOf(attribute.getContext(1)).getItemStack(), false)))
            		.getAttribute(attribute.fulfill(1));
            else
            	return new Element(String.valueOf(count(null, false)))
            		.getAttribute(attribute.fulfill(1));
        
        // Return the number of slots in the inventory
        
        if (attribute.startsWith("size"))
            return new Element(String.valueOf(getSize()))
                    .getAttribute(attribute.fulfill(1));
        
        // Get the number of itemstacks that match an item if one is
        // specified, or the number of all itemstacks if one is not
        
        if (attribute.startsWith("stacks"))
            if (attribute.hasContext(1) && dItem.matches(attribute.getContext(1)))
            	return new Element(String.valueOf(count
            		(dItem.valueOf(attribute.getContext(1)).getItemStack(), true)))
            		.getAttribute(attribute.fulfill(1));
            else
            	return new Element(String.valueOf(count(null, true)))
            		.getAttribute(attribute.fulfill(1));
        
        // Return the type of the inventory (e.g. "PLAYER", "CRAFTING")
        
        if (attribute.startsWith("type"))
            return new Element(getInventory().getType().name())
                    .getAttribute(attribute.fulfill(1));
        
        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
