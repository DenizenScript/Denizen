package net.aufdemrand.denizen.interfaces;

public interface dExternal {

    /**
     * Called by the RuntimeCompiler after an 'External Dependency' is compiled and loaded.
     *
     */
    public void load();

    /**
     * Called by Denizen when the server stops or '/denizen reload externals' is executed.
     *
     */
    public void unload();
}
