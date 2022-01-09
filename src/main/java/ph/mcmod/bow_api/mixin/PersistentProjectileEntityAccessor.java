package ph.mcmod.bow_api.mixin;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PersistentProjectileEntity.class)
public interface PersistentProjectileEntityAccessor {
@Accessor
@SuppressWarnings("unused")
int getLife();

@Accessor
void setLife(int value);


}
