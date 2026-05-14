package top.mrxiaom.sweet.buildertools.data;

import de.tr7zw.changeme.nbtapi.iface.ReadWriteItemNBT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.Messages;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.BlockMaterial;
import top.mrxiaom.sweet.buildertools.api.ItemMaterial;

import java.util.*;
import java.util.function.Consumer;

public class ToolConfig {
    private final @NotNull SweetBuilderTools plugin;
    private final @NotNull String id;
    private final boolean enable;
    private final @Nullable String permission;
    private final @NotNull BlockMaterial placeDefault;
    private final boolean placeDisableDrops;
    private final @NotNull List<BlockMaterial> placeList;
    private final @NotNull Map<String, BlockMaterial> placeListByKey;
    private final boolean placeUseMaterialByBlock;
    private final @Nullable Integer amount;
    private final boolean recoverBySwapEnable;
    private final @NotNull List<ItemMaterial> recoverBySwapList;
    private final @NotNull ToolConfigItem item;
    private final @NotNull List<IAction> eventPlaced;
    private final @NotNull List<IAction> eventNoAmounts;
    private final @NotNull List<IAction> eventNoSelected;

    private ToolConfig(@NotNull SweetBuilderTools plugin, @NotNull String id, @NotNull ConfigurationSection config) {
        this.plugin = plugin;
        this.id = id;
        this.enable = config.getBoolean("enable", false);
        String permission = config.getString("permission", "none");
        if (permission.equals("none") || permission.trim().isEmpty()) {
            this.permission = null;
        } else {
            this.permission = permission;
        }
        BlockMaterial placeDefault = plugin.parseBlockMaterial(config.getString("place-blocks.default"));
        if (placeDefault == null) {
            throw new IllegalArgumentException("place-blocks.default 的值无效");
        }
        this.placeDefault = placeDefault;
        this.placeDisableDrops = config.getBoolean("place-blocks.disable-drops", true);
        this.placeList = new ArrayList<>();
        for (String str : config.getStringList("place-blocks.list")) {
            BlockMaterial material = plugin.parseBlockMaterial(str);
            if (material == null) {
                throw new IllegalArgumentException("place-blocks.list 的值 " + str + " 无效");
            }
            this.placeList.add(material);
        }
        this.placeListByKey = new HashMap<>();
        for (BlockMaterial material : placeList) {
            this.placeListByKey.put(material.key(), material);
        }
        this.placeUseMaterialByBlock = config.getBoolean("place-blocks.use-material-by-block");
        String amountStr = config.getString("amount");
        if ("infinite".equals(amountStr)) {
            this.amount = null;
        } else {
            int amount = Util.parseInt(amountStr).orElse(0);
            if (amount < 1) {
                throw new IllegalArgumentException("amount 的值无效");
            }
            this.amount = amount;
        }
        this.recoverBySwapEnable = config.getBoolean("recover-by-swap.enable");
        this.recoverBySwapList = new ArrayList<>();
        boolean recoverBySwapAddedAll = false;
        for (String str : config.getStringList("recover-by-swap.list")) {
            if (str.equals("*")) {
                if (recoverBySwapAddedAll) continue;
                recoverBySwapAddedAll = true;
                for (BlockMaterial material : placeList) {
                    this.recoverBySwapList.add(material.getItemMaterial());
                }
                continue;
            }
            ItemMaterial material = plugin.parseItemMaterial(str, true);
            if (material == null) {
                throw new IllegalArgumentException("recover-by-swap.list 的值 " + str + " 无效");
            }
            this.recoverBySwapList.add(material);
        }
        this.item = ToolConfigItem.load(plugin, config, "item");
        this.eventPlaced = ActionProviders.loadActions(config, "events.placed");
        this.eventNoAmounts = ActionProviders.loadActions(config, "events.no-amounts");
        this.eventNoSelected = ActionProviders.loadActions(config, "events.no-selected");
    }

    public @NotNull SweetBuilderTools plugin() {
        return plugin;
    }

    public @NotNull String id() {
        return id;
    }

    public boolean enable() {
        return enable;
    }

    public @Nullable String permission() {
        return permission;
    }

    public boolean hasPermission(Permissible p) {
        return permission == null || p.hasPermission(permission);
    }

    public @NotNull BlockMaterial placeDefault() {
        return placeDefault;
    }

    public boolean placeDisableDrops() {
        return placeDisableDrops;
    }

    public @NotNull List<BlockMaterial> placeList() {
        return placeList;
    }

    public @NotNull Map<String, BlockMaterial> placeListByKey() {
        return placeListByKey;
    }

    public boolean placeUseMaterialByBlock() {
        return placeUseMaterialByBlock;
    }

    public @Nullable Integer amount() {
        return amount;
    }

    public boolean recoverBySwapEnable() {
        return recoverBySwapEnable;
    }

    public @NotNull List<ItemMaterial> recoverBySwapList() {
        return recoverBySwapList;
    }

    @Contract("_,null->false")
    public boolean isMatchRecoverBySwapList(@NotNull Player player, @Nullable ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getAmount() == 0) return false;
        for (ItemMaterial material : recoverBySwapList()) {
            if (material.isItemMatch(player, item)) {
                return true;
            }
        }
        return false;
    }

    public @NotNull ToolConfigItem item() {
        return item;
    }

    public @NotNull List<IAction> eventPlaced() {
        return eventPlaced;
    }

    public @NotNull List<IAction> eventNoAmounts() {
        return eventNoAmounts;
    }

    public @NotNull List<IAction> eventNoSelected() {
        return eventNoSelected;
    }

    public void eventPlaced(Player player, List<Pair<String, Object>> r) {
        ActionProviders.run(plugin, player, eventPlaced(), r);
    }

    public void eventNoAmounts(Player player, List<Pair<String, Object>> r) {
        ActionProviders.run(plugin, player, eventNoAmounts(), r);
    }

    public void eventNoSelected(Player player, List<Pair<String, Object>> r) {
        ActionProviders.run(plugin, player, eventNoSelected(), r);
    }

    public void addReplacements(List<Pair<String, Object>> r, ItemStack item, Player player) {
        addReplacements(r, item, player, getAmount(item));
    }

    public void addReplacements(List<Pair<String, Object>> r, ItemStack item, Player player, int amount) {
        addAmountReplacements(r, amount);
        BlockMaterial material = getMaterial(item);
        if (material != null) {
            r.add(Pair.of("%material%", material.getDisplayName(player)));
        } else {
            r.add(Pair.of("%material%", Messages.Item.unknown_material.str()));
        }
    }

    public void addAmountReplacements(List<Pair<String, Object>> r, int amount) {
        r.add(Pair.of("%amount%", amount));
        if (this.amount == null) {
            String inf = Messages.Item.infinite.str();
            r.add(Pair.of("%current_amount%", inf));
            r.add(Pair.of("%max_amount%", inf));
        } else {
            r.add(Pair.of("%current_amount%", this.amount - amount));
            r.add(Pair.of("%max_amount%", this.amount));
        }
    }

    public ItemStack createItem(Player player, int amount) {
        ItemStack item;
        BlockMaterial material = placeDefault();
        if (placeUseMaterialByBlock()) {
            item = material.getItemMaterial().create(player, 1);
        } else {
            item = item().material().create(player, 1);
        }
        ListPair<String, Object> r = new ListPair<>();
        addAmountReplacements(r, amount);
        r.add("%material%", material.getDisplayName(player));

        ToolData data = ToolData.create()
                .id(id)
                .unique(UUID.randomUUID().toString())
                .player(player.getUniqueId().toString())
                .current(material.key())
                .amount(amount);
        item().applyItemMeta(item, player, r, r, data::saveTo);
        return item;
    }

    @Nullable
    public BlockMaterial getMaterial(@NotNull ItemStack item) {
        ToolData data = ToolData.readFrom(item);
        if (data.isValid()) {
            return placeListByKey().get(data.current());
        }
        return null;
    }

    public int getAmount(@NotNull ItemStack item) {
        return ToolData.readFrom(item).amount();
    }

    public void setAmount(@NotNull ItemStack item, Player player, int amount) {
        ToolData data = ToolData.readFrom(item);
        if (data.isValid()) {
            data.amount(amount);
            refreshItem(item, player, amount, data::saveTo);
        }
    }

    public void refreshItem(ItemStack item, Player player) {
        refreshItem(item, player, getAmount(item), null);
    }

    public void refreshItem(ItemStack item, Player player, int amount, Consumer<ReadWriteItemNBT> extraNBT) {
        ListPair<String, Object> r = new ListPair<>();
        addReplacements(r, item, player, amount);
        item().applyItemMeta(item, player, r, r, extraNBT);
    }

    @NotNull
    public static ToolConfig load(SweetBuilderTools plugin, String id, ConfigurationSection config) {
        return new ToolConfig(plugin, id, config);
    }
}
