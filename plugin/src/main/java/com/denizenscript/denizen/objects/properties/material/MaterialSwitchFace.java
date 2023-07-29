package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Bell;

public class MaterialSwitchFace extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name switch_face
    // @input ElementTag
    // @description
    // Controls the current attach direction for a switch or other attachable material.
    // Values are "CEILING", "FLOOR", or "WALL". For bells, values are "CEILING", "FLOOR", "SINGLE_WALL", or "DOUBLE_WALL".
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof FaceAttachable || data instanceof Bell;
    }

    public static MaterialSwitchFace getFrom(MaterialTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialSwitchFace(_material);
        }
    }

    public MaterialSwitchFace(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    @Override
    public ElementTag getPropertyValue() {
        if (getData() instanceof FaceAttachable attachable) {
            return new ElementTag(attachable.getAttachedFace());
        }
        else if (getData() instanceof Bell bell) {
            return new ElementTag(bell.getAttachment());
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "switch_face";
    }

    @Override
    public void setPropertyValue(ElementTag attachment, Mechanism mechanism) {
        if (getData() instanceof FaceAttachable attachable) {
            if (mechanism.requireEnum(FaceAttachable.AttachedFace.class)) {
                attachable.setAttachedFace(FaceAttachable.AttachedFace.valueOf(attachment.asString().toUpperCase()));
            }
        }
        else if (getData() instanceof Bell bell) {
            if (mechanism.requireEnum(Bell.Attachment.class)) {
                bell.setAttachment(Bell.Attachment.valueOf(attachment.asString().toUpperCase()));
            }
        }
    }

    public BlockFace getAttachedTo() {
        if (getData() instanceof FaceAttachable attachable) {
            return switch (attachable.getAttachedFace()) {
                case WALL -> {
                    if (getData() instanceof Directional) {
                        yield ((Directional) getData()).getFacing().getOppositeFace();
                    }
                    yield BlockFace.SELF;
                }
                case FLOOR -> BlockFace.DOWN;
                case CEILING -> BlockFace.UP;
            };
        }
        return null;
    }

    public static void register() {
        autoRegister("switch_face", MaterialSwitchFace.class, ElementTag.class, false);
    }

    private BlockData getData() {
        return material.getModernData();
    }
}
