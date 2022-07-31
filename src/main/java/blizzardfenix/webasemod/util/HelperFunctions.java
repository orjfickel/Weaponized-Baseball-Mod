package blizzardfenix.webasemod.util;

import java.util.Random;

import javax.annotation.Nullable;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.config.ServerConfig;
import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.entity.BouncyFireBallEntity;
import blizzardfenix.webasemod.entity.PickableEggEntity;
import blizzardfenix.webasemod.entity.PickableEnderPearlEntity;
import blizzardfenix.webasemod.entity.PickableExperienceBottleEntity;
import blizzardfenix.webasemod.entity.PickablePotionEntity;
import blizzardfenix.webasemod.entity.PickableSnowballEntity;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

public class HelperFunctions {
	protected static final Random random = new Random();
	
	/**
	 * Throw an item as a throwable ball. Specifically for items that do not already have this behaviour implemented as their 'use' functionality
	 * @param world
	 * @param player
	 * @param hand
	 * @return
	 */
	public static InteractionResultHolder<ItemStack> throwBall(Level level, Player player, ItemStack itemstack, @Nullable ThrowableItemProjectile throwableentity, Vec3 playerVelocity) {		
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
			player.setDeltaMovement(playerVelocity);
			if (Settings.throwUp && player.getMainHandItem().getItem() instanceof BaseballBat) {
				// Aims the ball at 45 degree angle if looking straight ahead, otherwise converges to throwing straight
				float pitchrot = player.getXRot() - 0.5F * (90 - Math.abs(player.getXRot()));
				float inaccuracy = isBouncyBall ? bouncyBallEntity.baseInaccuracy + 0.5F : 1.0F;
				throwableentity.shootFromRotation(player, pitchrot, player.getYRot(), 
						item == Items.EXPERIENCE_BOTTLE ? -20.0F : 0.0F, 0.2F, inaccuracy);

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

	public static InteractionResult tryThrow(Level level, Player player, InteractionHand hand, Vec3 playerVelocity) {
		ItemStack itemstack = player.getItemInHand(hand);
		if (!itemstack.isEmpty()) {
			Item item = itemstack.getItem();
			ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
			boolean isExistingThrowable = tags.getTag(tags.createTagKey(new ResourceLocation("webasemod","vanilla_throwables"))).contains(item);
			if (tags.getTag(tags.createTagKey(new ResourceLocation("webasemod", "throwable_items"))).contains(item) || isExistingThrowable) {
				InteractionResult result;
				if (isExistingThrowable) {
					if (ServerConfig.override_vanilla_throwables.get()) {
						ThrowableItemProjectile itementity;
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
						result = HelperFunctions.throwBall(level, player, itemstack, itementity, playerVelocity).getResult();
					} else {
						return item.use(level, player, hand).getResult();
					}
				} else {
					BouncyBallEntity throwableentity;
					if (item == Items.FIRE_CHARGE) {
						throwableentity = new BouncyFireBallEntity(ModEntityTypes.BOUNCY_FIREBALL_ENTITY.get(), level, player);
					} else if (tags.getTag(tags.createTagKey(new ResourceLocation("forge","nuggets"))).contains(item)) {
						throwableentity = new BouncyBallEntity(ModEntityTypes.SMALL_THROWABLE_ITEM_ENTITY.get(), level, player);
					} else
						throwableentity = new BouncyBallEntity(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), level, player);
					result = HelperFunctions.throwBall(level, player, itemstack, throwableentity, playerVelocity).getResult();
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
