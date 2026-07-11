package top.mrxiaom.sweet.timer.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ActionBundle {
    public static final ActionBundle EMPTY = new ActionBundle();
    public final @NotNull List<IAction> runActions;
    public final @NotNull List<List<IAction>> randomActions;

    private ActionBundle() {
        this.runActions = new ArrayList<>();
        this.randomActions = new ArrayList<>();
    }

    public ActionBundle(ConfigurationSection config) {
        this.runActions = ActionProviders.loadActions(config, "run-actions");
        this.randomActions = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("random-actions");
        if (section != null) for (String key : section.getKeys(false)) {
            randomActions.add(ActionProviders.loadActions(section, key));
        }
    }

    @NotNull
    public List<IAction> baseActions() {
        return runActions;
    }

    @Nullable
    public List<IAction> randomActions() {
        if (randomActions.isEmpty()) return null;
        if (randomActions.size() == 1) return randomActions.get(0);
        return randomActions.get(new Random().nextInt(randomActions.size()));
    }

    public static ActionBundle load(ConfigurationSection config) {
        if (config == null) {
            return EMPTY;
        }
        return new ActionBundle(config);
    }
}
