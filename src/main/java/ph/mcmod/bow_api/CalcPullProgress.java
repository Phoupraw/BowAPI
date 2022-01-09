package ph.mcmod.bow_api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface CalcPullProgress {
double calcPullProgress(World world, LivingEntity user, ItemStack bowStack, ItemStack arrowStack, int usingTicks);
}
