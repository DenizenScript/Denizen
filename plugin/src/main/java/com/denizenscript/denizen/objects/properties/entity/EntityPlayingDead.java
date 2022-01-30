package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
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
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && ((EntityTag) entity).getBukkitEntity() instanceof Axolotl;
    }

    public static EntityPlayingDead getFrom(ObjectTag _entity) {
        if (!describes(_entity)) {
            return null;
        } else {
            return new EntityPlayingDead((EntityTag) _entity);
        }
    }

    public static final String[] handledMechs = new String[]{
        "playing_dead"
    };

    private EntityPlayingDead(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void registerTags() {
        PropertyParser.<EntityPlayingDead, ElementTag>registerTag(ElementTag.class, "playing_dead", (attribute, entity) -> {
            return new ElementTag(((Axolotl) entity.entity.getBukkitEntity()).isPlayingDead());
        });
    }

    @Override
    public String getPropertyString() {
        return ((Axolotl) entity.getBukkitEntity()).isPlayingDead() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "EntityPlayingDead";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name playing_dead
        // @input ElementTag(Boolean)
        // @description
        // If the entity is an axolotl, changes whether the entity is playing dead.
        // This won't be successful unless the entity is unaware of its surroundings. See <@mech
        // @tags
        // <EntityTag.playing_dead>
        // -->
        if (mechanism.matches("playing_dead") && mechanism.requireBoolean()) {
            ((Axolotl) entity.getBukkitEntity()).setPlayingDead(mechanism.getValue().asBoolean());
        }
    }
}