package top.mrxiaom.sweet.buildertools.nms.v1_15_R1;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import top.mrxiaom.sweet.buildertools.nms.IPlaceBlock;import java.lang.reflect.Constructor;

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
    private final Constructor<ItemActionContext> itemActionContext;
    public PlaceBlock() throws ReflectiveOperationException {
        itemActionContext = ItemActionContext.class.getDeclaredConstructor(World.class, EntityHuman.class, EnumHand.class, ItemStack.class, MovingObjectPositionBlock.class);
        itemActionContext.setAccessible(true);
    }

    public ItemActionContext newContext(World var0, EntityHuman var1, EnumHand var2, ItemStack var3, MovingObjectPositionBlock var4) {
        try {
            return itemActionContext.newInstance(var0, var1, var2, var3, var4);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
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

            ItemActionContext useOnContext = newContext(nmsPlayer.world, nmsPlayer, EnumHand.MAIN_HAND, item, blockHitResult);
            EnumInteractionResult result = blockItem.a(new BlockActionContext(useOnContext));
            return !result.equals(EnumInteractionResult.FAIL);
        }
        return false;
    }
}
