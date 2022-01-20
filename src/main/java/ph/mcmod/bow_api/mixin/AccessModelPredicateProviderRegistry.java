package ph.mcmod.bow_api.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelPredicateProviderRegistry.class)
@Environment(EnvType.CLIENT)
public interface AccessModelPredicateProviderRegistry {
@Invoker
void invokeRegister(Item item, Identifier id, UnclampedModelPredicateProvider provider);
}
