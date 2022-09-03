package blizzardfenix.webasemod.server;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import blizzardfenix.webasemod.items.ItemHelperFunctions;
import blizzardfenix.webasemod.items.tools.BaseballBat;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class WebaseMessage {
	private Hand hand;
	private Vector3d shooterVelocity;
	private Boolean throwUp;
    private Boolean tryUse;

	public WebaseMessage() {
	}

    public WebaseMessage(Hand hand, @Nullable Vector3d shooterVelocity, Boolean throwUp, Boolean tryUse) {
		this.hand = hand;
		this.shooterVelocity = shooterVelocity;
		this.throwUp = throwUp;
        this.tryUse = tryUse;
	}

	public static void encode(WebaseMessage message, PacketBuffer buffer) {
		buffer.writeEnum(message.hand);
		buffer.writeFloat((float) message.shooterVelocity.x);
		buffer.writeFloat((float) message.shooterVelocity.y);
		buffer.writeFloat((float) message.shooterVelocity.z);
        buffer.writeByte((byte) (((message.throwUp ? 1 : 0) << 1) + (message.tryUse ? 1 : 0)));
	}

	public static WebaseMessage decode(PacketBuffer buffer) {
		Hand hand = buffer.readEnum(Hand.class);
		Vector3d shooterVelocity = new Vector3d(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		Byte booleans = buffer.readByte();
		return new WebaseMessage(hand, shooterVelocity, ((booleans & 0b010) >> 1) == 1, (booleans & 0b001) == 1);
	}

	public static void handle(WebaseMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> {
			ServerPlayerEntity player = contextSupplier.get().getSender();
			player.setDeltaMovement(message.shooterVelocity); // Set the player velocity on the server, so that it will affect the throwable's shooting velocity
			if (message.throwUp) {
				ItemStack throwableItem = BaseballBat.getProjectile(player.getItemInHand(message.hand), player);
				ActionResultType result =  ItemHelperFunctions.throwBallUp(player.getCommandSenderWorld(), player, message.hand, throwableItem);
				if (result.consumesAction()) {
					ItemStack itemStack = player.getItemInHand(Hand.MAIN_HAND);
					Item item = itemStack.getItem();
					if (item instanceof BaseballBat) {
						BaseballBat batItem = (BaseballBat) item;
						if (batItem.consecutiveUse ) {
				            //Reduce bat durability when hitting a ball
							itemStack.hurtAndBreak(1, player, (consumedEntity) -> {
					        	consumedEntity.broadcastBreakEvent(EquipmentSlotType.MAINHAND); // Broadcasts whenever this bat breaks
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
