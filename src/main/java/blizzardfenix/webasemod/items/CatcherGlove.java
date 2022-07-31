package blizzardfenix.webasemod.items;

import java.util.Random;

import blizzardfenix.webasemod.entity.BouncyBallEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import net.minecraft.world.item.Item.Properties;

public class CatcherGlove extends Item {
	float range = 25;
	float dotAngle = 0.6F;
	Random random = new Random();

	public CatcherGlove(Properties properties) {
		super(properties.defaultDurability(250));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		level.playSound((Player) null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.NEUTRAL, 0.5F,
				0.4F / (random.nextFloat() * 0.4F + 0.8F));
		player.getCooldowns().addCooldown(this, 40);
		
		if (!level.isClientSide) {
			// Pick up any balls within a certain range and close enough to where the player is looking.
			Vec3 lookvec = player.getLookAngle();
			for(BouncyBallEntity ballentity : level.getEntitiesOfClass(BouncyBallEntity.class, player.getBoundingBox().inflate(range*1.5))) {
				Vec3 posdiff = ballentity.getCenterPositionVec().subtract(player.getEyePosition(1));
				float dist = (float) posdiff.lengthSqr();
				if (dist < range * range && posdiff.scale(1/Math.sqrt(dist)).dot(lookvec) > this.dotAngle) {
					ballentity.pickup(player);
				}
			}
			if (!player.getAbilities().instabuild) {
				itemstack.hurtAndBreak(1, player, (playerentity) -> {
					playerentity.broadcastBreakEvent(hand);
				});
			}
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		player.gameEvent(GameEvent.ITEM_INTERACT_START);

		return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
	}
}
