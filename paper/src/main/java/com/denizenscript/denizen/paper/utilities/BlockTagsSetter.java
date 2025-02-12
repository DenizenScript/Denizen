package com.denizenscript.denizen.paper.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class BlockTagsSetter {

    public static final MethodHandle JAVA_PLUGIN_GET_FILE = ReflectionHelper.getMethodHandle(JavaPlugin.class, "getFile");
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

    Map<TypedKey<BlockType>, Set<TagKey<BlockType>>> modifiedTags = new HashMap<>();

    public BlockTagsSetter(JavaPlugin plugin) {
        try {
            File pluginSourceFile = (File) JAVA_PLUGIN_GET_FILE.invoke(plugin);
            BootstrapContext fakeContext = (BootstrapContext) BOOTSTRAP_CONTEXT_CONSTRUCTOR.invoke(plugin.getPluginMeta(), plugin.getDataPath(), plugin.getComponentLogger(), pluginSourceFile.toPath());
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

    public void setTags(Material material, Set<NamespacedKey> tags) {
        TypedKey<BlockType> blockKey = TypedKey.create(RegistryKey.BLOCK, material.getKey());
        modifiedTags.put(blockKey, tags.stream().map(tag -> TagKey.create(RegistryKey.BLOCK, tag)).collect(Collectors.toSet()));
        Bukkit.reloadData();
    }
}
