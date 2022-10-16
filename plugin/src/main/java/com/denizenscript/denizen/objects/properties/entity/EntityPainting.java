package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Art;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;

public class EntityPainting implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntityType() == EntityType.PAINTING;
    }

    public static EntityPainting getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPainting((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "painting_width", "painting_height", "painting"
    };

    public static final String[] handledMechs = new String[] {
            "painting"
    };

    private EntityPainting(EntityTag entity) {
        painting = entity;
    }

    EntityTag painting;

    @Override
    public String getPropertyString() {
        return ((Painting) painting.getBukkitEntity()).getArt().name();
    }

    @Override
    public String getPropertyId() {
        return "painting";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.painting_width>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.painting
        // @group properties
        // @description
        // If the entity is a painting, returns its width.
        // -->
        if (attribute.startsWith("painting_width")) {
            return new ElementTag(((Painting) painting.getBukkitEntity()).getArt().getBlockWidth())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.painting_height>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.painting
        // @group properties
        // @description
        // If the entity is a painting, returns its height.
        // -->
        if (attribute.startsWith("painting_height")) {
            return new ElementTag(((Painting) painting.getBukkitEntity()).getArt().getBlockHeight())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.painting>
        // @returns ElementTag
        // @mechanism EntityTag.painting
        // @group properties
        // @description
        // If the entity is a painting, returns what art it shows.
        // See also <@link tag server.art_types>.
        // -->
        if (attribute.startsWith("painting")) {
            return new ElementTag(((Painting) painting.getBukkitEntity()).getArt())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name painting
        // @input ElementTag
        // @description
        // Changes the art shown by a painting. Valid a types: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Art.html>.
        // @tags
        // <EntityTag.painting>
        // <server.art_types>
        // -->
        if (mechanism.matches("painting") && mechanism.requireEnum(Art.class)) {
            Art art = Art.valueOf(mechanism.getValue().asString().toUpperCase());
            if (((Painting) painting.getBukkitEntity()).getArt() != art) {
                ((Painting) painting.getBukkitEntity()).setArt(art, true);
            }
        }
    }
}
