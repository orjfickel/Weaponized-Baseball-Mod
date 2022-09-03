package blizzardfenix.webasemod.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.BaseballMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class BallPhysicsHelper {
	protected static Logger LOGGER = LogManager.getLogger(BaseballMod.MODID + " BallPhysicsHelper");
	public static final float MAXBALLSPEEDSQR = 4F*4F;
	
	public BallPhysicsHelper() {
		
	}
	
	// For vanilla throwables
	public static void pickupThrowable(ThrowableItemProjectile throwable, Entity owner) {
		if (owner instanceof Player) {
			Player player = (Player) owner;
			if (player.getAbilities().instabuild || player.getInventory().add(throwable.getItem())) {
				((ServerLevel) player.level).getChunkSource().broadcast(throwable, new ClientboundTakeItemEntityPacket(throwable.getId(), player.getId(), 1));
	
				throwable.remove(RemovalReason.DISCARDED);
			}
		}
	}
	
	public static Vec2 getVelocityScaling(ThrowableBallEntity ball, BlockState hitblockstate, BlockState currblockstate, float speedSqr) {
		Block hitblock = hitblockstate.getBlock();
		Material material = hitblockstate.getMaterial();

		float blockbounce;
		// block.friction has 1 as zero friction and 0 as maximum friction, considering that ice has a friction value higher than regular blocks
		float blockfric = hitblock.getFriction();

		if (hitblock instanceof SlimeBlock) {
			blockbounce = 0.99F;
			blockfric = 0.9F;
		} else if (material == Material.DIRT || material == Material.CLAY) {
			blockbounce = 0.75F;
			blockfric = 0.9F;
		} else if (material == Material.GRASS) {
			blockbounce = 0.65F;
			blockfric = 0.9F;
		} else if (material == Material.SNOW || material == Material.SAND || material == Material.TOP_SNOW
				|| material == Material.CAKE || material == Material.LEAVES || material == Material.PLANT
				|| material == Material.REPLACEABLE_PLANT || material == Material.REPLACEABLE_WATER_PLANT
				|| material == Material.WATER_PLANT) {
			blockbounce = 0.6F;
			blockfric = 0.7F;
		} else {
			blockbounce = 0.8F;
			if (blockfric == 0.6F) blockfric = 0.95F;
		}
		
		//Override if we are inside snow or plants
		Material currmaterial = currblockstate.getMaterial();
		if (currmaterial == Material.TOP_SNOW || currmaterial == Material.PLANT
				|| currmaterial == Material.REPLACEABLE_PLANT || currmaterial == Material.REPLACEABLE_WATER_PLANT
				|| currmaterial == Material.WATER_PLANT) {
			blockbounce = 0.6F;
			blockfric = 0.7F;
		}

		float totalbounciness = ball.bounciness * blockbounce * hitblock.getJumpFactor();
		float totalfriction = ball.friction * blockfric * hitblock.getSpeedFactor();

		float speed = Mth.sqrt(speedSqr);
		// Reduce the elasticity if the ball moves too slow
		totalbounciness = BallPhysicsHelper.interpolateDown(totalbounciness, speed, ball.idleSpeed);
		// Increase the (static) friction if the ball moves very slow
		totalfriction = BallPhysicsHelper.interpolateDown(totalfriction, speed, ball.idleSpeed);
				
		return new Vec2(ball.bounciness >= 1.0F ? (speedSqr < BallPhysicsHelper.MAXBALLSPEEDSQR ? ball.bounciness : 1.0F) : totalbounciness, ball.friction >= 1.0F ?  (speedSqr < BallPhysicsHelper.MAXBALLSPEEDSQR ? ball.friction : 1.0F) : totalfriction);
	}

	/**
	 * 
	 * @param entity the colliding entity
	 * @param centerpos the ball's center position
	 * @return
	 */
	public static Vec3 getEntityCapsulePos(Entity entity, Vec3 centerpos) {
		Vec3 entitypos = entity.position();
		float halfwidth = entity.getBbWidth()/2;
		return new Vec3(entitypos.x,
				Mth.clamp(centerpos.y, entitypos.y + halfwidth, entitypos.y + entity.getBbHeight() - halfwidth),
				entitypos.z);
	}
	
	/**
	 * 
	 * @param entity the colliding entity
	 * @param centerpos the ball's center position
	 * @return
	 */
	public static Vec3 getEntityCylinderPos(Entity entity, Vec3 centerpos) {
		Vec3 entitypos = entity.position();
		return new Vec3(entitypos.x,
				Mth.clamp(centerpos.y, entitypos.y, entitypos.y + entity.getBbHeight()),
				entitypos.z);
	}
	
	public static float estimateEntityMass(Entity target) {
		float result = target.getBbWidth()*target.getBbWidth()*target.getBbHeight()*7F;
		if (target instanceof LivingEntity) {
			result += 5 * ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
			//if (((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue() > 0) result = 0.01F;
			if ((target instanceof Bat || target instanceof Parrot) && !target.isOnGround()) {
				// Small flying mobs can get knocked back a whole lot more
				result *= 0.5F;
			}
		}
		return result;
	}
	
	/**
	 * Interpolates var down to 0, based on speed, starting when speed is below slowspeed
	 */
	public static float interpolateDown(float var, float speed, float slowspeed) {
		if (speed < slowspeed) {
			if (speed > 0)
				return Math.max(var * speed / slowspeed, 0);
			else
				return 0;
		}
		return var;
	}

	public static BlockHitResult computeTargetBlock(Axis axis, Vec3 hitvec, Vec3 expmovement, Level level, EntityDimensions size) {
		Direction dir = null;
		double halfheight = size.height/2;
		double halfwidth = size.width/2;
		double relativeposx = hitvec.x - Math.floor(hitvec.x);
		double relativeposy = hitvec.y - Math.floor(hitvec.y);
		double relativeposz = hitvec.z - Math.floor(hitvec.z);

		// Check which direction the block side we hit faces, and calculate whether we hit it exactly at the side like we would if it was a simple square instead of something smaller.
		if (axis == Axis.X) { 
			if (expmovement.x > 0) {
				dir = Direction.WEST;
			} else {
				dir = Direction.EAST;
			}
		} else if (axis == Axis.Y) {
			if (expmovement.y > 0) {
				dir = Direction.DOWN;
			} else {
				dir = Direction.UP;
			}
		} else if (axis == Axis.Z) {

			if (expmovement.z > 0) {
				dir = Direction.NORTH;
			} else {
				dir = Direction.SOUTH;
			}
		}
		Vec3 rayVec = new Vec3(-dir.getStepX()*(halfwidth+0.1D), -dir.getStepY()*(halfheight+0.1D), -dir.getStepZ()*(halfwidth+0.1D));
		
		// Test whether we hit a block with the center of the ball/aabb. (Not that even if we missed a fast move ray trace, because we can hit at an angle that raytrace might have missed the block we hit)
		BlockHitResult raytraceresult = level.clip(new ClipContext(hitvec, hitvec.add(rayVec), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
		if (raytraceresult.getType() != BlockHitResult.Type.MISS) {
			return raytraceresult;
		}
		
		// Two booleans for each axis, denoting whether we hit near an edge and whether we hit high (h) or low (l) in the block in the axis direction
		// Calculate which edges of adjacent blocks we could have hit. (margin of error because move() will sometimes detect collisions where we shouldn't have hit an edge)
		double margin = 0.02D;
		boolean xedgeh = axis != Axis.X && 1 - relativeposx - halfwidth <= margin;
		boolean yedgeh = axis != Axis.Y && 1 - relativeposy - halfheight <= margin;
		boolean zedgeh = axis != Axis.Z && 1 - relativeposz - halfwidth <= margin;
		boolean xedgel = axis != Axis.X && relativeposx - halfwidth <= margin; // A block edge is hit at some distance along the x-axis. The ball center is outside of the block that is hit.
		boolean yedgel = axis != Axis.Y && relativeposy - halfheight <= margin;
		boolean zedgel = axis != Axis.Z && relativeposz - halfwidth <= margin;

		switch (axis) {
		case X:
			if (yedgeh || zedgeh || yedgel || zedgel) {
				raytraceresult = tryClosestEdges(rayVec, yedgeh, zedgeh, yedgel, zedgel, hitvec, level, size, 
						relativeposy, relativeposz, yedgeh ? Direction.UP : Direction.DOWN, zedgeh ? Direction.SOUTH : Direction.NORTH);
				return raytraceresult;
			}
			break;
		case Y:
			if (xedgeh || zedgeh || xedgel || zedgel) {
				raytraceresult = tryClosestEdges(rayVec, xedgeh, zedgeh, xedgel, zedgel, hitvec, level, size, 
						relativeposx, relativeposz, xedgeh ? Direction.EAST : Direction.WEST, zedgeh ? Direction.SOUTH : Direction.NORTH);
				return raytraceresult;
			}
			break;
		case Z:
			if (xedgeh || yedgeh || xedgel || yedgel) {
				raytraceresult = tryClosestEdges(rayVec, xedgeh, yedgeh, xedgel, yedgel, hitvec, level, size, 
						relativeposx, relativeposy, xedgeh ? Direction.EAST : Direction.WEST, yedgeh ? Direction.UP : Direction.DOWN);
				return raytraceresult;
			}
			break;
		}
		
		// Hitting neither the z or x edge, means we hit something at the surface of a block, but didn't detect a block with the raytrace
		// This could happen for example when hitting the top of a glass pane slightly off center.
		
		Vec3 contactPoint = hitvec.subtract(dir.getStepX()*(halfwidth), dir.getStepY()*(halfheight), dir.getStepZ()*(halfwidth));
		BlockPos blockpos = new BlockPos(hitvec.subtract(rayVec));
		Block hitblock = level.getBlockState(blockpos).getBlock();
		if (hitblock instanceof AirBlock) {
			return BlockHitResult.miss(contactPoint, dir, blockpos);
		}
		
		return new BlockHitResult(contactPoint, dir, blockpos, false);
	}
	

	public static BlockHitResult tryClosestEdges(Vec3 rayVec, boolean edgeh1, boolean edgeh2, boolean edgel1, boolean edgel2, 
			Vec3 hitvec, Level level, EntityDimensions size, double relativepos1, double relativepos2, Direction dir1, Direction dir2) {

		double comparepos1 = 1, comparepos2 = 1;
		if (edgeh1)
			comparepos1 = 1 - relativepos1;
		else if (edgel1)
			comparepos1 = relativepos1;
		if (edgeh2)
			comparepos2 = 1 - relativepos2;
		else if (edgel2)
			comparepos2 = relativepos2;
		
		if (comparepos1 < comparepos2) {
			// Test 1 first
			return tryOrderedEdges(rayVec, edgeh2 || edgel2, hitvec, level, size, dir1, dir2);
		}
		return tryOrderedEdges(rayVec, edgeh1 || edgel1, hitvec, level, size, dir2, dir1);
	}

	/**
	 * Checks if a block exists at another edge. First 1 is tested, then 2 if edgel2 or edgeh2 is true.
	 @param axisNormal should be the normal of the block face we hit.
	 @param axisToTest is what this function tests, plus also the corners between the two axes.
	 @return Where (and whether) a block has been found.
	 */
	public static BlockHitResult tryOrderedEdges(Vec3 rayVec, boolean edge2,
			Vec3 hitvec, Level level, EntityDimensions size, Direction dir1, Direction dir2) {
		BlockHitResult raytraceresult = null;
		double halfheight = size.height/2;
		double halfwidth = size.width/2;
		double margin = 0.02D;

		// Move the raycasts to slightly past the edge of the ball AABB
		Vec3 dirVec1 = new Vec3(dir1.getStepX()*(halfwidth+margin), dir1.getStepY()*(halfheight+margin), dir1.getStepZ()*(halfwidth+margin));
		
		// If we hit an edge, there is a chance we hit either a corner or a regular edge.
		// Therefore we should first check for regular edges, and then for corners.
		Vec3 temppos = hitvec.add(dirVec1);
		raytraceresult = level.clip(new ClipContext(temppos, temppos.add(rayVec), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
		if (raytraceresult.getType() != BlockHitResult.Type.MISS) {
			return raytraceresult;
		}
		
		// Test edge 2 if close enough
		if (edge2) {
			temppos = hitvec.add(dir2.getStepX()*(halfwidth+margin), dir2.getStepY()*(halfheight+margin), dir2.getStepZ()*(halfwidth+margin));
			raytraceresult = level.clip(new ClipContext(temppos, temppos.add(rayVec), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
			if (raytraceresult.getType() == BlockHitResult.Type.MISS) {
				return raytraceresult;
			}
			
			// Move temppos to the corner
			temppos = temppos.add(dirVec1);
			raytraceresult = level.clip(new ClipContext(temppos, temppos.add(rayVec), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
		}

		return raytraceresult;
	}
}
