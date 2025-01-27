package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import models.TexturedModel;
import normalMappingRenderer.NormalMappingRenderer;
import shaders.StaticShader;
import skybox.SkyboxRenderer;
import terrain.Terrain;
import terrain.TerrainShader;

public class MasterRenderer {
	
	public static final float FOV = 70;
	public static final float NEAR_PLANE = 0.1f; 
	public static final float FAR_PLANE = 100000.0f;  
	
	public static final float RED = 0.54f;
	public static final float GREEN = 0.62f;
	public static final float BLUE = 0.69f;
	
	private Matrix4f projectionMatrix;
	
	private StaticShader shader = new StaticShader();
	private EntityRenderer renderer;
	
	private TerrainRenderer terrainRenderer;
	private TerrainShader terrainShader = new TerrainShader();
	
	private NormalMappingRenderer normalMapRenderer;
	
	private SkyboxRenderer skyboxRenderer;
	
	private HashMap<TexturedModel,ArrayList<Entity>> entities = new HashMap<TexturedModel,ArrayList<Entity>>();
	private HashMap<TexturedModel,ArrayList<Entity>> normalMapEntities = new HashMap<TexturedModel,ArrayList<Entity>>();
	private ArrayList<Terrain> terrains = new ArrayList<Terrain>();
	
	public MasterRenderer(Loader loader, Camera camera) {
		enableCulling();
		createProjectionMatrix();
		renderer = new EntityRenderer(shader, projectionMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
		normalMapRenderer = new NormalMappingRenderer(projectionMatrix);
	}
	
	public Matrix4f getProjectionMatrix() { return projectionMatrix; }
	
	public static void enableCulling() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}
	
	public static void disableCulling() {
		GL11.glDisable(GL11.GL_CULL_FACE);
	}
	
	public void render(ArrayList<Light> lights, Camera camera, Vector4f plane, int time) {
		prepare();
		shader.start();
		shader.loadClipPlane(plane);
		shader.loadSkyColour(RED, GREEN, BLUE);
		shader.loadLight(lights);
		shader.loadViewMatrix(camera);
		renderer.render(entities);
		shader.stop();
		normalMapRenderer.render(normalMapEntities, lights, camera);
		terrainShader.start();
		terrainShader.loadClipPlane(plane);
		terrainShader.loadSkyColour(RED, GREEN, BLUE);
		terrainShader.loadLight(lights);
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains);
		terrainShader.stop();
		
		skyboxRenderer.render(camera, RED, GREEN, BLUE, time);
		
		terrains.clear();
		entities.clear();
		normalMapEntities.clear();
	}
	
	public void renderScene(ArrayList<Entity> entities,ArrayList<Entity> normalEntities,ArrayList<Terrain> terrains, Player player,
			ArrayList<Light> lights, Camera camera, Vector4f plane, int time){
		processEntity(player);
		for(Entity entity:entities) {
			processEntity(entity);
		}
		for(Entity normalEntity:normalEntities) {
			processNormalMapEntity(normalEntity);
		}
		for(Terrain terrain:terrains) {
			processTerrain(terrain);
		}
		render(lights, camera, plane, time);
	}
	
	public void processTerrain(Terrain terrain) {
		terrains.add(terrain);
	}
	
	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(RED, GREEN, BLUE, 1);
	}
	
	public void processEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		ArrayList<Entity> batch = entities.get(entityModel);
		if(batch!=null) {
			batch.add(entity);
		}else{
			ArrayList<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel,newBatch);
		}
	}
	
	public void processNormalMapEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		ArrayList<Entity> batch = normalMapEntities.get(entityModel);
		if(batch!=null) {
			batch.add(entity);
		}else{
			ArrayList<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			normalMapEntities.put(entityModel,newBatch);
		}
	}
	
	private void createProjectionMatrix() {
		projectionMatrix = new Matrix4f();
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;
		
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
		
	}
	
	public void cleanUp() {
		shader.cleanUp();
		terrainShader.cleanUp();
		normalMapRenderer.cleanUp();
	}

}
