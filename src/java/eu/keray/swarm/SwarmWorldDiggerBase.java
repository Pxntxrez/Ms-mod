package eu.keray.swarm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import swarm.api.SwarmControl;

public class SwarmWorldDiggerBase {

    static final Block web = Blocks.WEB;

    protected final SwarmWorld sw;
    protected Block bridge;
    protected final ObjPool<Vec3I> vecpool;
    private final ValueMap<Vec3I> damaged;
    private static final float minimum = 0.26F;
    Map<EntityCreature, Mob> mobs;

    public SwarmWorldDiggerBase(SwarmWorld sw) {
        this.bridge = Blocks.DIRT;
        this.vecpool = new ObjPool<>(Vec3I.class);
        this.damaged = new ValueMap<>(2048);
        this.mobs = new HashMap<>();
        this.sw = sw;
    }

    public boolean damage(Vec3I loc, int damage) {
        return damage(loc.x, loc.y, loc.z, damage);
    }

    public boolean damage(int x, int y, int z, int damage) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = this.sw.world.getBlockState(pos);
        Block type = state.getBlock();

        if (type == Blocks.AIR || type == null)
            return true;

        int maxdamage = getMatDmg(type, x, y, z);
        if (maxdamage < 0)
            return false;

        if (!SwarmControl.instance.canSwarmBreakBlock((World) this.sw.world, x, y, z))
            return false;

        if (maxdamage == 0) {
            if (state.getMaterial() == Material.GLASS)
                this.sw.world.playSound(null, x, y, z,
                        new SoundEvent(new ResourceLocation("dig.glass")),
                        SoundCategory.BLOCKS, 1.0F, 1.0F);
            type.dropBlockAsItem((World) this.sw.world, pos, state, 0);
            this.sw.world.setBlockToAir(pos);
            return true;
        }

        if (this.damaged.size() >= 2048)
            return false;

        Vec3I loc = new Vec3I(x, y, z);
        damage = this.damaged.increment(loc, damage) + damage;
        if (damage >= maxdamage) {
            this.damaged.remove(loc, 0);
            type.dropBlockAsItem((World) this.sw.world, pos, state, 0);
            this.sw.world.setBlockToAir(pos);
            if (state.getMaterial() == Material.GLASS)
                this.sw.world.playSound(null, x, y, z,
                        new SoundEvent(new ResourceLocation("dig.glass")),
                        SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        SoundEvent sound = null;
        if (type.getSoundType() != null) {
            SoundEvent s = type.getSoundType().getBreakSound();
            if (s != null)
                sound = s;
        }
        if (sound == null)
            sound = new SoundEvent(new ResourceLocation("dig.stone"));

        this.sw.world.playSound(null, x, y, z, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);

        if (type == Blocks.STONEBRICK
                && state.getValue(BlockStoneBrick.VARIANT) == BlockStoneBrick.EnumType.DEFAULT
                && damage > maxdamage / 2) {
            this.sw.world.setBlockState(pos,
                    state.withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED), 2);
        }

        return false;
    }

    protected int getMatDmg(Block type, int x, int y, int z) {
        float res = type.getExplosionResistance(null) * Config.RESISTANCE_MULTIPLIER;
        if (res < minimum)
            return 0;
        if (res > Config.MAX_RES)
            res = Config.MAX_RES;
        return Math.max(2, (int)(res * 2.5F));
    }

    public boolean bridge(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!this.sw.world.isBlockLoaded(pos))
            return false;

        if (!SwarmControl.instance.canSwarmPlaceBlock((World) this.sw.world, x, y, z))
            return false;

        // FIX: don't place on torches, slabs, or anything that isn't a full solid surface
        // The block directly below must have a solid top face
        if (!this.sw.world.isSideSolid(new BlockPos(x, y - 1, z), EnumFacing.UP))
            return false;

        IBlockState state = this.sw.world.getBlockState(pos);
        Block type = state.getBlock();

        // FIX: only replace truly replaceable blocks (air, grass, flowers, etc.)
        // This prevents replacing torches, slabs, or other non-replaceable blocks
        if (type != null && !type.isReplaceable(this.sw.world, pos))
            return false;

        if (type != null && type != Blocks.AIR)
            type.dropBlockAsItem((World) this.sw.world, pos, state, 0);

        this.sw.world.setBlockState(pos, this.bridge.getDefaultState(), 2);
        return true;
    }

    public Mob getMob(EntityCreature ent) {
        Mob mob = this.mobs.get(ent);
        if (mob != null) {
            return mob;
        }
        mob = new Mob(ent);
        this.mobs.put(ent, mob);
        return mob;
    }

    public boolean damageLights(Vec3I loc, int height) {
        if (loc.getLightBlocks((World) this.sw.world) < 8)
            return false;

        boolean damaged = false;
        for (int xs = -1; xs <= 1; xs++) {
            for (int ys = 0; ys < height; ys++) {
                for (int zs = -1; zs <= 1; zs++) {
                    Vec3I rel = loc.getRelative(xs, ys, zs);
                    if (rel.getData((World) this.sw.world).getLightValue((IBlockAccess) this.sw.world,
                            new BlockPos(rel.x, rel.y, rel.z)) > 7) {
                        damage(rel, 1);
                        damaged = true;
                    }
                }
            }
        }
        return damaged;
    }

    public void update() {
        this.damaged.reduce(null);
        Iterator<Map.Entry<EntityCreature, Mob>> iter = this.mobs.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<EntityCreature, Mob> next = iter.next();
            if (!next.getKey().isEntityAlive())
                iter.remove();
        }
    }

    // FIX: removed stray semicolon — liquid blocks are no longer treated as free passage
    public final boolean isFreePass(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState t = this.sw.world.getBlockState(pos);
        if (t.getMaterial().isLiquid())
            return false;
        return (!t.getMaterial().blocksMovement() && t.getMaterial() != Material.PORTAL);
    }

    public final boolean tryAttack(int x, int y, int z) {
        IBlockState state = this.sw.world.getBlockState(new BlockPos(x, y, z));
        Block type = state.getBlock();
        if (type == null || type == Blocks.AIR)
            return false;
        if (state.getMaterial().blocksMovement() || type == web)
            return !damage(x, y, z, 3);
        return false;
    }

    class Mob {
        int time;
        int x;
        int y;
        int z;

        public Mob(EntityCreature ent) {
            set(ent.posX, ent.posY, ent.posZ);
        }

        public void set(double x, double y, double z) {
            this.x = (int) x;
            this.y = (int) y;
            this.z = (int) z;
        }

        public boolean isNear(Vec3I vec) {
            int dx = Math.abs(vec.x - this.x);
            int dy = Math.abs(vec.y - this.y);
            int dz = Math.abs(vec.z - this.z);
            return (dx < 4 && dy < 3 && dz < 4);
        }

        @Override
        public String toString() {
            return "mob[" + this.x + "," + this.y + "," + this.z + "]";
        }
    }
}
