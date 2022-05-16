package ph.mcmod.bow_api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ClientMain {

public static void init() {
	FabricModelPredicateProviderRegistry.register(Items.BOW, new Identifier("pulling"), ClientMain::calcPulling);
	FabricModelPredicateProviderRegistry.register(Items.BOW, new Identifier("pull"), ClientMain::calcPull);
}

public static float calcPulling(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity living, int seed) {
	return living != null && living.isUsingItem() && living.getActiveItem() == stack ? 1 : 0;
}

public static float calcPull(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity living, int seed) {
	if (living == null)
		return 0;
	if (living.getActiveItem() != stack)
		return 0;
	int usingTicks = stack.getMaxUseTime() - living.getItemUseTimeLeft();
	return stack.getItem() instanceof RenderedAsBow customBow ? (float) customBow.calcPull(living instanceof AbstractClientPlayerEntity player ? player : null, stack, usingTicks) : 0;
}
}
