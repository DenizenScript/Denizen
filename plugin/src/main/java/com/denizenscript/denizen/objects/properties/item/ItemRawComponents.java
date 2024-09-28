package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class ItemRawComponents extends ItemProperty<MapTag> {

    public static final String DATA_VERSION_KEY = "denizen:__data_version";
    public static final String ENTITY_DATA_PROPERTY = "minecraft:entity_data";
    public static final Set<String> propertyHandledComponents = new HashSet<>();

    public static void registerHandledComponent(String component) {
        propertyHandledComponents.add("minecraft:" + component);
    }

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() != Material.AIR;
    }

    @Override
    public MapTag getPropertyValue() {
        MapTag rawComponents = NMSHandler.itemHelper.getRawComponents(getItemStack(), true);
        MapTag entityData = (MapTag) rawComponents.getObject(ENTITY_DATA_PROPERTY);
        if (entityData != null) {
            switch (entityData.getElement("id").asString()) {
                case "string:minecraft:item_frame" -> entityData.remove("Invisible");
                case "string:minecraft:armor_stand" -> {
                    entityData.remove("Pose");
                    entityData.remove("Small");
                    entityData.remove("NoBasePlate");
                    entityData.remove("Marker");
                    entityData.remove("Invisible");
                    entityData.remove("ShowArms");
                }
            }
            if (entityData.size() == 1) { // Just "id"
                rawComponents.remove(ENTITY_DATA_PROPERTY);
                if (rawComponents.size() == 1) { // Just the data version
                    rawComponents = new MapTag();
                }
            }
        }
        return rawComponents;
    }

    @Override
    public boolean isDefaultValue(MapTag value) {
        return value.isEmpty();
    }

    @Override
    public void setPropertyValue(MapTag value, Mechanism mechanism) {
        ElementTag dataVersionInput = value.getElement(DATA_VERSION_KEY);
        int dataVersion;
        if (dataVersionInput == null) {
            dataVersion = Integer.MAX_VALUE;
        }
        else if (!dataVersionInput.isInt()) {
            mechanism.echoError("Invalid data version '" + dataVersionInput + "' specified: must be a valid non-decimal number.");
            return;
        }
        else {
            dataVersion = dataVersionInput.asInt();
            value.remove(DATA_VERSION_KEY);
        }
        setItemStack(NMSHandler.itemHelper.setRawComponents(getItemStack(), value, dataVersion, mechanism::echoError));
    }

    @Override
    public String getPropertyId() {
        return "raw_components";
    }

    public static void register() {
        autoRegister("raw_components", ItemRawComponents.class, MapTag.class, false);
        PropertyParser.registerTag(ItemRawComponents.class, MapTag.class, "all_raw_components", (attribute, property) -> {
            return NMSHandler.itemHelper.getRawComponents(property.getItemStack(), false);
        });
    }
}
