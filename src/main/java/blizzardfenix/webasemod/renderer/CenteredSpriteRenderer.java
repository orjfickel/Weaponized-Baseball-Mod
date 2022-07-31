package blizzardfenix.webasemod.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CenteredSpriteRenderer<T extends Entity & ItemSupplier> extends ThrownItemRenderer<T> {
	protected final ItemRenderer itemRenderer;
	protected final float scale;
	protected final float offset;

	public CenteredSpriteRenderer(EntityRendererProvider.Context manager, ItemRenderer itemRenderer, float offset, float scale,
			boolean fullBright) {
		super(manager, scale, fullBright);
	    this.itemRenderer = manager.getItemRenderer();
		this.scale = scale;
		this.offset = offset;
	}

	public CenteredSpriteRenderer(EntityRendererProvider.Context manager, ItemRenderer itemRenderer) {
		this(manager, itemRenderer, 0.0F, 1.0F, false);
	}

	/**
	 * Adapted from {@link SpriteRenderer.render} in order to be able to insert the translations in between.
	 */
	@Override
	public void render(T entity, float number1, float number2, PoseStack matrixStack, MultiBufferSource renderBuffer, int number3) {
		if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D)) {
			matrixStack.pushPose();
			matrixStack.scale(this.scale, this.scale, this.scale);
			// Translating up to correct when viewing from the side
			matrixStack.translate(0, -this.offset*0.07 + entity.getBbHeight() / (2 * this.scale), 0);
			matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
			matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
			// Translate it along the coordinate system that looks at the camera to correct when viewing from the top or bottom
			matrixStack.translate(0, -0.125 + this.offset, 0);
			this.itemRenderer.renderStatic(entity.getItem(), ItemTransforms.TransformType.GROUND, number3, OverlayTexture.NO_OVERLAY, matrixStack,
					renderBuffer, entity.getId());
			matrixStack.popPose();
			// We cannot call super.super so we must create a separate method to do it ourselves
			superEntityRender(entity, number1, number2, matrixStack, renderBuffer, number3);
		}
	}

	/**
	 * Adapted from {@link EntityRenderer#render} because it is obscured by
	 * SpriteRenderer
	 * 
	 * @param entity
	 * @param number1
	 * @param number2
	 * @param matrixStack
	 * @param renderBuffer
	 * @param number3
	 */
	public void superEntityRender(T entity, float number1, float number2, PoseStack matrixStack, MultiBufferSource renderBuffer, int number3) {
		net.minecraftforge.client.event.RenderNameplateEvent renderNameplateEvent = new net.minecraftforge.client.event.RenderNameplateEvent(entity,
				entity.getDisplayName(), this, matrixStack, renderBuffer, number3, number2);
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
		if (renderNameplateEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY
				&& (renderNameplateEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.shouldShowName(entity))) {
			this.renderNameTag(entity, renderNameplateEvent.getContent(), matrixStack, renderBuffer, number3);
		}
	}
}
