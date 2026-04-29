package top.mrxiaom.sweet.buildertools.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;

public interface IMaterial {
    @NotNull String key();
    @NotNull ItemStack getIcon(@NotNull Player player);
    void placeBlock(@NotNull Player player, @NotNull Block block);
    interface Provider extends WithPriority {
        @Nullable IMaterial parse(@NotNull String str);
    }
}
