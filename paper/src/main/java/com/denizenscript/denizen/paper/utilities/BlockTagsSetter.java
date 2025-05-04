package com.denizenscript.denizen.paper.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.VanillaTagHelper;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class BlockTagsSetter implements Listener {

    public static final MethodHandle BOOTSTRAP_CONTEXT_CONSTRUCTOR;

    static {
        try {
            Class<?> bootstrapContextImplClass = Class.forName("io.papermc.paper.plugin.bootstrap.PluginBootstrapContextImpl");
            BOOTSTRAP_CONTEXT_CONSTRUCTOR = ReflectionHelper.getConstructor(bootstrapContextImplClass, PluginMeta.class, Path.class, ComponentLogger.class, Path.class);
        }
        catch (Throwable e) {
            throw new RuntimeException("Failed to initialize BlockTagsSetter", e);
        }
    }

    public static final BlockTagsSetter INSTANCE = new BlockTagsSetter(Denizen.getInstance());

    public Map<TypedKey<BlockType>, Set<TagKey<BlockType>>> modifiedTags = new HashMap<>();
    public boolean batchReloadNeeded;

    public BlockTagsSetter(Denizen plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        try {
            BootstrapContext fakeContext = (BootstrapContext) BOOTSTRAP_CONTEXT_CONSTRUCTOR.invoke(plugin.getPluginMeta(), plugin.getDataPath(), plugin.getComponentLogger(), plugin.getFile().toPath());
            fakeContext.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.BLOCK), event -> {
                Map<TagKey<BlockType>, Collection<TypedKey<BlockType>>> allTags = event.registrar().getAllTags();
                for (Map.Entry<TypedKey<BlockType>, Set<TagKey<BlockType>>> entry : modifiedTags.entrySet()) {
                    TypedKey<BlockType> blockType = entry.getKey();
                    Set<TagKey<BlockType>> tags = entry.getValue();
                    for (Map.Entry<TagKey<BlockType>, Collection<TypedKey<BlockType>>> tagEntry : allTags.entrySet()) {
                        TagKey<BlockType> tagKey = tagEntry.getKey();
                        Collection<TypedKey<BlockType>> values = tagEntry.getValue();
                        if (values.contains(blockType) && !tags.contains(tagKey)) {
                            List<TypedKey<BlockType>> modifiedValues = new ArrayList<>(values);
                            modifiedValues.remove(blockType);
                            event.registrar().setTag(tagKey, modifiedValues);
                        }
                    }
                    for (TagKey<BlockType> tag : tags) {
                        event.registrar().addToTag(tag, List.of(blockType));
                    }
                }
            });
        }
        catch (Throwable e) {
            Debug.echoError(e);
        }
    }

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event) {
        if (batchReloadNeeded) {
            batchReloadNeeded = false;
            Bukkit.reloadData();
        }
    }

    public void setTags(Material material, Set<NamespacedKey> tags) {
        TypedKey<BlockType> blockKey = TypedKey.create(RegistryKey.BLOCK, material.getKey());
        Set<TagKey<BlockType>> tagKeys = tags.stream().map(tag -> TagKey.create(RegistryKey.BLOCK, tag)).collect(Collectors.toCollection(HashSet::new));
        Set<TagKey<BlockType>> oldTagKeys = modifiedTags.put(blockKey, tagKeys);
        if (tagKeys.equals(oldTagKeys)) {
            return;
        }
        VanillaTagHelper.tagsByMaterial.put(material, tags.stream().map(Utilities::namespacedKeyToString).collect(Collectors.toCollection(HashSet::new)));
        batchReloadNeeded = true;
    }
}
