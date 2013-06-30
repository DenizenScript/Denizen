package net.aufdemrand.denizen.objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.utilities.Utilities;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import net.aufdemrand.denizen.tags.Attribute;

public class dMaterial implements dObject {

    final static Pattern materialPattern = Pattern.compile("(\\w+):?(\\w+)?");
	
    //////////////////
    //    OBJECT FETCHER
    ////////////////
    
    /**
     * Gets a Material Object from a string form.
     *
     * @param string  the string
     * @return  a Material, or null if incorrectly formatted
     *
     */
    public static dMaterial valueOf(String string) {
    	
    	if (string.toUpperCase().matches("RANDOM")) {
    		
    		// Get a random material
    		return new dMaterial(Material.values()[Utilities.getRandom().nextInt(Material.values().length)]);
    	}
    	
    	Matcher m = materialPattern.matcher(string);
    	
    	if (m.matches()) {
    		
    		if (aH.matchesInteger(m.group(1))) {
    			
    			return new dMaterial(aH.getIntegerFrom(m.group(1)));		
    		}
    		else {
    			
    			for (Material material : Material.values()) {
    				
    				if (material.name().equalsIgnoreCase(m.group(1))) {
    					
    					return new dMaterial(material);
    				}
    			}
    		}
    	}
    			
        // No match
        return null;
    }
    
    /**
     * Determine whether a string is a valid material.
     *
     * @param string  the string
     * @return  true if matched, otherwise false
     *
     */
    public static boolean matches(String arg) {

    	if (arg.toUpperCase().matches("RANDOM"))
    		return true;
    	
    	Matcher m = materialPattern.matcher(arg);
    	
    	if (m.matches())
    		return true;
    	
        return false;

    }
    
    
    ///////////////
    //   Constructors
    /////////////

    public dMaterial(Material material) {
        this.material = material;
    }
    
    public dMaterial(int id) {
        this.material = Material.getMaterial(id);
    }
    
    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Bukkit Material associated

    private Material material;

    public Material getMaterial() {
        return material;
    }
    
    public MaterialData getMaterialData() {
        return new MaterialData(material, (byte) 0);
    }
    

	@Override
	public String getPrefix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String debug() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public String getType() {
		return "material";
	}

	@Override
	public String identify() {
		return null;
	}

	@Override
	public dObject setPrefix(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttribute(Attribute attribute) {
		// TODO Auto-generated method stub
		return null;
	}

}
