package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ph.mcmod.bow_api.BAfterDamage;
import ph.mcmod.bow_api.HPullProgress;

@Mixin(PersistentProjectileEntity.class)
public abstract class MixinPersistentProjectileEntity implements HPullProgress {
private final PersistentProjectileEntity _this = (PersistentProjectileEntity) (Object) this;
private double pullProgress;

@Override
public void setPullProgress(double pullProgress) {
	this.pullProgress = pullProgress;
}

@Override
public double getPullProgress() {
	return pullProgress;
}

/**
 * @param entityHitResult {@link PersistentProjectileEntity#onEntityHit(EntityHitResult)}的参数
 */
@Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
private boolean onDamage(Entity entity, DamageSource source, float amount, EntityHitResult entityHitResult) {
	onEntityHit_damageSource = source;
	onEntityHit_damage = amount;
	return entity.damage(source, amount);
}

private DamageSource onEntityHit_damageSource;
private float onEntityHit_damage;

/**
 *
 */
@Inject(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V", shift = At.Shift.AFTER))
private void endDamageIf(EntityHitResult entityHitResult, CallbackInfo ci/*, Entity target, float velocity, int damage, Entity owner, DamageSource damageSource, boolean isEnderMan, int fireTicks*/) {
	if (_this instanceof BAfterDamage a) {
		a.afterDamage(entityHitResult,onEntityHit_damage,onEntityHit_damageSource);
	}
}

}
