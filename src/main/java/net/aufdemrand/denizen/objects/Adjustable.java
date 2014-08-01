package net.aufdemrand.denizen.objects;

public interface Adjustable {

    /**
     * Sets a specific attribute using this object to modify the necessary data.
     *
     * @param mechanism the mechanism to gather change information from
     */
    public void adjust(Mechanism mechanism);

    /**
     * Applies a property, passing it to 'adjust' or throwing an error, depending on whether
     * the mechanism may be used as a property.
     *
     * @param mechanism the mechanism to gather change information from
     */
    public void applyProperty(Mechanism mechanism);
}
