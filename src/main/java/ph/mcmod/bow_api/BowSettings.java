package ph.mcmod.bow_api;

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用于设置弓箭的简单属性
 *
 * @see SimpleBowItem#SimpleBowItem(BowSettings)
 */
@SuppressWarnings("unused")
public class BowSettings extends FabricItemSettings {

public interface FinallyModify extends Serializable {
	Entity finallyModify(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, double pullProgress, Entity projectile);
}

private double damageAddend = 0;
private double damageFactor = 1;
private double pullSpeed = 1;
private double velocityAddend = 0;
private double velocityFactor = 1;
private boolean arrowDiscard = false;
protected final List<ItemStack> neededItems = new ArrayList<>();

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
public BowSettings addNeededItems(ItemStack... itemStacks) {
	neededItems.addAll(Arrays.asList(itemStacks));
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

public List<ItemStack> getNeededItems() {
	return neededItems;
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
