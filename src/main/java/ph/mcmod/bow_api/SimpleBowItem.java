package ph.mcmod.bow_api;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import ph.mcmod.bow_api.mixin.AccessPersistentProjectileEntity;

import java.util.List;


@SuppressWarnings("unused")
public class SimpleBowItem extends BowItem implements RenderedAsBow, CalcPullProgress {
protected final double damageAddend;
protected final double damageFactor;
protected final double pullSpeed;
protected final double velocityAddend;
protected final double velocityFactor;
protected final boolean arrowDiscard;
protected final List<ItemStack> neededItems;

public SimpleBowItem(@NotNull BowSettings settings) {
	super(settings);
	damageAddend = settings.getDamageAddend();
	damageFactor = settings.getDamageFactor();
	pullSpeed = settings.getPullSpeed();
	velocityAddend = settings.getVelocityAddend();
	velocityFactor = settings.getVelocityFactor();
	arrowDiscard = settings.isArrowDiscard();
	neededItems= settings.getNeededItems();
}

@Override
public void onStoppedUsing(@NotNull ItemStack bowStack, @NotNull World world, @NotNull LivingEntity user, int remainingUseTicks) {
	if (world.isClient())
		return;
	int usingTicks = getMaxUseTime(bowStack) - remainingUseTicks;
	ItemStack arrowStack = calcArrowStack(bowStack, world, user, usingTicks);
	if (arrowStack.isEmpty())
		return;
	double pullProgress = calcPullProgress(world, user, bowStack, arrowStack, usingTicks);
	if (Double.isNaN(pullProgress))
		return;
	var projectileEntity = calcProjectileEntity(world, user, bowStack, arrowStack, pullProgress);
	projectileEntity.setOwner(user);
	projectileEntity.setPosition(user.getEyePos().add(0, -0.1, 0));
	projectileEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, (float) (pullProgress * 3), 1);
	projectileEntity.setDamage(projectileEntity.getDamage() + getDamageAddend());
	projectileEntity.setVelocity(projectileEntity.getVelocity().add(projectileEntity.getVelocity().normalize().multiply(pullProgress * getVelocityAddend())));
	((ProgressPulled)projectileEntity).setPullProgress(pullProgress);
	if (pullProgress >= 1)
		projectileEntity.setCritical(true);
	int power = getPower(world,user,bowStack,arrowStack,pullProgress,projectileEntity);
	if (power > 0)
		projectileEntity.setDamage(calcDamage(world,user,bowStack,arrowStack,pullProgress,projectileEntity,power));
	int punch = EnchantmentHelper.getLevel(Enchantments.PUNCH, bowStack);
	if (punch > 0)
		projectileEntity.setPunch(punch);
	int flame = EnchantmentHelper.getLevel(Enchantments.FLAME, bowStack);
	if (flame > 0)
		projectileEntity.setOnFireFor(100 * flame);
	bowStack.damage(1, user, living -> living.sendToolBreakStatus(user.getActiveHand()));
	if (user instanceof PlayerEntity player) {
		player.incrementStat(Stats.USED.getOrCreateStat(this));
		if (calcInfinity(world, player, bowStack, arrowStack, pullProgress) > world.random.nextDouble())
			projectileEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
		else
			arrowStack.decrement(1);
	} else {
		projectileEntity.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
	}
	projectileEntity.setDamage(projectileEntity.getDamage() * getDamageFactor());
	projectileEntity.setVelocity(projectileEntity.getVelocity().multiply(getVelocityFactor()));
	if (isArrowDiscard())
		((AccessPersistentProjectileEntity) projectileEntity).setLife(1200);
	world.spawnEntity(finallyModify(world, user, bowStack, arrowStack, pullProgress, projectileEntity));
	playSoundOnShoot(world, user, bowStack, arrowStack, projectileEntity, pullProgress);
//	System.out.println(projectileEntity.getVelocity().length());
}

/**
 * @see BowSettings#setDamageAddend(double)
 */
public double getDamageAddend() {
	return damageAddend;
}

/**
 * @see BowSettings#setDamageFactor(double)
 */
public double getDamageFactor() {
	return damageFactor;
}

/**
 * @see BowSettings#setPullSpeed(double)
 */
public double getPullSpeed() {
	return pullSpeed;
}

/**
 * @see BowSettings#setVelocityAddend(double)
 */
public double getVelocityAddend() {
	return velocityAddend;
}

/**
 * @see BowSettings#setVelocityFactor(double)
 */
public double getVelocityFactor() {
	return velocityFactor;
}

/**
 * @see BowSettings#setArrowDiscard(boolean)
 */
public boolean isArrowDiscard() {
	return arrowDiscard;
}

public List<ItemStack> getNeededItems() {
	return neededItems;
}

/**
 * 计算拉弓进度
 *
 * @param bowStack   弓
 * @param arrowStack 箭
 * @param user       拉弓的生物
 * @param usingTicks 已经拉了多久的弓，单位是游戏刻
 * @return 如果大于1，则箭会暴击；如果是{@link Double#NaN}，则视为拉弓失败，箭不会射出；可以大于1，会反映在箭的速度和伤害等属性
 */
@Override
public double calcPullProgress(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, int usingTicks) {
	return getPullProgress((int) (usingTicks * getPullSpeed()));
}

/**
 * 乘以了{@link #getPullSpeed()}
 */
@Override
public double calcPullProgress(AbstractClientPlayerEntity player, @NotNull ItemStack bow, int usingTicks) {
	return RenderedAsBow.super.calcPullProgress(player, bow, (int) (usingTicks * getPullSpeed()));
}

/**
 * 计算无限的概率，即箭不消耗的概率，在这种情况下，{@code arrowStack}不会消耗，且射出的箭实体的{@link PersistentProjectileEntity#pickupType}会被设为{@link PersistentProjectileEntity.PickupPermission#CREATIVE_ONLY}
 *
 * @param bowStack     弓
 * @param arrowStack   箭
 * @param world        玩家所处的世界
 * @param player       玩家
 * @param pullProgress 拉弓进度，见{@link #calcPullProgress(World, LivingEntity, ItemStack, ItemStack, int)}
 * @return 概率
 */
@SuppressWarnings("unused")
public double calcInfinity(World world, PlayerEntity player, ItemStack bowStack, ItemStack arrowStack, double pullProgress) {
	return player.isCreative() || EnchantmentHelper.getLevel(Enchantments.INFINITY, bowStack) >= 1 && arrowStack.isOf(Items.ARROW) ? 1 : 0;
}

/**
 * 计算要使用的箭。<br>
 * 默认情况下，当玩家身上有好几种箭时，该方法优先返回副手的、或者靠近物品栏左下角的箭。
 *
 * @param bowStack   弓
 * @param world      玩家所处的世界
 * @param user       玩家
 * @param usingTicks 已经拉了多久的弓，单位是游戏刻
 * @return 要使用的箭。请直接返回原物品的引用而不是复制（{@link ItemStack#copy()}）一个新的物品，因为之后如果要消耗箭，就是直接调用这个引用的{@link ItemStack#decrement(int)}
 */
@SuppressWarnings("unused")
public ItemStack calcArrowStack(ItemStack bowStack, World world, LivingEntity user, int usingTicks) {
	ItemStack arrowStack;
	if (user instanceof PlayerEntity player) {
		arrowStack = player.getArrowType(bowStack);
		if (arrowStack.isEmpty() && player.isCreative())
			arrowStack = Items.ARROW.getDefaultStack();
	} else {
		arrowStack = user.getStackInHand(Hand.MAIN_HAND);
		if (arrowStack.isEmpty())
			arrowStack = user.getStackInHand(Hand.OFF_HAND);
	}
	return arrowStack;
}

/**
 * 计算要生成的箭实体。<br>
 * 默认情况下，如果使用的是普通箭或者药箭，那么返回{@link ArrowEntity}；如果使用的是光灵箭，那么返回{@link SpectralArrowEntity}。
 *
 * @param bowStack     弓
 * @param arrowStack   箭
 * @param world        世界
 * @param user         射箭的生物
 * @param pullProgress 拉弓进度，见{@link #calcPullProgress(World, LivingEntity, ItemStack, ItemStack, int)}
 * @return 要生成的箭实体
 */
@SuppressWarnings("unused")
public PersistentProjectileEntity calcProjectileEntity(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, double pullProgress) {
	ArrowItem arrowItem = arrowStack.getItem() instanceof ArrowItem a ? a : (ArrowItem) Items.ARROW;
	return arrowItem.createArrow(world, arrowStack, user);
}

public Entity finallyModify(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, double pullProgress, PersistentProjectileEntity projectile) {
	return projectile;
}

/**
 * 在射箭的最后播放音效
 *
 * @param world        世界
 * @param user         射箭者
 * @param bowStack     弓
 * @param arrowStack   箭
 * @param arrowEntity  射出去的箭实体，此时已经调用了{@link World#spawnEntity(Entity)}
 * @param pullProgress 拉弓进度，可能大于1
 */
public void playSoundOnShoot(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, PersistentProjectileEntity arrowEntity, double pullProgress) {
	SoundEvent sound = SoundEvents.ENTITY_ARROW_SHOOT;
	SoundCategory category = SoundCategory.AMBIENT;
	if (user instanceof SkeletonEntity) {
		sound = SoundEvents.ENTITY_SKELETON_SHOOT;
		category = SoundCategory.HOSTILE;
	} else if (user instanceof PlayerEntity) {
		category = SoundCategory.PLAYERS;
	}
	world.playSound(null, user.getX(), user.getY(), user.getZ(), sound, category, 1.0F, (float) (1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + pullProgress * 0.5F));
}

@Override
public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
	super.usageTick(world, user, stack, remainingUseTicks);
}
public int getPower(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, double pullProgress, PersistentProjectileEntity projectileEntity) {
	return EnchantmentHelper.getLevel(Enchantments.POWER, bowStack);
}
public double calcDamage(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, double pullProgress, PersistentProjectileEntity projectileEntity,int power) {
	return projectileEntity.getDamage() + power * 0.5 + 0.5;
}

@Override
public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
	ItemStack handStack = player.getStackInHand(hand);
	boolean arrowEmpty = getNeededItems().isEmpty()? player.getArrowType(handStack).isEmpty(): getNeededItems().stream().allMatch(player.getInventory()::contains);
	if (!player.isCreative() && arrowEmpty) {
		return TypedActionResult.fail(handStack);
	} else {
		player.setCurrentHand(hand);
		return TypedActionResult.consume(handStack);
	}
}
}
