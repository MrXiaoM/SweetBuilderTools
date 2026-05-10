package top.mrxiaom.sweet.buildertools.event;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.BlockMaterial;
import top.mrxiaom.sweet.buildertools.data.ToolConfig;

public class FakeBlockPlaceEvent extends BlockPlaceEvent {
    private final SweetBuilderTools plugin;
    private final ToolConfig tool;
    private final BlockMaterial material;
    public FakeBlockPlaceEvent(SweetBuilderTools plugin, Block placedBlock, BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player thePlayer, boolean canBuild, ToolConfig tool, BlockMaterial material) {
        // noinspection deprecation
        super(placedBlock, replacedBlockState, placedAgainst, itemInHand, thePlayer, canBuild);
        this.plugin = plugin;
        this.tool = tool;
        this.material = material;
    }

    public ToolConfig tool() {
        return tool;
    }

    public BlockMaterial material() {
        return material;
    }

    @Override
    public void setCancelled(boolean cancel) {
        super.setCancelled(cancel);
        if (cancel && plugin.debug()) {
            StackTraceElement[] elements = new Throwable().getStackTrace();
            StackTraceElement element = elements[Math.min(1, elements.length - 1)];
            Player player = getPlayer();
            Block block = getBlock();
            plugin.warn(String.format("玩家 %s 在使用 %s 放置方块 (%s, %d, %d, %d) 时被其它插件阻止: \n  at %s",
                    player.getName(), tool.id(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(),
                    element.toString()));
        }
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return BlockPlaceEvent.getHandlerList();
    }
}
