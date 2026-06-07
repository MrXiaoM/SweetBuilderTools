package top.mrxiaom.sweet.buildertools.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceMetadata {
    private final @NotNull Player player;
    private final @NotNull ItemStack item;
    private final @NotNull Block block;
    private final @NotNull Block clickedBlock;
    private final @NotNull BlockState oldBlock;
    private final @NotNull BlockState oldClickedBlock;
    private final @NotNull BlockFace blockFace;
    private final @NotNull Location interactPoint;
    @ApiStatus.Internal
    public PlaceMetadata(@NotNull Player player, @NotNull ItemStack item, @NotNull Block block, @NotNull Block clickedBlock, @NotNull BlockFace blockFace, @NotNull Location interactPoint) {
        this.player = player;
        this.item = item;
        this.block = block;
        this.clickedBlock = clickedBlock;
        this.blockFace = blockFace;
        this.interactPoint = interactPoint;
        this.oldBlock = block.getState();
        this.oldClickedBlock = clickedBlock().getState();
    }

    public @NotNull Player player() {
        return player;
    }

    public @NotNull ItemStack item() {
        return item;
    }

    public @NotNull Block block() {
        return block;
    }

    public @NotNull Block clickedBlock() {
        return clickedBlock;
    }

    public @NotNull BlockState oldBlock() {
        return oldBlock;
    }

    public @NotNull BlockState oldClickedBlock() {
        return oldClickedBlock;
    }

    public @NotNull BlockFace blockFace() {
        return blockFace;
    }

    public @NotNull Location interactPoint() {
        return interactPoint;
    }
}
