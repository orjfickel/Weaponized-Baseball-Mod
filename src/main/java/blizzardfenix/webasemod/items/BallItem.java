package blizzardfenix.webasemod.items;

import net.minecraft.item.Item;

public class BallItem extends Item {

	public BallItem(Item.Properties builder) {
		super(builder);
	}
		
//	@Override
//	public ActionResult<ItemStack> use(World level, Player player, InteractionHand hand) {
//		ItemStack itemstack = player.getItemInHand(hand);
//
//		if(level.isClientSide()) {
//			BouncyBallEntity throwableentity = new BouncyBallEntity(ModEntityTypes.THROWABLE_ITEM_ENTITY.get(), level, player);
//			ActionResultType result = ItemHelperFunctions.throwBall(level, player, itemstack, throwableentity, false).getResult();
//			if (result.consumesAction()) {
//				// If the throw was successful, tell the server to perform the throw as well
//				WebasePacketHandler.INSTANCE.sendToServer(new WebaseMessage(hand, player.getDeltaMovement(), false, false));
//			}
//		}
//		
//		return ActionResult.sidedSuccess(itemstack, level.isClientSide());
//	}
}
