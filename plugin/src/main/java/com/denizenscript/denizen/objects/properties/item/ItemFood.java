package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;

public class ItemFood extends ItemProperty<MapTag> {

    // <--[property]
    // @object ItemTag
    // @name food
    // @input MapTag
    // @description
    // Controls the food properties of the item.
    // A food item has the 'nutrition', 'saturation', and 'can_always_eat' properties.
    // The 'nutrition' is the amount of hunger points the food restores.
    // The 'saturation' is the amount of saturation points the food restores.
    // The 'can_always_eat' is a boolean indicating if the food can be eaten even when the player is not hungry.
    // @tag
    // If the item does not have food properties, this will return null.
    // @mechanism
    // Provide no input to entirely remove the food component from the item.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() != Material.AIR;
    }

    @Override
    public MapTag getPropertyValue() {
        if (getItemMeta().hasFood()) {
            FoodComponent food = getItemMeta().getFood();
            MapTag map = new MapTag();
            map.putObject("nutrition", new ElementTag(food.getNutrition()));
            map.putObject("saturation", new ElementTag(food.getSaturation()));
            map.putObject("can_always_eat", new ElementTag(food.canAlwaysEat()));
            return map;
        }
        return null;
    }

    @Override
    public void setPropertyValue(MapTag map, Mechanism mechanism) {
        editMeta(ItemMeta.class, meta -> {
            if (map == null) {
                meta.setFood(null);
                return;
            }
            FoodComponent food = meta.getFood();
            ElementTag nutrition = map.getElement("nutrition");
            if (nutrition != null) {
                food.setNutrition(nutrition.asInt());
            }
            ElementTag saturation = map.getElement("saturation");
            if (saturation != null) {
                food.setSaturation(saturation.asFloat());
            }
            ElementTag canAlwaysEat = map.getElement("can_always_eat");
            if (canAlwaysEat != null) {
                food.setCanAlwaysEat(canAlwaysEat.asBoolean());
            }
            meta.setFood(food);
        });
    }

    @Override
    public String getPropertyId() {
        return "food";
    }

    public static void register() {
        autoRegisterNullable("food", ItemFood.class, MapTag.class, false);
    }
}
