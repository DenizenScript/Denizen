package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.EntityType;
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

    public EntityProfession(EntityTag entity) {
        professional = entity;
    }

    EntityTag professional;

    public Villager.Profession getProfession() {
        if (professional.getBukkitEntityType() == EntityType.ZOMBIE_VILLAGER) {
            return ((ZombieVillager) professional.getBukkitEntity()).getVillagerProfession();
        }
        return ((Villager) professional.getBukkitEntity()).getProfession();
    }

    @Override
    public ElementTag getPropertyValue() {
        if (NMSHandler.getVersion().isAtMost(NMSVersion.v1_20)) {
            return new ElementTag(String.valueOf(getProfession()), true);
        }
        return new ElementTag(Utilities.namespacedKeyToString(getProfession().getKey()).toUpperCase(), true);
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (Utilities.requireEnumlike(mechanism, Villager.Profession.class)) {
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
