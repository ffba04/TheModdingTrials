package ffba04.blockhologram;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import ffba04.blockhologram.Hologram.Part;
import ffba04.blockhologram.dummy.DummyWorld;
import ffba04.blockhologram.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = BlockHologram.ID, name = BlockHologram.NAME, version = BlockHologram.VERSION, clientSideOnly = true)
public class BlockHologram {

	public static final String ID = "blockhologram";
	public static final String NAME = "Block Hologram";
	public static final String VERSION = "v1.1";
	public static final Logger LOGGER = LogManager.getLogger(NAME);

	public static final List<Item> HOLOGRAM_EXLUSIONS = new ArrayList<>();
	public static final List<Block> OVERLAY_EXLUSIONS = new ArrayList<>();

	public static final KeyBinding TOGGLE_HOLOGRAM = new KeyBinding("key.toggleHologram", new IKeyConflictContext() {

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return false;
		}}, KeyModifier.CONTROL, Keyboard.KEY_H, "key.categories.misc");
	public static final KeyBinding TOGGLE_OVERLAY = new KeyBinding("key.toggleOverlay", new IKeyConflictContext() {

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return false;
		}}, KeyModifier.CONTROL, Keyboard.KEY_O, "key.categories.misc");
	
	public static boolean enableHologram;
	public static boolean enableOverlay;
	
	private Minecraft mc = Minecraft.getMinecraft();
	private Configuration config;
	private DummyWorld dummyWorld = null;

	private Hologram currentHologram = null;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(this);
		
		config = new Configuration(e.getSuggestedConfigurationFile());
		enableHologram = config.getBoolean("enableHologram", Configuration.CATEGORY_GENERAL, true, "");
		enableOverlay = config.getBoolean("enableOverlay", Configuration.CATEGORY_GENERAL, true, "");
		config.save();
		
		ClientRegistry.registerKeyBinding(TOGGLE_HOLOGRAM);
		ClientRegistry.registerKeyBinding(TOGGLE_OVERLAY);
		
		HOLOGRAM_EXLUSIONS.add(Items.SIGN);

		OVERLAY_EXLUSIONS.add(Blocks.PORTAL);
		OVERLAY_EXLUSIONS.add(Blocks.END_PORTAL);
		OVERLAY_EXLUSIONS.add(Blocks.END_GATEWAY);
	}
	
	@SubscribeEvent
    public void handleKeyInputEvent(InputEvent.KeyInputEvent event) {
		if (TOGGLE_HOLOGRAM.isPressed()) {
			enableHologram = !enableHologram;
			config.getCategory(Configuration.CATEGORY_GENERAL).get("enableHologram").set(enableHologram);
			config.save();
		} else if (TOGGLE_OVERLAY.isPressed()) {
			enableOverlay = !enableOverlay;
			config.getCategory(Configuration.CATEGORY_GENERAL).get("enableOverlay").set(enableOverlay);
			config.save();
		}
    }

	@SubscribeEvent
	public void entityJoin(EntityJoinWorldEvent e) {
		if (e.getEntity() == this.mc.thePlayer) {
			dummyWorld = new DummyWorld(e.getWorld(), 4);
		}
	}

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent e) {
		RayTraceResult rayTrace = this.mc.objectMouseOver;

		if (mc.theWorld == null || mc.thePlayer == null || rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.BLOCK) {
			currentHologram = null;
			return;
		}

		World world = mc.theWorld;
		EntityPlayer player = mc.thePlayer;
		BlockPos pos = rayTrace.getBlockPos();
		Vec3d hitVec = rayTrace.hitVec;
		EnumFacing facing = rayTrace.sideHit;
		float yaw = player.rotationYaw;
		float pitch = player.rotationPitch;

		Hologram hologram = new Hologram(pos, hitVec, facing, yaw, pitch);

		if (currentHologram == null || !currentHologram.equals(hologram)) {
			currentHologram = hologram;

			ItemStack mainHand = player.getHeldItemMainhand();
			ItemStack offHand = player.getHeldItemOffhand();

			dummyWorld.copyWorldToDummy(world, hologram);
			EnumActionResult result = null;
			if ((mainHand == null || (result = dummyWorld.useBlockItem(player, EnumHand.MAIN_HAND, hologram)) == EnumActionResult.PASS)
					&& offHand != null) {
				result = dummyWorld.useBlockItem(player, EnumHand.OFF_HAND, hologram);
			}
			if (result == EnumActionResult.SUCCESS) {
				dummyWorld.updateBlockModels(hologram);
			}
		}
	}

	@SubscribeEvent
	public void renderWorldLast(RenderWorldLastEvent e) {
		if (currentHologram != null) {
			Entity view = mc.getRenderViewEntity();
			float partialTicks = e.getPartialTicks();
			double offsetX = view.lastTickPosX + (view.posX - view.lastTickPosX) * (double) partialTicks;
			double offsetY = view.lastTickPosY + (view.posY - view.lastTickPosY) * (double) partialTicks;
			double offsetZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * (double) partialTicks;

			for (Part part : currentHologram.parts) {
				BlockPos pos = part.pos;

				GlStateManager.pushMatrix();
				GlStateManager.translate(-offsetX, -offsetY, -offsetZ);
				GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());

				part.renderPart(partialTicks);

				GlStateManager.popMatrix();
			}
		}
	}

	@SubscribeEvent
	public void renderBlockHighlight(DrawBlockHighlightEvent e) {
		Minecraft mc = Minecraft.getMinecraft();
		RayTraceResult rayTrace = e.getTarget();

		if (!enableOverlay || rayTrace.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}

		e.setCanceled(true);

		BlockPos pos = rayTrace.getBlockPos();
		World world = mc.theWorld;
		IBlockState state = world.getBlockState(pos).getActualState(world, pos);
		Block block = state.getBlock();
		TileEntity entity = world.getTileEntity(pos);

		if (OVERLAY_EXLUSIONS.contains(block)) {
			return;
		}

		if (block == Blocks.REDSTONE_WIRE || block == Blocks.WATERLILY) {
			state = Blocks.REDSTONE_WIRE.getDefaultState();
		}

		Entity view = mc.getRenderViewEntity();
		float partialTicks = e.getPartialTicks();
		double dX = view.lastTickPosX + (view.posX - view.lastTickPosX) * (double) partialTicks;
		double dY = view.lastTickPosY + (view.posY - view.lastTickPosY) * (double) partialTicks;
		double dZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * (double) partialTicks;

		GlStateManager.pushMatrix();
		GlStateManager.translate(-dX, -dY, -dZ);
		GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());
		GlStateManager.scale(1.004F, 1.012F, 1.004F);
		GlStateManager.translate(-0.002F, -0.002F, -0.002F);
		GlStateManager.disableTexture2D();

		if (state.getRenderType() == EnumBlockRenderType.MODEL) {
			BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
			IBakedModel model = dispatcher.getBlockModelShapes().getModelForState(state);

			RenderUtil.renderGhostModel(state, model, world, 0x55000000, false);
		}
		
		if (entity != null) {
			TileEntityRendererDispatcher.instance.renderTileEntityAt(entity, 0, 0, 0, partialTicks);
		}

		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

}
