package com.denizenscript.denizen.objects;

import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.Location;

public interface AreaContainmentObject extends ObjectTag {

    // <--[ObjectType]
    // @name AreaObject
    // @prefix None
    // @base None
    // @format
    // N/A
    //
    // @description
    // "AreaObject" is a pseudo-ObjectType that represents any object that indicates a world-space area, such as a CuboidTag.
    //
    // -->

    String getNoteName();

    boolean doesContainLocation(Location loc);
}
