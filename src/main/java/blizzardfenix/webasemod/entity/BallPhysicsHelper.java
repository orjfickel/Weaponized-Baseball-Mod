package blizzardfenix.webasemod.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.BaseballMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BallPhysicsHelper {
	protected static Logger LOGGER = LogManager.getLogger(BaseballMod.MODID + " BallPhysicsHelper");
	
	public BallPhysicsHelper() {
		
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
		BlockPos blockpos;
		Vec3 blockvec = null;
		Direction dir = null;
		double halfheight = size.height/2;
		double halfwidth = size.width/2;
		double relativeposx = -1;
		double relativeposy = -1;
		double relativeposz = -1;
		boolean hitexactblockside = false;
		blockvec = hitvec;
		relativeposx = hitvec.x - Math.floor(hitvec.x);
		relativeposy = hitvec.y - Math.floor(hitvec.y);
		relativeposz = hitvec.z - Math.floor(hitvec.z);

		// Check which direction the block side we hit faces, and calculate whether we hit it exactly at the side like we would if it was a simple square instead of something smaller.
		if (axis == Axis.X) { 
			if (expmovement.x > 0) {
				dir = Direction.WEST;
				hitexactblockside = Math.abs(relativeposx + halfwidth - 1) < 1E-5F;
			} else {
				dir = Direction.EAST;
				hitexactblockside = Math.abs(relativeposx - halfwidth) < 1E-5F;	
			}
		} else if (axis == Axis.Y) {
			if (expmovement.y > 0) {
				dir = Direction.DOWN;
				hitexactblockside = Math.abs(relativeposy + halfheight - 1) < 1E-5F;
			} else {
				dir = Direction.UP;
				hitexactblockside = Math.abs(relativeposy - halfheight) < 1E-5F;
			}
		} else if (axis == Axis.Z) {

			if (expmovement.z > 0) {
				dir = Direction.NORTH;
				hitexactblockside = Math.abs(relativeposz + halfwidth - 1) < 1E-5F;
			} else {
				dir = Direction.SOUTH;
				hitexactblockside = Math.abs(relativeposz - halfwidth) < 1E-5F;
			}
		}
		
		if (hitexactblockside) {
			blockvec = blockvec.subtract(axis == Axis.X ? dir.getStepX() : 0, axis == Axis.Y ? dir.getStepY() : 0, axis == Axis.Z ? dir.getStepZ() : 0);

			// Test whether we hit a block head on.
			BlockHitResult raytraceresult = level.clip(new ClipContext(hitvec, hitvec.add(axis == Axis.X ? expmovement.x : 0, axis == Axis.Y ? expmovement.y : 0, axis == Axis.Z ? expmovement.z : 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
			
			if (raytraceresult.getType() == BlockHitResult.Type.MISS) {
				// Two booleans for each axis, denoting whether we hit near an edge and whether we hit high (h) or low (l) in the block in the axis direction
				// Calculate which edges of adjacent blocks we could have hit. (large margin of error because move() will sometimes detect collisions where we shouldn't have hit an edge)
				boolean xedgeh = axis != Axis.X && 1 - relativeposx - halfwidth <= 0.1D;
				boolean yedgeh = axis != Axis.Y && 1 - relativeposy - halfheight <= 0.1D;
				boolean zedgeh = axis != Axis.Z && 1 - relativeposz - halfwidth <= 0.1D;
				boolean xedgel = axis != Axis.X && relativeposx - halfwidth <= 0.1D; // A block edge is hit at some distance along the x-axis. The ball center is outside of the block that is hit.
				boolean yedgel = axis != Axis.Y && relativeposy - halfheight <= 0.1D;
				boolean zedgel = axis != Axis.Z && relativeposz - halfwidth <= 0.1D;

				if (axis == Axis.X) { 
					HitType hitType = tryOtherEdge(axis, yedgeh, zedgeh, yedgel, zedgel, hitvec, expmovement, level, size);
					switch (hitType) {
					case MISS:
						// Do nothing if only misses
						if (yedgeh) // edgeh and edgel can't both be true, so we only need to set one as false;
							yedgeh = false;
						else
							yedgel = false;
						if (zedgeh)
							zedgeh = false;
						else {
							zedgel = false;
						}
						break;
					case EDGE1:
						// We hit the y edge, so disable the z edge booleans.
						if (zedgeh)
							zedgeh = false;
						else {
							zedgel = false;
						}
						break;
					case EDGE2:
						if (yedgeh)
							yedgeh = false;
						else
							yedgel = false;
						break;
					case CORNER:
						// Leave both booleans on
						break;
					}
				}
				if (axis == Axis.Y) { 
					HitType hitType = tryOtherEdge(axis, xedgeh, zedgeh, xedgel, zedgel, hitvec, expmovement, level, size);
					switch (hitType) {
					case MISS:
						if (xedgeh)
							xedgeh = false;
						else
							xedgel = false;
						if (zedgeh)
							zedgeh = false;
						else {
							zedgel = false;
						}
						break;
					case EDGE1:
						if (zedgeh)
							zedgeh = false;
						else {
							zedgel = false;
						}
						break;
					case EDGE2:
						if (xedgeh)
							xedgeh = false;
						else
							xedgel = false;
						break;
					case CORNER:
						break;
					}
				}
				if (axis == Axis.Z) { 
					HitType hitType = tryOtherEdge(axis, xedgeh, yedgeh, xedgel, yedgel, hitvec, expmovement, level, size);
					switch (hitType) {
					case MISS:
						if (xedgeh)
							xedgeh = false;
						else
							xedgel = false;
						if (yedgeh)
							yedgeh = false;
						else {
							yedgel = false;
						}
						break;
					case EDGE1:
						if (yedgeh)
							yedgeh = false;
						else {
							yedgel = false;
						}
						break;
					case EDGE2:
						if (xedgeh)
							xedgeh = false;
						else
							xedgel = false;
						break;
					case CORNER:
						break;
					}
				}
				
				// Adjust blockpos according which edge(s) we hit.
				if (xedgeh) {
					 // Hit just the high x edge
					blockvec = blockvec.add(1, 0, 0);
				} else if (xedgel) {
					 // Hit just the low x edge
					blockvec = blockvec.subtract(1, 0, 0);
				}
				if (yedgeh) {
					 // Hit just the high y edge
					blockvec = blockvec.add(0, 1, 0);
				} else if (yedgel) {
					 // Hit just the low y edge
					blockvec = blockvec.subtract(0, 1, 0);
				}
				if (zedgeh) {
					 // Hit just the high z edge
					blockvec = blockvec.add(0, 0, 1);
				} else if (zedgel) {
					 // Hit just the low z edge
					blockvec = blockvec.subtract(0, 0, 1);
				}
				// Hitting neither the z or x edge, means we hit something at the surface of a block, but didn't detect a block with the raytrace
				// This could happen for example when hitting the top of a glass pane slightly off center.
			}
		} else {
			// Leave blockvec to be equal to origpos
		}
		blockpos = new BlockPos(blockvec);
		
		return new BlockHitResult(hitvec, dir, blockpos, false);
	}

	/**
	 * Checks if a block exists at another edge.
	 @param axisNormal should be the normal of the block face we hit.
	 @param axisToTest is what this function tests, plus also the corners between the two axes.
	 @return Where (and whether) a block has been found.
	 */
	public static HitType tryOtherEdge(Axis axisNormal, boolean edgeh1, boolean edgeh2, boolean edgel1, boolean edgel2, 
			Vec3 hitvec, Vec3 expmovement, Level level, EntityDimensions size) {
		BlockHitResult raytraceresult = null;
		double halfheight = size.height/2;
		double halfwidth = size.width/2;
		Axis axisToTest = Axis.X;
		switch (axisNormal) {
		case X:
			axisToTest = Axis.Y;
			break;
		case Y:
			axisToTest = Axis.X;
			break;
		case Z:
			axisToTest = Axis.X;
			break;
		}
		
		Vec3 normalMovement = new Vec3(	axisNormal == Axis.X ? expmovement.x : 0, 
												axisNormal == Axis.Y ? expmovement.y : 0, 
												axisNormal == Axis.Z ? expmovement.z : 0);
		boolean edge1 = edgeh1 || edgel1;
		boolean edge2 = edgeh2 || edgel2;
		Vec3 temppos = hitvec;

		// If we hit an edge, there is a chance we hit either a corner or a regular edge.
		// Therefore we should first check for regular edges, and then for corners.
		if (edge1) {
			temppos = hitvec.add(	axisToTest == Axis.X ? halfwidth * (edgeh1 ? 1 : -1) : 0, 
									axisToTest == Axis.Y ? halfheight * (edgeh1 ? 1 : -1) : 0, 
									axisToTest == Axis.Z ? halfwidth * (edgeh1 ? 1 : -1) : 0);
			raytraceresult = level.clip(new ClipContext(temppos, temppos.add(normalMovement), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
		}
		if (!edge1 || raytraceresult.getType() == BlockHitResult.Type.MISS) {
			if (edge2) {
				temppos = hitvec.add(	axisNormal != Axis.X && axisToTest != Axis.X ? halfwidth * (edgeh2 ? 1 : -1) : 0, 
										axisNormal != Axis.Y && axisToTest != Axis.Y ? halfheight * (edgeh2 ? 1 : -1) : 0, 
										axisNormal != Axis.Z && axisToTest != Axis.Z ? halfwidth * (edgeh2 ? 1 : -1) : 0);
				raytraceresult = level.clip(new ClipContext(temppos, temppos.add(normalMovement), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
			}
			if (!edge2 || raytraceresult.getType() == BlockHitResult.Type.MISS) {
				if (edge1 && edge2) {
					// Since edge2 is true, temppos is already the position of the block next to the ball along 2nd axis, 
					// so now we move 1 along the 1st axis as well for the corner.
					temppos = temppos.add(	axisToTest == Axis.X ? halfwidth * (edgeh1 ? 1 : -1) : 0, 
											axisToTest == Axis.Y ? halfheight * (edgeh1 ? 1 : -1) : 0, 
											axisToTest == Axis.Z ? halfwidth * (edgeh1 ? 1 : -1) : 0);
					raytraceresult = level.clip(new ClipContext(temppos, temppos.add(normalMovement), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
				}
				if (!edge1 || !edge2 || raytraceresult.getType() == BlockHitResult.Type.MISS) {
					// Do nothing if only misses
					return HitType.MISS;
				} else {
					// Leave both booleans true
					return HitType.CORNER;
				}
			} else {
				return HitType.EDGE2;
			}
		} else {
			return HitType.EDGE1;
		}
	}
	
	public static enum HitType {
		MISS,
		EDGE1,
		EDGE2,
		CORNER
	}
}
