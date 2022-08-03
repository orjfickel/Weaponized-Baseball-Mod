package blizzardfenix.webasemod.server;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import blizzardfenix.webasemod.util.HelperFunctions;
import blizzardfenix.webasemod.util.Settings;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class WebaseMessage {
	private Hand hand;
	private Vector3d shooterVelocity;
	private Boolean throwUp;

	public WebaseMessage() {
	}

	public WebaseMessage(Hand hand, @Nullable Vector3d shooterVelocity, Boolean throwUp) {
		this.hand = hand;
		this.shooterVelocity = shooterVelocity;
		this.throwUp = throwUp;
	}

	public static void encode(WebaseMessage message, PacketBuffer buffer) {
		buffer.writeEnum(message.hand);
		buffer.writeFloat((float) message.shooterVelocity.x);
		buffer.writeFloat((float) message.shooterVelocity.y);
		buffer.writeFloat((float) message.shooterVelocity.z);
		buffer.writeBoolean(Settings.throwUp);
	}

	public static WebaseMessage decode(PacketBuffer buffer) {
		return new WebaseMessage(buffer.readEnum(Hand.class), new Vector3d(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()), buffer.readBoolean());
	}

	public static void handle(WebaseMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> {
			ServerPlayerEntity player = contextSupplier.get().getSender();
			HelperFunctions.tryThrow(player.getCommandSenderWorld(), player, message.hand, message.shooterVelocity, message.throwUp);
		});
		contextSupplier.get().setPacketHandled(true);
	}
}
