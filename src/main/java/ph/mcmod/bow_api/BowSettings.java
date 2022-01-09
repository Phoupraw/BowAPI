package ph.mcmod.bow_api;

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;
import ph.mcmod.bow_api.Serialization.SBiConsumer;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 用于设置弓箭的简单属性
 *
 * @see SimpleBowItem#SimpleBowItem(BowSettings)
 */
public class BowSettings extends FabricItemSettings {

public interface FinallyModify extends Serializable {
	Entity finallyModify(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, double pullProgress, Entity projectile);
}

public static Projectile.AfterDamage explodeAfterDamage(float power, boolean createFire, Explosion.DestructionType destructionType) {
	return (host, entityHitResult, damage, damageSource) -> {
		var pos = host.getPos().add(entityHitResult.getPos()).multiply(0.5);
		var world = host.getWorld();
		float power1 = (float) (power * host.getVelocity().length());
		world.createExplosion((host.getOwner() instanceof Entity owner) ? owner : ((host instanceof Entity entity) ? entity : null), pos.x, pos.y, pos.z, power1, createFire, destructionType);
	};
}

public static FinallyModify changeEntity(EntityType<?> entityType) {
	String typeId = Registry.ENTITY_TYPE.getId(entityType).toString();
	return (world, user, bowStack, arrowStack, pullProgress, projectile) -> {
		EntityType<?> type = Registry.ENTITY_TYPE.get(new Identifier(typeId));
		@NotNull var r = Objects.requireNonNull(type.create(world));
		r.readNbt(projectile.writeNbt(new NbtCompound()));
		r.resetPosition();
		return r;
	};
}

private double damageAddend = 2;
private double damageFactor = 1;
private double pullSpeed = 1;
private double velocityAddend = 0;
private double velocityFactor = 1;
private boolean arrowDiscard = false;
private final List<SBiConsumer<Projectile, HitResult>> afterHits = new LinkedList<>();
private final List<Projectile.AfterDamage> afterDamages = new LinkedList<>();
private final List<FinallyModify> finallyModifies = new LinkedList<>();

/**
 * 在最初，把箭的伤害（{@link PersistentProjectileEntity#getDamage()}、{@link PersistentProjectileEntity#setDamage(double)}）加上这个
 */
public @NotNull BowSettings setDamageAddend(double damageAddend) {
	this.damageAddend = damageAddend;
	return this;
}

/**
 * 在最后，把箭的伤害（{@link PersistentProjectileEntity#getDamage()}、{@link PersistentProjectileEntity#setDamage(double)}）乘以这个
 */
public @NotNull BowSettings setDamageFactor(double damageFactor) {
	this.damageFactor = damageFactor;
	return this;
}

/**
 * 将计算出的拉弓进度{@link SimpleBowItem#calcPullProgress(World, LivingEntity, ItemStack, ItemStack, int)}乘这个
 *
 * @see #setPullTicks(int)
 */
public @NotNull BowSettings setPullSpeed(double pullSpeed) {
	this.pullSpeed = pullSpeed;
	return this;
}

/**
 * 将计算出的拉弓进度{@link SimpleBowItem#calcPullProgress(World, LivingEntity, ItemStack, ItemStack, int)}乘20除以这个
 *
 * @see #setPullTicks(int)
 */
public @NotNull BowSettings setPullTicks(int usingTicks) {
	pullSpeed *= 20.0 / usingTicks;
	return this;
}

/**
 * 在最初，把箭的速度加上这个乘拉弓进度（见{@link SimpleBowItem#calcPullProgress(World, LivingEntity, ItemStack, ItemStack, int)}）
 */
public @NotNull BowSettings setVelocityAddend(double velocityAddend) {
	this.velocityAddend = velocityAddend;
	return this;
}

/**
 * 在最后，把箭的速度乘上这个
 */
public @NotNull BowSettings setVelocityFactor(double velocityFactor) {
	this.velocityFactor = velocityFactor;
	return this;
}

/**
 * 让箭在落地后立马消失
 */

public @NotNull BowSettings setArrowDiscard(boolean arrowDiscard) {
	this.arrowDiscard = arrowDiscard;
	return this;
}

//public @NotNull BowSettings setSpawnOnHit(@NotNull EntityType<?> entityType) {
//	this.spawnOnHit = Objects.requireNonNull(entityType)::create;
//	return this;
//}
//
//public @NotNull BowSettings setSpawnOnHit(@NotNull Function<World, Entity> function) {
//	this.spawnOnHit = Objects.requireNonNull(function);
//	return this;
//}

public @NotNull BowSettings addAfterHit(@NotNull SBiConsumer<Projectile, HitResult> callback) {
	afterHits.add(Objects.requireNonNull(callback));
	return this;
}

public @NotNull BowSettings addAfterDamage(@NotNull Projectile.AfterDamage callback) {
	afterDamages.add(Objects.requireNonNull(callback));
	return this;
}

public @NotNull BowSettings addFinallyModify(@NotNull FinallyModify callback) {
	finallyModifies.add(Objects.requireNonNull(callback));
	return this;
}

public double getDamageAddend() {
	return damageAddend;
}

public double getDamageFactor() {
	return damageFactor;
}

public double getPullSpeed() {
	return pullSpeed;
}

public double getVelocityAddend() {
	return velocityAddend;
}

public double getVelocityFactor() {
	return velocityFactor;
}

public boolean isArrowDiscard() {
	return arrowDiscard;
}

public @NotNull List<SBiConsumer<Projectile, HitResult>> getAfterHits() {
	return afterHits;
}

public @NotNull List<Projectile.AfterDamage> getAfterDamages() {
	return afterDamages;
}

protected @NotNull List<FinallyModify> getFinallyModifies() {
	return finallyModifies;
}

public BowSettings() {
	super();
}

@Override
public BowSettings equipmentSlot(EquipmentSlotProvider equipmentSlotProvider) {
	super.equipmentSlot(equipmentSlotProvider);
	return this;
}

@Override
public BowSettings customDamage(CustomDamageHandler handler) {
	super.customDamage(handler);
	return this;
}

@Override
public BowSettings food(FoodComponent foodComponent) {
	super.food(foodComponent);
	return this;
}

@Override
public BowSettings maxCount(int maxCount) {
	super.maxCount(maxCount);
	return this;
}

@Override
public BowSettings maxDamageIfAbsent(int maxDamage) {
	super.maxDamageIfAbsent(maxDamage);
	return this;
}

@Override
public BowSettings maxDamage(int maxDamage) {
	super.maxDamage(maxDamage);
	return this;
}

@Override
public BowSettings recipeRemainder(Item recipeRemainder) {
	super.recipeRemainder(recipeRemainder);
	return this;
}

@Override
public BowSettings group(ItemGroup group) {
	super.group(group);
	return this;
}

@Override
public BowSettings rarity(Rarity rarity) {
	super.rarity(rarity);
	return this;
}

@Override
public BowSettings fireproof() {
	super.fireproof();
	return this;
}
}
