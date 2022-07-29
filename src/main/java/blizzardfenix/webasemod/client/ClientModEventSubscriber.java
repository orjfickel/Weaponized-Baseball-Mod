package blizzardfenix.webasemod.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.config.ClientConfig;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.init.ModItems;
import blizzardfenix.webasemod.init.ModKeyBindings;
import blizzardfenix.webasemod.renderer.CenteredDragonFireBallRenderer;
import blizzardfenix.webasemod.renderer.CenteredSpriteRenderer;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = BaseballMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEventSubscriber {

	private static final Logger LOGGER = LogManager.getLogger(BaseballMod.MODID + " Client Mod Event Subscriber");

	@SubscribeEvent
	public static void onFMLClientSetup(final FMLClientSetupEvent event) {
		// Register key bindings
		ClientRegistry.registerKeyBinding(ModKeyBindings.throwUpToggleKey);
		ClientRegistry.registerKeyBinding(ModKeyBindings.throwKey);

		// Register Entity Renderers
		RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
		RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.SMALL_THROWABLE_ITEM_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
		RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.BOUNCY_FIREBALL_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0, 0.75F, true));
			
		if (ClientConfig.sprite_fix.get()) {
			RenderingRegistry.registerEntityRenderingHandler(EntityType.EGG, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0.03F, 1.0F, false));
			RenderingRegistry.registerEntityRenderingHandler(EntityType.ENDER_PEARL, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			RenderingRegistry.registerEntityRenderingHandler(EntityType.EXPERIENCE_BOTTLE, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0.1F, 1.0F, false));
			RenderingRegistry.registerEntityRenderingHandler(EntityType.EYE_OF_ENDER, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0, 1.0F, true));
			RenderingRegistry.registerEntityRenderingHandler(EntityType.FIREBALL, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0, 3.0F, true));
			RenderingRegistry.registerEntityRenderingHandler(EntityType.POTION, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0.1F, 1.0F, false));
			RenderingRegistry.registerEntityRenderingHandler(EntityType.SMALL_FIREBALL, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer(), 0, 0.75F, true));
			RenderingRegistry.registerEntityRenderingHandler(EntityType.SNOWBALL, manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.PICKABLE_SNOWBALL_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.PICKABLE_EGG_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.PICKABLE_ENDER_PEARL_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.PICKABLE_POTION_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.PICKABLE_EXPERIENCE_BOTTLE_ENTITY.get(), manager -> new CenteredSpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
			
			RenderingRegistry.registerEntityRenderingHandler(EntityType.DRAGON_FIREBALL, manager -> new CenteredDragonFireBallRenderer(manager));
		}

		// Make sure baseballs show an indication of how they are going to be thrown
		ItemModelsProperties.register((ModItems.WOODEN_BASEBALL_BAT.get()), new ResourceLocation("throwindication"), ClientModEventSubscriber::setThrowUp);
		ItemModelsProperties.register((ModItems.STONE_BASEBALL_BAT.get()), new ResourceLocation("throwindication"), ClientModEventSubscriber::setThrowUp);
		ItemModelsProperties.register((ModItems.GOLDEN_BASEBALL_BAT.get()), new ResourceLocation("throwindication"), ClientModEventSubscriber::setThrowUp);
		ItemModelsProperties.register((ModItems.IRON_BASEBALL_BAT.get()), new ResourceLocation("throwindication"), ClientModEventSubscriber::setThrowUp);
		ItemModelsProperties.register((ModItems.DIAMOND_BASEBALL_BAT.get()), new ResourceLocation("throwindication"), ClientModEventSubscriber::setThrowUp);
		ItemModelsProperties.register((ModItems.NETHERITE_BASEBALL_BAT.get()), new ResourceLocation("throwindication"), ClientModEventSubscriber::setThrowUp);
		
		LOGGER.info("Registered clientside stuff");
	}
	
	static float setThrowUp(ItemStack itemStack, ClientWorld world, LivingEntity entity) {
		if (entity != null && entity instanceof ClientPlayerEntity) {
			if (Settings.throwUp && entity.getMainHandItem() == itemStack && !(entity.swinging && entity.swingingArm == Hand.MAIN_HAND))
				return 1.0F;
		}
		return 0.0F;
	}
}
