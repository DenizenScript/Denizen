package net.aufdemrand.denizen;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.Toggleable;

public class DenizenTrait extends Trait implements Toggleable {

	public boolean isDenizen = false;
	public boolean enableChatTriggers = true;
	public boolean enableLocationTriggers = true;
	public boolean enableProximityTriggers = true;
	public boolean enableClickTriggers = true;
	public boolean enableDamageTriggers = false;
	public boolean enableDeathTriggers = false;
	
	protected DenizenTrait() {
		super("denizen");
		}

	@Override
	public void load(DataKey key) throws NPCLoadException {
	    isDenizen = key.getBoolean("toggle", false);
        enableClickTriggers = key.getBoolean("enabled.click_triggers", true);
        enableDamageTriggers = key.getBoolean("enabled.damage_triggers", false);
        enableChatTriggers = key.getBoolean("enabled.chat_triggers", true);
        enableProximityTriggers = key.getBoolean("enabled.proximity_triggers", true);
        enableLocationTriggers = key.getBoolean("enabled.location_triggers", true);
        enableDeathTriggers = key.getBoolean("enabled.death_triggers", true);
	}

	@Override
	public void save(DataKey key) {
	    key.setBoolean("toggle", isDenizen);
        key.setBoolean("enabled.click_triggers", enableClickTriggers);
        key.setBoolean("enabled.damage_triggers", enableDamageTriggers);
        key.setBoolean("enabled.chat_triggers", enableChatTriggers);
        key.setBoolean("enabled.proximity_triggers", enableProximityTriggers);
        key.setBoolean("enabled.location_triggers", enableLocationTriggers);
        key.setBoolean("enabled.death_triggers", enableDeathTriggers);
	}

	@Override
	public boolean toggle() {
		isDenizen = !isDenizen;
		return isDenizen;
	}
	
	
}
