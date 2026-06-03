package eu.keray.swarm;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = "monsterswarm", name = "Monster Swarm", version = "2.0.2", acceptedMinecraftVersions = "[1.12.2]")
public class MonsterSwarmMod {

    public static final String MODID = "monsterswarm";
    public static final String NAME = "Monster Swarm";
    public static final String VERSION = "2.0.2";

    public static Logger logger;

    @Instance("monsterswarm")
    public static MonsterSwarmMod INSTANCE;

    public static final List<Class<?>> ExcludedSurfaceAttackers = new ArrayList<>();
    public static final List<Class<?>> ExcludedAttackers = new ArrayList<>();
    public static final List<Class<?>> IncludedAttackers = new ArrayList<>();
    public static final List<Class<?>> IncludedTargets = new ArrayList<>();
    public static final List<Class<?>> IncludedDiggers = new ArrayList<>();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        Config.preInit(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        new EventHandling();

        if (Config.MODIFY_RESISTANCE) {
            Blocks.PLANKS.setResistance(10.0F);
            Blocks.LOG.setResistance(18.0F);
            Blocks.GRAVEL.setResistance(11.0F);
            Blocks.STONEBRICK.setResistance(16.0F);
            Blocks.BRICK_BLOCK.setResistance(15.0F);
            Blocks.NETHER_BRICK.setResistance(12.0F);
            Blocks.SANDSTONE.setResistance(15.0F);
            Blocks.COBBLESTONE.setResistance(12.0F);
            Blocks.RED_SANDSTONE.setResistance(15.0F);
            Blocks.RED_SANDSTONE_STAIRS.setResistance(12.0F);
            Blocks.DIRT.setResistance(3.0F);
            Blocks.STONE.setResistance(12.0F);
            Blocks.SAND.setResistance(4.0F);
            Blocks.STONE_STAIRS.setResistance(12.0F);

            Magic.setResist("Railcraft:brick.infernal", 16.0F);
            Magic.setResist("Railcraft:brick.abyssal", 16.0F);
            Magic.setResist("Railcraft:brick.sandy", 16.0F);
            Magic.setResist("Railcraft:brick.frostbound", 16.0F);
            Magic.setResist("Railcraft:brick.quarried", 16.0F);
            Magic.setResist("Railcraft:brick.bleachedbone", 16.0F);
            Magic.setResist("Railcraft:brick.bloodstained", 16.0F);
            Magic.setResist("Railcraft:brick.nether", 16.0F);
            Magic.setResist("Railcraft:stair", 12.0F);
            Magic.setResist("Railcraft:wall.beta", 12.0F);
            Magic.setResist("Railcraft:wall.alpha", 12.0F);
            Magic.setResist("Railcraft:slab", 12.0F);
            Magic.setResist("BiomesOPlenty:mudBricks", 15.0F);
            Magic.setResist("BiomesOPlenty:mudBricksStairs", 11.0F);
            Magic.setResist("MineFactoryReloaded:brick", 16.0F);

            Magic.addClass(IncludedAttackers, "net.minecraft.entity.monster.EntityMob");
            Magic.addClass(IncludedAttackers, "net.minecraft.entity.monster.IMob");
            Magic.addClass(IncludedAttackers, "drzhark.mocreatures.entity.passive.MoCEntityBear");
            Magic.addClass(IncludedAttackers, "drzhark.mocreatures.entity.passive.MoCEntityBoar");
            Magic.addClass(IncludedAttackers, "drzhark.mocreatures.entity.passive.MoCEntityCrocodile");

            Magic.addClass(ExcludedSurfaceAttackers, "drzhark.mocreatures.entity.monster.MoCEntityGolem");
            Magic.addClass(ExcludedSurfaceAttackers, "drzhark.mocreatures.entity.monster.MoCEntityMiniGolem");
            Magic.addClass(ExcludedSurfaceAttackers, "drzhark.mocreatures.entity.monster.MoCEntityOgre");
            Magic.addClass(ExcludedAttackers, "net.minecraft.entity.monster.EntityEnderman");
            Magic.addClass(ExcludedAttackers, "crazypants.enderzoo.entity.EntityOwl");

            Magic.addClass(IncludedDiggers, "drzhark.mocreatures.entity.monster.MoCEntitySilverSkeleton");
            Magic.addClass(IncludedDiggers, "net.minecraft.entity.monster.EntityZombie");
            Magic.addClass(IncludedDiggers, "net.minecraft.entity.monster.EntitySkeleton");
            Magic.addClass(IncludedDiggers, "com.gw.dm.entity.EntityLizalfos");
            Magic.addClass(IncludedDiggers, "com.gw.dm.entity.EntityRakshasa");
            Magic.addClass(IncludedDiggers, "com.gw.dm.entity.EntityCaveFisher");

            Magic.addClass(IncludedTargets, "net.minecraft.entity.player.EntityPlayer");
            Magic.addClass(IncludedTargets, "net.minecraft.entity.monster.EntityGolem");
            Magic.addClass(IncludedTargets, "net.minecraft.entity.passive.EntityVillager");
            Magic.addClass(IncludedTargets, "net.shadowmage.ancientwarfare.npc.entity.NpcBase");
        }
    }

    public static boolean isSurfaceExcluded(Entity ent) {
        for (Class<?> c : ExcludedSurfaceAttackers) {
            if (c.isInstance(ent))
                return true;
        }
        return false;
    }

    @EventHandler
    public void serverLoad(FMLServerStartedEvent event) {
        for (WorldServer ws : DimensionManager.getWorlds()) {
            if (ws != null) {
                new SwarmWorld(ws);
            }
        }
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {}
}
