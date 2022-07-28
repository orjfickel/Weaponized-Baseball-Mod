package blizzardfenix.webasemod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BouncyFireBallEntity extends BouncyBallEntity {

	public BouncyFireBallEntity(EntityType<? extends BouncyFireBallEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public BouncyFireBallEntity(EntityType<? extends BouncyFireBallEntity> type, World worldIn, LivingEntity throwerIn) {
		super(type, worldIn, throwerIn);
	}
	
	public BouncyFireBallEntity(EntityType<? extends BouncyFireBallEntity> type, World worldIn, double x, double y, double z) {
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
		
		Vector3d pos = this.getCenterPositionVec();
        this.level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
	}
		
	@Override
	public void hitBall(Vector3d shootvec, float inaccuracy) {		
		//Spawn fire charge entity and delete this entity
		SmallFireballEntity smallfireballentity = new SmallFireballEntity(this.level, (LivingEntity) this.getOwner(), // Must be a livingentity if hit with bat
				shootvec.x + 0.0075D * (double)inaccuracy, 
				shootvec.y + 0.0075D * (double)inaccuracy, 
				shootvec.z + 0.0075D * (double)inaccuracy);
        smallfireballentity.setPos(this.getX(), this.getY(), this.getZ());
        this.level.addFreshEntity(smallfireballentity);
		this.remove();
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
