package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import org.bukkit.entity.Villager;

import java.util.Map;
import java.util.UUID;

public class EntityReputation implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Villager;
    }

    public static EntityReputation getFrom(ObjectTag _entity) {
        if (!describes(_entity)) {
            return null;
        }
        else {
            return new EntityReputation((EntityTag) _entity);
        }
    }

    public EntityReputation(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return getReputationMap().identify();
    }

    @Override
    public String getPropertyId() {
        return "reputation";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.reputation>
        // @returns MapTag
        // @mechanism EntityTag.reputation
        // @group properties
        // @Plugin Paper
        // @description
        // Returns a villager's reputations as a map of player UUIDs to reputation maps.
        // Reputation maps are maps of reputation types to integer values, a full list of all valid reputation types can be found at <@link url https://jd.papermc.io/paper/1.19/com/destroystokyo/paper/entity/villager/ReputationType.html>.
        // -->
        PropertyParser.registerTag(EntityReputation.class, MapTag.class, "reputation", (attribute, object) -> {
            return object.getReputationMap();
        });

        // <--[mechanism]
        // @object EntityTag
        // @name reputation
        // @input MapTag
        // @Plugin Paper
        // @group properties
        // @description
        // Sets a villager's reputations as a map of player UUIDs to reputation maps.
        // Reputation maps are maps of reputation types to integer values, a full list of all valid reputation types can be found at <@link url https://jd.papermc.io/paper/1.19/com/destroystokyo/paper/entity/villager/ReputationType.html>.
        // @tags
        // <EntityTag.reputation>
        // -->
        PropertyParser.registerMechanism(EntityReputation.class, MapTag.class, "reputation", (object, mechanism, input) -> {
            Villager villager = object.getVillager();
            villager.clearReputations();
            for (Map.Entry<StringHolder, ObjectTag> entry : input.entrySet()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(entry.getKey().str);
                }
                catch (IllegalArgumentException exception) {
                    mechanism.echoError("Invalid uuid specified: " + entry.getKey().str);
                    continue;
                }
                MapTag reputationInput = entry.getValue().asType(MapTag.class, mechanism.context);
                if (reputationInput == null) {
                    mechanism.echoError("Invalid reputation map specified: " + entry.getValue());
                    continue;
                }
                Reputation reputation = new Reputation();
                for (Map.Entry<StringHolder, ObjectTag> reputationEntry : reputationInput.entrySet()) {
                    ReputationType reputationType = new ElementTag(reputationEntry.getKey().low).asEnum(ReputationType.class);
                    if (reputationType == null) {
                        mechanism.echoError("Invalid reputation type specified: " + reputationEntry.getKey().str);
                        continue;
                    }
                    reputation.setReputation(reputationType, reputationEntry.getValue().asElement().asInt());
                }
                villager.setReputation(uuid, reputation);
            }
        });
    }

    public Villager getVillager() {
        return (Villager) entity.getBukkitEntity();
    }

    public MapTag getReputationMap() {
        MapTag result = new MapTag();
        for (Map.Entry<UUID, Reputation> entry : getVillager().getReputations().entrySet()) {
            MapTag reputationMap = new MapTag();
            Reputation reputation = entry.getValue();
            for (ReputationType reputationType : ReputationType.values()) {
                reputationMap.putObject(reputationType.name(), new ElementTag(reputation.getReputation(reputationType)));
            }
            result.putObject(entry.getKey().toString(), reputationMap);
        }
        return result;
    }
}
