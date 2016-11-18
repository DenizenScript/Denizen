package net.aufdemrand.denizen.utilities.nbt;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.util.jnbt.*;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CustomNBT {

    public static final String KEY_DENIZEN = "Denizen NBT";

    private static final String KEY_ATTRIBUTES = "AttributeModifiers";

    public static MapOfEnchantments getEnchantments(ItemStack item) {
        return new MapOfEnchantments(item);
    }

    /*
     * Some static methods for dealing with Minecraft NBT data, which is used to store
     * custom NBT.
     */

    public static class AttributeReturn {
        public String attr;
        public String slot;
        public int op;
        public double amt;
        public long uuidMost;
        public long uuidLeast;
    }

    public static List<AttributeReturn> getAttributes(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        List<CompoundTag> attribs = new ArrayList<CompoundTag>();
        if (compoundTag.getValue().containsKey(KEY_ATTRIBUTES)) {
            List<Tag> temp = (List<Tag>) compoundTag.getValue().get(KEY_ATTRIBUTES).getValue();
            for (Tag tag : temp) {
                attribs.add((CompoundTag) tag);
            }
        }

        List<AttributeReturn> attrs = new ArrayList<AttributeReturn>();

        for (int i = 0; i < attribs.size(); i++) {
            CompoundTag ct = attribs.get(i);
            AttributeReturn atr = new AttributeReturn();
            atr.attr = (String) ct.getValue().get("AttributeName").getValue();
            atr.slot = (String) ct.getValue().get("Slot").getValue();
            atr.op = (Integer) ct.getValue().get("Operation").getValue();
            Tag t = ct.getValue().get("Amount");
            if (t instanceof IntTag) {
                atr.amt = (Integer) t.getValue();
            }
            else if (t instanceof LongTag) {
                atr.amt = (Long) t.getValue();
            }
            else if (t instanceof DoubleTag) {
                atr.amt = (Double) t.getValue();
            }
            else {
                /// ????
                atr.amt = 0;
            }
            t = ct.getValue().get("UUIDMost");
            if (t instanceof LongTag) {
                atr.uuidMost = (Long) t.getValue();
            }
            else if (t instanceof IntTag) {
                atr.uuidMost = (Integer) t.getValue();
            }
            t = ct.getValue().get("UUIDLeast");
            if (t instanceof LongTag) {
                atr.uuidLeast = (Long) t.getValue();
            }
            else if (t instanceof IntTag) {
                atr.uuidLeast = (Integer) t.getValue();
            }
            attrs.add(atr);
        }

        return attrs;
    }

    public static ItemStack addAttribute(ItemStack itemStack, String attr, String slot, int op, double amt) {
        if (itemStack == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        List<CompoundTag> attribs = new ArrayList<CompoundTag>();
        if (compoundTag.getValue().containsKey(KEY_ATTRIBUTES)) {
            List<Tag> temp = (List<Tag>) compoundTag.getValue().get(KEY_ATTRIBUTES).getValue();
            for (Tag tag : temp) {
                attribs.add((CompoundTag) temp);
            }
        }

        HashMap<String, Tag> tmap = new HashMap<String, Tag>();

        tmap.put("AttributeName", new StringTag(attr));
        tmap.put("Name", new StringTag(attr));
        tmap.put("Slot", new StringTag(slot));
        tmap.put("Operation", new IntTag(op));
        tmap.put("Amount", new DoubleTag(amt));

        UUID t = UUID.randomUUID();

        tmap.put("UUIDMost", new LongTag(t.getMostSignificantBits()));
        tmap.put("UUIDLeast", new LongTag(t.getLeastSignificantBits()));

        CompoundTag ct = NMSHandler.getInstance().createCompoundTag(tmap);
        attribs.add(ct);
        ListTag lt = new ListTag(CompoundTag.class, attribs);
        compoundTag = compoundTag.createBuilder().put(KEY_ATTRIBUTES, lt).build();

        return NMSHandler.getInstance().getItemHelper().setNbtData(itemStack, compoundTag);
    }

    public static ItemStack addCustomNBT(ItemStack itemStack, String key, String value, String basekey) {
        if (itemStack == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey(basekey)) {
            denizenTag = (CompoundTag) compoundTag.getValue().get(basekey);
        }
        else {
            denizenTag = NMSHandler.getInstance().createCompoundTag(new HashMap<String, Tag>());
        }

        // Add custom NBT
        denizenTag = denizenTag.createBuilder().putString(CoreUtilities.toLowerCase(key), value).build();

        compoundTag = compoundTag.createBuilder().put(basekey, denizenTag).build();

        // Write tag back
        return NMSHandler.getInstance().getItemHelper().setNbtData(itemStack, compoundTag);
    }

    public static ItemStack removeCustomNBT(ItemStack itemStack, String key, String basekey) {
        if (itemStack == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey(basekey)) {
            denizenTag = (CompoundTag) compoundTag.getValue().get(basekey);
        }
        else {
            return itemStack;
        }

        // Remove custom NBT
        denizenTag = denizenTag.createBuilder().remove(CoreUtilities.toLowerCase(key)).build();

        compoundTag = compoundTag.createBuilder().put(basekey, denizenTag).build();

        // Write tag back
        return NMSHandler.getInstance().getItemHelper().setNbtData(itemStack, compoundTag);
    }

    public static boolean hasCustomNBT(ItemStack itemStack, String key, String basekey) {
        if (itemStack == null) {
            return false;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey(basekey)) {
            denizenTag = (CompoundTag) compoundTag.getValue().get(basekey);
        }
        else {
            return false;
        }

        return denizenTag.getValue().containsKey(CoreUtilities.toLowerCase(key));
    }

    public static String getCustomNBT(ItemStack itemStack, String key, String basekey) {
        if (itemStack == null || key == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        if (compoundTag.getValue().containsKey(basekey)) {
             CompoundTag denizenTag = (CompoundTag) compoundTag.getValue().get(basekey);
            return denizenTag.getString(CoreUtilities.toLowerCase(key));
        }

        return null;
    }

    public static List<String> listNBT(ItemStack itemStack, String basekey) {
        List<String> nbt = new ArrayList<String>();
        if (itemStack == null) {
            return nbt;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        if (compoundTag.getValue().containsKey(basekey)) {
            CompoundTag denizenTag = (CompoundTag) compoundTag.getValue().get(basekey);
            nbt.addAll(denizenTag.getValue().keySet());
        }

        return nbt;
    }

    public static Entity addCustomNBT(Entity entity, String key, String value) {
        if (entity == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getEntityHelper().getNbtData(entity);

        // Add custom NBT
        compoundTag = compoundTag.createBuilder().putString(key, value).build();

        // Write tag back
        NMSHandler.getInstance().getEntityHelper().setNbtData(entity, compoundTag);
        return entity;
    }

    public static Entity removeCustomNBT(Entity entity, String key) {
        if (entity == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getEntityHelper().getNbtData(entity);

        // Remove custom NBT
        compoundTag = compoundTag.createBuilder().remove(key).build();

        // Write tag back
        NMSHandler.getInstance().getEntityHelper().setNbtData(entity, compoundTag);
        return entity;
    }

    public static boolean hasCustomNBT(Entity entity, String key) {
        if (entity == null) {
            return false;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getEntityHelper().getNbtData(entity);

        // Check for key
        return compoundTag.getValue().containsKey(key);
    }

    public static String getCustomNBT(Entity entity, String key) {
        if (entity == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getEntityHelper().getNbtData(entity);

        // Return contents of the tag
        return compoundTag.getString(key);
    }
}


