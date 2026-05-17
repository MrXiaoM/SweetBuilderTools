package top.mrxiaom.sweet.buildertools.func;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.Messages;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.BlockMaterial;
import top.mrxiaom.sweet.buildertools.api.PlaceMetadata;
import top.mrxiaom.sweet.buildertools.data.EnumBlockState;
import top.mrxiaom.sweet.buildertools.data.ToolConfig;
import top.mrxiaom.sweet.buildertools.data.ToolData;
import top.mrxiaom.sweet.buildertools.event.FakeBlockPlaceEvent;
import top.mrxiaom.sweet.buildertools.gui.GuiSelect;

import java.util.HashSet;
import java.util.Set;

@AutoRegister
public class BlockPlaceManager extends AbstractModule implements Listener {
    private final boolean supportsBoundingBox = Util.isPresent("org.bukkit.util.BoundingBox");
    private boolean selectByInvRightClick;
    private boolean selectBySwapToOffhand;
    public BlockPlaceManager(SweetBuilderTools plugin) {
        super(plugin);
        registerEvents();
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        this.selectByInvRightClick = config.getBoolean("select-methods.inventory-right-click", true);
        this.selectBySwapToOffhand = config.getBoolean("select-methods.swap-to-offhand", true);
    }

    private boolean isOffHand(PlayerInteractEvent e) {
        try {
            return EquipmentSlot.OFF_HAND.equals(e.getHand());
        } catch (LinkageError ignored) {
            return false;
        }
    }

    @Nullable
    private Location getInteractionPoint(PlayerInteractEvent e) {
        try {
            return e.getInteractionPoint();
        } catch (LinkageError ignored) {
            return null;
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.useItemInHand() == Event.Result.DENY) return;
        if (e.useInteractedBlock() == Event.Result.DENY) return;
        ItemStack item = e.getItem();
        ToolData data = ToolData.readFrom(item);
        if (data.isValid()) {
            if (!e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                e.setCancelled(true);
            }
            ToolConfig tool = ToolsManager.inst().get(data.id());
            if (tool == null || isOffHand(e)) return;
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Location interactionPoint = getInteractionPoint(e);
                rightClick(e, e.getPlayer(), item, tool, data, interactionPoint);
            }
        }
    }

    private final Set<Material> REPLACEABLE = new HashSet<Material>() {{
        Material grass = Util.valueOrNull(Material.class, "SHORT_GRASS", "GRASS");
        if (grass != null) add(grass);
        add(Material.TALL_GRASS);
        add(Material.VINE);
    }};
    private boolean isReplaceable(Block block) {
        try {
            // Paper
            return block.isReplaceable();
        } catch (LinkageError ignored) {
            // Spigot 暂时先加几种方块，能用就行
            return REPLACEABLE.contains(block.getType());
        }
    }

    @Nullable
    private Block getPlaceBlock(Block clickedBlock, BlockFace clickedFace) {
        // 点击空气是不允许的
        if (clickedBlock.getType().equals(Material.AIR)) {
            return null;
        }
        // 如果点击可替换方块，则返回点击的方块为需要放置的位置
        if (isReplaceable(clickedBlock)) {
            return clickedBlock;
        }
        // 获取点击那面相对的方块位置，如果是可替换方块，则允许放置
        Block block = clickedBlock.getRelative(clickedFace);
        if (block.getType().equals(Material.AIR) || isReplaceable(block)) {
            return block;
        }
        return null;
    }

    private void rightClick(PlayerInteractEvent e, Player player, ItemStack item, ToolConfig tool, ToolData data, Location interactionPoint) {
        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) {
            if (plugin.debug()) {
                player.sendMessage("工具 " + tool.id() + " 交互事件 - 未点击方块");
            }
            return;
        }
        if (!player.isSneaking()) {
            // 非潜行状态下需要考虑点击的方块是否可以右键交互的问题
            BlockState state = clickedBlock.getState();
            if (EnumBlockState.hasInteractFuncWithBlock(state)) {
                if (plugin.debug()) {
                    player.sendMessage("工具 " + tool.id() + " 交互事件 - 方块 " + state.getClass().getName() + " 存在交互功能，需要按住 Shift 才能放置");
                }
                return;
            }
        }

        Block block = getPlaceBlock(clickedBlock, e.getBlockFace());
        if (block == null) {
            if (plugin.debug()) {
                player.sendMessage("工具 " + tool.id() + " 交互事件 - 试图放置方块到不可放置的位置");
            }
            return;
        }
        World world = block.getWorld();
        if (supportsBoundingBox) {
            // 1.14+ 检查碰撞箱是否重叠
            int blockX = block.getX();
            int blockY = block.getY();
            int blockZ = block.getZ();
            BoundingBox blockBox = new BoundingBox(blockX, blockY, blockZ, blockX + 1, blockY + 1, blockZ + 1);
            for (Entity entity : world.getEntities()) {
                if (entity.getBoundingBox().overlaps(blockBox)) {
                    if (plugin.debug()) {
                        player.sendMessage("工具 " + tool.id() + " 交互事件 - 试图放置方块到与实体重叠的位置");
                    }
                    return;
                }
            }
        } else {
            // 1.14 以下检查坐标
            for (Entity entity : world.getEntities()) {
                if (entity.getLocation().getBlock().equals(block)) {
                    if (plugin.debug()) {
                        player.sendMessage("工具 " + tool.id() + " 交互事件 - 试图放置方块到与实体重叠的位置");
                    }
                    return;
                }
            }
        }

        int currentAmount = tool.getAmount(item);
        Integer maxAmount = tool.amount();
        if (maxAmount != null && currentAmount >= maxAmount) {
            if (plugin.debug()) {
                player.sendMessage("工具 " + tool.id() + " 交互事件 - 数量不足");
            }
            // 提示数量不足
            ListPair<String, Object> r = new ListPair<>();
            tool.addReplacements(r, item, player, currentAmount);
            tool.eventNoAmounts(player, r);
            return;
        }
        BlockMaterial material = tool.getMaterial(item);
        if (material == null) {
            if (plugin.debug()) {
                player.sendMessage("工具 " + tool.id() + " 交互事件 - 未选择方块类型");
            }
            // 提示未选择方块类型
            ListPair<String, Object> r = new ListPair<>();
            tool.addReplacements(r, item, player, currentAmount);
            tool.eventNoSelected(player, r);
            return;
        }

        boolean canBuild = !isUnderSpawnProtection(world, player, block) && world.getWorldBorder().isInside(block.getLocation());

        PlaceMetadata metadata = new PlaceMetadata(player, block, interactionPoint);

        // 通过 getState 备份方块快照，然后放置方块
        BlockState previousState = block.getState();
        if (!material.placeBlock(metadata)) {
            if (plugin.debug()) {
                player.sendMessage("工具 " + tool.id() + " 交互事件 - 放置方块 " + material.key() + " 失败");
            }
            return;
        }

        BlockPlaceEvent event = new FakeBlockPlaceEvent(plugin, block, previousState, clickedBlock, item, player, canBuild, tool, data, material);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (plugin.debug()) {
                player.sendMessage("工具 " + tool.id() + " 交互事件 - 其它插件阻止了放置方块");
            }
            // 如果其它插件阻止了方块放置，则恢复原方块
            previousState.update(true);
            return;
        }

        material.placeSound(metadata, player);

        int amount = currentAmount + 1;
        tool.setAmount(item, player, amount);

        ListPair<String, Object> r = new ListPair<>();
        tool.addReplacements(r, item, player, amount);
        tool.eventPlaced(player, r);
    }

    private void openSelectGui(Cancellable e, Player player, ItemStack item) {
        ToolConfig tool = ToolsManager.inst().get(item);
        if (tool != null) {
            e.setCancelled(true);
            if (GuiManager.inst().getOpeningGui(player) != null) return;
            plugin.getScheduler().runTask(() -> GuiSelect.create(player, tool, item).open());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onOpenSelectGui(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getView().getType().equals(InventoryType.CRAFTING)) {
            Player player = (Player) e.getWhoClicked();
            if (selectByInvRightClick && e.getAction().equals(InventoryAction.PICKUP_HALF)) {
                openSelectGui(e, player, e.getCurrentItem());
            }
            if (e.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
                ItemStack item = e.getCurrentItem();
                ItemStack cursor = e.getCursor();
                ToolData data = ToolData.readFrom(item);
                if (data.isValid()) {
                    ToolConfig tool = ToolsManager.inst().get(data.id());
                    if (tool == null || !tool.recoverBySwapEnable()) return;
                    if (tool.isMatchRecoverBySwapList(player, cursor)) {
                        e.setCancelled(true);
                        int reduceAmount = cursor.getAmount();
                        int amount = data.amount();
                        if (amount == 0) {
                            Messages.Item.amount__no_need_to_recover.tm(player);
                        } else {
                            int recoverAmount;
                            if (reduceAmount > amount) {
                                recoverAmount = amount;
                                data.amount(0);
                                cursor.setAmount(reduceAmount - amount);
                                // noinspection deprecation
                                e.setCursor(cursor);
                            } else {
                                recoverAmount = reduceAmount;
                                data.amount(amount - reduceAmount);
                                cursor.setAmount(0);
                                cursor.setType(Material.AIR);
                                // noinspection deprecation
                                e.setCursor(null);
                            }
                            tool.refreshItem(item, player, data.amount(), data::saveTo);

                            ListPair<String, Object> r = new ListPair<>();
                            tool.addAmountReplacements(r, data.amount());
                            r.add("%recover_amount%", recoverAmount);
                            Messages.Item.amount__recover_success.tm(player, r);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onOpenSelectGui(PlayerSwapHandItemsEvent e) {
        if (!selectBySwapToOffhand || e.isCancelled()) return;
        Player player = e.getPlayer();
        openSelectGui(e, player, player.getInventory().getItemInMainHand());
    }
}
