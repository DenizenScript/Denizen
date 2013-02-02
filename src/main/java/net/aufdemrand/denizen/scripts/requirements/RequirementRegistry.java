package net.aufdemrand.denizen.scripts.requirements;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.requirements.core.*;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.HashMap;
import java.util.Map;

public class RequirementRegistry implements DenizenRegistry {

    public Denizen denizen;

    private Map<String, AbstractRequirement> instances = new HashMap<String, AbstractRequirement>();

    private Map<Class<? extends AbstractRequirement>, String> classes = new HashMap<Class<? extends AbstractRequirement>, String>();
    public RequirementRegistry(Denizen denizen) {
        this.denizen = denizen;
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

    @Override
    public <T extends RegistrationableInstance> T get(Class<T> clazz) {
        if (classes.containsKey(clazz)) return (T) clazz.cast(instances.get(classes.get(clazz)));
        else return null;
    }

    @Override
    public AbstractRequirement get(String requirementName) {
        if (instances.containsKey(requirementName.toUpperCase())) return instances.get(requirementName);
        else return null;
    }

    @Override
    public Map<String, AbstractRequirement> list() {
        return instances;
    }

    @Override
    public boolean register(String requirementName, RegistrationableInstance requirementClass) {
        this.instances.put(requirementName.toUpperCase(), (AbstractRequirement) requirementClass);
        this.classes.put(((AbstractRequirement) requirementClass).getClass(), requirementName.toUpperCase());
        return true;
    }

	@Override
    public void registerCoreMembers() {
		String enchantedHint = "(-)enchanted (ITEMINHAND)";
        new EnchantedRequirement().activate().as("ENCHANTED").withOptions(enchantedHint, 1);
		
        String flaggedHint = "flagged T.B.D.";
        new FlaggedRequirement().activate().as("FLAGGED").withOptions(flaggedHint, 0);
		
        String inGroupHint = "ingroup [GROUP]";
        new InGroupRequirement().activate().as("INGROUP").withOptions(inGroupHint, 1);
		
        String permissionHint = "permission [PERMISSION]";
        new PermissionRequirement().activate().as("PERMISSION").withOptions(permissionHint, 1);
		
        String holdingHint = "holding [ITEMNAME]";
        new HoldingRequirement().activate().as("HOLDING").withOptions(holdingHint, 1);
		
        String liquidHint = "islquid [LOCATION:x,y,z,world]";
        new LiquidRequirement().activate().as("ISLIQUID").withOptions(liquidHint, 1);
		
        String moneyHint = "[QTY:#]";
        new MoneyRequirement().activate().as("MONEY").withOptions(moneyHint, 1);
		
        String ownerHint = "owner (no args)";
        new OwnerRequirement().activate().as("OWNER").withOptions(ownerHint, 0);
		
        String opHint = "op (no args)";;
        new OpRequirement().activate().as("OP").withOptions(opHint, 0);
		
        String poweredHint = "ispowered [LOCATION:x,y,z,world]";
        new PoweredRequirement().activate().as("ISPOWERED").withOptions(poweredHint, 1);
		
        String scriptHint = "script T.B.D.";
        new ScriptRequirement().activate().as("SCRIPT").withOptions(scriptHint, 0);
		
        String sneakingHint = "sneaking (no args)";
        new SneakingRequirement().activate().as("SNEAKING").withOptions(sneakingHint, 0);
		
        String stormHint = "storm (no args)";
        new StormRequirement().activate().as("STORMING").withOptions(stormHint, 0);
		
        String sunnyHint = "sunny (no args)";
        new SunnyRequirement().activate().as("SUNNY").withOptions(sunnyHint, 0);
		
        String timeHint = "time [DAWN, DAY, DUSK, NIGHT]";
		new TimeRequirement().activate().as("TIME").withOptions(timeHint, 1);
		
		String worldGuardRegionHint = "inregion [NAME:regionname]";
        new WorldGuardRegionRequirement().activate().as("INREGION").withOptions(worldGuardRegionHint, 1);
		
        String procedureHint = "procedure [SCRIPT:procedure_script]";
        new ProcedureRequirement().activate().as("PROCEDURE").withOptions(procedureHint, 1);
        
        dB.echoApproval("Loaded core requirements: " + instances.keySet().toString());
    }

}
