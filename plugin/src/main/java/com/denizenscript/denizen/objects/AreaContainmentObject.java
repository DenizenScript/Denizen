package com.denizenscript.denizen.objects;

import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.Location;

public interface AreaContainmentObject extends ObjectTag {

    String getNoteName();

    boolean doesContainLocation(Location loc);
}
