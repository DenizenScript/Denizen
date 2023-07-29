package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.type.Bell;

public class MaterialAttachment extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name attachment
    // @input ElementTag
    // @description
    // Controls the attachment of a bell.
    // For types of attachment, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/data/type/Bell.Attachment.html>
    // -->

    public static boolean describes(MaterialTag material) {
        return material.getModernData() instanceof Bell;
    }

    @Override
    public ElementTag getPropertyValue() {
        Bell bell = (Bell) getBlockData();
        return new ElementTag(bell.getAttachment().toString());
    }

    @Override
    public String getPropertyId() {
        return "attachment";
    }

    @Override
    public void setPropertyValue(ElementTag attachment, Mechanism mechanism) {
        if (!mechanism.requireEnum(Bell.Attachment.class)) {
            return;
        }
        Bell bell = (Bell) getBlockData();
        bell.setAttachment(Bell.Attachment.valueOf(attachment.toString().toUpperCase()));
    }

    public static void register() {
        autoRegister("attachment", MaterialAttachment.class, ElementTag.class, false);
    }
}
