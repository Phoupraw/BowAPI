package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
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
}
