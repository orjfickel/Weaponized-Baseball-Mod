package blizzardfenix.webasemod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BouncyFireBallEntity extends BouncyBallEntity {

	public BouncyFireBallEntity(EntityType<? extends BouncyFireBallEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public BouncyFireBallEntity(EntityType<? extends BouncyFireBallEntity> type, Level worldIn, LivingEntity throwerIn) {
		super(type, worldIn, throwerIn);
	}
	
	public BouncyFireBallEntity(EntityType<? extends BouncyFireBallEntity> type, Level worldIn, double x, double y, double z) {
	    super(type, worldIn, x, y, z);
	}
	
	@Override
	protected void setup() {
		super.setup();
	}
	

	@Override
	public void tick() {
		this.setSecondsOnFire(1);
		
		super.tick();
		
		Vec3 pos = this.getCenterPositionVec();
        this.level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
	}
		
	@Override
	public void hitBall(Vec3 shootvec, float inaccuracy) {		
		//Spawn fire charge entity and delete this entity
		SmallFireball smallfireballentity = new SmallFireball(this.level, (LivingEntity) this.getOwner(), // Must be a livingentity if hit with bat
				shootvec.x + 0.0075D * (double)inaccuracy, 
				shootvec.y + 0.0075D * (double)inaccuracy, 
				shootvec.z + 0.0075D * (double)inaccuracy);
        smallfireballentity.setPos(this.getX(), this.getY(), this.getZ());
        this.level.addFreshEntity(smallfireballentity);
		this.remove(RemovalReason.DISCARDED);
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.isFire())
			return false;
		else
			return super.hurt(source, amount);
	}
	
	@Override
	public float getBrightness() {
		return 1.0F;
	}
}
