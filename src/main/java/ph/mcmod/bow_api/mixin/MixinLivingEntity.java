package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ph.mcmod.bow_api.Main;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
@Shadow
public abstract boolean hasStatusEffect(StatusEffect effect);

@Shadow
public abstract @Nullable StatusEffectInstance getStatusEffect(StatusEffect effect);

private int timeUntilRegen0;

public MixinLivingEntity(EntityType<?> type, World world) {
	super(type, world);
}

@Inject(method = "damage", at = @At("HEAD"))
private void onDamageStart(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
	if (source.isProjectile()
	  || source.isMagic()
	  || source.isExplosive()
	  || source.isFallingBlock()
	  || source.isFromFalling()
	  || source.isNeutral()
	  || source.isUnblockable()) {
		timeUntilRegen0 = timeUntilRegen;
		timeUntilRegen = 10;
	} else {
		timeUntilRegen0 = -1;
	}
}

@Inject(method = "damage", at = @At("RETURN"))
private void onDamageEnd(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
	if (timeUntilRegen0 != -1) {
		timeUntilRegen = timeUntilRegen0;
	}
}

@Inject(method = "getJumpBoostVelocityModifier", at = @At("RETURN"))
private void onGetJumpBoostVelocityModifier(CallbackInfoReturnable<Double> cir) {
//	Main.print("entity=%s\nhasStatusEffect=%s\ngetStatusEffect=%s\ngetJumpBoostVelocityModifier=%s".formatted(
//	  this,
//	  hasStatusEffect(StatusEffects.JUMP_BOOST),
//	  getStatusEffect(StatusEffects.JUMP_BOOST),
//	  cir.getReturnValueD()));
}
}
