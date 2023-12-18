package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemRawNBT implements Property {

    public static boolean describes(ObjectTag item) {
        // All items can have raw NBT
        return item instanceof ItemTag && ((ItemTag) item).getBukkitMaterial() != Material.AIR;
    }

    public static ItemRawNBT getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemRawNBT((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "raw_nbt", "all_raw_nbt"
    };

    public static final String[] handledMechs = new String[] {
            "raw_nbt"
    };

    public ItemRawNBT(ItemTag _item) {
        item = _item;
    }

    public static String[] defaultNbtKeys = new String[] {
        // Denizen
        "Denizen Item Script", "DenizenItemScript", "Denizen NBT", "Denizen",
        // General
        "Damage", "Unbreakable", "CanDestroy", "CustomModelData", "trim",
        // Display data
        "display", "HideFlags",
        // Block
        "CanPlaceOn", "BlockEntityTag", "BlockStateTag",
        // Enchanting
        "Enchantments", "StoredEnchantments", "RepairCost",
        // Attributes
        "AttributeModifiers",
        // Potions
        "CustomPotionEffects", "Potion", "CustomPotionColor",
        // Crossbow specific
        "ChargedProjectiles", "Charged",
        // Book specific
        "resolved", "generation", "author", "title", "pages",
        // Player Head specific
        "SkullOwner",
        // Firework specific
        "Explosion", "Fireworks",
        //"EntityTag", // Special handling
        // Bucket specific
        //"BucketVariantTag", // Temporarily sent through as raw due to lack of property coverage
        // Map specific
        "map", "map_scale_direction",
        //"Decorations", // Temporarily sent through due to apparent usage in certain vanilla cases not covered by properties
        // Stew specific
        "Effects",
        // Lodestone compass specific
        //"LodestoneDimension", "LodestonePos", // Temporarily sent through due to "Dimension" inconsistency, and compatibility with unloaded worlds
        "LodestoneTracked",
        // Bundle specific
        "Items",
        // Goat Horn specific
        "instrument"
    };

    public MapTag getNonDefaultNBTMap() {
        MapTag result = getFullNBTMap();
        for (String key : defaultNbtKeys) {
            result.remove(key);
        }
        if (item.getBukkitMaterial() == Material.ITEM_FRAME) {
            MapTag entityMap = (MapTag) result.getObject("EntityTag");
            if (entityMap != null) {
                entityMap.putObject("Invisible", null);
                if (entityMap.isEmpty()) {
                    result.putObject("EntityTag", null);
                }
            }
        }
        if (item.getBukkitMaterial() == Material.ARMOR_STAND) {
            MapTag entityMap = (MapTag) result.getObject("EntityTag");
            if (entityMap != null) {
                entityMap.putObject("Pose", null);
                entityMap.putObject("Small", null);
                entityMap.putObject("NoBasePlate", null);
                entityMap.putObject("Marker", null);
                entityMap.putObject("Invisible", null);
                entityMap.putObject("ShowArms", null);
                if (entityMap.isEmpty()) {
                    result.putObject("EntityTag", null);
                }
            }
        }
        return result;
    }

    public MapTag getFullNBTMap() {
        CompoundTag compoundTag = NMSHandler.itemHelper.getNbtData(item.getItemStack());
        return (MapTag) jnbtTagToObject(compoundTag);
    }

    // <--[language]
    // @name Raw NBT Encoding
    // @group Useful Lists
    // @description
    // Several things in Minecraft use NBT to store data, such as items and entities.
    // For the sake of inter-compatibility, a special standard format is used in Denizen to preserve data types.
    // This system exists in Denizen primarily for the sake of compatibility with external plugins/systems.
    // It should not be used in any scripts that don't rely on data from external plugins.
    //
    // NBT Tags are encoded as follows:
    // CompoundTag: (a fully formed MapTag)
    // ListTag: list:(NBT type-code):(a fully formed ListTag)
    // ByteArrayTag: byte_array:(a pipe-separated list of numbers)
    // IntArrayTag: int_array:(a pipe-separated list of numbers)
    // ByteTag: byte:(#)
    // ShortTag: short:(#)
    // IntTag: int:(#)
    // LongTag: long:(#)
    // FloatTag: float:(#)
    // DoubleTag: double:(#)
    // StringTag: string:(text here)
    // EndTag: end
    //
    // -->

    public static Tag convertObjectToNbt(String object, TagContext context, String path) {
        if (object.startsWith("map@")) {
            MapTag map = MapTag.valueOf(object, context);
            Map<String, Tag> result = new LinkedHashMap<>();
            for (Map.Entry<StringHolder, ObjectTag> entry : map.entrySet()) {
                try {
                    result.put(entry.getKey().str, convertObjectToNbt(entry.getValue().toString(), context, path + "." + entry.getKey().str));
                }
                catch (Exception ex) {
                    Debug.echoError("Object NBT interpretation failed for key '" + path + "." + entry.getKey().str + "'.");
                    Debug.echoError(ex);
                    return null;
                }
            }
            return NMSHandler.instance.createCompoundTag(result);
        }
        else if (object.startsWith("list:")) {
            int nextColonIndex = object.indexOf(':', "list:".length() + 1);
            int typeCode = Integer.parseInt(object.substring("list:".length(), nextColonIndex));
            String listValue = object.substring(nextColonIndex + 1);
            List<Tag> result = new ArrayList<>();
            ListTag listTag = ListTag.valueOf(listValue, context);
            for (int i = 0; i < listTag.size(); i++) {
                try {
                    result.add(convertObjectToNbt(listTag.get(i), context, path + "[" + i + "]"));
                }
                catch (Exception ex) {
                    Debug.echoError("Object NBT interpretation failed for list key '" + path + "' at index " + i + ".");
                    Debug.echoError(ex);
                    return null;
                }
            }
            return new JNBTListTag(NBTUtils.getTypeClass(typeCode), result);
        }
        else if (object.startsWith("byte_array:")) {
            ListTag numberStrings = ListTag.valueOf(object.substring("byte_array:".length()), context);
            byte[] result = new byte[numberStrings.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = Byte.parseByte(numberStrings.get(i));
            }
            return new ByteArrayTag(result);
        }
        else if (object.startsWith("int_array:")) {
            ListTag numberStrings = ListTag.valueOf(object.substring("int_array:".length()), context);
            int[] result = new int[numberStrings.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = Integer.parseInt(numberStrings.get(i));
            }
            return new IntArrayTag(result);
        }
        else if (object.startsWith("byte:")) {
            return new ByteTag(Byte.parseByte(object.substring("byte:".length())));
        }
        else if (object.startsWith("short:")) {
            return new ShortTag(Short.parseShort(object.substring("short:".length())));
        }
        else if (object.startsWith("int:")) {
            return new IntTag(Integer.parseInt(object.substring("int:".length())));
        }
        else if (object.startsWith("long:")) {
            return new LongTag(Long.parseLong(object.substring("long:".length())));
        }
        else if (object.startsWith("float:")) {
            return new FloatTag(Float.parseFloat(object.substring("float:".length())));
        }
        else if (object.startsWith("double:")) {
            return new DoubleTag(Double.parseDouble(object.substring("double:".length())));
        }
        else if (object.startsWith("string:")) {
            return new StringTag(object.substring("string:".length()));
        }
        else if (object.equals("end")) {
            return new EndTag();
        }
        else {
            if (context == null || context.showErrors()) {
                Debug.echoError("Unknown raw NBT value: " + object);
            }
            return null;
        }
    }

    public static ObjectTag jnbtTagToObject(Tag tag) {
        if (tag instanceof CompoundTag) {
            MapTag result = new MapTag();
            for (Map.Entry<String, Tag> entry : ((CompoundTag) tag).getValue().entrySet()) {
                result.putObject(entry.getKey(), jnbtTagToObject(entry.getValue()));
            }
            return result;
        }
        else if (tag instanceof JNBTListTag) {
            ListTag result = new ListTag();
            for (Tag entry : ((JNBTListTag) tag).getValue()) {
                result.addObject(jnbtTagToObject(entry));
            }
            return new ElementTag("list:" + NBTUtils.getTypeCode(((JNBTListTag) tag).getType()) + ":" + result.identify());
        }
        else if (tag instanceof ByteArrayTag) {
            byte[] data = ((ByteArrayTag) tag).getValue();
            StringBuilder output = new StringBuilder(data.length * 4);
            for (int i = 0; i < data.length; i++) {
                output.append(data[i]).append("|");
            }
            return new ElementTag("byte_array:" + output);
        }
        else if (tag instanceof IntArrayTag) {
            int[] data = ((IntArrayTag) tag).getValue();
            StringBuilder output = new StringBuilder(data.length * 4);
            for (int i = 0; i < data.length; i++) {
                output.append(data[i]).append("|");
            }
            return new ElementTag("int_array:" + output);
        }
        else if (tag instanceof ByteTag) {
            return new ElementTag("byte:" + ((ByteTag) tag).getValue());
        }
        else if (tag instanceof ShortTag) {
            return new ElementTag("short:" + ((ShortTag) tag).getValue());
        }
        else if (tag instanceof IntTag) {
            return new ElementTag("int:" + ((IntTag) tag).getValue());
        }
        else if (tag instanceof LongTag) {
            return new ElementTag("long:" + ((LongTag) tag).getValue());
        }
        else if (tag instanceof FloatTag) {
            return new ElementTag("float:" + ((FloatTag) tag).getValue());
        }
        else if (tag instanceof DoubleTag) {
            return new ElementTag("double:" + ((DoubleTag) tag).getValue());
        }
        else if (tag instanceof StringTag) {
            return new ElementTag("string:" + ((StringTag) tag).getValue());
        }
        else if (tag instanceof EndTag) {
            return new ElementTag("end");
        }
        else {
            return new ElementTag("unknown:" + tag.getValue());
        }
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.raw_nbt>
        // @returns MapTag
        // @mechanism ItemTag.raw_nbt
        // @group properties
        // @description
        // Returns a map of all non-default raw NBT on this item.
        // Refer to format details at <@link language Raw NBT Encoding>.
        // -->
        if (attribute.startsWith("raw_nbt")) {
            return getNonDefaultNBTMap().getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.all_raw_nbt>
        // @returns MapTag
        // @mechanism ItemTag.raw_nbt
        // @group properties
        // @description
        // Returns a map of all raw NBT on this item, including default values.
        // Refer to format details at <@link language Raw NBT Encoding>.
        // -->
        if (attribute.startsWith("all_raw_nbt")) {
            return getFullNBTMap().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        MapTag nbt = getNonDefaultNBTMap();
        if (!nbt.isEmpty()) {
            return nbt.identify();
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "raw_nbt";
    }

    public void setFullNBT(ItemTag item, MapTag input, TagContext context, boolean retainOld) {
        CompoundTag compoundTag = retainOld ? NMSHandler.itemHelper.getNbtData(item.getItemStack()) : null;
        Map<String, Tag> result = compoundTag == null ? new LinkedHashMap<>() : new LinkedHashMap<>(compoundTag.getValue());
        for (Map.Entry<StringHolder, ObjectTag> entry : input.entrySet()) {
            try {
                Tag tag = convertObjectToNbt(entry.getValue().toString(), context, "(item).");
                if (tag != null) {
                    result.put(entry.getKey().str, tag);
                }
            }
            catch (Exception ex) {
                Debug.echoError("Raw_Nbt input failed for root key '" + entry.getKey().str + "'.");
                Debug.echoError(ex);
                return;
            }
        }
        compoundTag = NMSHandler.instance.createCompoundTag(result);
        item.setItemStack(NMSHandler.itemHelper.setNbtData(item.getItemStack(), compoundTag));
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name raw_nbt
        // @input MapTag
        // @description
        // Sets the given map of raw NBT keys onto this item.
        // Note that the input format must be strictly perfect.
        // Refer to <@link language Raw NBT Encoding> for explanation of the input format.
        // @tags
        // <ItemTag.raw_nbt>
        // <ItemTag.all_raw_nbt>
        // -->
        if (mechanism.matches("raw_nbt") && mechanism.requireObject(MapTag.class)) {
            MapTag input = mechanism.valueAsType(MapTag.class);
            setFullNBT(item, input, mechanism.context, true);
        }
    }
}
