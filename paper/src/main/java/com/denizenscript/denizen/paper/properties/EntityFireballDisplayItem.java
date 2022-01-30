package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.SizedFireball;

public class EntityFireballDisplayItem implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof SizedFireball;
    }

    public static EntityFireballDisplayItem getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityFireballDisplayItem((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "fireball_display_item"
    };

    private EntityFireballDisplayItem(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.fireball_display_item>
        // @returns ItemTag
        // @group properties
        // @Plugin Paper
        // @description
        // If the entity is a fireball, returns its display item.
        // -->
        PropertyParser.<EntityFireballDisplayItem, ItemTag>registerTag(ItemTag.class, "fireball_display_item", (attribute, entity) -> {
            return new ItemTag(((SizedFireball) entity.entity.getBukkitEntity()).getDisplayItem());
        });
    }

    @Override
    public String getPropertyString() {
        return new ItemTag(((SizedFireball) entity.getBukkitEntity()).getDisplayItem()).identify();
    }

    @Override
    public String getPropertyId() {
        return "fireball_display_item";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name fireball_display_item
        // @input ItemTag
        // @Plugin Paper
        // @description
        // If the entity is a fireball, sets its display item.
        // @tags
        // <EntityTag.fireball_display_item>
        // -->
        if (mechanism.matches("fireball_display_item") && mechanism.requireObject(ItemTag.class)) {
            ((SizedFireball) entity.getBukkitEntity()).setDisplayItem(mechanism.valueAsType(ItemTag.class).getItemStack());
        }
    }
}
