package com.bymarcin.openglasses;

import com.bymarcin.openglasses.block.OpenGlassesTerminalBlock;
import com.bymarcin.openglasses.config.Config;
import com.bymarcin.openglasses.drivers.DriverHostCard;
import com.bymarcin.openglasses.drivers.DriverTerminal;
import com.bymarcin.openglasses.event.minecraft.AnvilEvent;
import com.bymarcin.openglasses.event.minecraft.server.ServerEventHandler;
import com.bymarcin.openglasses.integration.opencomputers.ocProgramDisks;
import com.bymarcin.openglasses.integration.opensecurity.OpenSecurity;
import com.bymarcin.openglasses.item.OpenGlassesHostCard;
import com.bymarcin.openglasses.item.OpenGlassesItem;
import com.bymarcin.openglasses.manual.Manual;
import com.bymarcin.openglasses.network.NetworkRegistry;
import com.bymarcin.openglasses.network.packet.GlassesEventPacket;
import com.bymarcin.openglasses.network.packet.GlassesStackNBT;
import com.bymarcin.openglasses.network.packet.HostInfoPacket;
import com.bymarcin.openglasses.network.packet.TerminalStatusPacket;
import com.bymarcin.openglasses.proxy.ClientProxy;
import com.bymarcin.openglasses.proxy.CommonProxy;
import com.bymarcin.openglasses.surface.OCServerSurface;
import com.bymarcin.openglasses.tileentity.OpenGlassesTerminalTileEntity;

import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.EnvironmentProvider;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;

import net.minecraft.entity.player.EntityPlayer;
import techguns.api.tginventory.ITGSpecialSlot;
import techguns.api.tginventory.TGSlotType;
import techguns.capabilities.TGExtendedPlayer;
import techguns.capabilities.TGExtendedPlayerCapProvider;
import techguns.gui.player.TGPlayerInventory;
import techguns.items.additionalslots.ItemTGSpecialSlot;

import java.util.HashSet;

@Mod(
	modid = OpenGlasses.MODID,
	version = BuildInfo.versionNumber + "-" + BuildInfo.buildNumber,
	guiFactory = OpenGlasses.GUIFACTORY,
	dependencies =
			"required-after:opencomputers@[1.7.1,);" +
			"required-after:commons0815@[1.3.7,);" + //set version dependency for rendertoolkit + guitoolkit here so that commons0815 will fail on version mismatch (as its the project name on curseForge)
			"required-after:guitoolkit;" +
			"required-after:rendertoolkit;" +
			"after:baubles;after:rtfm;after:opensecurity"
)

public class OpenGlasses {
	public static final String MODID = BuildInfo.modID;

	static final String GUIFACTORY = "com.bymarcin.openglasses.config.ConfigGUI";

	@SidedProxy(clientSide = "com.bymarcin.openglasses.proxy.ClientProxy", serverSide = "com.bymarcin.openglasses.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static boolean baubles = false, techguns = false;

	public static boolean absoluteRenderingAllowed = true;
	public static int widgetLimit = 255;

	public static boolean opensecurity = false;

	private HashSet<Item> modItems = new HashSet<>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){

		OpenGlasses.baubles = Loader.isModLoaded("baubles");
		OpenGlasses.techguns = Loader.isModLoaded("techguns");
		OpenGlasses.opensecurity = Loader.isModLoaded("opensecurity") && OpenSecurity.isCompatible();

		Config.preInit();

		Manual.preInit();

		NetworkRegistry.initialize();
		MinecraftForge.EVENT_BUS.register(this);

		OpenGlassesTerminalBlock.DEFAULT_BLOCK = new OpenGlassesTerminalBlock();

		modItems.add(new ItemBlock(OpenGlassesTerminalBlock.DEFAULT_BLOCK).setRegistryName(OpenGlassesTerminalBlock.DEFAULT_BLOCK.getRegistryName()));

		GameRegistry.registerTileEntity(OpenGlassesTerminalTileEntity.class, new ResourceLocation(MODID, "openglassesterminalte"));

		OpenGlassesItem.DEFAULT_STACK = new ItemStack(new OpenGlassesItem());
		OpenGlassesItem.initGlassesStack(OpenGlassesItem.DEFAULT_STACK);
		modItems.add(OpenGlassesItem.DEFAULT_STACK.getItem());

		OpenGlassesHostCard.DEFAULTSTACK = new ItemStack(new OpenGlassesHostCard());
		modItems.add(OpenGlassesHostCard.DEFAULTSTACK.getItem());

		proxy.init();
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		for(Item item : modItems)
			ClientProxy.registermodel(item, 0);

		for(Item manualItem : Manual.items)
			ClientProxy.registermodel(manualItem, 0);
	}

	public static boolean isGlassesStack(ItemStack stack){
		return !stack.isEmpty() && stack.getItem() instanceof OpenGlassesItem;
	}

	public static ItemStack getGlassesStack(EntityPlayer player){
		if(player == null)
			return ItemStack.EMPTY;

		ItemStack glassesStack = player.inventory.armorInventory.get(EntityEquipmentSlot.HEAD.getIndex());

		if(isGlassesStack(glassesStack))
			return glassesStack;


		if(OpenGlasses.baubles) {
			glassesStack = getGlassesStackBaubles(player);
			if(!glassesStack.isEmpty())
				return glassesStack;
		}

		if(OpenGlasses.techguns) {
			glassesStack = getGlassesStackTechguns(player);
			if(!glassesStack.isEmpty())
				return glassesStack;
		}

		return ItemStack.EMPTY;
	}

	public static ItemStack getGlassesStackBaubles(EntityPlayer e){
		IBaublesItemHandler handler = BaublesApi.getBaublesHandler(e);
		if (handler == null) return null;

		ItemStack baublesStack = handler.getStackInSlot(4);

		return isGlassesStack(baublesStack) ? baublesStack : ItemStack.EMPTY;
	}

	public static ItemStack getGlassesStackTechguns(EntityPlayer e){
		if(!e.hasCapability(TGExtendedPlayerCapProvider.TG_EXTENDED_PLAYER, null))
			return null;

		TGExtendedPlayer tgExtendedPlayer = TGExtendedPlayer.get(e);

		ItemStack techgunsStack = tgExtendedPlayer.getTGInventory().getStackInSlot(TGPlayerInventory.SLOT_FACE);

		return isGlassesStack(techgunsStack) ? techgunsStack : ItemStack.EMPTY;
	}

	@EventHandler
	public void init(FMLInitializationEvent event){
		NetworkRegistry.registerPacket(GlassesEventPacket.class, Side.SERVER);
		NetworkRegistry.registerPacket(TerminalStatusPacket.class, Side.CLIENT);
		NetworkRegistry.registerPacket(HostInfoPacket.class, Side.CLIENT);
		NetworkRegistry.registerPacket(GlassesStackNBT.class, Side.CLIENT);

		li.cil.oc.api.Driver.add(DriverHostCard.driver);

		li.cil.oc.api.Driver.add((EnvironmentProvider) DriverTerminal.driver);
		li.cil.oc.api.Driver.add((DriverItem) DriverTerminal.driver);

		Config.load();
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		for(Item item : modItems)
			event.getRegistry().register(item);

		for(Item manualItem : Manual.items)
			event.getRegistry().register(manualItem);
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(OpenGlassesTerminalBlock.DEFAULT_BLOCK);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		ocProgramDisks.register();
		//ocAssembler.register();

		MinecraftForge.EVENT_BUS.register(AnvilEvent.instances);  //register anvil event

		MinecraftForge.EVENT_BUS.register(new ServerEventHandler());

		proxy.postInit();
	}

	@EventHandler
	public static void onServerStopped(FMLServerStoppedEvent event){
		OCServerSurface.onServerStopped();
	}


	public static CreativeTabs creativeTab = new CreativeTabs(MODID){
        @Override
        public ItemStack getTabIconItem(){
            return new ItemStack(OpenGlassesTerminalBlock.DEFAULT_BLOCK);
        }
    };

}
