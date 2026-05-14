package top.mrxiaom.sweet.buildertools.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;

public interface ItemMaterial extends WithPriority {

    @NotNull String key();

    /**
     * 创建一个新物品
     * @param player 请求的玩家
     * @param amount 数量
     */
    @NotNull ItemStack create(@NotNull Player player, int amount);

    /**
     * 指定物品是否匹配这个方块
     * @param player 请求的玩家
     * @param item 物品
     */
    boolean isItemMatch(@NotNull Player player, @NotNull ItemStack item);

    interface Provider extends WithPriority {
        @Nullable ItemMaterial parse(@NotNull String str);
    }
}
