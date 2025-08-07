package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizen.paper.utilities.DataComponentAdapter;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.FoodProperties;

public class FoodAdapter extends DataComponentAdapter<FoodProperties, MapTag> {

    // <--[property]
    // @object ItemTag
    // @name food
    // @input MapTag
    // @description
    // Controls an item's food <@link language Item Components>.
    // The map includes keys:
    // - "nutrition", ElementTag(Number) representing the amount of food points restored by this item.
    // - "saturation", ElementTag(Decimal) representing the amount of saturation points restored by this item.
    // - "can_always_eat", ElementTag(Boolean) controlling whether the item can always be eaten, even if the player isn't hungry.
    // -->

    // <--[tag]
    // @attribute <MaterialTag.food>
    // @returns MapTag
    // @description
    // Gets the material's default food value, in the same format as <@link tag ItemTag.food>.
    // -->

    public FoodAdapter() {
        super(DataComponentTypes.FOOD, MapTag.class, "food");
    }

    @Override
    public MapTag toDenizen(FoodProperties value) {
        MapTag foodData = new MapTag();
        foodData.putObject("nutrition", new ElementTag(value.nutrition()));
        foodData.putObject("saturation", new ElementTag(value.saturation()));
        foodData.putObject("can_always_eat", new ElementTag(value.canAlwaysEat()));
        return foodData;
    }

    @Override
    public FoodProperties toPaper(MapTag value, Mechanism mechanism) {
        FoodProperties.Builder builder = FoodProperties.food();
        setIfValid(builder::nutrition, value, "nutrition", "number", ElementTag::isInt, ElementTag::asInt, mechanism);
        setIfValid(builder::saturation, value, "saturation", "decimal number", ElementTag::isFloat, ElementTag::asFloat, mechanism);
        setIfValid(builder::canAlwaysEat, value, "can_always_eat", "boolean", ElementTag::isBoolean, ElementTag::asBoolean, mechanism);
        return builder.build();
    }
}
