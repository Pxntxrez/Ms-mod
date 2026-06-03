package eu.keray.swarm;

import java.util.Locale;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class Vec3D {
    public double x;
    public double y;
    public double z;

    public Vec3D() {}

    public Vec3D(Entity ent) {
        this.x = ent.posX;
        this.y = ent.posY;
        this.z = ent.posZ;
    }

    public Vec3D set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3D sub(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3D sub(Entity ent) {
        this.x -= ent.posX;
        this.y -= ent.posY;
        this.z -= ent.posZ;
        return this;
    }

    public Vec3D set(Vec3D that) {
        this.x = that.x;
        this.y = that.y;
        this.z = that.z;
        return this;
    }

    public Vec3d getMC() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public Vec3D normalize() {
        double len = length();
        return set(this.x / len, this.y / len, this.z / len);
    }

    public Vec3D scale(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        return this;
    }

    @Override
    public String toString() {
        return String.format(Locale.UK, "[%.1f, %.1f, %.1f]", this.x, this.y, this.z);
    }
}
