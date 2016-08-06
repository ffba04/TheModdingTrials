package ffba04.blockhologram;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ffba04.blockhologram.placement.BlockPlacement;
import ffba04.blockhologram.placement.DefaultBlockPlacement;
import ffba04.blockhologram.placement.LilyPadPlacement;
import ffba04.blockhologram.placement.MetadataBlockPlacement;
import ffba04.blockhologram.placement.RedstoneBlockPlacement;
import ffba04.blockhologram.placement.SlabBlockPlacement;
import ffba04.blockhologram.placement.SnowBlockPlacement;
import ffba04.blockhologram.placement.SpecialBlockPlacement;
import ffba04.blockhologram.renderer.BlockRenderer;
import ffba04.blockhologram.renderer.DefaultBlockRenderer;
import ffba04.blockhologram.renderer.FlatBlockRenderer;
import ffba04.blockhologram.renderer.ContainerBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAnvilBlock;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemCloth;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemLeaves;
import net.minecraft.item.ItemLilyPad;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemPiston;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemSnow;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = BlockHologram.ID, name = BlockHologram.NAME, version = BlockHologram.VERSION, clientSideOnly = true)
public class BlockHologram {

	public static final String ID = "blockhologram";
	public static final String NAME = "Block Hologram";
	public static final String VERSION = "v1.0";
	public static final Logger LOGGER = LogManager.getLogger(NAME);

	public static boolean inDevelopment = false;

	private List<Block> exclusions = new ArrayList<>();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		inDevelopment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

		MinecraftForge.EVENT_BUS.register(this);

		BlockPlacement.TYPES.put(ItemBlock.class, new DefaultBlockPlacement());
		BlockPlacement.TYPES.put(ItemMultiTexture.class, new MetadataBlockPlacement());
		BlockPlacement.TYPES.put(ItemColored.class, new MetadataBlockPlacement());
		BlockPlacement.TYPES.put(ItemBlockSpecial.class, new SpecialBlockPlacement());
		BlockPlacement.TYPES.put(ItemRedstone.class, new RedstoneBlockPlacement());
		BlockPlacement.TYPES.put(ItemPiston.class, new DefaultBlockPlacement());
		BlockPlacement.TYPES.put(ItemAnvilBlock.class, new DefaultBlockPlacement());
		BlockPlacement.TYPES.put(ItemBanner.class, new DefaultBlockPlacement());
		BlockPlacement.TYPES.put(ItemCloth.class, new DefaultBlockPlacement());
		BlockPlacement.TYPES.put(ItemLeaves.class, new DefaultBlockPlacement());
		BlockPlacement.TYPES.put(ItemLilyPad.class, new LilyPadPlacement());
		BlockPlacement.TYPES.put(ItemSnow.class, new SnowBlockPlacement());
		BlockPlacement.TYPES.put(ItemSlab.class, new SlabBlockPlacement());

		BlockRenderer.TYPES.put(Block.class, new DefaultBlockRenderer());
		BlockRenderer.TYPES.put(BlockContainer.class, new ContainerBlockRenderer());
		BlockRenderer.TYPES.put(BlockLilyPad.class, new FlatBlockRenderer());
		BlockRenderer.TYPES.put(BlockRedstoneWire.class, new FlatBlockRenderer());

		exclusions.add(Blocks.PORTAL);
		exclusions.add(Blocks.END_PORTAL);
		exclusions.add(Blocks.END_GATEWAY);
	}

	@SubscribeEvent
	public void renderWorldLast(RenderWorldLastEvent e) {
		Minecraft mc = Minecraft.getMinecraft();

		if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}

		RayTraceResult rayTrace = mc.objectMouseOver;
		World world = mc.theWorld;
		EntityPlayer player = mc.thePlayer;
		ItemStack mainHand = player.getHeldItemMainhand();
		ItemStack offHand = player.getHeldItemOffhand();

		EnumHand hand = null;

		if (mainHand != null && offHand != null) {
			if (BlockPlacement.TYPES.containsKey(mainHand.getItem().getClass())) {
				hand = EnumHand.MAIN_HAND;
			} else if (BlockPlacement.TYPES.containsKey(offHand.getItem().getClass())) {
				hand = EnumHand.OFF_HAND;
			}
		} else if (mainHand != null && BlockPlacement.TYPES.containsKey(mainHand.getItem().getClass())) {
			hand = EnumHand.MAIN_HAND;
		} else if (offHand != null && BlockPlacement.TYPES.containsKey(offHand.getItem().getClass())) {
			hand = EnumHand.OFF_HAND;
		}

		if (hand != null) {
			ItemStack stack = hand == EnumHand.MAIN_HAND ? mainHand : offHand;
			BlockPlacement placement = BlockPlacement.TYPES.get(stack.getItem().getClass());

			renderHologram(world, rayTrace, player, stack, placement, hand, e.getPartialTicks());
		}
	}

	private void renderHologram(World world, RayTraceResult rayTrace, EntityPlayer player, ItemStack stack,
			BlockPlacement placement, EnumHand hand, float partialTicks) {
		BlockPos pos = rayTrace.getBlockPos();
		EnumFacing facing = rayTrace.sideHit;
		Vec3d hitVec = rayTrace.hitVec;
		float hitX = (float) (hitVec.xCoord - pos.getX());
		float hitY = (float) (hitVec.yCoord - pos.getY());
		float hitZ = (float) (hitVec.zCoord - pos.getZ());
		Block block = placement.getBlock(stack);

		if (exclusions.contains(block)) {
			return;
		}

		IBlockState current = world.getBlockState(pos);
		pos = placement.getPosition(world, pos, current, stack, facing, player, hitX, hitY, hitZ);

		if (pos == null) {
			return;
		}

		current = world.getBlockState(pos);
		IBlockState state = block.getDefaultState();
		int metadata = placement.getMetadata(stack);

		if (block instanceof ITileEntityProvider) {
			ITileEntityProvider provider = (ITileEntityProvider) state.getBlock();
			TileEntity entity = provider.createNewTileEntity(world, metadata);
			world.setTileEntity(pos, entity);
		}

		state = placement.placeBlockInWorld(world, pos, player, state, current, stack, facing, metadata, hitX, hitY,
				hitZ);

		BlockRenderer renderer = BlockRenderer.TYPES.get(block.getClass());

		if (renderer == null) {
			if (block instanceof BlockContainer) {
				renderer = BlockRenderer.TYPES.get(BlockContainer.class);
			} else {
				renderer = BlockRenderer.TYPES.get(Block.class);
			}
		}

		Minecraft mc = Minecraft.getMinecraft();
		Entity view = mc.getRenderViewEntity();
		double dX = view.lastTickPosX + (view.posX - view.lastTickPosX) * (double) partialTicks;
		double dY = view.lastTickPosY + (view.posY - view.lastTickPosY) * (double) partialTicks;
		double dZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * (double) partialTicks;

		GlStateManager.pushMatrix();
		GlStateManager.translate(-dX, -dY, -dZ);
		GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());

		renderer.renderBlock(world, pos, state, stack, player, partialTicks);

		GlStateManager.popMatrix();

		world.setBlockState(pos, current, 0);

		if (block instanceof ITileEntityProvider) {
			world.removeTileEntity(pos);
		}
	}

	@SubscribeEvent
	public void renderBlockHighlight(DrawBlockHighlightEvent e) {
		Minecraft mc = Minecraft.getMinecraft();
		RayTraceResult rayTrace = e.getTarget();

		if (rayTrace.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}

		e.setCanceled(true);

		BlockPos pos = rayTrace.getBlockPos();
		World world = mc.theWorld;
		IBlockState state = world.getBlockState(pos).getActualState(world, pos);
		Block block = state.getBlock();

		if (exclusions.contains(block)) {
			return;
		}

		BlockRenderer renderer = BlockRenderer.TYPES.get(block.getClass());

		if (renderer == null) {
			if (block instanceof BlockContainer) {
				renderer = BlockRenderer.TYPES.get(BlockContainer.class);
			} else {
				renderer = BlockRenderer.TYPES.get(Block.class);
			}
		}

		Entity view = mc.getRenderViewEntity();
		float partialTicks = e.getPartialTicks();
		double dX = view.lastTickPosX + (view.posX - view.lastTickPosX) * (double) partialTicks;
		double dY = view.lastTickPosY + (view.posY - view.lastTickPosY) * (double) partialTicks;
		double dZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * (double) partialTicks;

		GlStateManager.pushMatrix();
		GlStateManager.translate(-dX, -dY, -dZ);
		GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());

		renderer.renderBlockOverlay(world, pos, state, rayTrace.sideHit, partialTicks);

		GlStateManager.popMatrix();
	}

}
