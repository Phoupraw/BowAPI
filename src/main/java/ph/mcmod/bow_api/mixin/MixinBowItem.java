package ph.mcmod.bow_api.mixin;

import net.minecraft.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import ph.mcmod.bow_api.RenderedAsBow;
@Mixin(BowItem.class)
public class MixinBowItem implements RenderedAsBow {
}
