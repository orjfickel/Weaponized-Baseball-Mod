package blizzardfenix.webasemod.items.tools;

import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.config.ServerConfig;
import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.entity.PickableEggEntity;
import blizzardfenix.webasemod.entity.PickableEnderPearlEntity;
import blizzardfenix.webasemod.entity.PickableExperienceBottleEntity;
import blizzardfenix.webasemod.entity.PickablePotionEntity;
import blizzardfenix.webasemod.entity.PickableSnowballEntity;
import blizzardfenix.webasemod.init.ModItems;
import blizzardfenix.webasemod.items.ItemHelperFunctions;
import blizzardfenix.webasemod.server.WebaseMessage;
import blizzardfenix.webasemod.server.WebasePacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;

public class BaseballBat extends SwordItem {
	Logger logger = LogManager.getLogger(BaseballMod.MODID + " BaseballBat");
	Random random = new Random();
	public boolean consecutiveUse = false;
	
	public BaseballBat(Tier tier, Properties builder) {
		super(tier, 1, -2.8F, builder);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		if (!player.level.isClientSide() && entity.tickCount > 5) {
			if (entity instanceof ThrowableItemProjectile) {
				ThrowableItemProjectile throwableentity = (ThrowableItemProjectile) entity;
				throwableentity.setOwner(player);
				boolean isBouncyBall = entity instanceof BouncyBallEntity;
				BouncyBallEntity ballentity = null;
				if (isBouncyBall) {
					ballentity = (BouncyBallEntity) entity;
					ballentity.comboDmg += 0.125F * this.getDamage() + ballentity.batHitDmg; // Wood has getDamage 1, Netherite as getDamage 5
			        float shootspeed = ballentity.batHitSpeed * (0.46F * this.getDamage());
			        Vec3 newvel = player.getLookAngle().scale(shootspeed);
			        ballentity.hitbybat = true;
			        
			        // Apply enchantments
			        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0 && !ballentity.infinityHit 
			        		&& ballentity.Pickup == Pickup.ALLOWED) {
			        	ballentity.infinityHit = true;
			        	ballentity.Pickup = Pickup.CREATIVE_ONLY;
			        	player.getInventory().add(throwableentity.getItem().copy());
			        }
			        
		            int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
		            if (power > 0) {
		            	ballentity.comboDmg += (double)power * 0.5D + 0.5D;
		            }		
		            int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
		            if (punch > 0) {
		            	ballentity.mass += punch * 10;
		            }		
		            int fire = EnchantmentHelper.getFireAspect(player);
		            if (fire > 0) {
		            	ballentity.health += fire * 4;
		            	ballentity.setSecondsOnFire(fire * 4);
		            }		            
		            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
		            	ballentity.health += 100;
		            	ballentity.setSecondsOnFire(100);
		            }
		            
			        ballentity.hitBall(newvel, ballentity.baseInaccuracy * 3F + 8F / (this.getDamage()) - 1.6F);
				} else {
					// Hit a throwable that is not an instance of BouncyBallEntity
			        float shootspeed = 1.2F * (0.46F * this.getDamage());
			        Vec3 newvel = player.getLookAngle().scale(shootspeed);
					float inaccuracy = 3F + 8F / (this.getDamage()) - 1.6F;
					throwableentity.setDeltaMovement(newvel.normalize().add(this.random.nextGaussian() * 0.0075D * (double)inaccuracy, 
							this.random.nextGaussian() * 0.0075D * (double)inaccuracy, 
							this.random.nextGaussian() * 0.0075D * (double)inaccuracy).scale((double)newvel.length()));

					if (throwableentity instanceof PickableEggEntity) ((PickableEggEntity) throwableentity).returnToInventory = false;
					else if (throwableentity instanceof PickableEnderPearlEntity) ((PickableEnderPearlEntity) throwableentity).returnToInventory = false;
					else if (throwableentity instanceof PickableExperienceBottleEntity) ((PickableExperienceBottleEntity) throwableentity).returnToInventory = false;
					else if (throwableentity instanceof PickablePotionEntity) ((PickablePotionEntity) throwableentity).returnToInventory = false;
					else if (throwableentity instanceof PickableSnowballEntity) ((PickableSnowballEntity) throwableentity).returnToInventory = false;
				}

				throwableentity.gameEvent(GameEvent.PROJECTILE_SHOOT, player);
				if (this.consecutiveUse) this.consecutiveUse = false;
	            //Reduce bat durability when hitting a ball
	            stack.hurtAndBreak(1, player, (consumedEntity) -> {
		        	consumedEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND); // Broadcasts whenever this bat breaks
		        });
			}
		}
		return false;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		if (hand == InteractionHand.MAIN_HAND) {
			if (level.isClientSide) {
				ItemStack throwableItem = BaseballBat.getProjectile(itemstack, player);
				if (!throwableItem.isEmpty() && ItemHelperFunctions.checkThrowDelay()) {
					// Tell the server to perform the throw as well. Necessary because the server doesn't know the player's velocity (also consistency with regular throwing)
					WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(hand, player.getDeltaMovement(), true, false));
					
					InteractionResult result = ItemHelperFunctions.throwBallUp(level, player, hand, throwableItem);
					if (result.consumesAction()) {
						ItemHelperFunctions.ticksBeforeThrowing = ServerConfig.throwCooldown.get();
						return InteractionResultHolder.consume(itemstack);
					}
					return InteractionResultHolder.fail(itemstack);
				} else {
					return InteractionResultHolder.fail(itemstack);
				}
			}
		}

		return InteractionResultHolder.pass(itemstack);
	}

	/**
	 * Adapted from {@link Player.getProjectile}
	 */
	public static ItemStack getProjectile(ItemStack itemstack, Player player) {
		Predicate<ItemStack> predicate = (item) -> {
			boolean isVanillaThrowable = item.is(ItemHelperFunctions.VANILLATHROWABLETAG);
			return (item.is(ItemHelperFunctions.THROWABLEITEMTAG) && !isVanillaThrowable) || (isVanillaThrowable && ServerConfig.override_vanilla_throwables.get());
			};
		
		ItemStack throwableItem = BaseballBat.getHeldProjectile(player, predicate);
		if (!throwableItem.isEmpty()) {
			return ForgeHooks.getProjectile(player, itemstack, throwableItem);
		} else {
			for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
				throwableItem = player.getInventory().getItem(i);
				if (predicate.test(throwableItem)) {
					return ForgeHooks.getProjectile(player, itemstack, throwableItem);
				}
			}

			return ForgeHooks.getProjectile(player, itemstack,
					player.getAbilities().instabuild ? new ItemStack(ModItems.BASIC_BASEBALL.get()) : ItemStack.EMPTY);
		}
	}

	public static ItemStack getHeldProjectile(LivingEntity player, Predicate<ItemStack> predicate) {
		if (predicate.test(player.getItemInHand(InteractionHand.OFF_HAND))) {
			return player.getItemInHand(InteractionHand.OFF_HAND);
		} else {
			return predicate.test(player.getItemInHand(InteractionHand.MAIN_HAND)) ? player.getItemInHand(InteractionHand.MAIN_HAND) : ItemStack.EMPTY;
		}
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		return this.getTier() == Tiers.WOOD ? 200 : 0;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState blockState) {
		Material material = blockState.getMaterial();
		return blockState.is(Blocks.BEE_NEST) || blockState.is(Blocks.BEEHIVE) || material == Material.CACTUS;
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		Material material = blockState.getMaterial();
		if (blockState.is(Blocks.BEE_NEST) || blockState.is(Blocks.BEEHIVE))
			return 2.0F;
		return material == Material.CACTUS || material == Material.GLASS || material == Material.BUILDABLE_GLASS ||
				blockState.is(Blocks.LANTERN) || blockState.is(Blocks.SEA_LANTERN) ? 3.0F : 1.0F;
	}

	@Override
	public boolean mineBlock(ItemStack itemStack, Level world, BlockState blockState, BlockPos blockPos,
			LivingEntity entity) {
		if (blockState.getDestroySpeed(world, blockPos) != 0.0F) {
			itemStack.hurtAndBreak(2, entity, (p_220044_0_) -> {
				p_220044_0_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
			});
		}

		return true;
	}
	
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment == Enchantments.INFINITY_ARROWS || enchantment == Enchantments.POWER_ARROWS || 
				enchantment == Enchantments.PUNCH_ARROWS || enchantment == Enchantments.FLAMING_ARROWS || 
				(enchantment != Enchantments.SHARPNESS && super.canApplyAtEnchantingTable(stack, enchantment));
	}
	
	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		Map<Enchantment, Integer> enchmap = EnchantmentHelper.getEnchantments(book);
		
		return !enchmap.containsKey(Enchantments.SHARPNESS) && super.isBookEnchantable(stack, book);
	}
}
