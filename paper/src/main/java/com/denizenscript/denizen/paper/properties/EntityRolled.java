package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Armadillo;

public class EntityRolled extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name rolled
    // @input ElementTag(Boolean)
    // @plugin Paper
    // @description
    // Controls whether an armadillo is rolled or not.
    // The entity may roll up or unroll during normal vanilla conditions. If this is not desired, disable <@link mechanism EntityTag.has_ai>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Armadillo;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Armadillo.class).getState() != Armadillo.State.IDLE);
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            if (param.asBoolean()) {
                if (as(Armadillo.class).getState() == Armadillo.State.IDLE) {
                    as(Armadillo.class).rollUp();
                }
                else {
                    mechanism.echoError("Armadillo is already rolled.");
                }
            }
            else {
                if (as(Armadillo.class).getState() != Armadillo.State.IDLE) {
                    as(Armadillo.class).rollOut();
                }
                else {
                    mechanism.echoError("Armadillo is already unrolled.");
                }
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "rolled";
    }

    public static void register() {
        autoRegister("rolled", EntityRolled.class, ElementTag.class, false);
    }
}
