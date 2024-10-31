package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;

public class MaterialFaces extends MaterialProperty<ListTag> {

    // <--[property]
    // @object MaterialTag
    // @name valid_faces
    // @input ListTag
    // @description
    // Controls a list of the current faces for a material that has multiple faces (like a mushroom block).
    // For valid faces, see <@link tag MaterialTag.valid_faces>.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof MultipleFacing;
    }

    MaterialTag material;

    @Override
    public String getPropertyId() {
        return "faces";
    }

    @Override
    public ListTag getPropertyValue() {
        return getFaceList();
    }

    @Override
    public void setPropertyValue(ListTag list, Mechanism mechanism) {
        MultipleFacing facing = getFaces();
        for (BlockFace face : facing.getAllowedFaces()) {
            facing.setFace(face, false);
        }
        for (String faceName : list) {
            facing.setFace(BlockFace.valueOf(faceName.toUpperCase()), true);
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
            ListTag toReturn = new ListTag();
            for (BlockFace face : material.getFaces().getAllowedFaces()) {
                toReturn.add(face.name());
            }
            return toReturn;
        });
    }

    public ListTag getFaceList() {
        ListTag toReturn = new ListTag();
        for (BlockFace face : getFaces().getFaces()) {
            toReturn.add(face.name());
        }
        return toReturn;
    }

    public MultipleFacing getFaces() {
        return (MultipleFacing) material.getModernData();
    }
}
