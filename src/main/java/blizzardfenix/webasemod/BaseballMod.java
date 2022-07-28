package blizzardfenix.webasemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blizzardfenix.webasemod.config.ClientConfig;
import blizzardfenix.webasemod.config.ServerConfig;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.init.ModItems;
import blizzardfenix.webasemod.server.WebasePacketHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BaseballMod.MODID)
public class BaseballMod
{
    public static final String MODID = "webasemod";
    public static final Logger LOGGER = LogManager.getLogger();
    	
	public BaseballMod() {
        // Register the setup method for modloading
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		modEventBus.addListener(this::setup);
		modEventBus.addListener(this::doClientStuff);

		ModItems.ITEMS.register(modEventBus);
		ModEntityTypes.ENTITY_TYPES.register(modEventBus);
		
		ModLoadingContext.get().registerConfig(Type.CLIENT, ClientConfig.SPEC, "webasemod-client.toml");
		ModLoadingContext.get().registerConfig(Type.SERVER, ServerConfig.SPEC, "webasemod-server.toml");
    }

    private void setup(final FMLCommonSetupEvent event)
    {
		WebasePacketHandler.init();

//    	HelperFunctions.THROWABLE_ITEMS.add(Items.TURTLE_EGG);
//    	HelperFunctions.THROWABLE_ITEMS.add(Items.SLIME_BALL);
//    	HelperFunctions.THROWABLE_ITEMS.add(Items.FIRE_CHARGE);
//    	HelperFunctions.THROWABLE_ITEMS.add(ModItems.BASIC_BASEBALL.get());
//    	HelperFunctions.THROWABLE_ITEMS.add(ModItems.DIRTBALL.get());
//    	HelperFunctions.THROWABLE_ITEMS.add(ModItems.STONEBALL.get());
//    	HelperFunctions.THROWABLE_ITEMS.add(ModItems.CORKBALL.get());
    	
    }

    private void doClientStuff(final FMLClientSetupEvent event) 
    {
    }
}
