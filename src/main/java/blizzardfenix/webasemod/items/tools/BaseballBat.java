package blizzardfenix.webasemod.items.tools;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.entity.BouncyFireBallEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity.PickupStatus;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.World;

public class BaseballBat extends SwordItem {

	Logger logger = LogManager.getLogger(BaseballMod.MODID + " BaseballBat");
	
	public BaseballBat(IItemTier tier, Properties builder) {
		super(tier, 1, -2.8F, builder);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
		boolean successHit = false;
		if (!player.level.isClientSide() && entity.tickCount > 5) {
			if (entity instanceof BouncyFireBallEntity) { // Summon a small fireball if a bouncy fireball is hit
				SmallFireballEntity ballentity = (SmallFireballEntity) entity;
				ballentity.setOwner(player);
		        Vector3d newvel = player.getLookAngle().scale(1.2F * (1.0F + 0.3F * this.getDamage()));
		        ballentity.setDeltaMovement(newvel);
		        ballentity.xPower = newvel.x * 0.1D;
		        ballentity.yPower = newvel.y * 0.1D;
	            ballentity.zPower = newvel.z * 0.1D;
	            successHit = true;
			} else if (entity instanceof ProjectileItemEntity) {
				ProjectileItemEntity throwableentity = (ProjectileItemEntity) entity;
				throwableentity.setOwner(player);
				boolean isBouncyBall = entity instanceof BouncyBallEntity;
				BouncyBallEntity ballentity = null;
				if (isBouncyBall) {
					ballentity = (BouncyBallEntity) entity;
					ballentity.comboDmg += 0.125F * this.getDamage() + ballentity.batHitDmg; // Wood has getDamage 3, Netherite as getDamage 7
			        float shootspeed = ballentity.batHitSpeed * (1.0F + 0.3F * this.getDamage());
			        Vector3d newvel = player.getLookAngle().scale(shootspeed);
			        ballentity.hitbybat = true;
			        
			        // Apply enchantments
			        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0 && !ballentity.infinityHit 
			        		&& ballentity.pickupStatus == PickupStatus.ALLOWED) {
			        	ballentity.infinityHit = true;
			        	ballentity.pickupStatus = PickupStatus.CREATIVE_ONLY;
			        	player.inventory.add(throwableentity.getItem().copy());
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
		            
			        ballentity.hitBall(newvel, ballentity.baseInaccuracy * 2.5F);
				} else {
					// Hit a throwable that is not an instance of BouncyBallEntity
			        float shootspeed = 1.2F * (1.0F + 0.3F * this.getDamage());
			        Vector3d newvel = player.getLookAngle().scale(shootspeed);
					float inaccuracy = 1.0F;

					throwableentity.setDeltaMovement(newvel.normalize().add(Item.random.nextDouble() * 0.0075D * (double)inaccuracy, 
							Item.random.nextDouble() * 0.0075D * (double)inaccuracy, 
							Item.random.nextDouble() * 0.0075D * (double)inaccuracy).scale((double)newvel.length()));
				}
	            successHit = true;
			}
		}
		
		if (successHit) {
            //Reduce bat durability when hitting a ball
            stack.hurtAndBreak(1, player, (consumedEntity) -> {
	        	consumedEntity.broadcastBreakEvent(EquipmentSlotType.MAINHAND); // Broadcasts whenever this bat breaks
	        });
		}
		
		return false;
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable IRecipeType<?> recipeType) {
		return this.getTier() == ItemTier.WOOD ? 200 : 0;
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
	public boolean mineBlock(ItemStack itemStack, World world, BlockState blockState, BlockPos blockPos,
			LivingEntity entity) {
		if (blockState.getDestroySpeed(world, blockPos) != 0.0F) {
			itemStack.hurtAndBreak(2, entity, (p_220044_0_) -> {
				p_220044_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
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
