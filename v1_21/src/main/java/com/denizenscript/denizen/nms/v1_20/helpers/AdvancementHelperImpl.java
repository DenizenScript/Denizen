package com.denizenscript.denizen.nms.v1_20.helpers;

import com.denizenscript.denizen.nms.interfaces.AdvancementHelper;
import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftNamespacedKey;
import org.bukkit.entity.Player;

import java.util.*;

public class AdvancementHelperImpl extends AdvancementHelper {

    private static final String IMPOSSIBLE_KEY = "impossible";
    private static final Map<String, Criterion<?>> IMPOSSIBLE_CRITERIA = Map.of(IMPOSSIBLE_KEY, new Criterion<>(new ImpossibleTrigger(), new ImpossibleTrigger.TriggerInstance()));
    private static final List<List<String>> IMPOSSIBLE_REQUIREMENTS = List.of(List.of(IMPOSSIBLE_KEY));

    public static ServerAdvancementManager getNMSAdvancementManager() {
        return ((CraftServer) Bukkit.getServer()).getServer().getAdvancements();
    }

    @Override
    public void register(com.denizenscript.denizen.nms.util.Advancement advancement) {
        if (advancement.temporary || advancement.registered) {
            return;
        }
        AdvancementHolder nmsAdvancementHolder = asNMSCopy(advancement);
        Map<ResourceLocation, AdvancementHolder> nmsAdvancements = getNMSAdvancementManager().advancements;
        ImmutableMap.Builder<ResourceLocation, AdvancementHolder> mapBuilder = ImmutableMap.builderWithExpectedSize(nmsAdvancements.size() + 1);
        mapBuilder.putAll(nmsAdvancements);
        mapBuilder.put(nmsAdvancementHolder.id(), nmsAdvancementHolder);
        getNMSAdvancementManager().advancements = mapBuilder.build();

        AdvancementTree tree = getNMSAdvancementManager().tree();
        tree.addAll(List.of(nmsAdvancementHolder));
        // recalculate advancement tree from this advancement's root
        AdvancementNode node = tree.get(nmsAdvancementHolder.id());
        if (node != null) {
            AdvancementNode root = node.root();
            if (root.holder().value().display().isPresent()) {
                TreeNodePosition.run(root);
            }
        }
        advancement.registered = true;
        if (!advancement.hidden && advancement.parent != null) {
            PacketHelperImpl.broadcast(new ClientboundUpdateAdvancementsPacket(false,
                    List.of(nmsAdvancementHolder), Set.of(), Map.of()));
        }
    }

    @Override
    public void unregister(com.denizenscript.denizen.nms.util.Advancement advancement) {
        if (advancement.temporary || !advancement.registered) {
            return;
        }
        ResourceLocation nmsKey = CraftNamespacedKey.toMinecraft(advancement.key);
        Map<ResourceLocation, AdvancementHolder> nmsAdvancements = getNMSAdvancementManager().advancements;
        ImmutableMap.Builder<ResourceLocation, AdvancementHolder> mapBuilder = ImmutableMap.builderWithExpectedSize(nmsAdvancements.size() - 1);
        for (Map.Entry<ResourceLocation, AdvancementHolder> entry : nmsAdvancements.entrySet()) {
            if (!entry.getKey().equals(nmsKey)) {
                mapBuilder.put(entry);
            }
        }
        getNMSAdvancementManager().advancements = mapBuilder.build();
        getNMSAdvancementManager().tree().remove(Set.of(nmsKey));
        advancement.registered = false;
        PacketHelperImpl.broadcast(new ClientboundUpdateAdvancementsPacket(false, List.of(), Set.of(nmsKey), Map.of()));
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
            AdvancementHolder nmsAdvancement = getNMSAdvancementManager().advancements.get(CraftNamespacedKey.toMinecraft(advancement.key));
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
            AdvancementHolder nmsAdvancement = getNMSAdvancementManager().advancements.get(CraftNamespacedKey.toMinecraft(advancement.key));
            ((CraftPlayer) player).getHandle().getAdvancements().award(nmsAdvancement, IMPOSSIBLE_KEY);
        }
    }

    @Override
    public void revoke(com.denizenscript.denizen.nms.util.Advancement advancement, Player player) {
        if (advancement.temporary) {
            PacketHelperImpl.send(player, new ClientboundUpdateAdvancementsPacket(false,
                    Collections.emptySet(),
                    Collections.singleton(CraftNamespacedKey.toMinecraft(advancement.key)),
                    Collections.emptyMap()));
        }
        else {
            AdvancementHolder nmsAdvancement = getNMSAdvancementManager().advancements.get(CraftNamespacedKey.toMinecraft(advancement.key));
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
        data.reload(getNMSAdvancementManager()); // clear progress
        data.flushDirty(nmsPlayer); // load progress and update client
    }

    private static AdvancementHolder asNMSCopy(com.denizenscript.denizen.nms.util.Advancement advancement) {
        AdvancementHolder parent = advancement.parent != null
                ? getNMSAdvancementManager().advancements.get(CraftNamespacedKey.toMinecraft(advancement.parent))
                : null;
        DisplayInfo display = new DisplayInfo(CraftItemStack.asNMSCopy(advancement.icon),
                Handler.componentToNMS(FormattedTextHelper.parse(advancement.title, ChatColor.WHITE)), Handler.componentToNMS(FormattedTextHelper.parse(advancement.description, ChatColor.WHITE)),
                Optional.ofNullable(advancement.background).map(CraftNamespacedKey::toMinecraft), AdvancementType.valueOf(advancement.frame.name()),
                advancement.toast, advancement.announceToChat, advancement.hidden);
        display.setLocation(advancement.xOffset, advancement.yOffset);
        Map<String, Criterion<?>> criteria = IMPOSSIBLE_CRITERIA;
        List<List<String>> requirements = IMPOSSIBLE_REQUIREMENTS;
        if (advancement.length > 1) {
            criteria = new HashMap<>();
            requirements = new ArrayList<>(advancement.length);
            for (int i = 0; i < advancement.length; i++) {
                criteria.put(IMPOSSIBLE_KEY + i, new Criterion<>(new ImpossibleTrigger(), new ImpossibleTrigger.TriggerInstance()));
                requirements.set(i, List.of(IMPOSSIBLE_KEY + i));
            }
        }
        AdvancementRequirements reqs = new AdvancementRequirements(requirements);
        Advancement adv = new Advancement(parent == null ? Optional.empty() : Optional.of(parent.id()), Optional.of(display), AdvancementRewards.EMPTY, criteria, reqs, false); // TODO: 1.20: do we want to ever enable telemetry?
        return new AdvancementHolder(CraftNamespacedKey.toMinecraft(advancement.key), adv);
    }
}
