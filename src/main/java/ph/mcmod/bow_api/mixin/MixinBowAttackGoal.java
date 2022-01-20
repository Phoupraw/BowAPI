package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import ph.mcmod.bow_api.UsedAsBow;
import ph.mcmod.bow_api.SimpleBowItem;

@Mixin(BowAttackGoal.class)
public abstract class MixinBowAttackGoal<T extends HostileEntity & RangedAttackMob> extends Goal {
@Shadow
@Final
private T actor;

/**
 * @author Phoupraw
 * @reason 让骷髅手持 {@link SimpleBowItem}也能射箭。
 */
@Overwrite
public boolean isHoldingBow() {
	return actor.isHolding(stack -> stack.getItem() instanceof UsedAsBow);
}

private ItemStack tick_bowStack;
private ItemStack tick_arrowStack;

//@SuppressWarnings("InvalidInjectorMethodSignature")
//@ModifyVariable(method = "tick", index = 6, at = @At(value = "STORE", ordinal = 0))
//private int onI(int i) {
//	tick_bowStack = actor.getActiveItem();
//	if (!(tick_bowStack.getItem() instanceof UsedAsBow bowItem))
//		return i;
//	tick_arrowStack = actor.getArrowType(tick_bowStack);
//	return (int) (20 * bowItem.calcPullProgress(actor.world, actor, tick_bowStack, tick_arrowStack, actor.getItemUseTime()));
//}

@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/HostileEntity;getItemUseTime()I"))
private int onGetItemUseTime(HostileEntity actor) {
	tick_bowStack = actor.getActiveItem();
	int usingTicks = actor.getItemUseTime();
	if (!(tick_bowStack.getItem() instanceof UsedAsBow bowItem))
		return usingTicks;
	tick_arrowStack = actor.getArrowType(tick_bowStack);
	return (int) (20 * bowItem.calcPullProgress(actor.world, actor, tick_bowStack, tick_arrowStack, usingTicks));
}

@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;getPullProgress(I)F"))
private float onGetPullProgress(int usingTicks) {
	if (!(tick_bowStack.getItem() instanceof UsedAsBow bowItem))
		return BowItem.getPullProgress(usingTicks);
	return (float) bowItem.calcPullProgress(actor.world, actor, tick_bowStack, tick_arrowStack, usingTicks);
}
}

