package top.mrxiaom.sweet.buildertools.material;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.IMaterial;
import top.mrxiaom.sweet.buildertools.func.AbstractModule;

@AutoRegister(priority = 990)
public class VanillaMaterial extends AbstractModule implements IMaterial.Provider {
    public VanillaMaterial(SweetBuilderTools plugin) {
        super(plugin);
        plugin.materialRegistry().register(this);
    }

    @Override
    public int getPriority() {
        return 2000;
    }

    @Override
    public @Nullable IMaterial parse(@NotNull String str) {
        Material material = Util.valueOrNull(Material.class, str);
        if (material != null) {
            return new Impl(material);
        }
        return null;
    }

    public static class Impl implements IMaterial {
        private final Material material;
        public Impl(Material material) {
            this.material = material;
        }

        @Override
        public @NotNull String key() {
            return String.valueOf(material);
        }

        @Override
        public @NotNull ItemStack getIcon(@NotNull Player player) {
            return new ItemStack(material);
        }

        @Override
        public boolean placeBlock(@NotNull Player player, @NotNull Block block) {
            try {
                // 1.18+
                BlockData data = material.createBlockData();
                if (block.canPlace(data)) {
                    block.setBlockData(data);
                    return true;
                }
                return false;
            } catch (LinkageError ignored) {
            }
            try {
                // 1.13+
                BlockData data = material.createBlockData();
                block.setBlockData(data);
                return true;
            } catch (LinkageError ignored) {
            }
            // 1.8 - 1.12
            block.setType(material);
            return true;
        }
    }
}
