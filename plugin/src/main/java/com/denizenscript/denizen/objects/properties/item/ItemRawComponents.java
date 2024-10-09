package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemRawComponents extends ItemProperty<MapTag> {

    public static final String DATA_VERSION_KEY = "denizen:__data_version";
    public static final String ENTITY_DATA_COMPONENT = "minecraft:entity_data";
    public static final StringHolder INSTRUMENT_COMPONENT = new StringHolder("minecraft:instrument");
    public static final String BLOCK_ENTITY_DATA_COMPONENT = "minecraft:block_entity_data";
    public static final Map<String, Set<StringHolder>> ENTITY_DATA_TO_REMOVE = new HashMap<>();
    public static final Set<String> propertyHandledComponents = new HashSet<>();

    public static void registerEntityDataRemove(EntityType type, String... dataKeys) {
        Set<StringHolder> keysSet = new HashSet<>(dataKeys.length + 1);
        keysSet.add(new StringHolder("id"));
        for (String dataKey : dataKeys) {
            keysSet.add(new StringHolder(dataKey));
        }
        ENTITY_DATA_TO_REMOVE.put("string:" + type.getKey(), keysSet);
    }

    public static void registerHandledComponent(String component) {
        propertyHandledComponents.add("minecraft:" + component);
    }

    static {
        registerEntityDataRemove(EntityType.ITEM_FRAME, "Invisible");
        registerEntityDataRemove(EntityType.ARMOR_STAND, "Pose", "Small", "NoBasePlate", "Marker", "Invisible", "ShowArms");
    }

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() != Material.AIR;
    }

    @Override
    public MapTag getPropertyValue() {
        MapTag rawComponents = NMSHandler.itemHelper.getRawComponents(getItemStack(), true);
        MapTag entityData = (MapTag) rawComponents.getObject(ENTITY_DATA_COMPONENT);
        if (entityData != null) {
            Set<StringHolder> keysToRemove = ENTITY_DATA_TO_REMOVE.get(entityData.getElement("id").asString());
            if (keysToRemove != null && keysToRemove.containsAll(entityData.keySet())) {
                rawComponents.remove(ENTITY_DATA_COMPONENT);
            }
        }
        rawComponents.map.computeIfPresent(INSTRUMENT_COMPONENT, (key, value) -> value instanceof ElementTag ? null : value);
        MapTag blockEntityData = (MapTag) rawComponents.getObject(BLOCK_ENTITY_DATA_COMPONENT);
        if (blockEntityData != null && blockEntityData.size() == 4 && blockEntityData.getElement("id").asString().endsWith("sign")
                && blockEntityData.containsKey("front_text") && blockEntityData.containsKey("back_text")
                && blockEntityData.getElement("is_waxed").asString().equals("byte:0")) {
            rawComponents.remove(BLOCK_ENTITY_DATA_COMPONENT);
        }
        if (rawComponents.size() == 1) { // Just the data version
            return new MapTag();
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
