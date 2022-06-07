package com.denizenscript.denizen.nms.v1_19.impl.jnbt;

import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.nms.util.jnbt.ByteArrayTag;
import com.denizenscript.denizen.nms.util.jnbt.ByteTag;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.DoubleTag;
import com.denizenscript.denizen.nms.util.jnbt.EndTag;
import com.denizenscript.denizen.nms.util.jnbt.FloatTag;
import com.denizenscript.denizen.nms.util.jnbt.IntArrayTag;
import com.denizenscript.denizen.nms.util.jnbt.IntTag;
import com.denizenscript.denizen.nms.util.jnbt.LongTag;
import com.denizenscript.denizen.nms.util.jnbt.ShortTag;
import com.denizenscript.denizen.nms.util.jnbt.StringTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import net.minecraft.nbt.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompoundTagImpl extends CompoundTag {

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public CompoundTagImpl(Map<String, Tag> value) {
        super(value);
    }

    public net.minecraft.nbt.CompoundTag toNMSTag() {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        for (Map.Entry<String, Tag> entry : value.entrySet()) {
            if (entry.getValue() instanceof IntTag) {
                tag.putInt(entry.getKey(), ((IntTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof ByteTag) {
                tag.putByte(entry.getKey(), ((ByteTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof ByteArrayTag) {
                tag.putByteArray(entry.getKey(), ((ByteArrayTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof CompoundTag) {
                tag.put(entry.getKey(), ((CompoundTagImpl) entry.getValue()).toNMSTag());
            }
            else if (entry.getValue() instanceof DoubleTag) {
                tag.putDouble(entry.getKey(), ((DoubleTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof FloatTag) {
                tag.putFloat(entry.getKey(), ((FloatTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof IntArrayTag) {
                tag.putIntArray(entry.getKey(), ((IntArrayTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof JNBTListTag) {
                List<net.minecraft.nbt.Tag> list = new ListTag();
                List<Tag> tags = ((JNBTListTag) entry.getValue()).getValue();
                for (Tag btag : tags) {
                    HashMap<String, Tag> btags = new HashMap<>();
                    btags.put("test", btag);
                    CompoundTagImpl comp = new CompoundTagImpl(btags);
                    list.add(comp.toNMSTag().get("test"));
                }
                tag.put(entry.getKey(), (ListTag) list);
            }
            else if (entry.getValue() instanceof LongTag) {
                tag.putLong(entry.getKey(), ((LongTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof ShortTag) {
                tag.putShort(entry.getKey(), ((ShortTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof StringTag) {
                tag.putString(entry.getKey(), ((StringTag) entry.getValue()).getValue());
            }
        }
        return tag;
    }

    public static CompoundTag fromNMSTag(net.minecraft.nbt.CompoundTag tag) {
        HashMap<String, Tag> tags = new HashMap<>();
        for (String key : tag.getAllKeys()) {
            net.minecraft.nbt.Tag base = tag.get(key);
            if (base instanceof net.minecraft.nbt.IntTag) {
                tags.put(key, new IntTag(((net.minecraft.nbt.IntTag) base).getAsInt()));
            }
            else if (base instanceof net.minecraft.nbt.ByteTag) {
                tags.put(key, new ByteTag(((net.minecraft.nbt.ByteTag) base).getAsByte()));
            }
            else if (base instanceof net.minecraft.nbt.FloatTag) {
                tags.put(key, new FloatTag(((net.minecraft.nbt.FloatTag) base).getAsFloat()));
            }
            else if (base instanceof net.minecraft.nbt.DoubleTag) {
                tags.put(key, new DoubleTag(((net.minecraft.nbt.DoubleTag) base).getAsDouble()));
            }
            else if (base instanceof net.minecraft.nbt.ByteArrayTag) {
                tags.put(key, new ByteArrayTag(((net.minecraft.nbt.ByteArrayTag) base).getAsByteArray()));
            }
            else if (base instanceof net.minecraft.nbt.IntArrayTag) {
                tags.put(key, new IntArrayTag(((net.minecraft.nbt.IntArrayTag) base).getAsIntArray()));
            }
            else if (base instanceof net.minecraft.nbt.CompoundTag) {
                tags.put(key, fromNMSTag(((net.minecraft.nbt.CompoundTag) base)));
            }
            else if (base instanceof net.minecraft.nbt.EndTag) {
                tags.put(key, new EndTag());
            }
            else if (base instanceof net.minecraft.nbt.LongTag) {
                tags.put(key, new LongTag(((net.minecraft.nbt.LongTag) base).getAsLong()));
            }
            else if (base instanceof net.minecraft.nbt.ShortTag) {
                tags.put(key, new ShortTag(((net.minecraft.nbt.ShortTag) base).getAsShort()));
            }
            else if (base instanceof net.minecraft.nbt.StringTag) {
                tags.put(key, new StringTag(base.getAsString()));
            }
            else if (base instanceof ListTag) {
                List<net.minecraft.nbt.Tag> list = (List<net.minecraft.nbt.Tag>) base;
                if (list.size() > 0) {
                    net.minecraft.nbt.Tag nbase = list.get(0);
                    net.minecraft.nbt.CompoundTag comp = new net.minecraft.nbt.CompoundTag();
                    comp.put("test", nbase);
                    ListTagBuilder ltb = new ListTagBuilder(fromNMSTag(comp).getValue().get("test").getClass());
                    for (int i = 0; i < list.size(); i++) {
                        net.minecraft.nbt.Tag nbase2 = list.get(i);
                        net.minecraft.nbt.CompoundTag comp2 = new net.minecraft.nbt.CompoundTag();
                        comp2.put("test", nbase2);
                        ltb.add(fromNMSTag(comp2).getValue().get("test"));
                    }
                    tags.put(key, ltb.build());
                }
            }
        }
        return new CompoundTagImpl(tags);
    }
}
