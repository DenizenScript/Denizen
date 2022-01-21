package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.properties.material.MaterialDirectional;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.block.data.type.Jigsaw;

public class MultiVersionHelper1_16 { // TODO: 1.16

    public static void materialDirectionalRunMech(Mechanism mechanism, MaterialDirectional object) {
        if (object.isJigsaw() && mechanism.requireEnum(false, Jigsaw.Orientation.values())) {
            ((Jigsaw) object.material.getModernData()).setOrientation(Jigsaw.Orientation.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
