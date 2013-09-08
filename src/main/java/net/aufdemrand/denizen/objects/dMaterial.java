package net.aufdemrand.denizen.objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.utilities.Utilities;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import net.aufdemrand.denizen.tags.Attribute;

public class dMaterial implements dObject {

    final static Pattern materialPattern = Pattern.compile("(?:m@)?(\\w+)[,:]?(\\d+)?", Pattern.CASE_INSENSITIVE);

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

        return m.matches();

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

    public String name() {
        return material.name();
    }

    public Byte getData() {
        return data;
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean matchesMaterialData(MaterialData data) {
        if (hasData())
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
    public String getObjectType() {
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

        // <--[tag]
        // @attribute <m@material.has_gravity>
        // @returns Element(boolean)
        // @description
        // Returns true if the material is affected by gravity
        // -->
        if (attribute.startsWith("has_gravity"))
            return new Element(material.hasGravity())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.id>
        // @returns Element(integer)
        // @description
        // Returns the material's ID
        // -->
        if (attribute.startsWith("id"))
            return new Element(material.getId())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_block>
        // @returns Element(boolean)
        // @description
        // Returns true if the material is a placeable block
        // -->
        if (attribute.startsWith("is_block"))
            return new Element(material.isBlock())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_burnable>
        // @returns Element(boolean)
        // @description
        // Returns true if the material is a block and can burn away
        // -->
        if (attribute.startsWith("is_burnable"))
            return new Element(material.isBurnable())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_edible>
        // @returns Element(boolean)
        // @description
        // Returns true if the material is edible
        // -->
        if (attribute.startsWith("is_edible"))
            return new Element(material.isEdible())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_flammable>
        // @returns Element(boolean)
        // @description
        // Returns true if the material is a block and can catch fire
        // -->
        if (attribute.startsWith("is_flammable"))
            return new Element(material.isFlammable())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_occluding>
        // @returns Element(boolean)
        // @description
        // Returns true if the material is a block and completely blocks vision
        // -->
        if (attribute.startsWith("is_occluding"))
            return new Element(material.isOccluding())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_record>
        // @returns Element(boolean)
        // @description
        // Returns true if the material is a playable music disc
        // -->
        if (attribute.startsWith("is_record"))
            return new Element(material.isRecord())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_solid>
        // @returns Element(boolean)
        // @description
        // Returns true if the material is a block and solid (cannot be passed through)
        // -->
        if (attribute.startsWith("is_solid"))
            return new Element(material.isSolid())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_transparent>
        // @returns Element(boolean)
        // @description
        // Returns true if the material is a block and does not block any light
        // -->
        if (attribute.startsWith("is_transparent"))
            return new Element(material.isTransparent())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.max_durability>
        // @returns Element(integer)
        // @description
        // Returns the maximum durability of this material
        // -->
        if (attribute.startsWith("max_durability"))
            return new Element(material.getMaxDurability())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.max_stack_size>
        // @returns Element(integer)
        // @description
        // Returns the maximum amount of this material that can be held in a stack
        // -->
        if (attribute.startsWith("max_stack_size"))
            return new Element(material.getMaxStackSize())
                    .getAttribute(attribute.fulfill(1));

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
