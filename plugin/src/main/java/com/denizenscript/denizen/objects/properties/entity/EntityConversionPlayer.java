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

        // <--[tag]
        // @attribute <EntityTag.conversion_player>
        // @returns PlayerTag
        // @mechanism EntityTag.conversion_player
        // @group properties
        // @description
        // Returns the player that caused a zombie villager to start converting back to a villager, if any.
        // -->
        PropertyParser.registerTag(EntityConversionPlayer.class, PlayerTag.class, "conversion_player", (attribute, object) -> {
            OfflinePlayer player = object.getZombieVillager().getConversionPlayer();
            if (player != null && player.hasPlayedBefore()) {
                return new PlayerTag(player);
            }
            return null;
        });
    }

    public ZombieVillager getZombieVillager() {
        return (ZombieVillager) entity.getBukkitEntity();
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name conversion_player
        // @input PlayerTag
        // @description
        // Sets the player that caused a zombie villager to start converting back to a villager.
        // Give no input to remove the player ID from the zombie-villager.
        // @tags
        // <EntityTag.conversion_player>
        // -->
        if (mechanism.matches("conversion_player")) {
            if (mechanism.hasValue()) {
                if (mechanism.requireObject(PlayerTag.class)) {
                    getZombieVillager().setConversionPlayer(mechanism.valueAsType(PlayerTag.class).getOfflinePlayer());
                }
            }
            else {
                getZombieVillager().setConversionPlayer(null);
            }
        }
    }
}
