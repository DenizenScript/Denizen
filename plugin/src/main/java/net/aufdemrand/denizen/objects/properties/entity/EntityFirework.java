package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class EntityFirework implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.FIREWORK;
    }

    public static EntityFirework getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityFirework((dEntity) entity);
        }
    }

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityFirework(dEntity entity) {
        firework = entity;
    }

    dEntity firework;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        ItemStack item = new ItemStack(Material.FIREWORK);
        item.setItemMeta(((Firework) firework.getBukkitEntity()).getFireworkMeta());
        return new dItem(item).identify();
    }

    @Override
    public String getPropertyId() {
        return "firework_item";
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
        // @attribute <e@entity.firework_item>
        // @returns dItem
        // @mechanism dEntity.firework_item
        // @group properties
        // @description
        // If the entity is a firework, returns the firework item used to launch it.
        // -->
        if (attribute.startsWith("firework_item")) {
            ItemStack item = new ItemStack(Material.FIREWORK);
            item.setItemMeta(((Firework) firework.getBukkitEntity()).getFireworkMeta());
            return new dItem(item).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name firework_item
        // @input dItem
        // @description
        // Changes the firework effect on this entity, using a firework item.
        // @tags
        // <e@entity.firework_item>
        // -->
        if (mechanism.matches("firework_item") && mechanism.requireObject(dItem.class)) {
            dItem item = mechanism.getValue().asType(dItem.class);
            if (item != null && item.getItemStack().getItemMeta() instanceof FireworkMeta) {
                ((Firework) firework.getBukkitEntity()).setFireworkMeta((FireworkMeta) item.getItemStack().getItemMeta());
            }
            else {
                dB.echoError("'" + mechanism.getValue().asString() + "' is not a valid firework item.");
            }
        }

        // <--[mechanism]
        // @object dEntity
        // @name detonate
        // @input None
        // @description
        // If the entity is a firework, detonates it.
        // @tags
        // <e@entity.firework_item>
        // -->
        if (mechanism.matches("detonate")) {
            ((Firework) firework.getBukkitEntity()).detonate();
        }
    }
}
