package com.denizenscript.denizen.nms.v1_17.helpers;

import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.kyori.adventure.nbt.*;
import net.minecraft.nbt.*;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

public class NBTAdapter {

    public static final MethodHandle COMPOUND_TAG_MAP_CONSTRUCTOR = ReflectionHelper.getConstructor(CompoundTag.class, Map.class);

    public static Tag toNMS(BinaryTag tag) {
        if (tag instanceof ByteBinaryTag byteTag) {
            return switch (byteTag.value()) {
                case 0 -> ByteTag.ZERO;
                case 1 -> ByteTag.ONE;
                default -> ByteTag.valueOf(byteTag.value());
            };
        }
        else if (tag instanceof ShortBinaryTag shortTag) {
            return ShortTag.valueOf(shortTag.value());
        }
        else if (tag instanceof IntBinaryTag intTag) {
            return IntTag.valueOf(intTag.value());
        }
        else if (tag instanceof LongBinaryTag longTag) {
            return LongTag.valueOf(longTag.value());
        }
        else if (tag instanceof FloatBinaryTag floatTag) {
            return FloatTag.valueOf(floatTag.value());
        }
        else if (tag instanceof DoubleBinaryTag doubleTag) {
            return DoubleTag.valueOf(doubleTag.value());
        }
        else if (tag instanceof ByteArrayBinaryTag byteArrayTag) {
            return new ByteArrayTag(byteArrayTag.value());
        }
        else if (tag instanceof IntArrayBinaryTag intArrayTag) {
            return new IntArrayTag(intArrayTag.value());
        }
        else if (tag instanceof LongArrayBinaryTag longArrayTag) {
            return new LongArrayTag(longArrayTag.value());
        }
        else if (tag instanceof StringBinaryTag stringTag) {
            return StringTag.valueOf(stringTag.value());
        }
        else if (tag instanceof ListBinaryTag listTag) {
            return toNMS(listTag);
        }
        else if (tag instanceof CompoundBinaryTag compoundTag) {
            return toNMS(compoundTag);
        }
        else if (tag instanceof EndBinaryTag) {
            return EndTag.INSTANCE;
        }
        throw new IllegalStateException("Unrecognized API tag of type '" + tag.type() + "': " + tag);
    }

    public static BinaryTag toAPI(Tag nmsTag) {
        if (nmsTag instanceof ByteTag nmsByteTag) {
            return ByteBinaryTag.byteBinaryTag(nmsByteTag.getAsByte());
        }
        else if (nmsTag instanceof ShortTag nmsShortTag) {
            return ShortBinaryTag.shortBinaryTag(nmsShortTag.getAsShort());
        }
        else if (nmsTag instanceof IntTag nmsIntTag) {
            return IntBinaryTag.intBinaryTag(nmsIntTag.getAsInt());
        }
        else if (nmsTag instanceof LongTag nmsLongTag) {
            return LongBinaryTag.longBinaryTag(nmsLongTag.getAsLong());
        }
        else if (nmsTag instanceof FloatTag nmsFloatTag) {
            return FloatBinaryTag.floatBinaryTag(nmsFloatTag.getAsFloat());
        }
        else if (nmsTag instanceof DoubleTag nmsDoubleTag) {
            return DoubleBinaryTag.doubleBinaryTag(nmsDoubleTag.getAsDouble());
        }
        else if (nmsTag instanceof ByteArrayTag nmsByteArrayTag) {
            return ByteArrayBinaryTag.byteArrayBinaryTag(nmsByteArrayTag.getAsByteArray());
        }
        else if (nmsTag instanceof IntArrayTag nmsIntArrayTag) {
            return IntArrayBinaryTag.intArrayBinaryTag(nmsIntArrayTag.getAsIntArray());
        }
        else if (nmsTag instanceof LongArrayTag nmsLongArrayTag) {
            return LongArrayBinaryTag.longArrayBinaryTag(nmsLongArrayTag.getAsLongArray());
        }
        else if (nmsTag instanceof StringTag nmsStringTag) {
            return StringBinaryTag.stringBinaryTag(nmsStringTag.getAsString());
        }
        else if (nmsTag instanceof ListTag nmsListTag) {
            return toAPI(nmsListTag);
        }
        else if (nmsTag instanceof CompoundTag nmsCompoundTag) {
            return toAPI(nmsCompoundTag);
        }
        else if (nmsTag instanceof EndTag) {
            return EndBinaryTag.endBinaryTag();
        }
        throw new IllegalStateException("Unrecognized NMS tag of type '" + nmsTag.getClass().getName() + '/' + nmsTag.getType().getName() + "': " + nmsTag);
    }

    public static ListBinaryTag toAPI(ListTag nmsListTag) {
        // TODO: adventure-nbt: builder initial capacity
        ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();
        for (Tag nmsEntry : nmsListTag) {
            builder.add(toAPI(nmsEntry));
        }
        return builder.build();
    }

    public static ListTag toNMS(ListBinaryTag listTag) {
        ListTag nmsListTag = new ListTag();
        for (BinaryTag value : listTag) {
            nmsListTag.add(toNMS(value));
        }
        return nmsListTag;
    }

    public static CompoundBinaryTag toAPI(CompoundTag nmsCompoundTag) {
        Map<String, BinaryTag> tags = new HashMap<>(nmsCompoundTag.size());
        for (String key : nmsCompoundTag.getAllKeys()) {
            tags.put(key, toAPI(nmsCompoundTag.get(key)));
        }
        return CompoundBinaryTag.from(tags);
    }

    public static CompoundTag toNMS(CompoundBinaryTag compoundTag) {
        Map<String, Tag> nmsTags = new HashMap<>(compoundTag.size());
        for (Map.Entry<String, ? extends BinaryTag> entry : compoundTag) {
            nmsTags.put(entry.getKey(), toNMS(entry.getValue()));
        }
        try {
            return (CompoundTag) COMPOUND_TAG_MAP_CONSTRUCTOR.invokeExact(nmsTags);
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
