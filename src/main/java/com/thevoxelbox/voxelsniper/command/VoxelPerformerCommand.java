package com.thevoxelbox.voxelsniper.command;

import com.google.common.collect.Lists;
import com.thevoxelbox.voxelsniper.VoxelProfileManager;
import com.thevoxelbox.voxelsniper.brush.IBrush;
import com.thevoxelbox.voxelsniper.snipe.SnipeData;
import com.thevoxelbox.voxelsniper.snipe.Sniper;
import org.bukkit.entity.Player;

import com.thevoxelbox.voxelsniper.brush.perform.IPerformerBrush;
import com.thevoxelbox.voxelsniper.brush.perform.Performer;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;

public class VoxelPerformerCommand extends VoxelCommand {

    public VoxelPerformerCommand() {
        super("VoxelPerformer");
        setIdentifier("p");
        setPermission("voxelsniper.sniper");
    }

    @Override
    public List<String> registerTabCompletion() {
        return Lists.newArrayList(Performer.getPerformerHandles());
    }

    @Override
    public boolean doCommand(Player player, String[] args) {
        Sniper sniper = VoxelProfileManager.getInstance().getSniperForPlayer(player);
        SnipeData snipeData = sniper.getSnipeData(sniper.getCurrentToolId());

        // Default command
        // Command: /p info, /p help
        if (args.length == 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("info"))) {
            player.sendMessage(ChatColor.DARK_AQUA + getName() + " Command Syntax:");
            player.sendMessage(ChatColor.GOLD + "/" + getActiveAlias() + "");
            player.sendMessage(ChatColor.YELLOW + "    Sets the performer to the default performer; Material performer.");
            player.sendMessage(ChatColor.GOLD + "/" + getActiveAlias() + " [performerHandle]");
            player.sendMessage(ChatColor.YELLOW + "    Sets the performer to the specified performer.");
            return true;
        }


        if (args.length == 0) {
            IBrush brush = sniper.getBrush(sniper.getCurrentToolId());
            if (brush instanceof IPerformerBrush) {
                ((IPerformerBrush) brush).parsePerformer("m", snipeData);
            } else {
                player.sendMessage("The active brush is not a performer brush.");
            }
            return true;
        }

        if (args.length == 1) {
            IBrush brush = sniper.getBrush(sniper.getCurrentToolId());
            if (brush instanceof IPerformerBrush) {
                boolean success = ((IPerformerBrush) brush).parsePerformer(args[0], snipeData);
                if (!success) {
                    player.sendMessage(ChatColor.RED + "No such performer with the handle " + ChatColor.DARK_RED + "'" + args[0] + "'" + ChatColor.RED + " exists.");
                }
            } else {
                player.sendMessage("The active brush is not a performer brush.");
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> doSuggestion(Player player, String[] args) {
        if (args.length == 1) {
            return getTabCompletion(args.length);
        }

        return new ArrayList<>();
    }
}
