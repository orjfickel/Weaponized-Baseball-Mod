package blizzardfenix.webasemod.entity;

import blizzardfenix.webasemod.init.ModEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class PickableExperienceBottleEntity extends ExperienceBottleEntity {

	public PickableExperienceBottleEntity(EntityType<? extends PickableExperienceBottleEntity> entityType, World level) {
		super(entityType, level);
	}

	public PickableExperienceBottleEntity(World level, LivingEntity player) {
		this(level, player.getX(), player.getEyeY() - (double) 0.1F, player.getZ());
		this.setOwner(player);
	}

	public PickableExperienceBottleEntity(World level, double x, double y, double z) {
		this(ModEntityTypes.PICKABLE_EXPERIENCE_BOTTLE_ENTITY.get(), level);
		this.setPos(x, y, z);
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
