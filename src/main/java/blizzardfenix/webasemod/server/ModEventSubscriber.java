package blizzardfenix.webasemod.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.commands.SetThrowableVarCommand;
import blizzardfenix.webasemod.config.ServerConfig;
import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.init.ModKeyBindings;
import blizzardfenix.webasemod.items.BaseballItem;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import blizzardfenix.webasemod.util.HelperFunctions;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import net.minecraftforge.server.command.ConfigCommand;

@EventBusSubscriber(modid = BaseballMod.MODID)
public class ModEventSubscriber {

	private static final Logger LOGGER = LogManager.getLogger(BaseballMod.MODID + " Mod Event Subscriber");
    @SubscribeEvent
    public static void onCommandsRegisterEvent(RegisterCommandsEvent event) {
		SetThrowableVarCommand.setThrowableVarCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
		LOGGER.info("Registered commands");
    }
	
	static int throwDelay = 0;
	static Timer timer = new Timer(20.0F, 0L);

	@SubscribeEvent
	public static void onClientTick(final TickEvent.ClientTickEvent event) {
		// If the throw key is pressed, throw what you're holding in your main hand and otherwise your off hand
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null && event.side == LogicalSide.CLIENT && ModKeyBindings.throwKey.isDown() && 
				ModKeyBindings.throwKey.getKey() != mc.options.keyUse.getKey() && !mc.options.keyUse.isDown()) {// Use button overrides the throw button
			// Check if enough ticks have passed since the last throw
	        int ticks = timer.advanceTime(Util.getMillis());
	        if (throwDelay <= ticks) {
	        	throwDelay = 0;
				for (InteractionHand hand : InteractionHand.values()) {
					InteractionResult result = HelperFunctions.tryThrow(mc.level, mc.player, hand, mc.player.getDeltaMovement());							
					if (result.consumesAction()) {
						throwDelay = 4;
						mc.gameRenderer.itemInHandRenderer.itemUsed(hand);
						
						// If the throw was successful, tell the server to perform the throw as well
						WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(hand,mc.player.getDeltaMovement()));
						return;
					}
				}
	        } else {
	        	throwDelay -= ticks;
	        }
		}
	}
    
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
		ITagManager<Item> itemtags = ForgeRegistries.ITEMS.tags();
		TagKey<Item> throwableItems = itemtags.createTagKey(new ResourceLocation("webasemod", "throwable_items"));
		
    	
    	// Set the ability for dispensers to shoot balls
		itemtags.getTag(throwableItems).forEach((item) -> {
    		if (item == Items.FIRE_CHARGE || item == Items.EGG) return;// Fireballs are the only throwable item that already have different dispenser behaviour
    		DispenserBlock.registerBehavior(item, new AbstractProjectileDispenseBehavior() {
    			protected Projectile getProjectile(Level world, Position pos, ItemStack itemstack) {
    				return Util.make(
    						itemtags.getTag(itemtags.createTagKey(new ResourceLocation("forge","nuggets"))).contains(itemstack.getItem())
							? new BouncyBallEntity(ModEntityTypes.SMALL_THROWABLE_ITEM_ENTITY.get(), world, pos.x(), pos.y(), pos.z())
							: new BouncyBallEntity(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), world, pos.x(), pos.y(), pos.z()), 
						(ballEntity) -> {
							ballEntity.setItem(itemstack);
							ballEntity.comboDmg += 0.125F * 4 + ballEntity.batHitDmg; // Equivalent to hitting with stone bat
							ballEntity.hitbybat = true;
							ballEntity.Pickup = AbstractArrow.Pickup.ALLOWED;
    				});
    			}
    		});
    	});
    }
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
    	LivingEntity target = event.getEntityLiving();
		// Apply extra knockback if the target was hit with a bat
		// (There don't seem to be any hooks for when an item is used to hit something that aren't limited to player attacks)
		Entity sourceEntity = event.getSource().getEntity();
		if (!(sourceEntity instanceof LivingEntity))
			return;
		LivingEntity attacker = (LivingEntity) sourceEntity;
		Item item = attacker.getMainHandItem().getItem();
		if(item instanceof BaseballBat && target.invulnerableTime == 20) { // Otherwise the entity would not normally have knockback applied in the hurt function
			target.knockback(0.3F * ((BaseballBat)item).getDamage() - 0.2F, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
		}
    }

	@SubscribeEvent
	public static void onRightClickItemEvent(final PlayerInteractEvent.RightClickItem event) {
		Level level = event.getWorld();
		ItemStack itemStack = event.getItemStack();
		Item item = itemStack.getItem();
		Player player = event.getPlayer();
		ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
		
		// If the player right clicked and if either the held item is a newly made throwable item and the throw key is set to the use key, 
		// or if the held item is a vanilla throwable and those should be overridden, then try to throw the held item.
		// BaseballItems handle throwing themselves through Item.use()
		if ((ModKeyBindings.throwKey.getKey() == Minecraft.getInstance().options.keyUse.getKey() && tags.getTag(tags.createTagKey(new ResourceLocation("webasemod", "throwable_items"))).contains(item) && 
				!(item instanceof BaseballItem) && item != Items.EGG) || 
				(tags.getTag(tags.createTagKey(new ResourceLocation("webasemod", "vanilla_throwables"))).contains(item) && ServerConfig.override_vanilla_throwables.get())) {
			if (level.isClientSide()) {
				InteractionResult result = HelperFunctions.tryThrow(level, player, event.getHand(), player.getDeltaMovement());
				if (result.consumesAction()) {
					// If the throw was successful, tell the server to perform the throw as well
					WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(event.getHand(), player.getDeltaMovement()));
					
					event.setCanceled(true);
					event.setCancellationResult(result);
				}
			} else {
				event.setCanceled(true);
				event.setCancellationResult(InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide()).getResult());
			}
		}
	}
}
