package top.mrxiaom.sweet.buildertools.data;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.Messages;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.BlockMaterial;

import java.util.*;

public class ToolConfig {
    public static final String KEY_ID = "SWEET_BUILDER_TOOLS_ID";
    public static final String KEY_UNIQUE = "SWEET_BUILDER_TOOLS_UNIQUE";
    public static final String KEY_PLAYER = "SWEET_BUILDER_TOOLS_PLAYER";
    public static final String KEY_AMOUNT = "SWEET_BUILDER_TOOLS_AMOUNT";
    public static final String KEY_CURRENT = "SWEET_BUILDER_TOOLS_CURRENT";
    public static final String BLOCK_ID = "SBT_ID";
    private final @NotNull SweetBuilderTools plugin;
    private final @NotNull String id;
    private final boolean enable;
    private final @Nullable String permission;
    private final @NotNull BlockMaterial placeDefault;
    private final boolean placeDisableDrops;
    private final @NotNull List<BlockMaterial> placeList;
    private final @NotNull Map<String, BlockMaterial> placeListByKey;
    private final @Nullable Integer amount;
    private final @NotNull LoadedIcon item;
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
        this.item = LoadedIcon.load(config, "item");
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

    public @Nullable Integer amount() {
        return amount;
    }

    public @NotNull LoadedIcon item() {
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
            r.add(Pair.of("%current_amount%", Messages.Item.infinite.str()));
            r.add(Pair.of("%max_amount%", Messages.Item.infinite.str()));
        } else {
            r.add(Pair.of("%current_amount%", this.amount - amount));
            r.add(Pair.of("%max_amount%", this.amount));
        }
    }

    public ItemStack createItem(Player player, int amount) {
        ListPair<String, Object> r = new ListPair<>();
        addAmountReplacements(r, amount);
        r.add("%material%", placeDefault().getDisplayName(player));
        ItemStack item = item().generateIcon(player, s -> Pair.replace(s, r), l -> Pair.replace(l, r));
        NBT.modify(item, nbt -> {
            nbt.setString(KEY_ID, id());
            nbt.setString(KEY_UNIQUE, UUID.randomUUID().toString());
            nbt.setString(KEY_PLAYER, player.getUniqueId().toString());
            nbt.setString(KEY_CURRENT, placeDefault().key());
            nbt.setInteger(KEY_AMOUNT, amount);
        });
        return item;
    }

    @Nullable
    public BlockMaterial getMaterial(@NotNull ItemStack item) {
        return NBT.get(item, nbt -> {
            return placeListByKey().get(nbt.getString(KEY_CURRENT));
        });
    }

    public int getAmount(@NotNull ItemStack item) {
        return NBT.get(item, nbt -> {
            return nbt.getInteger(KEY_AMOUNT);
        });
    }

    public void setAmount(@NotNull ItemStack item, Player player, int amount) {
        NBT.modify(item, nbt -> {
            nbt.setInteger(KEY_AMOUNT, amount);
        });
        refreshItem(item, player, amount);
    }

    public void refreshItem(ItemStack item, Player player) {
        refreshItem(item, player, getAmount(item));
    }

    public void refreshItem(ItemStack item, Player player, int amount) {
        ListPair<String, Object> r = new ListPair<>();
        addReplacements(r, item, player, amount);
        item().applyItemMeta(item, player, s -> Pair.replace(s, r), l -> Pair.replace(l, r));
    }

    @NotNull
    public static ToolConfig load(SweetBuilderTools plugin, String id, ConfigurationSection config) {
        return new ToolConfig(plugin, id, config);
    }
}
