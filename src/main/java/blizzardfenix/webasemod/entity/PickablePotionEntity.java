package blizzardfenix.webasemod.entity;

import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.items.ItemHelperFunctions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

public class PickablePotionEntity extends ThrownPotion {
	public boolean returnToInventory = false;

	public PickablePotionEntity(EntityType<? extends PickablePotionEntity> entityType, Level level) {
		super(entityType, level);
	}

	public PickablePotionEntity(Level level, LivingEntity player) {
		this(level, player.getX(), player.getEyeY() - (double) 0.1F, player.getZ());
		this.setOwner(player);
	}

	public PickablePotionEntity(Level level, double x, double y, double z) {
		this(ModEntityTypes.PICKABLE_POTION_ENTITY.get(), level);
		this.setPos(x, y, z);
	}

	@Override
	public void tick() {
		if (this.returnToInventory && this.tickCount > ItemHelperFunctions.hitTime) {
			BallPhysicsHelper.pickupThrowable(this, this.getOwner());
		}
		else super.tick();
	}

	@Override
	protected void onHit(HitResult hitresult) {
		if (this.returnToInventory) {
			BallPhysicsHelper.pickupThrowable(this, this.getOwner());
		}
		else super.onHit(hitresult);
	}

	@Override
	public boolean isPickable() {
		return true;
	}

//	/** Necessary for rendering */
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
