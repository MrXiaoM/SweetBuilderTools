package top.mrxiaom.sweet.buildertools;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.api.IRegistry;
import top.mrxiaom.pluginbase.data.SimpleRegistry;
import top.mrxiaom.pluginbase.func.LanguageManager;
import top.mrxiaom.pluginbase.paper.PaperFactory;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweet.buildertools.api.BlockMaterial;
import top.mrxiaom.sweet.buildertools.api.ItemMaterial;
import top.mrxiaom.sweet.buildertools.material.VanillaItemMaterial;

public class SweetBuilderTools extends BukkitPlugin {
    public static SweetBuilderTools getInstance() {
        return (SweetBuilderTools) BukkitPlugin.getInstance();
    }
    public SweetBuilderTools() throws Exception {
        super(options()
                .bungee(false)
                .adventure(true)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.buildertools.libs")
        );
        this.scheduler = new FoliaLibScheduler(this);

        try {
            //noinspection ResultOfMethodCallIgnored, deprecation
            getDescription().getLibraries();
        } catch (LinkageError ignored) {
            info("正在检查依赖库状态");
            File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                    ? new File("libraries")
                    : new File(this.getDataFolder(), "libraries");
            DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

            YamlConfiguration overrideLibraries = ConfigUtils.load(resolve("./.override-libraries.yml"));
            for (String key : overrideLibraries.getKeys(false)) {
                resolver.getStartsReplacer().put(key, overrideLibraries.getString(key));
            }
            resolver.addResolvedLibrary(BuildConstants.RESOLVED_LIBRARIES);

            List<URL> libraries = resolver.doResolve();
            info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
            for (URL library : libraries) {
                this.classLoader.addURL(library);
            }
        }
    }

    private boolean debug = false;
    public boolean debug() {
        return debug;
    }

    @Override
    public @NotNull ItemEditor initItemEditor() {
        return PaperFactory.createItemEditor();
    }

    @Override
    public @NotNull InventoryFactory initInventoryFactory() {
        return PaperFactory.createInventoryFactory();
    }

    @Override
    protected void beforeLoad() {
        MinecraftVersion.replaceLogger(getLogger());
        MinecraftVersion.disableUpdateCheck();
        MinecraftVersion.disableBStats();
        MinecraftVersion.getVersion();
    }

    private final IRegistry<BlockMaterial.Provider> blockMaterialRegistry = new SimpleRegistry<>();
    private final IRegistry<ItemMaterial.Provider> itemMaterialRegistry = new SimpleRegistry<>();

    public IRegistry<BlockMaterial.Provider> blockMaterialRegistry() {
        return blockMaterialRegistry;
    }

    public IRegistry<ItemMaterial.Provider> itemMaterialRegistry() {
        return itemMaterialRegistry;
    }

    @Nullable
    public BlockMaterial parseBlockMaterial(@Nullable String str) {
        if (str == null) {
            return null;
        }
        for (BlockMaterial.Provider provider : blockMaterialRegistry.all()) {
            BlockMaterial material = provider.parse(str);
            if (material != null) {
                return material;
            }
        }
        return null;
    }

    @Contract("_,false->!null")
    public ItemMaterial parseItemMaterial(@Nullable String str, boolean nullable) {
        if (str != null) for (ItemMaterial.Provider provider : itemMaterialRegistry.all()) {
            ItemMaterial material = provider.parse(str);
            if (material != null) {
                return material;
            }
        }
        return nullable ? null : VanillaItemMaterial.DEFAULT;
    }

    @Override
    protected void beforeReloadConfig(FileConfiguration config) {
        debug = config.getBoolean("debug", false);
    }

    @Override
    protected void beforeEnable() {
        LanguageManager.inst()
                .setLangFile("messages.yml")
                .register(Messages.class)
                .register(Messages.Item.class)
                .register(Messages.Command.class);
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetBuilderTools 加载完毕");
    }
}
