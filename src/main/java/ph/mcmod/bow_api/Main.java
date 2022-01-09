package ph.mcmod.bow_api;

import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.models.JModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.explosion.Explosion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class Main {
static final class Test {
	public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create(new Identifier(NAMESPACE, "runtime"));
	public static final Item CREEPER_BOW = Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "creeper_bow"), new SimpleBowItem(new BowSettings()
	  .group(ItemGroup.COMBAT)
	  .maxDamage(450)
	  .setPullTicks(10)
	  .addAfterDamage(BowSettings.explodeAfterDamage(1, false, Explosion.DestructionType.NONE))
	  .addAfterDamage((host, entityHitResult, damage, damageSource) -> {
		  var pos = host.getPos().add(entityHitResult.getPos()).multiply(0.5);
		  var fireworkStack = Items.FIREWORK_ROCKET.getDefaultStack();
		  {
			  var fireworks = new NbtCompound();
			  {
				  fireworks.putByte("Flight", (byte) 0);
				  var explosions = new NbtList();
				  {
					  var explosion = new NbtCompound();
					  {
						  explosion.putByte("Type", (byte) FireworkRocketItem.Type.CREEPER.getId());
						  int color0 = DyeColor.GREEN.getFireworkColor();
						  var colors0 = new int[]{color0, color0};
						  var colors = new NbtIntArray(colors0);
						  explosion.put("Colors", colors);
					  }
					  explosions.add(explosion);
				  }
				  fireworks.put("Explosions", explosions);
			  }
			  fireworkStack.setSubNbt("Fireworks", fireworks);
		  }
		  var r = new FireworkRocketEntity(host.getWorld(), host.getOwner() instanceof Entity owner ? owner : null, pos.x, pos.y, pos.z, fireworkStack);
		  NbtCompound nbt = r.writeNbt(new NbtCompound());
		  nbt.putInt("LifeTime", 0);
		  r.readNbt(nbt);
		  host.getWorld().spawnEntity(r);
	  })
	));
	public static final Item ENDER_PEARL_BOW = Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "ender_pearl_bow"), new SimpleBowItem(new BowSettings()
	  .group(ItemGroup.COMBAT)
	  .maxDamage(450)
	  .setPullTicks(10)
	  .addFinallyModify(BowSettings.changeEntity(EntityType.ENDER_PEARL))
	));

	static {
		RESOURCE_PACK.addModel(JModel.model("item/bow"), new Identifier(NAMESPACE, "item/creeper_bow"));
		RESOURCE_PACK.addModel(JModel.model("item/bow"), new Identifier(NAMESPACE, "item/ender_pearl_bow"));
		RRPCallback.AFTER_VANILLA.register(resources -> resources.add(RESOURCE_PACK));
//		System.out.println(CREEPER_BOW);
//		System.out.println(ENDER_PEARL_BOW);
	}

	static void init() {
//		EntityRendererRegistry.INSTANCE.register(CreeperArrowEntity.ENTITY_TYPE, ArrowEntityRenderer::new);


	}

//	static class CreeperArrowEntity extends ListenableArrowEntity {
//		public static final EntityType<CreeperArrowEntity> ENTITY_TYPE = register(new Identifier(NAMESPACE, "creeper_arrow_entity"), CreeperArrowEntity::new);
//
//		{
//			afterHit.add((arrowEntity, hitResult) -> {
//				Vec3d pos = hitResult.getPos().add(arrowEntity.getPos()).multiply(0.5);
//				arrowEntity.world.createExplosion(arrowEntity.getOwner(), pos.x, pos.y, pos.z, 3f, Explosion.DestructionType.NONE);
//				arrowEntity.discard();
//			});
//		}
//
//		public CreeperArrowEntity(EntityType<? extends CreeperArrowEntity> entityType, World world) {
//			super(entityType, world);
//		}
//	}
}


public static final String NAMESPACE = "bow_api";
public static final Logger LOGGER = LogManager.getLogger(NAMESPACE);

public static void init() {
//	Test.init();

}

public static <T extends Entity> EntityType<T> register(Identifier id, EntityType.EntityFactory<T> constructor) {
	return Registry.register(Registry.ENTITY_TYPE, id, EntityType.Builder.create(constructor, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build(id.toString()));
}

public static void assignFields(Object source, Object target, Class<?> cls) {
	while (!cls.isInstance(source) || !cls.isInstance(target))
		cls = cls.getSuperclass();
	for (Field field : cls.getDeclaredFields()) {
		if (Modifier.isStatic(field.getModifiers()))
			continue;
		field.setAccessible(true);
		try {
			Object value = field.get(source);
			field.set(target, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	if (cls.getSuperclass() != null)
		assignFields(source, target, cls.getSuperclass());
}

}
