package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ZombieVillager;

public class EntityConversionPlayer implements Property {

    // <--[property]
    // @object EntityTag
    // @name conversion_player
    // @input PlayerTag
    // @description
    // Controls which player caused a zombie villager to start converting back to a villager, if any.
    // -->

    public static boolean describes(ObjectTag object) {
        if (!(object instanceof EntityTag)) {
            return false;
        }
        Entity entity = ((EntityTag) object).getBukkitEntity();
        return entity instanceof ZombieVillager;
    }

    public static EntityConversionPlayer getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityConversionPlayer((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "conversion_player"
    };

    public EntityConversionPlayer(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;
    
    @Override
    public void adjust(Mechanism mechanism) {
        if (mechanism.hasValue()) {
            if (mechanism.requireObject(PlayerTag.class)) {
                getZombieVillager().setConversionPlayer(mechanism.valueAsType(PlayerTag.class).getOfflinePlayer());
            }
        }
        else {
            getZombieVillager().setConversionPlayer(null);
        }
    }

    @Override
    public String getPropertyString() {
        OfflinePlayer player = getZombieVillager().getConversionPlayer();
        if (player != null && player.hasPlayedBefore()) {
            return new PlayerTag(player).identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "conversion_player";
    }

    public static void register() {
        autoRegister("conversion_player", EntityConversionPlayer.class, PlayerTag.class, false);
        if (player != null && player.hasPlayedBefore()) {
            return new PlayerTag(player);
        }
        return null;
    }

    public ZombieVillager getZombieVillager() {
        return (ZombieVillager) entity.getBukkitEntity();
    }
}
