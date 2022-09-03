package blizzardfenix.webasemod.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.init.ModKeyBindings;
import blizzardfenix.webasemod.renderer.CenteredDragonFireBallRenderer;
import blizzardfenix.webasemod.renderer.CenteredSpriteRenderer;
import blizzardfenix.webasemod.renderer.EmptyRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = BaseballMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEventSubscriber {

	private static final Logger LOGGER = LogManager.getLogger(BaseballMod.MODID + " Client Mod Event Subscriber");

	@SubscribeEvent
	public static void onRegisterRenderers(final RegisterKeyMappingsEvent event) {
		event.register(ModKeyBindings.throwKey);
	}

	@SubscribeEvent
	public static void onRegisterRenderers(final RegisterRenderers event) {
		// Register Entity Renderers
		event.registerEntityRenderer(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
		event.registerEntityRenderer(ModEntityTypes.SMALL_THROWABLE_ITEM_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
		event.registerEntityRenderer(ModEntityTypes.MEDIUM_THROWABLE_ITEM_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
		event.registerEntityRenderer(ModEntityTypes.BOUNCY_FIREBALL_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0, 0.75F, true));
		event.registerEntityRenderer(ModEntityTypes.MOCKARROW_ENTITY.get(), manager -> new EmptyRenderer<>(manager));
			
			event.registerEntityRenderer(EntityType.EGG, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0.03F, 1.0F, false));
			event.registerEntityRenderer(EntityType.ENDER_PEARL, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			event.registerEntityRenderer(EntityType.EXPERIENCE_BOTTLE, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0.1F, 1.0F, false));
			event.registerEntityRenderer(EntityType.EYE_OF_ENDER, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0, 1.0F, true));
			event.registerEntityRenderer(EntityType.FIREBALL, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0, 3.0F, true));
			event.registerEntityRenderer(EntityType.POTION, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0.1F, 1.0F, false));
			event.registerEntityRenderer(EntityType.SMALL_FIREBALL, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0, 0.75F, true));
			event.registerEntityRenderer(EntityType.SNOWBALL, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			event.registerEntityRenderer(ModEntityTypes.PICKABLE_SNOWBALL_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			event.registerEntityRenderer(ModEntityTypes.PICKABLE_EGG_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			event.registerEntityRenderer(ModEntityTypes.PICKABLE_ENDER_PEARL_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			event.registerEntityRenderer(ModEntityTypes.PICKABLE_POTION_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			event.registerEntityRenderer(ModEntityTypes.PICKABLE_EXPERIENCE_BOTTLE_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			
			event.registerEntityRenderer(EntityType.DRAGON_FIREBALL, manager -> new CenteredDragonFireBallRenderer(manager));
	}
}
