package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

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
            "view_distance", "no_tick_view_distance"
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
        // @attribute <WorldTag.view_distance>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.view_distance
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the view distance of this world. Chunks are tracked inside this radius.
        // -->
        PropertyParser.<PaperWorldProperties, ElementTag>registerTag(ElementTag.class, "view_distance", (attribute, world) -> {
            return new ElementTag(world.world.getWorld().getViewDistance());
        });

        // <--[tag]
        // @attribute <WorldTag.no_tick_view_distance>
        // @returns ElementTag(Number)
        // @mechanism WorldTag.no_tick_view_distance
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the non-ticking view distance of this world. Chunks will not be tracked between the world's view distance and its non-ticking view distance.
        // This allows your world to have a higher visual view distance without impacting performance.
        // -->
        PropertyParser.<PaperWorldProperties, ElementTag>registerTag(ElementTag.class, "no_tick_view_distance", (attribute, world) -> {
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
        // Sets this world's view distance. All chunks within this radius will be tracked by the server.
        // Input should be a number from 2 to 32. To allow for a larger untracked radius, use <@link mechanism WorldTag.no_tick_view_distance>.
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
        // @name no_tick_view_distance
        // @input ElementTag(Number)
        // @Plugin Paper
        // @description
        // Sets this world's non-ticking view distance. Chunks will not be tracked between the world's view distance and its non-ticking view distance.
        // This allows your world to have a higher visual view distance without impacting performance.
        // Input should be a number from 2 to 32. Provide no input to reset this to the world's view distance.
        // NOTE: This should generally be set to a value higher than the world's view distance. Setting it lower may cause odd chunk issues.
        // @tags
        // <WorldTag.no_tick_view_distance>
        // -->
        if (mechanism.matches("no_tick_view_distance")) {
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
