package top.mrxiaom.sweet.buildertools.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;

public interface BlockMaterial {
    /**
     * 方块键，用于储存到物品中作为“当前选中方块”
     */
    @NotNull String key();

    /**
     * 获取方块显示名称，支持 MiniMessage
     */
    @NotNull String getDisplayName(@NotNull Player player);

    /**
     * 获取方块图标，用于在界面显示
     */
    @NotNull ItemStack getIcon(@NotNull Player player);

    /**
     * 获取方块对应的物品材质
     */
    @NotNull ItemMaterial getItemMaterial();

    /**
     * 在指定位置放置方块
     * @param metadata 方块放置行为元数据
     * @return 是否放置成功
     */
    boolean placeBlock(@NotNull PlaceMetadata metadata);

    /**
     * 播放放置方块音效
     * @param metadata 方块放置行为元数据
     */
    void placeSound(@NotNull PlaceMetadata metadata, @NotNull Entity entity);

    interface Provider extends WithPriority {
        @Nullable BlockMaterial parse(@NotNull String str);
    }
}
