package blizzardfenix.webasemod.server;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import blizzardfenix.webasemod.items.ItemHelperFunctions;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class WebaseMessage {
	private InteractionHand hand;
	private Vec3 shooterVelocity;
	private Boolean throwUp;
	private Boolean tryUse;

	public WebaseMessage() {
	}

	public WebaseMessage(InteractionHand hand, @Nullable Vec3 shooterVelocity, Boolean throwUp, Boolean tryUse) {
		this.hand = hand;
		this.shooterVelocity = shooterVelocity;
		this.throwUp = throwUp;
		this.tryUse = tryUse;
	}

	public static void encode(WebaseMessage message, FriendlyByteBuf buffer) {
		buffer.writeEnum(message.hand);
		buffer.writeFloat((float) message.shooterVelocity.x);
		buffer.writeFloat((float) message.shooterVelocity.y);
		buffer.writeFloat((float) message.shooterVelocity.z);
		buffer.writeByte((byte) (((message.throwUp ? 1 : 0) << 1) + (message.tryUse ? 1 : 0)));
	}

	public static WebaseMessage decode(FriendlyByteBuf buffer) {
		InteractionHand hand = buffer.readEnum(InteractionHand.class);
		Vec3 shooterVelocity = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		Byte booleans = buffer.readByte();
		return new WebaseMessage(hand, shooterVelocity, ((booleans & 0b010) >> 1) == 1, (booleans & 0b001) == 1);
	}

	public static void handle(WebaseMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> {
			ServerPlayer player = contextSupplier.get().getSender();
			player.setDeltaMovement(message.shooterVelocity); // Set the player velocity on the server, so that it will affect the throwable's shooting velocity
			if (message.throwUp) {
				ItemStack throwableItem = BaseballBat.getProjectile(player.getItemInHand(message.hand), player);
				InteractionResult result =  ItemHelperFunctions.throwBallUp(player.getCommandSenderWorld(), player, message.hand, throwableItem);
				if (result.consumesAction()) {
					ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
					Item item = itemStack.getItem();
					if (item instanceof BaseballBat) {
						BaseballBat batItem = (BaseballBat) item;
						if (batItem.consecutiveUse ) {
				            //Reduce bat durability when hitting a ball
							itemStack.hurtAndBreak(1, player, (consumedEntity) -> {
					        	consumedEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND); // Broadcasts whenever this bat breaks
					        });
						} else {
							batItem.consecutiveUse = true;
						}
					}
				}
			} else
				ItemHelperFunctions.tryThrow(player.getCommandSenderWorld(), player, message.hand, message.tryUse);
		});
		contextSupplier.get().setPacketHandled(true);
	}
}
