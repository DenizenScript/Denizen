package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;

public class MaterialHinge extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name hinge
    // @input ElementTag
    // @description
    // Controls a door's hinge side, either LEFT or RIGHT.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Door;
    }

    MaterialTag material;

    @Override
    public String getPropertyId() {
        return "hinge";
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getDoor().getHinge());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireEnum(Door.Hinge.class)) {
            getDoor().setHinge(value.asEnum(Door.Hinge.class));
        }
    }

    public static void register() {
        autoRegister("hinge", MaterialHinge.class, ElementTag.class, true);
    }

    public Door getDoor() {
        return (Door) material.getModernData();
    }
}
