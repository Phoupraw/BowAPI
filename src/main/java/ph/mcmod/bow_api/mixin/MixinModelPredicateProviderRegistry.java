package ph.mcmod.bow_api.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ph.mcmod.bow_api.RenderedAsBow;

import java.util.Map;

@Mixin(ModelPredicateProviderRegistry.class)
@Environment(EnvType.CLIENT)
public abstract class MixinModelPredicateProviderRegistry {

@Shadow
@Final
@SuppressWarnings({"DEPRECATED", "deprecation"})
private static Map<Item, Map<Identifier, ModelPredicateProvider>> ITEM_SPECIFIC;
@Shadow
@Final
private static Identifier DAMAGE_ID;
@Shadow
@Final
private static UnclampedModelPredicateProvider DAMAGE_PROVIDER;
@Shadow
@Final
private static Identifier DAMAGED_ID;
@Shadow
@Final
private static UnclampedModelPredicateProvider DAMAGED_PROVIDER;
@SuppressWarnings("deprecation")
@Shadow
@Final
private static Map<Identifier, ModelPredicateProvider> GLOBAL;

/**
 * @author Phoupraw
 * @reason
 */
@SuppressWarnings("deprecation")
@Overwrite
public static @Nullable ModelPredicateProvider get(Item item, Identifier id) {
	if (item.getMaxDamage() > 0) {
		if (DAMAGE_ID.equals(id))
			return DAMAGE_PROVIDER;
		if (DAMAGED_ID.equals(id))
			return DAMAGED_PROVIDER;
	}
	var map = ITEM_SPECIFIC.get(item);
	if (map != null) {
		var provider = map.get(id);
		if (provider != null)
			return provider;
	} else if (item instanceof RenderedAsBow){
		return ITEM_SPECIFIC.get(Items.BOW).get(id);
	}
	return GLOBAL.get(id);
}
///**
// * 让{@link CustomBowItem}的模型参数和{@link Items#BOW}相同：也传递{@code "pulling"}和{@code "pull"}参数。
// */
//@ModifyVariable(method = "get", at = @At("HEAD"))
//private static Item onGet(Item item) {
//	if (item instanceof BowItem)
//		return Items.BOW;
//	return item;
//}
}
