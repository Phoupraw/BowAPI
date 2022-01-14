package ph.mcmod.bow_api.test;

import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.models.JModel;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import ph.mcmod.bow_api.*;
import ph.mcmod.bow_api.util.MinecraftUtil;

import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Test {
static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create(new Identifier(Main.NAMESPACE, "runtime"));
static final SimpleEntityType<SimpleArrowEntity> CREEPER_ARROW = Main.register(new Identifier(Main.NAMESPACE, "creeper_arrow"), (type, world) -> new SimpleArrowEntity(type, world) {
	@Override
	public void afterDamage(EntityHitResult entityHitResult, double damage, DamageSource damageSource) {
		explode(this, entityHitResult, 1);
	}
});
static final Item CREEPER_BOW = Registry.register(Registry.ITEM, new Identifier(Main.NAMESPACE, "creeper_bow"), new SimpleBowItem(new BowSettings()
  .group(ItemGroup.COMBAT)
  .maxDamage(450)
  .setPullTicks(10)
) {
	@Override
	public PersistentProjectileEntity calcProjectileEntity(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, double pullProgress) {
		return MinecraftUtil.copyNbt(super.calcProjectileEntity(world, user, bowStack, arrowStack, pullProgress), CREEPER_ARROW.create(world));
	}
});
static final EntityType<SimpleArrowEntity> POWERED_CREEPER_ARROW = Main.register(new Identifier(Main.NAMESPACE, "powered_creeper_arrow"), (type, world) -> new SimpleArrowEntity(type, world) {
	@Override
	public void afterDamage(EntityHitResult entityHitResult, double damage, DamageSource damageSource) {
//		explode(this, entityHitResult,1);
		var server = world.getServer();
		if (server != null) {
			var creeper = MinecraftUtil.setPowered(new CreeperEntity(EntityType.CREEPER, world));
			creeper.setPosition(entityHitResult.getPos().add(this.getPos()).multiply(0.5));
			creeper.setFuseSpeed(100);
			double power = getDamage() * getVelocity().length()/2;
			var nbt = creeper.writeNbt(new NbtCompound())	;
			nbt.putByte("ExplosionRadius", (byte) Math.min(Math.round(power),127));
			creeper.readNbt(nbt);
			world.spawnEntity(creeper);
			var itemsOrbs = Stream.concat(world.getEntitiesByClass(ItemEntity.class,new Box(getPos(),getPos()).expand(power*3), Predicate.not(Entity::isInvulnerable)).stream(),world.getEntitiesByClass(ExperienceOrbEntity.class,new Box(getPos(),getPos()).expand(power*3), Predicate.not(Entity::isInvulnerable)).stream()).toList();
			for (Entity entity : itemsOrbs) {
				entity.setInvulnerable(true);
			}
			MinecraftUtil.invulnerable(getOwner(), () -> MinecraftUtil.temp(server.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING), value -> server.getGameRules().get(GameRules.DO_MOB_GRIEFING).set(value, server), false, creeper::tick));
			for (Entity entity : itemsOrbs) {
				entity.setInvulnerable(false);
			}
		}
//		Main.print(creeper);
	}
});
static final Item POWERED_CREEPER_BOW = Registry.register(Registry.ITEM, new Identifier(Main.NAMESPACE, "powered_creeper_bow"), new SimpleBowItem(new BowSettings()
  .group(ItemGroup.COMBAT)
  .maxDamage(450)
  .setPullTicks(10)
) {
	@Override
	public PersistentProjectileEntity calcProjectileEntity(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, double pullProgress) {
		return MinecraftUtil.copyNbt(super.calcProjectileEntity(world, user, bowStack, arrowStack, pullProgress), POWERED_CREEPER_ARROW.create(world));
	}
});

static {
	RRPCallback.AFTER_VANILLA.register(resources -> resources.add(RESOURCE_PACK));
}

static <T extends Item> T register(String path, T item) {
	var id = new Identifier(Main.NAMESPACE, path);
	RESOURCE_PACK.addModel(JModel.model("item/bow"), new Identifier(Main.NAMESPACE, "item/" + path));
	return Registry.register(Registry.ITEM, new Identifier(Main.NAMESPACE, path), item);
}

public static void init() {
//		EntityRendererRegistry.INSTANCE.register(CreeperArrowEntity.ENTITY_TYPE, ArrowEntityRenderer::new);


}
}
