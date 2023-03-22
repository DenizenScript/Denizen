package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import org.bukkit.block.data.BlockData;

public abstract class MaterialProperty extends ObjectProperty<MaterialTag> {

    public MaterialProperty() {
    }

    public MaterialProperty(MaterialTag material) {
        object = material;
    }

    public BlockData getBlockData() {
        return object.getModernData();
    }
}
