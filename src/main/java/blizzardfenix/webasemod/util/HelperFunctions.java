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
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HelperFunctions {
	protected static final Random random = new Random();
	
	/**
	 * Throw an item as a throwable ball. Specifically for items that do not already have this behaviour implemented as their 'use' functionality
	 * @param world
	 * @param player
	 * @param hand
	 * @return
	 */
	public static ActionResult<ItemStack> throwBall(World level, PlayerEntity player, ItemStack itemstack, @Nullable ProjectileItemEntity throwableentity, Vector3d playerVelocity) {		
		SoundEvent soundEvent;
		Item item = itemstack.getItem();
		if(item == Items.ENDER_PEARL)
			soundEvent = SoundEvents.ENDER_PEARL_THROW;
		else if(item == Items.SNOWBALL)
			soundEvent = SoundEvents.SNOWBALL_THROW;
		else if(item == Items.EXPERIENCE_BOTTLE)
			soundEvent = SoundEvents.EXPERIENCE_BOTTLE_THROW;
		else if(item == Items.POTION)
			soundEvent = SoundEvents.SPLASH_POTION_THROW;
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
			player.setDeltaMovement(playerVelocity);
			if (Settings.throwUp && player.getMainHandItem().getItem() instanceof BaseballBat) {
				// Aims the ball at 45 degree angle if looking straight ahead, otherwise converges to throwing straight
				float pitchrot = player.xRot - 0.5F * (90 - Math.abs(player.xRot));
				float inaccuracy = isBouncyBall ? bouncyBallEntity.baseInaccuracy + 0.5F : 1.0F;
				throwableentity.shootFromRotation(player, pitchrot, player.yRot, 
						item == Items.EXPERIENCE_BOTTLE ? -20.0F : 0.0F, 0.2F, inaccuracy);

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
			} else {
				float inaccuracy = isBouncyBall ? bouncyBallEntity.baseInaccuracy + 0.5F : 1.0F;
				float speed = 	player.isCrouching() ? 0.3F : 
								isBouncyBall ? bouncyBallEntity.throwSpeed : 
								item == Items.EXPERIENCE_BOTTLE ? 0.7F : 
								item == Items.SPLASH_POTION ? 0.5F : 
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

	public static ActionResultType tryThrow(World level, PlayerEntity player, Hand hand, Vector3d playerVelocity) {
		ItemStack itemstack = player.getItemInHand(hand);
		if (!itemstack.isEmpty()) {
			Item item = itemstack.getItem();
			ITagCollection<Item> tags = ItemTags.getAllTags();
			boolean isExistingThrowable = tags.getTag(new ResourceLocation("webasemod", "vanilla_throwables")).contains(item);
			if (tags.getTag(new ResourceLocation("webasemod", "throwable_items")).contains(item) || isExistingThrowable) {
				ActionResultType result;
				if (isExistingThrowable) {
					ProjectileItemEntity itementity;
					if (ServerConfig.hittable_vanilla_throwables.get()) {
						if(item == Items.ENDER_PEARL)
							itementity = new PickableEnderPearlEntity(level, player);
						else if(item == Items.EGG)
							itementity = new PickableEggEntity(level, player);
						else if(item == Items.EXPERIENCE_BOTTLE)
							itementity = new PickableExperienceBottleEntity(level, player);
						else if(item == Items.POTION)
							itementity = new PickablePotionEntity(level, player);
						else
							itementity = new PickableSnowballEntity(level, player);
					} else {
						if(item == Items.ENDER_PEARL)
							itementity = new EnderPearlEntity(level, player);
						else if(item == Items.EGG)
							itementity = new EggEntity(level, player);
						else if(item == Items.EXPERIENCE_BOTTLE)
							itementity = new ExperienceBottleEntity(level, player);
						else if(item == Items.POTION)
							itementity = new PotionEntity(level, player);
						else
							itementity = new SnowballEntity(level, player);
					}
					result = HelperFunctions.throwBall(level, player, itemstack, itementity, playerVelocity).getResult();
				} else {
					BouncyBallEntity throwableentity;
					if (item == Items.FIRE_CHARGE) {
						throwableentity = new BouncyFireBallEntity(ModEntityTypes.BOUNCY_FIREBALL_ENTITY.get(), level, player);
					} else if (tags.getTag(new ResourceLocation("forge","nuggets")).contains(item)) {
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
		return ActionResultType.PASS;
	}
}
