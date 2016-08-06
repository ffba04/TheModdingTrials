package ffba04.blockhologram.dummy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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

}
