package net.aufdemrand.denizen.interfaces;

/**
 * Container interface for instances loaded into a DenizenRegistry.
 * 
 * @author Jeremy Schroeder
 *
 */

public interface RegistrationableInstance {
    
    public RegistrationableInstance activate();
    
    public RegistrationableInstance as(String name);
    
    public String getName();
    
    public void onEnable();

}
