package blizzardfenix.webasemod.entity;

import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.items.ItemHelperFunctions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class PickablePotionEntity extends PotionEntity {
	public boolean returnToInventory = false;

	public PickablePotionEntity(EntityType<? extends PickablePotionEntity> entityType, World level) {
		super(entityType, level);
	}

	public PickablePotionEntity(World level, LivingEntity player) {
		this(level, player.getX(), player.getEyeY() - (double) 0.1F, player.getZ());
		this.setOwner(player);
	}

	public PickablePotionEntity(World level, double x, double y, double z) {
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
	protected void onHit(RayTraceResult hitresult) {
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
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
