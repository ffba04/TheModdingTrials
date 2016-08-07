package ffba04.blockhologram.dummy;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;

public class DummyPlayer extends EntityPlayerSP {

	public DummyPlayer(DummyWorld world) {
		super(Minecraft.getMinecraft(), world, new DummyNetHandler(), null);
	}

	@Override
	public void addChatComponentMessage(ITextComponent chatComponent) {
	}

	@Override
	public void addChatMessage(ITextComponent component) {
	}

	@Override
	public void displayGui(IInteractionObject guiOwner) {
	}

	@Override
	public boolean isSneaking() {
		return this.mc.thePlayer.isSneaking();
	}
	
	public static class DummyNetHandler extends NetHandlerPlayClient {

		public DummyNetHandler() {
			super(Minecraft.getMinecraft(), null, null, new GameProfile(UUID.randomUUID(), "dummy"));
		}

		@Override
		public void sendPacket(Packet<?> packet) {
		}

	}

}
