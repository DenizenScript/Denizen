package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.*;

public class EntitySize extends EntityProperty<ElementTag> {
    // TODO: once 26.2 is the minimum supported version, remove Slime usage in favor of AbstractCubeMob

    // <--[property]
    // @object EntityTag
    // @name size
    // @input ElementTag
    // @description
    // Controls the size of an entity.
    // Cube-type (slime, magma cube, sulfur cube) mob sizes are between 1 and 127.
    // Phantom mob sizes are between 0 and 64.
    // Pufferfish mob sizes are between 0 and 2.
    // -->

    public static boolean describes(EntityTag entityTag) {
        Entity entity = entityTag.getBukkitEntity();
        return entity instanceof Phantom
                || entity instanceof PufferFish
                || entity instanceof Slime
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v26_2) && entity instanceof AbstractCubeMob);
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof Phantom phantom) {
            return new ElementTag(phantom.getSize());
        }
        else if (getEntity() instanceof PufferFish pufferfish) {
            return new ElementTag(pufferfish.getPuffState());
        }
        else if (getEntity() instanceof Slime slime) {
            return new ElementTag(slime.getSize());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v26_2) && getEntity() instanceof AbstractCubeMob cube) {
            return new ElementTag(cube.getSize());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (!mechanism.requireInteger()) {
            return;
        }
        if (getEntity() instanceof Phantom phantom) {
            phantom.setSize(value.asInt());
        }
        else if (getEntity() instanceof PufferFish pufferfish) {
            pufferfish.setPuffState(value.asInt());
        }
        else if (getEntity() instanceof Slime slime) {
            slime.setSize(value.asInt());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v26_2) && getEntity() instanceof AbstractCubeMob cube) {
            cube.setSize(value.asInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "size";
    }

    public static void register() {
        autoRegister("size", EntitySize.class, ElementTag.class, false);
    }
}
