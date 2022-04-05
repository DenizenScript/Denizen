package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.properties.material.*;
import com.denizenscript.denizen.utilities.VanillaTagHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

import java.util.HashSet;

public class MaterialTag implements ObjectTag, Adjustable, FlaggableObject {

    // <--[ObjectType]
    // @name MaterialTag
    // @prefix m
    // @base ElementTag
    // @implements FlaggableObject, PropertyHolderObject
    // @ExampleTagBase material[stone]
    // @ExampleValues stone,dirt,stick,iron_sword
    // @format
    // The identity format for materials is the material type name.
    // For example, 'm@stick'.
    //
    // @description
    // A MaterialTag represents a material (a type of block or item).
    //
    // Block materials may sometimes also contain property data,
    // for specific values on the block material such as the growth stage of a plant or the orientation of a stair block.
    //
    // Material types: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html>.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the server saves file, under special sub-key "__materials"
    //
    // -->

    /**
     * Gets a Material Object from a string form.
     *
     * @param string the string
     * @return a Material, or null if incorrectly formatted
     */
    @Fetchable("m")
    public static MaterialTag valueOf(String string, TagContext context) {
        if (ObjectFetcher.isObjectWithProperties(string)) {
            return ObjectFetcher.getObjectFromWithProperties(MaterialTag.class, string, context);
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
            return new MaterialTag(getModernData().clone());
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

    public MaterialTag(Material material) {
        this.material = material;
        if (material.isBlock()) {
            modernData = material.createBlockData();
        }
    }

    public MaterialTag(BlockState state) {
        this.material = state.getType();
        this.modernData = state.getBlockData();
    }

    public MaterialTag(Block block) {
        this.modernData = block.getBlockData();
        this.material = modernData.getMaterial();
    }

    public MaterialTag(BlockData data) {
        this.modernData = data;
        this.material = data.getMaterial();
    }

    private Material material;
    private BlockData modernData;

    public boolean hasModernData() {
        return modernData != null;
    }

    public BlockData getModernData() {
        return modernData;
    }

    public void setModernData(BlockData data) {
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

    @Override
    public String debuggable() {
        return "<LG>m@<Y>" + CoreUtilities.toLowerCase(material.name()) + PropertyParser.getPropertiesDebuggable(this);
    }

    @Override
    public String identifySimple() {
        return "m@" + identifySimpleNoIdentifier();
    }

    public String identifyNoPropertiesNoIdentifier() {
        return CoreUtilities.toLowerCase(material.name());
    }

    public String identifyNoIdentifier() {
        return CoreUtilities.toLowerCase(material.name()) + PropertyParser.getPropertiesString(this);
    }

    public String identifySimpleNoIdentifier() {
        return CoreUtilities.toLowerCase(material.name());
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    @Override
    public boolean isTruthy() {
        return !getMaterial().isAir();
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(DenizenCore.serverFlagMap, "__materials." + material.name().replace(".", "&dot"));
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void registerTags() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);
        PropertyParser.registerPropertyTagHandlers(MaterialTag.class, tagProcessor);

        tagProcessor.registerTag(ElementTag.class, "is_ageable", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialAge.describes(object));
        }, "is_plant");
        tagProcessor.registerTag(ElementTag.class, "is_campfire", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialCampfire.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "is_directional", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialDirectional.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "has_multiple_faces", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialFaces.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "can_drag", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialDrags.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "is_bisected", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialHalf.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "has_leaf_size", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialLeafSize.describes(object));
        }, "is_bamboo");
        tagProcessor.registerTag(ElementTag.class, "is_levelable", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialLevel.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "is_lightable", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialLightable.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "is_leaves", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialPersistent.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "has_count", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialCount.describes(object));
        }, "is_pickle");
        tagProcessor.registerTag(ElementTag.class, "has_type", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialBlockType.describes(object));
        }, "is_slab");
        tagProcessor.registerTag(ElementTag.class, "is_snowable", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialSnowable.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "is_switch", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialSwitchFace.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "is_waterloggable", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialWaterlogged.describes(object));
        });
        tagProcessor.registerTag(ElementTag.class, "is_switchable", (attribute, object) -> {
            Deprecations.materialPropertyTags.warn(attribute.context);
            return new ElementTag(MaterialSwitchable.describes(object));
        });

        // <--[tag]
        // @attribute <MaterialTag.has_gravity>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is affected by gravity.
        // -->
        tagProcessor.registerTag(ElementTag.class, "has_gravity", (attribute, object) -> {
            return new ElementTag(object.material.hasGravity());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_block>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a placeable block.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_block", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "is_item", (attribute, object) -> {
            return new ElementTag(object.material.isItem());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_burnable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that can burn away.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_burnable", (attribute, object) -> {
            return new ElementTag(object.material.isBurnable());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_edible>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is edible.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_edible", (attribute, object) -> {
            return new ElementTag(object.material.isEdible());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_flammable>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that can catch fire.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_flammable", (attribute, object) -> {
            return new ElementTag(object.material.isFlammable());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_fuel>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that can be burned in a furnace as fuel.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_fuel", (attribute, object) -> {
            return new ElementTag(object.material.isFuel());
        });

        // <--[tag]
        // @attribute <MaterialTag.fuel_burn_time>
        // @returns DurationTag
        // @description
        // Returns the duration that a burnable fuel block will burn in a furnace for.
        // -->
        tagProcessor.registerTag(DurationTag.class, "fuel_burn_time", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "is_occluding", (attribute, object) -> {
            return new ElementTag(object.material.isOccluding());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_record>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a playable music disc.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_record", (attribute, object) -> {
            return new ElementTag(object.material.isRecord());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_solid>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that is solid (can be built upon).
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_solid", (attribute, object) -> {
            return new ElementTag(object.material.isSolid());
        });

        // <--[tag]
        // @attribute <MaterialTag.is_transparent>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material is a block that does not block any light.
        // -->
        tagProcessor.registerTag(ElementTag.class, "is_transparent", (attribute, object) -> {
            return new ElementTag(object.material.isTransparent());
        });

        // <--[tag]
        // @attribute <MaterialTag.max_durability>
        // @returns ElementTag(Number)
        // @description
        // Returns the maximum durability of this material.
        // -->
        tagProcessor.registerTag(ElementTag.class, "max_durability", (attribute, object) -> {
            return new ElementTag(object.material.getMaxDurability());
        });

        // <--[tag]
        // @attribute <MaterialTag.block_resistance>
        // @returns ElementTag(Decimal)
        // @mechanism MaterialTag.block_resistance
        // @description
        // Returns the explosion resistance for all blocks of this material type.
        // -->
        tagProcessor.registerTag(ElementTag.class, "block_resistance", (attribute, object) -> {
            if (!object.getMaterial().isBlock()) {
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
        tagProcessor.registerTag(ElementTag.class, "hardness", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "max_stack_size", (attribute, object) -> {
            return new ElementTag(object.material.getMaxStackSize());
        });

        // <--[tag]
        // @attribute <MaterialTag.translated_name>
        // @returns ElementTag
        // @description
        // Returns the localized name of the material.
        // Note that this is a magic Denizen tool - refer to <@link language Denizen Text Formatting>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "translated_name", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(CoreUtilities.toLowerCase(object.material.name()));
        });

        // <--[tag]
        // @attribute <MaterialTag.item>
        // @returns ItemTag
        // @description
        // Returns an item of the material.
        // -->
        tagProcessor.registerTag(ItemTag.class, "item", (attribute, object) -> {
            return new ItemTag(object, 1);
        });

        // <--[tag]
        // @attribute <MaterialTag.piston_reaction>
        // @returns ElementTag
        // @mechanism MaterialTag.piston_reaction
        // @description
        // Returns the material's piston reaction. (Only for block materials).
        // -->
        tagProcessor.registerTag(ElementTag.class, "piston_reaction", (attribute, object) -> {
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
        tagProcessor.registerTag(ElementTag.class, "block_strength", (attribute, object) -> {
            float res = NMSHandler.getBlockHelper().getBlockStength(object.material);
            return new ElementTag(res);
        });

        tagProcessor.registerTag(ElementTag.class, "has_vanilla_data_tag", (attribute, object) -> {
            Deprecations.materialHasDataPackTag.warn(attribute.context);
            if (!attribute.hasParam()) {
                attribute.echoError("MaterialTag.has_vanilla_data_tag[...] tag must have an input value.");
                return null;
            }
            NamespacedKey key = NamespacedKey.minecraft(CoreUtilities.toLowerCase(attribute.getParam()));
            Tag<Material> tagBlock = Bukkit.getTag("blocks", key, Material.class);
            Tag<Material> tagItem = Bukkit.getTag("items", key, Material.class);
            return new ElementTag((tagBlock != null && tagBlock.isTagged(object.getMaterial()) || (tagItem != null && tagItem.isTagged(object.getMaterial()))));
        });

        // <--[tag]
        // @attribute <MaterialTag.advanced_matches[<matcher>]>
        // @returns ElementTag(Boolean)
        // @description
        // Returns whether the material matches some matcher text, using the system behind <@link language Advanced Script Event Matching>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "advanced_matches", (attribute, object) -> {
            if (!attribute.hasParam()) {
                return null;
            }
            return new ElementTag(BukkitScriptEvent.tryMaterial(object, attribute.getParam()));
        });

        // <--[tag]
        // @attribute <MaterialTag.vanilla_tags>
        // @returns ListTag
        // @description
        // Returns a list of vanilla tags that apply to this material. See also <@link url https://minecraft.fandom.com/wiki/Tag>.
        // -->
        tagProcessor.registerTag(ListTag.class, "vanilla_tags", (attribute, object) -> {
            HashSet<String> tags = VanillaTagHelper.tagsByMaterial.get(object.getMaterial());
            if (tags == null) {
                return new ListTag();
            }
            return new ListTag(tags);
        });

        // <--[tag]
        // @attribute <MaterialTag.produced_instrument>
        // @returns ElementTag
        // @description
        // Returns the name of the instrument that would be used by a note block placed above a block of this material.
        // See list at <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Instrument.html>.
        // For the current instrument of a note block material refer to <@link tag MaterialTag.instrument>.
        // -->
        tagProcessor.registerTag(ElementTag.class, "produced_instrument", (attribute, object) -> {
            return new ElementTag(NMSHandler.getBlockHelper().getInstrumentFor(object.getMaterial()).name());
        });
    }

    public static ObjectTagProcessor<MaterialTag> tagProcessor = new ObjectTagProcessor<>();

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

    @Override
    public boolean advancedMatches(String matcher) {
        return BukkitScriptEvent.tryMaterial(this, matcher);
    }
}
