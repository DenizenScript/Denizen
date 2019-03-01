package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.nms.abstracts.ModernBlockData;
import net.aufdemrand.denizen.nms.interfaces.BlockData;
import net.aufdemrand.denizen.objects.properties.material.MaterialPlantGrowth;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.blocks.OldMaterialsHelper;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.properties.PropertyParser;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.TagContext;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.material.MaterialData;

import java.util.HashMap;

public class dMaterial implements dObject, Adjustable {

    /**
     * Legacy dMaterial identities.
     */
    private String forcedIdentity = null,
            forcedIdentityLow = null;

    /**
     * Legacy dMaterial identities. Do not use.
     */
    public dMaterial forceIdentifyAs(String string) {
        forcedIdentity = string;
        forcedIdentityLow = CoreUtilities.toLowerCase(string);
        return this;
    }


    //////////////////
    //    OBJECT FETCHER
    ////////////////


    public static dMaterial valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a Material Object from a string form.
     *
     * @param string the string
     * @return a Material, or null if incorrectly formatted
     */
    @Fetchable("m")
    public static dMaterial valueOf(String string, TagContext context) {

        ///////
        // Handle objects with properties through the object fetcher
        if (ObjectFetcher.DESCRIBED_PATTERN.matcher(string).matches()) {
            return ObjectFetcher.getObjectFrom(dMaterial.class, string, context);
        }

        string = string.toUpperCase();
        if (string.startsWith("M@")) {
            string = string.substring("M@".length());
        }
        if (string.equals("RANDOM")) {
            return OldMaterialsHelper.getMaterialFrom(Material.values()[CoreUtilities.getRandom().nextInt(Material.values().length)]);
        }
        int index = string.indexOf(',');
        if (index < 0) {
            index = string.indexOf(':');
        }
        int data = 0;
        if (index >= 0) {
            data = aH.getIntegerFrom(string.substring(index + 1));
            string = string.substring(0, index);
        }
        Material m = Material.getMaterial(string);
        if (m == null && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            m = Material.getMaterial(string, true);
            if (m != null) {
                m = Bukkit.getUnsafe().fromLegacy(m);
                if (context == null || context.debug) {
                    dB.log("'" + string + "' is a legacy (pre-1.13) material name. It is now '" + m.name() + "'.");
                }
            }
        }
        if (m != null) {
            if (index >= 0) {
                if (context == null || context.debug) {
                    dB.log("Material ID and data magic number support is deprecated and WILL be removed in a future release. Use relevant properties instead.");
                }
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                return new dMaterial(m);
            }
            return OldMaterialsHelper.getMaterialFrom(m, data);
        }
        dMaterial mat = OldMaterialsHelper.all_dMaterials.get(string);
        if (mat != null) {
            if ((context == null || context.debug) && index >= 0) {
                dB.log("Material ID and data magic number support is deprecated and WILL be removed in a future release. Use relevant properties instead.");
            }
            if (data == 0) {
                if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                    return new dMaterial(mat.material);
                }
                return mat;
            }
            return OldMaterialsHelper.getMaterialFrom(mat.material, data);
        }
        int matid = aH.getIntegerFrom(string);
        if (matid != 0) {
            // It's always an error (except in the 'matches' call) to use a material ID number instead of a name.
            if (context != noDebugContext) {
                dB.echoError("Material ID and data magic number support is deprecated and WILL be removed in a future release. Use material names instead.");
            }
            m = OldMaterialsHelper.getLegacyMaterial(matid);
            if (m != null) {
                return OldMaterialsHelper.getMaterialFrom(m, data);
            }
        }
        return null;
    }

    public static dMaterial quickOfNamed(String string) {
        string = string.toUpperCase();
        int index = string.indexOf(',');
        if (index < 0) {
            index = string.indexOf(':');
        }
        int data = 0;
        if (index >= 0) {
            data = aH.getIntegerFrom(string.substring(index + 1));
            string = string.substring(0, index);
        }
        Material m = Material.getMaterial(string);
        if (m != null) {
            return OldMaterialsHelper.getMaterialFrom(m, data);
        }
        dMaterial mat = OldMaterialsHelper.all_dMaterials.get(string);
        if (mat != null) {
            if (data == 0) {
                return mat;
            }
            return OldMaterialsHelper.getMaterialFrom(mat.material, data);
        }
        return null;
    }

    public static TagContext noDebugContext = new BukkitTagContext(null, null, false, null, false, null);

    /**
     * Determine whether a string is a valid material.
     *
     * @param arg the string
     * @return true if matched, otherwise false
     */
    public static boolean matches(String arg) {
        if (valueOf(arg, noDebugContext) != null) {
            return true;
        }
        return false;
    }

    /**
     * @param object object-fetchable String of a valid dMaterial, or a dMaterial object
     * @return true if the dMaterials are the same.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof dMaterial) {
            return getMaterial() == ((dMaterial) object).getMaterial()
                    && getData((byte) 0) == ((dMaterial) object).getData((byte) 0);
        }
        else {
            dMaterial parsed = valueOf(object.toString());
            return equals(parsed);
        }
    }

    public boolean matchesBlock(Block b) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            return getMaterial() == b.getType();
        }
        return matchesMaterialData(b.getType().getNewData(b.getData()));
    }


    ///////////////
    //   Constructors
    /////////////

    /**
     * Legacy material format. Do not use.
     */
    public dMaterial(Material material, int data) {
        this.material = material;
        if (data < 0) {
            this.data = null;
        }
        else {
            this.data = (byte) data;
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            modernData = new ModernBlockData(material);
        }
    }

    public dMaterial(Material material) {
        this(material, 0);
    }

    public dMaterial(BlockState state) {
        this.material = state.getType();
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            this.modernData = new ModernBlockData(state);
        }
        else {
            this.data = state.getRawData();
        }
    }

    public dMaterial(BlockData block) {
        this.material = block.getMaterial();
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            this.modernData = block.modern();
        }
        else {
            this.data = block.getData();
        }
    }

    public dMaterial(Block block) {
        this.material = block.getType();
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            this.modernData = new ModernBlockData(block);
        }
        else {
            this.data = block.getData();
        }
    }

    public dMaterial(ModernBlockData data) {
        this.modernData = data;
        this.material = data.getMaterial();
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    private Material material;
    private Byte data = 0;
    private ModernBlockData modernData;

    public boolean hasModernData() {
        return modernData != null;
    }

    public ModernBlockData getModernData() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            return modernData;
        }
        throw new IllegalStateException("Modern block data handler is not available prior to MC 1.13.");
    }

    public void setModernData(ModernBlockData data) {
        modernData = data;
    }

    public Material getMaterial() {
        return material;
    }

    public String name() {
        return material.name();
    }


    public byte getData(byte fallback) {
        if (data == null) {
            return fallback;
        }
        else {
            return data;
        }
    }

    public Byte getData() {
        return data;
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean matchesMaterialData(MaterialData data) {
        // If this material has data, check datas
        if (hasData()) {
            return (material == data.getItemType() && this.data == data.getData());
        }

        // Else, return matched itemType/materialType
        else {
            return material == data.getItemType();
        }
    }

    public MaterialData getMaterialData() {
        return new MaterialData(material, data != null ? data : 0);
    }

    public boolean isStructure() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_9_R2)
                && material == Material.CHORUS_PLANT) {
            return true;
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)
                && (material == Material.RED_MUSHROOM_BLOCK || material == Material.BROWN_MUSHROOM_BLOCK)) {
            return true;
        }
        else if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12_R1)) {
            if (material == Material.RED_MUSHROOM || material == Material.BROWN_MUSHROOM) {
                return true;
            }
            String name = material.name();
            return name.equals("SAPLING") || name.equals("HUGE_MUSHROOM_1") || name.equals("HUGE_MUSHROOM_2");
        }
        return false;
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
        return "m@" + identifyNoIdentifier();
    }

    public String identifyFull() {
        return "m@" + identifyFullNoIdentifier();
    }

    @Override
    public String identifySimple() {
        return "m@" + identifySimpleNoIdentifier();
    }

    public String identifyNoPropertiesNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12_R1) && getData() != null && getData() > 0) {
            return CoreUtilities.toLowerCase(material.name()) + "," + getData();
        }
        return CoreUtilities.toLowerCase(material.name());
    }

    public String identifyNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12_R1) && getData() != null && getData() > 0) {
            return CoreUtilities.toLowerCase(material.name()) + "," + getData();
        }
        return CoreUtilities.toLowerCase(material.name()) + PropertyParser.getPropertiesString(this);
    }

    public String identifySimpleNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        return CoreUtilities.toLowerCase(material.name());
    }

    public String identifyFullNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow + (getData() != null ? "," + getData() : "");
        }
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12_R1) && getData() != null && getData() > 0) {
            return CoreUtilities.toLowerCase(material.name()) + "," + getData();
        }
        return CoreUtilities.toLowerCase(material.name()) + PropertyParser.getPropertiesString(this);
    }

    @Override
    public String toString() {
        return identify();
    }

    public String realName() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        return CoreUtilities.toLowerCase(material.name());
    }

    @Override
    public dObject setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    public static void registerTags() {

        registerTag("id", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dB.echoError("Material ID and data magic number support is deprecated and WILL be removed in a future release. Use material names instead.");
                return new Element(((dMaterial) object).material.getId())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        registerTag("data", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (attribute.context == null || attribute.context.debug) {
                    dB.log("Material ID and data magic number support is deprecated and WILL be removed in a future release. Use relevant properties instead.");
                }
                return new Element(((dMaterial) object).getData())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_plant>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a plant block material.
        // When this returns true, <@link tag m@material.plant_growth>,  <@link tag m@material.maximum_plant_growth>,
        // and <@link mechanism dMaterial.plant_growth> are accessible.
        // -->
        registerTag("is_plant", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(MaterialPlantGrowth.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.has_gravity>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is affected by gravity.
        // -->
        registerTag("has_gravity", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.hasGravity())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_block>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a placeable block.
        // -->
        registerTag("is_block", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isBlock())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_burnable>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a block that can burn away.
        // -->
        registerTag("is_burnable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isBurnable())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_edible>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is edible.
        // -->
        registerTag("is_edible", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isEdible())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_flammable>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a block that can catch fire.
        // -->
        registerTag("is_flammable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isFlammable())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_occluding>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a block that completely blocks vision.
        // -->
        registerTag("is_occluding", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isOccluding())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_record>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a playable music disc.
        // -->
        registerTag("is_record", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isRecord())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_solid>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a block that is solid (cannot be walked through).
        // -->
        registerTag("is_solid", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isSolid())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_transparent>
        // @returns Element(Boolean)
        // @description
        // Returns whether the material is a block that does not block any light.
        // -->
        registerTag("is_transparent", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.isTransparent())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.max_durability>
        // @returns Element(Number)
        // @description
        // Returns the maximum durability of this material.
        // -->
        registerTag("max_durability", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.getMaxDurability())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.block_resistance>
        // @returns Element(Decimal)
        // @mechanism dMaterial.block_resistance
        // @description
        // Returns the explosion resistance for all blocks of this material type.
        // -->
        registerTag("block_resistance", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dMaterial material = (dMaterial) object;
                if (!NMSHandler.getInstance().getBlockHelper().hasBlock(material.getMaterial())) {
                    dB.echoError("Provided material does not have a placeable block.");
                    return null;
                }
                return new Element(NMSHandler.getInstance().getBlockHelper().getBlockResistance(material.getMaterial()))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.max_stack_size>
        // @returns Element(Number)
        // @description
        // Returns the maximum amount of this material that can be held in a stack.
        // -->
        registerTag("max_stack_size", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.getMaxStackSize())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.is_made_of[<material>]>
        // @returns Element(Boolean)
        // @description
        // Returns true if the material is a variety of the specified material.
        // Example: <m@red_wool.is_made_of[m@wool]> will return true.
        // Invalid for 1.13+ servers.
        // -->
        registerTag("is_made_of", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dMaterial compared = dMaterial.valueOf(attribute.getContext(1));
                return new Element(compared != null && ((dMaterial) object).material == compared.getMaterial())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.bukkit_enum>
        // @returns Element
        // @description
        // Returns the bukkit Material enum value. For example: <m@birch_sapling.bukkit_enum>
        // will return 'sapling'
        // Unneeded for 1.13+ servers.
        // -->
        registerTag("bukkit_enum", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).material.name())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.name>
        // @returns Element
        // @description
        // Returns the name of the material.
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dMaterial) object).forcedIdentity != null ? ((dMaterial) object).forcedIdentityLow :
                        CoreUtilities.toLowerCase(((dMaterial) object).material.name()))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <m@material.full>
        // @returns Element
        // @description
        // Returns the material's full identification.
        // Irrelevant on modern (1.13+) servers.
        // -->
        registerTag("full", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (((dMaterial) object).hasData()) {
                    return new Element(((dMaterial) object).identifyFull())
                            .getAttribute(attribute.fulfill(1));
                }
                else {
                    return new Element(((dMaterial) object).identify())
                            .getAttribute(attribute.fulfill(1));
                }
            }
        });

        // <--[tag]
        // @attribute <m@material.item>
        // @returns dItem
        // @description
        // Returns an item of the material.
        // -->
        registerTag("item", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                dMaterial material = (dMaterial) object;
                dItem item = new dItem(material, 1);
                attribute = attribute.fulfill(1);
                if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                    // Special patch for older material-item tags.
                    if (!attribute.isComplete()) {
                        String tag = attribute.getAttribute(1);
                        String returned = CoreUtilities.autoPropertyTag(object, attribute);
                        if (returned != null) {
                            dB.echoError("Usage of outdated 'material.item." + tag + "' tag should be replaced by 'material." + tag + "' (with '.item' removed).");
                            return returned;
                        }
                    }
                    if (item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta() instanceof BlockStateMeta) {
                        ((BlockStateMeta) item.getItemStack().getItemMeta()).setBlockState(material.modernData.getBlockState());
                    }
                }
                return item.getAttribute(attribute);
            }
        });

        // <--[tag]
        // @attribute <m@material.type>
        // @returns Element
        // @description
        // Always returns 'Material' for dMaterial objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element("Material").getAttribute(attribute.fulfill(1));
            }
        });

    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<String, TagRunnable>();

    public static void registerTag(String name, TagRunnable runnable) {
        if (runnable.name == null) {
            runnable.name = name;
        }
        registeredTags.put(name, runnable);
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // TODO: Scrap getAttribute, make this functionality a core system
        String attrLow = CoreUtilities.toLowerCase(attribute.getAttributeWithoutContext(1));
        TagRunnable tr = registeredTags.get(attrLow);
        if (tr != null) {
            if (!tr.name.equals(attrLow)) {
                net.aufdemrand.denizencore.utilities.debugging.dB.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dMaterial
        // @name block_resistance
        // @input Element(Decimal)
        // @description
        // Sets the explosion resistance for all blocks of this material type.
        // @tags
        // <m@material.block_resistance>
        // -->
        if (!mechanism.isProperty && mechanism.matches("block_resistance") && mechanism.requireFloat()) {
            if (!NMSHandler.getInstance().getBlockHelper().setBlockResistance(material, mechanism.getValue().asFloat())) {
                dB.echoError("Provided material does not have a placeable block.");
            }
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
