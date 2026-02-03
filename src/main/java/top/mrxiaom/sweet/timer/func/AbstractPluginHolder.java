package top.mrxiaom.sweet.timer.func;

import top.mrxiaom.sweet.timer.SweetTimer;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<SweetTimer> {
    public AbstractPluginHolder(SweetTimer plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(SweetTimer plugin, boolean register) {
        super(plugin, register);
    }
}
