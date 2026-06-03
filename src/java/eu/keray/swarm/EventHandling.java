package eu.keray.swarm;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandling {

    public EventHandling() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }

        if (!(event.getEntity() instanceof EntityCreature)) {
            return;
        }

        EntityCreature ent = (EntityCreature) event.getEntity();

        // Don't inject targeting AI into mobs from excluded mods (e.g. SRP parasites).
        if (MonsterSwarmMod.isSwarmExcluded(ent)) {
            return;
        }

        for (Class<?> cls : MonsterSwarmMod.ExcludedAttackers) {
            if (cls.isInstance(ent)) {
                return;
            }
        }

        boolean inst = false;
        for (Class<?> cls : MonsterSwarmMod.IncludedAttackers) {
            if (cls.isInstance(ent)) {
                inst = true;
                break;
            }
        }

        if (inst) {
            if (Config.ATTACK_ANIMALS && !(ent instanceof net.minecraft.entity.monster.EntityCreeper)) {
                ent.targetTasks.addTask(3,
                        (EntityAIBase) new EntityAINearestAttackableTarget<>(ent, EntityAnimal.class, false, false));
            }
            ent.targetTasks.addTask(5,
                    (EntityAIBase) new EntityAINearestAttackableTarget<>(ent, EntityGolem.class, false, false));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.getEntity() instanceof EntityMob && event.getTarget() instanceof EntityMob) {
            // Leave mob-vs-mob targeting alone if either side is from an excluded mod,
            // so SRP parasites can still fight/infect zombies, animals, etc.
            if (MonsterSwarmMod.isSwarmExcluded(event.getEntity())
                    || MonsterSwarmMod.isSwarmExcluded(event.getTarget())) {
                return;
            }
            ((EntityMob) event.getEntity()).setAttackTarget(null);
        }
    }
}
