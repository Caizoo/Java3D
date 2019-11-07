package terrain;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import models.RawModel;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import renderEngine.Loader;
import textures.ModelTexture;
import textures.TerrainTexture;
import toolBox.Maths;

public class Terrain {
	
	public static final float SIZE = 800;
	private static final float MAX_HEIGHT = 80;
	private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;
	private static final int VERTEX_COUNT = 128*2;
	private static Random random = new Random();
	private static final int SEED = random.nextInt(1000000000);
	
	private float x;
	private float z;
	private RawModel model;
	private ArrayList<TerrainTexture> textures;
	
	private float[][] heights;
	
	public HeightsGenerator generator;
	
	public Terrain(int gridX, int gridZ, Loader loader, ArrayList<TerrainTexture> textures) {
		this.x = gridX * SIZE;
		this.z = gridZ * SIZE;
		generator = new HeightsGenerator(gridX, gridZ, VERTEX_COUNT, SEED);
		this.model = generateTerrain(loader);
		this.textures = textures;
	}

	private RawModel generateTerrain(Loader loader){
		
		heights = new float[VERTEX_COUNT][VERTEX_COUNT];
		int count = VERTEX_COUNT * VERTEX_COUNT;
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count*2];
		int[] indices = new int[6*(VERTEX_COUNT-1)*(VERTEX_COUNT-1)];
		int vertexPointer = 0;
		for(int i=0;i<VERTEX_COUNT;i++){
			for(int j=0;j<VERTEX_COUNT;j++){
				vertices[vertexPointer*3] = (float)j/((float)VERTEX_COUNT - 1) * SIZE;
				float height = getHeight(j,i,generator);
				heights[j][i] = height;
				vertices[vertexPointer*3+1] = height;
				vertices[vertexPointer*3+2] = (float)i/((float)VERTEX_COUNT - 1) * SIZE;
				Vector3f normal = calculateNormal(j, i, generator);
				normals[vertexPointer*3] = normal.x;
				normals[vertexPointer*3+1] = normal.y;
				normals[vertexPointer*3+2] = normal.z;
				textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
				textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz=0;gz<VERTEX_COUNT-1;gz++){
			for(int gx=0;gx<VERTEX_COUNT-1;gx++){
				int topLeft = (gz*VERTEX_COUNT)+gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz+1)*VERTEX_COUNT)+gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		return loader.loadToVAO(vertices, textureCoords, normals, indices);
	}
	
	private Vector3f calculateNormal(int x, int z, HeightsGenerator generator) {
		float heightL = getHeight(x-1,z,generator);
		float heightR = getHeight(x+1,z,generator);
		float heightD = getHeight(x,z-1,generator);
		float heightU = getHeight(x,z+1,generator);
		Vector3f normal = new Vector3f(heightL-heightR,2f,heightD-heightU);
		normal.normalise();
		return normal;
	}
	
	public float getHeight(int j, int i, HeightsGenerator generator) {
		return generator.generateHeight(j, i);
	}
	
	public float getHeightOfTerrain(float worldx, float worldz) {
		
		float terrainX = worldx - this.x;
		float terrainZ = worldz - this.z;
		float gridSquareSize = SIZE / ((float)heights.length - 1);
		int gridX = (int) Math.floor(terrainX / gridSquareSize);
		int gridZ = (int) Math.floor(terrainZ / gridSquareSize);
		if(gridX >= heights.length - 1 || gridZ >= heights.length - 1 || gridX < 0 || gridZ < 0) {
			return 0;
		}
		float xCoord = (terrainX % gridSquareSize)/gridSquareSize;
		float zCoord = (terrainZ % gridSquareSize)/gridSquareSize;
		float answer;
		if(xCoord <= (1-zCoord)) {
			answer = Maths.barryCentric(new Vector3f(0,heights[gridX][gridZ],0), new Vector3f(1,heights[gridX+1][gridZ],0), 
					new Vector3f(0,heights[gridX][gridZ+1],1), new Vector2f(xCoord, zCoord));
		}else{
			answer = Maths.barryCentric(new Vector3f(1,heights[gridX+1][gridZ],0), new Vector3f(1,heights[gridX+1][gridZ+1],1),
					new Vector3f(0,heights[gridX][gridZ+1],1), new Vector2f(xCoord,zCoord));
		}
		return answer;
	}
	public float getX() { return x; }
	public float getZ() { return z; }
	public RawModel getModel() { return model; }
	public TerrainTexture getTexture (int index) { return textures.get(index); }
	
}
