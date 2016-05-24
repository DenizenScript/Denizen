package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.minecraft.server.v1_9_R2.ItemStack;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;

public class ItemUnbreakable implements Property {

    public static boolean describes(dObject object) {
        return object instanceof dItem;
    }

    public static ItemUnbreakable getFrom(dObject object) {
        if (!describes(object)) {
            return null;
        }
        return new ItemUnbreakable((dItem) object);
    }

    private ItemUnbreakable(dItem item) {
        this.item = item;
    }

    dItem item;

    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.unbreakable>
        // @returns Element(Boolean)
        // @group properties
        // @mechanism dItem.unbreakable
        // @description
        // Returns whether an item has the unbreakable flag.
        // -->
        if (attribute.startsWith("unbreakable")) {
            return new Element(getPropertyString() != null).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public String getPropertyString() {
        ItemStack itemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        if (itemStack != null && itemStack.hasTag() &&
                itemStack.getTag().getBoolean("Unbreakable")) {
            return "true";
        }
        return null;
    }

    public String getPropertyId() {
        return "unbreakable";
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name unbreakable
        // @input Element(Boolean)
        // @description
        // Changes whether an item has the unbreakable item flag.
        // @tags
        // <i@item.unbreakable>
        // -->
        if (mechanism.matches("unbreakable") && mechanism.requireBoolean()) {
            ItemStack itemStack = CraftItemStack.asNMSCopy(item.getItemStack());
            NBTTagCompound tag = itemStack.hasTag() ? itemStack.getTag() : new NBTTagCompound();
            tag.setInt("Unbreakable", mechanism.getValue().asBoolean() ? 1 : 0);
            itemStack.setTag(tag);
            item.setItemStack(CraftItemStack.asBukkitCopy(itemStack));
        }
    }
}
