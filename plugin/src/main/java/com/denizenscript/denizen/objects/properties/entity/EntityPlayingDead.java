package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Axolotl;

public class EntityPlayingDead implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        return ((EntityTag) entity).getBukkitEntity() instanceof Axolotl;
    }

    public static EntityPlayingDead getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        } else {
            return new EntityPlayingDead((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
        "playing_dead"
    };

    public EntityPlayingDead(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.playing_dead>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.playing_dead
        // @group properties
        // @description
        // If the entity is an axolotl, returns whether the entity is playing dead.
        // -->
        PropertyParser.registerTag(EntityPlayingDead.class, ElementTag.class, "playing_dead", (attribute, entity) -> {
            return new ElementTag(((Axolotl) entity.entity.getBukkitEntity()).isPlayingDead());
        });
    }

    @Override
    public String getPropertyString() {
        return ((Axolotl) entity.getBukkitEntity()).isPlayingDead() ? "true" : null;
    }

    @Override
    public String getPropertyId() {
        return "playing_dead";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name playing_dead
        // @input ElementTag(Boolean)
        // @description
        // If the entity is an axolotl, sets whether the entity is playing dead.
        // This won't be successful unless the entity is unaware of its surroundings. See <@link mechanism EntityTag.is_aware>.
        // @tags
        // <EntityTag.playing_dead>
        // -->
        if (mechanism.matches("playing_dead") && mechanism.requireBoolean()) {
            ((Axolotl) entity.getBukkitEntity()).setPlayingDead(mechanism.getValue().asBoolean());
        }
    }
}
