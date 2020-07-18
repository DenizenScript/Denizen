package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizen.objects.properties.material.*;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.blocks.ModernBlockData;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.ChatColor;
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
    // These use the object notation "m@".
    // The identity format for materials is the material type name.
    // For example, 'm@stick'.
    //
    // Block materials may sometimes also contain property data,
    // for specific values on the block material such as the growth stage of a plant or the orientation of a stair block.
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

    @Deprecated
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
        if (ObjectFetcher.isObjectWithProperties(string)) {
            return ObjectFetcher.getObjectFrom(MaterialTag.class, string, context);
        }
        string = string.toUpperCase();
        if (string.startsWith("M@")) {
            string = string.substring("M@".length());
        }
        if (string.equals("RANDOM")) {
            return new MaterialTag(Material.values()[CoreUtilities.getRandom().nextInt(Material.values().length)]);
        }
        Material m = Material.getMaterial(string);
        if (m != null) {
            return new MaterialTag(m);
        }
        return null;
    }

    public static MaterialTag quickOfNamed(String string) {
        Material m = Material.getMaterial(string.toUpperCase());
        if (m != null) {
            return new MaterialTag(m);
        }
        return null;
    }

    public static TagContext noDebugContext = new BukkitTagContext(null, null, null, false, null);

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

    @Override
    public ObjectTag duplicate() {
        if (hasModernData()) {
            return new MaterialTag(getModernData());
        }
        else {
            return new MaterialTag(getMaterial());
        }
    }

    /**
     * @param object object-fetchable String of a valid MaterialTag, or a MaterialTag object
     * @return true if the MaterialTags are the same.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof MaterialTag) {
            return getMaterial() == ((MaterialTag) object).getMaterial();
        }
        else {
            MaterialTag parsed = valueOf(object.toString(), CoreUtilities.noDebugContext);
            return equals(parsed);
        }
    }

    public boolean matchesBlock(Block b) {
        return getMaterial() == b.getType();
    }

    ///////////////
    //   Constructors
    /////////////

    public MaterialTag(Material material) {
        this.material = material;
        if (material.isBlock()) {
            modernData = new ModernBlockData(material);
        }
    }

    public MaterialTag(BlockState state) {
        this.material = state.getType();
        this.modernData = new ModernBlockData(state);
    }

    public MaterialTag(Block block) {
        this.modernData = new ModernBlockData(block);
        this.material = modernData.getMaterial();
    }

    public MaterialTag(ModernBlockData data) {
        this.modernData = data;
        this.material = data.getMaterial();
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    private Material material;
    private ModernBlockData modernData;

    public boolean hasModernData() {
        return modernData != null;
    }

    public ModernBlockData getModernData() {
        return modernData;
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

    public boolean isStructure() {
        if (material == Material.CHORUS_PLANT || material == Material.RED_MUSHROOM_BLOCK || material == Material.BROWN_MUSHROOM_BLOCK) {
            return true;
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
        return CoreUtilities.toLowerCase(material.name());
    }

    public String identifyNoIdentifier() {
        if (forcedIdentity != null) {
            return forcedIdentityLow;
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
            return forcedIdentityLow;
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
        }, "is_plant");

        // <--[tag]
        // @attribute <MaterialTag.is_campfire>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a campfire material.
        // When this returns true, <@link tag MaterialTag.signal_fire>,
        // and <@link mechanism MaterialTag.signal_fire> are accessible.
        // -->
        registerTag("is_campfire", (attribute, object) -> {
            return new ElementTag(MaterialCampfire.describes(object));
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
        // @attribute <MaterialTag.can_drag>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a material that can cause dragging (like bubble columns).
        // When this returns true, <@link tag MaterialTag.drags>
        // and <@link mechanism MaterialTag.drags> are accessible.
        // -->
        registerTag("can_drag", (attribute, object) -> {
            return new ElementTag(MaterialDrags.describes(object));
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
        // @attribute <MaterialTag.is_bamboo>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a bamboo material.
        // When this returns true, <@link tag MaterialTag.leaf_size>,
        // and <@link mechanism MaterialTag.leaf_size> are accessible.
        // -->
        registerTag("is_bamboo", (attribute, object) -> {
            return new ElementTag(MaterialLeafSize.describes(object));
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
        // @attribute <MaterialTag.has_count>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material has a 'count' value, which applies to SeaPickles and TurtleEggs.
        // When this returns true, <@link tag MaterialTag.count>,
        // <@link tag MaterialTag.count_max>, <@link tag MaterialTag.count_min>,
        // and <@link mechanism MaterialTag.count> are accessible.
        // -->
        registerTag("has_count", (attribute, object) -> {
            return new ElementTag(MaterialCount.describes(object));
        }, "is_pickle");

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
        // @attribute <MaterialTag.is_snowable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is snowable.
        // When this returns true, <@link tag MaterialTag.snowy>,
        // and <@link mechanism MaterialTag.snowy> are accessible.
        // -->
        registerTag("is_snowable", (attribute, object) -> {
            return new ElementTag(MaterialSnowable.describes(object));
        });

        // <--[tag]
        // @attribute <MaterialTag.is_switch>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is a switch.
        // When this returns true, <@link tag MaterialTag.switch_face>,
        // and <@link mechanism MaterialTag.switch_face> are accessible.
        // -->
        registerTag("is_switch", (attribute, object) -> {
            return new ElementTag(MaterialSnowable.describes(object));
        });

        // <--[tag]
        // @attribute <MaterialTag.is_waterloggable>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether the material is able to be waterlogged.
        // When this returns true, <@link tag MaterialTag.waterlogged>,
        // and <@link mechanism MaterialTag.waterlogged> are accessible.
        // -->
        registerTag("is_waterloggable", (attribute, object) -> {
            return new ElementTag(MaterialWaterlogged.describes(object));
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
        // @attribute <MaterialTag.is_fuel>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that can be burned in a furnace as fuel.
        // -->
        registerTag("is_fuel", (attribute, object) -> {
            return new ElementTag(object.material.isFuel());
        });

        // <--[tag]
        // @attribute <MaterialTag.fuel_burn_time>
        // @returns DurationTag
        // @description
        // Returns the duration that a burnable fuel block will burn in a furnace for.
        // -->
        registerTag("fuel_burn_time", (attribute, object) -> {
            Integer ticks = NMSHandler.getItemHelper().burnTime(object.getMaterial());
            if (ticks != null) {
                return new DurationTag(ticks.longValue());
            }
            return null;
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
        // @attribute <MaterialTag.is_switchable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is Openable, Powerable, or a Dispenser.
        // -->
        registerTag("is_switchable", (attribute, object) -> {
            return new ElementTag(MaterialSwitchable.describes(object));
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
            if (!NMSHandler.getBlockHelper().hasBlock(object.getMaterial())) {
                Debug.echoError("Provided material does not have a placeable block.");
                return null;
            }
            return new ElementTag(NMSHandler.getBlockHelper().getBlockResistance(object.getMaterial()));
        });

        // <--[tag]
        // @attribute <MaterialTag.hardness>
        // @returns ElementTag(Decimal)
        // @description
        // Returns the value representing how hard a material, used as a basis for calculating the time it takes to break.
        // -->
        registerTag("hardness", (attribute, object) -> {
            if (!object.getMaterial().isBlock()) {
                Debug.echoError("Provided material does not have a placeable block.");
                return null;
            }
            return new ElementTag(object.getMaterial().getHardness());
        });

        // <--[tag]
        // @attribute <MaterialTag.max_stack_size>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.max_stack_size
        // @description
        // Returns the maximum amount of this material that can be held in a stack.
        // -->
        registerTag("max_stack_size", (attribute, object) -> {
            return new ElementTag(object.material.getMaxStackSize());
        });

        // <--[tag]
        // @attribute <MaterialTag.translated_name>
        // @returns ElementTag
        // @description
        // Returns the localized name of the material.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        registerTag("translated_name", (attribute, object) -> {
            String key = object.material.getKey().getKey();
            key = key.replace("wall_banner", "banner");
            String type = object.material.isBlock() ? "block" : "item";
            return new ElementTag(ChatColor.COLOR_CHAR + "[translate=" + type + ".minecraft." + key + "]");
        });

        // <--[tag]
        // @attribute <MaterialTag.name>
        // @returns ElementTag
        // @description
        // Returns the name of the material.
        // -->
        registerTag("name", (attribute, object) -> {
            return new ElementTag(object.forcedIdentity != null ? object.forcedIdentityLow : CoreUtilities.toLowerCase(object.material.name()));
        });

        // <--[tag]
        // @attribute <MaterialTag.item>
        // @returns ItemTag
        // @description
        // Returns an item of the material.
        // -->
        registerTag("item", (attribute, object) -> {
            ItemTag item = new ItemTag(object, 1);
            if (item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta() instanceof BlockStateMeta) {
                ((BlockStateMeta) item.getItemStack().getItemMeta()).setBlockState(object.modernData.getBlockState());
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
                    mat.safeApplyProperty(new Mechanism(new ElementTag(data.get(0)), new ElementTag(data.get(1)), attribute.context));
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
        // @name max_stack_size
        // @input ElementTag(Number)
        // @description
        // Sets the maximum stack size for all items this material type.
        // Note that altering this will probably require a script performing "- inventory update" in the event "after player clicks in inventory:" to maintain sync.
        // The maximum the client will interact with is stacks of 64, however you can set the max up to 127 and the client will render it, but refuse to move stacks properly.
        // @tags
        // <MaterialTag.max_stack_size>
        // -->
        if (!mechanism.isProperty && mechanism.matches("max_stack_size") && mechanism.requireInteger()) {
            NMSHandler.getItemHelper().setMaxStackSize(material, mechanism.getValue().asInt());
        }

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
        // @input ElementTag
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
