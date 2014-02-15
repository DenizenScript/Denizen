package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.Mechanism;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.potion.Potion;

public class ItemSplash implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getType() == Material.POTION;
    }

    public static ItemSplash getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemSplash((dItem)_item);
    }


    public boolean isSplash() {
        return Potion.fromItemStack(item.getItemStack()).isSplash();
    }

    public void setSplash(boolean isSplash) {
        Potion.fromItemStack(item.getItemStack()).setSplash(isSplash);
    }


    private ItemSplash(dItem item) {
        this.item = item;
    }

    dItem item;


    @Override
    public String getPropertyString() {
        if (isSplash()) return "true";
        else return null;
    }

    @Override
    public String getPropertyId() {
        return "splash";
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.is_splash>
        // @returns Element(Boolean)
        // @description
        // Returns whether or not this potion is a splash potion.
        // -->
        if (attribute.startsWith("is_splash")) {
            return new Element(isSplash())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name splash
        // @input Element
        // @description
        // Sets whether this potion is a splash potion.
        // @tags
        // <i@item.is_splash>
        // -->
        if (mechanism.matches("splash") && mechanism.requireBoolean()) {
            setSplash(mechanism.getValue().asBoolean());
        }

    }
}
