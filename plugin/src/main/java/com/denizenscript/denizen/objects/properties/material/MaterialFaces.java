package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;

public class MaterialFaces implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof MultipleFacing;
    }

    public static MaterialFaces getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialFaces((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "faces"
    };

    public MaterialFaces(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.valid_faces>
        // @returns ListTag
        // @mechanism MaterialTag.faces
        // @group properties
        // @description
        // Returns a list of faces that are valid for a material that has multiple faces.
        // See also <@link tag MaterialTag.faces>
        // -->
        PropertyParser.registerStaticTag(MaterialFaces.class, ListTag.class, "valid_faces", (attribute, material) -> {
            ListTag toReturn = new ListTag();
            for (BlockFace face : material.getFaces().getAllowedFaces()) {
                toReturn.add(face.name());
            }
            return toReturn;
        });

        // <--[tag]
        // @attribute <MaterialTag.faces>
        // @returns ListTag
        // @mechanism MaterialTag.faces
        // @group properties
        // @description
        // Returns a list of the current faces for a material that has multiple faces (like a mushroom block).
        // Output is a direction name like "NORTH".
        // -->
        PropertyParser.registerStaticTag(MaterialFaces.class, ListTag.class, "faces", (attribute, material) -> {
            return material.getFaceList();
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

    @Override
    public String getPropertyString() {
        return getFaceList().identify();
    }

    @Override
    public String getPropertyId() {
        return "faces";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name faces
        // @input ListTag
        // @description
        // Sets the current faces for a material that has multiple faces (like a mushroom block).
        // @tags
        // <MaterialTag.faces>
        // <MaterialTag.valid_faces>
        // -->
        if (mechanism.matches("faces")) {
            MultipleFacing facing = getFaces();
            for (BlockFace face : facing.getAllowedFaces()) {
                facing.setFace(face, false);
            }
            for (String faceName : mechanism.valueAsType(ListTag.class)) {
                facing.setFace(BlockFace.valueOf(faceName.toUpperCase()), true);
            }
        }
    }
}
