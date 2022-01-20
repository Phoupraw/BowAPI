package ph.mcmod.bow_api.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ph.mcmod.bow_api.RenderedAsBow;

@Mixin(AbstractClientPlayerEntity.class)
@Environment(EnvType.CLIENT)
public abstract class MixinAbstractClientPlayerEntity extends PlayerEntity {
private ItemStack getSpeed_itemStack = ItemStack.EMPTY;

public MixinAbstractClientPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
	super(world, pos, yaw, profile);
}

/**
 *
 * 让{@link RenderedAsBow}也能降低玩家的移动速度，以此来让FOV变大。
 */
@Redirect(method = "getSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
private boolean onIfIsOf(ItemStack itemStack, Item item) {
	getSpeed_itemStack = itemStack;
	return itemStack.isOf(item) || itemStack.getItem() instanceof RenderedAsBow;
}

/**
 * 原版是把拉满弓的时长写死成1秒，这里改成调用{@link RenderedAsBow}自己的方法来计算拉满弓的时长。
 */
@Redirect(method = "getSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getItemUseTime()I"))
private int onGetItemUseTime(AbstractClientPlayerEntity player) {
	if (!(getSpeed_itemStack.getItem() instanceof RenderedAsBow renderedAsBow))
		return getItemUseTime();
	return (int) (Math.sqrt(renderedAsBow.calcPull(player,getSpeed_itemStack,  getItemUseTime())) * 20);
}

}
