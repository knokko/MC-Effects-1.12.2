package nl.knokko.effects.mod;

import org.lwjgl.util.vector.Vector3f;

import scala.actors.threadpool.Arrays;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EffectsEventHandler {
	
	private static EntityArrow[] arrows = new EntityArrow[0];
	
	public static float p1;
	public static float p2;
	public static float p3;
	
	public static float angle;
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void changeFogColor(FogColors event){
		Entity entity = event.getEntity();
		World world = entity.getEntityWorld();
		Block block = event.getState().getBlock();
		if(block == Blocks.LAVA){
			event.setRed(0.25f);
			event.setGreen(0);
			event.setBlue(0.5f);
		}
		if(block == Blocks.AIR && world.provider.getDimension() == -1){
			event.setRed(0.05f);
			event.setGreen(0f);
			event.setBlue(0.1f);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerRender(RenderPlayerEvent.Pre event){
		World world = event.getEntityPlayer().getEntityWorld();
		//world.spawnParticle(EnumParticleTypes.REDSTONE, event.getX() + 2, event.getY(), event.getZ(), 0, 0, 0, new int[0]);
		//System.out.println("render");
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onHandRender(RenderHandEvent event){
		//System.out.println("render hand");
		/*
		World world = Minecraft.getMinecraft().world;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		for(float y = 0; y < 2; y += 0.4f){
			world.spawnParticle(EnumParticleTypes.REDSTONE, player.posX + Math.sin(angle), player.posY + y, player.posZ + Math.cos(angle), p1, p2, p3);
		}*/
		angle += 0.1f;
	}
	
	@SubscribeEvent
	public void onClientChat(ClientChatEvent event){
		String m = event.getMessage();
		try {
			if(m.startsWith("setParticleColor(")){
				int index0 = m.indexOf("(");
				int index1 = m.indexOf(",");
				int index2 = m.indexOf(",", index1 + 1);
				int index3 = m.indexOf(")");
				p1 = Float.parseFloat(m.substring(index0 + 1, index1));
				p2 = Float.parseFloat(m.substring(index1 + 1, index2));
				p3 = Float.parseFloat(m.substring(index2 + 1, index3));
				if(p1 < 0.01f)
					p1 = 0.01f;//if p1 is too small, the color will be default (red)
				event.setCanceled(true);
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = false)
	public void onEntityJoin(EntityJoinWorldEvent event){
		if(event.getEntity() instanceof EntityArrow){
			EntityArrow[] newArrows = new EntityArrow[arrows.length + 1];
			for(int index = 0; index < arrows.length; index++)
				newArrows[index] = arrows[index];
			newArrows[arrows.length] = (EntityArrow) event.getEntity();
			arrows = newArrows;
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(ClientTickEvent event){
		WorldClient world = Minecraft.getMinecraft().world;
		if(world == null)
			return;
		for(int index = 0; index < arrows.length; index++){
			EntityArrow arrow = arrows[index];
			if(arrow.isDead || (arrow.posX == arrow.prevPosX && arrow.posY == arrow.prevPosY && arrow.posZ == arrow.prevPosZ)){
				int index2 = index + 1;
				EntityArrow[] newArrows = new EntityArrow[arrows.length - 1];
				for(; index2 < arrows.length; index2++){
					newArrows[index2 - 1] = arrows[index2];
				}
				for(int i = 0; i < index; i++)
					newArrows[i] = arrows[i];
				arrows = newArrows;
				index--;
			}
			else if(arrow.world == world){
				for(int i = 0; i < 5; i++){
					Vector3f rel = pointAt(angle + i * 0.4f, new Vector3f((float)arrow.motionX, (float)arrow.motionY, (float)arrow.motionZ));
					world.spawnParticle(EnumParticleTypes.REDSTONE, arrow.posX + rel.x * 0.2f, arrow.posY + rel.y * 0.2f, arrow.posZ + rel.z * 0.2f, p1, p2, p3);
				}
			}
		}
	}
	
	private static Vector3f pointAt(float angle, Vector3f normal) {
		normal.normalise();
        float xv = (float) Math.cos(angle);
        float yv = (float) Math.sin(angle);

        Vector3f v = findV(normal);
        Vector3f w = Vector3f.cross(v, normal, null);

        // Return center + r * (V * cos(a) + W * sin(a))
        Vector3f r1 = (Vector3f) v.scale(xv);
        Vector3f r2 = (Vector3f) w.scale(yv);

        return new Vector3f(r1.x + r2.x,
                         r1.y + r2.y,
                         r1.z + r2.z);
    }
	
	private static Vector3f findV(Vector3f normal) {
        Vector3f vp = new Vector3f(0f, 0f, 0f);
        if (normal.x != 0 || normal.y != 0) {
            vp = new Vector3f(0f, 0f, 1f);
        } else if (normal.x != 0 || normal.z != 0) {
            vp = new Vector3f(0f, 1f, 0f);
        } else if (normal.y != 0 || normal.z != 0) {
            vp = new Vector3f(1f, 0f, 0f);
        } else {
            return null; // will cause an exception later.
        }

        Vector3f cp = Vector3f.cross(normal, vp, null);
        return cp.normalise(null);
    }
}