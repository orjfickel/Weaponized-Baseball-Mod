package blizzardfenix.webasemod.init;

public class ThrowableProperties {
	public final int health;
	public final float mass;
	public final float baseDmg;
	public final float batHitDmg;
	public final float bounciness;
	public final float friction;
	public final float baseInaccuracy;
	public final float throwSpeed;
	public final float batHitSpeed;
	public final float minDmgSpeed;
	public final float destroyChance;
	public final float airResistance;
	public final boolean useOnEntityHit;
	public final boolean useOnBlockHit;
	public final boolean useOnIdle;

	public ThrowableProperties(int health, float mass, float baseDmg, float batHitDmg, float bounciness, float friction,
			float baseInaccuracy, float throwSpeed, float batHitSpeed, float minDmgSpeed, float destroyChance, float airResistance,
			boolean useOnEntityHit, boolean useOnBlockHit, boolean useOnIdle) {
		this.health = health;
		this.mass = mass;
		this.baseDmg = baseDmg;
		this.batHitDmg = batHitDmg;
		this.bounciness = bounciness;
		this.friction = friction;
		this.baseInaccuracy = baseInaccuracy;
		this.throwSpeed = throwSpeed;
		this.batHitSpeed = batHitSpeed;
		this.minDmgSpeed = minDmgSpeed;
		this.destroyChance = destroyChance;
		this.airResistance = airResistance;
		this.useOnEntityHit = useOnEntityHit;
		this.useOnBlockHit = useOnBlockHit;
		this.useOnIdle = useOnIdle;
	}
	public static class Builder {
		private int health = 10;
		private float mass = 1;
		private float baseDmg = 0.4F;// One fifth of a heart when normally thrown
		private float batHitDmg = 2.0F;
		private float bounciness = 0.8F;
		private float friction = 0.9F; // 1.0F is minimum friction, just as it is implemented for blocks in vanilla minecraft.
		private float baseInaccuracy = 0.8F;
		private float throwSpeed = 1.0F;
		private float batHitSpeed = 1.2F;
		private float minDmgSpeed = 0.35F;
		private float destroyChance = 0.05F;
		private float airResistance = 0.99F;
		private boolean useOnEntityHit = false;
		private boolean useOnBlockHit = false;
		private boolean useOnIdle = false;

		/** In case of fire damage  */
		public ThrowableProperties.Builder health(int health) {
			this.health = health;
			return this;
		}
		public ThrowableProperties.Builder mass(float mass) {
			this.mass = mass;
			return this;
		}
		public ThrowableProperties.Builder baseDmg(float baseDmg) {
			this.baseDmg = baseDmg;
			return this;
		}
		public ThrowableProperties.Builder batHitDmg(float batHitDmg) {
			this.batHitDmg = batHitDmg;
			return this;
		}
		public ThrowableProperties.Builder bounciness(float bounciness) {
			this.bounciness = bounciness;
			return this;
		}
		public ThrowableProperties.Builder friction(float friction) {
			this.friction = friction;
			return this;
		}
		public ThrowableProperties.Builder baseInaccuracy(float baseInaccuracy) {
			this.baseInaccuracy = baseInaccuracy;
			return this;
		}
		public ThrowableProperties.Builder throwSpeed(float throwSpeed) {
			this.throwSpeed = throwSpeed;
			return this;
		}
		public ThrowableProperties.Builder batHitSpeed(float batHitSpeed) {
			this.batHitSpeed = batHitSpeed;
			return this;
		}
		public ThrowableProperties.Builder minDmgSpeed(float minDmgSpeed) {
			this.minDmgSpeed = minDmgSpeed;
			return this;
		}
		/** Probability that this ball gets destroyed upon hurting an entity */
		public ThrowableProperties.Builder destroyChance(float destroyChance) {
			this.destroyChance = destroyChance;
			return this;
		}
		public ThrowableProperties.Builder airResistance(float airResistance) {
			this.airResistance = airResistance;
			return this;
		}
		public ThrowableProperties.Builder useOnEntityHit(boolean useOnEntityHit) {
			this.useOnEntityHit = useOnEntityHit;
			return this;
		}
		public ThrowableProperties.Builder useOnBlockHit(boolean useOnBlockHit) {
			this.useOnBlockHit = useOnBlockHit;
			return this;
		}
		public ThrowableProperties.Builder useIdle(boolean useOnIdle) {
			this.useOnIdle = useOnIdle;
			return this;
		}
		public ThrowableProperties build() {
			return new ThrowableProperties(health, mass, baseDmg, batHitDmg, bounciness, friction,
					baseInaccuracy, throwSpeed, batHitSpeed, minDmgSpeed, destroyChance, airResistance, useOnEntityHit, useOnBlockHit, useOnIdle);
		}
	}
}
