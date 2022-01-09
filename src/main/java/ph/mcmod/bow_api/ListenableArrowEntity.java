package ph.mcmod.bow_api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import ph.mcmod.bow_api.mixin.MixinPersistentProjectileEntity;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

@Deprecated
public abstract class ListenableArrowEntity extends ArrowEntity implements Cloneable {

@FunctionalInterface
public interface AfterEntityDamage {
	void accept(EntityHitResult entityHitResult, DamageSource source, float amount, boolean success);
}

/**
 * 在{@link PersistentProjectileEntity#onEntityHit(EntityHitResult)}里调用{@link Entity#damage(DamageSource, float)}后紧跟着调用
 *
 * @see MixinPersistentProjectileEntity#onDamage(Entity, DamageSource, float, EntityHitResult)
 */
public final List<AfterEntityDamage> followingDamage = new LinkedList<>();
/**
 * 在击中方块或伤害实体之前调用<br>
 * 具体来说，是在{@link #onBlockHit(BlockHitResult)}的开头和与{@link #followingDamage}相同的位置调用
 */
public final List<BiConsumer<? super ListenableArrowEntity,? super HitResult>> followingHit = new LinkedList<>();
/**
 * 在击中方块或伤害实体之后调用<br>
 * 具体来说，是在{@link #onBlockHit(BlockHitResult)}的末尾和{@link PersistentProjectileEntity#onEntityHit(EntityHitResult)}里调用{@link PersistentProjectileEntity#playSound(SoundEvent, float, float)}后调用
 *
 */
public final List<BiConsumer<? super ListenableArrowEntity,? super  HitResult>> afterHit = new LinkedList<>();

public static final String KEY_DISCARD = "discardAfterHit";
private boolean discardAfterHit;
public ListenableArrowEntity(EntityType<? extends ListenableArrowEntity> entityType, World world) {
	super(entityType, world);
	followingDamage.add((entityHitResult, source, amount, success) -> {
		if (success) {
			for (var callback : followingHit)
				callback.accept(this, entityHitResult);
		}
	});
	afterHit.add((arrowEntity, hitResult) -> {
		if (arrowEntity.isDiscardAfterHit()) {
			arrowEntity.discard();
		}
	});
}

@Override
public void onBlockHit(BlockHitResult blockHitResult) {
	for (var callback : followingHit)
		callback.accept(this, blockHitResult);
	super.onBlockHit(blockHitResult);
	for (var callback : afterHit)
		callback.accept(this, blockHitResult);
}

@Override
public void writeCustomDataToNbt(NbtCompound nbt) {
	super.writeCustomDataToNbt(nbt);
	nbt.putBoolean(KEY_DISCARD,isDiscardAfterHit());
}

@Override
public void readCustomDataFromNbt(NbtCompound nbt) {
	super.readCustomDataFromNbt(nbt);
	setDiscardAfterHit(nbt.getBoolean(KEY_DISCARD));
}
public ListenableArrowEntity setDiscardAfterHit() {
	return setDiscardAfterHit(true);
}
public ListenableArrowEntity setDiscardAfterHit(boolean discardAfterHit) {
	this.discardAfterHit = discardAfterHit;
	return this;
}

public boolean isDiscardAfterHit() {
	return discardAfterHit;
}
@Override
protected ListenableArrowEntity clone() {

	try {
		ListenableArrowEntity clone = (ListenableArrowEntity) super.clone();
		discard();
		return clone;
	} catch (CloneNotSupportedException e) {
		throw new AssertionError(e);
	}
}
}
