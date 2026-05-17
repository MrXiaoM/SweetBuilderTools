package top.mrxiaom.sweet.buildertools.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceMetadata {
    private final @NotNull Player player;
    private final @NotNull Block block;
    private final @Nullable Location interactPoint;
    @ApiStatus.Internal
    public PlaceMetadata(@NotNull Player player, @NotNull Block block, @Nullable Location interactPoint) {
        this.player = player;
        this.block = block;
        this.interactPoint = interactPoint;
    }

    public @NotNull Player player() {
        return player;
    }

    public @NotNull Block block() {
        return block;
    }

    public @Nullable Location interactPoint() {
        return interactPoint;
    }
}
