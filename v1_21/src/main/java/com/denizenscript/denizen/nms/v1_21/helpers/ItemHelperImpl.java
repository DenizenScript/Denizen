package com.denizenscript.denizen.nms.v1_21.helpers;

import com.denizenscript.denizen.nms.interfaces.ItemHelper;
import com.denizenscript.denizen.nms.util.PlayerProfile;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.IntArrayTag;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.nms.v1_21.Handler;
import com.denizenscript.denizen.nms.v1_21.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_21.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_21.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.item.ItemComponentsPatch;
import com.denizenscript.denizen.objects.properties.item.ItemRawNBT;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.google.common.collect.*;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R3.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R3.CraftServer;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R3.inventory.*;
import org.bukkit.craftbukkit.v1_21_R3.map.CraftMapView;
import org.bukkit.craftbukkit.v1_21_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_21_R3.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.SmithingTrimRecipe;
import org.bukkit.inventory.TransmuteRecipe;
import org.bukkit.inventory.*;
import org.bukkit.map.MapView;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemHelperImpl extends ItemHelper {

    public static net.minecraft.world.item.crafting.RecipeHolder<?> getNMSRecipe(NamespacedKey key) {
        ResourceKey<Recipe<?>> nmsKey = ResourceKey.create(Registries.RECIPE, CraftNamespacedKey.toMinecraft(key));
        return ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager().byKey(nmsKey).orElse(null);
    }

    public static final Field Item_components = ReflectionHelper.getFields(Item.class).get(ReflectionMappingsInfo.Item_components, DataComponentMap.class);

    public static final Field RecipeManager_featureFlagSet = ReflectionHelper.getFields(RecipeManager.class).getFirstOfType(FeatureFlagSet.class);

    public void setMaxStackSize(Material material, int size) {
        try {
            ReflectionHelper.getFinalSetter(Material.class, "maxStack").invoke(material, size);
            Item nmsItem = BuiltInRegistries.ITEM.getValue(CraftNamespacedKey.toMinecraft(material.getKey()));
            DataComponentMap currentComponents = nmsItem.components();
            Item_components.set(nmsItem, DataComponentMap.composite(currentComponents, DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, size).build()));
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    public static RecipeManager getRecipeManager() {
        return ((CraftServer) Bukkit.getServer()).getServer().getRecipeManager();
    }

    public Object recipeManagerFeatureFlagSetCache = null;

    @Override
    public void blockRecipeFinalization() {
        try {
            RecipeManager manager = getRecipeManager();
            Object flags = RecipeManager_featureFlagSet.get(manager);
            if (flags != null) {
                recipeManagerFeatureFlagSetCache = flags;
                RecipeManager_featureFlagSet.set(manager, null);

            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void restoreRecipeFinalization() {
        try {
            RecipeManager manager = getRecipeManager();
            if (recipeManagerFeatureFlagSetCache != null) {
                RecipeManager_featureFlagSet.set(manager, recipeManagerFeatureFlagSetCache);
                manager.finalizeRecipeLoading();
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void removeRecipes(List<NamespacedKey> keys) {
        blockRecipeFinalization();
        RecipeManager manager = getRecipeManager();
        for (NamespacedKey key: keys) {
            ResourceKey<Recipe<?>> nmsKey = ResourceKey.create(Registries.RECIPE, CraftNamespacedKey.toMinecraft(key));
            manager.removeRecipe(nmsKey);
        }
        restoreRecipeFinalization();
    }

    @Override
    public Integer burnTime(Material material) {
        return MinecraftServer.getServer().fuelValues().burnDuration(new net.minecraft.world.item.ItemStack(CraftMagicNumbers.getItem(material)));
    }

    @Override
    public void setShapedRecipeIngredient(ShapedRecipe recipe, char c, ItemStack[] item, boolean exact) {
        if (item.length == 1 && item[0].getType() == Material.AIR) {
            recipe.setIngredient(c, new RecipeChoice.MaterialChoice(Material.AIR));
        }
        else if (exact) {
            recipe.setIngredient(c, new RecipeChoice.ExactChoice(item));
        }
        else {
            Material[] mats = new Material[item.length];
            for (int i = 0; i < item.length; i++) {
                mats[i] = item[i].getType();
            }
            recipe.setIngredient(c, new RecipeChoice.MaterialChoice(mats));
        }
    }

    // TODO: Recipe registration should be moved to the API
    public static Ingredient itemArrayToRecipe(ItemStack[] items, boolean exact) {
        if (!exact) {
            return Ingredient.of(Arrays.stream(items).map(item -> CraftMagicNumbers.getItem(item.getType())));
        }
        return Ingredient.ofStacks(Arrays.stream(items).map(CraftItemStack::asNMSCopy).toList());
    }

    public static ResourceKey<Recipe<?>> createRecipeKey(String name) {
        return ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath("denizen", name));
    }

    @Override
    public void registerFurnaceRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, float exp, int time, String type, boolean exact, String category) {
        ResourceKey<Recipe<?>> key = createRecipeKey(keyName);
        Ingredient itemRecipe = itemArrayToRecipe(ingredient, exact);
        AbstractCookingRecipe recipe;
        CookingBookCategory categoryValue = category == null ? CookingBookCategory.MISC : CookingBookCategory.valueOf(CoreUtilities.toUpperCase(category));
        if (type.equalsIgnoreCase("smoker")) {
            recipe = new SmokingRecipe(group, categoryValue, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        else if (type.equalsIgnoreCase("blast")) {
            recipe = new BlastingRecipe(group, categoryValue, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        else if (type.equalsIgnoreCase("campfire")) {
            recipe = new CampfireCookingRecipe(group, categoryValue, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        else {
            recipe = new SmeltingRecipe(group, categoryValue, itemRecipe, CraftItemStack.asNMSCopy(result), exp, time);
        }
        RecipeHolder<AbstractCookingRecipe> holder = new RecipeHolder<>(key, recipe);
        getRecipeManager().addRecipe(holder);
    }

    @Override
    public void registerStonecuttingRecipe(String keyName, String group, ItemStack result, ItemStack[] ingredient, boolean exact) {
        ResourceKey<Recipe<?>> key = createRecipeKey(keyName);
        Ingredient itemRecipe = itemArrayToRecipe(ingredient, exact);
        StonecutterRecipe recipe = new StonecutterRecipe(group, itemRecipe, CraftItemStack.asNMSCopy(result));
        RecipeHolder<StonecutterRecipe> holder = new RecipeHolder<>(key, recipe);
        getRecipeManager().addRecipe(holder);
    }

    @Override
    public void registerSmithingRecipe(String keyName, ItemStack result, ItemStack[] baseItem, boolean baseExact, ItemStack[] upgradeItem, boolean upgradeExact, ItemStack[] templateItem, boolean templateExact) {
        ResourceKey<Recipe<?>> key = createRecipeKey(keyName);
        Ingredient templateItemRecipe = itemArrayToRecipe(templateItem, templateExact);
        Ingredient baseItemRecipe = itemArrayToRecipe(baseItem, baseExact);
        Ingredient upgradeItemRecipe = itemArrayToRecipe(upgradeItem, upgradeExact);
        SmithingTransformRecipe recipe = new SmithingTransformRecipe(Optional.of(templateItemRecipe), Optional.of(baseItemRecipe), Optional.of(upgradeItemRecipe), CraftItemStack.asNMSCopy(result));
        RecipeHolder<SmithingTransformRecipe> holder = new RecipeHolder<>(key, recipe);
        getRecipeManager().addRecipe(holder);
    }

    @Override
    public void registerShapelessRecipe(String keyName, String group, ItemStack result, List<ItemStack[]> ingredients, boolean[] exact, String category) {
        ResourceKey<Recipe<?>> key = createRecipeKey(keyName);
        ArrayList<Ingredient> ingredientList = new ArrayList<>();
        CraftingBookCategory categoryValue = category == null ? CraftingBookCategory.MISC : CraftingBookCategory.valueOf(CoreUtilities.toUpperCase(category));
        for (int i = 0; i < ingredients.size(); i++) {
            ingredientList.add(itemArrayToRecipe(ingredients.get(i), exact[i]));
        }
        // TODO: 1.19.3: Add support for choosing a CraftingBookCategory
        ShapelessRecipe recipe = new ShapelessRecipe(group, categoryValue, CraftItemStack.asNMSCopy(result), NonNullList.of(null, ingredientList.toArray(new Ingredient[0])));
        RecipeHolder<ShapelessRecipe> holder = new RecipeHolder<>(key, recipe);
        getRecipeManager().addRecipe(holder);
    }

    @Override
    public void registerOtherRecipe(org.bukkit.inventory.Recipe recipe) {
        // This method copied from Bukkit CraftServer source, just to bypass unwanted paper patch
        CraftRecipe toAdd;
        if (recipe instanceof CraftRecipe craft) {
            toAdd = craft;
        }
        else if (recipe instanceof ShapedRecipe) {
            toAdd = CraftShapedRecipe.fromBukkitRecipe((ShapedRecipe)recipe);
        }
        else if (recipe instanceof org.bukkit.inventory.ShapelessRecipe) {
            toAdd = CraftShapelessRecipe.fromBukkitRecipe((org.bukkit.inventory.ShapelessRecipe)recipe);
        }
        else if (recipe instanceof FurnaceRecipe) {
            toAdd = CraftFurnaceRecipe.fromBukkitRecipe((FurnaceRecipe)recipe);
        }
        else if (recipe instanceof org.bukkit.inventory.BlastingRecipe) {
            toAdd = CraftBlastingRecipe.fromBukkitRecipe((org.bukkit.inventory.BlastingRecipe)recipe);
        }
        else if (recipe instanceof CampfireRecipe) {
            toAdd = CraftCampfireRecipe.fromBukkitRecipe((CampfireRecipe)recipe);
        }
        else if (recipe instanceof org.bukkit.inventory.SmokingRecipe) {
            toAdd = CraftSmokingRecipe.fromBukkitRecipe((org.bukkit.inventory.SmokingRecipe)recipe);
        }
        else if (recipe instanceof StonecuttingRecipe) {
            toAdd = CraftStonecuttingRecipe.fromBukkitRecipe((StonecuttingRecipe)recipe);
        }
        else if (recipe instanceof org.bukkit.inventory.SmithingTransformRecipe) {
            toAdd = CraftSmithingTransformRecipe.fromBukkitRecipe((org.bukkit.inventory.SmithingTransformRecipe)recipe);
        }
        else if (recipe instanceof org.bukkit.inventory.SmithingTrimRecipe) {
            toAdd = CraftSmithingTrimRecipe.fromBukkitRecipe((SmithingTrimRecipe)recipe);
        }
        else {
            if (!(recipe instanceof org.bukkit.inventory.TransmuteRecipe)) {
                if (recipe instanceof ComplexRecipe) {
                    throw new UnsupportedOperationException("Cannot add custom complex recipe");
                }
                return;
            }
            toAdd = CraftTransmuteRecipe.fromBukkitRecipe((TransmuteRecipe)recipe);
        }
        toAdd.addToCraftingManager();
    }

    @Override
    public String getJsonString(ItemStack itemStack) {
        String json = CraftItemStack.asNMSCopy(itemStack).getDisplayName().getStyle().toString().replace("\\", "\\\\").replace("\"", "\\\"");
        return json.substring(176, json.length() - 185);
    }

    @Override
    public JsonObject getRawHoverComponentsJson(ItemStack item) {
        DataComponentPatch nmsComponents = CraftItemStack.asNMSCopy(item).getComponentsPatch();
        if (nmsComponents.isEmpty()) {
            return null;
        }
        return DataComponentPatch.CODEC.encodeStart(CraftRegistry.getMinecraftRegistry().createSerializationContext(JsonOps.INSTANCE), nmsComponents).getOrThrow().getAsJsonObject();
    }

    @Override
    public ItemStack applyRawHoverComponentsJson(ItemStack item, JsonObject components) {
        return DataComponentPatch.CODEC.parse(CraftRegistry.getMinecraftRegistry().createSerializationContext(JsonOps.INSTANCE), components).mapOrElse(
                nmsComponents -> {
                    net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
                    nmsItem.applyComponents(nmsComponents);
                    return CraftItemStack.asCraftMirror(nmsItem);
                },
                error -> {
                    Debug.echoError("Invalid hover item data '" + components + "': " + error.message());
                    return item;
                });
    }

    @Override
    public PlayerProfile getSkullSkin(ItemStack is) {
        net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(is);
        ResolvableProfile profile = itemStack.get(DataComponents.PROFILE);
        if (profile != null) {
            Property property = Iterables.getFirst(profile.properties().get("textures"), null);
            return new PlayerProfile(profile.name().orElse(null), profile.id().orElse(null),
                    property != null ? property.value() : null,
                    property != null ? property.signature() : null);
        }
        return null;
    }

    @Override
    public ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile) {
        GameProfile gameProfile = ProfileEditorImpl.getGameProfile(playerProfile);
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        nmsItemStack.set(DataComponents.PROFILE, new ResolvableProfile(gameProfile));
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public ItemStack addNbtData(ItemStack itemStack, String key, Tag value) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        nmsItemStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData -> {
            CompoundTag updatedTag = CompoundTagImpl.fromNMSTag(customData.getUnsafe()).createBuilder().put(key, value).build();
            return CustomData.of(((CompoundTagImpl) updatedTag).toNMSTag());
        });
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    // TODO: 1.20.6: this now needs to serialize components into NBT every single time, should probably only return custom NBT data with specialized methods for other usages
    // TODO: 1.20.6: NBT structure is different basically everywhere, usages of this will need an update
    @Override
    public CompoundTag getNbtData(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack != null && !nmsItemStack.isEmpty()) {
            return CompoundTagImpl.fromNMSTag((net.minecraft.nbt.CompoundTag) nmsItemStack.save(CraftRegistry.getMinecraftRegistry()));
        }
        return new CompoundTagImpl(new HashMap<>());
    }

    // TODO: 1.20.6: same as getNbtData, ideally needs to only set custom NBT data and have specialized methods for other usages
    @Override
    public ItemStack setNbtData(ItemStack itemStack, CompoundTag compoundTag) {
        net.minecraft.world.item.ItemStack nmsItemStack = net.minecraft.world.item.ItemStack.parseOptional(CraftRegistry.getMinecraftRegistry(), ((CompoundTagImpl) compoundTag).toNMSTag());
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public CompoundTag getCustomData(ItemStack item) {
        CustomData customData = CraftItemStack.asNMSCopy(item).get(DataComponents.CUSTOM_DATA);
        return customData != null ? CompoundTagImpl.fromNMSTag(customData.getUnsafe()) : null;
    }

    @Override
    public ItemStack setCustomData(ItemStack item, CompoundTag data) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);
        if (data == null) {
            nmsItemStack.remove(DataComponents.CUSTOM_DATA);
        }
        else {
            nmsItemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(((CompoundTagImpl) data).toNMSTag()));
        }
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    public static final int DATA_VERSION_1_20_4 = 3700;

    @Override
    public ItemStack setPartialOldNbt(ItemStack item, CompoundTag oldTag) {
        int currentDataVersion = CraftMagicNumbers.INSTANCE.getDataVersion();
        net.minecraft.nbt.CompoundTag nmsOldTag = new net.minecraft.nbt.CompoundTag();
        nmsOldTag.putString("id", item.getType().getKey().toString());
        nmsOldTag.putByte("Count", (byte) item.getAmount());
        nmsOldTag.put("tag", ((CompoundTagImpl) oldTag).toNMSTag());
        net.minecraft.nbt.CompoundTag nmsUpdatedTag = (net.minecraft.nbt.CompoundTag) MinecraftServer.getServer().fixerUpper.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, nmsOldTag), DATA_VERSION_1_20_4, currentDataVersion).getValue();
        net.minecraft.nbt.CompoundTag nmsCurrentTag = (net.minecraft.nbt.CompoundTag) CraftItemStack.asNMSCopy(item).save(CraftRegistry.getMinecraftRegistry());
        net.minecraft.nbt.CompoundTag nmsMergedTag = nmsCurrentTag.merge(nmsUpdatedTag);
        return CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.parse(CraftRegistry.getMinecraftRegistry(), nmsMergedTag).orElseThrow());
    }

    @Override
    public CompoundTag getEntityData(ItemStack item) {
        CustomData entityData = CraftItemStack.asNMSCopy(item).get(DataComponents.ENTITY_DATA);
        return entityData != null ? CompoundTagImpl.fromNMSTag(entityData.getUnsafe()) : null;
    }

    public static final net.minecraft.nbt.CompoundTag EMPTY_TAG = new net.minecraft.nbt.CompoundTag();

    @Override
    public ItemStack setEntityData(ItemStack item, CompoundTag entityNbt, EntityType entityType) {
        net.minecraft.nbt.CompoundTag nmsEntityNbt = EMPTY_TAG;
        if (entityNbt != null && !entityNbt.isEmpty() && (!entityNbt.containsKey("id") || entityNbt.size() > 1)) {
            nmsEntityNbt = ((CompoundTagImpl) entityNbt).toNMSTag();
            nmsEntityNbt.putString("id", entityType.getKey().toString());
        }
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);
        CustomData.set(DataComponents.ENTITY_DATA, nmsItemStack, nmsEntityNbt);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public MapTag getRawComponentsPatch(ItemStack item, boolean excludeHandled) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);
        DataComponentPatch patch = nmsItemStack.getComponentsPatch();
        if (excludeHandled) {
            patch = patch.forget(componentType -> {
                ResourceLocation componentId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType);
                return ItemComponentsPatch.propertyHandledComponents.contains(componentId.toString());
            });
        }
        if (patch.isEmpty()) {
            return new MapTag();
        }
        RegistryOps<net.minecraft.nbt.Tag> registryOps = CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE);
        net.minecraft.nbt.CompoundTag nmsPatch = (net.minecraft.nbt.CompoundTag) DataComponentPatch.CODEC.encodeStart(registryOps, patch).getOrThrow();
        MapTag rawComponents = (MapTag) ItemRawNBT.jnbtTagToObject(CompoundTagImpl.fromNMSTag(nmsPatch));
        rawComponents.putObject(ItemComponentsPatch.DATA_VERSION_KEY, new ElementTag(CraftMagicNumbers.INSTANCE.getDataVersion()));
        return rawComponents;
    }

    @Override
    public ItemStack setRawComponentsPatch(ItemStack item, MapTag rawComponentsMap, int dataVersion, Consumer<String> errorHandler) {
        int currentDataVersion = CraftMagicNumbers.INSTANCE.getDataVersion();
        Tag rawComponents = ItemRawNBT.convertObjectToNbt(rawComponentsMap.identify(), CoreUtilities.errorButNoDebugContext, "");
        net.minecraft.nbt.CompoundTag nmsRawComponents = ((CompoundTagImpl) rawComponents).toNMSTag();
        RegistryOps<net.minecraft.nbt.Tag> registryOps = CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE);
        if (dataVersion < currentDataVersion) {
            net.minecraft.nbt.CompoundTag legacyItemData = new net.minecraft.nbt.CompoundTag();
            legacyItemData.putString("id", item.getType().getKey().toString());
            legacyItemData.putInt("count", item.getAmount());
            legacyItemData.put("components", nmsRawComponents);
            net.minecraft.nbt.CompoundTag nmsUpdatedTag = (net.minecraft.nbt.CompoundTag) MinecraftServer.getServer().fixerUpper.update(References.ITEM_STACK, new Dynamic<>(registryOps, legacyItemData), dataVersion, currentDataVersion).getValue();
            nmsRawComponents = nmsUpdatedTag.getCompound("components");
        }
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);
        DataComponentPatch.CODEC.parse(registryOps, nmsRawComponents)
                .ifError(error -> errorHandler.accept(error.message()))
                .ifSuccess(nmsItemStack::applyComponents);
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    public static final Field AdventureModePredicate_predicates = ReflectionHelper.getFields(AdventureModePredicate.class).get(ReflectionMappingsInfo.AdventureModePredicate_predicates);

    @Override
    public List<Material> getCanPlaceOn(ItemStack item) {
        return getAdventureModePredicateMaterials(item, DataComponents.CAN_PLACE_ON);
    }

    @Override
    public ItemStack setCanPlaceOn(ItemStack item, List<Material> canPlaceOn) {
        return setAdventureModePredicateMaterials(item, DataComponents.CAN_PLACE_ON, canPlaceOn);
    }

    @Override
    public List<Material> getCanBreak(ItemStack item) {
        return getAdventureModePredicateMaterials(item, DataComponents.CAN_BREAK);
    }

    @Override
    public ItemStack setCanBreak(ItemStack item, List<Material> canBreak) {
        return setAdventureModePredicateMaterials(item, DataComponents.CAN_BREAK, canBreak);
    }

    private List<Material> getAdventureModePredicateMaterials(ItemStack item, DataComponentType<AdventureModePredicate> nmsComponent) {
        AdventureModePredicate nmsAdventurePredicate = CraftItemStack.asNMSCopy(item).get(nmsComponent);
        if (nmsAdventurePredicate == null) {
            return null;
        }
        List<BlockPredicate> nmsPredicates;
        try {
            nmsPredicates = (List<BlockPredicate>) AdventureModePredicate_predicates.get(nmsAdventurePredicate);
        }
        catch (Throwable e) {
            Debug.echoError(e);
            return null;
        }
        List<Material> materials = new ArrayList<>();
        for (BlockPredicate nmsPredicate : nmsPredicates) {
            nmsPredicate.blocks().ifPresent(nmsHolderSet -> {
                for (Holder<Block> nmsHolder : nmsHolderSet) {
                    materials.add(CraftMagicNumbers.getMaterial(nmsHolder.value()));
                }
            });
        }
        return materials;
    }

    private ItemStack setAdventureModePredicateMaterials(ItemStack item, DataComponentType<AdventureModePredicate> nmsComponent, List<Material> materials) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);
        AdventureModePredicate nmsAdventurePredicate = nmsItemStack.get(nmsComponent);
        if (materials == null) {
            if (nmsAdventurePredicate == null) {
                return item;
            }
            nmsItemStack.remove(nmsComponent);
            return CraftItemStack.asBukkitCopy(nmsItemStack);
        }
        BlockPredicate nmsPredicate = new BlockPredicate(Optional.of(
                HolderSet.direct(material -> BuiltInRegistries.BLOCK.get(CraftNamespacedKey.toMinecraft(material.getKey())).orElseThrow(), materials)
        ), Optional.empty(), Optional.empty());
        nmsItemStack.set(nmsComponent, new AdventureModePredicate(List.of(nmsPredicate), nmsAdventurePredicate == null || nmsAdventurePredicate.showInTooltip()));
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }

    @Override
    public void setInventoryItem(Inventory inventory, ItemStack item, int slot) {
        if (inventory instanceof CraftInventoryPlayer && ((CraftInventoryPlayer) inventory).getInventory().player == null) {
            ((CraftInventoryPlayer) inventory).getInventory().setItem(slot, CraftItemStack.asNMSCopy(item));
        }
        else {
            inventory.setItem(slot, item);
        }
    }

    @Override
    public IntArrayTag convertUuidToNbt(UUID id) {
        return new IntArrayTag(NbtUtils.createUUID(id).getAsIntArray());
    }

    @Override
    public UUID convertNbtToUuid(IntArrayTag id) {
        return NbtUtils.loadUUID(new net.minecraft.nbt.IntArrayTag(id.getValue()));
    }

    @Override
    public String getDisplayName(ItemTag item) {
        if (!item.getItemMeta().hasDisplayName()) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        Component nmsDisplayName = nmsItemStack.get(DataComponents.CUSTOM_NAME);
        return FormattedTextHelper.stringify(Handler.componentToSpigot(nmsDisplayName));
    }

    @Override
    public List<String> getLore(ItemTag item) {
        if (!item.getItemMeta().hasLore()) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        ItemLore nmsLore = nmsItemStack.get(DataComponents.LORE);
        List<String> outList = new ArrayList<>(nmsLore.lines().size());
        for (Component nmsLoreLine : nmsLore.lines()) {
            outList.add(FormattedTextHelper.stringify(Handler.componentToSpigot(nmsLoreLine)));
        }
        return outList;
    }

    @Override
    public void setDisplayName(ItemTag item, String name) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        if (name == null || name.isEmpty()) {
            nmsItemStack.remove(DataComponents.CUSTOM_NAME);
        }
        else {
            nmsItemStack.set(DataComponents.CUSTOM_NAME, Handler.componentToNMS(FormattedTextHelper.parse(name, ChatColor.WHITE)));
        }
        item.setItemStack(CraftItemStack.asBukkitCopy(nmsItemStack));
    }

    @Override
    public void setLore(ItemTag item, List<String> lore) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item.getItemStack());
        if (lore == null || lore.isEmpty()) {
            nmsItemStack.remove(DataComponents.LORE);
        }
        else {
            List<Component> nmsLore = new ArrayList<>(lore.size());
            for (String loreLine : lore) {
                nmsLore.add(Handler.componentToNMS(FormattedTextHelper.parse(loreLine, ChatColor.WHITE)));
            }
            nmsItemStack.set(DataComponents.LORE, new ItemLore(nmsLore));
        }
        item.setItemStack(CraftItemStack.asBukkitCopy(nmsItemStack));
    }

    /**
     * Copied from MapItem.getCorrectStateForFluidBlock.
     */
    public static BlockState getCorrectStateForFluidBlock(Level world, BlockState blockState, BlockPos blockPos) {
        FluidState fluid = blockState.getFluidState();
        return !fluid.isEmpty() && !blockState.isFaceSturdy(world, blockPos, Direction.UP) ? fluid.createLegacyBlock() : blockState;
    }

    /**
     * Copied from MapItem.update, redesigned slightly to render totally rather than just relative to a player.
     * Some variables manually renamed for readability.
     */
    public static void renderFullMap(MapItemSavedData worldmap, int xMin, int zMin, int xMax, int zMax) {
        Level world = ((CraftWorld) worldmap.mapView.getWorld()).getHandle();
        int scale = 1 << worldmap.scale;
        int mapX = worldmap.centerX;
        int mapZ = worldmap.centerZ;
        for (int x = xMin; x < xMax; x++) {
            double d0 = 0.0D;
            for (int z = zMin; z < zMax; z++) {
                int k2 = (mapX / scale + x - 64) * scale;
                int l2 = (mapZ / scale + z - 64) * scale;
                Multiset<MapColor> multiset = LinkedHashMultiset.create();
                LevelChunk chunk = world.getChunkAt(new BlockPos(k2, 0, l2));
                if (!chunk.isEmpty()) {
                    ChunkPos chunkcoordintpair = chunk.getPos();
                    int i3 = k2 & 15;
                    int j3 = l2 & 15;
                    int k3 = 0;
                    double d1 = 0.0D;
                    if (world.dimensionType().hasCeiling()) {
                        int l3 = k2 + l2 * 231871;
                        l3 = l3 * l3 * 31287121 + l3 * 11;
                        if ((l3 >> 20 & 1) == 0) {
                            multiset.add(Blocks.DIRT.defaultBlockState().getMapColor(world, BlockPos.ZERO), 10);
                        }
                        else {
                            multiset.add(Blocks.STONE.defaultBlockState().getMapColor(world, BlockPos.ZERO), 100);
                        }

                        d1 = 100.0D;
                    }
                    else {
                        BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();
                        BlockPos.MutableBlockPos blockposition_mutableblockposition1 = new BlockPos.MutableBlockPos();
                        for (int i4 = 0; i4 < scale; ++i4) {
                            for (int j4 = 0; j4 < scale; ++j4) {
                                int k4 = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, i4 + i3, j4 + j3) + 1;
                                BlockState iblockdata;
                                if (k4 <= world.getMinY() + 1) {
                                    iblockdata = Blocks.BEDROCK.defaultBlockState();
                                }
                                else {
                                    do {
                                        --k4;
                                        blockposition_mutableblockposition.set(chunkcoordintpair.getMinBlockX() + i4 + i3, k4, chunkcoordintpair.getMinBlockZ() + j4 + j3);
                                        iblockdata = chunk.getBlockState(blockposition_mutableblockposition);
                                    } while (iblockdata.getMapColor(world, blockposition_mutableblockposition) == MapColor.NONE && k4 > world.getMinY());
                                    if (k4 > world.getMinY() && !iblockdata.getFluidState().isEmpty()) {
                                        int l4 = k4 - 1;
                                        blockposition_mutableblockposition1.set(blockposition_mutableblockposition);

                                        BlockState iblockdata1;
                                        do {
                                            blockposition_mutableblockposition1.setY(l4--);
                                            iblockdata1 = chunk.getBlockState(blockposition_mutableblockposition1);
                                            k3++;
                                        } while (l4 > world.getMinY() && !iblockdata1.getFluidState().isEmpty());
                                        iblockdata = getCorrectStateForFluidBlock(world, iblockdata, blockposition_mutableblockposition);
                                    }
                                }
                                worldmap.checkBanners(world, chunkcoordintpair.getMinBlockX() + i4 + i3, chunkcoordintpair.getMinBlockZ() + j4 + j3);
                                d1 += (double) k4 / (double) (scale * scale);
                                multiset.add(iblockdata.getMapColor(world, blockposition_mutableblockposition));
                            }
                        }
                    }
                    k3 /= scale * scale;
                    double d2 = (d1 - d0) * 4.0D / (double) (scale + 4) + ((double) (x + z & 1) - 0.5D) * 0.4D;
                    byte b0 = 1;
                    if (d2 > 0.6D) {
                        b0 = 2;
                    }
                    if (d2 < -0.6D) {
                        b0 = 0;
                    }
                    MapColor materialmapcolor = Iterables.getFirst(Multisets.copyHighestCountFirst(multiset), MapColor.NONE);
                    if (materialmapcolor == MapColor.WATER) {
                        d2 = (double) k3 * 0.1D + (double) (x + z & 1) * 0.2D;
                        b0 = 1;
                        if (d2 < 0.5D) {
                            b0 = 2;
                        }
                        if (d2 > 0.9D) {
                            b0 = 0;
                        }
                    }
                    d0 = d1;
                    worldmap.updateColor(x, z, (byte) (materialmapcolor.id * 4 + b0));
                }
            }
        }
    }

    @Override
    public boolean renderEntireMap(int mapId, int xMin, int zMin, int xMax, int zMax) {
        MapItemSavedData worldmap = ((CraftServer) Bukkit.getServer()).getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD).getMapData(new MapId(mapId));
        if (worldmap == null) {
            return false;
        }
        renderFullMap(worldmap, xMin, zMin, xMax, zMax);
        return true;
    }

    @Override
    public BlockData getPlacedBlock(Material material) {
        Item nmsItem = BuiltInRegistries.ITEM.getOptional(CraftNamespacedKey.toMinecraft(material.getKey())).orElse(null);
        if (nmsItem instanceof BlockItem) {
            Block block = ((BlockItem) nmsItem).getBlock();
            return CraftBlockData.fromData(block.defaultBlockState());
        }
        return null;
    }

    @Override
    public boolean isValidMix(ItemStack input, ItemStack ingredient) {
        net.minecraft.world.item.ItemStack nmsInput = CraftItemStack.asNMSCopy(input);
        net.minecraft.world.item.ItemStack nmsIngredient = CraftItemStack.asNMSCopy(ingredient);
        return MinecraftServer.getServer().potionBrewing().hasMix(nmsInput, nmsIngredient);
    }

    public static Class<?> PaperPotionMix_CLASS = null;
    public static Map<NamespacedKey, BrewingRecipe> customBrewingRecipes = null;

    @Override
    public Map<NamespacedKey, BrewingRecipe> getCustomBrewingRecipes() {
        if (customBrewingRecipes == null) {
            customBrewingRecipes = Maps.transformValues((Map<NamespacedKey, ?>) ReflectionHelper.getFieldValue(PotionBrewing.class, "customMixes", MinecraftServer.getServer().potionBrewing()), paperMix -> {
                if (PaperPotionMix_CLASS == null) {
                    PaperPotionMix_CLASS = paperMix.getClass();
                }
                RecipeChoice ingredient = convertChoice(ReflectionHelper.getFieldValue(PaperPotionMix_CLASS, "ingredient", paperMix));
                RecipeChoice input = convertChoice(ReflectionHelper.getFieldValue(PaperPotionMix_CLASS, "input", paperMix));
                ItemStack result = CraftItemStack.asBukkitCopy(ReflectionHelper.getFieldValue(PaperPotionMix_CLASS, "result", paperMix));
                return new BrewingRecipe(input, ingredient, result);
            });
        }
        return customBrewingRecipes;
    }

    private RecipeChoice convertChoice(Predicate<net.minecraft.world.item.ItemStack> nmsPredicate) {
        // Not an instance of net.minecraft.world.item.crafting.Ingredient = a predicate recipe choice
        if (nmsPredicate instanceof Ingredient ingredient) {
            return CraftRecipe.toBukkit(ingredient);
        }
        return PaperAPITools.instance.createPredicateRecipeChoice(item -> nmsPredicate.test(CraftItemStack.asNMSCopy(item)));
    }

    @Override
    public byte[] renderMap(MapView mapView, Player player) {
        return ((CraftMapView) mapView).render((CraftPlayer) player).buffer;
    }

    @Override
    public int getFoodPoints(Material itemType) {
        return CraftMagicNumbers.getItem(itemType).components().get(DataComponents.FOOD).nutrition();
    }

    @Override
    public DyeColor getShieldColor(ItemStack item) {
        net.minecraft.world.item.DyeColor nmsColor = CraftItemStack.asNMSCopy(item).get(DataComponents.BASE_COLOR);
        return nmsColor != null ? DyeColor.getByWoolData((byte) nmsColor.getId()) : null;
    }

    @Override
    public ItemStack setShieldColor(ItemStack item, DyeColor color) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);
        if (color != null) {
            nmsItemStack.set(DataComponents.BASE_COLOR, net.minecraft.world.item.DyeColor.byId(color.getWoolData()));
        }
        else {
            nmsItemStack.remove(DataComponents.BASE_COLOR);
        }
        return CraftItemStack.asBukkitCopy(nmsItemStack);
    }
}
