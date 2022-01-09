package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileUtil.class)
public abstract class MixinProjectileUtil {
/**
 * 让骷髅能够使用任何{@link BowItem}
 */
@Inject(method = "getHandPossiblyHolding", at = @At("HEAD"), cancellable = true)
private static void on(LivingEntity entity, Item item, CallbackInfoReturnable<Hand> cir) {
	if (item == Items.BOW && entity instanceof AbstractSkeletonEntity)
		cir.setReturnValue(entity.getMainHandStack().getItem() instanceof BowItem ? Hand.MAIN_HAND : Hand.OFF_HAND);
}


}
