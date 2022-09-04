package com.cleanroommc.groovyscript.registry;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.IReloadableForgeRegistry;
import com.cleanroommc.groovyscript.compat.mods.ModPropertyContainer;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.compat.mods.ModSupport.Container;
import com.cleanroommc.groovyscript.core.mixin.jei.JeiProxyAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.JustEnoughItems;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@GroovyBlacklist
public class ReloadableRegistryManager {

    private static final AtomicBoolean firstLoad = new AtomicBoolean(true);
    private static final Map<Class<?>, List<Object>> recipeRecovery = new Object2ObjectOpenHashMap<>();
    private static final Map<Class<?>, List<Object>> scriptRecipes = new Object2ObjectOpenHashMap<>();

    public static boolean isFirstLoad() {
        return firstLoad.get();
    }

    public static void setLoaded() {
        firstLoad.set(false);
    }

    public static void backup(Class<?> clazz, Object object) {
        recipeRecovery.computeIfAbsent(clazz, k -> new ArrayList<>()).add(object);
    }

    public static void markScripted(Class<?> clazz, Object object) {
        scriptRecipes.computeIfAbsent(clazz, k -> new ArrayList<>()).add(object);
    }

    public static <T> List<T> restore(Class<?> registryClass, @SuppressWarnings("unused") Class<T> recipeClass) {
        @SuppressWarnings("unchecked") List<T> recoveredRecipes = (List<T>) recipeRecovery.remove(registryClass);
        return recoveredRecipes == null ? Collections.emptyList() : recoveredRecipes;
    }

    public static <T> List<T> unmarkScripted(Class<?> registryClass, @SuppressWarnings("unused") Class<T> recipeClass) {
        @SuppressWarnings("unchecked") List<T> marked = (List<T>) scriptRecipes.remove(registryClass);
        return marked == null ? Collections.emptyList() : marked;
    }

    @ApiStatus.Internal
    public static void onReload() {
        reloadForgeRegistries();
        ModSupport.getAllContainers().stream()
                .filter(Container::isLoaded)
                .map(Container::get)
                .map(ModPropertyContainer::getRegistries)
                .flatMap(Collection::stream)
                .forEach(VirtualizedRegistry::onReload);
    }

    @ApiStatus.Internal
    public static void afterScriptRun() {
        ModSupport.getAllContainers().stream()
                .filter(Container::isLoaded)
                .map(Container::get)
                .map(ModPropertyContainer::getRegistries)
                .flatMap(Collection::stream)
                .forEach(VirtualizedRegistry::afterScriptLoad);
        unfreezeForgeRegistries();
    }

    public static <V extends IForgeRegistryEntry<V>> void addRegistryEntry(IForgeRegistry<V> registry, String name, V entry) {
        addRegistryEntry(registry, new ResourceLocation(name), entry);
    }

    public static <V extends IForgeRegistryEntry<V>> void addRegistryEntry(IForgeRegistry<V> registry, ResourceLocation name, V entry) {
        ((IReloadableForgeRegistry<V>) registry).registerEntry(entry.setRegistryName(name));
    }

    public static <V extends IForgeRegistryEntry<V>> void removeRegistryEntry(IForgeRegistry<V> registry, String name) {
        removeRegistryEntry(registry, new ResourceLocation(name));
    }

    public static <V extends IForgeRegistryEntry<V>> void removeRegistryEntry(IForgeRegistry<V> registry, ResourceLocation name) {
        ((IReloadableForgeRegistry<V>) registry).removeEntry(name);
    }

    /**
     * Reloads JEI completely. Is called after groovy scripts are re ran.
     */
    @ApiStatus.Internal
    @SideOnly(Side.CLIENT)
    public static void reloadJei() {
        if (ModSupport.JEI.isLoaded()) {
            JeiProxyAccessor jeiProxy = (JeiProxyAccessor) JustEnoughItems.getProxy();
            jeiProxy.getStarter().start(jeiProxy.getPlugins(), jeiProxy.getTextures());
        }
    }

    private static void reloadForgeRegistries() {
        ((IReloadableForgeRegistry<?>) ForgeRegistries.RECIPES).onReload();
    }

    private static void unfreezeForgeRegistries() {
        ((ForgeRegistry<IRecipe>) ForgeRegistries.RECIPES).unfreeze();
    }

}
