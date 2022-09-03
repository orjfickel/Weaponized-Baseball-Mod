package blizzardfenix.webasemod.init;

import blizzardfenix.webasemod.BaseballMod;
import blizzardfenix.webasemod.items.BallItem;
import blizzardfenix.webasemod.items.CatcherGlove;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BaseballMod.MODID);

	public static final RegistryObject<BallItem> BASIC_BASEBALL = ITEMS.register("basic_baseball", () -> new BallItem(new BallItem.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<Item> CORK = ITEMS.register("cork", () -> new Item(new Item.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<Item> BASEBALL_CORE = ITEMS.register("baseball_core", () -> new Item(new Item.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<Item> BASEBALL_HALF = ITEMS.register("baseball_half", () -> new Item(new Item.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));

	public static final RegistryObject<BallItem> DIRTBALL = ITEMS.register("dirtball", () -> new BallItem(new BallItem.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BallItem> STONEBALL = ITEMS.register("stoneball", () -> new BallItem(new BallItem.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BallItem> GOLFBALL = ITEMS.register("golfball", () -> new BallItem(new BallItem.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BallItem> SUPER_SLIMEBALL = ITEMS.register("super_slimeball", () -> new BallItem(new BallItem.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	
	public static final RegistryObject<CatcherGlove> CATCHER_GLOVE = ITEMS.register("catcher_glove", () -> new CatcherGlove(new CatcherGlove.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));

	public static final RegistryObject<BaseballBat> WOODEN_BASEBALL_BAT = ITEMS.register("wooden_bat", () -> new BaseballBat(ItemTier.WOOD, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BaseballBat> STONE_BASEBALL_BAT = ITEMS.register("stone_bat", () -> new BaseballBat(ItemTier.STONE, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BaseballBat> GOLDEN_BASEBALL_BAT = ITEMS.register("golden_bat", () -> new BaseballBat(ItemTier.GOLD, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BaseballBat> IRON_BASEBALL_BAT = ITEMS.register("iron_bat", () -> new BaseballBat(ItemTier.IRON, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BaseballBat> DIAMOND_BASEBALL_BAT = ITEMS.register("diamond_bat", () -> new BaseballBat(ItemTier.DIAMOND, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
	public static final RegistryObject<BaseballBat> NETHERITE_BASEBALL_BAT = ITEMS.register("netherite_bat", () -> new BaseballBat(ItemTier.NETHERITE, new BaseballBat.Properties().tab(ModItemGroups.MOD_ITEM_GROUP)));
}
