package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Art;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;

public class EntityPainting implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.PAINTING;
    }

    public static EntityPainting getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPainting((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "painting_width", "painting_height", "painting"
    };

    public static final String[] handledMechs = new String[] {
            "painting"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityPainting(dEntity entity) {
        painting = entity;
    }

    dEntity painting;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return ((Painting) painting.getBukkitEntity()).getArt().name();
    }

    @Override
    public String getPropertyId() {
        return "painting";
    }

    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.painting_width>
        // @returns ElementTag
        // @mechanism dEntity.painting
        // @group properties
        // @description
        // If the entity is a painting, returns its width.
        // -->
        if (attribute.startsWith("painting_width")) {
            return new ElementTag(((Painting) painting.getBukkitEntity()).getArt().getBlockWidth())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.painting_height>
        // @returns ElementTag
        // @mechanism dEntity.painting
        // @group properties
        // @description
        // If the entity is a painting, returns its height.
        // -->
        if (attribute.startsWith("painting_height")) {
            return new ElementTag(((Painting) painting.getBukkitEntity()).getArt().getBlockHeight())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.painting>
        // @returns ElementTag
        // @mechanism dEntity.painting
        // @group properties
        // @description
        // If the entity is a painting, returns what art it shows.
        // -->
        if (attribute.startsWith("painting")) {
            return new ElementTag(((Painting) painting.getBukkitEntity()).getArt().name())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name painting
        // @input Element
        // @description
        // Changes the art shown by a painting.
        // @tags
        // <e@entity.painting>
        // -->

        if (mechanism.matches("painting") && mechanism.requireEnum(false, Art.values())) {
            ((Painting) painting.getBukkitEntity()).setArt(Art.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
