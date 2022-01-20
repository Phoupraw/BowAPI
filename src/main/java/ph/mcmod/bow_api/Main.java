package ph.mcmod.bow_api;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ph.mcmod.bow_api.util.JavaUtil;

public final class Main {

public static final String NAMESPACE = "bow_api";
public static final Logger LOGGER = LogManager.getLogger(NAMESPACE);

public static void init() {

}

public static <T extends ArrowEntity> SimpleEntityType<T> register(Identifier id, EntityType.EntityFactory<T> constructor) {
	var type = Registry.register(Registry.ENTITY_TYPE, id, SimpleEntityType.builder(constructor).copyFrom(EntityType.ARROW).build());
	EntityRendererRegistry.register(type, EArrowEntityRenderer::new);
	return type;
}

public static void print(Object obj) {
	try {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) {
			LOGGER.info(obj);
		} else {
			player.sendMessage(new LiteralText(JavaUtil.toString(obj)), false);
		}
	} catch (Throwable e) {
		e.printStackTrace();
	}
}
/**
 * 打印到聊天栏（仅客户端）
 *
 * @see Main#print(Object)
 */
public static void print(Object... objects) {
	String s = JavaUtil.toString(objects);
	Main.print(s.substring(1, s.length() - 1));
}
}
