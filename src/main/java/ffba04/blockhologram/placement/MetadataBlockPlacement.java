package ffba04.blockhologram.placement;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class MetadataBlockPlacement extends DefaultBlockPlacement {

	@Override
	public int getMetadata(ItemStack stack) {
		ItemBlock item = (ItemBlock) stack.getItem();
		return item.getMetadata(stack.getMetadata());
	}

}
