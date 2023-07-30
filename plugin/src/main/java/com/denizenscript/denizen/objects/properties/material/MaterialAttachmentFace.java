package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Bell;

public class MaterialAttachmentFace extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name attachment_face
    // @input ElementTag
    // @description
    // Controls the current attach direction for attachable materials such as switches, grindstones, and bells.
    // Values are "CEILING", "FLOOR", or "WALL". For bells, values are "CEILING", "FLOOR", "SINGLE_WALL", or "DOUBLE_WALL".
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof FaceAttachable || data instanceof Bell;
    }

    public MaterialAttachmentFace(MaterialTag material) {
        super(material);
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getBlockData() instanceof FaceAttachable attachable) {
            return new ElementTag(attachable.getAttachedFace());
        }
        else if (getBlockData() instanceof Bell bell) {
            return new ElementTag(bell.getAttachment());
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "attachment_face";
    }

    @Override
    public void setPropertyValue(ElementTag attachment, Mechanism mechanism) {
        if (getBlockData() instanceof FaceAttachable attachable) {
            if (mechanism.requireEnum(FaceAttachable.AttachedFace.class)) {
                attachable.setAttachedFace(FaceAttachable.AttachedFace.valueOf(attachment.asString().toUpperCase()));
            }
        }
        else if (getBlockData() instanceof Bell bell) {
            if (mechanism.requireEnum(Bell.Attachment.class)) {
                bell.setAttachment(Bell.Attachment.valueOf(attachment.asString().toUpperCase()));
            }
        }
    }

    public BlockFace getAttachedTo() {
        if (getBlockData() instanceof FaceAttachable attachable) {
            return switch (attachable.getAttachedFace()) {
                case WALL -> {
                    if (getBlockData() instanceof Directional) {
                        yield ((Directional) getBlockData()).getFacing().getOppositeFace();
                    }
                    yield BlockFace.SELF;
                }
                case FLOOR -> BlockFace.DOWN;
                case CEILING -> BlockFace.UP;
            };
        }
        else if (getBlockData() instanceof Bell bell) {
            return switch (bell.getAttachment()) {
                case SINGLE_WALL -> ((Directional) getBlockData()).getFacing();
                case FLOOR -> BlockFace.DOWN;
                case CEILING -> BlockFace.UP;
                default -> null;
            };
        }
        return null;
    }

    public static void register() {
        autoRegister("attachment_face", MaterialAttachmentFace.class, ElementTag.class, false, "switch_face");
    }
}
