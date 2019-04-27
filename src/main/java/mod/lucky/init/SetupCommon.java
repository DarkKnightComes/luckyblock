package mod.lucky.init;

import mod.lucky.Lucky;
import mod.lucky.block.BlockLuckyBlock;
import mod.lucky.crafting.RecipeLuckCrafting;
import mod.lucky.drop.func.DropFunction;
import mod.lucky.entity.EntityLuckyPotion;
import mod.lucky.entity.EntityLuckyProjectile;
import mod.lucky.item.ItemLuckyBlock;
import mod.lucky.item.ItemLuckyBow;
import mod.lucky.item.ItemLuckyPotion;
import mod.lucky.item.ItemLuckySword;
import mod.lucky.network.ParticlePacket;
import mod.lucky.resources.loader.PluginLoader;
import mod.lucky.resources.loader.ResourceRegistry;
import mod.lucky.structure.rotation.Rotations;
import mod.lucky.tileentity.TileEntityLuckyBlock;
import mod.lucky.world.LuckyTickHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.util.ArrayList;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SetupCommon {
    public static final RecipeSerializers.SimpleSerializer<RecipeLuckCrafting>
        luckCraftingSerializer =
            RecipeSerializers.register(new RecipeSerializers.SimpleSerializer<>(
                "lucky:luck_crafting", RecipeLuckCrafting::new));

    public static final EntityType<EntityLuckyPotion> luckyPotionType =
        EntityType.register("lucky:potion",
            EntityType.Builder.create(
                EntityLuckyPotion.class, EntityLuckyPotion::new));

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(Lucky.luckyBlock);
        for (PluginLoader plugin : Lucky.luckyBlockPlugins)
            event.getRegistry().register(plugin.getBlock());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(
            new ItemLuckyBlock(Lucky.luckyBlock)
                .setRegistryName(Lucky.luckyBlock.getRegistryName()));
        event.getRegistry().register(Lucky.luckySword);
        event.getRegistry().register(Lucky.luckyBow);
        event.getRegistry().register(Lucky.luckyPotion);

        for (PluginLoader plugin : Lucky.luckyBlockPlugins) {
            event.getRegistry().register(
                new ItemLuckyBlock(plugin.getBlock())
                    .setRegistryName(plugin.getBlock().getRegistryName()));
            if (plugin.getSword() != null) event.getRegistry().register(plugin.getSword());
            if (plugin.getBow() != null) event.getRegistry().register(plugin.getBow());
            if (plugin.getPotion() != null) event.getRegistry().register(plugin.getPotion());
        }

        Lucky.resourceRegistry.loadAllResources(true);
    }

    private static void setupNetwork() {
        // network channel
        Lucky.networkChannel = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation("lucky", "lucky_channel"))
            .clientAcceptedVersions(v -> true)
            .serverAcceptedVersions(v -> true)
            .networkProtocolVersion(() -> FMLNetworkConstants.NETVERSION)
            .simpleChannel();

        // packet for spawning particles
        Lucky.networkChannel.messageBuilder(ParticlePacket.class, 0)
            .decoder(ParticlePacket::decode)
            .encoder(ParticlePacket::encode)
            .consumer(ParticlePacket::handle)
            .add();
    }

    public static void setupEntities() {
        // lucky projectile entity
        ForgeRegistries.ENTITIES.register(
            EntityType.register("lucky:projectile",
                EntityType.Builder.create(
                    EntityLuckyProjectile.class, EntityLuckyProjectile::new)));

        // lucky potion entity
        ForgeRegistries.ENTITIES.register(
            EntityType.register("lucky:potion",
                EntityType.Builder.create(
                    EntityLuckyPotion.class, EntityLuckyPotion::new)));

        // lucky block tile entity
        ForgeRegistries.TILE_ENTITIES.register(
            TileEntityType.register("lucky:lucky_block",
                TileEntityType.Builder.create(TileEntityLuckyBlock::new)));
    }

    public static void setupItemsAndBlocks() {
        Lucky.luckyBlock = (BlockLuckyBlock) new BlockLuckyBlock()
            .setRegistryName("lucky_block");
        Lucky.luckySword = (ItemLuckySword) new ItemLuckySword()
            .setRegistryName("lucky_sword");
        Lucky.luckyBow = (ItemLuckyBow) new ItemLuckyBow()
            .setRegistryName("lucky_bow");
        Lucky.luckyPotion = (ItemLuckyPotion) new ItemLuckyPotion()
            .setRegistryName("lucky_potion");
    }

    public static void setup() {
        SetupCommon.setupNetwork();
        SetupCommon.setupItemsAndBlocks();
        SetupCommon.setupEntities();

        Lucky.luckyBlockPlugins = new ArrayList<PluginLoader>();
        //Lucky.structures = new ArrayList<Structure>();

        // lucky block worl generator
        //GameRegistry.registerWorldGenerator(luckyBlock.getWorldGenerator(), 1);

        Lucky.tickHandler = new LuckyTickHandler();
        MinecraftForge.EVENT_BUS.register(Lucky.tickHandler);

        Rotations.registerRotationHandlers();
        DropFunction.registerFunctions();

        Lucky.resourceRegistry = new ResourceRegistry(new File("."));
        Lucky.resourceRegistry.registerPlugins();
        Lucky.resourceRegistry.extractDefaultResources();
        Lucky.resourceRegistry.loadAllResources(false);
    }
}