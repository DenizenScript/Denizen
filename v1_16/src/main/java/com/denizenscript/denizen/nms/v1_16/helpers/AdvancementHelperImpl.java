package com.denizenscript.denizen.nms.v1_16.helpers;

import com.denizenscript.denizen.nms.interfaces.AdvancementHelper;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizen.nms.v1_16.Handler;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AdvancementHelperImpl extends AdvancementHelper {

    private static final String IMPOSSIBLE_KEY = "impossible";
    private static final Map<String, Criterion> IMPOSSIBLE_CRITERIA = Collections.singletonMap(IMPOSSIBLE_KEY, new Criterion(new CriterionTriggerImpossible.a()));
    private static final String[][] IMPOSSIBLE_REQUIREMENTS = new String[][]{{IMPOSSIBLE_KEY}};

    public static AdvancementDataWorld getAdvancementDataWorld() {
        return ((CraftServer) Bukkit.getServer()).getServer().getAdvancementData();
    }

    @Override
    public void register(com.denizenscript.denizen.nms.util.Advancement advancement) {
        if (advancement.temporary || advancement.registered) {
            return;
        }
        Advancement nms = asNMSCopy(advancement);
        if (advancement.parent == null) {
            Set<Advancement> roots = ReflectionHelper.getFieldValue(Advancements.class, "c", getAdvancementDataWorld().REGISTRY);
            roots.add(nms);
            Advancements.a something = ReflectionHelper.getFieldValue(Advancements.class, "e", getAdvancementDataWorld().REGISTRY);
            if (something != null) {
                something.a(nms);
            }
        }
        else {
            Set<Advancement> branches = ReflectionHelper.getFieldValue(Advancements.class, "d", getAdvancementDataWorld().REGISTRY);
            branches.add(nms);
            Advancements.a something = ReflectionHelper.getFieldValue(Advancements.class, "e", getAdvancementDataWorld().REGISTRY);
            if (something != null) {
                something.c(nms);
            }
        }
        getAdvancementDataWorld().REGISTRY.advancements.put(nms.getName(), nms);
        advancement.registered = true;
        if (!advancement.hidden && advancement.parent != null) {
            ((CraftServer) Bukkit.getServer()).getHandle().sendAll(new PacketPlayOutAdvancements(false,
                    Collections.singleton(nms), Collections.emptySet(), Collections.emptyMap()));
        }
    }

    @Override
    public void unregister(com.denizenscript.denizen.nms.util.Advancement advancement) {
        if (advancement.temporary || !advancement.registered) {
            return;
        }
        Map<MinecraftKey, Advancement> advancements = getAdvancementDataWorld().REGISTRY.advancements;
        MinecraftKey key = asMinecraftKey(advancement.key);
        Advancement nms = advancements.get(key);
        if (advancement.parent == null) {
            Set<Advancement> roots = ReflectionHelper.getFieldValue(Advancements.class, "c", getAdvancementDataWorld().REGISTRY);
            roots.remove(nms);
        }
        else {
            Set<Advancement> branches = ReflectionHelper.getFieldValue(Advancements.class, "d", getAdvancementDataWorld().REGISTRY);
            branches.remove(nms);
        }
        advancements.remove(key);
        advancement.registered = false;
        ((CraftServer) Bukkit.getServer()).getHandle().sendAll(new PacketPlayOutAdvancements(false,
                Collections.emptySet(), Collections.singleton(key), Collections.emptyMap()));
    }

    @Override
    public void grant(com.denizenscript.denizen.nms.util.Advancement advancement, Player player) {
        if (advancement.temporary) {
            Advancement nmsAdvancement = asNMSCopy(advancement);
            AdvancementProgress progress = new AdvancementProgress();
            progress.a(IMPOSSIBLE_CRITERIA, IMPOSSIBLE_REQUIREMENTS);
            progress.a(IMPOSSIBLE_KEY); // complete impossible criteria
            PacketHelperImpl.sendPacket(player, new PacketPlayOutAdvancements(false,
                    Collections.singleton(nmsAdvancement),
                    Collections.emptySet(),
                    Collections.singletonMap(nmsAdvancement.getName(), progress)));
        }
        else {
            Advancement nmsAdvancement = getAdvancementDataWorld().REGISTRY.advancements.get(asMinecraftKey(advancement.key));
            ((CraftPlayer) player).getHandle().getAdvancementData().grantCriteria(nmsAdvancement, IMPOSSIBLE_KEY);
        }
    }

    @Override
    public void revoke(com.denizenscript.denizen.nms.util.Advancement advancement, Player player) {
        if (advancement.temporary) {
            PacketHelperImpl.sendPacket(player, new PacketPlayOutAdvancements(false,
                    Collections.emptySet(),
                    Collections.singleton(asMinecraftKey(advancement.key)),
                    Collections.emptyMap()));
        }
        else {
            Advancement nmsAdvancement = getAdvancementDataWorld().REGISTRY.advancements.get(asMinecraftKey(advancement.key));
            ((CraftPlayer) player).getHandle().getAdvancementData().revokeCritera(nmsAdvancement, IMPOSSIBLE_KEY);
        }
    }

    @Override
    public void update(Player player) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.playerConnection.sendPacket(new PacketPlayOutAdvancements(true,
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyMap()));
        AdvancementDataPlayer data = nmsPlayer.getAdvancementData();
        data.b(); // save progress
        data.a(DedicatedServer.getServer().getAdvancementData()); // clear progress
        data.b(nmsPlayer); // load progress and update client
    }

    private static Advancement asNMSCopy(com.denizenscript.denizen.nms.util.Advancement advancement) {
        MinecraftKey key = asMinecraftKey(advancement.key);
        Advancement parent = advancement.parent != null
                ? getAdvancementDataWorld().REGISTRY.advancements.get(asMinecraftKey(advancement.parent))
                : null;
        AdvancementDisplay display = new AdvancementDisplay(CraftItemStack.asNMSCopy(advancement.icon),
                Handler.componentToNMS(FormattedTextHelper.parse(advancement.title)), Handler.componentToNMS(FormattedTextHelper.parse(advancement.description)),
                asMinecraftKey(advancement.background), AdvancementFrameType.valueOf(advancement.frame.name()),
                advancement.toast, advancement.announceToChat, advancement.hidden);
        display.a(advancement.xOffset, advancement.yOffset);
        return new Advancement(key, parent, display, AdvancementRewards.a, IMPOSSIBLE_CRITERIA, IMPOSSIBLE_REQUIREMENTS);
    }

    private static MinecraftKey asMinecraftKey(NamespacedKey key) {
        return key != null ? new MinecraftKey(key.getNamespace(), key.getKey()) : null;
    }
}
