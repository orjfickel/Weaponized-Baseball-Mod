package blizzardfenix.webasemod.entity;

import blizzardfenix.webasemod.init.ModEntityTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class PickableExperienceBottleEntity extends ThrownExperienceBottle {

	public PickableExperienceBottleEntity(EntityType<? extends PickableExperienceBottleEntity> entityType, Level level) {
		super(entityType, level);
	}

	public PickableExperienceBottleEntity(Level level, LivingEntity player) {
		this(level, player.getX(), player.getEyeY() - (double) 0.1F, player.getZ());
		this.setOwner(player);
	}

	public PickableExperienceBottleEntity(Level level, double x, double y, double z) {
		this(ModEntityTypes.PICKABLE_EXPERIENCE_BOTTLE_ENTITY.get(), level);
		this.setPos(x, y, z);
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
