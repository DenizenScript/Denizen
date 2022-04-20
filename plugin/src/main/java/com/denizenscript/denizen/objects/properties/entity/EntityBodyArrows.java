package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class EntityBodyArrows implements Property {

    public static boolean describes(ObjectTag object) {
        return object instanceof EntityTag
                && ((EntityTag) object).isLivingEntity();
    }

    public static EntityBodyArrows getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityBodyArrows((EntityTag) object);
        }
    }

    public static final String[] handledMechs = new String[] {
            "body_arrows", "clear_body_arrows"
    };

    private EntityBodyArrows(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    private int getBodyArrows() {
        return NMSHandler.entityHelper.getBodyArrows(entity.getBukkitEntity());
    }

    private void setBodyArrows(int numArrows) {
        NMSHandler.entityHelper.setBodyArrows(entity.getBukkitEntity(), numArrows);
    }

    @Override
    public String getPropertyString() {
        int numArrows = getBodyArrows();
        return numArrows == 0 ? null : String.valueOf(numArrows);
    }

    @Override
    public String getPropertyId() {
        return "body_arrows";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.body_arrows>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.body_arrows
        // @group properties
        // @description
        // Returns the number of arrows stuck in the entity's body.
        // Note: Body arrows will only be visible for players or player-type npcs.
        // -->
        PropertyParser.<EntityBodyArrows, ElementTag>registerTag(ElementTag.class, "body_arrows", (attribute, object) -> {
            return new ElementTag(object.getBodyArrows());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name body_arrows
        // @input ElementTag(Number)
        // @description
        // Sets the number of arrows stuck in the entity's body.
        // Note: Body arrows will only be visible for players or player-type npcs.
        // @tags
        // <EntityTag.body_arrows>
        // -->
        if (mechanism.matches("body_arrows") && mechanism.requireInteger()) {
            setBodyArrows(mechanism.getValue().asInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name clear_body_arrows
        // @input None
        // @description
        // Clears all arrows stuck in the entity's body.
        // Note: Body arrows will only be visible for players or player-type npcs.
        // @tags
        // <EntityTag.body_arrows>
        // -->
        if (mechanism.matches("clear_body_arrows")) {
            setBodyArrows(0);
        }
    }
}
