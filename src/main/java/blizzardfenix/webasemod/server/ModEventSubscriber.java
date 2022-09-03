package blizzardfenix.webasemod.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.commands.SetThrowableVarCommand;
import blizzardfenix.webasemod.config.ServerConfig;
import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.items.BallItem;
import blizzardfenix.webasemod.items.ItemHelperFunctions;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import net.minecraft.Util;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
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
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
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
    
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
    	ItemHelperFunctions.ITEMTAGS = ForgeRegistries.ITEMS.tags();
    	
    	// Set the ability for dispensers to shoot balls
    	ItemHelperFunctions.ITEMTAGS.getTag(ItemHelperFunctions.THROWABLEITEMTAG).forEach((item) -> {
			// Fireballs and eggs already have different dispenser behaviour. Also ignore any throwable items from other mods, as they might have different use functionality defined already.
			String namespace = ForgeRegistries.ITEMS.getKey(item).getNamespace();
    		if (item == Items.FIRE_CHARGE || item == Items.EGG || (namespace != "minecraft" && namespace != BaseballMod.MODID)
    				) 
    			return;
    		DispenserBlock.registerBehavior(item, new AbstractProjectileDispenseBehavior() {
    			protected Projectile getProjectile(Level world, Position pos, ItemStack itemstack) {
    				return Util.make(
    						itemstack.is(ItemHelperFunctions.NUGGETTAG)
							? new BouncyBallEntity(ModEntityTypes.SMALL_THROWABLE_ITEM_ENTITY.get(), world, pos.x(), pos.y(), pos.z())
							: (ItemHelperFunctions.isMediumThrowable(item)
								? new BouncyBallEntity(ModEntityTypes.MEDIUM_THROWABLE_ITEM_ENTITY.get(), world, pos.x(), pos.y(), pos.z())
								: new BouncyBallEntity(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), world, pos.x(), pos.y(), pos.z())), 
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
    	LivingEntity target = event.getEntity();
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
		Level level = event.getLevel();
		ItemStack itemStack = event.getItemStack();
		Item item = itemStack.getItem();
				
		// If the player right clicked and if either the held item is a newly made throwable item, 
		// or if the held item is a vanilla throwable and those should be overridden, then try to throw the held item.
		boolean isVanillaThrowable = itemStack.is(ItemHelperFunctions.VANILLATHROWABLETAG);
		// (Modded) eggs can be included in throwable_items, but vanilla eggs are not new throwables
		boolean isNewThrowable = (itemStack.is(ItemHelperFunctions.THROWABLEITEMTAG) && !isVanillaThrowable);
		if (isNewThrowable || (isVanillaThrowable && ServerConfig.override_vanilla_throwables.get())) {
			if (level.isClientSide()) {
				Player player = event.getEntity();

				InteractionResult result = ItemHelperFunctions.tryThrow(level, player, event.getHand(), !(item instanceof BallItem));
				if (result.consumesAction()) {
					
				}
				event.setCanceled(true);
				event.setCancellationResult(result);
			} else {
				event.setCanceled(true);
				event.setCancellationResult(InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide()).getResult());
			}
		}
	}
}
