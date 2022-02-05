package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.npc.traits.InvisibleTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;

public class EntityVisible implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                (((EntityTag) entity).getBukkitEntity() instanceof ArmorStand
                || ((EntityTag) entity).getBukkitEntity() instanceof ItemFrame);
    }

    public static EntityVisible getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityVisible((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "visible"
    };

    public static final String[] handledMechs = new String[] {
            "visible"
    };

    private EntityVisible(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    public boolean isVisible() {
        if (entity.getBukkitEntity() instanceof ArmorStand) {
            return ((ArmorStand) entity.getBukkitEntity()).isVisible();
        }
        else {
            return ((ItemFrame) entity.getBukkitEntity()).isVisible();
        }
    }

    @Override
    public String getPropertyString() {
        if (!isVisible()) {
            return "false";
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "visible";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.visible>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.visible
        // @group attributes
        // @description
        // Returns whether the armor stand or item frame is visible.
        // -->
        if (attribute.startsWith("visible")) {
            return new ElementTag(isVisible()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name visible
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the armor stand or item frame is visible.
        // @tags
        // <EntityTag.visible>
        // -->
        if (mechanism.matches("visible") && mechanism.requireBoolean()) {
            if (Depends.citizens != null && entity.isLivingEntity() && CitizensAPI.getNPCRegistry().isNPC(entity.getLivingEntity())) {
                InvisibleTrait.setInvisible(entity.getLivingEntity(), CitizensAPI.getNPCRegistry().getNPC(entity.getBukkitEntity()), !mechanism.getValue().asBoolean());
            }
            else if (entity.getBukkitEntity() instanceof ArmorStand) {
                ((ArmorStand) entity.getBukkitEntity()).setVisible(mechanism.getValue().asBoolean());
            }
            else {
                ((ItemFrame) entity.getBukkitEntity()).setVisible(mechanism.getValue().asBoolean());
            }
        }
    }
}
