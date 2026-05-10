package top.mrxiaom.sweet.buildertools.func;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.IMaterial;
import top.mrxiaom.sweet.buildertools.data.EnumBlockState;
import top.mrxiaom.sweet.buildertools.data.ToolConfig;
import top.mrxiaom.sweet.buildertools.event.FakeBlockPlaceEvent;
import top.mrxiaom.sweet.buildertools.gui.GuiSelect;

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
            if (!player.isSneaking()) {
                // 非潜行状态下需要考虑点击的方块是否可以右键交互的问题
                if (EnumBlockState.hasInteractFuncWithBlock(clickedBlock.getState())) {
                    return;
                }
            }
            int currentAmount = tool.getAmount(item);
            Integer maxAmount = tool.amount();
            if (maxAmount != null && currentAmount >= maxAmount) {
                // 提示数量不足
                ListPair<String, Object> r = new ListPair<>();
                tool.addReplacements(r, item, player, currentAmount);
                tool.eventNoAmounts(player, r);
                return;
            }
            IMaterial material = tool.getMaterial(item);
            if (material == null) {
                // 提示未选择方块类型
                ListPair<String, Object> r = new ListPair<>();
                tool.addReplacements(r, item, player, currentAmount);
                tool.eventNoSelected(player, r);
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

            BlockPlaceEvent event = new FakeBlockPlaceEvent(plugin, block, previousState, clickedBlock, item, player, canBuild, tool, material);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                // 如果其它插件阻止了方块放置，则恢复原方块
                previousState.update(true);
                return;
            }

            material.placeSound(block, player);

            int amount = currentAmount + 1;
            tool.setAmount(item, player, amount);

            ListPair<String, Object> r = new ListPair<>();
            tool.addReplacements(r, item, player, amount);
            tool.eventPlaced(player, r);
        }
    }

    @EventHandler
    public void onOpenSelectGui(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView().getType().equals(InventoryType.CRAFTING)) {
            Player player = (Player) e.getWhoClicked();
            if (e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                ItemStack item = e.getCurrentItem();
                ToolConfig tool = ToolsManager.inst().get(item);
                if (tool == null) return;
                e.setCancelled(true);
                if (GuiManager.inst().getOpeningGui(player) != null) return;
                plugin.getScheduler().runTask(() -> GuiSelect.create(player, tool, item).open());
            }
        }
    }
}
