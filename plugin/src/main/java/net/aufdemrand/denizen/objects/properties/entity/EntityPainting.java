package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Art;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;

public class EntityPainting implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.PAINTING;
    }

    public static EntityPainting getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPainting((dEntity) entity);
        }
    }

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
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.painting_width>
        // @returns Element
        // @mechanism dEntity.painting
        // @group properties
        // @description
        // If the entity is a painting, returns its width.
        // -->
        if (attribute.startsWith("painting_width")) {
            return new Element(((Painting) painting.getBukkitEntity()).getArt().getBlockWidth())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.painting_height>
        // @returns Element
        // @mechanism dEntity.painting
        // @group properties
        // @description
        // If the entity is a painting, returns its height.
        // -->
        if (attribute.startsWith("painting_height")) {
            return new Element(((Painting) painting.getBukkitEntity()).getArt().getBlockHeight())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.painting>
        // @returns Element
        // @mechanism dEntity.painting
        // @group properties
        // @description
        // If the entity is a painting, returns what art it shows.
        // -->
        if (attribute.startsWith("painting")) {
            return new Element(((Painting) painting.getBukkitEntity()).getArt().name())
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
