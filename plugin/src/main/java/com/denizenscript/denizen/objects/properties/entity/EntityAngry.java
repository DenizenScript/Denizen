package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Wolf;

public class EntityAngry extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name angry
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a Wolf or PigZombie is angry, or whether a Vindicator is in "Johnny" mode.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof PigZombie
                || entity.getBukkitEntity() instanceof Wolf
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && entity.getBukkitEntity() instanceof Vindicator);
    }

    @Override
    public boolean isDefaultValue(ElementTag val) {
        return !val.asBoolean();
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof PigZombie entity) {
            return new ElementTag(entity.isAngry());
        }
        else if (getEntity() instanceof Wolf entity) {
            return new ElementTag(entity.isAngry());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && getEntity() instanceof Vindicator entity) {
            return new ElementTag(entity.isJohnny());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            if (getEntity() instanceof PigZombie entity) {
                entity.setAngry(param.asBoolean());
            }
            else if (getEntity() instanceof Wolf entity) {
                entity.setAngry(param.asBoolean());
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && getEntity() instanceof Vindicator entity) {
                entity.setJohnny(param.asBoolean());
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "angry";
    }

    public static void register() {
        autoRegister("angry", EntityAngry.class, ElementTag.class, false);
    }
}
