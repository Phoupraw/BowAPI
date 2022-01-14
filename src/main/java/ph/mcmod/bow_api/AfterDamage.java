package ph.mcmod.bow_api;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ph.mcmod.bow_api.mixin.MixinPersistentProjectileEntity;

@FunctionalInterface
public interface AfterDamage {
/**
 * 在服务端和客户端都会调用。
 *
 * @see MixinPersistentProjectileEntity#endIf(EntityHitResult, CallbackInfo)
 */
void afterDamage(EntityHitResult entityHitResult, double damage, DamageSource damageSource);
}
