package top.mrxiaom.sweet.buildertools.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.WithPriority;

public interface ItemMaterial extends WithPriority {

    @NotNull ItemStack create(@NotNull Player player, int amount);

    interface Provider extends WithPriority {
        @Nullable ItemMaterial parse(@NotNull String str);
    }
}
