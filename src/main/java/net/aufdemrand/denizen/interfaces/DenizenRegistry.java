package net.aufdemrand.denizen.interfaces;

import java.util.Map;

/**
 * Represents a Denizen Instance Registry for the various extensible parts,
 * including Commands, Requirements, and Activities.
 * 
 * @author Jeremy Schroeder
 * 
 */

public interface DenizenRegistry {


    /**
     * Called by an AbstractInstance to get added to the registry.
     * 
     * @param registrationName      
     *      The name to use in the registry
     * @param commandInstance  
     *      A cast (AbstractInstance) of the instance being registered. Usually '(AbstractInstance) this'.
     * 
     * @return false if an exception has been thrown.
     *   
     */
    public boolean register(String registrationName, RegistrationableInstance commandInstance);


    /**
     * Returns a Map of the instances added by the register() method keyed by the registration name.
     * 
     * @return the entire registry.
     * 
     */
    public Map<String, ? extends RegistrationableInstance> list();


    /**
     * Gets a specific instance of AbstractInstance that has been previously registered.
     * 
     * @param clazz  
     *      class of the requested AbstractInstance.
     *      
     * @return the instance of the class specified.
     * 
     */
    public <T extends RegistrationableInstance> T get(Class<T> clazz);


    /**
     * Gets a specific instance of AbstractInstance that has been previously registered.
     * 
     * @param instanceName
     *      string key in which the AbstractInstance was registered with.
     *      
     * @return the instance of the key specified.
     * 
     */
    public RegistrationableInstance get(String instanceKey);


    /**
     * Generic method for registering the core classes of this registry's type
     * 
     */
    public void registerCoreMembers();

    
    /**
     * Calls an onDisable method on all registered instances.
     *  
     */
    public void disableCoreMembers();
    
}
