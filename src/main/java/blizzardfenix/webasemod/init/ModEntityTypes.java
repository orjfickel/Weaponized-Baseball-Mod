package blizzardfenix.webasemod.init;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.entity.BouncyFireBallEntity;
import blizzardfenix.webasemod.entity.MockArrow;
import blizzardfenix.webasemod.entity.PickableEggEntity;
import blizzardfenix.webasemod.entity.PickableEnderPearlEntity;
import blizzardfenix.webasemod.entity.PickableExperienceBottleEntity;
import blizzardfenix.webasemod.entity.PickablePotionEntity;
import blizzardfenix.webasemod.entity.PickableSnowballEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, BaseballMod.MODID);
	private static final EntityType.Builder<BouncyBallEntity> THROWABLE_ITEM_ENTITY_BUILDER = EntityType.Builder.<BouncyBallEntity>
		of(BouncyBallEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).updateInterval(1).clientTrackingRange(6).setTrackingRange(6);

	public static final String THROWABLE_ITEM_NAME = "throwable_item";
	public static final RegistryObject<EntityType<BouncyBallEntity>> THROWABLE_ITEM_ENTITY =
			ENTITY_TYPES.register(THROWABLE_ITEM_NAME, () -> THROWABLE_ITEM_ENTITY_BUILDER
				.sized(0.34F, 0.34F) // 0.35 means that the bounding box exactly encloses the baseball sprite.
				.build(new ResourceLocation(BaseballMod.MODID, THROWABLE_ITEM_NAME).toString()));
	
	public static final String MEDIUM_THROWABLE_ITEM_NAME = "medium_throwable_item";
	public static final RegistryObject<EntityType<BouncyBallEntity>> MEDIUM_THROWABLE_ITEM_ENTITY =
			ENTITY_TYPES.register(MEDIUM_THROWABLE_ITEM_NAME, () -> THROWABLE_ITEM_ENTITY_BUILDER
				.sized(0.3F, 0.3F)
				.build(new ResourceLocation(BaseballMod.MODID, MEDIUM_THROWABLE_ITEM_NAME).toString()));
	
	public static final String SMALL_THROWABLE_ITEM_NAME = "small_throwable_item";
	public static final RegistryObject<EntityType<BouncyBallEntity>> SMALL_THROWABLE_ITEM_ENTITY =
			ENTITY_TYPES.register(SMALL_THROWABLE_ITEM_NAME, () -> THROWABLE_ITEM_ENTITY_BUILDER
				.sized(0.2F, 0.2F)
				.build(new ResourceLocation(BaseballMod.MODID, SMALL_THROWABLE_ITEM_NAME).toString()));

	public static final String BOUNCY_FIREBALL_NAME = "bouncy_fireball";
	public static final RegistryObject<EntityType<BouncyFireBallEntity>> BOUNCY_FIREBALL_ENTITY =
			ENTITY_TYPES.register(BOUNCY_FIREBALL_NAME, () -> EntityType.Builder.<BouncyFireBallEntity>
				of(BouncyFireBallEntity::new, MobCategory.MISC)
				.setShouldReceiveVelocityUpdates(true).updateInterval(1).clientTrackingRange(6)
				.sized(0.3125F, 0.3125F)
				.build(new ResourceLocation(BaseballMod.MODID, BOUNCY_FIREBALL_NAME).toString()));

	public static final ThrowableProperties BASEBALL_PROPERTIES = new ThrowableProperties.Builder().build();
	public static final ThrowableProperties BRICK_PROPERTIES = new ThrowableProperties.Builder()
			.health(20).mass(2).bounciness(0.25F).friction(0.4F).throwSpeed(0.8F).baseInaccuracy(4.0F).destroyChance(0.5F)
			.batHitDmg(2F).batHitSpeed(0.5F).baseDmg(0.5F).build();
	public static final ThrowableProperties COAL_PROPERTIES = new ThrowableProperties.Builder()
			.health(40).mass(2).bounciness(0.35F).friction(0.5F).throwSpeed(0.9F).baseInaccuracy(1.5F).destroyChance(0.5F)
			.batHitDmg(1.5F).batHitSpeed(0.8F).baseDmg(0.1F).build();
	public static final ThrowableProperties INGOT_PROPERTIES = new ThrowableProperties.Builder()
			.health(40).mass(2).bounciness(0.25F).friction(0.4F).throwSpeed(0.8F).baseInaccuracy(4.0F).destroyChance(0.001F)
			.batHitDmg(2.5F).batHitSpeed(0.5F).baseDmg(0.5F).build();
	public static final ThrowableProperties EMERALD_PROPERTIES = new ThrowableProperties.Builder()
			.health(60).mass(2).bounciness(0.4F).friction(0.7F).throwSpeed(0.9F).baseInaccuracy(1.5F).destroyChance(0.005F)
			.batHitDmg(2.5F).batHitSpeed(0.7F).baseDmg(0.5F).build();
	public static final ThrowableProperties DIAMOND_PROPERTIES = new ThrowableProperties.Builder()
			.health(100).mass(3).bounciness(0.4F).friction(0.7F).throwSpeed(0.9F).baseInaccuracy(1.5F).destroyChance(0.001F)
			.batHitDmg(3.5F).batHitSpeed(0.7F).baseDmg(0.6F).build();
	public static final ThrowableProperties NUGGET_PROPERTIES = new ThrowableProperties.Builder()
			.mass(0.75F).bounciness(0.6F).friction(0.7F).throwSpeed(0.8F).baseInaccuracy(1.5F).destroyChance(0.01F)
			.batHitDmg(1.0F).batHitSpeed(1.0F).baseDmg(0).build();
	public static final ThrowableProperties EGG_PROPERTIES = new ThrowableProperties.Builder()
			.throwSpeed(1.5F).baseInaccuracy(0.8F).destroyChance(1F).baseDmg(0F).batHitDmg(0F).build();
	public static final ThrowableProperties SLIMEBALL_PROPERTIES = new ThrowableProperties.Builder()
			.bounciness(0.999F).friction(0.9F).throwSpeed(0.9F).baseInaccuracy(0.9F)
			.batHitDmg(1.5F).batHitSpeed(1.4F).baseDmg(0.2F).airResistance(0.995F).build();
	public static final ThrowableProperties FIREBALL_PROPERTIES = new ThrowableProperties.Builder()
			.bounciness(0.7F).friction(0.8F).throwSpeed(0.9F).baseInaccuracy(0.9F)
			.baseDmg(0.2F).build();

	public static final ThrowableProperties DIRTBALL_PROPERTIES = new ThrowableProperties.Builder()
			.bounciness(0.4F).friction(0.5F).throwSpeed(1.0F).baseInaccuracy(1.0F)
			.batHitDmg(1.2F).batHitSpeed(0.8F).baseDmg(0.1F).build();
	public static final ThrowableProperties STONEBALL_PROPERTIES = new ThrowableProperties.Builder()
			.bounciness(0.5F).friction(0.6F).throwSpeed(1.0F).baseInaccuracy(0.9F)
			.batHitDmg(1.3F).batHitSpeed(0.9F).baseDmg(0.2F).build();
	public static final ThrowableProperties CORK_PROPERTIES = new ThrowableProperties.Builder()
			.mass(0.5F).bounciness(0.6F).friction(0.6F).throwSpeed(1.0F).baseInaccuracy(1.0F)
			.batHitDmg(1.0F).batHitSpeed(1.2F).baseDmg(0F).build();
	public static final ThrowableProperties CORE_PROPERTIES = new ThrowableProperties.Builder()
			.mass(0.75F).bounciness(0.5F).friction(0.6F).throwSpeed(1.0F).baseInaccuracy(0.9F)
			.batHitDmg(1.2F).batHitSpeed(1.0F).baseDmg(0.1F).build();
	public static final ThrowableProperties GOLFBALL_PROPERTIES = new ThrowableProperties.Builder()
			.bounciness(0.6F).friction(0.95F).throwSpeed(1.0F).baseInaccuracy(0.8F)
			.batHitDmg(2.1F).batHitSpeed(1.4F).baseDmg(0.6F).build();
	public static final ThrowableProperties SUPER_SLIMEBALL_PROPERTIES = new ThrowableProperties.Builder()
			.bounciness(1.2F).friction(1.2F).throwSpeed(1.0F).baseInaccuracy(0.9F)
			.batHitDmg(1.5F).batHitSpeed(1.4F).baseDmg(0.2F).build();
	
	public static final String MOCKARROW_NAME = "mock_arrow";
	public static final RegistryObject<EntityType<MockArrow>> MOCKARROW_ENTITY = ENTITY_TYPES.register(MOCKARROW_NAME, () -> 
		EntityType.Builder.<MockArrow>of(MockArrow::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.build(new ResourceLocation(BaseballMod.MODID, MOCKARROW_NAME).toString()));
	
	public static final String PICKABLE_SNOWBALL_NAME = "snowball_pickable";
	public static final RegistryObject<EntityType<PickableSnowballEntity>> PICKABLE_SNOWBALL_ENTITY =
			ENTITY_TYPES.register(PICKABLE_SNOWBALL_NAME, () -> EntityType.Builder.<PickableSnowballEntity>
				of(PickableSnowballEntity::new, MobCategory.MISC)
				.sized(0.34F, 0.34F).clientTrackingRange(6).updateInterval(10)
				.build(new ResourceLocation(BaseballMod.MODID, PICKABLE_SNOWBALL_NAME).toString()));
	public static final String PICKABLE_EGG_NAME = "egg_pickable";
	public static final RegistryObject<EntityType<PickableEggEntity>> PICKABLE_EGG_ENTITY =
			ENTITY_TYPES.register(PICKABLE_EGG_NAME, () -> EntityType.Builder.<PickableEggEntity>
				of(PickableEggEntity::new, MobCategory.MISC)
				.sized(0.34F, 0.34F).clientTrackingRange(6).updateInterval(10)
				.build(new ResourceLocation(BaseballMod.MODID, PICKABLE_EGG_NAME).toString()));
	public static final String PICKABLE_ENDER_PEARL_NAME = "ender_pearl_pickable";
	public static final RegistryObject<EntityType<PickableEnderPearlEntity>> PICKABLE_ENDER_PEARL_ENTITY =
			ENTITY_TYPES.register(PICKABLE_ENDER_PEARL_NAME, () -> EntityType.Builder.<PickableEnderPearlEntity>
				of(PickableEnderPearlEntity::new, MobCategory.MISC)
				.sized(0.34F, 0.34F).clientTrackingRange(6).updateInterval(10)
				.build(new ResourceLocation(BaseballMod.MODID, PICKABLE_ENDER_PEARL_NAME).toString()));
	public static final String PICKABLE_POTION_NAME = "potion_pickable";
	public static final RegistryObject<EntityType<PickablePotionEntity>> PICKABLE_POTION_ENTITY =
			ENTITY_TYPES.register(PICKABLE_POTION_NAME, () -> EntityType.Builder.<PickablePotionEntity>
				of(PickablePotionEntity::new, MobCategory.MISC)
				.sized(0.25F, 0.25F).clientTrackingRange(6).updateInterval(10)
				.build(new ResourceLocation(BaseballMod.MODID, PICKABLE_POTION_NAME).toString()));
	public static final String PICKABLE_EXPERIENCE_BOTTLE_NAME = "experience_bottle_pickable";
	public static final RegistryObject<EntityType<PickableExperienceBottleEntity>> PICKABLE_EXPERIENCE_BOTTLE_ENTITY =
			ENTITY_TYPES.register(PICKABLE_EXPERIENCE_BOTTLE_NAME, () -> EntityType.Builder.<PickableExperienceBottleEntity>
				of(PickableExperienceBottleEntity::new, MobCategory.MISC)
				.sized(0.34F, 0.34F).clientTrackingRange(4).updateInterval(10)
				.build(new ResourceLocation(BaseballMod.MODID, PICKABLE_EXPERIENCE_BOTTLE_NAME).toString()));
}

