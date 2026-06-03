package eu.keray.swarm;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SwarmWorld {

    protected WorldServer world;
    private SwarmWorldDigger digg;

    public final List<EntityCreature> attackers = new ArrayList<>();
    public final List<EntityLivingBase> targets = new ArrayList<>();

    public boolean isOverworld = false;
    public boolean isNether = false;
    public boolean fullmoon = true;
    public boolean isDay = false;
    int index;
    int ticks;

    public boolean isSurface(Entity mob) {
        if (mob == null) {
            return false;
        }
        return (this.isOverworld && mob.posY > 40.0D);
    }

    public SwarmWorld(WorldServer world) {
        this.index = 0;
        this.ticks = 0;
        Log.log("Registering world dimension " + world.provider.getDimension());
        this.world = world;
        this.digg = new SwarmWorldDigger(this);
        MinecraftForge.EVENT_BUS.register(this);
        if (world.provider.getDimension() == 0)
            this.isOverworld = true;
        if (world.provider.getDimension() == -1)
            this.isNether = true;
    }

    public void run() {
        this.ticks++;

        if (this.isOverworld) {
            int time = (int) this.world.getWorldTime() % 24000;
            int day = (int) this.world.getWorldTime() / 24000;
            this.isDay = (time < 12200 || time > 23850);
            this.fullmoon = (day % 8 == 0 && day > 2);
        } else {
            this.fullmoon = true;
            this.isDay = false;
        }

        if (this.index >= this.attackers.size()) {
            if (this.ticks < 20)
                return;
            this.ticks = 0;
            this.index = 0;
            collectEntities();
            this.digg.update();
            return;
        }

        int top = Math.min(this.index + 2, this.attackers.size());

        for (; this.index < top; this.index++) {
            EntityCreature mob = this.attackers.get(this.index);

            if (this.isDay && Config.KILL_MOBS_DAYTIME
                    && !mob.isNoDespawnRequired()
                    && !(mob instanceof net.minecraft.entity.monster.EntityPigZombie)) {
                int light = mob.world.getLightFor(EnumSkyBlock.SKY, mob.getPosition());
                if (light > 11) {
                    mob.setFire(8);
                    mob.attackEntityFrom(DamageSource.IN_FIRE, 2.0F);
                }
            }

            EntityLivingBase target = findTargetFor(mob, 64);
            if (target == null) {
                target = mob.getAttackTarget();
                if (target instanceof net.minecraft.entity.monster.IMob) {
                    target = null;
                    mob.setAttackTarget(null);
                }
            }

            if (target != null) {
                mob.setAttackTarget(target);

                Vec3i point = Maths.findPointTowards(mob, target, 15);

                boolean canDigg = true;

                if (this.isOverworld) {
                    if (mob.posY > 40.0D) {
                        canDigg = Config.SWARM_OVERWORLD.isSwawrm(this.fullmoon);
                    } else {
                        canDigg = Config.SWARM_UNDERGROUND.isSwawrm(this.fullmoon);
                    }
                } else if (this.isNether) {
                    canDigg = Config.SWARM_NETHER;
                } else {
                    canDigg = Config.SWARM_DIMENSIONS;
                }

                if (canDigg && (mob instanceof net.minecraft.entity.monster.EntityZombie
                        || mob instanceof net.minecraft.entity.monster.EntitySkeleton)) {
                    this.digg.process(mob, target);
                }

                mob.getNavigator().tryMoveToXYZ(
                        point.getX() + 0.5D,
                        point.getY() + 0.5D,
                        point.getZ() + 0.5D,
                        1.0D);

                if (mob.world.getBlockState(mob.getPosition()).getMaterial().isLiquid()) {
                    Vec3D vec = (new Vec3D((Entity) target)).sub((Entity) mob).normalize().scale(0.4D);
                    mob.motionX = vec.x;
                    mob.motionY = vec.y;
                    mob.motionZ = vec.z;
                }
            }
        }
    }

    public EntityLivingBase findTargetFor(EntityCreature attacker, int radius) {
        AxisAlignedBB aabb = new AxisAlignedBB(
                attacker.posX - radius, attacker.posY - radius - radius, attacker.posZ - radius,
                attacker.posX + radius, attacker.posY + radius + radius, attacker.posZ + radius);

        EntityLivingBase nearest = null;
        double ndistSq = Double.POSITIVE_INFINITY;

        for (EntityLivingBase ent : this.targets) {
            if (!ent.isEntityAlive()) {
                continue;
            }
            if (!Maths.contains(aabb, (Entity) ent)) {
                continue;
            }

            double dx = attacker.posX - ent.posX;
            double dy = attacker.posY - ent.posY;
            double dz = attacker.posZ - ent.posZ;

            double distSq = dx * dx + dz * dz;

            if (distSq > (radius * radius)) {
                continue;
            }
            if ((ent instanceof net.minecraft.entity.passive.EntityAnimal
                    || ent instanceof net.minecraft.entity.passive.EntityVillager)
                    && attacker instanceof net.minecraft.entity.monster.EntityCreeper) {
                continue;
            }
            if ((ent instanceof net.minecraft.entity.monster.EntityGolem
                    || ent instanceof net.minecraft.entity.passive.EntityAnimal) && distSq > 400.0D) {
                continue;
            }
            if (!(ent instanceof EntityPlayer)) {
                distSq += distSq;
            }
            if (distSq < ndistSq) {
                if (this.isOverworld && (
                        (ent.posY < 40.0D) ? (attacker.posY > 40.0D) : (attacker.posY < 40.0D))) {
                    continue;
                }
                nearest = ent;
                ndistSq = distSq;
            }
        }

        return nearest;
    }

    private void collectEntities() {
        this.attackers.clear();
        this.targets.clear();
        for (Entity ent : this.world.loadedEntityList) {
            if (ent instanceof net.minecraft.entity.monster.EntityEnderman) {
                continue;
            }
            if (ent instanceof EntityCreature) {
                if (ent instanceof net.minecraft.entity.monster.EntityMob
                        || ent instanceof net.minecraft.entity.monster.IMob) {

                    // Mobs from excluded mods (e.g. SRP parasites) are fully ignored —
                    // not added as attackers, so the swarm never hijacks their target or makes them dig.
                    if (MonsterSwarmMod.isSwarmExcluded(ent)) {
                        continue;
                    }

                    if (isSurface(ent) && !Config.SWARM_OVERWORLD.isSwawrm(this.fullmoon)) {
                        if (ent instanceof net.minecraft.entity.monster.EntityEnderman) {
                            continue;
                        }
                        if (MonsterSwarmMod.isSurfaceExcluded(ent)) {
                            continue;
                        }
                    }
                    this.attackers.add((EntityCreature) ent);
                    continue;
                }
            }
            if (!(ent instanceof EntityPlayer)
                    || ((EntityPlayer) ent).capabilities.isCreativeMode)
                continue;
            if (((EntityPlayer) ent).isSpectator()) {
                continue;
            }
            this.targets.add((EntityLivingBase) ent);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        run();
    }
}
