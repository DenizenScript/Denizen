package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;

public class MaterialPersistent extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name persistent
    // @input ElementTag(Boolean)
    // @description
    // Controls whether this block will decay from being too far away from a tree.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Leaves;
    }

    MaterialTag material;

    @Override
    public String getPropertyId() {
        return "persistent";
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getLeaves().isPersistent());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        getLeaves().setPersistent(value.asBoolean());
    }

    public static void register() {
        autoRegister("persistent", MaterialPersistent.class, ElementTag.class, true);
    }

    public Leaves getLeaves() {
        return (Leaves) material.getModernData();
    }
}
