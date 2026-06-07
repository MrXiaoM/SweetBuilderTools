package top.mrxiaom.sweet.buildertools.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import top.mrxiaom.sweet.buildertools.nms.IPlaceBlock;

public class PlaceBlock implements IPlaceBlock {
    private static EnumDirection convert(BlockFace blockFace) {
        switch (blockFace) {
            case DOWN:
                return EnumDirection.DOWN;
            case UP:
                return EnumDirection.UP;
            case NORTH:
                return EnumDirection.NORTH;
            case SOUTH:
                return EnumDirection.SOUTH;
            case WEST:
                return EnumDirection.WEST;
            case EAST:
                return EnumDirection.EAST;
        }
        return null;
    }
    @Override
    public boolean place(
            Player player, org.bukkit.inventory.ItemStack placeBlockItem,
            double interactionX, double interactionY, double interactionZ,
            BlockFace blockFace,
            int blockX, int blockY, int blockZ,
            boolean inside
    ) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        ItemStack item = CraftItemStack.asNMSCopy(placeBlockItem);
        Item type = item.getItem();
        if (type instanceof ItemBlock) {
            ItemBlock blockItem = (ItemBlock) type;
            EnumDirection direction = convert(blockFace);
            if (direction == null) return false;
            Vec3D location = new Vec3D(interactionX, interactionY, interactionZ);
            BlockPosition blockPos = new BlockPosition(blockX, blockY, blockZ);
            MovingObjectPositionBlock blockHitResult = new MovingObjectPositionBlock(location, direction, blockPos, inside);

            ItemActionContext useOnContext = new ItemActionContext(nmsPlayer.world, nmsPlayer, EnumHand.MAIN_HAND, item, blockHitResult);
            EnumInteractionResult result = blockItem.a(new BlockActionContext(useOnContext));
            return !result.equals(EnumInteractionResult.FAIL);
        }
        return false;
    }
}
