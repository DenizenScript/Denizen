package net.aufdemrand.denizen.objects;

public interface Adjustable {

    /**
     * Gets a specific attribute using this object to fetch the necessary data.
     *
     * @param mechanism  the name of mechanism to change
     */
    public void adjust(Mechanism mechanism);

}
