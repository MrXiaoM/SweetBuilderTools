package top.mrxiaom.sweet.buildertools.material;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ItemStackUtil;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.ItemMaterial;
import top.mrxiaom.sweet.buildertools.func.AbstractModule;

@AutoRegister(priority = 990)
public class VanillaItemMaterial extends AbstractModule implements ItemMaterial.Provider {
    public static final ItemMaterial DEFAULT = new Impl(Material.PAPER, null);
    public VanillaItemMaterial(SweetBuilderTools plugin) {
        super(plugin);
        plugin.itemMaterialRegistry().register(this);
    }

    @Override
    public int getPriority() {
        return 2000;
    }

    @Override
    public @Nullable ItemMaterial parse(@NotNull String str) {
        Pair<Material, Integer> pair = ItemStackUtil.parseMaterial(str.toUpperCase());
        if (pair != null) {
            return new Impl(pair.key(), pair.value());
        }
        return null;
    }

    public static class Impl implements ItemMaterial {
        private final @NotNull Material material;
        private final @Nullable Integer dataValue;
        public Impl(@NotNull Material material, @Nullable Integer dataValue) {
            this.material = material;
            this.dataValue = dataValue;
        }

        @Override
        public @NotNull ItemStack create(@NotNull Player player, int amount) {
            if (dataValue != null) {
                //noinspection deprecation
                return new ItemStack(material, amount, dataValue.shortValue());
            } else {
                return new ItemStack(material, amount);
            }
        }
    }
}
