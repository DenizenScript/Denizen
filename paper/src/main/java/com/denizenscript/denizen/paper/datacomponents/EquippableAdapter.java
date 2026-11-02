package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.entity.DenizenEntityType;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;

public class EquippableAdapter extends DataComponentAdapter.Valued<MapTag, Equippable> {

    // <--[property]
    // @object ItemTag
    // @name equippable
    // @input MapTag
    // @description
    // Controls the properties of an item's equippable <@link language Item Components>.
    // The map includes keys:
    // - "slot", an ElementTag representing the equipment slot. Valid values: HAND, OFF_HAND, FEET, LEGS, CHEST, HEAD, BODY.
    // - "equip_sound", an ElementTag representing the sound played when equipping this item in namespaced key format.
    // - "asset_id", an ElementTag representing the asset id for this item in namespaced key format.
    // - "camera_overlay", an ElementTag representing the camera overlay to use when the item is equipped in namespaced key format.
    // - "allowed_entities", a ListTag(EntityTag) representing entity types that can equip this item. If not set, all entities are allowed to wear this item.
    // - "dispensable", a ElementTag(Boolean) controlling whether the item can be dispensed.
    // - "swappable", a ElementTag(Boolean) controlling whether the item can be swapped.
    // - "damage_on_hurt", a ElementTag(Boolean) controlling whether the item takes damage when the wearer is hurt.
    // - "equip_on_interact", a ElementTag(Boolean) controlling whether the item is equipped on entity interaction.
    // - "can_be_sheared", a ElementTag(Boolean) controlling whether the item can be sheared off an entity.
    // - "shear_sound", an ElementTag representing the sound played when shearing using this item in namespaced key format.
    // @mechanism
    // Provide no input to reset the item to its default value.
    // -->

    public EquippableAdapter() {
        super(MapTag.class, DataComponentTypes.EQUIPPABLE, "equippable");
    }

    @Override
    public MapTag toDenizen(Equippable value) {
        MapTag map = new MapTag();
        map.putObject("slot", new ElementTag(value.slot()));
        map.putObject("equip_sound", new ElementTag(value.equipSound().asMinimalString(), true));
        if (value.assetId() != null) {
            map.putObject("asset_id", new ElementTag(value.assetId().asMinimalString(), true));
        }
        if (value.cameraOverlay() != null) {
            map.putObject("camera_overlay", new ElementTag(value.cameraOverlay().asMinimalString(), true));
        }
        if (value.allowedEntities() != null) {
            ListTag allowedEntities = new ListTag(value.allowedEntities().values(), key -> new EntityTag(DenizenEntityType.getByName(key.key().value())));
            map.putObject("allowed_entities", allowedEntities);
        }
        map.putObject("dispensable", new ElementTag(value.dispensable()));
        map.putObject("swappable", new ElementTag(value.swappable()));
        map.putObject("damage_on_hurt", new ElementTag(value.damageOnHurt()));
        map.putObject("equip_on_interact", new ElementTag(value.equipOnInteract()));
        map.putObject("can_be_sheared", new ElementTag(value.canBeSheared()));
        map.putObject("shear_sound", new ElementTag(value.shearSound().asMinimalString(), true));
        return map;
    }

    @Override
    public Equippable fromDenizen(MapTag value, Mechanism mechanism) {
        ElementTag slot = value.getElement("slot");
        if (slot == null) {
            mechanism.echoError("Equippable map must have a 'slot' key.");
            return null;
        }
        if (!slot.matchesEnum(EquipmentSlot.class)) {
            mechanism.echoError("Invalid 'slot' specified for equippable: must be a valid EquipmentSlot.");
            return null;
        }
        Equippable.Builder builder = Equippable.equippable(slot.asEnum(EquipmentSlot.class));
        setIfValid(builder::equipSound, value, "equip_sound", ElementTag.class, null, element -> Utilities.parseNamespacedKey(element.asString()), "namespaced key", mechanism);
        setIfValid(builder::assetId, value, "asset_id", ElementTag.class, null, element -> Utilities.parseNamespacedKey(element.asString()), "namespaced key", mechanism);
        setIfValid(builder::cameraOverlay, value, "camera_overlay", ElementTag.class, null, element -> Utilities.parseNamespacedKey(element.asString()), "namespaced key", mechanism);
        setIfValid(builder::dispensable, value, "dispensable", ElementTag.class, ElementTag::isBoolean, ElementTag::asBoolean, "boolean", mechanism);
        setIfValid(builder::swappable, value, "swappable", ElementTag.class, ElementTag::isBoolean, ElementTag::asBoolean, "boolean", mechanism);
        setIfValid(builder::damageOnHurt, value, "damage_on_hurt", ElementTag.class, ElementTag::isBoolean, ElementTag::asBoolean, "boolean", mechanism);
        setIfValid(builder::equipOnInteract, value, "equip_on_interact", ElementTag.class, ElementTag::isBoolean, ElementTag::asBoolean, "boolean", mechanism);
        setIfValid(builder::canBeSheared, value, "can_be_sheared", ElementTag.class, ElementTag::isBoolean, ElementTag::asBoolean, "boolean", mechanism);
        setIfValid(builder::shearSound, value, "shear_sound", ElementTag.class, null, element -> Utilities.parseNamespacedKey(element.asString()), "namespaced key", mechanism);
        ListTag entityList = value.getObjectAs("allowed_entities", ListTag.class, mechanism.context);
        if (entityList != null) {
            List<TypedKey<EntityType>> keys = new ArrayList<>(entityList.size());
            for (String entry : entityList) {
                keys.add(TypedKey.create(RegistryKey.ENTITY_TYPE, Utilities.parseNamespacedKey(entry)));
            }
            builder.allowedEntities(RegistrySet.keySet(RegistryKey.ENTITY_TYPE, keys));
        }
        return builder.build();
    }
}
