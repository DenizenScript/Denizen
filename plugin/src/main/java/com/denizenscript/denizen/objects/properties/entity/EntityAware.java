package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Mob;

public class EntityAware extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name is_aware
    // @input ElementTag(Boolean)
    // @description
    // For mobs (<@link tag EntityTag.is_mob>), this is whether the entity is aware of its surroundings.
    // Unaware entities will not perform any actions on their own, such as pathfinding or attacking.
    // Similar to <@link property EntityTag.has_ai>, except allows the entity to be moved by gravity, being pushed or attacked, etc.
    // For interaction entities, this is whether interacting with them should trigger a response (arm swings, sounds, etc.).
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Mob || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && entity.getBukkitEntity() instanceof Interaction);
    }

    @Override
    public ElementTag getPropertyValue() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getEntity() instanceof Interaction interaction) {
            return new ElementTag(interaction.isResponsive());
        }
        return new ElementTag(as(Mob.class).isAware());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        // Default value is true for mobs, false for interaction entities
        return value.asBoolean() == getEntity() instanceof Mob;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (!mechanism.requireBoolean()) {
            return;
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getEntity() instanceof Interaction interaction) {
            interaction.setResponsive(value.asBoolean());
            return;
        }
        as(Mob.class).setAware(value.asBoolean());
    }

    @Override
    public String getPropertyId() {
        return "is_aware";
    }

    public static void register() {
        autoRegister("is_aware", EntityAware.class, ElementTag.class, false);
    }
}
