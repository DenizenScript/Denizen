package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Armadillo;

public class EntityState extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name state
    // @input ElementTag
    // @description
    // Controls the current state of an armadillo.
    // Valid states are IDLE, ROLLING, SCARED, and UNROLLING.
    // The entity may roll or unroll due to normal vanilla conditions. If this is not desired, disable <@link mechanism EntityTag.has_ai>.
    // -->

    public static boolean describes(EntityTag entity) {
          return entity.getBukkitEntity() instanceof Armadillo;
    }

    @Override
    public ElementTag getPropertyValue() {
        return NMSHandler.entityHelper.getArmadilloState(as(Armadillo.class));
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        NMSHandler.entityHelper.setArmadilloState(as(Armadillo.class), mechanism, param);
    }

    @Override
    public String getPropertyId() {
        return "state";
    }

    public static void register() {
        autoRegister("state", EntityState.class, ElementTag.class, false);
    }
}
