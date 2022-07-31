package blizzardfenix.webasemod.renderer;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class EmptyRenderer<T extends Entity> extends EntityRenderer<T> {

	public EmptyRenderer(Context c) {
		super(c);
	}
	
	@Override
	public boolean shouldRender(T entity, Frustum frustrum, double a, double b, double c) {
		return false;
	}

	@Override
	public ResourceLocation getTextureLocation(T p_114482_) {
		return null;
	}

}
