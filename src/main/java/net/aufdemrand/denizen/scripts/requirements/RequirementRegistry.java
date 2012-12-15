package net.aufdemrand.denizen.scripts.requirements;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.scripts.requirements.core.FlaggedRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.WorldGuardRegionRequirement;

public class RequirementRegistry implements DenizenRegistry {

    public Denizen denizen;

    public RequirementRegistry(Denizen denizen) {
        this.denizen = denizen;
    }

    private Map<String, AbstractRequirement> instances = new HashMap<String, AbstractRequirement>();
    private Map<Class<? extends AbstractRequirement>, String> classes = new HashMap<Class<? extends AbstractRequirement>, String>();

    @Override
    public boolean register(String requirementName, RegistrationableInstance requirementClass) {
        this.instances.put(requirementName.toUpperCase(), (AbstractRequirement) requirementClass);
        this.classes.put(((AbstractRequirement) requirementClass).getClass(), requirementName.toUpperCase());
        return true;
    }

    @Override
    public Map<String, AbstractRequirement> list() {
        return instances;
    }

    @Override
    public AbstractRequirement get(String requirementName) {
        if (instances.containsKey(requirementName.toUpperCase())) return instances.get(requirementName);
        else return null;
    }

    @Override
    public <T extends RegistrationableInstance> T get(Class<T> clazz) {
        if (classes.containsKey(clazz)) return (T) clazz.cast(instances.get(classes.get(clazz)));
        else return null;
    }

    @Override
    public void registerCoreMembers() {
        new FlaggedRequirement().activate().as("FLAGGED");
        new WorldGuardRegionRequirement().activate().as("INREGION");
        denizen.getDebugger().echoApproval("Loaded core requirements: " + instances.keySet().toString());
    }

}
