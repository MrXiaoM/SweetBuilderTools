package top.mrxiaom.sweet.buildertools.gui;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.gui.IGuiHolder;
import top.mrxiaom.pluginbase.utils.AdventureItemStack;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.IMaterial;
import top.mrxiaom.sweet.buildertools.data.ToolConfig;
import top.mrxiaom.sweet.buildertools.func.AbstractGuiModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class GuiSelect extends AbstractGuiModule {
    public GuiSelect(SweetBuilderTools plugin) {
        super(plugin, plugin.resolve("./gui/select.yml"));
    }

    @Override
    protected String warningPrefix() {
        return "[gui/select]";
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        File guiFolder = plugin.resolve(config.getString("gui-folder", "./gui"));
        if (!guiFolder.exists()) {
            Util.mkdirs(guiFolder);
        }
        file = new File(guiFolder, "select.yml");
        if (!file.exists()) {
            plugin.saveResource("gui/select.yml", file);
        }
        super.reloadConfig(config);
    }

    @Override
    protected void reloadMenuConfig(YamlConfiguration config) {
        iconBlock = null;
        iconPrevPage = null;
        iconNextPage = null;
        selectedFlag.clear();
        notSelectedFlag.clear();
    }

    LoadedIcon iconBlock, iconPrevPage, iconNextPage;
    List<String> selectedFlag = new ArrayList<>();
    List<String> notSelectedFlag = new ArrayList<>();
    @Override
    protected void loadMainIcon(ConfigurationSection section, String id, LoadedIcon icon) {
        if (id.equals("方")) {
            iconBlock = icon;
            selectedFlag.addAll(section.getStringList(id + ".selected"));
            notSelectedFlag.addAll(section.getStringList(id + ".not-selected"));
        }
        if (id.equals("上")) {
            iconPrevPage = icon;
        }
        if (id.equals("下")) {
            iconNextPage = icon;
        }
    }

    @Override
    protected @Nullable ItemStack applyMainIcon(IGuiHolder instance, Player player, char id, int index, int appearTimes) {
        Impl gui = (Impl) instance;
        if (id == '方') {
            int i = ((gui.page - 1) * gui.pageSize) + appearTimes - 1;
            List<IMaterial> list = gui.tool.placeList();
            if (i >= list.size()) {
                return new ItemStack(Material.AIR);
            } else {
                IMaterial material = list.get(i);
                boolean isSelected = gui.selectedBlock.key().equals(material.key());
                ItemStack item = material.getIcon(player);
                IModifier<List<String>> loreModifier = oldLore -> {
                    List<String> lore = new ArrayList<>();
                    for (String line : oldLore) {
                        if (line.equals("old lore")) {
                            lore.addAll(AdventureItemStack.getItemLoreAsMiniMessage(item));
                            continue;
                        }
                        if (line.equals("selected flag")) {
                            lore.addAll(isSelected ? selectedFlag : notSelectedFlag);
                            continue;
                        }
                        lore.add(line);
                    }
                    return lore;
                };
                return iconBlock.generateIcon(item, player, null, loreModifier);
            }
        }
        if (id == '上') {
            return iconPrevPage.generateIcon(player);
        }
        if (id == '下') {
            return iconNextPage.generateIcon(player);
        }
        return null;
    }

    public static Impl create(Player player, ToolConfig tool, ItemStack item) {
        GuiSelect inst = inst();
        return inst.new Impl(player, tool, item);
    }

    public static GuiSelect inst() {
        return instanceOf(GuiSelect.class);
    }

    public class Impl extends Gui {
        private final ToolConfig tool;
        private final ItemStack item;
        private IMaterial selectedBlock;
        private int page = 1;
        private final int pageSize;
        private final int maxPages;
        protected Impl(Player player, ToolConfig tool, ItemStack item) {
            super(player, guiTitle, guiInventory);
            this.tool = tool;
            this.item = item;
            this.selectedBlock = tool.getMaterial(item);
            int pageSize = 0;
            for (char ch : guiInventory) {
                if (ch == '方') pageSize++;
            }
            this.pageSize = pageSize;
            this.maxPages = (int) Math.ceil((double)tool.placeList().size() / pageSize);
        }

        public ToolConfig tool() {
            return tool;
        }

        public ItemStack item() {
            return item;
        }

        @Override
        public void onClick(
                InventoryAction action, ClickType click,
                InventoryType.SlotType slotType, int slot,
                ItemStack currentItem, ItemStack cursor,
                InventoryView view, InventoryClickEvent event
        ) {
            event.setCancelled(true);
            Character clickedId = getClickedId(slot);
            if (clickedId == null) return;
            if (clickedId.equals('方')) {
                if (!click.isShiftClick() && click.isLeftClick()) {
                    int i = ((page - 1) * pageSize) + getAppearTimes(clickedId, slot) - 1;
                    List<IMaterial> list = tool.placeList();
                    if (i >= list.size()) return;
                    IMaterial material = list.get(i);
                    NBT.modify(item, nbt -> {
                        nbt.setString(ToolConfig.KEY_CURRENT, material.key());
                    });
                    tool.refreshItem(item, player);
                    selectedBlock = material;
                    plugin.getScheduler().runTask(this::open);
                }
                return;
            }
            if (clickedId.equals('上')) {
                if (!click.isShiftClick() && click.isLeftClick()) {
                    if (page > 1) {
                        page--;
                        plugin.getScheduler().runTask(this::open);
                    }
                }
                return;
            }
            if (clickedId.equals('下')) {
                if (!click.isShiftClick() && click.isLeftClick()) {
                    if (page < maxPages) {
                        page++;
                        plugin.getScheduler().runTask(this::open);
                    }
                }
                return;
            }
            handleOtherClick(click, clickedId);
        }
    }
}
