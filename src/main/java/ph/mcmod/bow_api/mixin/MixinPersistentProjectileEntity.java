package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ph.mcmod.bow_api.*;
import ph.mcmod.bow_api.Serialization.SBiConsumer;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import static ph.mcmod.bow_api.Serialization.*;

@Mixin(PersistentProjectileEntity.class)
public abstract class MixinPersistentProjectileEntity extends MixinProjectileEntity implements ProgressPulled {
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
 * @see ListenableArrowEntity#followingDamage
 */
@Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
private boolean onDamage(Entity entity, DamageSource source, float amount, EntityHitResult entityHitResult) {
	onEntityHit_damageSource = source;
	onEntityHit_damage = amount;
	//	if ((Object) this instanceof ListenableArrowEntity a) {
//		boolean success = success0 && !(entity instanceof EndermanEntity);
//		for (var callback : a.followingDamage)
//			callback.accept(entityHitResult, source, amount, success);
//	}
	return entity.damage(source, amount);
}

private DamageSource onEntityHit_damageSource;
private float onEntityHit_damage;

/**
 *
 */
@Inject(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V", shift = At.Shift.AFTER))
private void endIf(EntityHitResult entityHitResult, CallbackInfo ci/*, Entity target, float velocity, int damage, Entity owner, DamageSource damageSource, boolean isEnderMan, int fireTicks*/) {
//	if ((Object) this instanceof ListenableArrowEntity a) {
//		for (var callback : a.afterHit)
//			callback.accept(a, entityHitResult);
//	}
	for (var callback : afterHits)
		callback.accept(this, entityHitResult);
	for (var callback : afterDamages)
		callback.afterDamage(this, entityHitResult, onEntityHit_damage, onEntityHit_damageSource);
}

//@Inject(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V", shift = At.Shift.AFTER), locals = LocalCapture.PRINT)
//private void endIf2(EntityHitResult entityHitResult, CallbackInfo ci) {}

@Inject(method = "onBlockHit", at = @At("RETURN"))
private void afterHitBlock(BlockHitResult blockHitResult, CallbackInfo ci) {
	for (var callback : afterHits)
		callback.accept(this, blockHitResult);
}

}
