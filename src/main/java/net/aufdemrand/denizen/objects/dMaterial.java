package net.aufdemrand.denizen.objects;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import net.aufdemrand.denizen.tags.Attribute;

public class dMaterial implements dObject {

    final static Pattern materialPattern = Pattern.compile("(?:m@)?(\\w+)[,:]?(\\d+)?", Pattern.CASE_INSENSITIVE);

    // Will be called a lot, no need to construct/deconstruct.
    public final static dMaterial AIR = dMaterial.valueOf("m@air");


    /////////////////
    // dMaterial Varieties
    ///////////////

    // dMaterial 'extra materials' for making 'data variety' materials easier to work with. Register materials
    // that aren't included in the bukkit Material enum here to make lookup easier.

    public static enum dMaterials { WHITE_WOOL, ORANGE_WOOL, MAGENTA_WOOL, LIGHT_BLUE_WOOL, YELLOW_WOOL,
        LIME_WOOL, PINK_WOOL, GRAY_WOOL, LIGHT_GRAY_WOOL, CYAN_WOOL, PURPLE_WOOL, BLUE_WOOL, BROWN_WOOL,
        GREEN_WOOL, RED_WOOL, BLACK_WOOL, WHITE_CARPET, ORANGE_CARPET, MAGENTA_CARPET, LIGHT_BLUE_CARPET,
        YELLOW_CARPET, LIME_CARPET, PINK_CARPET, GRAY_CARPET, LIGHT_GRAY_CARPET, CYAN_CARPET, PURPLE_CARPET,
        BLUE_CARPET, BROWN_CARPET, GREEN_CARPET, RED_CARPET, BLACK_CARPET, WHITE_CLAY, ORANGE_CLAY,
        MAGENTA_CLAY, LIGHT_BLUE_CLAY, YELLOW_CLAY, LIME_CLAY, PINK_CLAY, GRAY_CLAY, LIGHT_GRAY_CLAY,
        CYAN_CLAY, PURPLE_CLAY, BLUE_CLAY, BROWN_CLAY, GREEN_CLAY, RED_CLAY, BLACK_CLAY, NATURAL_COAL,
        CHARCOAL, OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, OAK_SAPLING, SPRUCE_SAPLING,
        BIRCH_SAPLING, JUNGLE_SAPLING, OAK_LEAVES, SPRUCE_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES,
        NATURAL_SANDSTONE, CHISELED_SANDSTONE, SMOOTH_SANDSTONE }

    // dMaterials are just made and disposed of for standard 'Materials', but these we will keep around since
    // they are special :)

    public final static dMaterial WHITE_WOOL = dMaterial.valueOf("m@wool,0").forceIdentifyAs("WHITE_WOOL");
    public final static dMaterial ORANGE_WOOL = dMaterial.valueOf("m@wool,1").forceIdentifyAs("ORANGE_WOOL");
    public final static dMaterial MAGENTA_WOOL = dMaterial.valueOf("m@wool,2").forceIdentifyAs("MAGENTA_WOOL");
    public final static dMaterial LIGHT_BLUE_WOOL = dMaterial.valueOf("m@wool,3").forceIdentifyAs("LIGHT_BLUE_WOOL");
    public final static dMaterial YELLOW_WOOL = dMaterial.valueOf("m@wool,4").forceIdentifyAs("YELLOW_WOOL");
    public final static dMaterial LIME_WOOL = dMaterial.valueOf("m@wool,5").forceIdentifyAs("LIME_WOOL");
    public final static dMaterial PINK_WOOL = dMaterial.valueOf("m@wool,6").forceIdentifyAs("PINK_WOOL");
    public final static dMaterial GRAY_WOOL = dMaterial.valueOf("m@wool,7").forceIdentifyAs("GRAY_WOOL");
    public final static dMaterial LIGHT_GRAY_WOOL = dMaterial.valueOf("m@wool,8").forceIdentifyAs("LIGHT_GRAY_WOOL");
    public final static dMaterial CYAN_WOOL = dMaterial.valueOf("m@wool,9").forceIdentifyAs("CYAN_WOOL");
    public final static dMaterial PURPLE_WOOL = dMaterial.valueOf("m@wool,10").forceIdentifyAs("PURPLE_WOOL");
    public final static dMaterial BLUE_WOOL = dMaterial.valueOf("m@wool,11").forceIdentifyAs("BLUE_WOOL");
    public final static dMaterial BROWN_WOOL = dMaterial.valueOf("m@wool,12").forceIdentifyAs("BROWN_WOOL");
    public final static dMaterial GREEN_WOOL = dMaterial.valueOf("m@wool,13").forceIdentifyAs("GREEN_WOOL");
    public final static dMaterial RED_WOOL = dMaterial.valueOf("m@wool,14").forceIdentifyAs("RED_WOOL");
    public final static dMaterial BLACK_WOOL = dMaterial.valueOf("m@wool,15").forceIdentifyAs("BLACK_WOOL");

    public final static dMaterial WHITE_CARPET = dMaterial.valueOf("m@carpet,0").forceIdentifyAs("WHITE_CARPET");
    public final static dMaterial ORANGE_CARPET = dMaterial.valueOf("m@carpet,1").forceIdentifyAs("ORANGE_CARPET");
    public final static dMaterial MAGENTA_CARPET = dMaterial.valueOf("m@carpet,2").forceIdentifyAs("MAGENTA_CARPET");
    public final static dMaterial LIGHT_BLUE_CARPET = dMaterial.valueOf("m@carpet,3").forceIdentifyAs("LIGHT_BLUE_CARPET");
    public final static dMaterial YELLOW_CARPET = dMaterial.valueOf("m@carpet,4").forceIdentifyAs("YELLOW_CARPET");
    public final static dMaterial LIME_CARPET = dMaterial.valueOf("m@carpet,5").forceIdentifyAs("LIME_CARPET");
    public final static dMaterial PINK_CARPET = dMaterial.valueOf("m@carpet,6").forceIdentifyAs("PINK_CARPET");
    public final static dMaterial GRAY_CARPET = dMaterial.valueOf("m@carpet,7").forceIdentifyAs("GRAY_CARPET");
    public final static dMaterial LIGHT_GRAY_CARPET = dMaterial.valueOf("m@carpet,8").forceIdentifyAs("LIGHT_GRAY_CARPET");
    public final static dMaterial CYAN_CARPET = dMaterial.valueOf("m@carpet,9").forceIdentifyAs("CYAN_CARPET");
    public final static dMaterial PURPLE_CARPET = dMaterial.valueOf("m@carpet,10").forceIdentifyAs("PURPLE_CARPET");
    public final static dMaterial BLUE_CARPET = dMaterial.valueOf("m@carpet,11").forceIdentifyAs("BLUE_CARPET");
    public final static dMaterial BROWN_CARPET = dMaterial.valueOf("m@carpet,12").forceIdentifyAs("BROWN_CARPET");
    public final static dMaterial GREEN_CARPET = dMaterial.valueOf("m@carpet,13").forceIdentifyAs("GREEN_CARPET");
    public final static dMaterial RED_CARPET =  dMaterial.valueOf("m@carpet,14").forceIdentifyAs("RED_CARPET");
    public final static dMaterial BLACK_CARPET = dMaterial.valueOf("m@carpet,15").forceIdentifyAs("BLACK_CARPET");

    public final static dMaterial WHITE_CLAY = dMaterial.valueOf("m@clay,0").forceIdentifyAs("WHITE_CLAY");
    public final static dMaterial ORANGE_CLAY = dMaterial.valueOf("m@clay,1").forceIdentifyAs("ORANGE_CLAY");
    public final static dMaterial MAGENTA_CLAY = dMaterial.valueOf("m@clay,2").forceIdentifyAs("MAGENTA_CLAY");
    public final static dMaterial LIGHT_BLUE_CLAY = dMaterial.valueOf("m@clay,3").forceIdentifyAs("LIGHT_BLUE_CLAY");
    public final static dMaterial YELLOW_CLAY = dMaterial.valueOf("m@clay,4").forceIdentifyAs("YELLOW_CLAY");
    public final static dMaterial LIME_CLAY = dMaterial.valueOf("m@clay,5").forceIdentifyAs("LIME_CLAY");
    public final static dMaterial PINK_CLAY = dMaterial.valueOf("m@clay,6").forceIdentifyAs("PINK_CLAY");
    public final static dMaterial GRAY_CLAY = dMaterial.valueOf("m@clay,7").forceIdentifyAs("GRAY_CLAY");
    public final static dMaterial LIGHT_GRAY_CLAY = dMaterial.valueOf("m@clay,8").forceIdentifyAs("LIGHT_GRAY_CLAY");
    public final static dMaterial CYAN_CLAY = dMaterial.valueOf("m@clay,9").forceIdentifyAs("CYAN_CLAY");
    public final static dMaterial PURPLE_CLAY = dMaterial.valueOf("m@clay,10").forceIdentifyAs("PURPLE_CLAY");
    public final static dMaterial BLUE_CLAY = dMaterial.valueOf("m@clay,11").forceIdentifyAs("BLUE_CLAY");
    public final static dMaterial BROWN_CLAY = dMaterial.valueOf("m@clay,12").forceIdentifyAs("BROWN_CLAY");
    public final static dMaterial GREEN_CLAY = dMaterial.valueOf("m@clay,13").forceIdentifyAs("GREEN_CLAY");
    public final static dMaterial RED_CLAY = dMaterial.valueOf("m@clay,14").forceIdentifyAs("RED_CLAY");
    public final static dMaterial BLACK_CLAY = dMaterial.valueOf("m@clay,15").forceIdentifyAs("BLACK_CLAY");

    public final static dMaterial NATURAL_COAL = dMaterial.valueOf("m@coal,0").forceIdentifyAs("NATURAL_COAL");
    public final static dMaterial CHARCOAL = dMaterial.valueOf("m@coal,1").forceIdentifyAs("CHARCOAL");

    public final static dMaterial OAK_PLANKS = dMaterial.valueOf("m@wood,0").forceIdentifyAs("OAK_PLANKS");
    public final static dMaterial SPRUCE_PLANKS = dMaterial.valueOf("m@wood,1").forceIdentifyAs("SPRUCE_PLANKS");
    public final static dMaterial BIRCH_PLANKS = dMaterial.valueOf("m@wood,2").forceIdentifyAs("BIRCH_PLANKS");
    public final static dMaterial JUNGLE_PLANKS = dMaterial.valueOf("m@wood,3").forceIdentifyAs("JUNGLE_PLANKS");

    public final static dMaterial OAK_SAPLING = dMaterial.valueOf("m@sapling,0").forceIdentifyAs("OAK_SAPLING");
    public final static dMaterial SPRUCE_SAPLING = dMaterial.valueOf("m@sapling,1").forceIdentifyAs("SPRUCE_SAPLING");
    public final static dMaterial BIRCH_SAPLING = dMaterial.valueOf("m@sapling,2").forceIdentifyAs("BIRCH_SAPLING");
    public final static dMaterial JUNGLE_SAPLING = dMaterial.valueOf("m@sapling,3").forceIdentifyAs("JUNGLE_SAPLING");

    public final static dMaterial OAK_LEAVES = dMaterial.valueOf("m@leaves,0").forceIdentifyAs("OAK_LEAVES");
    public final static dMaterial SPRUCE_LEAVES = dMaterial.valueOf("m@leaves,1").forceIdentifyAs("SPRUCE_LEAVES");
    public final static dMaterial BIRCH_LEAVES = dMaterial.valueOf("m@leaves,2").forceIdentifyAs("BIRCH_LEAVES");
    public final static dMaterial JUNGLE_LEAVES = dMaterial.valueOf("m@leaves,3").forceIdentifyAs("JUNGLE_LEAVES");

    public final static dMaterial NATURAL_SANDSTONE = dMaterial.valueOf("m@sandstone,0").forceIdentifyAs("NATURAL_SANDSTONE");
    public final static dMaterial CHISELED_SANDSTONE = dMaterial.valueOf("m@sandstone,1").forceIdentifyAs("CHISELED_SANDSTONE");
    public final static dMaterial SMOOTH_SANDSTONE = dMaterial.valueOf("m@sandstone,2").forceIdentifyAs("SMOOTH_SANDSTONE");

    // TODO: Add bricks, potions, logs, etc.


    // Built on startup's call to initialize_ based on the dMaterials enum and available
    // 'final static' dMaterial fields.
    // valueOf and getMaterialFrom will check this to turn 'wool,1' into 'orange_wool'
    public static Map<Material, Map<Integer, dMaterial>> material_varieties = new HashMap<Material, Map<Integer, dMaterial>>();

    /**
     * Registers a dMaterial as a 'variety'. Upon construction of a dMaterial, this
     * registry will be checked to see if a variety can be used instead of the traditional
     * enum/data format.
     *
     * dMaterials in this list should probably 'forceIdentifyAs'.
     *
     * @param material the dMaterial variety
     * @return the dMaterial registered
     */
    public static dMaterial registerVariety(dMaterial material) {
        Map<Integer, dMaterial> entry;
        // Get any existing entries for the Material, or make a new HashMap for entries.
        if (material_varieties.containsKey(material.getMaterial()))
            entry = material_varieties.get(material.getMaterial());
        else entry = new HashMap<Integer, dMaterial>();
        // Put in new entry
        entry.put((int) material.data, material);
        // Return the dMaterial
        material_varieties.put(material.getMaterial(), entry);
        return material;
    }

    // Called on startup
    public static void _initialize() {
        for (dMaterials material : dMaterials.values()) {
            try {
                Field field = dMaterial.class.getField(material.name());
                dMaterial mat = (dMaterial) field.get(null);
                registerVariety(mat);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // dMaterials that are registered as a 'variety' will need to identify as
    // something more specific than the traditional enum/data information.
    private String forcedIdentity = null;

    /**
     * Forces the dMaterial to identify as something other than the enum value
     * of the material. This should be used on materials that are being registered
     * as a variety.
     *
     * @param string the name of the new identity
     * @return the identified dMaterial
     */
    private dMaterial forceIdentifyAs(String string) {
        forcedIdentity = string;
        return this;
    }


    /**
     * Gets a dMaterial from a bukkit Material.
     *
     * @param material the bukkit Material
     * @return a dMaterial representation of the Material
     */
    public static dMaterial getMaterialFrom(Material material) {
        return getMaterialFrom(material, 0);
    }

    /**
     * Gets a dMaterial from a bukkit Material/Data. dMaterials can identify
     * as something more straight-forward than the traditional material,data format.
     * Example: wool,1 would return the ORANGE_WOOL dMaterial.
     *
     * @param material
     * @param data
     * @return
     */
    public static dMaterial getMaterialFrom(Material material, int data) {
        if (material == Material.AIR) return AIR;
        if (material_varieties.containsKey(material)) {
            if (material_varieties.get(material).containsKey(data))
                return material_varieties.get(material).get(data);
        }

        return new dMaterial(material, data);
    }



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

            } else {
                // Iterate through Materials
                for (Material material : Material.values()) {
                    if (material.name().equalsIgnoreCase(m.group(1))) {
                        return new dMaterial(material, data);
                    }
                }

                // Iterate through dMaterials
                for (dMaterials material : dMaterials.values()) {
                    if (material.name().equalsIgnoreCase(m.group(1))) {
                        try {
                            Field field = dMaterial.class.getField(material.name());
                            field.setAccessible(true);
                            // Should be pretty safe, unless there's an enum without a matching field.
                            return (dMaterial) field.get(null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
     * @param arg  the string
     * @return  true if matched, otherwise false
     *
     */
    public static boolean matches(String arg) {
        if (arg.toUpperCase().matches("(?:m@)?RANDOM"))
            return true;

        Matcher m = materialPattern.matcher(arg);
        return m.matches();
    }

    /**
     * TODO: Needs testing.
     *
     * @param object object-fetchable String of a valid dMaterial, or a dMaterial object
     * @return true if the dMaterials are the same.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof dMaterial)
            if (((dMaterial) object).identify() == this.identify())
                return true;
            else return false;

        if (valueOf(object.toString()) != null
                && valueOf(object.toString()).identify() == this.identify())
            return true;

        return false;
    }


    ///////////////
    //   Constructors
    /////////////

    private dMaterial(Material material, int data) {
        this.material = material;

        if (data < 0) this.data = null;
        else this.data = (byte) data;
    }

    private dMaterial(Material material) {
        this(material, 0);
    }

    private dMaterial(int id, int data) {
        this(Material.getMaterial(id), data);
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
            return (material == data.getItemType() && data.getData() == data.getData());
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
        if (forcedIdentity != null) return "m@" + forcedIdentity.toLowerCase();
        return "m@" + material.name().toLowerCase();
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
        // Returns whether the material is affected by gravity.
        // -->
        if (attribute.startsWith("has_gravity"))
            return new Element(material.hasGravity())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.id>
        // @returns Element(integer)
        // @description
        // Returns the material's ID.
        // -->
        if (attribute.startsWith("id"))
            return new Element(material.getId())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_block>
        // @returns Element(boolean)
        // @description
        // Returns whether the material is a placeable block.
        // -->
        if (attribute.startsWith("is_block"))
            return new Element(material.isBlock())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_burnable>
        // @returns Element(boolean)
        // @description
        // Returns whether the material is a block that can burn away.
        // -->
        if (attribute.startsWith("is_burnable"))
            return new Element(material.isBurnable())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_edible>
        // @returns Element(boolean)
        // @description
        // Returns whether the material is edible.
        // -->
        if (attribute.startsWith("is_edible"))
            return new Element(material.isEdible())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_flammable>
        // @returns Element(boolean)
        // @description
        // Returns whether the material is a block that can catch fire.
        // -->
        if (attribute.startsWith("is_flammable"))
            return new Element(material.isFlammable())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_occluding>
        // @returns Element(boolean)
        // @description
        // Returns whether the material is a block that completely blocks vision.
        // -->
        if (attribute.startsWith("is_occluding"))
            return new Element(material.isOccluding())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_record>
        // @returns Element(boolean)
        // @description
        // Returns whether the material is a playable music disc.
        // -->
        if (attribute.startsWith("is_record"))
            return new Element(material.isRecord())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_solid>
        // @returns Element(boolean)
        // @description
        // Returns whether the material is a block that is solid (cannot be walked through).
        // -->
        if (attribute.startsWith("is_solid"))
            return new Element(material.isSolid())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_transparent>
        // @returns Element(boolean)
        // @description
        // Returns whether the material is a block that does not block any light.
        // -->
        if (attribute.startsWith("is_transparent"))
            return new Element(material.isTransparent())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.max_durability>
        // @returns Element(integer)
        // @description
        // Returns the maximum durability of this material.
        // -->
        if (attribute.startsWith("max_durability"))
            return new Element(material.getMaxDurability())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.max_stack_size>
        // @returns Element(integer)
        // @description
        // Returns the maximum amount of this material that can be held in a stack.
        // -->
        if (attribute.startsWith("max_stack_size"))
            return new Element(material.getMaxStackSize())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.is_made_of[<material>]>
        // @returns Element(boolean)
        // @description
        // Returns true if the material is a variety of the specified material.
        // Example: <m@red_wool.is_made_of[m@wool]> will return true.
        // -->
        if (attribute.startsWith("is_made_of")) {
            dMaterial compared = dMaterial.valueOf(attribute.getContext(1));
            if (compared == null) return Element.FALSE.getAttribute(attribute.fulfill(1));
            else return new Element(material.name().equalsIgnoreCase(compared.getMaterial().name()))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <m@material.bukkit_enum>
        // @returns Element
        // @description
        // Returns the bukkit Material enum value. For example: <m@birch_sapling.bukkit_enum>
        // will return 'sapling'
        // -->
        if (attribute.startsWith("bukkit_enum"))
            return new Element(material.name())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <m@material.data>
        // @returns Element(Number)
        // @description
        // Returns the bukkit Material data value. For example: <m@red_clay.data>
        // will return '14'. Note: This kind of 'material identification' has been deprecated
        // by bukkit and should be used sparingly.
        // -->
        if (attribute.startsWith("data"))
            return new Element(material.name())
                    .getAttribute(attribute.fulfill(1));

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
