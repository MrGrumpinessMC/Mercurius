package net.minecraftforge.mercurius;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.mercurius.binding.ClientBinding;
import net.minecraftforge.mercurius.binding.ModConfigGui;
import net.minecraftforge.mercurius.binding.ServerBinding;
import net.minecraftforge.mercurius.helpers.DataHelper;
import net.minecraftforge.mercurius.utils.Commands;
import net.minecraftforge.mercurius.utils.GameEnvironment;
import net.minecraftforge.mercurius.utils.LogHelper;

import java.util.Set;

@Mod(modid = StatsMod.MODID, name = StatsMod.MODNAME,  version = StatsMod.VERSION, guiFactory = StatsMod.GUIFACTORY)
public class StatsMod
{
    public static final String MODID = "mercurius";
    public static final String MODNAME = "Mercurius";
    public static final String VERSION = "1.0.4"; //Can we get gradle to replace these things?
    public static final String GUIFACTORY = "net.minecraftforge.mercurius.StatsMod$GuiFactory";

    @Mod.Instance(StatsMod.MODID)
    public static StatsMod instance;
    public static IMinecraftBinding binding;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        if (e.getSide() == Side.CLIENT)
            binding = new ClientBinding(e.getModConfigurationDirectory());
        else
            binding = new ServerBinding(e.getModConfigurationDirectory());
        Mercurius.bootstrap(binding);
    }

    @EventHandler
    public void init(FMLInitializationEvent e)
    {
        FMLCommonHandler.instance().bus().register(new ConnectionEvents());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        try
        {
            Mercurius.getSender().collectData(Commands.START, true);
            Mercurius.getSender().startTimer(); //Do we want to tick when people are sitting in menu?
            //We also need to figure out how to deal with ticking the client in MP? Because the cancel in server stop would derp it up.
        }
        catch (Exception e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent e)
    {
        try
        {
            Mercurius.getSender().collectData(Commands.START, true);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent e)
    {
        try
        {
            Mercurius.getSender().collectData(Commands.STOP, true);
            Mercurius.getSender().cancelTimer();
            Mercurius.getBinding().resetSessionID();
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    public static class GuiFactory implements IModGuiFactory
    {
        @Override public Class<? extends GuiScreen> mainConfigGuiClass(){ return ModConfigGui.class; }
        @Override public void initialize(Minecraft minecraftInstance) {}
        @Override public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
        @Override public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) { return null; }
    }

    public class ConnectionEvents
    {
        @SubscribeEvent
        public void onConnectionOpened(FMLNetworkEvent.ClientConnectedToServerEvent e)
        {
            try
            {
                if(!e.isLocal) // only fire on actual MP servers not on local.
                {
                    Mercurius.getSender().collectData(Commands.START, true, GameEnvironment.SERVER_NON_LOCAL);
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }

        @SubscribeEvent
        public void disconnectedFromServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent e)
        {
            try
            {
                if(!e.manager.isLocalChannel()) // only fire on actual MP servers not on local.
                {
                    Mercurius.getSender().collectData(Commands.STOP, true, GameEnvironment.SERVER_NON_LOCAL);
                    Mercurius.getBinding().resetSessionID();
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }
}
