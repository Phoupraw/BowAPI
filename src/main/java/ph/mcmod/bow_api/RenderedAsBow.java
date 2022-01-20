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
 * 自定义弓在渲染时的拉弓进度。
 * 原版{@link BowItem}实现了此接口。
 * 实现此接口的物品，其模型会接收到"pull"和"pulling"两个物品谓词参数。关于物品谓词参数，具体请见<a href="https://wiki.biligame.com/mc/%E6%A8%A1%E5%9E%8B#.E7.89.A9.E5.93.81.E6.A0.87.E7.AD.BE.E8.B0.93.E8.AF.8D">...</a>。其中，"pull"参数由此接口下的方法计算得出，其默认实现为直接调用原版计算拉弓进度的方法。
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
