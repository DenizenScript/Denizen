package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.npc.traits.InvisibleTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;

public class EntityVisible implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                (((EntityTag) entity).getBukkitEntity() instanceof ArmorStand
                || ((EntityTag) entity).getBukkitEntity() instanceof ItemFrame
                || ((EntityTag) entity).isLivingEntity());
    }

    public static EntityVisible getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityVisible((EntityTag) entity);
        }
    }

    private EntityVisible(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    public boolean isVisible() {
        if (isArmorStand()) {
            return getArmorStand().isVisible();
        }
        else if (isItemFrame()) {
            return getItemFrame().isVisible();
        }
        else {
            return !entity.getLivingEntity().isInvisible();
        }
    }

    @Override
    public String getPropertyString() {
        return isVisible() ? null : "false";
    }

    @Override
    public String getPropertyId() {
        return "visible";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.visible>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.visible
        // @group attributes
        // @description
        // Returns whether the entity is visible.
        // Supports armor stands, item frames, and living entities.
        // -->
        PropertyParser.registerTag(EntityVisible.class, ElementTag.class, "visible", (attribute, object) -> {
            return new ElementTag(object.isVisible());
        });

        // <--[mechanism]
        // @object EntityTag
        // @name visible
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the entity is visible.
        // Supports armor stands, item frames, and living entities.
        // @tags
        // <EntityTag.visible>
        // -->
        PropertyParser.registerMechanism(EntityVisible.class, ElementTag.class, "visible", (object, mechanism, input) -> {
            if (mechanism.requireBoolean()) {
                if (object.entity.isCitizensNPC()) {
                    InvisibleTrait.setInvisible(object.entity.getLivingEntity(), object.entity.getDenizenNPC().getCitizen(), !input.asBoolean());
                }
                else if (object.isArmorStand()) {
                    object.getArmorStand().setVisible(input.asBoolean());
                }
                else if (object.isItemFrame()) {
                    object.getItemFrame().setVisible(input.asBoolean());
                }
                else {
                    object.entity.getLivingEntity().setInvisible(!input.asBoolean());
                }
            }
        });
    }

    public boolean isArmorStand() {
        return entity.getBukkitEntity() instanceof ArmorStand;
    }

    public boolean isItemFrame() {
        return entity.getBukkitEntity() instanceof ItemFrame;
    }

    public ArmorStand getArmorStand() {
        return (ArmorStand) entity.getBukkitEntity();
    }

    public ItemFrame getItemFrame() {
        return (ItemFrame) entity.getBukkitEntity();
    }
}
