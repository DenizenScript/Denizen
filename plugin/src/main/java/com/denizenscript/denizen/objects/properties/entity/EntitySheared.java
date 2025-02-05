package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.*;

public class EntitySheared extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name sheared
    // @synonyms has_pumpkin_head
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a sheep is sheared, a bogged is harvested, or a snow golem is derped (ie not wearing a pumpkin).
    // To include drops or for harvesting mushroom cows consider <@link mechanism EntityTag.shear>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Sheep
                || entity.getBukkitEntity() instanceof Snowman
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && entity.getBukkitEntity() instanceof Bogged);
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof Sheep sheep) {
            return new ElementTag(sheep.isSheared());
        }
        else if (getEntity() instanceof Snowman snowman) {
            return new ElementTag(snowman.isDerp());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Bogged bogged) {
            return new ElementTag(bogged.isSheared());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (!mechanism.requireBoolean()) {
            return;
        }
        if (getEntity() instanceof Sheep sheep) {
            sheep.setSheared(param.asBoolean());
        }
        else if (getEntity() instanceof Snowman snowman) {
            snowman.setDerp(param.asBoolean());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getEntity() instanceof Bogged bogged) {
            bogged.setSheared(param.asBoolean());
        }
    }


    @Override
    public String getPropertyId() {
        return "sheared";
    }

    public static void register() {
        autoRegister("sheared", EntitySheared.class, ElementTag.class, false);

        // <--[tag]
        // @attribute <EntityTag.is_sheared>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @deprecated use 'EntityTag.sheared'
        // @description
        // Deprecated in favor of <@link tag EntityTag.sheared>.
        // -->
        PropertyParser.registerTag(EntitySheared.class, ElementTag.class, "is_sheared", (attribute, prop) -> {
            BukkitImplDeprecations.entityIsSheared.warn(attribute.context);
            return prop.getPropertyValue();
        });

        // <--[tag]
        // @attribute <EntityTag.has_pumpkin_head>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.has_pumpkin_head
        // @group properties
        // @deprecated use 'EntityTag.sheared'
        // @description
        // Deprecated in favor of <@link tag EntityTag.sheared>.
        // -->
        PropertyParser.registerTag(EntitySheared.class, ElementTag.class, "has_pumpkin_head", (attribute, prop) -> {
            BukkitImplDeprecations.entityIsSheared.warn(attribute.context);
            if (!(prop.getEntity() instanceof Snowman)) {
                return null;
            }
            return new ElementTag(!prop.getPropertyValue().asBoolean());
        });

        // <--[mechanism]
        // @object EntityTag
        // @name has_pumpkin_head
        // @input ElementTag(Boolean)
        // @description
        // @deprecated use 'EntityTag.sheared'
        // Deprecated in favor of <@link mechanism EntityTag.sheared>.
        // @tags
        // <EntityTag.has_pumpkin_head>
        // -->
        PropertyParser.registerMechanism(EntitySheared.class, ElementTag.class, "has_pumpkin_head", (prop, mechanism, input) -> {
            BukkitImplDeprecations.entityIsSheared.warn(mechanism.context);
            if (!(prop.getEntity() instanceof Snowman)) {
                return;
            }
            prop.setPropertyValue(new ElementTag(!input.asBoolean()), mechanism);
        });
    }
}

