package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.util.BoundingBox;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EntityBoundingBox implements Property {

    public static boolean describes(dObject object) {
        return object instanceof dEntity && !(((dEntity) object).isCitizensNPC());
    }

    public static EntityBoundingBox getFrom(dObject object) {
        if (!describes(object)) {
            return null;
        }

        else {
            return new EntityBoundingBox((dEntity) object);
        }
    }

    private static Set<UUID> modifiedBoxes = new HashSet<UUID>();

    public static void remove(UUID uuid) {
        if (modifiedBoxes.contains(uuid)) {
            modifiedBoxes.remove(uuid);
        }
    }

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityBoundingBox(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    private dList getBoundingBox() {
        BoundingBox boundingBox = NMSHandler.getInstance().getEntityHelper().getBoundingBox(entity.getBukkitEntity());
        dList list = new dList();
        list.add(new dLocation(boundingBox.getPosition()).identify());
        list.add(new dLocation(boundingBox.getSize()).identify());
        return list;
    }

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (modifiedBoxes.contains(entity.getUUID())) {
            return getBoundingBox().identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "bounding_box";
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.bounding_box>
        // @returns dList(dLocation)
        // @mechanism dEntity.bounding_box
        // @group properties
        // @description
        // Returns the collision bounding box of the entity in the format "<position>|<size>".
        // -->
        if (attribute.startsWith("bounding_box")) {
            return getBoundingBox().getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {
        Element value = mechanism.getValue();

        // <--[mechanism]
        // @object dEntity
        // @name bounding_box
        // @input dList(dLocation)
        // @description
        // Changes the collision bounding box of the entity in the format "<position>|<size>".
        // @tags
        // <e@entity.bounding_box>
        // -->

        if (mechanism.matches("bounding_box")) {
            List<dLocation> locations = value.asType(dList.class).filter(dLocation.class);
            if (locations.size() == 2) {
                BoundingBox boundingBox = new BoundingBox(locations.get(0).toVector(), locations.get(1).toVector());
                NMSHandler.getInstance().getEntityHelper().setBoundingBox(entity.getBukkitEntity(), boundingBox);
                modifiedBoxes.add(entity.getUUID());
            }
            else {
                dB.echoError("Must specify exactly 2 dLocations in the format \"<position>|<size>\"!");
            }
        }
    }
}
