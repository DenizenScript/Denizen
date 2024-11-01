package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.util.BoundingBox;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EntityBoundingBox implements Property {

    // <--[property]
    // @object EntityTag
    // @name bounding_box
    // @input ListTag(LocationTag)
    // @description
    // Controls the collision bounding box of an entity in the format "<low>|<high>", essentially a cuboid with decimals.
    // -->

    public static boolean describes(ObjectTag object) {
        return object instanceof EntityTag;
    }

    public static EntityBoundingBox getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityBoundingBox((EntityTag) object);
        }
    }

    public static final String[] handledMechs = new String[] {
            "bounding_box"
    };

    public static Set<UUID> modifiedBoxes = new HashSet<>();

    public static void remove(UUID uuid) {
        modifiedBoxes.remove(uuid);
    }

    public EntityBoundingBox(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public ListTag getBoundingBox() {
        BoundingBox boundingBox = entity.getBukkitEntity().getBoundingBox();
        ListTag list = new ListTag();
        list.addObject(new LocationTag(entity.getWorld(), boundingBox.getMin()));
        list.addObject(new LocationTag(entity.getWorld(), boundingBox.getMax()));
        return list;
    }

    @Override
    public void setPropertyValue(ListTag param, Mechanism mechanism) {
        if (entity.isCitizensNPC()) {
            // TODO: Allow editing NPC boxes properly?
            return;
        }
        List<LocationTag> locations = mechanism.valueAsType(ListTag.class).filter(LocationTag.class, mechanism.context);
        if (locations.size() == 2) {
            NMSHandler.entityHelper.setBoundingBox(entity.getBukkitEntity(), BoundingBox.of(locations.get(0), locations.get(1)));
            modifiedBoxes.add(entity.getUUID());
        }
        else {
            mechanism.echoError("Must specify exactly 2 LocationTags in the format '<low>|<high>'!");
        }
    }

    @Override
    public String getPropertyString() {
        if (entity.isCitizensNPC()) {
            return null;
        }
        if (modifiedBoxes.contains(entity.getUUID())) {
            return getBoundingBox().identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "bounding_box";
    }

    public static void register() {
        PropertyParser.registerTag(EntityBoundingBox.class, ListTag.class, "bounding_box", (attribute, object) -> {
            return object.getBoundingBox();
        });
    }
}
