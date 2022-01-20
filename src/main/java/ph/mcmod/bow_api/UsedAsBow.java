package ph.mcmod.bow_api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * 自定义弓的实际拉弓进度。<br>
 * 原版{@link BowItem}实现了此接口。
 * 在{@link SimpleBowItem}中和骷髅射箭时会调用此接口下的方法计算出拉弓进度，其默认实现为调用原版计算拉弓进度的方法。
 */
public interface UsedAsBow {
	static double defaultPullProgress(int usingTicks) {
		double f = BowItem.getPullProgress(usingTicks);
		return f < 0.1 ? Double.NaN : f;
	}
/**
 * 计算拉弓进度
 *
 * @param world 世界
 * @param bowStack   弓
 * @param arrowStack 箭
 * @param user       拉弓的实体
 * @param usingTicks 已经拉了多久的弓，单位是游戏刻
 * @return 如果大于1，则箭会暴击；如果是{@link Double#NaN}，则视为拉弓失败，箭不会射出；可以大于1，会反映在箭的速度和伤害等属性
 */
default double calcPullProgress(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, int usingTicks) {
	return defaultPullProgress(usingTicks);
}
}
