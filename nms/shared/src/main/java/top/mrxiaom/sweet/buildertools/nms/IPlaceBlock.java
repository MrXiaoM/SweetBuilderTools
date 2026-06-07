package top.mrxiaom.sweet.buildertools.nms;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IPlaceBlock {
    boolean place(
            Player player, ItemStack placeBlockItem,
            double interactionX, double interactionY, double interactionZ,
            BlockFace blockFace,
            int blockX, int blockY, int blockZ,
            boolean inside
    );
}
