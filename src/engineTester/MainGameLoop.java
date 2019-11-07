package engineTester;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.Renderer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import particles.Particle;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.EntityRenderer;
import shaders.StaticShader;
import terrain.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import toolBox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class MainGameLoop {
	
	private static int time;
	private static Terrain[][] terrains = new Terrain[1][1];
	private static BufferedImage wholeHeightmap;
	private static BufferedImage[][] heightmaps = new BufferedImage[1][1];
	public static final float maxX = (terrains.length * Terrain.SIZE);
	public static final float maxZ = (terrains[0].length * Terrain.SIZE);
	
	public static void main(String[] args) {
		
		DisplayManager.createDisplay();
		Loader loader = new Loader();

		
		// TERRAIN TEXTURE STUFF
		ArrayList<TerrainTexture> terrainTextures = new ArrayList<TerrainTexture>();
		terrainTextures.add(new TerrainTexture(loader.loadTexture("terrainGrass")));
		terrainTextures.add(new TerrainTexture(loader.loadTexture("terrainGrass2")));
		terrainTextures.add(new TerrainTexture(loader.loadTexture("mud")));
		terrainTextures.add(new TerrainTexture(loader.loadTexture("rock")));
		
		ArrayList<Terrain> terrainsList = new ArrayList<Terrain>();
		for(int i = 0;i<terrains.length;i++) {
			for(int j = 0;j<terrains[0].length;j++){
				terrains[j][i] = new Terrain(j,i,loader,terrainTextures);
				terrainsList.add(terrains[j][i]);
			}
		}
		
		ModelData treeData = OBJFileLoader.loadOBJ("pine");
		ModelData grassData = OBJFileLoader.loadOBJ("grass");
		ModelData fernData = OBJFileLoader.loadOBJ("fern");
		ModelData playerData = OBJFileLoader.loadOBJ("person");
		ModelData lampData = OBJFileLoader.loadOBJ("lamp");
		
		RawModel treeModel = loader.loadToVAO(treeData.getVertices(), treeData.getTextureCoords(), treeData.getNormals(), treeData.getIndices());
		RawModel grassModel = loader.loadToVAO(grassData.getVertices(), grassData.getTextureCoords(), grassData.getNormals(), grassData.getIndices());
		RawModel fernModel = loader.loadToVAO(fernData.getVertices(), fernData.getTextureCoords(), fernData.getNormals(), fernData.getIndices());
		RawModel playerModel = loader.loadToVAO(playerData.getVertices(), playerData.getTextureCoords(), playerData.getNormals(), playerData.getIndices());
		RawModel lampModel = loader.loadToVAO(lampData.getVertices(), lampData.getTextureCoords(), lampData.getNormals(), lampData.getIndices());
		
		// TREE
		TexturedModel texturedTreeModel = new TexturedModel(treeModel,new ModelTexture(loader.loadTexture("pine")));
		// GRASS
		TexturedModel texturedGrassModel = new TexturedModel(grassModel, new ModelTexture(loader.loadTexture("grass")));
		texturedGrassModel.getTexture().setTransparency(true);
		texturedGrassModel.getTexture().setFakeLighting(true);
		// FERN
		ModelTexture fernAtlasTexture = new ModelTexture(loader.loadTexture("fern"));
		fernAtlasTexture.setNumberOfRows(2);
		TexturedModel texturedFernModel = new TexturedModel(fernModel, fernAtlasTexture);
		texturedFernModel.getTexture().setTransparency(true);
		// LAMP
		TexturedModel texturedLampModel = new TexturedModel(lampModel, new ModelTexture(loader.loadTexture("lamp")));
		texturedLampModel.getTexture().setFakeLighting(true);
		//
		TexturedModel texturedPlayerModel = new TexturedModel(playerModel, new ModelTexture(loader.loadTexture("playerTexture")));
		// ENTITIES
		ArrayList<Entity> entities = new ArrayList<Entity>();
		ArrayList<Entity> normalEntities = new ArrayList<Entity>();
		
		TexturedModel texturedBarrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader),new ModelTexture(loader.loadTexture("barrel")));
		texturedBarrelModel.getTexture().setNormalMap(loader.loadTexture("barrelNormal"));
		texturedBarrelModel.getTexture().setShineDamper(10);
		texturedBarrelModel.getTexture().setReflectivity(0.5f);
		normalEntities.add(new Entity(texturedBarrelModel,new Vector3f(10,70,10),0,0,0,1f));
		TexturedModel texturedBrickModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("brick", loader),new ModelTexture(loader.loadTexture("brick_diffuse")));
		texturedBrickModel.getTexture().setNormalMap(loader.loadTexture("brick_normal"));
		normalEntities.add(new Entity(texturedBrickModel,new Vector3f(10,50,10),0,0,0,5f));
		
		Random random = new Random();
		for(int i = 0;i < 1500 * terrains.length/6;i++) {
			float x = random.nextFloat() * Terrain.SIZE * terrains.length;
			float z = random.nextFloat() * Terrain.SIZE * terrains.length;
			float y = getLocalTerrain(x,z).getHeightOfTerrain(x, z);
			entities.add(new Entity(texturedTreeModel, new Vector3f(x,y,z), 0, random.nextFloat() * 180f, 0f, 1f, false));
		}
		for(int i = 0;i < 4000 * terrains.length/6;i++) {
			float x = random.nextFloat() * Terrain.SIZE * terrains.length;
			float z = random.nextFloat() * Terrain.SIZE * terrains.length;
			float y = getLocalTerrain(x,z).getHeightOfTerrain(x, z);
			entities.add(new Entity(texturedGrassModel, new Vector3f(x,y,z), 0, random.nextFloat() * 180, 0, 1f));
		}
		for(int i = 0;i < 5000 * terrains.length/6;i++) {
			float x = random.nextFloat() * Terrain.SIZE * terrains.length;
			float z = random.nextFloat() * Terrain.SIZE * terrains.length;
			float y = getLocalTerrain(x,z).getHeightOfTerrain(x, z);
			int index = random.nextInt(4);
			entities.add(new Entity(texturedFernModel, index, new Vector3f(x,y,z),0, random.nextFloat() * 180f, 0, 0.5f));
		}
		
		
		Light sun = new Light(new Vector3f(0,100000,-100000), new Vector3f(1f,1f,1f));
		ArrayList<Light> lights = new ArrayList<Light>();
		lights.add(sun);
		lights.add(new Light(new Vector3f(100, 60, 100), new Vector3f(0.9686f, 0.8431f, 0.0313f), new Vector3f(1, 0.01f, 0.002f)));
		lights.add(new Light(new Vector3f(370, 17, -300), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
		lights.add(new Light(new Vector3f(293, 7, -305), new Vector3f(2, 2, 0), new Vector3f(1, 0.01f, 0.002f)));
		
		entities.add(new Entity(texturedLampModel, new Vector3f(185, -4.7f, -293), 0, 0, 0, 1));
		entities.add(new Entity(texturedLampModel, new Vector3f(370, 4.2f, -300), 0, 0, 0, 1));
		entities.add(new Entity(texturedLampModel, new Vector3f(293, -6.8f, -305), 0, 0, 0, 1));
		
		Player player = new Player(texturedPlayerModel, new Vector3f(100,20,100),0,0,0,0.5f);
		
		Camera camera = new Camera(player);
		camera.setPitch(10);
		
		MasterRenderer renderer = new MasterRenderer(loader, camera);
		
		ArrayList<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		//guis.add(new GuiTexture(renderer.getShadowMapTexture(),new Vector2f(0.5f,0.5f),new Vector2f(0.5f,0.5f)));
		
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture("fire"),8,true);
		ParticleSystem system = new ParticleSystem(particleTexture,150, 7, 0, 1.6f, 10);
		system.setLifeError(0.7f);
		system.setSpeedError(0.25f);
		system.setScaleError(0.2f);
		system.randomizeRoation();
		system.setDirection(new Vector3f(0,1,0), 0.2f);
		
		WaterFrameBuffers fbos = new WaterFrameBuffers();
		WaterShader waterShader = new WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(),fbos);
		ArrayList<WaterTile> waters = new ArrayList<WaterTile>();
		waters.add(new WaterTile(400,400,-10));
		MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix());
		
		while(!Display.isCloseRequested()) {
			camera.move();
			player.move(getLocalTerrain(player.getPosition().x,player.getPosition().z));
			picker.update();
			
			system.generateParticles(new Vector3f(100,47,100));
			ParticleMaster.update(camera);
			
			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
			
			//wireframe
			if(Keyboard.isKeyDown(Keyboard.KEY_1)) {
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE );
			}else if(Keyboard.isKeyDown(Keyboard.KEY_2)) {
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL );
			}	
		
			// time
			time += DisplayManager.getFrameTimeSeconds() * 500;
			time %= 24000;
			
			sun.moveSun((float)(time));
			
			// render scene to reflection texture
			fbos.bindReflectionFrameBuffer();
			float distance = 2 * (camera.getPosition().y - waters.get(0).getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			renderer.renderScene(entities,normalEntities,terrainsList,player,lights,camera,new Vector4f(0,1,0,-waters.get(0).getHeight()+1),time);
			camera.invertPitch();
			camera.getPosition().y += distance;
			
			// render scene to refraction texture
			fbos.bindRefractionFrameBuffer();
			renderer.renderScene(entities,normalEntities,terrainsList,player,lights,camera,new Vector4f(0,-1,0,waters.get(0).getHeight()+1),time);

			// render scene to screen
			fbos.unbindCurrentFrameBuffer();
			renderer.renderScene(entities,normalEntities,terrainsList,player,lights,camera,new Vector4f(0,-1,0,1000000),time);
			// water
			waterRenderer.render(waters, camera, sun);
			// particles
			ParticleMaster.renderParticles(camera);
			// guis
			guiRenderer.render(guis);
			
			DisplayManager.updateDisplay();
			
		}
		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		waterShader.cleanUp();
		fbos.cleanUp();
		ParticleMaster.cleanUp();
		DisplayManager.closeDisplay();
	}
	
	public static Terrain getLocalTerrain(float xSend, float zSend) {
		int x = (int)(xSend/Terrain.SIZE);
		int z = (int)(zSend/Terrain.SIZE);
		return terrains[x][z];
	}
	
}
		
		

