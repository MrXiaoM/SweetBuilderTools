package top.mrxiaom.sweet.buildertools.material;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.CollectionUtils;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.BlockMaterial;
import top.mrxiaom.sweet.buildertools.api.ItemMaterial;
import top.mrxiaom.sweet.buildertools.api.PlaceMetadata;
import top.mrxiaom.sweet.buildertools.func.AbstractModule;

import java.util.StringJoiner;

@AutoRegister(priority = 990)
public class VanillaBlockMaterial extends AbstractModule implements BlockMaterial.Provider {
    public VanillaBlockMaterial(SweetBuilderTools plugin) {
        super(plugin);
        plugin.blockMaterialRegistry().register(this);
    }

    @Override
    public int getPriority() {
        return 2000;
    }

    @Override
    public @Nullable BlockMaterial parse(@NotNull String str) {
        Material material = Util.valueOrNull(Material.class, str);
        if (material != null) {
            return new Impl(material);
        }
        return null;
    }

    public static class Impl implements BlockMaterial {
        private final Material material;
        private final ItemMaterial itemMaterial;
        public Impl(Material material) {
            this.material = material;
            this.itemMaterial = new VanillaItemMaterial.Impl(material, null);
        }

        @Override
        public @NotNull String key() {
            return String.valueOf(material);
        }

        @Override
        @SuppressWarnings({"removal"})
        public @NotNull String getDisplayName(@NotNull Player player) {
            try {
                // Paper Adventure 方法
                return "<translate:" + material.translationKey() + ">";
            } catch (LinkageError ignored) {
            }
            try {
                // Spigot 1.19+ 方法
                return "<translate:" + material.getTranslationKey() + ">";
            } catch (LinkageError ignored) {
            }
            try {
                // LangUtils 1.7-1.18
                return com.meowj.langutils.lang.LanguageHelper.getItemName(new ItemStack(material), player);
            } catch (LinkageError ignored) {
            }
            // 最坏的情况下，将 material 格式化为每个单次首字母大写
            StringJoiner words = new StringJoiner(" ");
            for (String word : CollectionUtils.split(String.valueOf(material), '_')) {
                char upperCase = Character.toUpperCase(word.charAt(0));
                words.add(upperCase + word.substring(1).toLowerCase());
            }
            return words.toString();
        }

        @Override
        public @NotNull ItemStack getIcon(@NotNull Player player) {
            return new ItemStack(material);
        }

        @Override
        public @NotNull ItemMaterial getItemMaterial() {
            return itemMaterial;
        }

        @Override
        public boolean placeBlock(@NotNull PlaceMetadata metadata) {
            Block block = metadata.block();
            try {
                // 1.18+
                BlockData data = material.createBlockData();
                if (block.canPlace(data)) {
                    postPlace(metadata, data);
                    block.setBlockData(data);
                    return true;
                }
                return false;
            } catch (LinkageError ignored) {
            }
            try {
                // 1.13+
                BlockData data = material.createBlockData();
                postPlace(metadata, data);
                block.setBlockData(data);
                return true;
            } catch (LinkageError ignored) {
            }
            // 1.8 - 1.12
            block.setType(material);
            return true;
        }

        private void postPlace(PlaceMetadata metadata, BlockData blockData) {
            if (blockData instanceof Directional) {
                Location eyeLocation = metadata.player().getEyeLocation();
                BlockFace blockFacing = getOppositeFacing((eyeLocation.getYaw() + 180.0f) % 360.0f);
                ((Directional) blockData).setFacing(blockFacing);
                if (blockData instanceof Bisected) {
                    Location interactPoint = metadata.interactPoint();
                    if (interactPoint != null) {
                        double y = interactPoint.getY() - interactPoint.getBlockY();
                        Bisected.Half half = y >= 0.5
                                ? Bisected.Half.TOP
                                : Bisected.Half.BOTTOM;
                        ((Bisected) blockData).setHalf(half);
                    }
                }
            }
        }

        private BlockFace getOppositeFacing(float angle) {
            // 获取玩家面向方向的反向方向
            if (angle >= 45) {
                if (angle < 135) return BlockFace.EAST;
                if (angle < 225) return BlockFace.SOUTH;
                if (angle < 315) return BlockFace.WEST;
            }
            return BlockFace.NORTH;
        }

        @Override
        @SuppressWarnings("removal")
        public void placeSound(@NotNull PlaceMetadata metadata, @NotNull Entity entity) {
            Block block = metadata.block();
            World world = block.getWorld();
            try {
                // Paper 1.19+
                SoundGroup group = block.getBlockSoundGroup();
                world.playSound(entity, group.getPlaceSound(), SoundCategory.BLOCKS, group.getVolume(), group.getPitch());
                return;
            } catch (LinkageError ignored) {
            }
            try {
                // Paper 1.15+
                com.destroystokyo.paper.block.BlockSoundGroup group = block.getSoundGroup();
                world.playSound(entity, group.getPlaceSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
            } catch (LinkageError ignored) {
            }
        }
    }
}
