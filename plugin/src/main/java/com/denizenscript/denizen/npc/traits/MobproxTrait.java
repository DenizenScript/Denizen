package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import net.citizensnpcs.api.event.NPCTraitCommandAttachEvent;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Documenting language docs

public class MobproxTrait extends Trait {

    public MobproxTrait() {
        super("mobprox");
    }

    int checkTimer = 0;
    int timerBounce = 0;
    LivingEntity liveEnt;
    NPCTag dnpc;
    List<Entity> inrange = new ArrayList<>();

    @Override
    public void run() {
        checkTimer++;
        if (checkTimer == 10) {
            checkTimer = 0;
            timerBounce++;
            if (timerBounce >= getTimer()) {
                timerBounce = 0;
                if (getNPC().isSpawned()) {
                    int range = getRange();
                    boolean acceptnpc = acceptNpcs();
                    List<Entity> nearby = liveEnt.getNearbyEntities(range, range, range);
                    List<Entity> removeme = new ArrayList<>(inrange);
                    for (Entity ent : nearby) {
                        if (ent instanceof LivingEntity && (!(ent instanceof Player) || EntityTag.isCitizensNPC(ent))
                                && (acceptnpc || (!EntityTag.isCitizensNPC(ent)))) {
                            removeme.remove(ent);
                            if (!inrange.contains(ent)) {
                                inrange.add(ent);
                                callAction("enter", ent);
                            }
                            else {
                                callAction("move", ent);
                            }
                        }
                    }
                    for (Entity ent : removeme) {
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
        Map<String, ObjectTag> context = new HashMap<>();
        context.put("entity", new EntityTag(ent).getDenizenObject());
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
        if (!dnpc.getCitizen().hasTrait(AssignmentTrait.class)) {
            event.getCommandSender().sendMessage(ChatColor.RED + "Warning: This NPC doesn't have a script assigned! Mobprox only works with scripted Denizen NPCs!");
        }
    }

    public int getRange() {
        // TODO: Make this not flag based.
        ObjectTag range = dnpc.getFlagTracker().getFlagValue("mobprox_range");
        if (range == null) {
            return 10;
        }
        return range.asElement().asInt();
    }

    public int getTimer() {
        ObjectTag range = dnpc.getFlagTracker().getFlagValue("mobprox_timer");
        if (range == null) {
            return 4;
        }
        return range.asElement().asInt();
    }

    public boolean acceptNpcs() {
        ObjectTag range = dnpc.getFlagTracker().getFlagValue("mobprox_acceptnpcs");
        if (range == null) {
            return false;
        }
        return range.asElement().asBoolean();
    }

    @Override
    public void onSpawn() {
        liveEnt = (LivingEntity) getNPC().getEntity();
        dnpc = new NPCTag(getNPC());
    }
}
