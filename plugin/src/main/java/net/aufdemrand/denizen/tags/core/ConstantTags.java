package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.traits.ConstantsTrait;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.Listener;

@Deprecated
public class ConstantTags {

    public ConstantTags(Denizen denizen) {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                constantTags(event);
            }
        }, "cons");
    }

    public void constantTags(ReplaceableTagEvent event) {
        if (!event.matches("cons")) {
            return;
        }

        if (!event.hasValue()) {
            dB.echoError("Constant tag '" + event.raw_tag + " does not contain a valid constant! " +
                    "Replacement has been aborted...");
            return;
        }

        dB.echoError(event.getAttributes().getScriptEntry().getResidingQueue(), "constant: tags are deprecated! Use <npc.constant[]>!");

        NPC npc = null;
        if (event.getType() != null && event.getType().matches("\\d+")) {
            npc = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(event.getType()));
        }
        else if (((BukkitTagContext) event.getContext()).npc != null) {
            npc = ((BukkitTagContext) event.getContext()).npc.getCitizen();
        }

        if (npc == null) {
            dB.echoError("Constant tag '" + event.raw_tag + " does not contain a valid NPC! " +
                    "Has the NPC been removed, or is there no NPC list available? " +
                    "Replacement has been aborted...");
            return;
        }

        Attribute attribute = event.getAttributes();

        if (npc.hasTrait(ConstantsTrait.class)
                && npc.getTrait(ConstantsTrait.class).getConstant(event.getValue()) != null) {
            event.setReplaced(new Element(npc.getTrait(ConstantsTrait.class)
                    .getConstant(event.getValue())).getAttribute(attribute.fulfill(1)));
        }

    }
}
