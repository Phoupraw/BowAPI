package ph.mcmod.bow_api.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ph.mcmod.bow_api.RenderedAsBow;

/**
 * 让实现了{@link RenderedAsBow}的物品也能像弓一样渲染模型
 */
@Mixin(HeldItemRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class MixinHeldItemRenderer {
/**
 * @see RenderedAsBow
 */
@Redirect(method = "getHandRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
private static boolean onIsOf(ItemStack itemStack, Item item) {
	return itemStack.isOf(item) || itemStack.getItem() instanceof RenderedAsBow;
}

/**
 * @see RenderedAsBow
 */
@Redirect(method = "getUsingItemHandRenderType", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 0))
private static boolean onIsOf2(ItemStack itemStack, Item item) {
	return itemStack.isOf(item) || itemStack.getItem() instanceof RenderedAsBow;
}

/**
 * 捕获的局部变量
 */
private AbstractClientPlayerEntity player;
/**
 * 捕获的局部变量
 */
private ItemStack bowItem;

/**
 * 捕获局部变量
 *
 * @see #player
 * @see #bowItem
 * @see #onV(float)
 */
@Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
private void onRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
	this.player = player;
	bowItem = item;
}

/**
 * @see #onRenderFirstPersonItem(AbstractClientPlayerEntity, float, float, Hand, float, ItemStack, float, MatrixStack, VertexConsumerProvider, int, CallbackInfo)
 * @see RenderedAsBow
 */
@ModifyVariable(method = "renderFirstPersonItem", index = 16, at = @At(value = "STORE", ordinal = 3))
private float onV(float v) {
	if (!(bowItem.getItem() instanceof RenderedAsBow renderedAsBow))
		return v;
	return (float) renderedAsBow.calcPullProgress( player,bowItem, bowItem.getMaxUseTime() - player.getItemUseTimeLeft());
}

}
