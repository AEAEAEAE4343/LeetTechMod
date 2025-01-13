package com.leetftw.tech_mod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class DebugHelper
{
    private static final boolean doDebug = false;

    public static void chatOutput(String message)
    {
        if (doDebug) chatOutput(Component.literal(message));
    }

    public static void chatOutput(Component message)
    {
        if (doDebug) Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(message));
    }
}
