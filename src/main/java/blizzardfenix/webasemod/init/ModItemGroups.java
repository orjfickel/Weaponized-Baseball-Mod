package blizzardfenix.webasemod.init;

import java.util.function.Supplier;

import blizzardfenix.webasemod.BaseballMod;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroups {
	public static final ItemGroup MOD_ITEM_GROUP = new ModItemGroup(BaseballMod.MODID, () -> new ItemStack(ModItems.BASIC_BASEBALL.get()));


	public static class ModItemGroup extends ItemGroup {
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
