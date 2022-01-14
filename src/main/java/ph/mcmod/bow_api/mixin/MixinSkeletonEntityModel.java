package ph.mcmod.bow_api.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ph.mcmod.bow_api.RenderedAsBow;

@Mixin(SkeletonEntityModel.class)
@Environment(EnvType.CLIENT)
public class MixinSkeletonEntityModel<T extends MobEntity & RangedAttackMob> extends BipedEntityModel<T> {
public MixinSkeletonEntityModel(ModelPart root) {
	super(root);
}

/**
 * @author Phoupraw
 * @reason 在泛型方法里重定向导致publish报错，无法修复，因此改用重写
 */
@Overwrite
public void animateModel(T mobEntity, float f, float g, float h) {
	this.rightArmPose = ArmPose.EMPTY;
	this.leftArmPose = ArmPose.EMPTY;
	ItemStack bowStack = mobEntity.getStackInHand(Hand.MAIN_HAND);
	if (bowStack.getItem() instanceof RenderedAsBow && mobEntity.isAttacking()) {
		if (mobEntity.getMainArm() == Arm.RIGHT) {
			this.rightArmPose = ArmPose.BOW_AND_ARROW;
		} else {
			this.leftArmPose = ArmPose.BOW_AND_ARROW;
		}
	}

	super.animateModel(mobEntity, f, g, h);
}

/**
 * @author Phoupraw
 * @reason 在泛型方法里重定向导致publish报错，无法修复，因此改用重写
 */
@Overwrite
public void setAngles(T mobEntity, float f, float g, float h, float i, float j) {
	super.setAngles(mobEntity, f, g, h, i, j);
	ItemStack bowStack = mobEntity.getMainHandStack();
	if (mobEntity.isAttacking() && (bowStack.isEmpty() || !(bowStack.getItem() instanceof RenderedAsBow))) {
		float k = MathHelper.sin(this.handSwingProgress * 3.1415927F);
		float l = MathHelper.sin((1.0F - (1.0F - this.handSwingProgress) * (1.0F - this.handSwingProgress)) * 3.1415927F);
		this.rightArm.roll = 0.0F;
		this.leftArm.roll = 0.0F;
		this.rightArm.yaw = -(0.1F - k * 0.6F);
		this.leftArm.yaw = 0.1F - k * 0.6F;
		this.rightArm.pitch = -1.5707964F;
		this.leftArm.pitch = -1.5707964F;
		ModelPart var10000 = this.rightArm;
		var10000.pitch -= k * 1.2F - l * 0.4F;
		var10000 = this.leftArm;
		var10000.pitch -= k * 1.2F - l * 0.4F;
		CrossbowPosing.swingArms(this.rightArm, this.leftArm, h);
	}
}

}
