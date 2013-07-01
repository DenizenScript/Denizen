package net.aufdemrand.denizen.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.utilities.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.tags.Attribute;

public class dInventory implements dObject {

    final static Pattern inventoryPattern = Pattern.compile("(\\w+):?(\\d+)?");
    
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
    public static dMaterial valueOf(String string) {
                
        // No match
        return null;
    }
    
    /**
     * Determine whether a string is a valid inventory.
     *
     * @param string  the string
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

    
    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Associated with Bukkit Inventory

    private Inventory inventory;

    public Inventory getInventory() {
        return inventory;
    }
    
    public dInventory add(ItemStack[] items) {
    	
    	for (ItemStack item : items) {
    		
    		inventory.addItem(item);
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
     * Replace another inventory with this one,
     * cropping it if necessary so that it fits.
     *
     * @param destination  The destination inventory
     *
     */
    
    public void replace(dInventory destination) {
    	
    	// If the destination is smaller than our current inventory,
    	// remove empty stacks from our current inventory in the hope
    	// that there will then be enough room
    	
    	if (destination.getSize() < this.getSize()) {

    		List<ItemStack> itemList = new ArrayList<ItemStack>();
    		
    		for (ItemStack item : this.getContents()) {

    			if (item != null) itemList.add(item);
    		}
    		
    		// If there is still not enough room, crop our list of items
    		// so it fits
    		
    		if (destination.getSize() < itemList.size()) {
    			
    			itemList = itemList.subList(0, destination.getSize());
    		}

    		// Set the contents of the destination to our modified
    		// item list
    		
    		ItemStack[] results = itemList.toArray(new ItemStack[itemList.size()]);
    		destination.setContents(results);
    	}
    	else {
    	
    		destination.setContents(this.getContents());
    	}
    }
    
    public void clear() {
    	inventory.clear();
    }
    
    public ItemStack[] getContents() {
    	return inventory.getContents();
    }
    
    public int getSize() {
    	return inventory.getSize();
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
        return "dInventory";
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
            		attribute.hasContext(2) && aH.matchesInteger(attribute.getContext(2))) {
            		
            		qty = attribute.getIntContext(2);
            	}
            	
            	return new Element(getInventory().containsAtLeast
            			(dItem.valueOf(attribute.getContext(1)).getItemStack(), qty))
                		.getAttribute(attribute.fulfill(1));
            }
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
