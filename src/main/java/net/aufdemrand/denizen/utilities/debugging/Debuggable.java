package net.aufdemrand.denizen.utilities.debugging;

public interface Debuggable {

    public boolean shouldDebug() throws Exception;

    public boolean shouldFilter(String criteria) throws Exception;
}
