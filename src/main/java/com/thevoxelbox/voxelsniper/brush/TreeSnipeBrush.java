package com.thevoxelbox.voxelsniper.brush;

import com.google.common.base.Objects;
import com.thevoxelbox.voxelsniper.VoxelMessage;
import com.thevoxelbox.voxelsniper.snipe.SnipeData;
import com.thevoxelbox.voxelsniper.snipe.Undo;
import com.thevoxelbox.voxelsniper.util.UndoDelegate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

/**
 * http://www.voxelwiki.com/minecraft/Voxelsniper#The_Tree_Brush
 *
 * @author Mick
 */
public class TreeSnipeBrush extends Brush {

    private TreeType treeType = TreeType.TREE;

    /**
     *
     */
    public TreeSnipeBrush() {
        this.setName("Tree Snipe");
    }

    /***
     * Generate a tree only if the terrain is allowing it.
     * @param v SnipeData
     * @param targetBlock Block
     */
    @SuppressWarnings("deprecation")
    private void safeGenerateTree(final SnipeData v, Block targetBlock) {

        if (!targetBlock.getType().isAir()) {
            // We need the air block location above the target to generate a tree.
            Block airBlockOnTop = targetBlock.getRelative(BlockFace.UP);

            if (airBlockOnTop.getType().isAir()) {
                UndoDelegate undoDelegate = new UndoDelegate(targetBlock.getWorld());
                boolean generated = this.getWorld().generateTree(airBlockOnTop.getLocation(), this.treeType, undoDelegate);

                // We let minecraft decide if a tree can be generated at this location and check the result.
                if (generated) {
                    v.owner().storeUndo(undoDelegate.getUndo());
                } else {
                    v.sendMessage(ChatColor.GOLD + "Life didn't found a way here...");
                    v.sendMessage(ChatColor.GOLD + "Try to use the gunpowder to force tree generation.");
                }
            } else {
                v.sendMessage(ChatColor.RED + "Block above the snipe target must be an air block");
            }
        } else {
            v.sendMessage(ChatColor.GOLD + "The snipe tool failed to reach a valid target. Try somewhere else.");
        }

    }

    /***
     * Try to force the tree generation by altering the terrain beneath the snipe target location.
     * @param v SnipeData
     * @param targetBlock Block
     */
    @SuppressWarnings("deprecation")
    private void forceGenerateTree(final SnipeData v, Block targetBlock) {

        if (!targetBlock.getType().isAir()) {
            //Get the air block on top.
            Block airBlockOnTop = targetBlock.getRelative(BlockFace.UP);

            if (airBlockOnTop.getType().isAir()){

                //Prepare the new material
                Material newMaterialBeneath;
                if (treeType == TreeType.CHORUS_PLANT) {
                    newMaterialBeneath = Material.END_STONE;
                } else {
                    newMaterialBeneath = Material.GRASS_BLOCK;
                }

                //Backup and prepare undo.
                Material backupMat = targetBlock.getType();
                BlockData backupData = targetBlock.getBlockData().clone();
                UndoDelegate undoDelegate = new UndoDelegate(targetBlock.getWorld());
                undoDelegate.setBlockData(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), targetBlock.getBlockData());

                //Creating proper terrain and spawn attempt.
                targetBlock.setType(newMaterialBeneath);
                targetBlock.setBlockData(newMaterialBeneath.createBlockData());
                boolean generated = this.getWorld().generateTree(airBlockOnTop.getLocation(), this.treeType, undoDelegate);

                //Process result.
                if (generated) {
                    v.owner().storeUndo(undoDelegate.getUndo());
                } else {
                    v.sendMessage(ChatColor.RED + "Nothing will let you spawn a tree here.");
                    targetBlock.setType(backupMat);
                    targetBlock.setBlockData(backupData);
                }
            }
        } else {
            v.sendMessage(ChatColor.GOLD + "Snipe tree tool is unable to spawn trees on a block of type : " + targetBlock.getType().name());
        }

    }

    @Deprecated
    private void single(final SnipeData v, Block targetBlock) {
        UndoDelegate undoDelegate = new UndoDelegate(targetBlock.getWorld());
        Block blockBelow = targetBlock.getRelative(BlockFace.DOWN);
        BlockState currentState = blockBelow.getState();
        undoDelegate.setBlock(blockBelow);
        blockBelow.setType(Material.GRASS);
        this.getWorld().generateTree(targetBlock.getLocation(), this.treeType, undoDelegate);
        Undo undo = undoDelegate.getUndo();
        blockBelow.setBlockData(currentState.getBlockData().getMaterial().createBlockData(), true);
        undo.put(blockBelow);
        v.owner().storeUndo(undo);
    }

    @Deprecated
    private int getYOffset() {
        for (int i = 1; i < (getTargetBlock().getWorld().getMaxHeight() - 1 - getTargetBlock().getY()); i++) {
            if (Objects.equal(getTargetBlock().getRelative(0, i + 1, 0).getType(), Material.AIR)) {
                return i;
            }
        }
        return 0;
    }


    private void printTreeType(final VoxelMessage vm) {
        StringBuilder printout = new StringBuilder();

        boolean delimiterHelper = true;
        for (final TreeType treeType : TreeType.values()) {
            if (delimiterHelper) {
                delimiterHelper = false;
            } else printout.append(", ");
            printout.append((treeType.equals(this.treeType)) ? ChatColor.GRAY + treeType.name().toLowerCase() : ChatColor.DARK_GRAY + treeType.name().toLowerCase()).append(ChatColor.WHITE);
        }

        vm.custom(printout.toString());
    }

    @Override
    protected final void arrow(final SnipeData v) {
        this.safeGenerateTree(v, getTargetBlock());
    }

    @Override
    protected final void powder(final SnipeData v) {
        this.forceGenerateTree(v, getTargetBlock());
    }

    @Override
    public final void info(final VoxelMessage vm) {
        vm.brushName(this.getName());
        this.printTreeType(vm);
    }

    @Override
    public final void parseParameters(final String triggerHandle, final String[] params, final SnipeData v) {

        if (params[0].equalsIgnoreCase("info")) {
            v.sendMessage(ChatColor.GOLD + "Tree Snipe Brush Parameters:");
            v.sendMessage(ChatColor.AQUA + "/b " + triggerHandle + " [treeType]  -- Change tree type");
            v.sendMessage(ChatColor.GREEN + "----USAGE----");
            v.sendMessage(ChatColor.GREEN + "Tool -> Arrow : Safe generation");
            v.sendMessage(ChatColor.GREEN + "Tool -> Gunpowder : Forced generation");
            return;
        }

        try {
            this.treeType = TreeType.valueOf(params[0].toUpperCase());
            this.printTreeType(v.getVoxelMessage());
        } catch (Exception e) {
            v.getVoxelMessage().brushMessage(ChatColor.RED + "That tree type does not exist. Use " + ChatColor.LIGHT_PURPLE + " /b " + triggerHandle + " info " + ChatColor.GOLD + " to see brush parameters.");
        }
    }

    @Override
    public List<String> registerArguments() {
        return Arrays.stream(TreeType.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Override
    public String getPermissionNode() {
        return "voxelsniper.brush.treesnipe";
    }
}
