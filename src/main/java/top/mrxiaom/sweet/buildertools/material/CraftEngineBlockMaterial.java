package top.mrxiaom.sweet.buildertools.material;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.InteractUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockHitResult;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.BlockMaterial;
import top.mrxiaom.sweet.buildertools.api.ItemMaterial;
import top.mrxiaom.sweet.buildertools.api.PlaceMetadata;
import top.mrxiaom.sweet.buildertools.func.AbstractModule;

@AutoRegister(priority = 990, requirePlugins = {"CraftEngine"})
public class CraftEngineBlockMaterial extends AbstractModule implements BlockMaterial.Provider {
    public CraftEngineBlockMaterial(SweetBuilderTools plugin) {
        super(plugin);
        if (Util.isPresent("net.momirealms.craftengine.bukkit.item.BukkitItemDefinition")) {
            plugin.blockMaterialRegistry().register(this);
        }
    }

    @Override
    public @Nullable BlockMaterial parse(@NotNull String str) {
        if (str.startsWith("craftengine:")) {
            Key blockId = Key.of(str.substring(12));
            BlockDefinition customBlock = CraftEngineBlocks.byId(blockId);
            if (customBlock != null) {
                return new Impl(customBlock);
            }
        }
        return null;
    }

    public static class Impl implements BlockMaterial {
        private final BlockDefinition customBlock;
        private final BukkitItemDefinition customIcon;
        private final ItemMaterial itemMaterial;
        public Impl(BlockDefinition customBlock) {
            this.customBlock = customBlock;
            Key itemId = customBlock.defaultState().settings().itemId();
            this.customIcon = itemId != null
                    ? CraftEngineItems.byId(itemId)
                    : null;
            if (customIcon != null) {
                this.itemMaterial = new CraftEngineItemMaterial.Impl(customIcon);
            } else {
                this.itemMaterial = new DefaultIcon(customBlock);
            }
        }

        @Override
        public @NotNull String key() {
            return "craftengine:" + customBlock.id();
        }

        @Override
        public @NotNull String getDisplayName(@NotNull Player player) {
            if (customIcon != null) {
                return "<translate:" + customIcon.translationKey() + ">";
            } else {
                return "<translate:" + customBlock.translationKey() + ">";
            }
        }

        @Override
        public @NotNull ItemStack getIcon(@NotNull Player player) {
            return itemMaterial.create(player, 1);
        }

        @Override
        public @NotNull ItemMaterial getItemMaterial() {
            return itemMaterial;
        }

        @Override
        public boolean placeBlock(@NotNull PlaceMetadata metadata) {
            Block block = metadata.block();
            Direction direction = DirectionUtils.toDirection(metadata.blockFace());
            BlockPos pos = LocationUtils.toBlockPos(metadata.clickedBlock().getLocation());
            Location interactionPoint = metadata.interactPoint();
            Vec3d vec3d = interactionPoint != null
                    ? new Vec3d(interactionPoint.getX(), interactionPoint.getY(), interactionPoint.getZ())
                    : new Vec3d(pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5);
            BlockHitResult hitResult = new BlockHitResult(vec3d, direction, pos, false);
            BlockPlaceContext context = new BlockPlaceContext(
                    BukkitAdaptor.adapt(block.getWorld()),
                    BukkitAdaptor.adapt(metadata.player()),
                    InteractionHand.MAIN_HAND,
                    BukkitAdaptor.adapt(metadata.item()),
                    hitResult);
            if (!InteractUtils.canPlaceBlock(context)) {
                return false;
            }
            ImmutableBlockState blockState = customBlock.getStateForPlacement(context);
            return CraftEngineBlocks.place(block.getLocation(), blockState, 0, false);
        }

        @Override
        public void placeSound(@NotNull PlaceMetadata metadata, @NotNull Entity entity) {
            BukkitExistingBlock block = BukkitAdaptor.adapt(metadata.block());
            ImmutableBlockState blockState = block.customBlockState();
            if (blockState != null) {
                SoundData data = blockState.settings().sounds().placeSound();
                block.world().playBlockSound(block.position(), data);
            }
        }
    }

    public static class DefaultIcon implements ItemMaterial {
        private final BlockDefinition customBlock;
        public DefaultIcon(BlockDefinition customBlock) {
            this.customBlock = customBlock;
        }

        @Override
        public @NotNull String key() {
            return "";
        }

        @Override
        public @NotNull ItemStack create(@NotNull Player player, int amount) {
            ItemStack item = new ItemStack(Material.STONE);
            AdventureItemStack.setItemDisplayName(item, Component.translatable(customBlock.translationKey()));
            return item;
        }

        @Override
        public boolean isItemMatch(@NotNull Player player, @NotNull ItemStack item) {
            return false;
        }
    }
}
