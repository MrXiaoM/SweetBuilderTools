package top.mrxiaom.sweet.buildertools.func;

import top.mrxiaom.sweet.buildertools.SweetBuilderTools;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<SweetBuilderTools> {
    public AbstractPluginHolder(SweetBuilderTools plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(SweetBuilderTools plugin, boolean register) {
        super(plugin, register);
    }
}
