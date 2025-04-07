package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.inventory.meta.FireworkMeta;

public class ItemFireworkPower extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name firework_power
    // @input ElementTag(Number)
    // @description
    // Controls the firework's power.
    // Power primarily affects how high the firework flies, with each level of power corresponding to approximately half a second of additional flight time.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getItemMeta() instanceof FireworkMeta;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(FireworkMeta.class).getPower());
    }

    @Override
    public String getPropertyId() {
        return "firework_power";
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            editMeta(FireworkMeta.class, fireworkMeta -> fireworkMeta.setPower(value.asInt()));
        }
    }

    public static void register() {
        autoRegister("firework_power", ItemFireworkPower.class, ElementTag.class, false);
    }
}
