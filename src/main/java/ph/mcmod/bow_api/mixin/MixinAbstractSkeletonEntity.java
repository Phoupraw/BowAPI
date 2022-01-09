package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ph.mcmod.bow_api.SimpleBowItem;

@Mixin(AbstractSkeletonEntity.class)
public abstract class MixinAbstractSkeletonEntity extends HostileEntity {
@Shadow
@Final
private MeleeAttackGoal meleeAttackGoal;

@Shadow
@Final
private BowAttackGoal<AbstractSkeletonEntity> bowAttackGoal;

protected MixinAbstractSkeletonEntity(EntityType<? extends HostileEntity> entityType, World world) {
	super(entityType, world);
}

/**
 * @author Phoupraw
 * @reason 让骷髅手持任何 {@link BowItem}都能射箭。
 */
@Overwrite
public void updateAttackType() {
	if (this.world != null && !this.world.isClient) {
		this.goalSelector.remove(this.meleeAttackGoal);
		this.goalSelector.remove(this.bowAttackGoal);
		ItemStack itemStack = this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
		if (itemStack.getItem() instanceof BowItem) {
			int i = 20;
			if (this.world.getDifficulty() != Difficulty.HARD) {
				i = 40;
			}

			this.bowAttackGoal.setAttackInterval(i);
			this.goalSelector.add(4, this.bowAttackGoal);
		} else {
			this.goalSelector.add(4, this.meleeAttackGoal);
		}

	}
}

/**
 * @author Phoupraw
 * @reason 让骷髅手持任何 {@link BowItem}都能射箭。
 */
@Overwrite
public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
	return weapon instanceof BowItem;
}

/**
 * 如果是{@link SimpleBowItem}，那就不发声，而是在{@link SimpleBowItem#onStoppedUsing(ItemStack, World, LivingEntity, int)}里发声。
 */
@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/AbstractSkeletonEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
private void on2(AbstractSkeletonEntity skeleton, SoundEvent soundEvent, float volume, float pitch) {
	ItemStack bowStack = getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
	if (!(bowStack.getItem() instanceof SimpleBowItem)) {
		skeleton.playSound(soundEvent, volume, pitch);
	}
}

/**
 * 如果是{@link SimpleBowItem}，那就调用{@link BowItem#onStoppedUsing(ItemStack, World, LivingEntity, int)}来射箭。
 */
@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
private boolean on(World world, Entity entity) {
	ItemStack bowStack = getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW));
	Item bowItem = bowStack.getItem();
	if (bowItem instanceof SimpleBowItem) {
		bowItem.onStoppedUsing(bowStack, world, this, bowItem.getMaxUseTime(bowStack) - 20);
		return false;
	} else {
		return world.spawnEntity(entity);
	}
}
}
