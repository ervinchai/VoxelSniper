package com.thevoxelbox.voxelsniper.brush;

import java.util.ArrayList;
import java.util.List;

import com.thevoxelbox.voxelsniper.VoxelMessage;
import com.thevoxelbox.voxelsniper.snipe.SnipeData;

import org.bukkit.ChatColor;

public class ErodeBlendBrush extends Brush {

    private ErodeBrush erodeBrush;
    private BlendBallBrush blendBallBrush;

    public ErodeBlendBrush() {
        this.erodeBrush = new ErodeBrush();
        this.blendBallBrush = new BlendBallBrush();
        this.setName("Erode BlendBall");
    }

    @Override
    public final void arrow(final SnipeData v) {
        erodeBrush.setTargetBlock(this.getTargetBlock());
        erodeBrush.arrow(v);
        blendBallBrush.setTargetBlock(this.getTargetBlock());
        blendBallBrush.arrow(v);
    }

    @Override
    protected void powder(SnipeData v) {
        erodeBrush.setTargetBlock(this.getTargetBlock());
        erodeBrush.powder(v);
        blendBallBrush.excludeAir = false;
        blendBallBrush.setTargetBlock(this.getTargetBlock());
        blendBallBrush.arrow(v);
    }

    @Override
    public void parseParameters(String triggerHandle, String[] params, SnipeData v) {

        if (params[0].equalsIgnoreCase("water")) {
            this.erodeBrush.parseParameters(triggerHandle, params, v);
            this.blendBallBrush.parseParameters(triggerHandle, params, v);
        } else if (params[0].equalsIgnoreCase("info")) {
            v.sendMessage(ChatColor.GOLD + "Erode Blend Brush Parameters:");
            v.sendMessage(ChatColor.AQUA + "/b " + triggerHandle + " [preset]  -- Change active erode brush preset");
            v.sendMessage(ChatColor.AQUA + "/b " + triggerHandle + " water -- toggle include water (default: exclude)");
            
        } else {
            this.erodeBrush.parseParameters(triggerHandle, params, v);
        }
    }

    @Override
    public void info(VoxelMessage vm) {
        vm.brushName(this.getName());
        vm.size();
        vm.voxel();
        vm.custom(ChatColor.GOLD + "Active brush preset is " + ChatColor.YELLOW + erodeBrush.getPresetName() + ChatColor.GOLD + ".");
        vm.custom(ChatColor.BLUE + "Water Mode: " + (blendBallBrush.excludeWater ? "exclude" : "include"));
    }

    @Override
    public String getPermissionNode() {
        return "voxelsniper.brush.erodeblend";
    }

    @Override
    public List<String> registerArguments() {
        List<String> arguments = new ArrayList<>();

        arguments.addAll(erodeBrush.registerArguments());
        arguments.addAll(blendBallBrush.registerArguments());
        return arguments;
    }
}