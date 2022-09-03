package blizzardfenix.webasemod.items;

import java.util.Random;

import javax.annotation.Nullable;

import blizzardfenix.webasemod.config.ServerConfig;
import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.entity.BouncyFireBallEntity;
import blizzardfenix.webasemod.entity.PickableEggEntity;
import blizzardfenix.webasemod.entity.PickableEnderPearlEntity;
import blizzardfenix.webasemod.entity.PickableExperienceBottleEntity;
import blizzardfenix.webasemod.entity.PickablePotionEntity;
import blizzardfenix.webasemod.entity.PickableSnowballEntity;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.init.ModItems;
import blizzardfenix.webasemod.init.ModKeyBindings;
import blizzardfenix.webasemod.server.WebaseMessage;
import blizzardfenix.webasemod.server.WebasePacketHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

public class ItemHelperFunctions {
	protected static final Random random = new Random();
	public static ITagManager<Item> ITEMTAGS = ForgeRegistries.ITEMS.tags();
	public static TagKey<Item> THROWABLEITEMTAG = ITEMTAGS.createTagKey(new ResourceLocation("webasemod","throwable_items"));
	public static TagKey<Item> VANILLATHROWABLETAG = ITEMTAGS.createTagKey(new ResourceLocation("webasemod","vanilla_throwables"));
	public static TagKey<Item> NUGGETTAG = ITEMTAGS.createTagKey(new ResourceLocation("forge","nuggets"));
	
	public static float hitTime = 20;
	public static int ticksBeforeThrowing = 0;
	public static Timer timer;
	
	/**
	 * Checks if enough ticks have passed to throw again
	 * @return
	 */
	public static boolean checkThrowDelay() {
        int ticks = ItemHelperFunctions.timer.advanceTime(Util.getMillis());
        if (ItemHelperFunctions.ticksBeforeThrowing <= ticks) {
        	ItemHelperFunctions.ticksBeforeThrowing = 0;
        	return true;
        } else {
        	ItemHelperFunctions.ticksBeforeThrowing -= ticks;
        	return false;
        }
	}
	
	/**
	 * Throw an item as a throwable ball. Specifically for items that do not already have this behaviour implemented as their 'use' functionality
	 * @param world
	 * @param player
	 * @param hand
	 * @return
	 */
	public static InteractionResultHolder<ItemStack> throwBall(Level level, Player player, ItemStack itemstack, @Nullable ThrowableItemProjectile throwableentity, Boolean throwUp) {		
		SoundEvent soundEvent;
		Item item = itemstack.getItem();
		if(item == Items.ENDER_PEARL)
			soundEvent = SoundEvents.ENDER_PEARL_THROW;
		else if(item == Items.SNOWBALL)
			soundEvent = SoundEvents.SNOWBALL_THROW;
		else if(item == Items.EXPERIENCE_BOTTLE)
			soundEvent = SoundEvents.EXPERIENCE_BOTTLE_THROW;
		else if(item == Items.SPLASH_POTION)
			soundEvent = SoundEvents.SPLASH_POTION_THROW;
        else if(item == Items.LINGERING_POTION)
            soundEvent = SoundEvents.LINGERING_POTION_THROW;
		else
			soundEvent = SoundEvents.EGG_THROW;
		level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundSource.NEUTRAL, 0.5F,
				0.4F / (random.nextFloat() * 0.4F + 0.8F));
		
		if (!level.isClientSide()) {
			boolean isBouncyBall = throwableentity instanceof BouncyBallEntity;
			BouncyBallEntity bouncyBallEntity = null;
			if (isBouncyBall)
				bouncyBallEntity = (BouncyBallEntity) throwableentity;				
			throwableentity.setItem(itemstack);
			if (throwUp) {
				if (!player.getAbilities().flying) player.setDeltaMovement(player.getDeltaMovement().multiply(1,0,1)); // Don't let y velocity affect the throw
				// Aims the ball at 45 degree angle if looking straight ahead, otherwise converges to throwing straight
				float pitchrot = player.getXRot() - 0.5F * (90 - Math.abs(player.getXRot()));
				float inaccuracy = isBouncyBall ? bouncyBallEntity.baseInaccuracy + 0.5F : 1.0F;
				throwableentity.shootFromRotation(player, pitchrot, player.getYRot(), 
						item == Items.EXPERIENCE_BOTTLE ? -20.0F : 0.0F, 0.2F, inaccuracy);

				Vec3 playerVelocity = player.getDeltaMovement();
				// Add more of the player velocity so that the ball should be thrown such that it passes the center of the screen, allowing it to be easily hit.
				if (!Double.isNaN(playerVelocity.y)) {
					double playerSpeed = playerVelocity.length();
					if (playerSpeed != 0 && (!player.getAbilities().flying || playerSpeed > 0.4F)) {
						Vec3 playerVelScaled = playerVelocity.scale(
								Math.sqrt(playerSpeed*0.5F) / playerSpeed - 0.72F);
						throwableentity.setDeltaMovement(throwableentity.getDeltaMovement()
								.add(playerVelScaled.x, 
								!player.getAbilities().flying ? 0.0D : playerVelScaled.y, 
								playerVelScaled.z));
					}
				}
				// Make sure the ball knows to get collected again if it isn't hit by the player
				if (isBouncyBall) bouncyBallEntity.thrownUp = true;
				else if (throwableentity instanceof PickableEggEntity) ((PickableEggEntity) throwableentity).returnToInventory = true;
				else if (throwableentity instanceof PickableEnderPearlEntity) ((PickableEnderPearlEntity) throwableentity).returnToInventory = true;
				else if (throwableentity instanceof PickableExperienceBottleEntity) ((PickableExperienceBottleEntity) throwableentity).returnToInventory = true;
				else if (throwableentity instanceof PickablePotionEntity) ((PickablePotionEntity) throwableentity).returnToInventory = true;
				else if (throwableentity instanceof PickableSnowballEntity) ((PickableSnowballEntity) throwableentity).returnToInventory = true;
			} else {
				float inaccuracy = isBouncyBall ? bouncyBallEntity.baseInaccuracy + 0.5F : 1.0F;
				float speed = 	player.isCrouching() ? 0.3F : 
								isBouncyBall ? bouncyBallEntity.throwSpeed : 
								item == Items.EXPERIENCE_BOTTLE ? 0.7F : 
                                item == Items.SPLASH_POTION || item == Items.LINGERING_POTION ? 0.5F : 
								1.5F;
				throwableentity.shootFromRotation(player, player.getXRot(), player.getYRot(), 
						item == Items.EXPERIENCE_BOTTLE ? -20.0F : 0.0F, speed, inaccuracy);
			}
			level.addFreshEntity(throwableentity);
		}

		player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
		if (!player.getAbilities().instabuild) {
			itemstack.shrink(1);
		}
		
		return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
	}
	
	public static InteractionResult throwVanillaItem(Level level, Player player, InteractionHand hand, Boolean throwUp, ItemStack itemstack) {
		Item item = itemstack.getItem();
		if (ServerConfig.override_vanilla_throwables.get()) {
			ThrowableItemProjectile itementity = null;
			if (!level.isClientSide) {
				if(item == Items.ENDER_PEARL)
					itementity = new PickableEnderPearlEntity(level, player);
				else if(item == Items.EGG)
					itementity = new PickableEggEntity(level, player);
				else if(item == Items.EXPERIENCE_BOTTLE)
					itementity = new PickableExperienceBottleEntity(level, player);
	            else if(item == Items.SPLASH_POTION || item == Items.LINGERING_POTION)
					itementity = new PickablePotionEntity(level, player);
				else
					itementity = new PickableSnowballEntity(level, player);
			}
			return ItemHelperFunctions.throwBall(level, player, itemstack, itementity, throwUp).getResult();
		} else {
			return item.use(level, player, hand).getResult();
		}
	}
	
	public static boolean isMediumThrowable(Item item) {
		return item == ModItems.BASEBALL_CORE.get() || item == ModItems.CORK.get() || item == ModItems.GOLFBALL.get();
	}
	
	public static InteractionResult throwNewThrowableItem(Level level, Player player, InteractionHand hand, Boolean throwUp, ItemStack itemstack) {
		BouncyBallEntity throwableentity = null;
		if (!level.isClientSide) {
			Item item = itemstack.getItem();
			if (item == Items.FIRE_CHARGE) {
				throwableentity = new BouncyFireBallEntity(ModEntityTypes.BOUNCY_FIREBALL_ENTITY.get(), level, player);
			} else if (itemstack.is(ItemHelperFunctions.NUGGETTAG)) {
				throwableentity = new BouncyBallEntity(ModEntityTypes.SMALL_THROWABLE_ITEM_ENTITY.get(), level, player);
			} else if (ItemHelperFunctions.isMediumThrowable(item)) {
				throwableentity = new BouncyBallEntity(ModEntityTypes.MEDIUM_THROWABLE_ITEM_ENTITY.get(), level, player);
			} else
				throwableentity = new BouncyBallEntity(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), level, player);
		}
		return ItemHelperFunctions.throwBall(level, player, itemstack, throwableentity, throwUp).getResult();
	}

	public static InteractionResult throwBallUp(Level level, Player player, InteractionHand hand, ItemStack throwableItem) {
		if (throwableItem.is(ItemHelperFunctions.VANILLATHROWABLETAG)) {
			return ItemHelperFunctions.throwVanillaItem(level, player, hand, true, throwableItem);
		} else if (throwableItem.is(ItemHelperFunctions.THROWABLEITEMTAG)) {
			return ItemHelperFunctions.throwNewThrowableItem(level, player, hand, true, throwableItem);
		}
		return InteractionResult.PASS;
	}
	
	public static InteractionResult useItem(Level level, Player player, InteractionHand hand, ItemStack itemstack) {
		InteractionResultHolder<ItemStack> interactionresultholder = InteractionResultHolder.pass(itemstack);
		Item item = itemstack.getItem();
		if (!(item instanceof BallItem)) {// Code adapted from the vanilla minecraft way of calling itemstack.use on the client & server
			interactionresultholder = itemstack.use(level, player, hand);
			ItemStack newItemStack = interactionresultholder.getObject();
			if (!level.isClientSide()) {
				int i = itemstack.getCount();
				int j = itemstack.getDamageValue();
				
				if (!(newItemStack == itemstack && newItemStack.getCount() == i && newItemStack.getUseDuration() <= 0 && newItemStack.getDamageValue() == j) && 
						!(interactionresultholder.getResult() == InteractionResult.FAIL && newItemStack.getUseDuration() > 0 && !player.isUsingItem())) {
					if (itemstack != newItemStack) {
						player.setItemInHand(hand, newItemStack);
					}

					if (player.isCreative()) {
						newItemStack.setCount(i);
						if (newItemStack.isDamageableItem() && newItemStack.getDamageValue() != j) {
							newItemStack.setDamageValue(j);
						}
					}

					if (newItemStack.isEmpty()) {
						player.setItemInHand(hand, ItemStack.EMPTY);
					}

					if (!player.isUsingItem()) {
						player.inventoryMenu.sendAllDataToRemote();
					}
				}
			} else {
				if (newItemStack != itemstack) {
					player.setItemInHand(hand, newItemStack);
					if (newItemStack.isEmpty())
						net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, itemstack, hand);
				}
			}
		}
		return interactionresultholder.getResult();
	}

	public static InteractionResult tryThrow(Level level, Player player, InteractionHand hand, Boolean tryUse) {
		ItemStack itemstack = player.getItemInHand(hand);
		if (!itemstack.isEmpty()) {
			boolean isExistingThrowable = itemstack.is(ItemHelperFunctions.VANILLATHROWABLETAG);
			if (itemstack.is(ItemHelperFunctions.THROWABLEITEMTAG) || isExistingThrowable) {
				InteractionResult result;
				// If did not use the item, and the throw key is bound to the use key if we right clicked the item, it's okay to throw. (Also check the throw delay)
				boolean throwNew = ((!tryUse || ModKeyBindings.throwKey.getKey() == Minecraft.getInstance().options.keyUse.getKey()));
				boolean okToThrow = (throwNew || isExistingThrowable) && (!level.isClientSide || ItemHelperFunctions.checkThrowDelay());
				if (okToThrow) {
					if (level.isClientSide) {
						// Tell the server to perform the throw as well. Necessary because the server doesn't know if the throw key is the same as the use key.
						WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(hand, player.getDeltaMovement(), false, tryUse));
					}	
				}
				if (isExistingThrowable) {
					if (okToThrow) {
						result = ItemHelperFunctions.throwVanillaItem(level, player, hand, false, itemstack);
						if (result.consumesAction() && level.isClientSide) {
							ItemHelperFunctions.ticksBeforeThrowing = ServerConfig.throwCooldown.get();
						}
					} else {
						result = InteractionResult.FAIL;
					}
				} else {
					// Try to just use the item first. If this does not consume the action, continue throwing
					InteractionResult interactionresult = ItemHelperFunctions.useItem(level, player, hand, itemstack);
					
					if (interactionresult.consumesAction() || !okToThrow) {
						result = interactionresult;
					} else {
						result = ItemHelperFunctions.throwNewThrowableItem(level, player, hand, false, itemstack);
						if (result.consumesAction() && level.isClientSide) {
							ItemHelperFunctions.ticksBeforeThrowing = ServerConfig.throwCooldown.get();
						}
					}
				}
	
				if(result.consumesAction() && result.shouldSwing())
				{
					if (level.isClientSide()) {
						player.swing(hand);
					}
					else
						player.swing(hand, true);
				}
				return result;					
			}
		}
		return InteractionResult.PASS;
	}
}
