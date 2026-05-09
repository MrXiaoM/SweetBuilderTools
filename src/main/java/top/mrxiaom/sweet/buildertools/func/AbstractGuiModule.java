package top.mrxiaom.sweet.buildertools.func;

import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;

import java.io.File;

public abstract class AbstractGuiModule extends top.mrxiaom.pluginbase.func.AbstractGuiModule<SweetBuilderTools> {
    public AbstractGuiModule(SweetBuilderTools plugin, File file) {
        super(plugin, file);
    }

    public AbstractGuiModule(SweetBuilderTools plugin, File file, @Nullable String mainIconsKey, @Nullable String otherIconsKey) {
        super(plugin, file, mainIconsKey, otherIconsKey);
    }
}
