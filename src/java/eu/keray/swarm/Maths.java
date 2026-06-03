package eu.keray.swarm;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;

public class Maths {

    public static double fastSqrt(double a) {
        return Double.longBitsToDouble((Double.doubleToLongBits(a) - 4503599627370496L >> 1L) + 2305843009213693952L);
    }

    public static double fastSqrtNewton(double a) {
        double sqrt = fastSqrt(a);
        sqrt = (sqrt + a / sqrt) / 2.0D;
        return (sqrt + a / sqrt) / 2.0D;
    }

    public static Vec3i findPointTowards(EntityCreature attacker, EntityLivingBase target, int dist) {
        double dx = target.posX - attacker.posX;
        double dy = target.posY - attacker.posY;
        double dz = target.posZ - attacker.posZ;
        double len = fastSqrtNewton(dx * dx + dy * dy + dz * dz);

        if (len < dist) {
            return new Vec3i(target.posX, target.posY, target.posZ);
        }

        dx = dx / len * dist;
        dy = dy / len * dist;
        dz = dz / len * dist;

        return new Vec3i(attacker.posX + dx, attacker.posY + dy, attacker.posZ + dz);
    }

    public static final boolean contains(AxisAlignedBB aabb, Entity ent) {
        return (ent.posX >= aabb.minX && ent.posX <= aabb.maxX
                && ent.posY >= aabb.minY && ent.posY <= aabb.maxY
                && ent.posZ >= aabb.minZ && ent.posZ <= aabb.maxZ);
    }
}
