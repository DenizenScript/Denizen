package net.aufdemrand.denizen.scripts.requirements;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.dRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.scripts.requirements.core.*;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.HashMap;
import java.util.Map;

public class RequirementRegistry implements dRegistry {

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
        if (instances.containsKey(requirementName.toUpperCase())) return instances.get(requirementName.toUpperCase());
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
        registerCoreMember(EnchantedRequirement.class, 
        		"ENCHANTED", "enchanted (iteminhand)", 1);
        
        registerCoreMember(FlaggedRequirement.class, 
        		"FLAGGED", "(-)flagged [player/npc/global] [<name>([<#>])](:<value>)", 0);
        
        registerCoreMember(HoldingRequirement.class, 
        		"HOLDING", "holding [<item>] [qty:<#>] [exact]", 1);
        
        registerCoreMember(InGroupRequirement.class, 
        		"INGROUP", "ingroup (global) [<group>]", 1);
        
        registerCoreMember(ItemRequirement.class, 
        		"ITEM", "item [<item>] (qty:<#>)", 1);
        
        registerCoreMember(LiquidRequirement.class, 
        		"ISLIQUID", "isliquid [location:<location>]", 1);
        
        registerCoreMember(MoneyRequirement.class, 
        		"MONEY", "money [qty:<#>]", 1);
        
        registerCoreMember(OpRequirement.class, 
        		"OP", "op", 0);
        
        registerCoreMember(OwnerRequirement.class, 
        		"OWNER", "owner", 0);
        
        registerCoreMember(PermissionRequirement.class, 
        		"PERMISSION", "permission (global) [<permission>]", 1);
        
        registerCoreMember(PoweredRequirement.class, 
        		"ISPOWERED", "ispowered [location:<location>]", 1);
        
        registerCoreMember(OxygenRequirement.class,
        		"OXYGEN", "oxygen (range:below/equals/above) [qty:<#>]", 1);
        
        registerCoreMember(ProcedureRequirement.class, 
        		"PROCEDURE", "procedure [<script>]", 1);
        
        registerCoreMember(ScriptRequirement.class, 
        		"SCRIPT", "script [finished/failed] [script:<name>]", 0);
        
        registerCoreMember(SneakingRequirement.class, 
        		"SNEAKING", "sneaking", 0);
        
        registerCoreMember(StormRequirement.class, 
        		"STORMING", "storm", 0);
        
        registerCoreMember(SunnyRequirement.class, 
        		"SUNNY", "sunny", 0);

		registerCoreMember(RainyRequirement.class,
				"RAINY", "rainy", 0);
        
        registerCoreMember(TimeRequirement.class, 
        		"TIME", "time [dawn/day/dusk/night]", 1);
        
        registerCoreMember(WorldGuardRegionRequirement.class, 
        		"INREGION", "inregion [name:<region>]", 1);

        dB.echoApproval("Loaded core requirements: " + instances.keySet().toString());
    }
	
    private <T extends AbstractRequirement> void registerCoreMember(Class<T> requirement, String name, String hint, int args) {
        try {
            requirement.newInstance().activate().as(name).withOptions("(-)" + hint, args);
        } catch(Exception e) {
            dB.echoError("Could not register requirement " + name + ": " + e.getMessage());
            if (dB.showStackTraces) e.printStackTrace();
        }
    }

}
