package ph.mcmod.bow_api_test;

import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.models.JModel;
import net.minecraft.entity.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import ph.mcmod.bow_api.BowSettings;
import ph.mcmod.bow_api.SimpleBowItem;
import ph.mcmod.bow_api.exp.TemplateBowBuilder;
import ph.mcmod.bow_api.exp.TemplateBowBuilder.TemplateBowItem;

public class Test {
static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create(new Identifier(ph.mcmod.bow_api_test.Main.NAMESPACE, "runtime"));
static final TemplateBowItem CREEPER_BOW = new TemplateBowBuilder(new Identifier(ph.mcmod.bow_api_test.Main.NAMESPACE, "creeper_bow"))
  .setRuntimeResourcePack(RESOURCE_PACK)
  .setExplosive(3)
  .setBowSettings(new BowSettings()
	.maxDamage(512)
	.setPullTicks(10)
	.group(ItemGroup.COMBAT))
  .build();
static final SimpleBowItem POWERED_CREEPER_BOW = new TemplateBowBuilder(new Identifier(ph.mcmod.bow_api_test.Main.NAMESPACE, "powered_creeper_bow"))
  .setRuntimeResourcePack(RESOURCE_PACK)
  .setExplosive(6)
  .setBowSettings(new BowSettings()
	.maxDamage(512)
	.setPullTicks(10)
	.group(ItemGroup.COMBAT))
  .build();

static final TemplateBowItem LIGHTNING_BOW = new TemplateBowBuilder(new Identifier(ph.mcmod.bow_api_test.Main.NAMESPACE, "lightning_bow"))
  .setRuntimeResourcePack(RESOURCE_PACK)
  .setLightning()
  .setParticle(new DustParticleEffect(new Vec3f(0, 0, 0.5f), 0.8f))
  .setBowSettings(new BowSettings()
	.maxDamage(512)
	.setPullTicks(10)
	.group(ItemGroup.COMBAT))
  .build();
static final TemplateBowItem ENDER_PEARL_BOW = new TemplateBowBuilder(new Identifier(ph.mcmod.bow_api_test.Main.NAMESPACE, "ender_pearl_bow"))
  .setRuntimeResourcePack(RESOURCE_PACK)
  .setTransferOnShoot(EntityType.ENDER_PEARL)
  .setBowSettings(new BowSettings()
	.maxDamage(512)
	.setPullTicks(10)
	.group(ItemGroup.COMBAT))
  .build();

static {
	RRPCallback.AFTER_VANILLA.register(resources -> resources.add(RESOURCE_PACK));
//	AccessPersistentProjectileEntity.
}

static <T extends Item> T register(String path, T item) {
	var id = new Identifier(ph.mcmod.bow_api_test.Main.NAMESPACE, path);
	RESOURCE_PACK.addModel(JModel.model("item/bow"), new Identifier(ph.mcmod.bow_api_test.Main.NAMESPACE, "item/" + path));
	return Registry.register(Registry.ITEM, new Identifier(Main.NAMESPACE, path), item);
}

public static void init() {
//		EntityRendererRegistry.INSTANCE.register(CreeperArrowEntity.ENTITY_TYPE, ArrowEntityRenderer::new);


}
}
