package blizzardfenix.webasemod.entity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.config.ServerConfig;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.init.ModItems;
import blizzardfenix.webasemod.init.ThrowableProperties;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

// An item projectile that collides with blocks and entities, only lacks the bouncing logic
public abstract class ThrowableBallEntity extends ThrowableItemProjectile implements IEntityAdditionalSpawnData {
	private static final EntityDataAccessor<Byte> BOOLS = SynchedEntityData.defineId(ThrowableBallEntity.class, EntityDataSerializers.BYTE);
	public UUID[] ballEntityHits; // All the ids of entities that were collided with during the current tick.
	// There can be 'holes' in this array when another ball handles the collisions with this ball.
	private int ballEntityHitsIndex = 0;
	private int maxBallCramming;

	/** Whether this ball has been hit with an infinity bat already (and therefore should not add more copies to the player's inventory) */
	public boolean infinityHit = false;
	public boolean hitbybat = false;
	public float comboDmg = 0;
	protected boolean leftOwner;
	
	// Not very memory efficient but to prevent having to copy paste all the minecraft code that only executes for instances of AbstractArrow
	private UUID arrowId;
	private MockArrow mockArrow;

	// Variables that are initialised in/with ThrowableProperties
	/** In case of fire damage  */
	public int health;
	public float mass;
	public float baseDmg = -1;
	public float batHitDmg;
	public float bounciness;
	public float baseInaccuracy;
	public float throwSpeed;
	public float batHitSpeed;
	public float friction;
	public float minDmgSpeed;
	/** Probability that this ball gets destroyed upon hurting an entity */
	public float destroyChance;
	public float airResistance;
	public boolean useOnEntityHit;
	public boolean useOnBlockHit;
	public boolean useOnIdle;
	
	float droptimer = -1;
	public float idleTime;// Set in the config file (default 1200)

	/** The speed at which static friction etc starts working */
	final float idleSpeed = 0.05F;
	final float idleSpeedSqr = idleSpeed*idleSpeed;
	final float epsilonSpeedSqr = 1E-5F;
	final float fastmoveSpeedSqr = 0.2F*0.2F;
	public boolean againstXWall = false;
	public boolean againstZWall = false;
	protected Vec3 totPosCorrection = Vec3.ZERO;
	protected int posCorrectionCount = 0;
	protected Vec3 stuckSpeedOverride = Vec3.ZERO;
	
	protected Logger LOGGER;
	public boolean tracking; // Debug variable to select individual balls for debugging
	public Pickup Pickup;
	
	/** Never use this for the first time spawning of an entity, as parameters will be uninitialised */
	public ThrowableBallEntity(EntityType<? extends ThrowableBallEntity> type, Level worldIn) {
		super(type,worldIn);
		setup();
	}

	public ThrowableBallEntity(EntityType<? extends ThrowableBallEntity> type, Level worldIn, LivingEntity throwerIn) {
	    super(type, throwerIn, worldIn);
		setup();
	}
	
	public ThrowableBallEntity(EntityType<? extends ThrowableBallEntity> type, Level worldIn, double x, double y, double z) {
		super(type, x, y, z, worldIn);
	    setup();
	}
	
	protected void setup() {
		LOGGER = LogManager.getLogger(BaseballMod.MODID + " BaseballEntity " + this.getId());
		this.setInvulnerable(false);
		this.maxBallCramming = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
		this.ballEntityHits = new UUID[this.maxBallCramming];
		if (this.Pickup == null) {
			this.Pickup = Pickup.DISALLOWED;
		}
		this.idleTime = ServerConfig.throwable_idle_time.get();

        this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner(), this.blockPosition());
	}
	
	public void initProperties(Item item) {
		ThrowableProperties properties;
		
		ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
		if (item == ModItems.BASIC_BASEBALL.get())
			properties = ModEntityTypes.BASEBALL_PROPERTIES;
		else if (item == ModItems.DIRTBALL.get())
			properties = ModEntityTypes.DIRTBALL_PROPERTIES;
		else if (item == ModItems.STONEBALL.get())
			properties = ModEntityTypes.STONEBALL_PROPERTIES;
		else if (item == ModItems.CORKBALL.get())
			properties = ModEntityTypes.CORKBALL_PROPERTIES;
		else if (item == Items.DIAMOND)
			properties = ModEntityTypes.DIAMOND_PROPERTIES;
		else if (item == Items.EMERALD)
			properties = ModEntityTypes.EMERALD_PROPERTIES;
		else if (tags.getTag(tags.createTagKey(new ResourceLocation("forge","ingots"))).contains(item) && item != Items.BRICK)
			properties = ModEntityTypes.INGOT_PROPERTIES;
		else if (tags.getTag(tags.createTagKey(new ResourceLocation("minecraft","coals"))).contains(item))
			properties = ModEntityTypes.COAL_PROPERTIES;
		else if (tags.getTag(tags.createTagKey(new ResourceLocation("forge","nuggets"))).contains(item))
			properties = ModEntityTypes.NUGGET_PROPERTIES;
		else if (item == Items.TURTLE_EGG || tags.getTag(tags.createTagKey(new ResourceLocation("forge","eggs"))).contains(item))
			properties = ModEntityTypes.EGG_PROPERTIES;
		else if (tags.getTag(tags.createTagKey(new ResourceLocation("forge","slimeballs"))).contains(item))
			properties = ModEntityTypes.SLIMEBALL_PROPERTIES;
		else if (item == Items.FIRE_CHARGE)
			properties = ModEntityTypes.FIREBALL_PROPERTIES;
		else
			properties = ModEntityTypes.BRICK_PROPERTIES;

		this.health = properties.health;
		this.mass = properties.mass;
		this.baseDmg = properties.baseDmg;
		this.batHitDmg = properties.batHitDmg;
		this.bounciness = Settings.overrideBounciness ? Settings.bounciness : properties.bounciness;
		this.friction = Settings.overrideFriction ? Settings.friction : properties.friction;
		this.baseInaccuracy = properties.baseInaccuracy;
		this.throwSpeed = properties.throwSpeed;
		this.batHitSpeed = properties.batHitSpeed;
		this.minDmgSpeed = properties.minDmgSpeed;
		this.destroyChance = properties.destroyChance;
		this.airResistance = properties.airResistance;
		this.useOnEntityHit = properties.useOnEntityHit;
		this.useOnBlockHit = properties.useOnBlockHit;
		this.useOnIdle = properties.useOnIdle;
	}
	
	@Override
	public void tick() {
		if (this.baseDmg == -1)
			this.initProperties(this.getItem().getItem());
		
		Vec3 motionvec = this.getDeltaMovement();
		if (!this.level.isClientSide) {
			if ((this.isOnGround() || this.isInWater()) && motionvec.lengthSqr() < this.idleSpeedSqr) {
                // If idle for long enough, destroy
				if(this.tickCount > this.droptimer + this.idleTime) {
					if (ServerConfig.drop_balls.get())
						this.dropSelf();
					else
						this.remove(RemovalReason.DISCARDED);
				}
                // Reset the combo
                this.comboDmg = 0;
			} else {
				this.droptimer = this.tickCount;
			}
		}

		if (!this.leftOwner) {
			this.leftOwner = this.checkLeftOwner();
		}
		
		if (!this.level.isClientSide) { // Copied from Entity.Tick()
			this.setSharedFlag(6, this.isCurrentlyGlowing());
		}
		this.baseTick();
	      
		double motx = motionvec.x;
		double moty = motionvec.y;
		double motz = motionvec.z;
		if (Math.abs(motx) < 1e-5D) {
			motx = 0.0D;
		}

		if (Math.abs(moty) < 1e-5D) {
			moty = 0.0D;
		}

		if (Math.abs(motz) < 1e-5D) {
			motz = 0.0D;
		}
		motionvec = new Vec3(motx,moty,motz);
				
		this.throwableTick();
	}

	/**
	 * Called to update the entity's movement. Partly adapted from ThrowableEntity for more control.
	 */
	public void throwableTick() {
		Arrays.fill(this.ballEntityHits, null); // Reset since we haven't detected any hits in this tick yet.
		this.ballEntityHitsIndex = 0;
		
		Vec3 velocity = this.getDeltaMovement();
		// Apply gravity
		if (!this.isNoGravity()) { 
			velocity = velocity.subtract(0,this.getGravity(),0);
		}
		// Add drag from things like webs
		if (this.stuckSpeedOverride.lengthSqr() > this.epsilonSpeedSqr) {
			velocity = velocity.multiply(this.stuckSpeedOverride);
			this.stuckSpeedOverride = Vec3.ZERO;
		}
		float drag;
		if (this.isInWater()) {
			// Add buoyancy
			velocity = new Vec3(velocity.x, velocity.y + (this.getFluidHeight(FluidTags.WATER) < 0.6F ? 
	        		(this.getFluidHeight(FluidTags.WATER) < 0.35F ? 
	        				(this.getFluidHeight(FluidTags.WATER) < 0.25F ? 0.029 : 0.03) : 0.031) : 0.05), 
					velocity.z);
			// Add bubble particles
			if (velocity.y < 0.1) {
				for (int i = 0; i < Math.min(Math.round(velocity.length() * 10), 4); ++i) {
					this.level.addParticle(ParticleTypes.BUBBLE, this.getX() - velocity.x * 0.25D,
							this.getY() - velocity.y * 0.25D, this.getZ() - velocity.z * 0.25D, 
							velocity.x, velocity.y, velocity.z);
				}
			}
			// Increase drag when under water
			if (this.getFluidHeight(FluidTags.WATER) > 0.35F) {
				drag = 0.95F;
			} else {
				drag = 0.8F;
			}
		} else {
			drag = airResistance; // Air resistance
		}

		velocity = velocity.scale((double) drag);
		this.setDeltaMovement(velocity);

		if (velocity.lengthSqr() > this.fastmoveSpeedSqr) {
			this.fastMove();
		} else {
			this.slowMove(); // Also handles changing position
			this.testInsideBlock();
		}
        this.tryCheckInsideBlocks();

		this.updateRotation(); 

		this.totPosCorrection = Vec3.ZERO;
		this.posCorrectionCount = 0;
	}
	
	
	boolean isRegistered(ThrowableBallEntity entity) {
		boolean alreadyRegistered = false;
		// Check whether the target ball has already registered this collision, if not, register it.
		for(UUID tempid : entity.ballEntityHits) {
			if (tempid == this.getUUID()) {
				alreadyRegistered = true;
				break;
			}
		}
		return alreadyRegistered;
	}
	
	/** Registers an throwable ball collision */
	void registerHit(ThrowableBallEntity entity) {
		// Register the entity
		this.ballEntityHits[this.ballEntityHitsIndex++] = entity.getUUID();
	}
	
	/** Do entity collision detection with ray traces, then apply movement */
	protected void fastMove() {
		Vec3 pos = this.getCenterPositionVec();
		Vec3 mot = this.getDeltaMovement();
		Vec3 newpos = pos.add(mot);
		
		BlockHitResult blockres = this.level.clip(new ClipContext(this.getCenterPositionVec(), newpos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
		if (blockres.getType() != BlockHitResult.Type.MISS) {
			newpos = blockres.getLocation();
		}

		EntityHitResult entityres = ProjectileUtil.getEntityHitResult(this.level, this, pos, newpos, this.getBoundingBox().expandTowards(mot).inflate(1.0D), (entityIn) -> {
			return this.canHitEntity(entityIn);
		});

		boolean entityhit = false;
		Entity entity = null;
		boolean isThrowableBall = false;
		if (entityres != null) {
			entity = entityres.getEntity();
			isThrowableBall = entity instanceof ThrowableBallEntity;
			entityhit = !isThrowableBall || this.level.isClientSide || !this.isRegistered((ThrowableBallEntity) entity);
		}
		
		if (entityhit) {
			Vec3 expmovement = mot.scale(0.4F); // A very very rough estimation of where along the motion trajectory the ball actually hits the entity
			this.setPos(this.position().add(expmovement));
			this.setDeltaMovement(mot);
			if (!this.level.isClientSide) {
				if (isThrowableBall) this.registerHit((ThrowableBallEntity) entity);
				onEntityImpact(entityres);
			}
		} else if (blockres.getType() != BlockHitResult.Type.MISS) {
            // Since the ray trace will put the center of the ball as newpos inside of a block, we need to move it back a bit
            Vec3 offsetHitPos = null;
            switch(blockres.getDirection().getAxis()) {
            case X:
                offsetHitPos = newpos.subtract(mot.scale(this.getBbWidth()/(2*Math.abs(mot.x))));
                break;
            case Y:
                offsetHitPos = newpos.subtract(mot.scale(this.getBbHeight()/(2*Math.abs(mot.y)))).subtract(0,this.getBbHeight()/2,0);
                break;
            case Z:
                offsetHitPos = newpos.subtract(mot.scale(this.getBbWidth()/(2*Math.abs(mot.z))));
                break;
            };
            this.setPos(offsetHitPos.x, offsetHitPos.y, offsetHitPos.z);
			this.onBlockImpact(blockres, mot);
		} else {
			// If both ray traces missed, do a final collision check with the bounding box
			if(!this.level.isClientSide) markHurt();
			this.blockDetect(mot);
		}
	}
		
	/** Handles applying movement and collision detection with blocks. (Needs to do both because we need to move first to detect block collision).
	 * */
	protected void slowMove() {		
		Vec3 velocity = this.getDeltaMovement();
		boolean nomove = false;
		
		// If we are on the ground and not moving, do a simple raytrace to test if there is a block below us, in which case we can safely (and efficiently) remain in place.
		if (this.isOnGround() && this.isIdle()) {
			if (!this.level.isClientSide) {
				Vec3 pos = this.getCenterPositionVec();
				Vec3 newpos = pos.subtract(0,0.5F,0);
				BlockHitResult blockres = this.level.clip(new ClipContext(pos, newpos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
				nomove = blockres.getType() != BlockHitResult.Type.MISS;
				if (nomove)
					this.setDeltaMovement(Vec3.ZERO);
			} else {
				nomove = true;
				this.setDeltaMovement(Vec3.ZERO);
			}
		} 
		
		if (!nomove && velocity.lengthSqr() > this.epsilonSpeedSqr) {
			this.blockDetect(velocity);
		}

		// Do not handle further collision on the client or on lite mode
		if (this.level.isClientSide || ServerConfig.lite_mode.get())
			return;

		// Handle entity collisions after the block collisions, since in order to detect a block collision we need to have already moved and actually hit a block.
		this.boxCollideEntities();
		ProfilerFiller profiler = this.level.getProfiler();
		profiler.push("boxcollidemove");
		if (posCorrectionCount > 0) {
			Vec3 posCorrection = this.posCorrectionCount > 0 ? this.totPosCorrection.scale(1/(float)this.posCorrectionCount) : Vec3.ZERO;
			Vec3 allowedVec = collideBoundingBox(this, posCorrection, this.getBoundingBox(), this.level, List.of());
			this.setPos(this.position().add(allowedVec));
		}
		profiler.pop();
	}
	
	/** Detect if we hit a block based on whether our (expected) movement was blocked */
	void blockDetect(Vec3 expmovement) {
		Vec3 velocity = this.getDeltaMovement();
		Vec3 allowedmotionvec = collideBoundingBox(this, expmovement, this.getBoundingBox(), this.level, List.of());
		
		boolean xblock = (Math.round(expmovement.x*100) != Math.round(allowedmotionvec.x*100)) && expmovement.x != 0;
		boolean yblock = ((Math.round(expmovement.y*100) < Math.round(allowedmotionvec.y*100)) && expmovement.y < 0) || 
				((Math.round(expmovement.y*100) > Math.round(allowedmotionvec.y*100)) && expmovement.y > 0);
		boolean zblock = (Math.round(expmovement.z*100) != Math.round(allowedmotionvec.z*100)) && expmovement.z != 0;
		
		Vec3 hitpos = null;
		BlockHitResult blockres = null;
		this.setPos(this.position().add(allowedmotionvec));
		
		if (!xblock && !yblock && !zblock) {			
			// If there is no collision, simply move as far as the expected movement.
			if (expmovement.x != 0)
				this.againstXWall = false;
			if (expmovement.z != 0)
				this.againstZWall = false; // only works if pushed against wall not standing still
			if (expmovement.y != 0)
				this.onGround = false;
			return;
		}
		if (expmovement.x != 0)
			this.againstXWall = xblock;
		if (expmovement.z != 0)
			this.againstZWall = zblock;
		if (expmovement.y != 0)
			this.onGround = yblock && expmovement.y <= 0;

		// The position we hit is our original position plus the expected movement scaled so that the added x movement is exactly the allowed x movement.
		// Ensure allowedmotionvec moves us directly to the hitvec
		allowedmotionvec = expmovement.scale(xblock ? allowedmotionvec.x/expmovement.x :
											 yblock ? allowedmotionvec.y/expmovement.y :
													  allowedmotionvec.z/expmovement.z);
		// If the allowed movement is small enough, just set it to zero.
		if (allowedmotionvec.lengthSqr() < this.epsilonSpeedSqr) {
			allowedmotionvec = Vec3.ZERO;
		}
		hitpos = this.position().add(allowedmotionvec);

		if (xblock) {
			blockres = BallPhysicsHelper.computeTargetBlock(Axis.X, hitpos, expmovement, this.level, this.getDimensions(this.getPose()));
		}
		if (yblock) {
			blockres = BallPhysicsHelper.computeTargetBlock(Axis.Y, hitpos, expmovement, this.level, this.getDimensions(this.getPose()));
		}
		if (zblock) {
			blockres = BallPhysicsHelper.computeTargetBlock(Axis.Z, hitpos, expmovement, this.level, this.getDimensions(this.getPose()));
		}		
		
		if (xblock || yblock || zblock) {
			onBlockImpact(blockres, velocity);
		}
	}
	
	/** Handles entity collision using an aabb at the current position. Handles both velocity and position correction
	 * Returns whether a collision was detected 
	 * Adapted from LivingEntity.collideWithNearbyEntities() */
	protected boolean boxCollideEntities() {
		boolean collisiondetected = false;
		AABB aabb = this.getBoundingBox();
	    Set<Entity> entitySet = new HashSet<Entity>(this.level.getEntities(this, aabb, (input) -> {
			return this.canHitEntity(input);
		}));
	    	    
		if (!entitySet.isEmpty()) {
			int maxCramCount = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
			if (maxCramCount > 0 && entitySet.size() > maxCramCount - 1 && !this.level.isClientSide()) {
				int entityCount = 0;

				for (Entity entity : entitySet) {
					if (!entity.isPassenger()) {
						++entityCount;
					}
				}

				if (entityCount > maxCramCount - 1) {
					this.destroy(RemovalReason.DISCARDED);
				}
			}

			for (Entity entity : entitySet) {
				// If the target is also a throwable ball, handle collision like a sphere rather than the rectangular bounding box;
				if (entity instanceof ThrowableBallEntity) {
					ThrowableBallEntity btarget = ((ThrowableBallEntity) entity);
					Vec3 velocity = this.getDeltaMovement();
					float speed = (float) velocity.length();
					Vec3 posvec = this.getCenterPositionVec();
					Vec3 targetposvec = btarget.getCenterPositionVec();
					
					// If the ball is not going too fast, ignore outside of sphere
					// Also make sure we only handle this collision in 1 of the balls.
					if ((targetposvec.subtract(posvec).lengthSqr() <= (entity.getBbWidth() + this.getBbWidth()) * (entity.getBbWidth() + this.getBbWidth()) / 4 
							|| speed > 0.4F)) {
						
						if (this.isRegistered(btarget))
							continue;
						this.registerHit(btarget);
						
						collisiondetected = true;
						if (this.level.isClientSide)
							return true;
						
						// Make sure there is no overlap between the entities
						this.posCorrectionEntity(entity);

						onEntityImpact(new EntityHitResult(entity));
					}
				} else {
					collisiondetected = true;
					if (this.level.isClientSide)
						return true;
					// Make sure there is no overlap between the entities
					this.posCorrectionEntity(entity);
					onEntityImpact(new EntityHitResult(entity));
				}
			}
		}
		return collisiondetected;
	}
		
	protected void testInsideBlock() {
		Vec3 centerposition = this.position();
		BlockPos blockpos = new BlockPos(centerposition);
		if (this.level.getBlockState(blockpos).isCollisionShapeFullBlock(this.level, blockpos)) {
			this.moveTowardsClosestSpace(centerposition.x,centerposition.y,centerposition.z);
		}
	}

	/**
	 * Handles position correction for a given colliding entity.
	 * Adapted from {@link Entity#push} from 1.15 to include pushing in the y axis as well.
	 */
	public void posCorrectionEntity(Entity entityIn) {
		ProfilerFiller profiler = this.level.getProfiler();
		profiler.push("poscorrection");
		if (!entityIn.noPhysics && !this.noPhysics) {
	
			ThrowableBallEntity btarget = null;
			boolean targetIsThrowableBall = entityIn instanceof ThrowableBallEntity;
			if (targetIsThrowableBall) {
				btarget = (ThrowableBallEntity) entityIn;
			}
		    float im1 = this.mass; 
		    float im2 = 25;
	
			Vec3 posvec = this.getCenterPositionVec();
			Vec3 otherposvec = BallPhysicsHelper.getEntityCapsulePos(entityIn, posvec);
			Vec3 oldposvec = posvec;
			Vec3 otheroldposvec = otherposvec;
			Vec3 diff;
			Vec3 thisdiff;
			Vec3 otherdiff;
			// Sphere on sphere collision
			diff = posvec.subtract(otherposvec);
			
		    double dist = diff.length();
		    if (dist == 0) {// If they have the exact same positions somehow, still allow seperating them.
		    	diff = new Vec3(0.01,0.01,0.01); 
		    	dist = diff.length();
		    }

			double overlapdist = (this.getBbWidth()+entityIn.getBbWidth())/2 - dist;
			if (overlapdist <= 0) {

				profiler.pop();
				return;
			}
		    // Scale the diff vector to transform it from the difference between the sphere centers, to the difference between the sphere edges. Also slightly overestimate the distance.
		    diff = diff.scale(overlapdist/dist);
		    
			if (targetIsThrowableBall) {
			    im2 = btarget.mass;
			} else {
				im2 = BallPhysicsHelper.estimateEntityMass(entityIn);
			}

		    float thisinertia = im2 / (im1+im2);
		    float otherinertia = im1 / (im1+im2);
			thisdiff = diff.scale(thisinertia);
			otherdiff = diff.scale(otherinertia);
						
			// Handle not pushing entities into the ground
			if ((!this.isOnGround() && !entityIn.isOnGround())) {
				// Just add the vertical diff if neither are on the ground
				posvec = posvec.add(0,thisdiff.y,0);
				otherposvec = otherposvec.subtract(0,otherdiff.y,0);
			} else if (!this.isOnGround() || !entityIn.isOnGround()) {
				// If only one of the entities is on the ground, apply the full diff to the other one
				if (this.isOnGround()) {
					//otherposvec = otherposvec.subtract(0,diff.y,0);
				}
				if (entityIn.isOnGround()) {
					posvec = posvec.add(0,diff.y,0);
				}
			}
			// Also handle not pushing entities into walls
			if (targetIsThrowableBall) { // Essentially the same logic as above for isOnGround
				if (!this.againstXWall && !btarget.againstXWall) {
					posvec = posvec.add(thisdiff.x,0,0);
					otherposvec = otherposvec.subtract(otherdiff.x,0,0);
				} else if (this.againstXWall && btarget.againstXWall) {
					if (this.ballEntityHitsIndex > 0) {
						posvec = posvec.add(0.01,0,0);
						otherposvec = otherposvec.subtract(0.01,0,0);
					}
				} else {
					if (this.againstXWall) {
						otherposvec = otherposvec.subtract(diff.x,0,0);
					}
					if (btarget.againstXWall) {
						posvec = posvec.add(diff.x,0,0);
					}
				}
				if (!this.againstZWall && !btarget.againstZWall) {
					posvec = posvec.add(0,0,thisdiff.z);
					otherposvec = otherposvec.subtract(0,0,otherdiff.z);
				} else if (this.againstZWall && btarget.againstZWall) {
					if (this.ballEntityHitsIndex > 0) {
						posvec = posvec.add(0,0,0.01);
						otherposvec = otherposvec.subtract(0,0,0.01);
					}
				} else {
					if (this.againstZWall) {
						otherposvec = otherposvec.subtract(0,0,diff.z);
					}
					if (btarget.againstZWall) {
						posvec = posvec.add(0,0,diff.z);
					}
				}
			} else { // If the other entity is not a throwable ball
				if (this.againstXWall) {
					otherposvec = otherposvec.subtract(diff.x,0,0);
				} else {
					posvec = posvec.add(thisdiff.x,0,0);
					otherposvec = otherposvec.subtract(otherdiff.x,0,0);
				}
				if (this.againstZWall) {
					otherposvec = otherposvec.subtract(0,0,diff.z);
				} else {
					posvec = posvec.add(0,0,thisdiff.z);
					otherposvec = otherposvec.subtract(0,0,otherdiff.z);
				}
			}

			boolean getFullPushed = !targetIsThrowableBall && this.getDeltaMovement().lengthSqr() < 0.2F*0.2F && this.mass <= 1.0F;
			
			// If we get pushed something that is crouching it will have no velocity.
			// Because of this we need to do some position-based-dynamics to apply some impulse.
			if (entityIn.isCrouching()) {
				this.setDeltaMovement(this.getDeltaMovement().add(getFullPushed ? diff : posvec.subtract(oldposvec)));
			}
			
			this.totPosCorrection = this.totPosCorrection.add(getFullPushed ? diff : posvec.subtract(oldposvec));
			this.posCorrectionCount++;
			
			if (targetIsThrowableBall) {
				// It is possible for btarget's collision detection to have missed this collision, since we look at collision after moving.
				// Therefore we do not use totPosCorrection here.
				//TODO: alternative is to also register whenever a ball tests for collision with another but concludes that there is no collision.
				// That way we can still use totPosCorrection if we know that the target didn't miss this collision but simply hasn't been ticked yet.
				// Then use totPosCorrection for unticked targets and move for missed targets.
				Vec3 othertempmotionvec = entityIn.getDeltaMovement();
				boolean targetOnGround = entityIn.isOnGround();
				Vec3 allowedVec = collideBoundingBox(entityIn, otherposvec.subtract(otheroldposvec), entityIn.getBoundingBox(), entityIn.level, List.of());
				entityIn.setPos(entityIn.position().add(allowedVec));
			    entityIn.setOnGround(targetOnGround);
		    	entityIn.setDeltaMovement(othertempmotionvec);
			} else {
				Vec3 othertempmotionvec = entityIn.getDeltaMovement();
				boolean targetOnGround = entityIn.isOnGround();
			    if (!getFullPushed) entityIn.move(MoverType.SELF, otherposvec.subtract(otheroldposvec));
			    entityIn.setOnGround(targetOnGround);
			    if (otherposvec.y > posvec.y) {
			    	if (this.mass > 1.0F) entityIn.setOnGround(true);
				    entityIn.fallDistance = 0;
			    }
			    if (entityIn.isCrouching() && !getFullPushed) {
				    entityIn.setDeltaMovement(othertempmotionvec.add(otherposvec.subtract(otheroldposvec)));
				}
			    else
			    	entityIn.setDeltaMovement(othertempmotionvec);
			}
		}
		profiler.pop();
	}
	
	/**
	 * Handle a block collision.
	 */
	public boolean onBlockImpact(BlockHitResult result, Vec3 prevvel) {
		if (net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, result))
			return false;
		
		BlockPos blockPos = result.getBlockPos();
		BlockState hitblockstate = this.level.getBlockState(blockPos);
		Block blockhit = hitblockstate.getBlock();

        this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());

		// Play the impact sound
		float speed = (float) prevvel.length();
		float impactspeed = 0;
		switch(result.getDirection().getAxis()) {
		case X:
			impactspeed = Math.abs((float)prevvel.x);
			break;
		case Y:
			impactspeed = Math.abs((float)prevvel.y);
			break;
		case Z:
			impactspeed = Math.abs((float)prevvel.z);
			break;
		};
		if (impactspeed > 0.3F)
			this.playStepSound(blockPos, hitblockstate, Math.max(impactspeed * 1.0F + 0.1F, 0));
		
		if (!this.level.isClientSide) {
			// If we hit a target block, trigger TargetBlock's arrow hit functionality with our mock arrow, otherwise call onProjectileHit regularly
			if (blockhit instanceof TargetBlock) {
				this.getMockArrow();
				hitblockstate.onProjectileHit(this.level, hitblockstate, result, this.mockArrow);
			} else if (blockhit instanceof TurtleEggBlock && ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
				// If mob griefing is allowed, try to destroy an egg by mocking a player falling on it.
				blockhit.fallOn(this.level, hitblockstate, blockPos, level.players().get(0), impactspeed);
			} else if ((blockhit instanceof AbstractGlassBlock)
                    && ForgeEventFactory.getMobGriefingEvent(this.level, this) && random.nextFloat() * this.mass * impactspeed > 0.7F) {
				this.level.destroyBlock(blockPos, false);
				this.setDeltaMovement(this.getDeltaMovement().scale(0.4D));
				return false;
			} else if (blockhit instanceof IronBarsBlock && blockhit != Blocks.IRON_BARS
                    && ForgeEventFactory.getMobGriefingEvent(this.level, this) && random.nextFloat() * this.mass * impactspeed > 0.55F) {
				this.level.destroyBlock(blockPos, false);
				this.setDeltaMovement(this.getDeltaMovement().scale(0.65D));
				return false;
			} else if (blockhit instanceof NoteBlock) {
				this.triggerNoteblock(hitblockstate, blockPos, blockhit);
			} else if (blockhit instanceof CactusBlock) {
				this.hurt(DamageSource.CACTUS, 1.0F);
			} else {
				hitblockstate.onProjectileHit(this.level, hitblockstate, result, this);
	
				// Check if we are inside of a button block, in which case use the mock arrow to press the button.
				BlockState thisblockstate = this.level.getBlockState(this.blockPosition());
				Block thisblock = thisblockstate.getBlock();
				
				if (thisblock instanceof ButtonBlock) {
					this.getMockArrow();
				}
			}
		}

		ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
		Item item = this.getItem().getItem();
		if (item == Items.TURTLE_EGG || tags.getTag(tags.createTagKey(new ResourceLocation("forge","eggs"))).contains(item)) {
			this.setDeltaMovement(Vec3.ZERO);
			this.destroy(RemovalReason.KILLED);
			return false;
		}
		
		return !this.level.isClientSide;
	}
	
	void triggerNoteblock(BlockState hitblockstate, BlockPos blockPos, Block blockhit) {
		int newval = ForgeHooks.onNoteChange(this.level, blockPos, hitblockstate, hitblockstate.getValue(NoteBlock.NOTE),
				hitblockstate.cycle(NoteBlock.NOTE).getValue(NoteBlock.NOTE));

		hitblockstate = hitblockstate.setValue(NoteBlock.NOTE, newval);
		this.level.setBlock(blockPos, hitblockstate, 3);
		if (this.level.isEmptyBlock(blockPos.above())) {
			this.level.blockEvent(blockPos, blockhit, 0, 0);
		}
	}
	
	/**
	    * Returns whether target.hurt() returns true.
	*/
	public boolean onEntityImpact(EntityHitResult result) {
		if (net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, result))
			return false;
		
        this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
		
		Entity target = result.getEntity();
		Vec3 velocity = this.getDeltaMovement();
		float speed = (float) velocity.length();
		
		Entity thrower = this.getOwner(); // Note: this is null on the client
		DamageSource source;
			
		if (this.level.isClientSide)
			return false;
		
		boolean successAttack = false;
		boolean hitThrower = target.is(thrower);

		// If we're moving fast enough, try to hurt the target entity
		if (speed > minDmgSpeed) {
			float attackdmg = this.baseDmg;
            if (!hitThrower) attackdmg += this.comboDmg; // Add combo damage but prevent hurting yourself too badly
			attackdmg = (float) Mth.clamp(speed * attackdmg, 0.0D, Integer.MAX_VALUE);
			// If the attackdmg is lower than the minimum damage then use probability to still deal the correct damage on average
			int actualdmg = (attackdmg < 1F) ? (this.random.nextFloat() > attackdmg ? 0 : 1) : Mth.ceil(attackdmg);
			
			if (this.hitbybat && !hitThrower) {
				long j = (long) this.random.nextInt(actualdmg / 2 + 2); // Same bonus as arrows
				actualdmg = (int) Math.min(j + (long) actualdmg, Integer.MAX_VALUE);
                
                this.comboDmg += 3.0F; // Increase the combo damage for the next entity we hit
			}
			
			Entity sourceEntity;
			// Pretend to be an arrow if we hit a specific entity
			if (target instanceof MinecartTNT || target instanceof Wolf || target instanceof ServerPlayer
					|| target instanceof Shulker || target instanceof WitherBoss) {
				this.getMockArrow();
				sourceEntity = mockArrow;
			} else {
				sourceEntity = this;
			}
			
			if (thrower != null) {
				source = (new IndirectEntityDamageSource("ball", sourceEntity, thrower)).setProjectile();
				if (thrower instanceof LivingEntity) {
					((LivingEntity) thrower).setLastHurtMob(target);
				}
			} else {
				source = (new IndirectEntityDamageSource("ball", sourceEntity, this)).setProjectile();
			}
			Vec3 targetvelocity = target.getDeltaMovement();
			successAttack = target.hurt(source, actualdmg);
			target.setDeltaMovement(targetvelocity);
		}
		
		if (successAttack) {
			if (target.getType() == EntityType.ENDERMAN) {
				return successAttack;
			}
			if (target instanceof LivingEntity) {
				LivingEntity livingentity = (LivingEntity) target;
	
				if (thrower instanceof LivingEntity) {
					EnchantmentHelper.doPostHurtEffects(livingentity, thrower);
				}
			}
			
			if (this.isOnFire()) {
				target.setSecondsOnFire(3);
			}
		}
		
		return successAttack;
	}
	
	
	@Override
	protected void onInsideBlock(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof GrowingPlantBlock || block instanceof BushBlock) {
			this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
		}
	}
	
	@Override
	protected Vec3 limitPistonMovement(Vec3 expmovement) {
		BlockPos newPos = new BlockPos(getCenterPositionVec().add(expmovement));
		if (this.level.getBlockState(newPos).isCollisionShapeFullBlock(level, newPos)) {
			this.destroy(RemovalReason.KILLED);
		}
		this.setDeltaMovement(this.getDeltaMovement().add(expmovement));
		return expmovement;
	}

	/** Adapted from {@link ThrowableItemProjectile#canHitEntity} because leftOwner couldn't be modified without calling tick() */
	@Override
	protected boolean canHitEntity(Entity entityIn) {// isAlive is only false when the entity has died or removed
        if (!entityIn.isSpectator() && entityIn.isAlive() && !(entityIn instanceof MockArrow) && entityIn.isPickable() && (!ServerConfig.lite_mode.get() || !(entityIn instanceof ThrowableBallEntity))) {
			Entity owner = this.getOwner();
			return owner == null || (this.leftOwner || entityIn != owner);
		} else {
			return false;
		}
	}
	
	/** Adapted from {@link ThrowableItemProjectile#checkLeftOwner}*/
	protected boolean checkLeftOwner() {
		Entity owner = this.getOwner();
		if (owner != null) {
			for (Entity entity1 : this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), (input) -> {
				return !input.isSpectator() && input.isPickable();
			})) {
				if (entity1 == owner) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void dropSelf() {
		dropSelf(0);
	}
	public void dropSelf(float offsety) {
		spawnAtLocation(this.getItem(), offsety);
	    this.remove(RemovalReason.DISCARDED);
	}
	
	// Adapted from Entity.playStepSound to include a volume parameter
	protected void playStepSound(BlockPos pos, BlockState blockIn, float volume) {
		if (!blockIn.getMaterial().isLiquid()) {
			BlockState blockstate = this.level.getBlockState(pos.above());
	        SoundType soundtype = blockstate.getBlock() == Blocks.SNOW ? blockstate.getSoundType(this.level, pos, this) : blockIn.getSoundType(this.level, pos, this);
	        this.playSound(soundtype.getStepSound(), soundtype.getVolume() * volume, soundtype.getPitch());
		}
	}
		
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		if (this.arrowId != null)
			compound.put("mockarrow", NbtUtils.createUUID(this.arrowId));
		compound.putByte("pickup", (byte) this.Pickup.ordinal());

		compound.putInt("health", this.health);
		compound.putFloat("mass", this.mass);
		compound.putFloat("basedmg", this.baseDmg);
		compound.putFloat("batHitDmg", this.batHitDmg);
		compound.putFloat("bounciness", this.bounciness);
		compound.putFloat("friction", this.friction);
		compound.putFloat("baseinaccuracy", this.baseInaccuracy);
		compound.putFloat("throwSpeed", this.throwSpeed);
		compound.putFloat("batHitSpeed", this.batHitSpeed);
		compound.putFloat("minDmgSpeed", this.minDmgSpeed);
		compound.putFloat("destroyChance", this.destroyChance);
		compound.putFloat("airResistance", this.airResistance);
		compound.putBoolean("useOnEntityHit", this.useOnEntityHit);
		compound.putBoolean("useOnBlockHit", this.useOnBlockHit);
		compound.putBoolean("useOnIdle", this.useOnIdle);
		compound.putFloat("combodmg", this.comboDmg);
		
		compound.putBoolean("onGround", this.isOnGround());
		compound.putBoolean("againstXWall", this.againstXWall);
		compound.putBoolean("againstZWall", this.againstZWall);
		compound.putBoolean("infinityHit", this.infinityHit);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("mockarrow",10) && this.level instanceof ServerLevel) {
			this.arrowId = NbtUtils.loadUUID(compound.getCompound("mockarrow"));
		}
        this.Pickup = AbstractArrow.Pickup.byOrdinal(compound.getByte("pickup"));

        this.health = compound.getInt("health");
        this.mass = compound.getFloat("mass");
        this.baseDmg = compound.getFloat("basedmg");
        this.batHitDmg = compound.getFloat("batHitDmg");
        this.bounciness = compound.getFloat("bounciness");
        this.friction = compound.getFloat("friction");
        this.baseInaccuracy = compound.getFloat("baseinaccuracy");
        this.throwSpeed = compound.getFloat("throwSpeed");
        this.batHitSpeed = compound.getFloat("batHitSpeed");
        this.minDmgSpeed = compound.getFloat("minDmgSpeed");
        this.destroyChance = compound.getFloat("destroyChance");
        this.airResistance = compound.getFloat("airResistance");
        this.useOnEntityHit = compound.getBoolean("useOnEntityHit");
        this.useOnBlockHit = compound.getBoolean("useOnBlockHit");
        this.useOnIdle = compound.getBoolean("useOnIdle");
        this.comboDmg = compound.getFloat("combodmg");

		this.onGround =  compound.getBoolean("onGround");
		this.againstXWall =  compound.getBoolean("againstXWall");
		this.againstZWall =  compound.getBoolean("againstZWall");
		this.infinityHit =  compound.getBoolean("infinityHit");
	}
	
	/** Necessary for rendering */
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		buffer.writeBoolean(this.onGround);
		Entity owner = this.getOwner();
		buffer.writeInt(owner != null ? owner.getId() : -1);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf additionalData) {
		this.onGround = additionalData.readBoolean();
		
		int entityid = additionalData.readInt();
		if (entityid >= 0)
			this.setOwner((LivingEntity) this.level.getEntity(entityid));
	}

	@Override
	public void setOwner(@Nullable Entity owner) {
		super.setOwner(owner);
		if (owner instanceof Player) {
			this.Pickup = ((Player) owner).getAbilities().instabuild ? AbstractArrow.Pickup.CREATIVE_ONLY
					: AbstractArrow.Pickup.ALLOWED;
		}
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.level.isClientSide)
	         return false;

		Entity directEntity = source.getDirectEntity();
		// If we got hit by an arrow or trident, or we are relatively easily destroyable and hit a cactus, destroy this throwable.
		if (source == DamageSource.CRAMMING || (directEntity != null && (directEntity.getType().is(EntityTypeTags.ARROWS) 
				|| directEntity instanceof ThrownTrident)) || (source == DamageSource.CACTUS && random.nextFloat() < this.destroyChance * 10)) {
			this.destroy(RemovalReason.KILLED);
			return true;
		}
		
		if (directEntity instanceof Player) {
			Item heldItem = ((Player)directEntity).getMainHandItem().getItem();
			if (heldItem instanceof BaseballBat) {
				return true;
			} else if (heldItem instanceof TieredItem) {
				this.destroy(RemovalReason.KILLED);
				return true;
//			} else if (heldItem == Items.STICK) {// For debugging
//				this.tracking = !this.tracking;
//				return true;
			}
		}

		if (source.isFire()) {
			this.markHurt();
			this.health = (int)((float)this.health - amount);
			if (this.health <= 0) {
			   this.remove(RemovalReason.KILLED);
			}
		}
		return false;
	}
	
	/**
	 * Called by a player entity when they collide with an entity. Adapted from {@link AbstractArrow#playerTouch}
	 */
	@Override
	public void playerTouch(Player player) {
		// If the ball is idle, check if the ball can be picked up.
		if (!this.level.isClientSide && !player.isCrouching() && (this.isIdle() || this.getNoPhysics())) {
			this.pickup(player);
		}
	}
	
	public void pickup(Player player) {
		boolean flag = this.Pickup == AbstractArrow.Pickup.ALLOWED
				|| (this.Pickup == AbstractArrow.Pickup.CREATIVE_ONLY
						&& player.getAbilities().instabuild)
				|| this.getNoPhysics() && this.getOwner().getUUID() == player.getUUID();
		if (this.Pickup == AbstractArrow.Pickup.ALLOWED
				&& !player.getInventory().add(this.getItem())) {
			flag = false;
		}

		// If the ball can be picked up, tell the player to pick it up.
		if (flag) {
			((ServerLevel)player.level).getChunkSource().broadcast(this, new ClientboundTakeItemEntityPacket(this.getId(), player.getId(), 1));

			this.remove(RemovalReason.DISCARDED);
		}
	}
	
	@Override
	public void remove(RemovalReason reason) {
		this.getMockArrow(false);
		if (this.mockArrow != null) this.mockArrow.remove(reason);
		super.remove(reason);
	}
	
	public void destroy(RemovalReason reason) {
		if (!this.level.isClientSide) {
			this.level.broadcastEntityEvent(this, (byte) 3);
			this.remove(reason);
		}
	}
	
	@Override
	public void setItem(ItemStack itemstack) {
		super.setItem(itemstack);
		this.initProperties(itemstack.getItem());
	}

	public ItemStack getHalfItem() {
		ItemStack item = this.getItem();
		return item.getItem() == ModItems.BASIC_BASEBALL.get() ? new ItemStack(ModItems.BASEBALL_HALF.get()) : item;
	}

	@OnlyIn(Dist.CLIENT)
	protected void createBreakParticles() {
		ItemStack itemstack = this.getHalfItem();
		ParticleOptions iparticledata = (ParticleOptions) (new ItemParticleOption(ParticleTypes.ITEM, itemstack));
		
		for (int i = 0; i < 8; ++i) {
			/** Adapted from {@link net.minecraft.client.particle.Particle#Particle} */
			float xd = ((float) Math.random() * 2.0F - 1.0F) *  0.4F;
			float yd = ((float) Math.random() * 2.0F - 1.0F) *  0.4F;
			float zd = ((float) Math.random() * 2.0F - 1.0F) *  0.4F;
			float f = (float) (Math.random() + Math.random() + 1.0D) * 0.15F;
			float f1 = Mth.sqrt(xd * xd + yd * yd + zd * zd);
			xd = (xd / f1) * f * 0.4F;
			yd = (yd / f1) * f * 0.4F;// + 0.1F;
			zd = (zd / f1) * f * 0.4F;
			this.level.addParticle(iparticledata, this.getX(), this.getY(), this.getZ(), xd, yd, zd);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleEntityEvent(byte id) {
		switch (id) {
		case 3:
			this.createBreakParticles();
			break;
		default: 
			break;
		}
	}
	
	public boolean isIdle() {
		return this.tickCount > this.droptimer;
	}
		
	public void getMockArrow() {
		getMockArrow(true);
	}

	public void getMockArrow(Boolean createNew) {
		if ((this.mockArrow == null || !this.mockArrow.isAddedToWorld()) && this.level instanceof ServerLevel) {
			if (this.arrowId != null && this.mockArrow.isAddedToWorld()) {
				Entity entity = ((ServerLevel) this.level).getEntity(this.arrowId);
				if (entity instanceof MockArrow) {
					this.mockArrow = (MockArrow) entity;
				}
			} else if (createNew) {
				this.mockArrow = new MockArrow(this.getOwner(), this, this.getX(), this.getY(), this.getZ(), this.level);
				this.arrowId = this.mockArrow.getUUID();
				this.level.addFreshEntity(this.mockArrow);
			}
		}
		if (this.mockArrow != null) {
			this.mockArrow.setLastAccessTime();
		}
		this.setBoundingBox(this.getBoundingBox());
	}
	
	public Vec3 getCenterPositionVec() {
		return this.position().add(0,this.getBbHeight()/2,0);
	}
	
	@Override
	public void makeStuckInBlock(BlockState blockState, Vec3 stuckSpeed) {
		if (blockState.getBlock() instanceof PowderSnowBlock)
			this.stuckSpeedOverride = new Vec3(0.7D, 0.7D, 0.7D);
		else
			this.stuckSpeedOverride = stuckSpeed;
	}
	
	@Override
	protected float getBlockSpeedFactor() {
		return 1F;
	}
	
	@Override
	public boolean canBeCollidedWith() {
		return false;
	} 

	@Override
	public boolean isAttackable() {
		return true;
	}
	
	@Override
	public boolean isPickable() {
		return true;
	}
	
	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public float getPickRadius() {
		return 0.1F; // Slightly increase the bounding box for player hits
	}
	
	// Adapted from AbstractArrow ------------------------------------------------------------
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(BOOLS, (byte) 0);
	}
	
	private void setFlag(int index, boolean bool) {
		byte b0 = this.entityData.get(BOOLS);
		if (bool) {
			this.entityData.set(BOOLS, (byte) (b0 | index));
		} else {
			this.entityData.set(BOOLS, (byte) (b0 & ~index));
		}
	}

	public void setNoPhysics(boolean noClipIn) {
		this.noPhysics = noClipIn;
		this.setFlag(2, noClipIn);
	}

	public boolean getNoPhysics() {
		if (!this.level.isClientSide) {
			return this.noPhysics;
		} else {
			return (this.entityData.get(BOOLS) & 2) != 0;
		}
	}
		
	//----------------------------------------------------------------------------------------
}
