package blizzardfenix.webasemod.renderer;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class EmptyRenderer<T extends Entity> extends EntityRenderer<T> {

	public EmptyRenderer(EntityRendererManager c) {
		super(c);
	}
	
	@Override
	public boolean shouldRender(T entity, ClippingHelper frustrum, double a, double b, double c) {
		return false;
	}

	@Override
	public ResourceLocation getTextureLocation(T p_114482_) {
		return null;
	}

}
