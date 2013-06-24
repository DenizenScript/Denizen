package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.npc.traits.ConstantsTrait;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Deprecated
public class ConstantTags implements Listener {

	public ConstantTags(Denizen denizen) {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

	@EventHandler
	public void constantTags(ReplaceableTagEvent event) {
		if (!event.matches("CONS")) return;

        if (!event.hasValue()) {
            dB.echoError("Constant tag '" + event.raw_tag + " does not contain a valid constant! " +
                    "Replacement has been aborted...");
            return;
        }

		NPC npc = null;
		if (event.getType() != null && event.getType().matches("\\d+"))
			npc = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(event.getType()));
		else if (event.getNPC() != null)
			npc = event.getNPC().getCitizen();

        if (npc == null) {
            dB.echoError("Constant tag '" + event.raw_tag + " does not contain a valid NPC! " +
                    "Has the NPC been removed, or is there no NPC list available? " +
                    "Replacement has been aborted...");
            return;
        }

        Attribute attribute = new Attribute(event.raw_tag.split(":", 2)[1], event.getScriptEntry());

		if (npc.hasTrait(ConstantsTrait.class)
				&& npc.getTrait(ConstantsTrait.class).getConstant(attribute.getAttribute(1)) != null) {
            event.setReplaced(new Element(npc.getTrait(ConstantsTrait.class)
                    .getConstant(attribute.getAttribute(1))).getAttribute(attribute.fulfill(1)));
        }

	}


}