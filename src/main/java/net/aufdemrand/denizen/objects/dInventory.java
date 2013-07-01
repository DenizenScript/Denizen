package net.aufdemrand.denizen.objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.utilities.Utilities;

import org.bukkit.Material;
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

    
    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Associated with Bukkit Inventory

    private Inventory inventory;

    public Inventory getInventory() {
        return inventory;
    }
    
    public int countItems(ItemStack item)
    {
    	int qty = 0;
    	
    	for (ItemStack invStack : inventory)
		{
			// If ItemStacks are empty here, they are null
			if (invStack != null)
			{
				// If item is null, add up the quantity of every stack
				// in the inventory
				//
				// If not, add up the quantities of the stacks that
				// match the item
				
				if (item == null || invStack.isSimilar(item))
					qty = qty + invStack.getAmount();
			}
		}
    	
    	return qty;
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
        
        // Return the number of slots in the inventory
        
        if (attribute.startsWith("size"))
            return new Element(String.valueOf(getInventory().getSize()))
                    .getAttribute(attribute.fulfill(1));
        
        // Return the type of the inventory (e.g. "PLAYER", "CRAFTING")
        
        if (attribute.startsWith("type"))
            return new Element(getInventory().getType().name())
                    .getAttribute(attribute.fulfill(1));
        
        
        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
