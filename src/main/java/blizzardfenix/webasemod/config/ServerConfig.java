package blizzardfenix.webasemod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Boolean> lite_mode;
	public static final ForgeConfigSpec.ConfigValue<Integer> throwable_idle_time;
	public static final ForgeConfigSpec.ConfigValue<Boolean> override_vanilla_throwables;
	public static final ForgeConfigSpec.ConfigValue<Boolean> drop_balls;
	public static final ForgeConfigSpec.ConfigValue<Boolean> nerf_super_slimeball;
	public static final ForgeConfigSpec.ConfigValue<Integer> throwCooldown;
	
	static {
		BUILDER.push("Server config for the Weaponized Baseball Mod");

		lite_mode = BUILDER.comment("Turn on lite mode which disables slow collisions and throwable on throwable collisions.").define("Lite mode", false);

		throwable_idle_time = BUILDER.comment("How many ticks a throwable can be idle before despawning. Default value is 1200 (1 min).").define("Throwable Idle Time", 1200);

		override_vanilla_throwables = BUILDER.comment("Disable overriding the vanilla throwables behaviour to make them hittable with bats.").define("Override Vanilla Throwables", true);

		drop_balls = BUILDER.comment("Enable throwables dropping themselves after the idle time is over.").define("Throwables drop selves", false);

		nerf_super_slimeball = BUILDER.comment("Let the super slimeball's bounciness be bound by the laws of physics.").define("Reduce super slimeball bounciness", false);

		throwCooldown = BUILDER.comment("How many ticks in between consecutive throws. Default value is 12").define("Throwing cooldown", 12);
		
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
