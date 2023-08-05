package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.npc.traits.SneakingTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class EntitySneaking extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name is_sneaking
    // @input ElementTag(Boolean)
    // @plugin Paper
    // @description
    // Whether an entity is sneaking.
    // For most entities this just makes the name tag less visible, and doesn't actually update the pose.
    // Note that <@link command sneak> is also available.
    // -->

    public static boolean describes(EntityTag entity) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getEntity().isSneaking());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return !value.asBoolean();
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            boolean sneaking = value.asBoolean();
            getEntity().setSneaking(sneaking);
            if (object.isCitizensNPC()) {
                SneakingTrait sneakingTrait = object.getDenizenNPC().getCitizen().getOrAddTrait(SneakingTrait.class);
                if (sneaking) {
                    sneakingTrait.sneak();
                }
                else {
                    sneakingTrait.stand();
                }
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "is_sneaking";
    }

    public static void register() {
        autoRegister("is_sneaking", EntitySneaking.class, ElementTag.class, false);
    }
}
