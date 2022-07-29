package blizzardfenix.webasemod.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.ConfigValue<Boolean> lite_mode;
	public static final ForgeConfigSpec.ConfigValue<Integer> throwable_idle_time;
	public static final ForgeConfigSpec.ConfigValue<Boolean> override_vanilla_throwables;
	
	static {
		BUILDER.push("Server config for the Advanced & Bouncy Throwables Mod");

		lite_mode = BUILDER.comment("Turn on lite mode which disables slow collisions and throwable on throwable collisions.").define("Lite mode", false);

		throwable_idle_time = BUILDER.comment("How many ticks a throwable can be idle before despawning. Default value is 1200 (2 mins).").define("Throwable Idle Time", 1200);
		
		override_vanilla_throwables = BUILDER.comment("Disable overriding the vanilla throwables behaviour to make them hittable with bats.").define("Override Vanilla Throwables", true);
		
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
