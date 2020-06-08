package com.thevoxelbox.voxelsniper.brush;

import com.thevoxelbox.voxelsniper.VoxelMessage;
import com.thevoxelbox.voxelsniper.snipe.SnipeData;

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
        blendBallBrush.excludeAir = false;
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

        if (params[0].equals("water")) {
            this.erodeBrush.parseParameters(triggerHandle, params, v);
            this.blendBallBrush.parseParameters(triggerHandle, params, v);
        } else {
            this.erodeBrush.parseParameters(triggerHandle, params, v);
        }
    }

    @Override
    public void info(VoxelMessage vm) {
        this.erodeBrush.info(vm);
        this.blendBallBrush.info(vm);

    }

    @Override
    public String getPermissionNode() {
        return "voxelsniper.brush.erodeblend";
    }

}