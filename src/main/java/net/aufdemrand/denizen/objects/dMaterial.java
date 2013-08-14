package net.aufdemrand.denizen.objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.utilities.Utilities;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import net.aufdemrand.denizen.tags.Attribute;

public class dMaterial implements dObject {

    final static Pattern materialPattern = Pattern.compile("(?:m@)?(\\w+):?(\\d+)?", Pattern.CASE_INSENSITIVE);
    
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
    @ObjectFetcher("m")
    public static dMaterial valueOf(String string) {

        if (string.toLowerCase().matches("random")
                || string.toLowerCase().matches("m@random")) {
            
            // Get a random material
            return new dMaterial(Material.values()[Utilities.getRandom().nextInt(Material.values().length)]);
        }
        
        Matcher m = materialPattern.matcher(string);
        
        if (m.matches()) {
            
            int data = -1;
            
            if (m.group(2) != null) {
                
                data = aH.getIntegerFrom(m.group(2));
            }
            
            if (aH.matchesInteger(m.group(1))) {
                
                return new dMaterial(aH.getIntegerFrom(m.group(1)), data);        
            }
            else {
                
                for (Material material : Material.values()) {
                    
                    if (material.name().equalsIgnoreCase(m.group(1))) {
                        
                        return new dMaterial(material, data);
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
    
    public dMaterial(Material material, int data) {
        this.material = material;
        if (data < 0)
            this.data = null;
        else this.data = (byte) data;
    }
    
    public dMaterial(int id) {
        this.material = Material.getMaterial(id);
    }
    
    public dMaterial(int id, int data) {
        this.material = Material.getMaterial(id);
        if (data < 0)
            this.data = null;
        else this.data = (byte) data;
    }
    
    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Associated with Bukkit Material

    private Material material;
    private Byte data = 0;

    public Material getMaterial() {
        return material;
    }

    public boolean specifiedData() {
        return data != null;
    }

    public boolean matchesMaterialData(MaterialData data) {
        if (specifiedData())
            return (material == data.getItemType() && data.equals(data.getData()));
        else return material == data.getItemType();
    }
    
    public MaterialData getMaterialData() {
        return new MaterialData(material, data != null ? data : 0);
    }

    String prefix = "material";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return (prefix + "='<A>" + identify() + "<G>'  ");
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getType() {
        return "Material";
    }

    @Override
    public String identify() {
        return "m@" + material.name();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public dObject setPrefix(String prefix) {
        if (prefix != null)
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        return new Element(identify()).getAttribute(attribute);
    }

}
