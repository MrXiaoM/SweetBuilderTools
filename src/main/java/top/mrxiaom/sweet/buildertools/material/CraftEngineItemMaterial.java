package top.mrxiaom.sweet.buildertools.material;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.ItemMaterial;
import top.mrxiaom.sweet.buildertools.func.AbstractModule;

@AutoRegister(priority = 990, requirePlugins = {"CraftEngine"})
public class CraftEngineItemMaterial extends AbstractModule implements ItemMaterial.Provider, Listener {
    public CraftEngineItemMaterial(SweetBuilderTools plugin) {
        super(plugin);
        if (Util.isPresent("net.momirealms.craftengine.bukkit.item.BukkitItemDefinition")) {
            plugin.itemMaterialRegistry().register(this);
            registerEvents();
        } else {
            warn("你的 CraftEngine 太旧了，请更新到 26.5 或以上");
        }
    }

    @EventHandler
    public void onReload(CraftEngineReloadEvent e) {
        if (e.isFirstReload()) return;
        plugin.reloadConfig();
    }

    @Override
    public @Nullable ItemMaterial parse(@NotNull String str) {
        if (str.startsWith("craftengine:")) {
            BukkitItemDefinition customItem = CraftEngineItems.byId(Key.of(str.substring(12)));
            if (customItem != null) {
                return new Impl(customItem);
            }
        }
        return null;
    }

    public static class Impl implements ItemMaterial {
        private final @NotNull BukkitItemDefinition customItem;
        public Impl(@NotNull BukkitItemDefinition customItem) {
            this.customItem = customItem;
        }

        @Override
        public @NotNull String key() {
            return String.valueOf(customItem.id());
        }

        @Override
        public @NotNull ItemStack create(@NotNull Player player, int amount) {
            return customItem.buildBukkitItem(ItemBuildContext.of(BukkitAdaptor.adapt(player)), amount);
        }

        @Override
        public boolean isItemMatch(@NotNull Player player, @NotNull ItemStack item) {
            return customItem.id().equals(CraftEngineItems.getCustomItemId(item));
        }
    }
}
