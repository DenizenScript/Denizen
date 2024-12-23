package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.BlockFace;
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
    // For bell values, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/data/type/Bell.Attachment.html>
    // For all other supported type values, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/data/FaceAttachable.AttachedFace.html>
    // -->

    public static boolean describes(MaterialTag material) {
        return material.getModernData() instanceof FaceAttachable || material.getModernData() instanceof Bell;
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
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (getBlockData() instanceof FaceAttachable attachable) {
            if (mechanism.requireEnum(FaceAttachable.AttachedFace.class)) {
                attachable.setAttachedFace(value.asEnum(FaceAttachable.AttachedFace.class));
            }
        }
        else if (getBlockData() instanceof Bell bell) {
            if (mechanism.requireEnum(Bell.Attachment.class)) {
                bell.setAttachment(value.asEnum(Bell.Attachment.class));
            }
        }
    }

    public BlockFace getAttachedTo() {
        if (getBlockData() instanceof FaceAttachable attachable) {
            return switch (attachable.getAttachedFace()) {
                case WALL -> getBlockData() instanceof Directional directional ? directional.getFacing().getOppositeFace() : BlockFace.SELF;
                case FLOOR -> BlockFace.DOWN;
                case CEILING -> BlockFace.UP;
            };
        }
        else if (getBlockData() instanceof Bell bell) {
            return switch (bell.getAttachment()) {
                case SINGLE_WALL, DOUBLE_WALL -> ((Directional) getBlockData()).getFacing();
                case FLOOR -> BlockFace.DOWN;
                case CEILING -> BlockFace.UP;
            };
        }
        return null;
    }

    public static void register() {
        autoRegister("attachment_face", MaterialAttachmentFace.class, ElementTag.class, false, "switch_face");
    }
}
