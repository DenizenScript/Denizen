package net.aufdemrand.denizen.objects;

import org.bukkit.inventory.Inventory;

import net.aufdemrand.denizen.tags.Attribute;

public class dInventory implements dObject {


    //////////////////
    //    OBJECT FETCHER
    ////////////////
    
    /**
     * Gets an Inventory Object from a string form.
     *
     * @param string  the string
     * @return  a Material, or null if incorrectly formatted
     *
     */
    public static dInventory valueOf(String string) {
             
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
    

    @Override
    public String getPrefix() {
        return null;
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
    public String getType() {
        return "inventory";
    }

    @Override
    public String identify() {
        return null;
    }

    @Override
    public dObject setPrefix(String prefix) {
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
