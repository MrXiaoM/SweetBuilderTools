package top.mrxiaom.sweet.buildertools.nms.mojmap_1_21_9;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweet.buildertools.nms.IPlaceBlock;

public class PlaceBlock implements IPlaceBlock {
    public PlaceBlock() {
        //noinspection ResultOfMethodCallIgnored
        convert(BlockFace.UP);
        CraftItemStack.asNMSCopy(new ItemStack(Material.STONE));
    }
    private static Direction convert(BlockFace blockFace) {
        return switch (blockFace) {
            case DOWN -> Direction.DOWN;
            case UP -> Direction.UP;
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case EAST -> Direction.EAST;
            default -> null;
        };
    }
    @Override
    public boolean place(
            Player player, ItemStack placeBlockItem,
            double interactionX, double interactionY, double interactionZ,
            BlockFace blockFace,
            int blockX, int blockY, int blockZ,
            boolean inside
    ) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(placeBlockItem);
        Item type = item.getItem();
        if (type instanceof BlockItem blockItem) {
            Direction direction = convert(blockFace);
            if (direction == null) return false;
            Vec3 location = new Vec3(interactionX, interactionY, interactionZ);
            BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);
            BlockHitResult blockHitResult = new BlockHitResult(location, direction, blockPos, inside);

            UseOnContext useOnContext = new UseOnContext(nmsPlayer.level(), nmsPlayer, InteractionHand.MAIN_HAND, item, blockHitResult);
            InteractionResult result = blockItem.place(new BlockPlaceContext(useOnContext));
            return !result.equals(InteractionResult.FAIL);
        }
        return false;
    }
}
