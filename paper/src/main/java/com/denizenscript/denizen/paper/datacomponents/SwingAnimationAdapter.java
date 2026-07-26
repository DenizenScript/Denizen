package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.SwingAnimation;

public class SwingAnimationAdapter extends DataComponentAdapter.Valued<MapTag, SwingAnimation> {

    // <--[property]
    // @object ItemTag
    // @name swing_animation
    // @input ElementTag
    // @description
    // Controls an item's swing animation <@link language Item Components>.
    // The map includes keys:
    // - "type", an ElementTag representing the animation type. Valid animation types can be found at <@link url https://jd.papermc.io/paper/io/papermc/paper/datacomponent/item/SwingAnimation.Animation.html>.
    // - "duration", a DurationTag representing the duration of the animation.
    // @mechanism
    // Provide no input to reset the item to its default value.
    // -->

    public SwingAnimationAdapter() {
        super(MapTag.class, DataComponentTypes.SWING_ANIMATION, "swing_animation");
    }

    @Override
    public MapTag toDenizen(SwingAnimation value) {
        MapTag result = new MapTag();
        result.putObject("type", new ElementTag(value.type()));
        result.putObject("duration", new DurationTag(value.duration()));
        return result;
    }

    @Override
    public SwingAnimation fromDenizen(MapTag value, Mechanism mechanism) {
        SwingAnimation.Builder builder = SwingAnimation.swingAnimation();
        setIfValid(builder::type, value, "type", ElementTag.class,
                null,
                element -> element.asEnum(SwingAnimation.Animation.class),
                "swing animation type", mechanism);
        setIfValid(builder::duration, value, "duration", DurationTag.class, null, DurationTag::getTicksAsInt, "number", mechanism);
        return builder.build();
    }
}
