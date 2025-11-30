package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.Villager;
import org.bukkit.entity.ZombieVillager;

public class EntityProfession extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name profession
    // @input ElementTag
    // @description
    // Controls the profession of a villager or zombie villager.
    // For the list of possible professions, refer to <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Villager.Profession.html>
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Villager
                || entity.getBukkitEntity() instanceof ZombieVillager;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof Villager villager) {
            return Utilities.enumlikeToElement(villager.getProfession());
        }
        else if (getEntity() instanceof ZombieVillager zvillager) {
            return Utilities.enumlikeToElement(zvillager.getVillagerProfession());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireEnum(Villager.Profession.class)) {
            if (getEntity() instanceof Villager villager) {
                villager.setProfession(value.asEnum(Villager.Profession.class));
            }
            else if (getEntity() instanceof ZombieVillager zvillager) {
                zvillager.setVillagerProfession(value.asEnum(Villager.Profession.class));
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "profession";
    }

    public static void register() {
        autoRegister("profession", EntityProfession.class, ElementTag.class, false);
    }
}
