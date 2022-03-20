package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.Deprecations;

public class PaperWorldProperties implements Property {

    public static boolean describes(ObjectTag world) {
        return world instanceof WorldTag;
    }

    public static PaperWorldProperties getFrom(ObjectTag world) {
        if (!describes(world)) {
            return null;
        }
        return new PaperWorldProperties((WorldTag) world);
    }

    public static final String[] handledMechs = new String[] {
            "view_distance", "simulation_distance", "no_tick_view_distance"
    };

    private PaperWorldProperties(WorldTag world) {
        this.world = world;
    }

    WorldTag world;

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "PaperWorldProperties";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <WorldTag.no_tick_view_distance>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.no_tick_view_distance
        // @group properties
        // @deprecated replaced by Minecraft's simulation_distance and view_distance config pairing
        // @Plugin Paper
        // @description
        // Deprecated: replaced by Minecraft's simulation_distance and view_distance config pairing
        // -->
        PropertyParser.<PaperWorldProperties, ElementTag>registerTag(ElementTag.class, "no_tick_view_distance", (attribute, world) -> {
            Deprecations.paperNoTickViewDistance.warn(attribute.context);
            return new ElementTag(world.world.getWorld().getNoTickViewDistance());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object WorldTag
        // @name view_distance
        // @input ElementTag(Number)
        // @Plugin Paper
        // @description
        // Sets this world's view distance. All chunks within this radius of a player will be visible to that player.
        // Input should be a number from 2 to 32.
        // See also <@link mechanism WorldTag.simulation_distance>
        // @tags
        // <WorldTag.view_distance>
        // <server.view_distance>
        // -->
        if (mechanism.matches("view_distance") && mechanism.requireInteger()) {
            int distance = mechanism.getValue().asInt();
            if (distance < 2 || distance > 32) {
                Debug.echoError("View distance must be a number from 2 to 32!");
            }
            else {
                world.getWorld().setViewDistance(distance);
            }
        }

        // <--[mechanism]
        // @object WorldTag
        // @name simulation_distance
        // @input ElementTag(Number)
        // @Plugin Paper
        // @description
        // Sets this world's view distance. All chunks within this radius will be tracked by the server.
        // Input should be a number from 2 to 32.
        // See also <@link mechanism WorldTag.view_distance>
        // @tags
        // <WorldTag.view_distance>
        // <server.view_distance>
        // -->
        if (mechanism.matches("simulation_distance") && mechanism.requireInteger()) {
            int distance = mechanism.getValue().asInt();
            if (distance < 2 || distance > 32) {
                Debug.echoError("View distance must be a number from 2 to 32!");
            }
            else {
                world.getWorld().setSimulationDistance(distance);
            }
        }

        // <--[mechanism]
        // @object WorldTag
        // @name no_tick_view_distance
        // @input ElementTag(Number)
        // @Plugin Paper
        // @deprecated replaced by Minecraft's simulation_distance and view_distance config pairing
        // @description
        // Deprecated: replaced by Minecraft's simulation_distance and view_distance config pairing
        // @tags
        // <WorldTag.no_tick_view_distance>
        // -->
        if (mechanism.matches("no_tick_view_distance")) {
            Deprecations.paperNoTickViewDistance.warn(mechanism.context);
            if (!mechanism.hasValue()) {
                world.getWorld().setNoTickViewDistance(-1);
            }
            else if (mechanism.requireInteger()) {
                int distance = mechanism.getValue().asInt();
                if (distance < 2 || distance > 32) {
                    Debug.echoError("View distance must be a number from 2 to 32!");
                }
                else {
                    world.getWorld().setNoTickViewDistance(distance);
                }
            }
        }
    }
}
