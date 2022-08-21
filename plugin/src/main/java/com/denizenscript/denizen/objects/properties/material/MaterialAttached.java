package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Hangable;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Lantern;

public class MaterialAttached implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData() instanceof Gate
                || ((MaterialTag) material).getModernData() instanceof Lantern
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && ((MaterialTag) material).getModernData() instanceof Hangable));
    }

    public static MaterialAttached getFrom(ObjectTag _material){
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialAttached((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "attached", "attached_to_wall"
    };

    private MaterialAttached(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.attached>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.attached
        // @group properties
        // @description
        // Returns whether a material is attached.
        // For a lantern, this returns whether it is hanging from the ceiling.
        // For a gate, this returns whether it is lowered to attach to a wall block.
        // For a mangrove_propagule, this returns whether it is hanging from the block above it.
        // -->
        PropertyParser.registerStaticTag(MaterialAttached.class, ElementTag.class, "attached", (attribute, material) -> {
            if (material.isGate()) {
                return new ElementTag(material.getGate().isInWall());
            }
            else if (material.isLantern()) {
                return new ElementTag(material.getLantern().isHanging());
            }
            else if (material.isHangable()) {
                return new ElementTag(((Hangable) material.material.getModernData()).isHanging());
            }
            else { // Unreachable
                return null;
            }
        }, "attached_to_wall");
    }

    public boolean isGate() {
        return material.getModernData() instanceof Gate;
    }

    public boolean isLantern() { // TODO: 1.19 - Lantern extends Hangable
        return material.getModernData() instanceof Lantern;
    }

    public boolean isHangable() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && material.getModernData() instanceof Hangable;
    }

    public Gate getGate() {
        return (Gate) material.getModernData();
    }

    public Lantern getLantern() { // TODO: 1.19 - Lantern extends Hangable
        return (Lantern) material.getModernData();
    }

    /*public Hangable getHangable() { // TODO: 1.19
        return (Hangable) material.getModernData();
    }*/

    public boolean isAttached() {
        if (isGate()) {
            return getGate().isInWall();
        }
        else if (isLantern()) {
            return getLantern().isHanging();
        }
        else if (isHangable()) {
            return ((Hangable) material.getModernData()).isHanging(); // TODO: 1.19
        }
        return false; // Unreachable
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(isAttached());
    }

    @Override
    public String getPropertyId() {
        return "attached";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name attached
        // @input ElementTag(Boolean)
        // @description
        // Sets whether a material is attached.
        // For a lantern, this sets whether it is hanging from the ceiling.
        // For a gate, this sets whether it is lowered to attach to a wall block.
        // For a mangrove_propagule, this sets whether it is hanging from the block above it.
        // @tags
        // <MaterialTag.attached>
        // -->
        if ((mechanism.matches("attached") || mechanism.matches("attached_to_wall")) && mechanism.requireBoolean()) {
            if (isGate()) {
                getGate().setInWall(mechanism.getValue().asBoolean());
            }
            else if (isLantern()) {
                getLantern().setHanging(mechanism.getValue().asBoolean());
            }
            else if (isHangable()) {
                ((Hangable) material.getModernData()).setHanging(mechanism.getValue().asBoolean()); // TODO: 1.19
            }
        }
    }
}
