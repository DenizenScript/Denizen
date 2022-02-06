package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Lantern;

public class MaterialAttachedToWall implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData() instanceof Gate
                || ((MaterialTag) material).getModernData() instanceof Lantern);
    }

    public static MaterialAttachedToWall getFrom(ObjectTag _material){
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialAttachedToWall((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "attached_to_wall"
    };

    private MaterialAttachedToWall(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.attached_to_wall>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.attached_to_wall
        // @group properties
        // @description
        // Returns whether this material is attached to a wall.
        // For a lantern, this returns true if it is hanging from the ceiling.
        // For a gate, this returns whether it is lowered to attach to a wall block.
        // -->
        PropertyParser.<MaterialAttachedToWall, ElementTag>registerStaticTag(ElementTag.class, "attached_to_wall", (attribute, material) -> {
            if (material.isGate()) {
                return new ElementTag(material.getGate().isInWall());
            }
            else if (material.isLantern()) {
                return new ElementTag(material.getLantern().isHanging());
            }
            else { // Unreachable
                return null;
            }
        });
    }

    public boolean isGate() { return material.getModernData() instanceof Gate; }

    public boolean isLantern() { return material.getModernData() instanceof Lantern; }

    public Gate getGate() { return (Gate) material.getModernData(); }

    public Lantern getLantern() { return (Lantern) material.getModernData(); }

    public boolean getAttachment() {
        if (isGate()) {
            return getGate().isInWall();
        }
        else if (isLantern()) {
            return getLantern().isHanging();
        }
        return false; // Unreachable
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getAttachment());
    }

    @Override
    public String getPropertyId() {
        return "attached_to_wall";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name attached_to_wall
        // @input ElementTag(Boolean)
        // @description
        // Sets whether a material is 'attached to a wall', which has a different meaning depending on the material type.
        // Refer to <@link tag MaterialTag.attached_to_wall> for specifics.
        // @tags
        // <MaterialTag.attached_to_wall>
        // -->
        if (mechanism.matches("attached_to_wall") && mechanism.requireBoolean()) {
            if (isGate()) {
                getGate().setInWall(mechanism.getValue().asBoolean());
            }
            else if (isLantern()) {
                getLantern().setHanging(mechanism.getValue().asBoolean());
            }
        }
    }
}
