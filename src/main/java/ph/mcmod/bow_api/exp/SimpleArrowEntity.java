package ph.mcmod.bow_api.exp;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import ph.mcmod.bow_api.BAfterDamage;
import ph.mcmod.bow_api.util.MinecraftUtil;

import java.util.Collection;
import java.util.List;

public class SimpleArrowEntity extends ArrowEntity implements BAfterDamage {
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
//	System.out.println("entityHitResult = " + entityHitResult + ", damage = " + damage + ", damageSource = " + damageSource);//TODO
//	if (entityHitResult.getEntity() instanceof PlayerEntity player) {
//player.damage()
//	}
}
public Collection<ItemStack> getNeededItems() {
	return List.of();
}

@Override
protected boolean tryPickup(PlayerEntity player) {
	switch (this.pickupType) {
		case ALLOWED -> {
			MinecraftUtil.give(player, getNeededItems().toArray(ItemStack[]::new));
			return true;
		}
		case CREATIVE_ONLY -> {
			return player.isCreative();
		}
		default -> {
			return false;
		}
	}
}
}
