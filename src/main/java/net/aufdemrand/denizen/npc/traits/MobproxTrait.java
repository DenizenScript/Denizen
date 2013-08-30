package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dObject;
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
            if (timerBounce == ftimer.getLast().asInteger()) {
                timerBounce = 0;
                if (getNPC().isSpawned()) {
                    int range = frange.getLast().asInteger();
                    boolean acceptnpc = facceptnpc.getLast().asBoolean();
                    List<Entity> nearby = liveEnt.getNearbyEntities(range, range, range);
                    List<Entity> removeme = new ArrayList<Entity>();
                    removeme.addAll(inrange);
                    for (Entity ent: nearby) {
                        if (ent instanceof LivingEntity && !(ent instanceof Player) && (acceptnpc || (!new dEntity(ent).isNPC()))) {
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
        liveEnt = getNPC().getBukkitEntity();
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
