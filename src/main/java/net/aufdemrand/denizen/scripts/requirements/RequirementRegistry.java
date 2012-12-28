package net.aufdemrand.denizen.scripts.requirements;

import java.util.HashMap;
import java.util.Map;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.scripts.requirements.core.EnchantedRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.FlaggedRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.HoldingRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.LiquidRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.OwnerRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.PoweredRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.ScriptRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.SneakingRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.StormRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.SunnyRequirement;
import net.aufdemrand.denizen.scripts.requirements.core.WorldGuardRegionRequirement;
import net.aufdemrand.denizen.utilities.debugging.dB;

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
        new EnchantedRequirement().activate().as("ENCHANTED").withOptions("(ITEMINHAND)", 1);
        new FlaggedRequirement().activate().as("FLAGGED").withOptions("NO ARGS", 0);
        new HoldingRequirement().activate().as("HOLDING").withOptions("[ITEMNAME]", 1);
        new LiquidRequirement().activate().as("ISLIQUID").withOptions("[LOCATION:x,y,z,world", 1);
        new OwnerRequirement().activate().as("OWNER").withOptions("NONE", 0);
        new PoweredRequirement().activate().as("ISPOWERED").withOptions("[LOCATION:x,y,z,world", 1);
        new ScriptRequirement().activate().as("SCRIPT").withOptions("TBD", 0);
        new SneakingRequirement().activate().as("SNEAKING").withOptions("NONE", 0);
        new StormRequirement().activate().as("STORMING").withOptions("NONE", 0);
        new SunnyRequirement().activate().as("SUNNY").withOptions("NONE", 0);
        new WorldGuardRegionRequirement().activate().as("INREGION").withOptions("[NAME:regionname]", 1);
        dB.echoApproval("Loaded core requirements: " + instances.keySet().toString());
    }

	@Override
	public void disableCoreMembers() {
		for (RegistrationableInstance member : instances.values())
			try { 
				member.onDisable(); 
			} catch (Exception e) {
				dB.echoError("Unable to disable '" + member.getClass().getName() + "'!");
				if (dB.showStackTraces) e.printStackTrace();
			}
	}

}
