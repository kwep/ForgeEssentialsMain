package com.forgeessentials.core.misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.forgeessentials.commons.selections.WarpPoint;
import net.minecraft.entity.player.EntityPlayerMP;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.util.FunctionHelper;
import com.forgeessentials.util.OutputHandler;
import com.forgeessentials.util.PlayerInfo;
import com.forgeessentials.util.events.ServerEventHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TeleportHelper extends ServerEventHandler {

    public static class TeleportInfo {
        
        private EntityPlayerMP player;

        private long start;

        private int timeout;

        private WarpPoint point;

        private WarpPoint playerPos;

        public TeleportInfo(EntityPlayerMP player, WarpPoint point, int timeout)
        {
            this.point = point;
            this.timeout = timeout;
            this.start = System.currentTimeMillis();
            this.player = player;
            this.playerPos = new WarpPoint(player);
        }

        public boolean check()
        {
            if (playerPos.getDistanceTo(new WarpPoint(player)) > 0.2)
            {
                OutputHandler.chatWarning(player, "Teleport cancelled.");
                return true;
            }
            if (System.currentTimeMillis() - start < timeout)
            {
                return false;
            }
            doTeleport(player, point);
            return true;
        }

    }

    public static final String TELEPORT_COOLDOWN = "fe.teleport.cooldown";
    public static final String TELEPORT_WARMUP = "fe.teleport.warmup";

    private static Map<UUID, TeleportInfo> tpInfos = new HashMap<>();

    public static void teleport(EntityPlayerMP player, WarpPoint point)
    {
        // Get and check teleport cooldown
        int teleportCooldown = FunctionHelper.parseIntDefault(APIRegistry.perms.getPermissionProperty(player, TELEPORT_COOLDOWN), 0) * 1000;
        if (teleportCooldown > 0)
        {
            PlayerInfo pi = PlayerInfo.getPlayerInfo(player);
            long cooldownDuration = (pi.getLastTeleportTime() + teleportCooldown) - System.currentTimeMillis();
            if (cooldownDuration >= 0)
            {
                OutputHandler.chatNotification(player, String.format("Cooldown still active. %d seconds to go.", cooldownDuration / 1000));
                return;
            }
        }

        // Get and check teleport warmup
        int teleportWarmup = FunctionHelper.parseIntDefault(APIRegistry.perms.getPermissionProperty(player, TELEPORT_WARMUP), 0);
        if (teleportWarmup <= 0)
        {
            doTeleport(player, point);
            OutputHandler.chatConfirmation(player, "Teleported.");
            return;
        }

        // Setup timed teleport
        tpInfos.put(player.getPersistentID(), new TeleportInfo(player, point, teleportWarmup * 1000));
        OutputHandler.chatNotification(player, String.format("Teleporting. Please stand still for %s.", FunctionHelper.parseTime(teleportWarmup)));
    }

    public static void doTeleport(EntityPlayerMP player, WarpPoint point)
    {
        PlayerInfo pi = PlayerInfo.getPlayerInfo(player);
        pi.setLastTeleportOrigin(new WarpPoint(player));
        pi.setLastTeleportTime(System.currentTimeMillis());
        if (player.dimension != point.getDimension())
        {
            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().transferPlayerToDimension(player, point.getDimension());
        }
        player.playerNetServerHandler.setPlayerLocation(point.getX(), point.getY() + 0.1, point.getZ(), point.getYaw(), point.getPitch());
    }

    @SubscribeEvent
    public void serverTickEvent(TickEvent.ServerTickEvent e)
    {
        if (e.phase == TickEvent.Phase.START)
        {
            for (Iterator<TeleportInfo> it = tpInfos.values().iterator(); it.hasNext();)
            {
                TeleportInfo tpInfo = it.next();
                if (tpInfo.check())
                {
                    it.remove();
                }
            }
        }
    }

}
