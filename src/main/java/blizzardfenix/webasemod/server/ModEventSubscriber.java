package blizzardfenix.webasemod.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.commands.SetThrowableVarCommand;
import blizzardfenix.webasemod.config.ServerConfig;
import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.items.BaseballItem;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import blizzardfenix.webasemod.util.HelperFunctions;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.World;
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
		ITagCollection<Item> itemtags = ItemTags.getAllTags();
		ITag<Item> throwableItems = itemtags.getTag(new ResourceLocation("webasemod", "throwable_items"));
    	
    	// Set the ability for dispensers to shoot balls
		throwableItems.getValues().forEach((item) -> {
			// Fireballs and eggs already have different dispenser behaviour. Also ignore any throwable items from other mods, as they might have different use functionality defined already.
			String namespace = ForgeRegistries.ITEMS.getKey(item).getNamespace();
    		if (item == Items.FIRE_CHARGE || item == Items.EGG || (namespace != "minecraft" && !BaseballMod.MODID.equals(namespace))) return;
    		DispenserBlock.registerBehavior(item, new ProjectileDispenseBehavior() {
    			protected ProjectileEntity getProjectile(World world, IPosition pos, ItemStack itemstack) {
    				return Util.make(
    						itemtags.getTag(new ResourceLocation("forge","nuggets")).contains(itemstack.getItem())
							? new BouncyBallEntity(ModEntityTypes.SMALL_THROWABLE_ITEM_ENTITY.get(), world, pos.x(), pos.y(), pos.z())
							: new BouncyBallEntity(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), world, pos.x(), pos.y(), pos.z()), 
						(ballEntity) -> {
							ballEntity.setItem(itemstack);
							ballEntity.comboDmg += 0.125F * 4 + ballEntity.batHitDmg; // Equivalent to hitting with stone bat
							ballEntity.hitbybat = true;
							ballEntity.pickupStatus = AbstractArrowEntity.PickupStatus.ALLOWED;
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
		World level = event.getWorld();
		ItemStack itemStack = event.getItemStack();
		Item item = itemStack.getItem();
		ITagCollection<Item> tags = ItemTags.getAllTags();
		
		// If the player right clicked and if either the held item is a newly made throwable item and the throw key is set to right click, or if the held item is a vanilla throwable, then try to throw the held item.
		// BaseballItems handle throwing themselves through Item.use()
		boolean isVanillaThrowable = tags.getTag(new ResourceLocation("webasemod", "vanilla_throwables")).contains(item);
		boolean isNewThrowable = (tags.getTag(new ResourceLocation("webasemod", "throwable_items")).contains(item) && !(item instanceof BaseballItem) && !isVanillaThrowable);
		if (isNewThrowable || (isVanillaThrowable && ServerConfig.override_vanilla_throwables.get())) {
			if (level.isClientSide()) {
				if (true) {// Make sure the throwkey is pressed when it is a new throwable
					PlayerEntity player = event.getPlayer();
                    Hand hand = event.getHand();

					ActionResultType result = HelperFunctions.tryThrow(level, player, event.getHand(), player.getDeltaMovement(), Settings.throwUp, true);
					if (result.consumesAction()) {
						// If the throw was successful, tell the server to perform the throw as well. Necessary because the server doesn't know if the throw key is the same as the use key.
						WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(event.getHand(), player.getDeltaMovement(), Settings.throwUp, true));
						
					}
					event.setCanceled(true);
					event.setCancellationResult(result);
				}
			} else {
				event.setCanceled(true);
				event.setCancellationResult(ActionResult.sidedSuccess(itemStack, level.isClientSide()).getResult());
			}
		}
	}
}
