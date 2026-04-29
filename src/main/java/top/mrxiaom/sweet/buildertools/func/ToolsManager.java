package top.mrxiaom.sweet.buildertools.func;

import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.data.ToolConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoRegister
public class ToolsManager extends AbstractModule {
    private final Map<String, ToolConfig> tools = new HashMap<>();
    public ToolsManager(SweetBuilderTools plugin) {
        super(plugin);
    }

    @Override
    public void reloadConfig(MemoryConfiguration pluginConfig) {
        File folder = plugin.resolve(pluginConfig.getString("tools-folder", "./tools"));
        if (!folder.exists()) {
            plugin.saveResource("tools/example.yml", new File(folder, "example.yml"));
        }
        tools.clear();
        Util.reloadFolder(folder, false, (rawId, file) -> {
            String id = rawId.replace('\\', '/');
            YamlConfiguration config = ConfigUtils.load(file);
            try {
                tools.put(id, ToolConfig.load(plugin, id, config));
            } catch (Throwable t) {
                warn("加载工具配置 " + id + " 时出现错误: " + t.getMessage());
            }
        });
        info("加载了 " + tools.size() + " 个工具配置");
    }

    @NotNull
    public List<String> keys() {
        return keys(null);
    }

    @NotNull
    public List<String> keys(@Nullable Permissible p) {
        if (p == null) {
            return Lists.newArrayList(tools.keySet());
        }
        List<String> list = new ArrayList<>();
        for (ToolConfig config : tools.values()) {
            if (config.hasPermission(p)) {
                list.add(config.id());
            }
        }
        return list;
    }

    @Nullable
    public ToolConfig get(String id) {
        return tools.get(id);
    }

    @Nullable
    public ToolConfig get(@Nullable ItemStack item) {
        if (item == null || item.getAmount() < 1 || item.getType().equals(Material.AIR)) {
            return null;
        }
        return NBT.get(item, nbt -> {
            return get(nbt.getString(ToolConfig.KEY_ID));
        });
    }

    public static ToolsManager inst() {
        return instanceOf(ToolsManager.class);
    }
}
