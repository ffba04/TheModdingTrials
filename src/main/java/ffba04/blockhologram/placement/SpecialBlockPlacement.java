package ffba04.blockhologram.placement;

import java.lang.reflect.Field;

import ffba04.blockhologram.BlockHologram;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemStack;

public class SpecialBlockPlacement extends DefaultBlockPlacement {
	private Field field = null;

	@Override
	public Block getBlock(ItemStack stack) {
		ItemBlockSpecial item = (ItemBlockSpecial) stack.getItem();

		if (field == null) {
			try {
				field = item.getClass().getDeclaredField(BlockHologram.inDevelopment ? "block" : "field_150935_a");
				field.setAccessible(true);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		try {
			return (Block) field.get(item);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
