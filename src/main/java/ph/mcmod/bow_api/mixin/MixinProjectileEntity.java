package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ph.mcmod.bow_api.Owned;
import ph.mcmod.bow_api.Projectile;
import ph.mcmod.bow_api.Serialization;
import ph.mcmod.bow_api.Serialization.SBiConsumer;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static ph.mcmod.bow_api.Serialization.deserialize;
import static ph.mcmod.bow_api.Serialization.serialize;

@Mixin(ProjectileEntity.class)
public abstract class MixinProjectileEntity implements Projectile {
private final ProjectileEntity _this = (ProjectileEntity) (Object) this;
protected final List<SBiConsumer<Projectile, HitResult>> afterHits = new LinkedList<>();
protected final List<AfterDamage> afterDamages = new LinkedList<>();

@Override
public void addAfterHit(@NotNull SBiConsumer<Projectile, HitResult> callback) {
	afterHits.add(Objects.requireNonNull(callback));
}

@Override
public void addAfterDamage(@NotNull AfterDamage afterDamage) {
	afterDamages.add(Objects.requireNonNull(afterDamage));
}

@Override
public World getWorld() {
	return _this.getEntityWorld();
}

@Override
public Object getOwner() {
	return _this.getOwner();
}

@Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
private void writeNbt(NbtCompound nbt, CallbackInfo ci) {
	nbt.put("afterHits", serialize(afterHits));
	nbt.put("afterDamages", serialize(afterDamages));
}

@Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
private void readNbt(NbtCompound nbt, CallbackInfo ci) {
	afterHits.clear();
	afterHits.addAll(deserialize(nbt.getList("afterHits", NbtElement.BYTE_ARRAY_TYPE)));
	afterDamages.clear();
	afterDamages.addAll(deserialize(nbt.getList("afterDamages", NbtElement.BYTE_ARRAY_TYPE)));
}

@Inject(method = "onEntityHit", at = @At(value = "RETURN"))
private void endIf(EntityHitResult entityHitResult, CallbackInfo ci) {
//	if ((Object) this instanceof ListenableArrowEntity a) {
//		for (var callback : a.afterHit)
//			callback.accept(a, entityHitResult);
//	}
//	if (_this instanceof PersistentProjectileEntity)
//		return;
//	for (var callback : afterHits)
//		callback.accept(this, entityHitResult);
//	for (var callback : afterDamages)
//		callback.afterDamage(this, entityHitResult, 0, null);
}

@Inject(method = "onBlockHit", at = @At("RETURN"))
private void afterHitBlock(BlockHitResult blockHitResult, CallbackInfo ci) {
	if (_this instanceof PersistentProjectileEntity)
		return;
	for (var callback : afterHits)
		callback.accept(this, blockHitResult);
}
}
