package net.aufdemrand.denizen.objects.properties.Item;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

public class ItemPlantgrowth implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && (
                ((dItem) item).getItemStack().getData() instanceof Crops
                || ((dItem) item).getItemStack().getData() instanceof NetherWarts
                || ((dItem) item).getItemStack().getData() instanceof CocoaPlant
                || ((dItem) item).getItemStack().getType().equals(Material.PUMPKIN_STEM)
                || ((dItem) item).getItemStack().getType().equals(Material.MELON_STEM)
                || ((dItem) item).getItemStack().getType().equals(Material.CARROT)
                || ((dItem) item).getItemStack().getType().equals(Material.POTATO)
                );
    }

    public static ItemPlantgrowth getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemPlantgrowth((dItem)_item);
    }


    private ItemPlantgrowth(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.plant_growth>
        // @returns Element
        // @description
        // Returns the growth level of a plant item as one of the following:
        // Wheat: SEEDED, GERMINATED, VERY_SMALL, SMALL, MEDIUM, TALL, VERY_TALL, RIPE
        // Nether Warts: SEEDED, STAGE_ONE, STAGE_TWO, RIPE
        // Cocoa Plants: SMALL, MEDIUM, LARGE
        // Pumpkin stem, melon stem, carrot, potato: 0-7
        // -->
        if (attribute.startsWith("plant_growth")) {
            if (item.getItemStack().getData() instanceof Crops)
                return new Element(((Crops)item.getItemStack().getData()).getState().name())
                    .getAttribute(attribute.fulfill(1));
            else if (item.getItemStack().getData() instanceof NetherWarts)
                return new Element(((NetherWarts)item.getItemStack().getData()).getState().name())
                        .getAttribute(attribute.fulfill(1));
            else if (item.getItemStack().getData() instanceof CocoaPlant)
                return new Element(((CocoaPlant)item.getItemStack().getData()).getSize().name())
                        .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        String state;
        if (item.getItemStack().getData() instanceof Crops)
            state = ((Crops)item.getItemStack().getData()).getState().name();
        else if (item.getItemStack().getData() instanceof NetherWarts)
            state = ((NetherWarts)item.getItemStack().getData()).getState().name();
        else if (item.getItemStack().getData() instanceof CocoaPlant)
            state = ((CocoaPlant)item.getItemStack().getData()).getSize().name();
        else
            state = String.valueOf(item.getItemStack().getData().getData());

        if (!state.equalsIgnoreCase("SEEDED") && !state.equalsIgnoreCase("0"))
            return state;
        else
            return null;
    }

    @Override
    public String getPropertyId() {
        return "plant_growth";
    }
}
