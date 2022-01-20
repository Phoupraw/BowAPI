package ph.mcmod.bow_api.exp;

import org.jetbrains.annotations.NotNull;
import ph.mcmod.bow_api.BowSettings;
import ph.mcmod.bow_api.SimpleBowItem;

import java.util.ArrayList;
import java.util.List;

public class ModularBowItem extends SimpleBowItem {
private final List<BowModule> bowModules = new ArrayList<>();
public ModularBowItem(@NotNull BowSettings settings) {
	super(settings);
}
}
