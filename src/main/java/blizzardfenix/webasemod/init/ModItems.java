package blizzardfenix.webasemod.init;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.items.BaseballItem;
import blizzardfenix.webasemod.items.CatcherGlove;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BaseballMod.MODID);

	public static final RegistryObject<BaseballItem> BASIC_BASEBALL = ITEMS.register("basic_baseball", () -> new BaseballItem(new BaseballItem.Properties().tab(ModItemGroups.MOD_ITEM_GROUP), ModEntityTypes.BASEBALL_PROPERTIES));
	public static final RegistryObject<Item> CORK = ITEMS.register("cork", () -> new Item(new Item.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<Item> BASEBALL_CORE = ITEMS.register("baseball_core", () -> new Item(new Item.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<Item> BASEBALL_HALF = ITEMS.register("baseball_half", () -> new Item(new Item.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));

	public static final RegistryObject<BaseballItem> DIRTBALL = ITEMS.register("dirtball", () -> new BaseballItem(new BaseballItem.Properties().tab(ModItemGroups.MOD_ITEM_GROUP),ModEntityTypes.DIRTBALL_PROPERTIES));
	public static final RegistryObject<BaseballItem> STONEBALL = ITEMS.register("stoneball", () -> new BaseballItem(new BaseballItem.Properties().tab(ModItemGroups.MOD_ITEM_GROUP),ModEntityTypes.STONEBALL_PROPERTIES));
	public static final RegistryObject<BaseballItem> CORKBALL = ITEMS.register("corkball", () -> new BaseballItem(new BaseballItem.Properties().tab(ModItemGroups.MOD_ITEM_GROUP),ModEntityTypes.CORKBALL_PROPERTIES));
	
	public static final RegistryObject<CatcherGlove> CATCHER_GLOVE = ITEMS.register("catcher_glove", () -> new CatcherGlove(new CatcherGlove.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));

	public static final RegistryObject<BaseballBat> WOODEN_BASEBALL_BAT = ITEMS.register("wooden_bat", () -> new BaseballBat(Tiers.WOOD, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BaseballBat> STONE_BASEBALL_BAT = ITEMS.register("stone_bat", () -> new BaseballBat(Tiers.STONE, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BaseballBat> GOLDEN_BASEBALL_BAT = ITEMS.register("golden_bat", () -> new BaseballBat(Tiers.GOLD, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BaseballBat> IRON_BASEBALL_BAT = ITEMS.register("iron_bat", () -> new BaseballBat(Tiers.IRON, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BaseballBat> DIAMOND_BASEBALL_BAT = ITEMS.register("diamond_bat", () -> new BaseballBat(Tiers.DIAMOND, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BaseballBat> NETHERITE_BASEBALL_BAT = ITEMS.register("netherite_bat", () -> new BaseballBat(Tiers.NETHERITE, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
}
