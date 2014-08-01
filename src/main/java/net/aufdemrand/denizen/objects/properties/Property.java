package net.aufdemrand.denizen.objects.properties;

import net.aufdemrand.denizen.objects.Mechanism;
import net.aufdemrand.denizen.tags.Attribute;

public interface Property {

    public String getPropertyString();

    public String getPropertyId();

    public String getAttribute(Attribute attribute);

    public void adjust(Mechanism mechanism);
}
