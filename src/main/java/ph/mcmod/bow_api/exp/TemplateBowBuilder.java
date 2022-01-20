package ph.mcmod.bow_api.exp;

import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.models.JModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import ph.mcmod.bow_api.BowSettings;
import ph.mcmod.bow_api.EArrowEntityRenderer;
import ph.mcmod.bow_api.SimpleBowItem;
import ph.mcmod.bow_api.SimpleEntityType;
import ph.mcmod.bow_api.util.MinecraftUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import static ph.mcmod.bow_api.Main.print;

public class TemplateBowBuilder {

private final Identifier identifier;
private boolean arrowDiscard;
private boolean lightning;
private double explosive;
private BowSettings bowSettings = new BowSettings();
private ParticleEffect particle;
private EntityRendererFactory<TemplateArrowEntity> rendererFactory;
private EntityType<?> transferOnShoot;
private RuntimeResourcePack runtimeResourcePack;
private NbtCompound spawnAfterDamage;
public class TemplateBowItem extends SimpleBowItem {

	private final SimpleEntityType<TemplateArrowEntity> arrowEntityType;

	protected TemplateBowItem(@NotNull BowSettings settings, SimpleEntityType<TemplateArrowEntity> arrowEntityType) {
		super(settings);
		this.arrowEntityType = arrowEntityType;
	}

	@Override
	public PersistentProjectileEntity calcProjectileEntity(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, double pullProgress) {
		var arrowEntity = getArrowEntityType().create(world);
		return MinecraftUtil.transfer(super.calcProjectileEntity(world, user, bowStack, arrowStack, pullProgress), arrowEntity,false);
	}

	@Override
	public Entity finallyModify(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, double pullProgress, PersistentProjectileEntity projectile) {
		return transferOnShoot == null ? super.finallyModify(world, user, bowStack, arrowStack, pullProgress, projectile) : MinecraftUtil.transfer(projectile, transferOnShoot.create(world), false);
	}

	public SimpleEntityType<TemplateArrowEntity> getArrowEntityType() {
		return arrowEntityType;
	}

}

public class TemplateArrowEntity extends SimpleArrowEntity {

	protected TemplateArrowEntity(EntityType<? extends TemplateArrowEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public void tick() {
		Vec3d prevPos = getPos();
		super.tick();
		if (particle != null)
			MinecraftUtil.stepInvoke(prevPos, getPos(), 0.2, pos -> world.addParticle(particle, true, pos.x, pos.y, pos.z, 0, 0, 0));
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		if (hitResult.getType() != HitResult.Type.MISS)
			if (arrowDiscard)
				discard();
	}

	@Override
	public void afterDamage(EntityHitResult entityHitResult, double damage, DamageSource damageSource) {
		super.afterDamage(entityHitResult, damage, damageSource);
		Vec3d averagePos = MinecraftUtil.average(entityHitResult, this);
		System.out.println(averagePos);
		boolean destructive = lightning || explosive > 0;
		Collection<Entity> vulnerable = new HashSet<>();
		if (destructive) {
			if (getOwner() != null && getOwner().isInvulnerable())
				vulnerable.add(getOwner());
			for (var cls : List.of(ItemEntity.class, ExperienceOrbEntity.class))
				vulnerable.addAll(world.getEntitiesByClass(cls, new Box(averagePos, averagePos).expand(Math.max(explosive * 4, 5)), Predicate.not(Entity::isInvulnerable)));
			for (Entity entity : vulnerable) {
				entity.setInvulnerable(true);
			}
		}
		if (lightning) {
			var lightningEntity = EntityType.LIGHTNING_BOLT.create(world);
			lightningEntity.setPosition(averagePos);
			world.spawnEntity(lightningEntity);
			lightningEntity.tick();
			lightningEntity.setCosmetic(true);
		}
		if (explosive != 0) {
			SimpleArrowEntity.explode(world, getOwner(), averagePos, getDamage()*getVelocity().length()*explosive/3, isOnFire());
		}
		if (destructive)
			for (Entity entity : vulnerable) {
				entity.setInvulnerable(false);
				entity.setFireTicks(-1000);
			}
		if (spawnAfterDamage!=null) {
			MinecraftUtil.summon(world,averagePos,spawnAfterDamage);
		}
	}

	@Override
	public Collection<ItemStack> getNeededItems() {
		return bowSettings.getNeededItems();
	}
}

public TemplateBowBuilder(Identifier identifier) {this.identifier = identifier;}

public TemplateBowBuilder setRuntimeResourcePack(RuntimeResourcePack runtimeResourcePack) {
	this.runtimeResourcePack = runtimeResourcePack;
	return this;
}

public TemplateBowBuilder setArrowDiscard() {
	this.arrowDiscard = true;
	return this;
}

public TemplateBowBuilder setLightning() {
	this.lightning = true;
	return this;
}

public TemplateBowBuilder setExplosive(double explosive) {
	this.explosive = explosive;
	return this;
}

public TemplateBowBuilder setBowSettings(BowSettings bowSettings) {
	this.bowSettings = bowSettings;
	return this;
}

public TemplateBowBuilder setParticle(ParticleEffect particle) {
	this.particle = particle;
	return this;
}

public TemplateBowBuilder setRendererFactory(EntityRendererFactory<TemplateArrowEntity> rendererFactory) {
	this.rendererFactory = rendererFactory;
	return this;
}

public TemplateBowBuilder setTransferOnShoot(EntityType<?> transferOnShoot) {
	if (!transferOnShoot.isSummonable())
		throw new IllegalArgumentException("!transferOnShoot.isSummonable()");
	this.transferOnShoot = transferOnShoot;
	return this;
}

public TemplateBowItem build() {
	if (bowSettings.isDefaultDamage())
		bowSettings.maxDamage(Items.ARROW.getMaxDamage());
	if (bowSettings.getItemGroup()==null)
		bowSettings.group(ItemGroup.COMBAT);
	var entityType = Registry.register(
	  Registry.ENTITY_TYPE,
	  new Identifier(identifier.getNamespace(), "arrow/" + identifier.getPath()),
	  SimpleEntityType.builder(TemplateArrowEntity::new).copyFrom(EntityType.ARROW).build());
	EntityRendererRegistry.register(entityType, rendererFactory == null ? EArrowEntityRenderer::new : rendererFactory);
	if (runtimeResourcePack != null)
		runtimeResourcePack.addModel(JModel.model("item/bow"), new Identifier(identifier.getNamespace(), "item/" + identifier.getPath()));
	return Registry.register(Registry.ITEM, identifier, new TemplateBowItem(bowSettings, entityType));
}

}
