package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntityScoreboardTags implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static EntityScoreboardTags getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityScoreboardTags((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "scoreboard_tags"
    };

    public static final String[] handledMechs = new String[] {
            "scoreboard_tags", "clear_scoreboard_tags"
    };

    public EntityScoreboardTags(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    public ListTag getTags() {
        return new ListTag(entity.getBukkitEntity().getScoreboardTags());
    }

    @Override
    public String getPropertyString() {
        ListTag tags = getTags();
        if (tags.isEmpty()) {
            return null;
        }
        return tags.identify();
    }

    @Override
    public String getPropertyId() {
        return "scoreboard_tags";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.scoreboard_tags>
        // @returns ListTag
        // @mechanism EntityTag.scoreboard_tags
        // @group attributes
        // @description
        // Returns a list of the scoreboard tags on the entity.
        // -->
        if (attribute.startsWith("scoreboard_tags")) {
            return getTags().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name clear_scoreboard_tags
        // @input None
        // @description
        // Clears the list of the scoreboard tags on the entity.
        // @tags
        // <EntityTag.scoreboard_tags>
        // -->
        if (mechanism.matches("clear_scoreboard_tags")) {
            for (String str : getTags()) {
                entity.getBukkitEntity().removeScoreboardTag(str);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name scoreboard_tags
        // @input ListTag
        // @description
        // Adds the list of the scoreboard tags to the entity.
        // To clear existing scoreboard tags, use <@link mechanism EntityTag.clear_scoreboard_tags>.
        // @tags
        // <EntityTag.scoreboard_tags>
        // -->
        if (mechanism.matches("scoreboard_tags") && mechanism.hasValue()) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            for (String str : list) {
                entity.getBukkitEntity().addScoreboardTag(str);
            }
        }
    }
}
