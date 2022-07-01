package com.cleanroommc.groovyscript.registry;

import com.cleanroommc.groovyscript.mixin.JeiProxyAccessor;
import com.cleanroommc.groovyscript.mixin.RegistryManagerAccessor;
import mezz.jei.JustEnoughItems;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;

public class ReloadableRegistryManager {

    /**
     * Reloads all forge registries, removing all reloadable entries.
     * Is called before groovy scripts are ran.
     */
    @ApiStatus.Internal
    public static void onReload() {
        ((RegistryManagerAccessor) RegistryManager.ACTIVE).getRegistries().values().forEach(registry -> ((IReloadableForgeRegistry<?>) registry).onReload());
        ((RegistryManagerAccessor) RegistryManager.VANILLA).getRegistries().values().forEach(registry -> ((IReloadableForgeRegistry<?>) registry).onReload());
        ((RegistryManagerAccessor) RegistryManager.FROZEN).getRegistries().values().forEach(registry -> ((IReloadableForgeRegistry<?>) registry).onReload());
    }

    /**
     * Registers a reloadable entry to a forge registry
     *
     * @param registry registry to register too
     * @param value    value to register
     * @param <V>      type of the registry
     */
    public static <V extends IForgeRegistryEntry<V>> void registerEntry(IForgeRegistry<V> registry, V value) {
        ((IReloadableForgeRegistry<V>) registry).registerReloadableEntry(value);
    }

    /**
     * Registers a reloadable recipe
     *
     * @param recipe recipe to register
     */
    public static void registerRecipe(IRecipe recipe) {
        registerEntry(ForgeRegistries.RECIPES, recipe);
    }

    /**
     * Reloads JEI completely. Is called after groovy scripts are re ran.
     */
    @ApiStatus.Internal
    @SideOnly(Side.CLIENT)
    public static void reloadJei() {
        if (Loader.isModLoaded("jei")) {
            JeiProxyAccessor jeiProxy = (JeiProxyAccessor) JustEnoughItems.getProxy();
            jeiProxy.getStarter().start(jeiProxy.getPlugins(), jeiProxy.getTextures());
        }
    }
}