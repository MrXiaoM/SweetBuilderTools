package top.mrxiaom.sweet.buildertools.func;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTPersistentDataContainer;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.data.ToolConfig;
import top.mrxiaom.sweet.buildertools.data.ToolData;
import top.mrxiaom.sweet.buildertools.event.FakeBlockPlaceEvent;

import java.util.*;

/**
 * 这个类修改自 <a href="https://github.com/mfnalex/CustomBlockData/blob/master/src/main/java/com/jeff_media/customblockdata/BlockDataListener.java">mfnalex/CustomBlockData</a> 的方块监听器，并且将纯 <code>PDC</code> 储存数据改为 <code>item-nbt-api</code> 包装的 <code>PDC</code> 储存数据
 */
@AutoRegister
public class AntiDropManager extends AbstractModule implements Listener {
    public AntiDropManager(SweetBuilderTools plugin) {
        super(plugin);
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R3)) {
            registerEvents();
        } else {
            warn("你的服务端版本在 1.16.4 以下，不支持禁止挖掘方块后掉落物品功能");
        }
    }

    private NBTCompound getNBT(Block block) {
        // 提取并简化 NBTBlock#getData() 的操作
        return new NBTPersistentDataContainer(block.getChunk().getPersistentDataContainer())
                .getOrCreateCompound("blocks")
                .getOrCreateCompound(block.getX() + "_" + block.getY() + "_" + block.getZ());
    }

    private void removeBlockStateListFlag(List<BlockState> blockStates) {
        for (BlockState blockState : blockStates) {
            removeFlag(blockState.getBlock());
        }
    }

    private void removeBlockListFlag(List<Block> blocks) {
        for (Block block : blocks) {
            removeFlag(block);
        }
    }

    private void removeFlag(Block block) {
        NBTCompound nbt = getNBT(block);
        if (nbt.hasTag(ToolData.BLOCK_ID)) {
            if (plugin.debug()) {
                info(String.format("移除了 %s 方块 (%s, %d, %d, %d)",
                        nbt.getString(ToolData.BLOCK_ID),
                        block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
            }
            nbt.removeKey(ToolData.BLOCK_ID);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (event instanceof FakeBlockPlaceEvent) {
            // 放置方块时添加标签
            String toolId = ((FakeBlockPlaceEvent) event).tool().id();
            NBTCompound nbt = getNBT(event.getBlock());
            nbt.setString(ToolData.BLOCK_ID, toolId);
            if (plugin.debug()) {
                info(String.format("为方块 (%s, %d, %d, %d) 添加标签 %s",
                        event.getBlock().getWorld().getName(),
                        event.getBlock().getX(),
                        event.getBlock().getY(),
                        event.getBlock().getZ(),
                        toolId));
            }
        } else {
            removeFlag(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        // 挖掘方块时检测到标签就禁止掉落
        Block block = event.getBlock();
        NBTCompound nbt = getNBT(block);
        if (nbt.hasTag(ToolData.BLOCK_ID)) {
            String toolId = nbt.getString(ToolData.BLOCK_ID);
            nbt.removeKey(ToolData.BLOCK_ID);
            ToolConfig tool = ToolsManager.inst().get(toolId);
            if (tool != null && tool.placeDisableDrops()) {
                if (plugin.debug()) {
                    info(String.format("阻止了 %s 方块 (%s, %d, %d, %d) 掉落物品",
                            toolId,
                            block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
                }
                event.setDropItems(false);
                event.setExpToDrop(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntity(EntityChangeBlockEvent event) {
        if (event.isCancelled()) return;
        if (event.getTo() != event.getBlock().getType()) {
            removeFlag(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExplode(BlockExplodeEvent event) {
        if (event.isCancelled()) return;
        removeBlockListFlag(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) return;
        removeBlockListFlag(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBurn(BlockBurnEvent event) {
        if (event.isCancelled()) return;
        removeFlag(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPiston(BlockPistonExtendEvent event) {
        if (event.isCancelled()) return;
        onPiston(event.getBlocks(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPiston(BlockPistonRetractEvent event) {
        if (event.isCancelled()) return;
        onPiston(event.getBlocks(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFade(BlockFadeEvent event) {
        if (event.isCancelled()) return;
        if (event.getBlock().getType() == Material.FIRE) return;
        if (event.getNewState().getType() != event.getBlock().getType()) {
            removeFlag(event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStructure(StructureGrowEvent event) {
        if (event.isCancelled()) return;
        removeBlockStateListFlag(event.getBlocks());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFertilize(BlockFertilizeEvent event) {
        if (event.isCancelled()) return;
        removeBlockStateListFlag(event.getBlocks());
    }

    private void onPiston(List<Block> blocks, BlockPistonEvent bukkitEvent) {
        BlockFace direction = bukkitEvent.getDirection();
        List<Block> list = new ArrayList<>(blocks);
        Collections.reverse(list);
        for (Block block : list) {
            PistonMoveReaction reaction = block.getPistonMoveReaction();
            if (reaction == PistonMoveReaction.BREAK) {
                removeFlag(block);
                continue;
            }
            Block destinationBlock = block.getRelative(direction);
            NBTCompound current = getNBT(block);
            if (current.hasTag(ToolData.BLOCK_ID)) {
                NBTCompound destination = getNBT(destinationBlock);
                String blockId = current.getString(ToolData.BLOCK_ID);
                destination.setString(ToolData.BLOCK_ID, blockId);
                current.removeKey(ToolData.BLOCK_ID);
                if (plugin.debug()) {
                    info(String.format("活塞将 %s 方块 (%s, %d, %d, %d) 移到了 (%s, %d, %d, %d)",
                            blockId,
                            block.getWorld().getName(), block.getX(), block.getY(), block.getZ(),
                            destinationBlock.getWorld().getName(), destinationBlock.getX(), destinationBlock.getY(), destinationBlock.getZ()));
                }
            }
        }
    }

    public static AntiDropManager inst() {
        return instanceOf(AntiDropManager.class);
    }
}
