package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.inventory.meta.MapMeta;

public class ItemMap implements Property {

    public static Material MAP_MATERIAL = NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) ? Material.FILLED_MAP : Material.MAP;

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && (((dItem) item).getItemStack().getType() == MAP_MATERIAL);
    }

    public static ItemMap getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemMap((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[]{
            "map"
    };

    public static final String[] handledMechs = new String[] {
            "map"
    };


    private ItemMap(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.map>
        // @returns Element(Number)
        // @group properties
        // @mechanism dItem.map
        // @description
        // Returns the ID number of the map item's map.
        // -->
        if (attribute.startsWith("map")) {
            return new Element(getMapId())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public int getMapId() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            MapMeta map = (MapMeta) item.getItemStack().getItemMeta();
            return map.getMapId();
        }
        return item.getItemStack().getDurability();
    }

    public void setMapId(int id) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            MapMeta map = (MapMeta) item.getItemStack().getItemMeta();
            map.setMapId(id);
            item.getItemStack().setItemMeta(map);
        }
        item.getItemStack().setDurability((short) (id));
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getMapId());
    }

    @Override
    public String getPropertyId() {
        return "map";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name map
        // @input Element(Number)
        // @description
        // Changes what map ID number a map item uses.
        // @tags
        // <i@item.map>
        // -->

        if (mechanism.matches("map") && mechanism.requireInteger()) {
            setMapId(mechanism.getValue().asInt());
        }
    }
}
