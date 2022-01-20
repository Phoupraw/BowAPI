package ph.mcmod.bow_api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import ph.mcmod.bow_api.mixin.AccessModelPredicateProviderRegistry;

@Environment(EnvType.CLIENT)
public final class ClientMain {
@SuppressWarnings("InstantiationOfUtilityClass")
public static final AccessModelPredicateProviderRegistry MODEL_PREDICATE_PROVIDER_REGISTRY = (AccessModelPredicateProviderRegistry) new ModelPredicateProviderRegistry();
public static final UnclampedModelPredicateProvider PULLING = (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1 : 0;
public static final UnclampedModelPredicateProvider PULL = (stack, world, entity, seed) -> {
	if (entity == null)
		return 0;
	if (entity.getActiveItem() != stack)
		return 0;
	int usingTicks = stack.getMaxUseTime() - entity.getItemUseTimeLeft();
	return stack.getItem() instanceof RenderedAsBow customBow ? (float) customBow.calcPull(entity instanceof AbstractClientPlayerEntity player ? player : null, stack, usingTicks) : 0;
};

public static void init() {
	MODEL_PREDICATE_PROVIDER_REGISTRY.invokeRegister(Items.BOW, new Identifier("pulling"), PULLING);
	MODEL_PREDICATE_PROVIDER_REGISTRY.invokeRegister(Items.BOW, new Identifier("pull"), PULL);
}
}
