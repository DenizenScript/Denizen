package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;

public class MaterialFaces extends MaterialProperty<ListTag> {

    // <--[property]
    // @object MaterialTag
    // @name valid_faces
    // @input ListTag
    // @description
    // Controls the current faces of a material that has multiple faces (like a mushroom block).
    // -->

    public static boolean describes(MaterialTag material) {
        return material.getModernData() instanceof MultipleFacing;
    }

    @Override
    public String getPropertyId() {
        return "faces";
    }

    @Override
    public ListTag getPropertyValue() {
        return new ListTag(((MultipleFacing) getBlockData()).getFaces(), ElementTag::new);
    }

    @Override
    public void setPropertyValue(ListTag list, Mechanism mechanism) {
        MultipleFacing facing = (MultipleFacing) getBlockData();
        for (BlockFace face : facing.getAllowedFaces()) {
            facing.setFace(face, false);
        }
        for (String faceName : list) {
            if (mechanism.requireEnum(BlockFace.class)) {
                facing.setFace(new ElementTag(faceName).asEnum(BlockFace.class), true);
            }
        }
    }

    public static void register() {
        autoRegister("faces", MaterialFaces.class, ListTag.class, true);

        // <--[tag]
        // @attribute <MaterialTag.valid_faces>
        // @returns ListTag
        // @mechanism MaterialTag.faces
        // @group properties
        // @description
        // Returns a list of faces that are valid for a material that has multiple faces.
        // See also <@link property MaterialTag.faces>
        // -->
        PropertyParser.registerStaticTag(MaterialFaces.class, ListTag.class, "valid_faces", (attribute, material) -> {
            return new ListTag(((MultipleFacing) material.getBlockData()).getAllowedFaces(), ElementTag::new);
        });
    }
}
