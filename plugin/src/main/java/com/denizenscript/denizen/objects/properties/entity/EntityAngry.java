package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Wolf;

public class EntityAngry implements Property {

    public static boolean describes(ObjectTag object) {
        if (!(object instanceof EntityTag)) {
            return false;
        }
        Entity entity = ((EntityTag) object).getBukkitEntity();
        return entity instanceof Wolf
                || entity instanceof PigZombie
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && entity instanceof Vindicator);
    }

    public static EntityAngry getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAngry((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "angry"
    };

    public EntityAngry(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        if (isWolf()) {
            return getWolf().isAngry() ? "true" : null;
        }
        else if (isPigZombie()) {
            return getPigZombie().isAngry() ? "true" : null;
        }
        else if (isVindicator()) {
            return getVindicator().isJohnny() ? "true" : null;
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "angry";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.angry>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.angry
        // @group properties
        // @description
        // If the entity is a wolf or PigZombie, returns whether the entity is angry.
        // If the entity is a Vindicator, returns whether it is in "Johnny" mode.
        // -->
        PropertyParser.registerTag(EntityAngry.class, ElementTag.class, "angry", (attribute, entity) -> {
            if (entity.isWolf()) {
                return new ElementTag(entity.getWolf().isAngry());
            }
            else if (entity.isPigZombie()) {
                return new ElementTag(entity.getPigZombie().isAngry());
            }
            else if (entity.isVindicator()) {
                return new ElementTag(entity.getVindicator().isJohnny());
            }
            return null;
        });
    }

    public boolean isWolf() {
        return entity.getBukkitEntity() instanceof Wolf;
    }

    public boolean isPigZombie() {
        return entity.getBukkitEntity() instanceof PigZombie;
    }

    public boolean isVindicator() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18) && entity.getBukkitEntity() instanceof Vindicator;
    }

    public Wolf getWolf() {
        return (Wolf) entity.getBukkitEntity();
    }

    public PigZombie getPigZombie() {
        return (PigZombie) entity.getBukkitEntity();
    }

    public Vindicator getVindicator() {
        return (Vindicator) entity.getBukkitEntity();
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name angry
        // @input ElementTag(Boolean)
        // @description
        // If the entity is wolf or PigZombie, sets whether the entity is angry.
        // If the entity is a Vindicator, returns whether it is in "Johnny" mode.
        // @tags
        // <EntityTag.angry>
        // -->
        if (mechanism.matches("angry") && mechanism.requireBoolean()) {
            if (isWolf()) {
                getWolf().setAngry(mechanism.getValue().asBoolean());
            }
            else if (isPigZombie()) {
                getPigZombie().setAngry(mechanism.getValue().asBoolean());
            }
            else if (isVindicator()) {
                getVindicator().setJohnny(mechanism.getValue().asBoolean());
            }
        }
    }
}
