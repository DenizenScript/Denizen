package com.denizenscript.denizen.v1_12.impl.jnbt;

import com.denizenscript.denizen.nms.util.jnbt.*;
import net.minecraft.server.v1_12_R1.*;

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

    public NBTTagCompound toNMSTag() {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<String, Tag> entry : value.entrySet()) {
            if (entry.getValue() instanceof IntTag) {
                tag.setInt(entry.getKey(), ((IntTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof ByteTag) {
                tag.setByte(entry.getKey(), ((ByteTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof ByteArrayTag) {
                tag.setByteArray(entry.getKey(), ((ByteArrayTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof CompoundTag) {
                tag.set(entry.getKey(), ((CompoundTagImpl) entry.getValue()).toNMSTag());
            }
            else if (entry.getValue() instanceof DoubleTag) {
                tag.setDouble(entry.getKey(), ((DoubleTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof FloatTag) {
                tag.setFloat(entry.getKey(), ((FloatTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof IntArrayTag) {
                tag.setIntArray(entry.getKey(), ((IntArrayTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof ListTag) {
                NBTTagList list = new NBTTagList();
                List<Tag> tags = ((ListTag) entry.getValue()).getValue();
                for (Tag btag : tags) {
                    HashMap<String, Tag> btags = new HashMap<>();
                    btags.put("test", btag);
                    CompoundTagImpl comp = new CompoundTagImpl(btags);
                    list.add(comp.toNMSTag().get("test"));
                }
                tag.set(entry.getKey(), list);
            }
            else if (entry.getValue() instanceof LongTag) {
                tag.setLong(entry.getKey(), ((LongTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof ShortTag) {
                tag.setShort(entry.getKey(), ((ShortTag) entry.getValue()).getValue());
            }
            else if (entry.getValue() instanceof StringTag) {
                tag.setString(entry.getKey(), ((StringTag) entry.getValue()).getValue());
            }
        }
        return tag;
    }

    public static CompoundTag fromNMSTag(NBTTagCompound tag) {
        HashMap<String, Tag> tags = new HashMap<>();
        for (String key : tag.c()) {
            NBTBase base = tag.get(key);
            if (base instanceof NBTTagInt) {
                tags.put(key, new IntTag(((NBTTagInt) base).e()));
            }
            else if (base instanceof NBTTagByte) {
                tags.put(key, new ByteTag(((NBTTagByte) base).g()));
            }
            else if (base instanceof NBTTagFloat) {
                tags.put(key, new FloatTag(((NBTTagFloat) base).i()));
            }
            else if (base instanceof NBTTagDouble) {
                tags.put(key, new DoubleTag(((NBTTagDouble) base).asDouble()));
            }
            else if (base instanceof NBTTagByteArray) {
                tags.put(key, new ByteArrayTag(((NBTTagByteArray) base).c()));
            }
            else if (base instanceof NBTTagIntArray) {
                tags.put(key, new IntArrayTag(((NBTTagIntArray) base).d()));
            }
            else if (base instanceof NBTTagCompound) {
                tags.put(key, fromNMSTag(((NBTTagCompound) base)));
            }
            else if (base instanceof NBTTagEnd) {
                tags.put(key, new EndTag());
            }
            else if (base instanceof NBTTagLong) {
                tags.put(key, new LongTag(((NBTTagLong) base).d()));
            }
            else if (base instanceof NBTTagShort) {
                tags.put(key, new ShortTag(((NBTTagShort) base).f()));
            }
            else if (base instanceof NBTTagString) {
                tags.put(key, new StringTag(((NBTTagString) base).c_()));
            }
            else if (base instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) base;
                if (list.size() > 0) {
                    NBTBase nbase = list.i(0);
                    NBTTagCompound comp = new NBTTagCompound();
                    comp.set("test", nbase);
                    ListTagBuilder ltb = new ListTagBuilder(fromNMSTag(comp).getValue().get("test").getClass());
                    for (int i = 0; i < list.size(); i++) {
                        NBTBase nbase2 = list.i(i);
                        NBTTagCompound comp2 = new NBTTagCompound();
                        comp2.set("test", nbase2);
                        ltb.add(fromNMSTag(comp2).getValue().get("test"));
                    }
                    tags.put(key, ltb.build());
                }
            }
        }
        return new CompoundTagImpl(tags);
    }
}
