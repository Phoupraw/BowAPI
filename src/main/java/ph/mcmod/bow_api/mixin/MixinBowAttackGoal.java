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
import ph.mcmod.bow_api.CalcPullProgress;
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
	return actor.isHolding(stack -> stack.getItem() instanceof BowItem);
}

private ItemStack bowStack;
private ItemStack arrowStack;

@ModifyVariable(method = "tick", index = 6, at = @At(value = "STORE", ordinal = 0))
private int onI(int i) {
	bowStack = actor.getActiveItem();
	if (!(bowStack.getItem() instanceof CalcPullProgress bowItem))
		return i;
	arrowStack = actor.getArrowType(bowStack);
	return (int) (20 * bowItem.calcPullProgress(actor.world, actor, bowStack, arrowStack, actor.getItemUseTime()));
}

@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;getPullProgress(I)F"))
private float onGetPullProgress(int usingTicks) {
	if (!(bowStack.getItem() instanceof CalcPullProgress bowItem))
		return BowItem.getPullProgress(usingTicks);
	return (float) bowItem.calcPullProgress(actor.world, actor, bowStack, arrowStack, usingTicks);
}
}

