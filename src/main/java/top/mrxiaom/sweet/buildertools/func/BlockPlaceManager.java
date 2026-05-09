package top.mrxiaom.sweet.buildertools.func;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.IMaterial;
import top.mrxiaom.sweet.buildertools.data.ToolConfig;
import top.mrxiaom.sweet.buildertools.event.FakeBlockPlaceEvent;

@AutoRegister
public class BlockPlaceManager extends AbstractModule implements Listener {
    public BlockPlaceManager(SweetBuilderTools plugin) {
        super(plugin);
        registerEvents();
    }

    private boolean isOffHand(PlayerInteractEvent e) {
        try {
            return EquipmentSlot.OFF_HAND.equals(e.getHand());
        } catch (LinkageError ignored) {
            return false;
        }
    }

    private boolean isUnderSpawnProtection(World world, Player player, Block block) {
        // net.minecraft.server.dedicated.DedicatedServer#isUnderSpawnProtection
        int spawnProtectionRadius = Bukkit.getServer().getSpawnRadius();
        if (player.isOp()) {
            return false;
        } else if (spawnProtectionRadius <= 0) {
            return false;
        } else {
            Location blockPos = world.getSpawnLocation();
            int abs = Math.abs(block.getX() - blockPos.getBlockX());
            int abs1 = Math.abs(block.getZ() - blockPos.getBlockZ());
            int max = Math.max(abs, abs1);
            return max <= spawnProtectionRadius;
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.useItemInHand() == Event.Result.DENY) return;
        ItemStack item = e.getItem();
        if (item == null || isOffHand(e)) return;
        ToolConfig tool = ToolsManager.inst().get(item);
        if (tool == null) return;
        Player player = e.getPlayer();
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (e.useInteractedBlock() == Event.Result.DENY) return;
            e.setCancelled(true);
            Block clickedBlock = e.getClickedBlock();
            if (clickedBlock == null) return;
            int currentAmount = ToolConfig.getAmount(item);
            Integer maxAmount = tool.amount();
            if (maxAmount != null && currentAmount >= maxAmount) {
                // TODO: 提示数量不足
                return;
            }
            IMaterial material = tool.getMaterial(item);
            if (material == null) {
                // TODO: 提示重新选择方块
                return;
            }

            Block block = clickedBlock.getRelative(e.getBlockFace());
            World world = block.getWorld();
            boolean canBuild = !isUnderSpawnProtection(world, player, block) && world.getWorldBorder().isInside(block.getLocation());

            // 通过 getState 备份方块快照，然后放置方块
            BlockState previousState = block.getState();
            if (!material.placeBlock(player, block)) {
                return;
            }

            BlockPlaceEvent event = new FakeBlockPlaceEvent(block, previousState, clickedBlock, item, player, canBuild, tool, material);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                // 如果其它插件阻止了方块放置，则恢复原方块
                previousState.update(true);
                return;
            }

            ToolConfig.setAmount(item, currentAmount + 1);

            BlockState state = block.getState();
            NBT.modifyPersistentData(state, nbt -> {
                nbt.setString(ToolConfig.BLOCK_ID, tool.id());
            });
            state.update();
        }
    }

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent e) {
        if (e.isCancelled()) return;
        ToolConfig tool = NBT.getPersistentData(e.getBlockState(), nbt -> {
            String id = nbt.getString(ToolConfig.BLOCK_ID);
            if (id.isEmpty()) {
                return null;
            } else {
                return ToolsManager.inst().get(id);
            }
        });
        if (tool != null && tool.placeDisableDrops()) {
            e.setCancelled(true);
        }
    }
}
