package ph.mcmod.bow_api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;
import ph.mcmod.bow_api.Serialization.SBiConsumer;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

public interface Projectile extends Owned, Movable {
static boolean followingDamage(Projectile projectile, List<SBiConsumer<Projectile, HitResult>> afterHits, List<AfterDamage> afterDamages, Entity entity, DamageSource source, float amount, EntityHitResult entityHitResult) {
	boolean r = entity.damage(source, amount);
	if (r) {
		for (var callback : afterHits)
			callback.accept(projectile, entityHitResult);
		for (AfterDamage callback : afterDamages)
			callback.afterDamage(projectile, entityHitResult, amount, source);
	}
	return r;
}

void addAfterHit(@NotNull SBiConsumer<Projectile, HitResult> callback);

@FunctionalInterface
interface AfterDamage extends Serializable {
	void afterDamage(Projectile host, EntityHitResult entityHitResult, double damage, DamageSource damageSource);
}

void addAfterDamage(@NotNull AfterDamage afterDamage);

}
