package com.thevoxelbox.voxelsniper.brush;

import com.google.common.collect.Lists;
import com.thevoxelbox.voxelsniper.snipe.SnipeData;
import com.thevoxelbox.voxelsniper.snipe.Undo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * http://www.voxelwiki.com/minecraft/Voxelsniper#Blend_Brushes
 */
public class BlendBallBrush extends BlendBrushBase {

    /**
     * TODO: NEEDS TO CHECK AFTER REWRITE
     */
    public BlendBallBrush() {
        this.setName("Blend Ball");
    }

    @SuppressWarnings("deprecation")
    @Override
    protected final void blend(final SnipeData v) {
        final int brushSize = v.getBrushSize();
        final int brushSizeDoubled = 2 * brushSize;
        // Array that holds the original materials plus a buffer
        final Material[][][] oldMaterials = new Material[2 * (brushSize + 1) + 1][2 * (brushSize + 1) + 1][2 * (brushSize + 1) + 1];
        // Array that holds the blended materials
        final Material[][][] newMaterials = new Material[brushSizeDoubled + 1][brushSizeDoubled + 1][brushSizeDoubled + 1];

        // Log current materials into oldmats
        for (int x = 0; x <= 2 * (brushSize + 1); x++) {
            for (int y = 0; y <= 2 * (brushSize + 1); y++) {
                for (int z = 0; z <= 2 * (brushSize + 1); z++) {
                    oldMaterials[x][y][z] = this.getBlockMaterialAt(this.getTargetBlock().getX() - brushSize - 1 + x, this.getTargetBlock().getY() - brushSize - 1 + y, this.getTargetBlock().getZ() - brushSize - 1 + z);
                }
            }
        }

        // Log current materials into newmats
        for (int x = 0; x <= brushSizeDoubled; x++) {
            for (int y = 0; y <= brushSizeDoubled; y++) {
                for (int z = 0; z <= brushSizeDoubled; z++) {
                    newMaterials[x][y][z] = oldMaterials[x + 1][y + 1][z + 1];
                }
            }
        }

        // Blend materials
        for (int x = 0; x <= brushSizeDoubled; x++) {
            for (int y = 0; y <= brushSizeDoubled; y++) {
                for (int z = 0; z <= brushSizeDoubled; z++) {
                    Map<Material, Integer> materialFrequency = new HashMap<>();

                    boolean tiecheck = true;

                    for (int m = -1; m <= 1; m++) {
                        for (int n = -1; n <= 1; n++) {
                            for (int o = -1; o <= 1; o++) {
                                if (!(m == 0 && n == 0 && o == 0)) {
                                    Material currentMaterial = oldMaterials[x + 1 + m][y + 1 + n][z + 1 + o];
                                    int currentFrequency = materialFrequency.getOrDefault(currentMaterial, 0) + 1;

                                    materialFrequency.put(currentMaterial, currentFrequency);
                                }
                            }
                        }
                    }

                    int highestMaterialCount = 0;
                    Material highestMaterial = Material.AIR;

                    // Find most common neighbouring material
                    for (Entry<Material, Integer> e : materialFrequency.entrySet()) {
                        if (e.getValue() > highestMaterialCount && !(this.excludeAir && e.getKey().isAir()) && !(this.excludeWater && e.getKey() == Material.WATER)) {
                            highestMaterialCount = e.getValue();
                            highestMaterial = e.getKey();
                        }
                    }

                    // Make sure that there's no tie in highest material
                    for (Entry<Material, Integer> e : materialFrequency.entrySet()) {
                        if (e.getValue() == highestMaterialCount && !(this.excludeAir && e.getKey().isAir()) && !(this.excludeWater && e.getKey() == Material.WATER)) {
                            if (e.getKey() == highestMaterial) {
                                continue;
                            }
                            tiecheck = false;
                        }
                    }

                    // Record most common neighbor material for this block
                    if (tiecheck) {
                        newMaterials[x][y][z] = highestMaterial;
                    }
                }
            }
        }

        final Undo undo = new Undo();
        final double rSquared = Math.pow(brushSize + 1, 2);

        // Make the changes  
        for (int x = brushSizeDoubled; x >= 0; x--) {
            final double xSquared = Math.pow(x - brushSize - 1, 2);

            for (int y = 0; y <= brushSizeDoubled; y++) {
                final double ySquared = Math.pow(y - brushSize - 1, 2);

                for (int z = brushSizeDoubled; z >= 0; z--) {
                    if (xSquared + ySquared + Math.pow(z - brushSize - 1, 2) <= rSquared) {
                        if (!(this.excludeAir && newMaterials[x][y][z].isAir()) && !(this.excludeWater && (newMaterials[x][y][z] == Material.WATER))) {
                            if (this.getBlockMaterialAt(this.getTargetBlock().getX() - brushSize + x, this.getTargetBlock().getY() - brushSize + y, this.getTargetBlock().getZ() - brushSize + z) != newMaterials[x][y][z]) {
                                undo.put(this.clampY(this.getTargetBlock().getX() - brushSize + x, this.getTargetBlock().getY() - brushSize + y, this.getTargetBlock().getZ() - brushSize + z));
                            }
                            this.setBlockMaterialAt(this.getTargetBlock().getZ() - brushSize + z, this.getTargetBlock().getX() - brushSize + x, this.getTargetBlock().getY() - brushSize + y, newMaterials[x][y][z]);
                        }
                    }
                }
            }
        }
        v.owner().storeUndo(undo);
    }

    @Override
    public final void parseParameters(final String triggerHandle, final String[] params, final SnipeData v) {
        if (params[0].equalsIgnoreCase("info")) {
            v.sendMessage(ChatColor.GOLD + "Blend Ball Parameters:");
            v.sendMessage(ChatColor.AQUA + "/b " + triggerHandle + " water -- toggle include water (default: exclude)");
            return;
        }

        super.parseParameters(triggerHandle, params, v);
    }

    @Override
    public String getPermissionNode() {
        return "voxelsniper.brush.blendball";
    }
}
