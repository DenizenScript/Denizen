package net.aufdemrand.denizen.scripts.requirements;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.requirements.core.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.interfaces.RegistrationableInstance;
import net.aufdemrand.denizencore.interfaces.dRegistry;

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
        for (RegistrationableInstance member : instances.values()) {
            try {
                member.onDisable();
            }
            catch (Exception e) {
                dB.echoError("Unable to disable '" + member.getClass().getName() + "'!");
                dB.echoError(e);
            }
        }
    }

    @Override
    public <T extends RegistrationableInstance> T get(Class<T> clazz) {
        if (classes.containsKey(clazz)) {
            return clazz.cast(instances.get(classes.get(clazz)));
        }
        else {
            return null;
        }
    }

    @Override
    public AbstractRequirement get(String requirementName) {
        if (instances.containsKey(requirementName.toUpperCase())) {
            return instances.get(requirementName.toUpperCase());
        }
        else {
            return null;
        }
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
        // @Syntax enchanted [iteminhand]
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
        // - enchanted iteminhand
        //
        // -->
        registerCoreMember(EnchantedRequirement.class,
                "ENCHANTED", "enchanted [iteminhand]", 1);

        // <--[requirement]
        // @Name Flagged
        // @Syntax flagged ({player}/npc/global) [<name>([<#>])](:<value>)
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
        // -->
        registerCoreMember(FlaggedRequirement.class,
                "FLAGGED", "flagged ({player}/npc/global) [<name>([<#>])](:<value>)", 1);

        // <--[requirement]
        // @Name Holding
        // @Syntax holding [<item>] (qty:<#>) (exact)
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
        // -->
        registerCoreMember(HoldingRequirement.class,
                "HOLDING", "holding [<item>] (qty:<#>) (exact)", 1);

        // <--[requirement]
        // @Name InGroup
        // @Syntax ingroup (global) [<group>]
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
        // -->
        registerCoreMember(InGroupRequirement.class,
                "INGROUP", "ingroup (global) [<group>]", 1);

        // <--[requirement]
        // @Name Item
        // @Syntax item [<item>] (qty:<#>)
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
        // -->
        registerCoreMember(ItemRequirement.class,
                "ITEM", "item [<item>] (qty:<#>)", 1);

        // <--[requirement]
        // @Name IsLiquid
        // @Syntax isliquid [location:<location>]
        // @Required 1
        // @Stable Stable
        // @Short Checks if a block is a liquid.
        //
        // @Description
        // Checks if the block at the specified location is a liquid. (Water or lava)
        //
        // @Usage
        // Check if the block is a liquid.
        // - isliquid location:103,70,413,world
        //
        // -->
        registerCoreMember(LiquidRequirement.class,
                "ISLIQUID", "isliquid [location:<location>]", 1);

        // <--[requirement]
        // @Name Money
        // @Syntax money [qty:<#>]
        // @Required 1
        // @Stable Stable
        // @Short Checks if the player has an amount of money.
        //
        // @Description
        // Checks if the player has a specified amount of money in their account.
        //
        // @Usage
        // Check if the player has an amount of money.
        // - money qty:100
        //
        // -->
        registerCoreMember(MoneyRequirement.class,
                "MONEY", "money [qty:<#>]", 1);

        // <--[requirement]
        // @Name Op
        // @Syntax op
        // @Required 0
        // @Stable Stable
        // @Short Checks if the player is an op.
        //
        // @Description
        // Checks if the player has Minecraft Op status.
        //
        // @Usage
        // Check if the player is opped.
        // - op
        //
        // -->
        registerCoreMember(OpRequirement.class,
                "OP", "op", 0);

        // <--[requirement]
        // @Name Owner
        // @Syntax owner
        // @Required 0
        // @Stable Stable
        // @Short Checks if the player is the owner of the current NPC.
        //
        // @Description
        // Checks if the player is the owner of the NPC attached to the current script.
        //
        // @Usage
        // Check if the player is the owner of the NPC.
        // - owner
        //
        // -->
        registerCoreMember(OwnerRequirement.class,
                "OWNER", "owner", 0);

        // <--[requirement]
        // @Name Permission
        // @Syntax permission (global) [<permission>]
        // @Required 1
        // @Stable Stable
        // @Short Checks if the player has a permission node.
        //
        // @Description Check if the player has a specified permission node.
        // (Requires Vault)
        //
        // @Usage
        // Check if the player has a permission.
        // - permission denizen.basic
        //
        // -->
        registerCoreMember(PermissionRequirement.class,
                "PERMISSION", "permission (global) [<permission>]", 1);

        // <--[requirement]
        // @Name IsPowered
        // @Syntax ispowered [location:<location>]
        // @Required 1
        // @Stable Stable
        // @Short Checks if a block is powered.
        //
        // @Description Checks if the block at a specified location is powered
        // by a redstone current.
        //
        // @Usage
        // Check if the block is powered.
        // - ispowered location:919,78,298
        //
        // -->
        registerCoreMember(PoweredRequirement.class,
                "ISPOWERED", "ispowered [location:<location>]", 1);

        // <--[requirement]
        // @Name Oxygen
        // @Syntax oxygen (range:below/equals/above) [qty:<#>]
        // @Required 1
        // @Stable Stable
        // @Short Checks the player's oxygen level.
        //
        // @Description Checks if the specified oxygen level is above, below, or
        // equal to the oxygen level of the player.
        //
        // @Usage Check if the player has above an amount of oxygen.
        // - oxygen range:above qty:3
        //
        // -->
        registerCoreMember(OxygenRequirement.class,
                "OXYGEN", "oxygen (range:below/equals/above) [qty:<#>]", 1);

        // <--[requirement]
        // @Name Procedure
        // @Syntax procedure [<script>]
        // @Required 1
        // @Stable Stable
        // @Short Checks the value of the procedure script.
        //
        // @Description Checks the value of a specified procedure script.
        //
        // @Usage
        // Check if the procedure script determines true
        // - procedure procScriptName
        //
        // -->
        registerCoreMember(ProcedureRequirement.class,
                "PROCEDURE", "procedure [<script>]", 1);

        // <--[requirement]
        // @Name Script
        // @Syntax script [finished/failed] [script:<name>]
        // @Required 2
        // @Stable Stable
        // @Short Checks if a script is finished or failed.
        //
        // @Description
        // Checks if the specified script was finished or failed by the player.
        //
        // @Usage
        // Check if the script was finished
        // - script finished script:ScriptName
        //
        // @Usage
        // Check if the script was failed
        // - script failed script:ScriptName
        //
        // -->
        registerCoreMember(ScriptRequirement.class,
                "SCRIPT", "script [finished/failed] [script:<name>]", 2);

        // <--[requirement]
        // @Name Sneaking
        // @Syntax sneaking
        // @Required 0
        // @Stable Stable
        // @Short Checks if the player is sneaking.
        //
        // @Description
        // Checks if the player is currently sneaking.
        //
        // @Usage
        // Check if the player is sneaking
        // - sneaking
        //
        // -->
        registerCoreMember(SneakingRequirement.class,
                "SNEAKING", "sneaking", 0);

        // <--[requirement]
        // @Name Storming
        // @Syntax storming
        // @Required 0
        // @Stable Stable
        // @Short Checks if the player's world is storming.
        //
        // @Description
        // Checks if the world the player is currently in has stormy weather.
        //
        // @Usage
        // Check if the world is storming
        // - storming
        //
        // -->
        registerCoreMember(StormRequirement.class,
                "STORMING", "storming", 0);

        // <--[requirement]
        // @Name Sunny
        // @Syntax sunny
        // @Required 0
        // @Stable Stable
        // @Short Checks if the player's world is sunny.
        //
        // @Description
        // Checks if the world the player is currently in has sunny weather.
        //
        // @Usage
        // Check if the world is sunny
        // - sunny
        //
        // -->
        registerCoreMember(SunnyRequirement.class,
                "SUNNY", "sunny", 0);

        // <--[requirement]
        // @Name Rainy
        // @Syntax rainy
        // @Required 0
        // @Stable Stable
        // @Short Checks if the player's world is rainy.
        //
        // @Description
        // Checks if the world the player is currently in has rainy weather.
        //
        // @Usage
        // Check if the world is rainy
        // - rainy
        //
        // -->
        registerCoreMember(RainyRequirement.class,
                "RAINY", "rainy", 0);

        // <--[requirement]
        // @Name Time
        // @Syntax time [dawn/day/dusk/night]
        // @Required 1
        // @Stable Stable
        // @Short Checks the time of the player's world.
        //
        // @Description
        // Checks if the time of the player's world is currently dawn, day, dusk, or night.
        //
        // @Usage
        // Check the time of day
        // - time dusk
        //
        // -->
        registerCoreMember(TimeRequirement.class,
                "TIME", "time [dawn/day/dusk/night]", 1);

        // <--[requirement]
        // @Name InRegion
        // @Syntax inregion [name:<region>]
        // @Required 1
        // @Stable Stable
        // @Short Checks if the player is in a region.
        //
        // @Description
        // Checks if the player is in a WorldGuard region. (Requires WorldGuard!)
        //
        // @Usage
        // Check if the player is in a region
        // - inregion name:MyRegion
        //
        // -->
        registerCoreMember(WorldGuardRegionRequirement.class,
                "INREGION", "inregion [name:<region>]", 1);

        dB.echoApproval("Loaded core requirements: " + instances.keySet().toString());
    }

    private <T extends AbstractRequirement> void registerCoreMember(Class<T> requirement, String name, String hint, int args) {
        try {
            requirement.newInstance().activate().as(name).withOptions("(-)" + hint, args);
        }
        catch (Exception e) {
            dB.echoError("Could not register requirement " + name + ": " + e.getMessage());
            dB.echoError(e);
        }
    }
}
