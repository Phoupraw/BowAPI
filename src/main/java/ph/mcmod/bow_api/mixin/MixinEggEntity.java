package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ph.mcmod.bow_api.Projectile;

@Mixin(EggEntity.class)
public abstract class MixinEggEntity extends MixinProjectileEntity {
@Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
private boolean followingDamage(Entity entity, DamageSource source, float amount, EntityHitResult entityHitResult) {
	return Projectile.followingDamage(this, afterHits, afterDamages, entity, source, amount, entityHitResult);
}
}
