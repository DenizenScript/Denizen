package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.properties.material.*;
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
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.material.MaterialData;

import java.util.List;

public class MaterialTag implements ObjectTag, Adjustable {

    // <--[language]
    // @name MaterialTag Objects
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
    // For general info, see <@link language MaterialTag Objects>
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
            String dataStr = string.substring(index + 1);
            if (ArgumentHelper.matchesInteger(dataStr)) {
                data = Integer.parseInt(dataStr);
                string = string.substring(0, index);
            }
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
        if (ArgumentHelper.matchesInteger(string)) {
            int matid = Integer.parseInt(string);
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
            String dataStr = string.substring(index + 1);
            if (ArgumentHelper.matchesInteger(dataStr)) {
                data = Integer.parseInt(dataStr);
                string = string.substring(0, index);
            }
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

        registerTag("id", (attribute, object) -> {
            Deprecations.materialIdsSuggestNames.warn(attribute.getScriptEntry());
            return new ElementTag(object.material.getId());
        });

        registerTag("data", (attribute, object) -> {
            if (attribute.context == null || attribute.context.debug) {
                Deprecations.materialIdsSuggestProperties.warn(attribute.getScriptEntry());
            }
            return new ElementTag(object.getData());
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
        registerTag("is_ageable", (attribute, object) -> {
            return new ElementTag(MaterialAge.describes(object));
        });

        registerTag("is_plant", (attribute, object) -> {
            return new ElementTag(MaterialAge.describes(object));
        });

        // <--[tag]
        // @attribute <MaterialTag.is_directional>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a directional material.
        // When this returns true, <@link tag MaterialTag.direction>, <@link tag MaterialTag.valid_directions>,
        // and <@link mechanism MaterialTag.direction> are accessible.
        // -->
        registerTag("is_directional", (attribute, object) -> {
            return new ElementTag(MaterialDirectional.describes(object));
        });

        // <--[tag]
        // @attribute <MaterialTag.has_multiple_faces>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a material that has multiple faces.
        // When this returns true, <@link tag MaterialTag.faces>, <@link tag MaterialTag.valid_faces>,
        // and <@link mechanism MaterialTag.faces> are accessible.
        // -->
        registerTag("has_multiple_faces", (attribute, object) -> {
            return new ElementTag(MaterialFaces.describes(object));
        });

        // <--[tag]
        // @attribute <MaterialTag.is_bisected>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a bisected material (doors, beds, double chests, double plants, ...).
        // When this returns true, <@link tag MaterialTag.half>, <@link tag MaterialTag.relative_vector>
        // and <@link mechanism MaterialTag.half> are accessible.
        // -->
        registerTag("is_bisected", (attribute, object) -> {
            return new ElementTag(MaterialHalf.describes(object));
        });

        // <--[tag]
        // @attribute <MaterialTag.is_leaves>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a leaves material.
        // When this returns true, <@link tag LocationTag.tree_distance>,
        // <@link tag MaterialTag.persistent>, and
        // <@link mechanism MaterialTag.persistent> are accessible.
        // -->
        registerTag("is_leaves", (attribute, object) -> {
            return new ElementTag(MaterialPersistent.describes(object));
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
        registerTag("is_slab", (attribute, object) -> {
            return new ElementTag(MaterialSlab.describes(object));
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
        registerTag("is_levelable", (attribute, object) -> {
            return new ElementTag(MaterialLevel.describes(object));
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
        registerTag("is_lightable", (attribute, object) -> {
            return new ElementTag(MaterialLightable.describes(object));
        });

        // <--[tag]
        // @attribute <MaterialTag.has_gravity>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is affected by gravity.
        // -->
        registerTag("has_gravity", (attribute, object) -> {
            return new ElementTag(object.material.hasGravity());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_block>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a placeable block.
        // -->
        registerTag("is_block", (attribute, object) -> {
            return new ElementTag(object.material.isBlock());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_item>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a holdable item.
        // Note that most blocks are valid items as well.
        // This only returns "false" for certain non-holdable "special" blocks, like Fire.
        // -->
        registerTag("is_item", (attribute, object) -> {
            return new ElementTag(object.material.isItem());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_burnable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that can burn away.
        // -->
        registerTag("is_burnable", (attribute, object) -> {
            return new ElementTag(object.material.isBurnable());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_edible>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is edible.
        // -->
        registerTag("is_edible", (attribute, object) -> {
            return new ElementTag(object.material.isEdible());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_flammable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that can catch fire.
        // -->
        registerTag("is_flammable", (attribute, object) -> {
            return new ElementTag(object.material.isFlammable());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_occluding>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that completely blocks vision.
        // -->
        registerTag("is_occluding", (attribute, object) -> {
            return new ElementTag(object.material.isOccluding());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_record>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a playable music disc.
        // -->
        registerTag("is_record", (attribute, object) -> {
            return new ElementTag(object.material.isRecord());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_solid>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that is solid (cannot be walked through).
        // -->
        registerTag("is_solid", (attribute, object) -> {
            return new ElementTag(object.material.isSolid());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_transparent>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that does not block any light.
        // -->
        registerTag("is_transparent", (attribute, object) -> {
            return new ElementTag(object.material.isTransparent());
        });

        // <--[tag]
        // @attribute <MaterialTag.max_durability>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum durability of this material.
        // -->
        registerTag("max_durability", (attribute, object) -> {
            return new ElementTag(object.material.getMaxDurability());
        });

        // <--[tag]
        // @attribute <MaterialTag.block_resistance>
        // @returns ElementTag(Decimal)
        // @mechanism MaterialTag.block_resistance
        // @description
        // Returns the explosion resistance for all blocks of this material type.
        // -->
        registerTag("block_resistance", (attribute, object) -> {
            MaterialTag material = object;
            if (!NMSHandler.getBlockHelper().hasBlock(material.getMaterial())) {
                Debug.echoError("Provided material does not have a placeable block.");
                return null;
            }
            return new ElementTag(NMSHandler.getBlockHelper().getBlockResistance(material.getMaterial()));
        });

        // <--[tag]
        // @attribute <MaterialTag.hardness>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the value representing how hard a material, used as a basis for calculating the time it takes to break.
        // -->
        registerTag("hardness", (attribute, object) -> {
            MaterialTag material = object;
            if (!material.getMaterial().isBlock()) {
                Debug.echoError("Provided material does not have a placeable block.");
                return null;
            }
            return new ElementTag(material.getMaterial().getHardness());
        });

        // <--[tag]
        // @attribute <MaterialTag.max_stack_size>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum amount of this material that can be held in a stack.
        // -->
        registerTag("max_stack_size", (attribute, object) -> {
            return new ElementTag(object.material.getMaxStackSize());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_made_of[<material>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns true if the material is a variety of the specified material.
        // Example: <m@red_wool.is_made_of[m@wool]> will return true.
        // Invalid for 1.13+ servers.
        // -->
        registerTag("is_made_of", (attribute, object) -> {
            MaterialTag compared = MaterialTag.valueOf(attribute.getContext(1));
            return new ElementTag(compared != null && object.material == compared.getMaterial());
        });

        // <--[tag]
        // @attribute <MaterialTag.bukkit_enum>
        // @returns ElementTag
        // @description
        // Returns the bukkit Material enum value. For example: <m@birch_sapling.bukkit_enum>
        // will return 'sapling'
        // Unneeded for 1.13+ servers.
        // -->
        registerTag("bukkit_enum", (attribute, object) -> {
            return new ElementTag(object.material.name());
        });

        // <--[tag]
        // @attribute <MaterialTag.name>
        // @returns ElementTag
        // @description
        // Returns the name of the material.
        // -->
        registerTag("name", (attribute, object) -> {
            return new ElementTag(object.forcedIdentity != null ? object.forcedIdentityLow :
                    CoreUtilities.toLowerCase(object.material.name()));
        });

        // <--[tag]
        // @attribute <MaterialTag.full>
        // @returns ElementTag
        // @description
        // Returns the material's full identification.
        // Irrelevant on modern (1.13+) servers.
        // -->
        registerTag("full", (attribute, object) -> {
            if (object.hasData()) {
                return new ElementTag(object.identifyFull());
            }
            else {
                return new ElementTag(object.identify());
            }
        });

        // <--[tag]
        // @attribute <MaterialTag.item>
        // @returns ItemTag
        // @description
        // Returns an item of the material.
        // -->
        registerTag("item", (attribute, object) -> {
            MaterialTag material = object;
            ItemTag item = new ItemTag(material, 1);
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                if (item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta() instanceof BlockStateMeta) {
                    ((BlockStateMeta) item.getItemStack().getItemMeta()).setBlockState(material.modernData.getBlockState());
                }
            }
            return item;
        });

        // <--[tag]
        // @attribute <MaterialTag.piston_reaction>
        // @returns ElementTag
        // @mechanism MaterialTag.piston_reaction
        // @description
        // Returns the material's piston reaction. (Only for block materials).
        // -->
        registerTag("piston_reaction", (attribute, object) -> {
            String res = NMSHandler.getBlockHelper().getPushReaction(object.material);
            if (res == null) {
                return null;
            }
            return new ElementTag(res);
        });

        // <--[tag]
        // @attribute <MaterialTag.block_strength>
        // @returns ElementTag(Decimal)
        // @mechanism MaterialTag.block_strength
        // @description
        // Returns the material's strength level. (Only for block materials).
        // This is a representation of how much time mining is needed to break a block.
        // -->
        registerTag("block_strength", (attribute, object) -> {
            float res = NMSHandler.getBlockHelper().getBlockStength(object.material);
            return new ElementTag(res);
        });

        // <--[tag]
        // @attribute <MaterialTag.type>
        // @returns ElementTag
        // @description
        // Always returns 'Material' for MaterialTag objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", (attribute, object) -> {
            return new ElementTag("Material");
        });


        // <--[tag]
        // @attribute <MaterialTag.with[<mechanism>=<value>;...]>
        // @returns MaterialTag
        // @group properties
        // @description
        // Returns a copy of the material with mechanism adjustments applied.
        // -->
        registerTag("with", (attribute, object) -> {
            if (!attribute.hasContext(1)) {
                Debug.echoError("MaterialTag.with[...] tag must have an input mechanism list.");
            }
            MaterialTag mat = new MaterialTag(object.getModernData().clone());
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
            return mat;
        });
    }

    public static ObjectTagProcessor<MaterialTag> tagProcessor = new ObjectTagProcessor<>();

    public static void registerTag(String name, TagRunnable.ObjectInterface<MaterialTag> runnable, String... variants) {
        tagProcessor.registerTag(name, runnable, variants);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
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
        // @input ElementTag(Decimal)
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
        // @input ElementTag(Decimal)
        // @description
        // Sets the strength for all blocks of this material type.
        // This does not work for specifically obsidian (as it is a hardcoded special case in the Minecraft internals).
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
