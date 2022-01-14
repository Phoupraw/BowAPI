package ph.mcmod.bow_api.mixin;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityType.class)
public interface AccessEntityType {
@Accessor
ImmutableSet<Block> getCanSpawnInside();
}
