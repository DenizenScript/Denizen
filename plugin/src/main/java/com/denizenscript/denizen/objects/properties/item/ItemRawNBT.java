package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.kyori.adventure.nbt.*;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRawNBT extends ItemProperty<MapTag> {

    public static boolean describes(ItemTag item) {
        // All items can have raw NBT
        return item.getBukkitMaterial() != Material.AIR;
    }

    public ItemRawNBT(ItemTag item) {
        this.object = item;
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
        if (getMaterial() == Material.ITEM_FRAME) {
            MapTag entityMap = (MapTag) result.getObject("EntityTag");
            if (entityMap != null) {
                entityMap.putObject("Invisible", null);
                if (entityMap.isEmpty()) {
                    result.putObject("EntityTag", null);
                }
            }
        }
        if (getMaterial() == Material.ARMOR_STAND) {
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
        return (MapTag) nbtTagToObject(NMSHandler.itemHelper.getNbtData(getItemStack()));
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

    public static final BinaryTagType<?>[] BY_ID;
    public static final boolean HAS_NBT_LIST_TYPES = NMSHandler.getVersion().isAtMost(NMSVersion.v1_20);
    public static final TagStringIO SNBT_PARSER = TagStringIO.builder().emitHeterogeneousLists(HAS_NBT_LIST_TYPES).acceptHeterogeneousLists(!HAS_NBT_LIST_TYPES).build();

    static {
        // TODO: adventure-nbt: get type by id
        List<BinaryTagType<?>> allTypes = ReflectionHelper.getFieldValue(BinaryTagType.class, "TYPES", null);
        BY_ID = new BinaryTagType[allTypes.size()];
        for (BinaryTagType<?> type : allTypes) {
            BY_ID[type.id()] = type;
        }
    }

    public static CompoundBinaryTag compoundOrEmpty(CompoundBinaryTag compoundTag) {
        return compoundTag != null ? compoundTag : CompoundBinaryTag.empty();
    }

    public static BinaryTag convertObjectToNbt(ObjectTag inputObject, TagContext context, String path) {
        if (inputObject.canBeType(MapTag.class)) {
            MapTag map = inputObject.asType(MapTag.class, context);
            // TODO: adventure-nbt: builders initial size
            Map<String, BinaryTag> result = new HashMap<>(map.size());
            for (Map.Entry<StringHolder, ObjectTag> entry : map.entrySet()) {
                try {
                    result.put(entry.getKey().str, convertObjectToNbt(entry.getValue(), context, path + "." + entry.getKey().str));
                }
                catch (Exception ex) {
                    Debug.echoError("Object NBT interpretation failed for key '" + path + "." + entry.getKey().str + "'.");
                    Debug.echoError(ex);
                    return null;
                }
            }
            return CompoundBinaryTag.from(result);
        }
        else if (!HAS_NBT_LIST_TYPES && inputObject.shouldBeType(ListTag.class)) {
            ListTag list = inputObject.asType(ListTag.class, context);
            // TODO: adventure-nbt: builders initial size
            List<BinaryTag> result = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                try {
                    result.add(convertObjectToNbt(list.getObject(i), context, path + '[' + i + ']'));
                }
                catch (Exception ex) {
                    Debug.echoError("Object NBT interpretation failed for list key '" + path + "' at index " + i + '.');
                    Debug.echoError(ex);
                    return null;
                }
            }
            return ListBinaryTag.listBinaryTag(BinaryTagTypes.LIST_WILDCARD, result);
        }
        String input = inputObject.identify();
        if (input.equals("end")) {
            return EndBinaryTag.endBinaryTag();
        }
        int colonIndex = input.indexOf(':');
        if (colonIndex == -1) {
            Debug.echoError("Object NBT interpretation failed for key '" + path + "': missing object type.");
            return null;
        }
        String type = input.substring(0, colonIndex), value = input.substring(colonIndex + 1);
        return switch (type) {
            case "list" -> {
                int nextColonIndex = value.indexOf(':');
                int typeCode = Integer.parseInt(value.substring(0, nextColonIndex));
                String listValue = value.substring(nextColonIndex + 1);
                List<BinaryTag> result = new ArrayList<>();
                ListTag listTag = ListTag.valueOf(listValue, context);
                for (int i = 0; i < listTag.size(); i++) {
                    try {
                        result.add(convertObjectToNbt(listTag.getObject(i), context, path + '[' + i + ']'));
                    }
                    catch (Exception ex) {
                        Debug.echoError("Object NBT interpretation failed for list key '" + path + "' at index " + i + '.');
                        Debug.echoError(ex);
                        yield null;
                    }
                }
                yield ListBinaryTag.listBinaryTag(BY_ID[typeCode], result);
            }
            case "byte_array" -> {
                ListTag numberStrings = ListTag.valueOf(value, context);
                byte[] result = new byte[numberStrings.size()];
                for (int i = 0; i < result.length; i++) {
                    result[i] = Byte.parseByte(numberStrings.get(i));
                }
                yield ByteArrayBinaryTag.byteArrayBinaryTag(result);
            }
            case "int_array" -> {
                ListTag numberStrings = ListTag.valueOf(value, context);
                int[] result = new int[numberStrings.size()];
                for (int i = 0; i < result.length; i++) {
                    result[i] = Integer.parseInt(numberStrings.get(i));
                }
                yield IntArrayBinaryTag.intArrayBinaryTag(result);
            }
            case "long_array" -> {
                ListTag numberStrings = ListTag.valueOf(value, context);
                long[] result = new long[numberStrings.size()];
                for (int i = 0; i < result.length; i++) {
                    result[i] = Long.parseLong(numberStrings.get(i));
                }
                yield LongArrayBinaryTag.longArrayBinaryTag(result);
            }
            case "byte" -> ByteBinaryTag.byteBinaryTag(Byte.parseByte(value));
            case "short" -> ShortBinaryTag.shortBinaryTag(Short.parseShort(value));
            case "int" -> IntBinaryTag.intBinaryTag(Integer.parseInt(value));
            case "long" -> LongBinaryTag.longBinaryTag(Long.parseLong(value));
            case "float" -> FloatBinaryTag.floatBinaryTag(Float.parseFloat(value));
            case "double" -> DoubleBinaryTag.doubleBinaryTag(Double.parseDouble(value));
            case "string" -> StringBinaryTag.stringBinaryTag(value);
            default -> {
                if (context == null || context.showErrors()) {
                    Debug.echoError("Unknown raw NBT value: " + inputObject);
                }
                yield null;
            }
        };
    }

    public static ObjectTag nbtTagToObject(BinaryTag tag) {
        return nbtTagToObject(tag, false);
    }

    public static ObjectTag nbtTagToObject(BinaryTag tag, boolean unwrapLists) {
        if (tag instanceof CompoundBinaryTag compoundTag) {
            MapTag result = new MapTag();
            for (Map.Entry<String, ? extends BinaryTag> entry : compoundTag) {
                result.putObject(entry.getKey(), nbtTagToObject(entry.getValue(), unwrapLists));
            }
            return result;
        }
        else if (tag instanceof ListBinaryTag listTag) {
            if (!HAS_NBT_LIST_TYPES && unwrapLists) {
                listTag = listTag.unwrapHeterogeneity();
            }
            ListTag result = new ListTag(listTag.size());
            for (BinaryTag entry : listTag) {
                result.addObject(nbtTagToObject(entry, unwrapLists));
            }
            return HAS_NBT_LIST_TYPES ? new ElementTag("list:" + listTag.elementType().id() + ':' + result.identify()) : result;
        }
        else if (tag instanceof ByteArrayBinaryTag byteArrayTag) {
            byte[] data = byteArrayTag.value();
            StringBuilder output = new StringBuilder(data.length * 4);
            for (byte value : data) {
                output.append(value).append('|');
            }
            return new ElementTag("byte_array:" + output);
        }
        else if (tag instanceof IntArrayBinaryTag intArrayTag) {
            int[] data = intArrayTag.value();
            StringBuilder output = new StringBuilder(data.length * 4);
            for (int value : data) {
                output.append(value).append('|');
            }
            return new ElementTag("int_array:" + output);
        }
        else if (tag instanceof LongArrayBinaryTag longArrayTag) {
            long[] data = longArrayTag.value();
            StringBuilder output = new StringBuilder(data.length * 4);
            for (long value : data) {
                output.append(value).append('|');
            }
            return new ElementTag("long_array:" + output);
        }
        else if (tag instanceof ByteBinaryTag byteTag) {
            return new ElementTag("byte:" + byteTag.value());
        }
        else if (tag instanceof ShortBinaryTag shortTag) {
            return new ElementTag("short:" + shortTag.value());
        }
        else if (tag instanceof IntBinaryTag intTag) {
            return new ElementTag("int:" + intTag.value());
        }
        else if (tag instanceof LongBinaryTag longTag) {
            return new ElementTag("long:" + longTag.value());
        }
        else if (tag instanceof FloatBinaryTag floatTag) {
            return new ElementTag("float:" + floatTag.value());
        }
        else if (tag instanceof DoubleBinaryTag doubleTag) {
            return new ElementTag("double:" + doubleTag.value());
        }
        else if (tag instanceof StringBinaryTag stringTag) {
            return new ElementTag("string:" + stringTag.value());
        }
        else  if (tag instanceof EndBinaryTag) {
            return new ElementTag("end");
        }
        throw new IllegalStateException("Unrecognized API tag of type '" + tag.type() + "': " + tag);
    }

    @Override
    public MapTag getPropertyValue() {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
            MapTag nonDefaultNBT = getNonDefaultNBTMap();
            return nonDefaultNBT.isEmpty() ? null : nonDefaultNBT;
        }
        return null;
    }

    @Override
    public void setPropertyValue(MapTag value, Mechanism mechanism) {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
            setFullNBT(object, value, mechanism.context, true);
            return;
        }
        BukkitImplDeprecations.oldNbtProperty.warn(mechanism.context);
        CompoundBinaryTag oldNbtData;
        try {
            oldNbtData = (CompoundBinaryTag) ItemRawNBT.convertObjectToNbt(value, mechanism.context, "(item)");
        }
        catch (Exception ex) {
            mechanism.echoError("Invalid NBT data specified:");
            Debug.echoError(ex);
            return;
        }
        if (oldNbtData == null) {
            mechanism.echoError("Invalid NBT data specified.");
            return;
        }
        setItemStack(NMSHandler.itemHelper.setPartialOldNbt(getItemStack(), oldNbtData));
    }

    public static void register() {

        // <--[tag]
        // @attribute <ItemTag.raw_nbt>
        // @returns MapTag
        // @mechanism ItemTag.raw_nbt
        // @deprecated use 'ItemTag.custom_data'
        // @description
        // Returns a map of all non-default raw NBT on this item.
        // Refer to format details at <@link language Raw NBT Encoding>.
        // Deprecated in favor of <@link tag ItemTag.custom_data> on MC 1.20+.
        // -->
        PropertyParser.registerTag(ItemRawNBT.class, MapTag.class, "raw_nbt", (attribute, prop) -> {
            if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_19)) {
                return prop.getPropertyValue();
            }
            BukkitImplDeprecations.oldNbtProperty.warn(attribute.context);
            return new ItemCustomData(prop.object).getPropertyValue();
        });

        // <--[mechanism]
        // @object ItemTag
        // @name raw_nbt
        // @input MapTag
        // @deprecated use 'ItemTag.custom_data'
        // @description
        // Sets the given map of raw NBT keys onto this item.
        // Note that the input format must be strictly perfect.
        // Refer to <@link language Raw NBT Encoding> for explanation of the input format.
        // Deprecated in favor of <@link property ItemTag.custom_data> on MC 1.20+.
        // @tags
        // <ItemTag.raw_nbt>
        // <ItemTag.all_raw_nbt>
        // -->
        PropertyParser.registerMechanism(ItemRawNBT.class, MapTag.class, "raw_nbt", (prop, mechanism, value) -> {
            prop.setPropertyValue(value, mechanism);
        });

        // <--[tag]
        // @attribute <ItemTag.all_raw_nbt>
        // @returns MapTag
        // @mechanism ItemTag.raw_nbt
        // @group properties
        // @description
        // Returns a map of all raw NBT on this item, including default values.
        // Refer to format details at <@link language Raw NBT Encoding>.
        // -->
        PropertyParser.registerTag(ItemRawNBT.class, MapTag.class, "all_raw_nbt", (attribute, prop) -> {
            return prop.getFullNBTMap();
        });
    }

    @Override
    public String getPropertyId() {
        return "raw_nbt";
    }

    public void setFullNBT(ItemTag item, MapTag input, TagContext context, boolean retainOld) {
        CompoundBinaryTag currentTag = retainOld ? NMSHandler.itemHelper.getNbtData(item.getItemStack()) : null;
        // TODO: adventure-nbt: compound to builder
        CompoundBinaryTag.Builder compoundTagBuilder = currentTag != null ? CompoundBinaryTag.builder().put(currentTag) : CompoundBinaryTag.builder();
        for (Map.Entry<StringHolder, ObjectTag> entry : input.entrySet()) {
            try {
                BinaryTag tag = convertObjectToNbt(entry.getValue(), context, "(item).");
                if (tag != null) {
                    compoundTagBuilder.put(entry.getKey().str, tag);
                }
            }
            catch (Exception ex) {
                Debug.echoError("Raw_Nbt input failed for root key '" + entry.getKey().str + "'.");
                Debug.echoError(ex);
                return;
            }
        }
        item.setItemStack(NMSHandler.itemHelper.setNbtData(item.getItemStack(), compoundTagBuilder.build()));
    }
}
