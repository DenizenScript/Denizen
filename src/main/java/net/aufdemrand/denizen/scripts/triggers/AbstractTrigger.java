package net.aufdemrand.denizen.scripts.triggers;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.helpers.ScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.TriggerRegistry.CooldownType;
import net.aufdemrand.denizen.utilities.debugging.Debugger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class AbstractTrigger implements RegistrationableInstance {

    public Denizen denizen;

    protected ScriptHelper sH;
    protected ScriptBuilder sB;
    protected Debugger dB;

    protected String name;
    public TriggerOptions triggerOptions = new TriggerOptions();

    public class TriggerOptions { 
        public boolean ENABLED_BY_DEFAULT = true; 
        public double DEFAULT_COOLDOWN = -1;
        public int DEFAULT_RADIUS = -1;
        public CooldownType DEFAULT_COOLDOWN_TYPE = CooldownType.NPC;

        public TriggerOptions() { }
        
        public TriggerOptions(boolean enabledByDefault, double defaultCooldown) {
            this.ENABLED_BY_DEFAULT = enabledByDefault;
            this.DEFAULT_COOLDOWN = defaultCooldown;
        }
        
        public TriggerOptions(boolean enabledByDefault, double defaultCooldown, CooldownType defaultCooldownType) {
            this.ENABLED_BY_DEFAULT = enabledByDefault;
            this.DEFAULT_COOLDOWN = defaultCooldown;
            this.DEFAULT_COOLDOWN_TYPE = defaultCooldownType;
        }
        
        public TriggerOptions(boolean enabledByDefault, double defaultCooldown, int defaultRadius) {
            this.ENABLED_BY_DEFAULT = enabledByDefault;
            this.DEFAULT_COOLDOWN = defaultCooldown;
            this.DEFAULT_RADIUS = defaultRadius;
        }
        
        public TriggerOptions(boolean enabledByDefault, double defaultCooldown, int defaultRadius, CooldownType defaultCooldownType) {
            this.ENABLED_BY_DEFAULT = enabledByDefault;
            this.DEFAULT_COOLDOWN = defaultCooldown;
            this.DEFAULT_RADIUS = defaultRadius;
            this.DEFAULT_COOLDOWN_TYPE = defaultCooldownType;
        }
    }

    @Override
    public AbstractTrigger activate() {
        denizen = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
        // Reference Helper Classes
        sH = denizen.getScriptEngine().getScriptHelper();
        sB = denizen.getScriptEngine().getScriptBuilder();		
        dB = denizen.getDebugger();
        return this;
    }

    @Override
    public AbstractTrigger as(String triggerName) {
        this.name = triggerName.toUpperCase();
        // Register command with Registry
        denizen.getTriggerRegistry().register(triggerName, this);
        onEnable();
        return this;
    }
    
    public AbstractTrigger withOptions(boolean enabledByDefault, double defaultCooldown, CooldownType defaultCooldownType) {
        this.triggerOptions = new TriggerOptions(enabledByDefault, defaultCooldown, defaultCooldownType);
        return this;
    }
    
    public AbstractTrigger withOptions(boolean enabledByDefault, double defaultCooldown, int defaultRadius, CooldownType defaultCooldownType) {
        this.triggerOptions = new TriggerOptions(enabledByDefault, defaultCooldown, defaultRadius, defaultCooldownType);
        return this;
    }
    
    public TriggerOptions getOptions() {
        return triggerOptions;
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract boolean parse(DenizenNPC npc, Player player, String script);

}
