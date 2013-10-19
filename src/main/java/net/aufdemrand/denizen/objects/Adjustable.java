package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.scripts.commands.core.AdjustCommand;
import net.aufdemrand.denizen.tags.Attribute;

public interface Adjustable {

    /**
     * Gets a specific attribute using this object to fetch the necessary data.
     *
     * @param mechanism  the name of the mechanism
     * @param value      the value of the mechanism
     * @return  a string result of the fetched attribute
     */
    public void adjust(Mechanism mechanism, Element value);

}
