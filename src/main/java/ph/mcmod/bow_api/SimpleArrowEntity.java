package ph.mcmod.bow_api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;

public class SimpleArrowEntity extends ArrowEntity implements AfterDamage {

public static void explode(World world, Entity owner, Vec3d pos, double power, boolean fire) {
	world.createExplosion(owner, new EntityDamageSource("explosion", owner).setScaledWithDifficulty().setExplosive(), new EntityExplosionBehavior(owner), pos.x, pos.y, pos.z, (float) power, fire, Explosion.DestructionType.NONE);
}

public static void explode(PersistentProjectileEntity projectile,EntityHitResult entityHitResult,double powerFactor) {
	Vec3d pos = projectile.getPos().add(entityHitResult.getPos()).multiply(0.5);
	double power = projectile.getDamage() * projectile.getVelocity().length() / 2 * powerFactor;
	boolean fire = projectile.isOnFire();
	explode(projectile.world, projectile.getOwner(), pos, power, fire);
}

public SimpleArrowEntity(EntityType<? extends SimpleArrowEntity> entityType, World world) {
	super(entityType, world);
}

@Override
public void afterDamage(EntityHitResult entityHitResult, double damage, DamageSource damageSource) {

}

}
