package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {
	
	private float distanceFromPlayer = 50;
	private boolean isThirdPerson = true;
	private boolean fHeld = false;
	private float angleAroundPlayer = 0;
	private final float HEIGHT_OFFSET = 5;
	private final float MAX_ZOOM = 4;
	private final float MAX_DISTANCE = 200;
	
	private Vector3f position = new Vector3f(0,5,0);
	private float pitch;
	private float yaw;
	private float roll;
	
	private Player player;
	
	public Camera(Player player) {
		this.player = player;
	}
	
	public void move() {
		if(Keyboard.isKeyDown(Keyboard.KEY_NUMPAD0)) {
			angleAroundPlayer = 0;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_F)) {
			if(isThirdPerson && !fHeld) {
				isThirdPerson = false;
			}else if(!isThirdPerson && !fHeld) {
				isThirdPerson = true;
				distanceFromPlayer = 50;
			}
			fHeld = true;
		}else{
			fHeld = false;
		}
		calculateZoom();
		calculatePitch();
		calculateAngleAroundPlayer();
		calculateCameraPosition();
	}
	
	private void calculateZoom() {
		float zoomLevel = Mouse.getDWheel() * 0.01f;
		if(distanceFromPlayer > MAX_ZOOM) {
			distanceFromPlayer -= zoomLevel;
			if(distanceFromPlayer <= MAX_ZOOM) {
				distanceFromPlayer = MAX_ZOOM + 0.0001f;
			}
			if(distanceFromPlayer >= MAX_DISTANCE) {
				distanceFromPlayer = MAX_DISTANCE - 0.0001f;
			}
		}
		if(!isThirdPerson) {
			distanceFromPlayer = 0;
		}
	}
	
	private void calculatePitch() {
		if(Mouse.isButtonDown(1)){
			float pitchChange = Mouse.getDY() * 0.1f;
			pitch -= pitchChange;
		}
	}
	
	private void calculateAngleAroundPlayer() {
		
	}
	
	private void calculateCameraPosition() {
		float horizontalDistance = (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
		float verticalDistance = (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
		float theta = player.getRotY() + angleAroundPlayer;
		float offsetX = (float) (horizontalDistance * Math.sin(Math.toRadians(theta)));
		float offsetZ = (float) (horizontalDistance * Math.cos(Math.toRadians(theta)));
		yaw = 180 - (player.getRotY() + angleAroundPlayer);
		position.x = player.getPosition().x - offsetX;
		position.y = player.getPosition().y + verticalDistance + HEIGHT_OFFSET;
		position.z = player.getPosition().z - offsetZ;
	}

	public Vector3f getPosition() { return position; }
	public void setPosition(Vector3f position) { this.position = position; }
	public float getPitch() { return pitch; }
	public void setPitch(float pitch) { this.pitch = pitch; }
	public float getYaw() { return yaw; }
	public void setYaw(float yaw) { this.yaw = yaw; }
	public float getRoll() { return roll; }
	public void setRoll(float roll) { this.roll = roll; }
	public void invertPitch() {
		this.pitch = -pitch;
	}
	
}
