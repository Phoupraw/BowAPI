package ph.mcmod.bow_api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface RenderedAsBow {
/**
 * 计算拉弓进度，用于渲染弓的模型
 *
 * @param bow        弓
 * @param player     被渲染的玩家
 * @param usingTicks 到目前位置，已经拉了多久的弓
 * @return 拉弓进度
 */
default double calcPullProgress(@Nullable AbstractClientPlayerEntity player, @NotNull ItemStack bow, int usingTicks) {
	return BowItem.getPullProgress(usingTicks);
}
}
