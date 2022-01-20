package ph.mcmod.bow_api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ph.mcmod.bow_api.mixin.MixinAbstractClientPlayerEntity;
import ph.mcmod.bow_api.mixin.MixinHeldItemRenderer;
import ph.mcmod.bow_api.mixin.MixinModelPredicateProviderRegistry;
import ph.mcmod.bow_api.mixin.MixinSkeletonEntityModel;

/**
 * 如果想让物品像弓一样渲染，请实现此接口。
 * 原版{@link BowItem}实现了此接口。
 *
 * @see MixinAbstractClientPlayerEntity
 * @see MixinHeldItemRenderer
 * @see MixinModelPredicateProviderRegistry
 * @see MixinSkeletonEntityModel
 */
@Environment(EnvType.CLIENT)
public interface RenderedAsBow {
/**
 * 计算拉弓进度，用于渲染弓的模型
 *
 * @param bowStack   弓
 * @param entity     被渲染的实体
 * @param usingTicks 已经拉了多久的弓，单位为游戏刻
 * @return 拉弓进度
 */
default double calcPull(@Nullable Entity entity, @NotNull ItemStack bowStack, int usingTicks) {
	return BowItem.getPullProgress(usingTicks);
}
}
