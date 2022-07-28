package blizzardfenix.webasemod.entity;

import blizzardfenix.webasemod.init.ModEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class PickableEnderPearlEntity extends EnderPearlEntity {

	public PickableEnderPearlEntity(EntityType<? extends PickableEnderPearlEntity> entityType, World level) {
		super(entityType, level);
	}

	public PickableEnderPearlEntity(World level, LivingEntity player) {
		this(level, player.getX(), player.getEyeY() - (double) 0.1F, player.getZ());
		this.setOwner(player);
	}

	public PickableEnderPearlEntity(World level, double x, double y, double z) {
		this(ModEntityTypes.PICKABLE_ENDER_PEARL_ENTITY.get(), level);
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
