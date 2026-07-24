package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Weapon;

public class WeaponAdapter extends DataComponentAdapter.Valued<MapTag, Weapon> {

    // <--[property]
    // @object ItemTag
    // @name weapon
    // @input MapTag
    // @description
    // Controls an item's weapon <@link language Item Components>.
    // The map includes keys:
    // - "disable_blocking_duration", an ElementTag(Number) representing the number of seconds that a shield will be disabled for after blocking an attack from this item.
    // - "durability_per_attack", an ElementTag(Number) representing the amount of durability damage this item will take when used to attack an entity or break a block.
    // @mechanism
    // Provide no input to reset the item to its default value.
    // -->

    public WeaponAdapter() {
        super(MapTag.class, DataComponentTypes.WEAPON, "weapon");
    }

    @Override
    public MapTag toDenizen(Weapon value) {
        MapTag weaponData = new MapTag();
        weaponData.putObject("disable_blocking_duration", new ElementTag(value.disableBlockingForSeconds()));
        weaponData.putObject("durability_per_attack", new ElementTag(value.itemDamagePerAttack()));
        return weaponData;
    }

    @Override
    public Weapon fromDenizen(MapTag value, Mechanism mechanism) {
        Weapon.Builder builder = Weapon.weapon();
        setIfValid(builder::disableBlockingForSeconds, value, "disable_blocking_duration", ElementTag.class, ElementTag::isFloat, ElementTag::asFloat, "number", mechanism);
        setIfValid(builder::itemDamagePerAttack, value, "durability_per_attack", ElementTag.class, ElementTag::isInt, ElementTag::asInt, "number", mechanism);
        return builder.build();
    }
}
