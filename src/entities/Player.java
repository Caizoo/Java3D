package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import engineTester.MainGameLoop;
import models.TexturedModel;
import renderEngine.DisplayManager;
import terrain.Terrain;
import toolBox.Maths;

public class Player extends Entity{
	
	private static final float RUN_SPEED = 20;
	private static final float SPRINT_SPEED = 300;
	private static final float TURN_SPEED = 160;

	private static final float JUMP_POWER = 20;

	private float currentSpeed = 0;
	private float currentTurnSpeed = 0;
	private float upwardsSpeed = 0;
	private boolean isInAir = false;
	private boolean isSprinting = false;
	
	public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
	}
	
	public void move(Terrain terrain) {
		checkInputs();
		super.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
		float dx = (float)(distance * Math.sin(Math.toRadians(super.getRotY())));
		float dz = (float)(distance * Math.cos(Math.toRadians(super.getRotY())));
		if(this.getPosition().x + dx < MainGameLoop.maxX && this.getPosition().z + dz < MainGameLoop.maxZ && this.getPosition().x + dx > 0 && this.getPosition().z > 0) {
			super.increasePosition(dx, 0, dz);
		}else{
			super.increasePosition(-dx*2, 0, -dz*2);
		}
		checkIfOffTerrain();
		upwardsSpeed += Maths.GRAVITY * DisplayManager.getFrameTimeSeconds();
		super.increasePosition(0, upwardsSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		float terrainHeight = terrain.getHeightOfTerrain(getPosition().x, getPosition().z);
		if(terrainHeight == 0) {
			terrainHeight = getPosition().y;
		}
		if(getPosition().y < terrainHeight) {
			upwardsSpeed = 0;
			isInAir = false;
			getPosition().y = terrainHeight;
		}
		if(Mouse.isButtonDown(1)) {
			float angleChange = Mouse.getDX() * 0.3f;
			super.increaseRotation(0, -angleChange, 0);
		}
		//flying
		if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
			upwardsSpeed = 500;
		}else if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
			upwardsSpeed = 0;
		}
	}
	
	private void jump() {
		//if(getPosition().y <= TERRAIN_HEIGHT) {
		//	this.upwardsSpeed = JUMP_POWER;
		//}
		if(!isInAir) {
			this.upwardsSpeed = JUMP_POWER;
			isInAir = true;
		}
	}
	
	private void checkInputs() {
		if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)||Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
				currentSpeed = SPRINT_SPEED;
			}else if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
				currentSpeed = -SPRINT_SPEED;
			}else{
				currentSpeed = 0;
			}
		}else{
			if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
				currentSpeed = RUN_SPEED;
			}else if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
				currentSpeed = -RUN_SPEED;
			}else{
				currentSpeed = 0;
			}
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
			currentTurnSpeed = TURN_SPEED;
		}else if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
			currentTurnSpeed = -TURN_SPEED;
		}else{
			currentTurnSpeed = 0;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			jump();
		}
	}
	
	private void checkIfOffTerrain() {
		if(this.getPosition().x < 0) {
			this.setPosition(new Vector3f(1,this.getPosition().y,this.getPosition().z));
		}else if(this.getPosition().x > MainGameLoop.maxX) {
			this.setPosition(new Vector3f(MainGameLoop.maxX-1,this.getPosition().y,this.getPosition().z));
		}
		if(this.getPosition().z < 0) {
			this.setPosition(new Vector3f(this.getPosition().x,this.getPosition().y,1));
		}else if(this.getPosition().z > MainGameLoop.maxZ) {
			this.setPosition(new Vector3f(this.getPosition().x,this.getPosition().y,MainGameLoop.maxZ-1));
		}
	}
	
}
