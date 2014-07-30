package net.aufdemrand.denizen.objects.notable;

public interface Notable {

    public boolean isUnique();

    /**
     * Gets the object to be saved to the notables.yml.
     * This should either be a String, or a ConfigurationSerializable object.
     *
     * @return the object to be saved
     */
    public Object getSaveObject();

    /**
     * Saves the object in the NotableManager. Notable objects are saved through
     * a server restart.
     *
     * @param id the id of the notable
     */
    public void makeUnique(String id);

    public void forget();

}
