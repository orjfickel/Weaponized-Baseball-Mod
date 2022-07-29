package blizzardfenix.webasemod.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import blizzardfenix.webasemod.init.ModEntityTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

// The sole purpose of this class is so that ThrowableBallEntity can pretend to be an instance of AbstractArrowEntity
public class MockArrow extends AbstractArrowEntity implements IEntityAdditionalSpawnData {
	public float lastAccessTime = -1;
	private ThrowableBallEntity spawner;
	private UUID spawnerid;
	
	public MockArrow(EntityType<? extends MockArrow> type, World worldIn) {
		super(type, worldIn);
	}
	
	public MockArrow(Entity originalshooter, ThrowableBallEntity spawner, double x, double y, double z, World worldIn) {
		super(ModEntityTypes.MOCKARROW_ENTITY.get(), x, y, z, worldIn);
		this.spawner = spawner;
		this.spawnerid = spawner.getUUID();
		this.setOwner(originalshooter);
		if (originalshooter instanceof PlayerEntity) {
			this.pickup = AbstractArrowEntity.PickupStatus.ALLOWED;
		}
	}
	
	@Override
	protected ItemStack getPickupItem() {
		return null;
	}
	
	@Override
	public void tick() {		
		// Remove if it has been more than five seconds since this MockArrow was last accessed.
		if(!this.level.isClientSide && this.tickCount >= this.lastAccessTime + 250) {
			this.remove();
		}
	}
	
	public void setLastAccessTime() {
		this.lastAccessTime = this.tickCount;
	}
	
	@Nullable
	public ThrowableBallEntity getSpawner() {
		if ((this.spawner == null || !this.spawner.isAlive()) && this.spawnerid != null && this.level instanceof ServerWorld) {
			Entity entity = ((ServerWorld) this.level).getEntity(this.spawnerid);
			if (entity instanceof ThrowableBallEntity) {
				this.spawner = (ThrowableBallEntity) entity;
			} else {
				this.spawner = null;
			}
		}
		return this.spawner;
	}
	
	@Override
	public void addAdditionalSaveData(CompoundNBT compound) {
		super.addAdditionalSaveData(compound);
		if (this.spawnerid != null)
			compound.put("spawner", NBTUtil.createUUID(this.spawnerid));
	}
	
	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("spawner") && this.level instanceof ServerWorld)
			this.spawnerid = NBTUtil.loadUUID(compound.get("spawner"));
	}
	
	@Override
	public boolean isInvisible() {
		return true;
	}
	
	@Override
	public void writeSpawnData(PacketBuffer buffer) {
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
	}
	
	@Override
	public Entity getOwner() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.getOwner() : super.getOwner();
	}
	@Override
	public Vector3d getDeltaMovement() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.getDeltaMovement() : Vector3d.ZERO;
	}
	@Override
	public Vector3d position() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.position() : super.position();
	}
	@Override
	public BlockPos blockPosition() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.blockPosition() : super.blockPosition();
	}
	@Override
	public AxisAlignedBB getBoundingBox() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.getBoundingBox() : super.getBoundingBox();
	}
	@Override
	public boolean isInWaterRainOrBubble() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.isInWaterRainOrBubble() : super.isInWaterRainOrBubble();
	}
	@Override
	public boolean isInvulnerable() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.isInvulnerable() : super.isInvulnerable();
	}
	@Override
	public boolean isOnFire() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.isOnFire() : super.isOnFire();
	}
}
