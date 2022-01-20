package ph.mcmod.bow_api;

import net.minecraft.client.render.entity.ArrowEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.Identifier;

public class EArrowEntityRenderer<T extends ArrowEntity> extends ProjectileEntityRenderer<T> {
protected final ArrowEntityRenderer arrowEntityRenderer;

public EArrowEntityRenderer(EntityRendererFactory.Context context) {
	super(context);
	arrowEntityRenderer = new ArrowEntityRenderer(context);
}

@Override
public Identifier getTexture(T arrowEntity) {
	return arrowEntityRenderer.getTexture(arrowEntity);
}
}
