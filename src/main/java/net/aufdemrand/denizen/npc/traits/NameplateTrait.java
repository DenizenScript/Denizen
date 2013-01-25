package net.aufdemrand.denizen.npc.traits;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.event.Listener;


/**
 * <p>Adds the ability to 'nickname' an NPC. This is meant to extend the NPCs real
 * name to perhaps add more description. Similar to a Player's 'Display Name', but better.
 * Inside Denizen, Nicknames can be utilized containing Replaceable TAGs. Outside Denizen,
 * the methods contained in this Trait can be used to get, set, and remove nicknames.</p>
 *
 * <p>Nicknames should not used as a static reference to an NPC because of the
 * dynamic nature of the Trait. Each time the trait is asked for a nickname, tags are
 * replaced. This allows for nicknames to use FLAGs and other dynamically changing
 * TAGs and have the linked information updated live.</p>
 *
 * <p>Though not in this Trait class, Denizen also provides some Replaceable TAGs
 * for getting a NPCs nickname. Use <</p>
 *
 */
public class NameplateTrait extends Trait implements Listener {

	@Persist("")
    private String nameplate = null;

	public NameplateTrait() {
		super("nameplate");
	}

    @Override
    public void onSpawn() {

        // if nameplate != null apply nameplate

    }

    public void setNameplate(String string) {

        // nameplate = string;

        // Utilities.Nameplate.setNameplate(...)
    }


}
