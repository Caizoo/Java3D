package entities;

import org.lwjgl.util.vector.Vector3f;

public class Light {

	private Vector3f position;
	private Vector3f colour;
	private Vector3f attenuation = new Vector3f(1,0,0);
	
	public Light(Vector3f position, Vector3f colour) {
		this.position = position;
		this.colour = colour;
	}
	
	public Light(Vector3f position, Vector3f colour, Vector3f attenuation) {
		this.position = position;
		this.colour = colour;
		this.attenuation = attenuation;
	}
	
	public void moveSun(float time) {
		// set position
		float t = (float)(time / 24000);
		t = t*360 + 270;
		int radius = 100000;
		float x = (float) (radius*Math.cos(Math.toRadians(t)));
		float y = (float) (radius*Math.sin(Math.toRadians(t)));
		this.setPosition(new Vector3f(x,y,0));
	}
	
	public Vector3f getPosition() { return position; }
	public Vector3f getColour() { return colour; }
	public Vector3f getAttenuation() { return attenuation; }
	public void setPosition(Vector3f position) { this.position = position; }
	public void setColour(Vector3f colour) { this.colour = colour; }
}
