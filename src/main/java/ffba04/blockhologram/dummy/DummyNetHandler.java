package ffba04.blockhologram.dummy;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;

public class DummyNetHandler extends NetHandlerPlayClient {

	public DummyNetHandler() {
		super(Minecraft.getMinecraft(), null, null, new GameProfile(UUID.randomUUID(), "dummy"));
	}

	@Override
	public void sendPacket(Packet<?> packet) {
	}

}
