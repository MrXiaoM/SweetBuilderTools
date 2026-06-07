package top.mrxiaom.sweet.buildertools.nms.v1_20_R4;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweet.buildertools.nms.IPlaceBlock;

public class PlaceBlock implements IPlaceBlock {
    private static EnumDirection convert(BlockFace blockFace) {
        return switch (blockFace) {
            case DOWN -> EnumDirection.a;
            case UP -> EnumDirection.b;
            case NORTH -> EnumDirection.c;
            case SOUTH -> EnumDirection.d;
            case WEST -> EnumDirection.e;
            case EAST -> EnumDirection.f;
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
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(placeBlockItem);
        Item type = item.g();
        if (type instanceof ItemBlock blockItem) {
            EnumDirection direction = convert(blockFace);
            if (direction == null) return false;
            Vec3D location = new Vec3D(interactionX, interactionY, interactionZ);
            BlockPosition blockPos = new BlockPosition(blockX, blockY, blockZ);
            MovingObjectPositionBlock blockHitResult = new MovingObjectPositionBlock(location, direction, blockPos, inside);

            ItemActionContext useOnContext = new ItemActionContext(nmsPlayer.dP(), nmsPlayer, EnumHand.a, item, blockHitResult);
            EnumInteractionResult result = blockItem.a(new BlockActionContext(useOnContext));
            // EnumInteractionResult.d = InteractionResult.FAIL
            return !result.equals(EnumInteractionResult.d);
        }
        return false;
    }
}
