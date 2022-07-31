package blizzardfenix.webasemod.entity;

import blizzardfenix.webasemod.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

/** Adds bouncing logic (direction & speed) and handles being hit */
public class BouncyBallEntity extends ThrowableBallEntity implements IEntityAdditionalSpawnData , ItemSupplier {
	/** Chance of bouncing towards a living entity */
	public final float ricochetChance = 0.0F;//Turned off for now as shooting towards a target needs more tweaking than expected
	public final double richochetRange = 50D;
	/** Chance of, if bouncing towards a living entity, bouncing specifically towards the player, no matter how far away */
	public final float bounceBackChance = 0.0F;//0.5F;
	
	public BouncyBallEntity(EntityType<? extends BouncyBallEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public BouncyBallEntity(EntityType<? extends BouncyBallEntity> type, Level worldIn, LivingEntity throwerIn) {
		super(type, worldIn, throwerIn);
	}
	
	public BouncyBallEntity(EntityType<? extends BouncyBallEntity> type, Level worldIn, double x, double y, double z) {
	    super(type, worldIn, x, y, z);
	}
	
	@Override
	protected void setup() {
		super.setup();
		
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.BASIC_BASEBALL.get();
	}
	
	/**
	 *  Handle the velocity correction of the block impact
	 */
	@Override
	public boolean onBlockImpact(BlockHitResult result, Vec3 prevvel) {
		if (!super.onBlockImpact(result, prevvel))
			return false;

		float speed = (float) prevvel.length();
		if (speed == 0)
			return false;
		
		Vec3 shootvec = Vec3.ZERO;
		BlockPos blockPos = result.getBlockPos();
		BlockState blockState = this.level.getBlockState(blockPos);
		Block block = blockState.getBlock();
		Material material = blockState.getMaterial();
		Direction face = result.getDirection();
		Axis axis = face.getAxis();

		float thisblockbounce = this.level.getBlockState(this.blockPosition()).getBlock().getJumpFactor();
		float hitblockbounce = block.getJumpFactor();
		float blockbounce = thisblockbounce == 1.0F ? hitblockbounce : thisblockbounce;
		// block.friction has 1 as zero friction and 0 as maximum friction, considering that ice has a friction value higher than regular blocks
		float blockfric = block.getFriction();
		
		if (material == Material.DIRT || material == Material.CLAY) {
			blockbounce = 0.75F;
			blockfric = 0.85F;
		} else if (material == Material.GRASS) {
			blockbounce = 0.65F;
			blockfric = 0.75F;
		} else if (material == Material.SNOW || material == Material.TOP_SNOW || material == Material.SAND
				|| material == Material.CAKE || material == Material.LEAVES || material == Material.PLANT
				|| material == Material.REPLACEABLE_PLANT || material == Material.REPLACEABLE_WATER_PLANT
				|| material == Material.WATER_PLANT) {
			blockbounce = 0.5F;
			blockfric = 0.6F;
		} else if (block instanceof SlimeBlock) {
			blockbounce = 0.99F;
			blockfric = 0.75F;
		} else {
			blockbounce = 0.8F;
			blockfric = 0.9F;
		}

		float totalbounciness = this.bounciness * blockbounce;
		float totalfriction = friction * blockfric * block.getSpeedFactor();
		
		// Clamp velocity in each axis to 0 if it is too small.
		prevvel = prevvel.multiply(Math.abs(prevvel.x)>1e-4 ? 1 : 0, Math.abs(prevvel.y)>1e-4 ? 1 : 0, Math.abs(prevvel.z)>1e-4 ? 1 : 0);

		// Reduce the elasticity if the ball moves too slow
		totalbounciness = BallPhysicsHelper.interpolateDown(totalbounciness, speed, idleSpeed);
		// Increase the (static) friction if the ball moves very slow
		totalfriction = BallPhysicsHelper.interpolateDown(totalfriction, speed, idleSpeed);
		
		switch(axis) {
		case X:
			shootvec = new Vec3(-prevvel.x*totalbounciness, prevvel.y*totalfriction, prevvel.z*totalfriction);
			break;
		case Y:
			if (prevvel.y <= 0) {
				if (Math.round(prevvel.y*100) == 0) 
					prevvel = prevvel.multiply(1,0,1);
			}
			shootvec = new Vec3(prevvel.x*totalfriction, -prevvel.y*totalbounciness, prevvel.z*totalfriction);
			break;
		case Z:
			shootvec = new Vec3(prevvel.x*totalfriction, prevvel.y*totalfriction, -prevvel.z*totalbounciness);
			break;
		}
		
		bounceBall(shootvec, speed > this.minDmgSpeed ? this.baseInaccuracy*0.7F : 0);
		return true;
	}
	
	@Override
	public boolean onEntityImpact(EntityHitResult result) {
		boolean successAttack = super.onEntityImpact(result);

		Entity target = result.getEntity();
		boolean isBouncyBall = target instanceof BouncyBallEntity;
		if (isBouncyBall && this.destroyChance == 1F)
			return successAttack;
		
		Vec3 velocity = this.getDeltaMovement();
		Vec3 targetvelocity = target.getDeltaMovement();
		
		if (target.isOnGround())
			targetvelocity = targetvelocity.multiply(1, 0, 1);
		if (this.isOnGround())
			velocity = velocity.multiply(1, 0, 1);

		Vec3 posvec = this.getCenterPositionVec();

		Vec3 otherposvec = BallPhysicsHelper.getEntityCapsulePos(target, posvec);
	    Vec3 posdiff = posvec.subtract(otherposvec);

	    // The normal vector from the target entity to this ball's position
	    Vec3 normal = posdiff.normalize();

		// Calculate this object's motion relative to the target entity.
		Vec3 relativemot = velocity.subtract(targetvelocity);
		
		// Relative speed along normal
	    double vn = relativemot.dot(normal);

		if (successAttack) {
			if (target.getType() == EntityType.ENDERMAN) {
				return successAttack;
			}
			// Code for ricochetting
			//Entity thrower = this.getOwner(); // Note: this is null on the client
//			boolean specialbounce = target != thrower && target instanceof LivingEntity && speed > 0.4F;
//			if (!this.level.isClientSide && specialbounce) {				
//				// Chance of bouncing towards a living entity
//				if (syncedrand.nextFloat() < ricochetChance) {
//					// Shoot towards the player that threw the ball, or otherwise shoot towards the closest mob
//					if (thrower instanceof Player && syncedrand.nextFloat() < bounceBackChance && ((LivingEntity)thrower).canSee(this)) {
//						bounceBall(thrower);
//						return successAttack;
//					} else {
//						// Find the closest entity
//						double d0 = -1.0D;
//						Entity nexttarget = null;
//						for (Entity entity : this.level.getEntities(this,
//								this.getBoundingBox().inflate(richochetRange), input -> {
//									return !input.isSpectator() && input.isPickable() && input != target && input instanceof LivingEntity
//											&& ((LivingEntity)input).canSee(this);
//								})) {
//							double d1 = entity.distanceToSqr(posvec);
//							if (d0 == -1.0D || d1 < d0) {
//								d0 = d1;
//								nexttarget = entity;
//							}
//						}
//
//						if (nexttarget != null) {
//							bounceBall(nexttarget);
//							return successAttack;
//						}
//					}
//				}
//			}
		}

		// If no special bounce has been performed, apply a normal bounce
		
		float mass1 = this.mass;
		float mass2 = 1;
    
		// Split the relative motion vector into a vector along the normal and a vector
		// perpendicular to the normal. 
		Vec3 newmotY = normal.scale(-1 * vn); // Assuming perfect elastic collision, apart from bounciness and friction
		
		// If you hit the target at an angle more than 90 degrees, you are moving away from from the target.
	    if (vn > 0.001F) {
	    	return false;
	    }

		Vec3 newmotXZ = relativemot.add(newmotY).reverse();
		Vec3 impulse;
		Vec3 newimpulse; // The new impulse/motion vector for this ball
		Vec3 othernewimpulse; // The new impulse/motion vector for the target entity
		BouncyBallEntity btarget = null;

		float entitybounciness = 0.5F;
		float entityfriction = 0.5F;
		if (target instanceof IronGolem) {
			entitybounciness = 0.8F;//TODO: test
			entityfriction = 0.9F;
		} else if (target instanceof Slime) {
				entitybounciness = 0.99F;
		}
		if (isBouncyBall) {
			btarget = ((BouncyBallEntity) target);
			entitybounciness = this.bounciness * btarget.bounciness;
			entityfriction = this.friction * btarget.friction;
		} else {
			entitybounciness = this.bounciness * entitybounciness;
			entityfriction = this.friction * entityfriction;
		}
		
		float relativespeed = (float) relativemot.length();
		
		// Reduce the elasticity if the ball moves too slow
		entitybounciness = BallPhysicsHelper.interpolateDown(entitybounciness, relativespeed, idleSpeed);
		// Increase the (static) friction if the ball moves very slow
		entityfriction = BallPhysicsHelper.interpolateDown(entityfriction, relativespeed, idleSpeed);

		// Calculate the new impulse vector for this ball and the other entity.
		if (isBouncyBall) {
			mass2 = btarget.mass;
		} else { // If the target is not another throwable ball
			// Estimate the target's mass based on volume
			mass2 = BallPhysicsHelper.estimateEntityMass(target);
		}
		final float totmass = mass1+mass2;

		impulse = (newmotY.scale((1+entitybounciness)/totmass)
				.add(newmotXZ.scale(entityfriction/totmass)));

		newimpulse = impulse.scale(mass2);
		othernewimpulse = impulse.scale(-mass1);
		
		boolean ignore_y = !isBouncyBall && this.mass <= 1.0F && posvec.y < otherposvec.y;
		if (this.isOnGround()) {
			if (!target.isOnGround() && !ignore_y)
				othernewimpulse = othernewimpulse.subtract(0, newimpulse.y, 0);
			newimpulse = newimpulse.multiply(1, 0, 1);
		}
		if (target.isOnGround()) { // If the target is on the ground you maintain your momentum in y axis
			othernewimpulse = othernewimpulse.multiply(1, 0, 1);
			if (!this.isOnGround())
				newimpulse = newimpulse.subtract(0, othernewimpulse.y, 0);
			else if (!isBouncyBall && !(target instanceof Player) && velocity.lengthSqr() <= this.idleSpeedSqr && this.mass > 1.0F && target.position().y < posvec.y && relativemot.multiply(1,0,1).dot(normal) < 0) {
				// If idle speed, heavy, the target's feet are below the ball's center and target is moving (horizontally) towards the ball, then make them jump over.
				target.setDeltaMovement(target.getDeltaMovement().x, 0.4F, target.getDeltaMovement().z);
			}
		}		
		
		// If the target is also a ball, check if it is agains a wall for more accuracy.
		if (isBouncyBall) {
			if (this.againstXWall) {
				if (!btarget.againstXWall) othernewimpulse = othernewimpulse.subtract(newimpulse.x,0,0);
				newimpulse = newimpulse.multiply(0,1,1);
			}
			if (btarget.againstXWall) {
				if (!this.againstXWall) newimpulse = newimpulse.subtract(othernewimpulse.x,0,0);
				othernewimpulse = othernewimpulse.multiply(0,1,1);
			}
		
			if (this.againstZWall) {
				if (!btarget.againstZWall) othernewimpulse = othernewimpulse.subtract(0,0,newimpulse.z);
				newimpulse = newimpulse.multiply(1,1,0);
			}
			if (btarget.againstZWall) {
				if (!this.againstZWall) newimpulse = newimpulse.subtract(0,0,othernewimpulse.z);
				othernewimpulse = othernewimpulse.multiply(1,1,0);
			}
		}
		
		// If the new impulse is too small, just set it to 0 instead.
		if (newimpulse.lengthSqr() < this.epsilonSpeedSqr) newimpulse = Vec3.ZERO;
		if (othernewimpulse.lengthSqr() < this.epsilonSpeedSqr) othernewimpulse = Vec3.ZERO;

		// Actually apply the new impulse
		this.bounceBall(this.getDeltaMovement().add(newimpulse), 0);

		// Also apply the new impulse to the target entity.
		if (isBouncyBall) {
			btarget.bounceBall(btarget.getDeltaMovement().add(othernewimpulse), 0);
		} else if (successAttack && target instanceof LivingEntity){
			LivingEntity ltarget = (LivingEntity) target;
			ltarget.hurtDir = (float)(Mth.atan2(-othernewimpulse.z, -othernewimpulse.x) * (double)(180F / (float)Math.PI) - (double)this.getYRot());
			ltarget.knockback((float) othernewimpulse.length()*1F, -othernewimpulse.x, -othernewimpulse.z);
		} else {
			target.setDeltaMovement(target.getDeltaMovement().add(othernewimpulse.x, othernewimpulse.y, othernewimpulse.z));
		}
		
		if (successAttack) {
			// Chance of getting destroyed
			if (this.random.nextFloat() < this.destroyChance) { 
				this.setDeltaMovement(Vec3.ZERO);
				this.destroy(RemovalReason.KILLED);
			}
		} else if (this.destroyChance == 1F) { 
			this.setDeltaMovement(Vec3.ZERO);
			this.destroy(RemovalReason.KILLED);
		}

		return successAttack;
	}
	
	/**
	    * Currently unused as it still needs tweaking.
	    * Bounces/shoots the ball at the nexttarget entity.
	*/
//	public void bounceBall(Entity nexttarget) {
//	    double xdir = nexttarget.getX() - this.getX();
//	    double yplus = 10.0D;
//	    double ydir = nexttarget.getEyeY() - this.getY();
//	    double ydirplus = ydir + yplus;
//	    double zdir = nexttarget.getZ() - this.getZ();
//
//	    double dist = Math.sqrt(xdir * xdir + 0.04D * ydirplus * ydirplus * ydirplus + zdir * zdir -  0.04D * yplus*yplus*yplus) + 0.15D * ydir;
//	    Vec3 shootvec = new Vec3(xdir, ydir, zdir);
//	    double hordist = Math.sqrt(getHorizontalDistanceSqr(shootvec));
//	    double getXRot() = -(Mth.atan2(shootvec.y, hordist) * 
//	    		(180D / Math.PI));
//	    float getYRot() = (float) -(Mth.atan2(shootvec.x, shootvec.z) * (180D / Math.PI));
//
//	    double powdist = Math.pow(dist, 0.6D);
//	    float speedscale = (float) Math.max(0.2D, 0.1475D * powdist + 0.03D);
//	    double distfrac = 1.0D / Math.max(((dist - 0.6D * ydir) * 0.05D + 1.0D), 1.0D);
//	    LOGGER.debug("dist bounce ball target " + dist + " distfrac " + distfrac + " rawdistnofrac " + ((dist - 0.6D * ydir) * 0.05D + 1.0D));
//	    // In principle, the ball is aimed at 45 degree angle if the target is at the same height position, otherwise converges to throwing straight. 
//	    // On the other hand, distfrac makes sure that the ball is almost thrown straight if the target is close, but converges to throwing
//	    // according to the above rule as the target moves further away.
//		double anglediff = (90 - Math.abs(getXRot()));
//	    float pitchrot = (float) ((getXRot() - 0.5D * (anglediff)) * (1.0D-distfrac) + getXRot() * distfrac);
//	    LOGGER.debug("bounce target powdist" + powdist + " getXRot() " + getXRot() + " pitchrot " + pitchrot);
//	    
//	    if (speedscale < 0 || Float.isNaN(speedscale)) {
//	    	LOGGER.debug("BAD SPEEDSCALE " + speedscale);
//	    	this.dropSelf();
//	    	return;
//	    }
//		this.shootFromRotation(this.getOwner(), (float) pitchrot, getYRot(), 0.0F, speedscale, this.baseInaccuracy);
//	}
	
	public void hitBall(Vec3 shootvec, float inaccuracy) {
		this.bounceBall(shootvec, inaccuracy);
	}

	/**
	    * Bounces/shoots the ball. Should only be called on the server.
	*/
	public void bounceBall(Vec3 shootvec, float inaccuracy) {
		markHurt();
		
	    setDeltaMovement(shootvec.normalize().add(this.random.nextDouble() * 0.0075D * (double)inaccuracy, 
	    		this.random.nextGaussian() * 0.0075D * (double)inaccuracy, 
	    		this.random.nextGaussian() * 0.0075D * (double)inaccuracy).scale((double)shootvec.length()));
	}

	/**
	 * Only called right after spawning.
	 */
	@Override
	public void shootFromRotation(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float speed, float inaccuracy) {
		if (!this.level.isClientSide)
			markHurt();
		super.shootFromRotation(entityThrower, rotationPitchIn, rotationYawIn, pitchOffset, speed, inaccuracy);
}
	
	/**
	 * Override because we want to handle this ourselves in onBlockImpact()
	 */
	@Override
	protected float getBlockSpeedFactor() {
		return 1.0F;
	}
	
	public int getHealth() {
		return health;
	}
}
