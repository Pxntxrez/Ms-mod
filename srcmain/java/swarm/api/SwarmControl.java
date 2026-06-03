package swarm.api;

import net.minecraft.world.World;

public class SwarmControl {
    public static SwarmControl instance = new SwarmControl();

    public boolean canSwarmBreakBlock(World world, int x, int y, int z) {
        return true;
    }

    public boolean canSwarmPlaceBlock(World world, int x, int y, int z) {
        return true;
    }
}
