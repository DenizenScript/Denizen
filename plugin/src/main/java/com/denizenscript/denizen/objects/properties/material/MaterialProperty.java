package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import org.bukkit.block.data.BlockData;

public abstract class MaterialProperty<TData extends ObjectTag> extends ObjectProperty<MaterialTag, TData> {

    public MaterialProperty() {
    }

    public MaterialProperty(MaterialTag material) {
        object = material;
    }

    public BlockData getBlockData() {
        return object.getModernData();
    }
}
