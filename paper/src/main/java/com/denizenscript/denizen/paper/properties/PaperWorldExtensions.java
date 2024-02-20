package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.objects.core.ElementTag;

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
