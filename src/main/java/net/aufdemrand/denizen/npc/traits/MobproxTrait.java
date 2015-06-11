package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.event.NPCTraitCommandAttachEvent;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import net.aufdemrand.denizen.flags.FlagManager.Flag;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

// TODO: Documenting language/tutorial files

public class MobproxTrait extends Trait {
    public MobproxTrait() {
        super("Mobprox");
    }

    int checkTimer = 0;
    int timerBounce = 0;
    LivingEntity liveEnt;
    dNPC dnpc;
    Flag frange;
    Flag facceptnpc;
    Flag ftimer;
    List<Entity> inrange = new ArrayList<Entity>();
    @Override
    public void run() {
        checkTimer++;
        if (checkTimer == 10) {
            checkTimer = 0;
            timerBounce++;
            if (timerBounce >= ftimer.getLast().asInteger()) {
                ftimer.rebuild();
                frange.rebuild();
                facceptnpc.rebuild();
                timerBounce = 0;
                if (getNPC().isSpawned()) {
                    int range = frange.getLast().asInteger();
                    boolean acceptnpc = facceptnpc.getLast().asBoolean();
                    List<Entity> nearby = liveEnt.getNearbyEntities(range, range, range);
                    List<Entity> removeme = new ArrayList<Entity>();
                    removeme.addAll(inrange);
                    for (Entity ent: nearby) {
                        if (ent instanceof LivingEntity && !(ent instanceof Player) && (acceptnpc || (!dEntity.isCitizensNPC(ent)))) {
                            if (removeme.contains(ent)) {
                                removeme.remove(ent);
                            }
                            if (!inrange.contains(ent)) {
                                inrange.add(ent);
                                callAction("enter", ent);
                            }
                            else {
                                callAction("move", ent);
                            }
                        }
                    }
                    for (Entity ent: removeme) {
                        inrange.remove(ent);
                        callAction("exit", ent);
                    }
                }
            }
        }
    }
    // <--[action]
    // @Actions
    // mob enter proximity
    // <entity> enter proximity
    //
    // @Triggers when a mob enters the proximity of the NPC (Requires MobProx trait).
    //
    // @Context
    // <context.entity> returns the mob that entered the proximity
    //
    // -->
    // <--[action]
    // @Actions
    // mob exit proximity
    // <entity> exit proximity
    //
    // @Triggers when a mob exits the proximity of the NPC (Requires MobProx trait).
    //
    // @Context
    // <context.entity> returns the mob that exited the proximity
    //
    // -->
    // <--[action]
    // @Actions
    // mob move proximity
    // <entity> move proximity
    //
    // @Triggers when a mob moves in the proximity of the NPC (Requires MobProx trait).
    // (Fires at a rate of specified by the 'mobprox_timer' flag, default of 2 seconds)
    //
    // @Context
    // <context.entity> returns the mob that entered the proximity
    //
    // -->
    private void callAction(String act, Entity ent) {
        Map<String, dObject> context = new HashMap<String, dObject>();
        context.put("entity", new dEntity(ent));
        dnpc.action("mob " + act + " proximity", null, context);
        dnpc.action(ent.getType().name() + " " + act + " proximity", null, context);
    }
    @EventHandler
    public void onTraitAttachEvent(NPCTraitCommandAttachEvent event) {
        if (!event.getTraitClass().equals(MobproxTrait.class)) {
            return;
        }
        if (event.getNPC() != getNPC()) {
            return;
        }
        onSpawn();
        AssignmentTrait at = dnpc.getAssignmentTrait();
        if (at == null || !at.hasAssignment()) {
            event.getCommandSender().sendMessage(ChatColor.RED + "Warning: This NPC doesn't have a script assigned! Mobprox only works with scripted Denizen NPCs!");
        }
    }
    @Override
    public void onSpawn() {
        liveEnt = (LivingEntity) getNPC().getEntity();
        dnpc = dNPC.mirrorCitizensNPC(getNPC());
        frange = DenizenAPI.getCurrentInstance().flagManager().getNPCFlag(dnpc.getId(), "mobprox_range");
        if (frange.isEmpty()) {
            frange.set("10");
        }
        facceptnpc = DenizenAPI.getCurrentInstance().flagManager().getNPCFlag(dnpc.getId(), "mobprox_acceptnpcs");
        if (facceptnpc.isEmpty()) {
            facceptnpc.set("false");
        }
        ftimer = DenizenAPI.getCurrentInstance().flagManager().getNPCFlag(dnpc.getId(), "mobprox_timer");
        if (ftimer.isEmpty()) {
            ftimer.set("4");
        }
    }
}
