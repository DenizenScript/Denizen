package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.properties.material.MaterialAge;
import com.denizenscript.denizen.objects.properties.material.MaterialHalf;
import com.denizenscript.denizen.objects.properties.material.MaterialSlab;
import com.denizenscript.denizen.objects.properties.material.MaterialLevel;
import com.denizenscript.denizen.objects.properties.material.MaterialLightable;
import com.denizenscript.denizen.utilities.blocks.OldMaterialsHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.utilities.blocks.ModernBlockData;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.List;

public class MaterialTag implements ObjectTag, Adjustable {

    // <--[language]
    // @name MaterialTag
    // @group Object System
    // @description
    // A MaterialTag represents a material (a type of block or item).
    //
    // For format info, see <@link language m@>
    //
    // -->

    // <--[language]
    // @name m@
    // @group Object Fetcher System
    // @description
    // m@ refers to the 'object identifier' of a MaterialTag. The 'm@' is notation for Denizen's Object
    // Fetcher. The constructor for a MaterialTag is the material type name.
    // For example, 'm@stick'.
    //
    // For general info, see <@link language MaterialTag>
    //
    // -->

    /**
     * Legacy MaterialTag identities.
     */
    private String forcedIdentity = null,
            forcedIdentityLow = null;

    /**
     * Legacy MaterialTag identities. Do not use.
     */
    public MaterialTag forceIdentifyAs(String string) {
        forcedIdentity = string;
        forcedIdentityLow = CoreUtilities.toLowerCase(string);
        return this;
    }


    //////////////////
    //    OBJECT FETCHER
    ////////////////


    public static MaterialTag valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a Material Object from a string form.
     *
     * @param string the string
     * @return a Material, or null if incorrectly formatted
     */
    @Fetchable("m")
    public static MaterialTag valueOf(String string, TagContext context) {

        ///////
        // Handle objects with properties through the object fetcher
        if (ObjectFetcher.DESCRIBED_PATTERN.matcher(string).matches()) {
            return ObjectFetcher.getObjectFrom(MaterialTag.class, string, context);
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
            data = ArgumentHelper.getIntegerFrom(string.substring(index + 1));
            string = string.substring(0, index);
        }
        Material m = Material.getMaterial(string);
        if (m == null && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            m = Material.getMaterial(string, true);
            if (m != null) {
                m = Bukkit.getUnsafe().fromLegacy(m);
                if (context == null || context.debug) {
                    Debug.log("'" + string + "' is a legacy (pre-1.13) material name. It is now '" + m.name() + "'.");
                }
            }
        }
        if (m != null) {
            if (index >= 0) {
                if (context != noDebugContext && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                    Deprecations.materialIdsSuggestProperties.warn(context);
                }
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                return new MaterialTag(m);
            }
            return OldMaterialsHelper.getMaterialFrom(m, data);
        }
        if (OldMaterialsHelper.all_dMaterials != null) {
            MaterialTag mat = OldMaterialsHelper.all_dMaterials.get(string);
            if (mat != null) {
                if (index >= 0 && context != noDebugContext && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                    Deprecations.materialIdsSuggestProperties.warn(context);
                }
                if (data == 0) {
                    if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                        return new MaterialTag(mat.material);
                    }
                    return mat;
                }
                return OldMaterialsHelper.getMaterialFrom(mat.material, data);
            }
        }
        int matid = ArgumentHelper.getIntegerFrom(string);
        if (matid != 0) {
            // It's always an error (except in the 'matches' call) to use a material ID number instead of a name.
            if (context != noDebugContext) {
                Deprecations.materialIdsSuggestNames.warn(context);
            }
            m = OldMaterialsHelper.getLegacyMaterial(matid);
            if (m != null) {
                return OldMaterialsHelper.getMaterialFrom(m, data);
            }
        }
        return null;
    }

    public static MaterialTag quickOfNamed(String string) {
        string = string.toUpperCase();
        int index = string.indexOf(',');
        if (index < 0) {
            index = string.indexOf(':');
        }
        int data = 0;
        if (index >= 0) {
            data = ArgumentHelper.getIntegerFrom(string.substring(index + 1));
            string = string.substring(0, index);
        }
        Material m = Material.getMaterial(string);
        if (m != null) {
            return OldMaterialsHelper.getMaterialFrom(m, data);
        }
        MaterialTag mat = OldMaterialsHelper.all_dMaterials.get(string);
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
     * @param object object-fetchable String of a valid MaterialTag, or a MaterialTag object
     * @return true if the MaterialTags are the same.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof MaterialTag) {
            return getMaterial() == ((MaterialTag) object).getMaterial()
                    && getData((byte) 0) == ((MaterialTag) object).getData((byte) 0);
        }
        else {
            MaterialTag parsed = valueOf(object.toString());
            return equals(parsed);
        }
    }

    public boolean matchesBlock(Block b) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
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
    public MaterialTag(Material material, int data) {
        this.material = material;
        if (data < 0) {
            this.data = null;
        }
        else {
            this.data = (byte) data;
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)
                && material.isBlock()) {
            modernData = new ModernBlockData(material);
        }
    }

    public MaterialTag(Material material) {
        this(material, 0);
    }

    public MaterialTag(BlockState state) {
        this.material = state.getType();
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            this.modernData = new ModernBlockData(state);
        }
        else {
            this.data = state.getRawData();
        }
    }

    public MaterialTag(BlockData block) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            this.modernData = block.modern();
            this.material = modernData.getMaterial();
        }
        else {
            this.material = block.getMaterial();
            this.data = block.getData();
        }
    }

    public MaterialTag(Block block) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
            this.modernData = new ModernBlockData(block);
            this.material = modernData.getMaterial();
        }
        else {
            this.material = block.getType();
            this.data = block.getData();
        }
    }

    public MaterialTag(ModernBlockData data) {
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
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
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

    public BlockData getNmsBlockData() {
        if (modernData != null) {
            return NMSHandler.getBlockHelper().getBlockData(modernData);
        }
        return NMSHandler.getBlockHelper().getBlockData(getMaterial(), getData((byte) 0));
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
        if (material == Material.CHORUS_PLANT) {
            return true;
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)
                && (material == Material.RED_MUSHROOM_BLOCK || material == Material.BROWN_MUSHROOM_BLOCK)) {
            return true;
        }
        else if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12)) {
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
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12) && getData() != null && getData() > 0) {
            return CoreUtilities.toLowerCase(material.name()) + "," + getData();
        }
        return CoreUtilities.toLowerCase(material.name());
    }

    public String identifyNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
        }
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12) && getData() != null && getData() > 0) {
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
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_12) && getData() != null && getData() > 0) {
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
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    public static void registerTags() {

        registerTag("id", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                Deprecations.materialIdsSuggestNames.warn(attribute.getScriptEntry());
                return new ElementTag(((MaterialTag) object).material.getId())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        registerTag("data", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                if (attribute.context == null || attribute.context.debug) {
                    Deprecations.materialIdsSuggestProperties.warn(attribute.getScriptEntry());
                }
                return new ElementTag(((MaterialTag) object).getData())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_ageable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is an ageable material.
        // When this returns true, <@link tag MaterialTag.age>, <@link tag MaterialTag.maximum_age>,
        // and <@link mechanism MaterialTag.age> are accessible.
        // -->
        registerTag("is_ageable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(MaterialAge.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        registerTag("is_plant", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(MaterialAge.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_directional>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a is_directional material.
        // When this returns true, <@link tag MaterialTag.direction>, <@link tag MaterialTag.valid_directions>,
        // and <@link mechanism MaterialTag.direction> are accessible.
        // -->
        registerTag("is_bisected", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(MaterialHalf.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_bisected>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a bisected material.
        // When this returns true, <@link tag MaterialTag.half>,
        // and <@link mechanism MaterialTag.half> are accessible.
        // -->
        registerTag("is_bisected", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(MaterialHalf.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_slab>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a slab.
        // When this returns true, <@link tag MaterialTag.slab_type>,
        // and <@link mechanism MaterialTag.slab_type> are accessible.
        // -->
        registerTag("is_slab", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(MaterialSlab.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_levelable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a levelable material.
        // When this returns true, <@link tag MaterialTag.level>, <@link tag MaterialTag.maximum_level>,
        // and <@link mechanism MaterialTag.level> are accessible.
        // -->
        registerTag("is_levelable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(MaterialLevel.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_lightable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a lightable material.
        // When this returns true, <@link tag MaterialTag.lit>,
        // and <@link mechanism MaterialTag.lit> are accessible.
        // -->
        registerTag("is_lightable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(MaterialLightable.describes(object))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.has_gravity>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is affected by gravity.
        // -->
        registerTag("has_gravity", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.hasGravity())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_block>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a placeable block.
        // -->
        registerTag("is_block", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.isBlock())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_burnable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that can burn away.
        // -->
        registerTag("is_burnable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.isBurnable())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_edible>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is edible.
        // -->
        registerTag("is_edible", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.isEdible())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_flammable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that can catch fire.
        // -->
        registerTag("is_flammable", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.isFlammable())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_occluding>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that completely blocks vision.
        // -->
        registerTag("is_occluding", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.isOccluding())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_record>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a playable music disc.
        // -->
        registerTag("is_record", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.isRecord())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_solid>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that is solid (cannot be walked through).
        // -->
        registerTag("is_solid", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.isSolid())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_transparent>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that does not block any light.
        // -->
        registerTag("is_transparent", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.isTransparent())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.max_durability>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum durability of this material.
        // -->
        registerTag("max_durability", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.getMaxDurability())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.block_resistance>
        // @returns ElementTag(Decimal)
        // @mechanism MaterialTag.block_resistance
        // @description
        // Returns the explosion resistance for all blocks of this material type.
        // -->
        registerTag("block_resistance", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                MaterialTag material = (MaterialTag) object;
                if (!NMSHandler.getBlockHelper().hasBlock(material.getMaterial())) {
                    Debug.echoError("Provided material does not have a placeable block.");
                    return null;
                }
                return new ElementTag(NMSHandler.getBlockHelper().getBlockResistance(material.getMaterial()))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.hardness>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the value representing how hard a material, used as a basis for calculating the time it takes to break.
        // -->
        registerTag("hardness", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                MaterialTag material = (MaterialTag) object;
                if (!material.getMaterial().isBlock()) {
                    Debug.echoError("Provided material does not have a placeable block.");
                    return null;
                }
                return new ElementTag(material.getMaterial().getHardness())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.max_stack_size>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum amount of this material that can be held in a stack.
        // -->
        registerTag("max_stack_size", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.getMaxStackSize())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.is_made_of[<material>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the material is a variety of the specified material.
        // Example: <m@red_wool.is_made_of[m@wool]> will return true.
        // Invalid for 1.13+ servers.
        // -->
        registerTag("is_made_of", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                MaterialTag compared = MaterialTag.valueOf(attribute.getContext(1));
                return new ElementTag(compared != null && ((MaterialTag) object).material == compared.getMaterial())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.bukkit_enum>
        // @returns ElementTag
        // @description
        // Returns the bukkit Material enum value. For example: <m@birch_sapling.bukkit_enum>
        // will return 'sapling'
        // Unneeded for 1.13+ servers.
        // -->
        registerTag("bukkit_enum", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).material.name())
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.name>
        // @returns ElementTag
        // @description
        // Returns the name of the material.
        // -->
        registerTag("name", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag(((MaterialTag) object).forcedIdentity != null ? ((MaterialTag) object).forcedIdentityLow :
                        CoreUtilities.toLowerCase(((MaterialTag) object).material.name()))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.full>
        // @returns ElementTag
        // @description
        // Returns the material's full identification.
        // Irrelevant on modern (1.13+) servers.
        // -->
        registerTag("full", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                if (((MaterialTag) object).hasData()) {
                    return new ElementTag(((MaterialTag) object).identifyFull())
                            .getAttribute(attribute.fulfill(1));
                }
                else {
                    return new ElementTag(((MaterialTag) object).identify())
                            .getAttribute(attribute.fulfill(1));
                }
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.item>
        // @returns ItemTag
        // @description
        // Returns an item of the material.
        // -->
        registerTag("item", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                MaterialTag material = (MaterialTag) object;
                ItemTag item = new ItemTag(material, 1);
                attribute = attribute.fulfill(1);
                if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                    // Special patch for older material-item tags.
                    if (!attribute.isComplete()) {
                        String tag = attribute.getAttribute(1);
                        String returned = CoreUtilities.autoPropertyTag(object, attribute);
                        if (returned != null) {
                            Debug.echoError("Usage of outdated 'material.item." + tag + "' tag should be replaced by 'material." + tag + "' (with '.item' removed).");
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
        // @attribute <MaterialTag.piston_reaction>
        // @returns ElementTag
        // @mechanism piston_reaction
        // @description
        // Returns the material's piston reaction. (Only for block materials).
        // -->
        registerTag("piston_reaction", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                String res = NMSHandler.getBlockHelper().getPushReaction(((MaterialTag) object).material);
                if (res == null) {
                    return null;
                }
                return new ElementTag(res).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.block_strength>
        // @returns ElementTag(Decimal)
        // @mechanism block_strength
        // @description
        // Returns the material's strength level. (Only for block materials).
        // -->
        registerTag("block_strength", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                float res = NMSHandler.getBlockHelper().getBlockStength(((MaterialTag) object).material);
                return new ElementTag(res).getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Material' for MaterialTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                return new ElementTag("Material").getAttribute(attribute.fulfill(1));
            }
        });


        // <--[tag]
        // @attribute <MaterialTag.with[<mechanism>=<value>;...]>
        // @returns MaterialTag
        // @group properties
        // @description
        // Returns a copy of the material with mechanism adjustments applied.
        // -->
        registerTag("with", new TagRunnable() {
            @Override
            public String run(Attribute attribute, ObjectTag object) {
                if (!attribute.hasContext(1)) {
                    Debug.echoError("MaterialTag.with[...] tag must have an input mechanism list.");
                }
                MaterialTag mat = new MaterialTag(((MaterialTag) object).getModernData().clone());
                List<String> properties = ObjectFetcher.separateProperties("[" + attribute.getContext(1) + "]");
                for (int i = 1; i < properties.size(); i++) {
                    List<String> data = CoreUtilities.split(properties.get(i), '=', 2);
                    if (data.size() != 2) {
                        Debug.echoError("Invalid property string '" + properties.get(i) + "'!");
                    }
                    else {
                        mat.safeApplyProperty(new Mechanism(new ElementTag(data.get(0)), new ElementTag((data.get(1)).replace((char) 0x2011, ';')), attribute.context));
                    }
                }
                return mat.getAttribute(attribute.fulfill(1));
            }
        });
    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<>();

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
                com.denizenscript.denizencore.utilities.debugging.Debug.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new ElementTag(identify()).getAttribute(attribute.fulfill(0));
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        adjust(mechanism);
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name block_resistance
        // @input Element(Decimal)
        // @description
        // Sets the explosion resistance for all blocks of this material type.
        // @tags
        // <MaterialTag.block_resistance>
        // -->
        if (!mechanism.isProperty && mechanism.matches("block_resistance") && mechanism.requireFloat()) {
            if (!NMSHandler.getBlockHelper().setBlockResistance(material, mechanism.getValue().asFloat())) {
                Debug.echoError("Provided material does not have a placeable block.");
            }
        }

        // <--[mechanism]
        // @object MaterialTag
        // @name block_strength
        // @input Element(Decimal)
        // @description
        // Sets the strength for all blocks of this material type.
        // @tags
        // <MaterialTag.block_strength>
        // -->
        if (!mechanism.isProperty && mechanism.matches("block_strength") && mechanism.requireFloat()) {
            if (!material.isBlock()) {
                Debug.echoError("'block_strength' mechanism is only valid for block types.");
            }
            NMSHandler.getBlockHelper().setBlockStrength(material, mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object MaterialTag
        // @name piston_reaction
        // @input Element
        // @description
        // Sets the piston reaction for all blocks of this material type.
        // Input may be: NORMAL (push and pull allowed), DESTROY (break when pushed), BLOCK (prevent a push or pull), IGNORE (don't use this), or PUSH_ONLY (push allowed but not pull)
        // @tags
        // <MaterialTag.piston_reaction>
        // -->
        if (!mechanism.isProperty && mechanism.matches("piston_reaction")) {
            if (!material.isBlock()) {
                Debug.echoError("'piston_reaction' mechanism is only valid for block types.");
            }
            NMSHandler.getBlockHelper().setPushReaction(material, mechanism.getValue().asString().toUpperCase());
        }

        CoreUtilities.autoPropertyMechanism(this, mechanism);
    }
}
