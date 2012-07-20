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
	
	protected DenizenTrait() {
		super("denizen");
		}

	@Override
	public void load(DataKey key) throws NPCLoadException {
	    isDenizen = key.getBoolean("toggle");
        enableClickTriggers = key.getBoolean("enable.click_triggers");
        enableDamageTriggers = key.getBoolean("enable.damage_triggers");
        enableChatTriggers = key.getBoolean("enable.chat_triggers");
        enableProximityTriggers = key.getBoolean("enable.proximity_triggers");
        enableLocationTriggers = key.getBoolean("enable.location_triggers");
	}

	@Override
	public void save(DataKey key) {
	    key.setBoolean("toggle", isDenizen);
        key.setBoolean("enable.click_triggers", enableClickTriggers);
        key.setBoolean("enable.damage_triggers", enableDamageTriggers);
        key.setBoolean("enable.chat_triggers", enableChatTriggers);
        key.setBoolean("enable.proximity_triggers", enableProximityTriggers);
        key.setBoolean("enable.location_triggers", enableLocationTriggers);
	}

	@Override
	public boolean toggle() {
		isDenizen = !isDenizen;
		return isDenizen;
	}
	
	
}
