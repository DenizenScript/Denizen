package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;

public class ItemCustomData extends ItemProperty<MapTag> {

    // <--[property]
    // @object ItemTag
    // @name custom_data
    // @input MapTag
    // @description
    // Controls an item's custom NBT data, if any.
    // The map is in NBT format, see <@link language Raw NBT Encoding>.
    // This does not include any normal vanilla data (enchantments, lore, etc.), just extra custom data.
    // This is useful for integrating with items from external systems (such as custom items from plugins), but item flags should be preferred otherwise.
    // @mechanism
    // Provide no input to clear custom data.
    // @tag-example
    // # Use to check if an item has custom data from another plugin.
    // - if <[item].custom_data.get[external_plugin_data].if_null[null]> == external_custom_item:
    //   - narrate "You are using an item from an external plugin!"
    // -->

    // Custom data added by Denizen
    public static final String[] DENIZEN_DATA = new String[] { "Denizen Item Script", "DenizenItemScript", "Denizen NBT", "Denizen" };

    public static boolean describes(ItemTag item) {
        return !item.getBukkitMaterial().isAir();
    }

    public ItemCustomData(ItemTag item) {
        this.object = item;
    }

    @Override
    public MapTag getPropertyValue() {
        CompoundBinaryTag customData = NMSHandler.itemHelper.getCustomData(getItemStack());
        if (customData == null) {
            return null;
        }
        if (customData.isEmpty()) {
            return new MapTag();
        }
        MapTag dataMap = (MapTag) ItemRawNBT.nbtTagToObject(customData);
        for (String denizenKey : DENIZEN_DATA) {
            dataMap.remove(denizenKey);
        }
        return dataMap.isEmpty() ? null : dataMap;
    }

    @Override
    public void setPropertyValue(MapTag value, Mechanism mechanism) {
        if (value == null) {
            setItemStack(NMSHandler.itemHelper.setCustomData(getItemStack(), addDenizenKeys(null)));
            return;
        }
        CompoundBinaryTag customData;
        try {
            customData = (CompoundBinaryTag) ItemRawNBT.convertObjectToNbt(value, mechanism.context, "(data)");
        }
        catch (Exception ex) {
            mechanism.echoError("Invalid custom data specified:");
            Debug.echoError(ex);
            return;
        }
        if (customData == null) {
            mechanism.echoError("Invalid custom data specified.");
            return;
        }
        setItemStack(NMSHandler.itemHelper.setCustomData(getItemStack(), addDenizenKeys(customData)));
    }

    private CompoundBinaryTag addDenizenKeys(CompoundBinaryTag tag) {
        CompoundBinaryTag currentData = NMSHandler.itemHelper.getCustomData(getItemStack());
        if (currentData == null || currentData.isEmpty()) {
            return tag;
        }
        CompoundBinaryTag.Builder tagBuilder = null;
        for (String denizenKey : DENIZEN_DATA) {
            BinaryTag denizenValue = currentData.get(denizenKey);
            if (denizenValue != null) {
                if (tagBuilder == null) {
                    // TODO: adventure-nbt: compound tag builder
                    tagBuilder = tag != null ? CompoundBinaryTag.builder().put(tag) : CompoundBinaryTag.builder();
                }
                tagBuilder.put(denizenKey, denizenValue);
            }
        }
        return tagBuilder != null ? tagBuilder.build() : tag;
    }

    @Override
    public String getPropertyId() {
        return "custom_data";
    }

    public static void register() {
        autoRegisterNullable("custom_data", ItemCustomData.class, MapTag.class, false);
    }
}
