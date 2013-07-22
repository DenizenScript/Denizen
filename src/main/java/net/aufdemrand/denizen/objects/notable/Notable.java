package net.aufdemrand.denizen.objects.notable;

public interface Notable {

    public boolean isUnique();

    public String getSaveString();

    public void makeUnique(String id);

    public void forget();

}
