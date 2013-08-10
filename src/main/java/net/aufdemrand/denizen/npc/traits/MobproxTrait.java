package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import net.aufdemrand.denizen.flags.FlagManager.Flag;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class MobproxTrait extends Trait {
    public MobproxTrait() {
        super("Mobprox");
    }
    int checkTimer = 0;
    LivingEntity liveEnt;
    dNPC dnpc;
    Flag frange;
    List<Entity> inrange = new ArrayList<Entity>();
    @Override
    public void run() {
        checkTimer++;
        if (checkTimer == 40) {
            int range = frange.getLast().asInteger();
            checkTimer = 0;
            if (getNPC().isSpawned()) {
                List<Entity> nearby = liveEnt.getNearbyEntities(range, range, range);
                List<Entity> removeme = new ArrayList<Entity>();
                removeme.addAll(inrange);
                for (Entity ent: nearby) {
                    if (ent instanceof LivingEntity && !(ent instanceof Player)) {
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
    private void callAction(String act, Entity ent) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("entity", new dEntity(ent));
        dnpc.action("mob " + act + " proximity", null, context);
        dnpc.action(ent.getType().name() + " " + act + " proximity", null, context);
    }
    @Override
    public void onSpawn() {
        liveEnt = getNPC().getBukkitEntity();
        dnpc = dNPC.mirrorCitizensNPC(getNPC());
        frange = DenizenAPI.getCurrentInstance().flagManager().getNPCFlag(dnpc.getId(), "mobprox_range");
        if (frange.isEmpty()) {
            frange.set("10");
        }
    }
    @Override
    public void onAttach() {
        onSpawn();
    }

}
