package com.denizenscript.denizen.nms.v1_20.helpers;

import com.denizenscript.denizen.nms.interfaces.AdvancementHelper;
import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.*;

public class AdvancementHelperImpl extends AdvancementHelper {

    private static final String IMPOSSIBLE_KEY = "impossible";
    private static final Map<String, Criterion<?>> IMPOSSIBLE_CRITERIA = Collections.singletonMap(IMPOSSIBLE_KEY, new Criterion(new ImpossibleTrigger(), new ImpossibleTrigger.TriggerInstance()));
    private static final String[][] IMPOSSIBLE_REQUIREMENTS = new String[][]{{IMPOSSIBLE_KEY}};

    public static ServerAdvancementManager getAdvancementDataWorld() {
        return ((CraftServer) Bukkit.getServer()).getServer().getAdvancements();
    }

    // TODO: 1.20.2: advancement registration is different now
    //public static Field FIELD_ADVANCEMENTLIST_LISTENER = ReflectionHelper.getFields(AdvancementList.class).getFirstOfType(AdvancementList.Listener.class);

    @Override
    public void register(com.denizenscript.denizen.nms.util.Advancement advancement) {
        if (advancement.temporary || advancement.registered) {
            return;
        }
        /*
        AdvancementHolder nms = asNMSCopy(advancement);
        if (advancement.parent == null) {
            Set<Advancement> roots = ReflectionHelper.getFieldValue(AdvancementList.class, ReflectionMappingsInfo.AdvancementList_roots, getAdvancementDataWorld().advancements);
            roots.add(nms);
            AdvancementList.Listener something = ReflectionHelper.getFieldValue(AdvancementList.class, FIELD_ADVANCEMENTLIST_LISTENER.getName(), getAdvancementDataWorld().advancements);
            if (something != null) {
                something.onAddAdvancementRoot(nms);
            }
        }
        else {
            Set<Advancement> branches = ReflectionHelper.getFieldValue(AdvancementList.class, ReflectionMappingsInfo.AdvancementList_tasks, getAdvancementDataWorld().advancements);
            branches.add(nms);
            AdvancementList.Listener something = ReflectionHelper.getFieldValue(AdvancementList.class, FIELD_ADVANCEMENTLIST_LISTENER.getName(), getAdvancementDataWorld().advancements);
            if (something != null) {
                something.onAddAdvancementTask(nms);
            }
        }
        getAdvancementDataWorld().advancements.put(nms.id(), nms);
        advancement.registered = true;
        if (!advancement.hidden && advancement.parent != null) {
            ((CraftServer) Bukkit.getServer()).getHandle().broadcastAll(new ClientboundUpdateAdvancementsPacket(false,
                    Collections.singleton(nms), Collections.emptySet(), Collections.emptyMap()), (net.minecraft.world.entity.player.Player) null);
        }
        */
    }

    @Override
    public void unregister(com.denizenscript.denizen.nms.util.Advancement advancement) {
        if (advancement.temporary || !advancement.registered) {
            return;
        }
        /*
        Map<ResourceLocation, AdvancementHolder> advancements = getAdvancementDataWorld().advancements;
        ResourceLocation key = asResourceLocation(advancement.key);
        Advancement nms = advancements.get(key).value();
        if (advancement.parent == null) {
            Set<Advancement> roots = ReflectionHelper.getFieldValue(AdvancementList.class, ReflectionMappingsInfo.AdvancementList_roots, getAdvancementDataWorld().advancements);
            roots.remove(nms);
        }
        else {
            Set<Advancement> branches = ReflectionHelper.getFieldValue(AdvancementList.class, ReflectionMappingsInfo.AdvancementList_tasks, getAdvancementDataWorld().advancements);
            branches.remove(nms);
        }
        advancements.remove(key);
        advancement.registered = false;
        ((CraftServer) Bukkit.getServer()).getHandle().broadcastAll(new ClientboundUpdateAdvancementsPacket(false,
                Collections.emptySet(), Collections.singleton(key), Collections.emptyMap()), (net.minecraft.world.entity.player.Player) null);
         */
    }

    @Override
    public void grantPartial(com.denizenscript.denizen.nms.util.Advancement advancement, Player player, int len) {
        if (advancement.length <= 1) {
            grant(advancement, player);
            return;
        }
        if (advancement.temporary) {
            AdvancementHolder nmsAdvancement = asNMSCopy(advancement);
            AdvancementProgress progress = new AdvancementProgress();
            Map<String, Criterion> criteria = new HashMap<>();
            String[][] requirements = new String[advancement.length][];
            for (int i = 0; i < advancement.length; i++) {
                criteria.put(IMPOSSIBLE_KEY + i, new Criterion(new ImpossibleTrigger(), new ImpossibleTrigger.TriggerInstance()));
                requirements[i] = new String[] { IMPOSSIBLE_KEY + i };
            }
            progress.update(new AdvancementRequirements(IMPOSSIBLE_REQUIREMENTS));
            for (int i = 0; i < len; i++) {
                progress.grantProgress(IMPOSSIBLE_KEY + i); // complete impossible criteria
            }
            PacketHelperImpl.send(player, new ClientboundUpdateAdvancementsPacket(false,
                    Collections.singleton(nmsAdvancement),
                    Collections.emptySet(),
                    Collections.singletonMap(nmsAdvancement.id(), progress)));
        }
        else {
            AdvancementHolder nmsAdvancement = getAdvancementDataWorld().advancements.get(asResourceLocation(advancement.key));
            for (int i = 0; i < len; i++) {
                ((CraftPlayer) player).getHandle().getAdvancements().award(nmsAdvancement, IMPOSSIBLE_KEY + i);
            }
        }
    }

    @Override
    public void grant(com.denizenscript.denizen.nms.util.Advancement advancement, Player player) {
        if (advancement.length > 1) {
            grantPartial(advancement, player, advancement.length);
            return;
        }
        if (advancement.temporary) {
            AdvancementHolder nmsAdvancement = asNMSCopy(advancement);
            AdvancementProgress progress = new AdvancementProgress();
            progress.update(new AdvancementRequirements(IMPOSSIBLE_REQUIREMENTS));
            progress.grantProgress(IMPOSSIBLE_KEY); // complete impossible criteria
            PacketHelperImpl.send(player, new ClientboundUpdateAdvancementsPacket(false,
                    Collections.singleton(nmsAdvancement),
                    Collections.emptySet(),
                    Collections.singletonMap(nmsAdvancement.id(), progress)));
        }
        else {
            AdvancementHolder nmsAdvancement = getAdvancementDataWorld().advancements.get(asResourceLocation(advancement.key));
            ((CraftPlayer) player).getHandle().getAdvancements().award(nmsAdvancement, IMPOSSIBLE_KEY);
        }
    }

    @Override
    public void revoke(com.denizenscript.denizen.nms.util.Advancement advancement, Player player) {
        if (advancement.temporary) {
            PacketHelperImpl.send(player, new ClientboundUpdateAdvancementsPacket(false,
                    Collections.emptySet(),
                    Collections.singleton(asResourceLocation(advancement.key)),
                    Collections.emptyMap()));
        }
        else {
            AdvancementHolder nmsAdvancement = getAdvancementDataWorld().advancements.get(asResourceLocation(advancement.key));
            ((CraftPlayer) player).getHandle().getAdvancements().revoke(nmsAdvancement, IMPOSSIBLE_KEY);
        }
    }

    @Override
    public void update(Player player) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.connection.send(new ClientboundUpdateAdvancementsPacket(true,
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyMap()));
        PlayerAdvancements data = nmsPlayer.getAdvancements();
        data.save(); // save progress
        data.reload(DedicatedServer.getServer().getAdvancements()); // clear progress
        data.flushDirty(nmsPlayer); // load progress and update client
    }

    private static AdvancementHolder asNMSCopy(com.denizenscript.denizen.nms.util.Advancement advancement) {
        ResourceLocation key = asResourceLocation(advancement.key);
        AdvancementHolder parent = advancement.parent != null
                ? getAdvancementDataWorld().advancements.get(asResourceLocation(advancement.parent))
                : null;
        DisplayInfo display = new DisplayInfo(CraftItemStack.asNMSCopy(advancement.icon),
                Handler.componentToNMS(FormattedTextHelper.parse(advancement.title, ChatColor.WHITE)), Handler.componentToNMS(FormattedTextHelper.parse(advancement.description, ChatColor.WHITE)),
                asResourceLocation(advancement.background), FrameType.valueOf(advancement.frame.name()),
                advancement.toast, advancement.announceToChat, advancement.hidden);
        display.setLocation(advancement.xOffset, advancement.yOffset);
        Map<String, Criterion<?>> criteria = IMPOSSIBLE_CRITERIA;
        String[][] requirements = IMPOSSIBLE_REQUIREMENTS;
        if (advancement.length > 1) {
            criteria = new HashMap<>();
            requirements = new String[advancement.length][];
            for (int i = 0; i < advancement.length; i++) {
                criteria.put(IMPOSSIBLE_KEY + i, new Criterion(new ImpossibleTrigger(), new ImpossibleTrigger.TriggerInstance()));
                requirements[i] = new String[] { IMPOSSIBLE_KEY + i };
            }
        }
        AdvancementRequirements reqs = new AdvancementRequirements(requirements);
        Advancement adv = new Advancement(parent == null ? Optional.empty() : Optional.of(parent.id()), Optional.of(display), AdvancementRewards.EMPTY, criteria, reqs, false); // TODO: 1.20: do we want to ever enable telemetry?
        return new AdvancementHolder(key, adv);
    }

    private static ResourceLocation asResourceLocation(NamespacedKey key) {
        return key != null ? new ResourceLocation(key.getNamespace(), key.getKey()) : null;
    }
}
