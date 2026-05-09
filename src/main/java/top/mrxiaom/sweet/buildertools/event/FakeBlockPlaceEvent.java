package top.mrxiaom.sweet.buildertools.event;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweet.buildertools.api.IMaterial;
import top.mrxiaom.sweet.buildertools.data.ToolConfig;

public class FakeBlockPlaceEvent extends BlockPlaceEvent {
    private final ToolConfig tool;
    private final IMaterial material;
    public FakeBlockPlaceEvent(Block placedBlock, BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player thePlayer, boolean canBuild, ToolConfig tool, IMaterial material) {
        // noinspection deprecation
        super(placedBlock, replacedBlockState, placedAgainst, itemInHand, thePlayer, canBuild);
        this.tool = tool;
        this.material = material;
    }

    public ToolConfig tool() {
        return tool;
    }

    public IMaterial material() {
        return material;
    }
}
