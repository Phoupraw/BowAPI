package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ph.mcmod.bow_api.Main;

@Mixin(HorseBaseEntity.class)
public abstract class MixinHorseBaseEntity extends AnimalEntity {
@Shadow protected float jumpStrength;

protected MixinHorseBaseEntity(EntityType<? extends AnimalEntity> entityType, World world) {
	super(entityType, world);
}
@Inject(method = "travel", at = @At(value = "INVOKE",target = "Lnet/minecraft/entity/passive/HorseBaseEntity;getJumpBoostVelocityModifier()D",shift = At.Shift.AFTER))
private void onGetJumpBoostVelocityModifier(Vec3d movementInput, CallbackInfo ci) {
//	Main.print("".formatted(jumpStrength));
}
}
