package top.mrxiaom.sweet.buildertools.event;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class FakeBlockPlaceEvent extends BlockPlaceEvent {
    public FakeBlockPlaceEvent(Block placedBlock, BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player thePlayer, boolean canBuild) {
        // noinspection deprecation
        super(placedBlock, replacedBlockState, placedAgainst, itemInHand, thePlayer, canBuild);
    }
}
