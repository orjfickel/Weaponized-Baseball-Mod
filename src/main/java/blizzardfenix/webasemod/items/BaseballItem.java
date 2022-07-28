package blizzardfenix.webasemod.items;

import blizzardfenix.webasemod.entity.BouncyBallEntity;
import blizzardfenix.webasemod.init.ModEntityTypes;
import blizzardfenix.webasemod.init.ThrowableProperties;
import blizzardfenix.webasemod.server.WebaseMessage;
import blizzardfenix.webasemod.server.WebasePacketHandler;
import blizzardfenix.webasemod.util.HelperFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BaseballItem extends Item {
	ThrowableProperties properties;

	public BaseballItem(Item.Properties builder, ThrowableProperties properties) {
		super(builder);
		this.properties = properties;
	}
	
	// For when we add enchantment support for baseballs
//	@Override
//	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
//		return enchantment == Enchantments.FIRE_PROTECTION || enchantment == Enchantments.FIRE_ASPECT || 
//				enchantment == Enchantments.KNOCKBACK || enchantment == Enchantments.SHARPNESS || 
//				enchantment == Enchantments.MOB_LOOTING;
//	}
	/*
	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		Map<Enchantment, Integer> enchmap = EnchantmentHelper.getEnchantments(book);
		
		return enchmap.containsKey(Enchantments.FIRE_PROTECTION) || enchmap.containsKey(Enchantments.FIRE_ASPECT) || 
				enchmap.containsKey(Enchantments.KNOCKBACK) || enchmap.containsKey(Enchantments.SHARPNESS) ||
				enchmap.containsKey(Enchantments.LOOTING);
	}*/
		
	@Override
	public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		if(level.isClientSide()) {
			BouncyBallEntity throwableentity = new BouncyBallEntity(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), level, player);
			ActionResultType result = HelperFunctions.throwBall(level, player, itemstack, throwableentity, player.getDeltaMovement()).getResult();						
			if (result.consumesAction()) {			
				// If the throw was successful, tell the server to perform the throw as well
				WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(hand, player.getDeltaMovement()));
			}
		}
		
		return ActionResult.sidedSuccess(itemstack, level.isClientSide());
	}
}
