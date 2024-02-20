package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.FallingBlock;

public class EntityAutoExpire implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof FallingBlock;
    }

    public static EntityAutoExpire getFrom(ObjectTag _entity) {
        if (!describes(_entity)) {
            return null;
        }
        else {
            return new EntityAutoExpire((EntityTag) _entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "auto_expire"
    };

    public EntityAutoExpire(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.auto_expire>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.auto_expire
        // @group properties
        // @Plugin Paper
        // @description
        // Returns whether a falling_block will auto-expire (after 30 seconds, or 5 when outside the world).
        // See also <@link tag EntityTag.time_lived>
        // -->
        PropertyParser.registerTag(EntityAutoExpire.class, ElementTag.class, "auto_expire", (attribute, object) -> {
            return new ElementTag(object.doesAutoExpire());
        });
    }

    public FallingBlock getFallingBlock() {
        return (FallingBlock) entity.getBukkitEntity();
    }

    public boolean doesAutoExpire() {
        return getFallingBlock().doesAutoExpire();
    }

    @Override
    public String getPropertyString() {
        return doesAutoExpire() ? null : "false";
    }

    @Override
    public String getPropertyId() {
        return "auto_expire";
    }


    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name auto_expire
        // @input ElementTag(Boolean)
        // @Plugin Paper
        // @group properties
        // @description
        // Sets whether a falling_block will auto-expire (after 30 seconds, or 5 when outside the world).
        // See also <@link mechanism EntityTag.time_lived>
        // @tags
        // <EntityTag.auto_expire>
        // -->
        if (mechanism.matches("auto_expire") && mechanism.requireBoolean()) {
            getFallingBlock().shouldAutoExpire(mechanism.getValue().asBoolean());
        }
    }
}
