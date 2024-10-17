package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.boss.DragonBattle;

public class PaperWorldExtensions  {

    public static void register() {

        // <--[tag]
        // @attribute <WorldTag.no_tick_view_distance>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.no_tick_view_distance
        // @group paper
        // @deprecated replaced by Minecraft's simulation_distance and view_distance config pairing
        // @Plugin Paper
        // @description
        // Deprecated: replaced by Minecraft's simulation_distance and view_distance config pairing
        // -->
        WorldTag.tagProcessor.registerTag(ElementTag.class, "no_tick_view_distance", (attribute, world) -> {
            BukkitImplDeprecations.paperNoTickViewDistance.warn(attribute.context);
            return new ElementTag(world.getWorld().getNoTickViewDistance());
        });

        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {

            // <--[tag]
            // @attribute <WorldTag.gateway_count>
            // @returns ElementTag(Number)
            // @group paper
            // @Plugin Paper
            // @description
            // Returns the number of end gateway portals.
            // Only works if the world is an end world.
            // -->
            WorldTag.tagProcessor.registerTag(ElementTag.class, "gateway_count", (attribute, object) -> {
                DragonBattle battle = object.getWorld().getEnderDragonBattle();
                if (battle == null) {
                    attribute.echoError("Provided world is not an end world!");
                    return null;
                }
                return new ElementTag(battle.getGatewayCount());
            });

            // <--[tag]
            // @attribute <WorldTag.healing_crystals>
            // @returns ListTag(EntityTag)
            // @group paper
            // @Plugin Paper
            // @description
            // Returns a ListTag of the healing end crystals located on top of the obsidian towers.
            // Only works if the world is an end world.
            // -->
            WorldTag.tagProcessor.registerTag(ListTag.class, "healing_crystals", (attribute, object) -> {
                DragonBattle battle = object.getWorld().getEnderDragonBattle();
                if (battle == null) {
                    attribute.echoError("Provided world is not an end world!");
                    return null;
                }
                return new ListTag(battle.getHealingCrystals(), EntityTag::new);
            });

            // <--[tag]
            // @attribute <WorldTag.respawn_crystals>
            // @returns ListTag(EntityTag)
            // @group paper
            // @Plugin Paper
            // @description
            // Returns a ListTag of the respawn end crystals located at the end exit portal.
            // Only works if the world is an end world.
            // -->
            WorldTag.tagProcessor.registerTag(ListTag.class, "respawn_crystals", (attribute, object) -> {
                DragonBattle battle = object.getWorld().getEnderDragonBattle();
                if (battle == null) {
                    attribute.echoError("Provided world is not an end world!");
                    return null;
                }
                return new ListTag(battle.getRespawnCrystals(), EntityTag::new);
            });

            // <--[mechanism]
            // @object WorldTag
            // @name spawn_gateway
            // @input LocationTag
            // @group paper
            // @Plugin Paper
            // @description
            // Spawns a new end gateway portal at the specified location.
            // If no location is specified, tries to spawn a new end gateway using default game mechanics.
            // Only works if the world is an end world.
            // -->
            WorldTag.tagProcessor.registerMechanism("spawn_gateway", false, (object, mechanism) -> {
                DragonBattle battle = object.getWorld().getEnderDragonBattle();
                if (battle == null) {
                    mechanism.echoError("Cannot spawn gateway in non-end world!");
                    return;
                }
                if (!mechanism.hasValue()) {
                    battle.spawnNewGateway();
                }
                else if (mechanism.requireObject(LocationTag.class)) {
                    battle.spawnNewGateway(mechanism.valueAsType(LocationTag.class));
                }
            });
        }

        // <--[mechanism]
        // @object WorldTag
        // @name view_distance
        // @input ElementTag(Number)
        // @Plugin Paper
        // @group paper
        // @description
        // Sets this world's view distance. All chunks within this radius of a player will be visible to that player.
        // Input should be a number from 2 to 32.
        // See also <@link mechanism WorldTag.simulation_distance>
        // @tags
        // <WorldTag.view_distance>
        // <server.view_distance>
        // -->
        WorldTag.tagProcessor.registerMechanism("view_distance", false, ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireInteger()) {
                int distance = input.asInt();
                if (distance < 2 || distance > 32) {
                    mechanism.echoError("View distance must be a number from 2 to 32!");
                }
                else {
                    object.getWorld().setViewDistance(distance);
                }
            }
        });

        // <--[mechanism]
        // @object WorldTag
        // @name simulation_distance
        // @input ElementTag(Number)
        // @Plugin Paper
        // @group paper
        // @description
        // Sets this world's view distance. All chunks within this radius will be tracked by the server.
        // Input should be a number from 2 to 32.
        // See also <@link mechanism WorldTag.view_distance>
        // @tags
        // <WorldTag.view_distance>
        // <server.view_distance>
        // -->
        WorldTag.tagProcessor.registerMechanism("simulation_distance", false, ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireInteger()) {
                int distance = input.asInt();
                if (distance < 2 || distance > 32) {
                    mechanism.echoError("View distance must be a number from 2 to 32!");
                }
                else {
                    object.getWorld().setSimulationDistance(distance);
                }
            }
        });

        // <--[mechanism]
        // @object WorldTag
        // @name no_tick_view_distance
        // @input ElementTag(Number)
        // @Plugin Paper
        // @group paper
        // @deprecated replaced by Minecraft's simulation_distance and view_distance config pairing
        // @description
        // Deprecated: replaced by Minecraft's simulation_distance and view_distance config pairing
        // @tags
        // <WorldTag.no_tick_view_distance>
        // -->
        WorldTag.tagProcessor.registerMechanism("no_tick_view_distance", false, (object, mechanism) -> {
            BukkitImplDeprecations.paperNoTickViewDistance.warn(mechanism.context);
            if (!mechanism.hasValue()) {
                object.getWorld().setNoTickViewDistance(-1);
            }
            else if (mechanism.requireInteger()) {
                int distance = mechanism.getValue().asInt();
                if (distance < 2 || distance > 32) {
                    mechanism.echoError("View distance must be a number from 2 to 32!");
                }
                else {
                    object.getWorld().setNoTickViewDistance(distance);
                }
            }
        });
    }
}
