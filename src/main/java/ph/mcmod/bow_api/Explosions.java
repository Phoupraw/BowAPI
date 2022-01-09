package ph.mcmod.bow_api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.EntityExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

public class Explosions {
public static void explode(World world, Entity entity, Vec3d pos, float power, boolean createFire, Explosion.DestructionType destructionType) {
	world.createExplosion(entity, DamageSource.ANVIL, new EntityExplosionBehavior(entity), pos.x, pos.y, pos.z, power, createFire, destructionType);
}
}
