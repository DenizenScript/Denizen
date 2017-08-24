package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.SpawnEggMeta;

public class ItemSpawnEgg implements Property {

    public static boolean describes(dObject item) {
        if (!NMSHandler.getVersion().isAtLeast(NMSVersion.v1_9_R2)) {
            return false;
        }
        return item instanceof dItem
                && ((dItem) item).getItemStack().getType() == Material.MONSTER_EGG;
    }

    public static ItemSpawnEgg getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemSpawnEgg((dItem) _item);
        }
    }


    private ItemSpawnEgg(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.spawn_egg_entity>
        // @returns Element(Number)
        // @group properties
        // @mechanism dItem.spawn_egg
        // @description
        // Returns the spawn egg number of the item.
        // Also works for mob spawners.
        // -->
        if (attribute.startsWith("spawn_egg_entity")) {
            return new Element(((SpawnEggMeta) item.getItemStack().getItemMeta()).getSpawnedType().getTypeId())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        return String.valueOf(((SpawnEggMeta) item.getItemStack().getItemMeta()).getSpawnedType().getTypeId());
    }

    @Override
    public String getPropertyId() {
        return "spawn_egg";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name spawn_egg
        // @input Element(Number)
        // @description
        // Sets what mob a spawn egg holds.
        // Also works for mob spawners.
        // @tags
        // <i@item.spawn_egg_entity>
        // -->

        if (mechanism.matches("spawn_egg")) {
            SpawnEggMeta sem = (SpawnEggMeta) item.getItemStack().getItemMeta();
            sem.setSpawnedType(EntityType.fromId(mechanism.getValue().asInt()));
            item.getItemStack().setItemMeta(sem);
        }
    }
}
