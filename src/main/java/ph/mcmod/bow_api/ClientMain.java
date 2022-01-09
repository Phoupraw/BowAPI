package ph.mcmod.bow_api;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.ArrowEntityRenderer;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import ph.mcmod.bow_api.mixin.ModelPredicateProviderRegistryAccessor;

@Environment(EnvType.CLIENT)
public final class ClientMain {
public static void init() {
	ModelPredicateProviderRegistryAccessor.invokeRegister(Items.BOW, new Identifier("pulling"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1 : 0);
	ModelPredicateProviderRegistryAccessor.invokeRegister(Items.BOW, new Identifier("pull"), (stack, world, entity, seed) -> {
		if (entity == null)
			return 0;
		if (entity.getActiveItem() != stack)
			return 0;
		int usingTicks = stack.getMaxUseTime() - entity.getItemUseTimeLeft();
		return stack.getItem() instanceof RenderedAsBow customBow ? (float) customBow.calcPullProgress(entity instanceof AbstractClientPlayerEntity player ? player : null, stack, usingTicks) : 0;
	});
//	EntityRendererRegistry.INSTANCE.register(SpawnerArrowEntity.ENTITY_TYPE, ArrowEntityRenderer::new);
}

}
