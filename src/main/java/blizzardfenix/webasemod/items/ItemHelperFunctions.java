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
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags.IOptionalNamedTag;

public class ItemHelperFunctions {
	protected static final Random random = new Random();
	public static IOptionalNamedTag<Item> THROWABLEITEMTAG() {return ItemTags.createOptional(new ResourceLocation("webasemod","throwable_items"));}
	public static IOptionalNamedTag<Item> VANILLATHROWABLETAG() {return ItemTags.createOptional(new ResourceLocation("webasemod","vanilla_throwables"));}
	public static IOptionalNamedTag<Item> NUGGETTAG() {return ItemTags.createOptional(new ResourceLocation("forge","nuggets"));}
	
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
	public static ActionResult<ItemStack> throwBall(World level, PlayerEntity player, ItemStack itemstack, @Nullable ProjectileItemEntity throwableentity, Boolean throwUp) {		
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
		level.playSound((PlayerEntity) null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundCategory.NEUTRAL, 0.5F,
				0.4F / (random.nextFloat() * 0.4F + 0.8F));
		
		if (!level.isClientSide()) {
			boolean isBouncyBall = throwableentity instanceof BouncyBallEntity;
			BouncyBallEntity bouncyBallEntity = null;
			if (isBouncyBall)
				bouncyBallEntity = (BouncyBallEntity) throwableentity;				
			throwableentity.setItem(itemstack);
			if (throwUp) {
				if (!player.abilities.flying) player.setDeltaMovement(player.getDeltaMovement().multiply(1,0,1)); // Don't let y velocity affect the throw
				// Aims the ball at 45 degree angle if looking straight ahead, otherwise converges to throwing straight
				float pitchrot = player.xRot - 0.5F * (90 - Math.abs(player.xRot));
				float inaccuracy = isBouncyBall ? bouncyBallEntity.baseInaccuracy + 0.5F : 1.0F;
				throwableentity.shootFromRotation(player, pitchrot, player.yRot, 
						item == Items.EXPERIENCE_BOTTLE ? -20.0F : 0.0F, 0.2F, inaccuracy);

				Vector3d playerVelocity = player.getDeltaMovement();
				// Add more of the player velocity so that the ball should be thrown such that it passes the center of the screen, allowing it to be easily hit.
				if (!Double.isNaN(playerVelocity.y)) {
					double playerSpeed = playerVelocity.length();
					if (playerSpeed != 0 && (!player.abilities.flying || playerSpeed > 0.4F)) {
						Vector3d playerVelScaled = playerVelocity.scale(
								Math.sqrt(playerSpeed*0.5F) / playerSpeed - 0.72F);
						throwableentity.setDeltaMovement(throwableentity.getDeltaMovement()
								.add(playerVelScaled.x, 
								!player.abilities.flying ? 0.0D : playerVelScaled.y, 
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
				throwableentity.shootFromRotation(player, player.xRot, player.yRot, 
						item == Items.EXPERIENCE_BOTTLE ? -20.0F : 0.0F, speed, inaccuracy);
			}
			level.addFreshEntity(throwableentity);
		}

		player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
		if (!player.abilities.instabuild) {
			itemstack.shrink(1);
		}
		
		return ActionResult.sidedSuccess(itemstack, level.isClientSide());
	}
	
	public static ActionResultType throwVanillaItem(World level, PlayerEntity player, Hand hand, Boolean throwUp, ItemStack itemstack) {
		Item item = itemstack.getItem();
		if (ServerConfig.override_vanilla_throwables.get()) {
			ProjectileItemEntity itementity = null;
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
	
	public static ActionResultType throwNewThrowableItem(World level, PlayerEntity player, Hand hand, Boolean throwUp, ItemStack itemstack) {
		BouncyBallEntity throwableentity = null;
		if (!level.isClientSide) {
			Item item = itemstack.getItem();
			if (item == Items.FIRE_CHARGE) {
				throwableentity = new BouncyFireBallEntity(ModEntityTypes.BOUNCY_FIREBALL_ENTITY.get(), level, player);
			} else if (item.is(ItemHelperFunctions.NUGGETTAG())) {
				throwableentity = new BouncyBallEntity(ModEntityTypes.SMALL_THROWABLE_ITEM_ENTITY.get(), level, player);
			} else if (ItemHelperFunctions.isMediumThrowable(item)) {
				throwableentity = new BouncyBallEntity(ModEntityTypes.MEDIUM_THROWABLE_ITEM_ENTITY.get(), level, player);
			} else
				throwableentity = new BouncyBallEntity(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), level, player);
		}
		return ItemHelperFunctions.throwBall(level, player, itemstack, throwableentity, throwUp).getResult();
	}

	public static ActionResultType throwBallUp(World level, PlayerEntity player, Hand hand, ItemStack throwableItem) {
		Item item = throwableItem.getItem();
		if (item.is(ItemHelperFunctions.VANILLATHROWABLETAG())) {
			return ItemHelperFunctions.throwVanillaItem(level, player, hand, true, throwableItem);
		} else if (item.is(ItemHelperFunctions.THROWABLEITEMTAG())) {
			return ItemHelperFunctions.throwNewThrowableItem(level, player, hand, true, throwableItem);
		}
		return ActionResultType.PASS;
	}
	
	public static ActionResultType useItem(World level, PlayerEntity player, Hand hand, ItemStack itemstack) {
		ActionResult<ItemStack> result = ActionResult.pass(itemstack);
		Item item = itemstack.getItem();
		if (!(item instanceof BallItem)) {// Code adapted from the vanilla minecraft way of calling itemstack.use on the client & server
			result = itemstack.use(level, player, hand);
			ItemStack newItemStack = result.getObject();
			if (!level.isClientSide()) {
				int i = itemstack.getCount();
				int j = itemstack.getDamageValue();
				
				if (!(newItemStack == itemstack && newItemStack.getCount() == i && newItemStack.getUseDuration() <= 0 && newItemStack.getDamageValue() == j) && 
						!(result.getResult() == ActionResultType.FAIL && newItemStack.getUseDuration() > 0 && !player.isUsingItem())) {
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
						((ServerPlayerEntity) player).refreshContainer(player.inventoryMenu);
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
		return result.getResult();
	}

	public static ActionResultType tryThrow(World level, PlayerEntity player, Hand hand, Boolean tryUse) {
		ItemStack itemstack = player.getItemInHand(hand);
		Item item = itemstack.getItem();
		if (!itemstack.isEmpty()) {
			boolean isExistingThrowable = item.is(ItemHelperFunctions.VANILLATHROWABLETAG());
			if (item.is(ItemHelperFunctions.THROWABLEITEMTAG()) || isExistingThrowable) {
				ActionResultType result;
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
						result = ActionResultType.FAIL;
					}
				} else {
					// Try to just use the item first. If this does not consume the action, continue throwing
					ActionResultType ActionResultType = ItemHelperFunctions.useItem(level, player, hand, itemstack);
					
					if (ActionResultType.consumesAction() || !okToThrow) {
						result = ActionResultType;
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
		return ActionResultType.PASS;
	}
}
