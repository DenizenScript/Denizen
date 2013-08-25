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
        
        // <--[requirement]
        // @Name Enchanted
        // @Usage enchanted [iteminhand]
        // @Required 1
        // @Stable Stable
        // @Short Checks if an item has an enchantment or not.
        //
        // @Description
        // Checks if the specified item has an enchantment. Currently, the only
        // item available for this is the "iteminhand".
        //
        // @Usage
        // Use to check if the item in the player's hand has an enchantment.
        // - enchantment iteminhand
        //
        // @Example
        // TODO
        //
        // -->
        registerCoreMember(EnchantedRequirement.class, 
                "ENCHANTED", "enchanted [iteminhand]", 1);
        
        // <--[requirement]
        // @Name Flagged
        // @Usage (-)flagged ({player}/npc/global) [<name>([<#>])](:<value>)
        // @Required 1
        // @Stable Stable
        // @Short Checks if the specified flag exists.
        //
        // @Description
        // Checks if the specified flag exists on the specified owner, which is "player"
        // by default.
        //
        // @Usage
        // Check if a flag exists.
        // - flagged FlagName
        //
        // @Usage
        // Check if a flag has a specified value.
        // - flagged FlagName:Value
        //
        // @Usage
        // Check if a flag does not exist.
        // - -flagged FlagName
        //
        // @Example
        // TODO
        //
        // -->
        registerCoreMember(FlaggedRequirement.class, 
                "FLAGGED", "(-)flagged ({player}/npc/global) [<name>([<#>])](:<value>)", 2);
        
        // <--[requirement]
        // @Name Holding
        // @Usage holding [<item>] (qty:<#>) (exact)
        // @Required 1
        // @Stable Stable
        // @Short Checks if the player is holding an item.
        //
        // @Description
        // Checks if the player is holding a specified item. The item can be a dItem (i@itemName)
        // or it can be a normal material name or ID (wood, 5, 5:1). If a quantity is given, it
        // checks if the ItemStack in the player's hand hand has at least that many items. If
        // "exact" is specified, it must be the exact quantity of items.
        //
        // @Usage
        // Check if the player is holding at least 3 pieces of wood.
        // - holding wood qty:3
        //
        // @Example
        // TODO
        //
        // -->
        registerCoreMember(HoldingRequirement.class, 
                "HOLDING", "holding [<item>] (qty:<#>) (exact)", 1);
        
        // <--[requirement]
        // @Name InGroup
        // @Usage ingroup (global) [<group>]
        // @Required 1
        // @Stable Stable
        // @Short Checks if the player is in a group.
        //
        // @Description
        // Checks if the player is in the specified group in the current world, or global group if specified.
        //
        // @Usage
        // Check if the player is in a group.
        // - ingroup Builder
        //
        // @Usage
        // Check if the player is in a global group.
        // - ingroup global Admin
        //
        // @Example
        // TODO
        //
        // -->
        registerCoreMember(InGroupRequirement.class, 
                "INGROUP", "ingroup (global) [<group>]", 1);
        
        // <--[requirement]
        // @Name Item
        // @Usage item [<item>] (qty:<#>)
        // @Required 1
        // @Stable Stable
        // @Short Checks if the player has an item.
        //
        // @Description
        // Checks if the player has the specified item and the quantity of that item in their inventory.
        //
        // @Usage
        // Check if the player has an item.
        // - item wood qty:3
        //
        // @Example
        // TODO
        //
        // -->
        registerCoreMember(ItemRequirement.class, 
                "ITEM", "item [<item>] (qty:<#>)", 1);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(LiquidRequirement.class, 
                "ISLIQUID", "isliquid [location:<location>]", 1);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example
        //
        // -->
        registerCoreMember(MoneyRequirement.class, 
                "MONEY", "money [qty:<#>]", 1);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(OpRequirement.class, 
                "OP", "op", 0);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(OwnerRequirement.class, 
                "OWNER", "owner", 0);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(PermissionRequirement.class, 
                "PERMISSION", "permission (global) [<permission>]", 1);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(PoweredRequirement.class, 
                "ISPOWERED", "ispowered [location:<location>]", 1);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(OxygenRequirement.class,
                "OXYGEN", "oxygen (range:below/equals/above) [qty:<#>]", 1);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(ProcedureRequirement.class, 
                "PROCEDURE", "procedure [<script>]", 1);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(ScriptRequirement.class, 
                "SCRIPT", "script [finished/failed] [script:<name>]", 2);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(SneakingRequirement.class, 
                "SNEAKING", "sneaking", 0);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(StormRequirement.class, 
                "STORMING", "storming", 0);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(SunnyRequirement.class, 
                "SUNNY", "sunny", 0);

        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(RainyRequirement.class,
                "RAINY", "rainy", 0);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
        registerCoreMember(TimeRequirement.class, 
                "TIME", "time [dawn/day/dusk/night]", 1);
        
        // <--[requirement]
        // @Name TODO
        // @Usage TODO
        // @Required TODO
        // @Stable TODO
        // @Short TODO
        //
        // @Description TODO
        //
        //
        // @Usage TODO
        //
        //
        // @Example TODO
        //
        // -->
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
