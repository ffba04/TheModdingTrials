package ffba04.blockhologram.placement;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class RedstoneBlockPlacement extends DefaultBlockPlacement {

	@Override
	public Block getBlock(ItemStack stack) {
		return Blocks.REDSTONE_WIRE;
	}

}
