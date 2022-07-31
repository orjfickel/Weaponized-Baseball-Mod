package blizzardfenix.webasemod.init;

import java.util.function.Supplier;

import blizzardfenix.webasemod.BaseballMod;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {
	public static final CreativeModeTab MOD_ITEM_GROUP = new ModItemGroup(BaseballMod.MODID, () -> new ItemStack(ModItems.BASIC_BASEBALL.get()));


	public static class ModItemGroup extends CreativeModeTab {
		private final Supplier<ItemStack> iconSupplier;

		public ModItemGroup(final String name, final Supplier<ItemStack> iconSupplier) {
			super(name);
			this.iconSupplier = iconSupplier;
		}

		@Override
		public ItemStack makeIcon() {
			return iconSupplier.get();
		}
	}
}
