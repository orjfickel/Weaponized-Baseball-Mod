package blizzardfenix.webasemod.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import blizzardfenix.webasemod.init.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

// The sole purpose of this class is so that ThrowableBallEntity can pretend to be an instance of AbstractArrow
public class MockArrow extends AbstractArrow implements IEntityAdditionalSpawnData {
	public float lastAccessTime = -1;
	private ThrowableBallEntity spawner;
	private UUID spawnerid;
	
	public MockArrow(EntityType<? extends MockArrow> type, Level worldIn) {
		super(type, worldIn);
	}
	
	public MockArrow(Entity originalshooter, ThrowableBallEntity spawner, double x, double y, double z, Level worldIn) {
		super(ModEntityTypes.MOCKARROW_ENTITY.get(), x, y, z, worldIn);
		this.spawner = spawner;
		this.spawnerid = spawner.getUUID();
		this.setOwner(originalshooter);
		if (originalshooter instanceof Player) {
			this.pickup = AbstractArrow.Pickup.ALLOWED;
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
			this.remove(RemovalReason.DISCARDED);
		}
		ThrowableBallEntity tempspawner = this.getSpawner();
		if (tempspawner != null)
			this.setBoundingBox(tempspawner.getBoundingBox());
	}
	
	public void setLastAccessTime() {
		this.lastAccessTime = this.tickCount;
	}
	
	@Nullable
	public ThrowableBallEntity getSpawner() {
		if ((this.spawner == null || !this.spawner.isAlive()) && this.spawnerid != null && this.level instanceof ServerLevel) {
			Entity entity = ((ServerLevel) this.level).getEntity(this.spawnerid);
			if (entity instanceof ThrowableBallEntity) {
				this.spawner = (ThrowableBallEntity) entity;
			} else {
				this.spawner = null;
			}
		}
		return this.spawner;
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		if (this.spawnerid != null)
			compound.put("spawner", NbtUtils.createUUID(this.spawnerid));
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("spawner") && this.level instanceof ServerLevel)
			this.spawnerid = NbtUtils.loadUUID(compound.get("spawner"));
	}

	@Override
	public boolean shouldRender(double a, double b, double c) {
		return false;
	}
	
	@Override
	public boolean shouldRenderAtSqrDistance(double a) {
		return false;
	}
	
	@Override
	public boolean isInvisible() {
		return true;
	}
	
	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
	}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {
	}
	
	@Override
	public Entity getOwner() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.getOwner() : super.getOwner();
	}
	@Override
	public Vec3 getDeltaMovement() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.getDeltaMovement() : Vec3.ZERO;
	}
	@Override
	public Vec3 position() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.position() : super.position();
	}
	@Override
	public BlockPos blockPosition() {
		ThrowableBallEntity tempspawner = this.getSpawner();
		return tempspawner != null ? tempspawner.blockPosition() : super.blockPosition();
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
