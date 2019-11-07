package textures;

public class ModelTexture {
	
	private int textureID;
	private int normalMap;
	
	private float shineDamper = 1;
	private float reflectivity = 0;
	
	private Boolean hasTransparency = false;
	private Boolean hasFakeLighting = false;
	
	private int numberOfRows = 1;
	
	public ModelTexture(int id) {
		this.textureID = id;
	}
	
	public int getID() { return this.textureID; }
	public int getNormalID() { return this.normalMap; }
	public void setNormalMap(int ID) { this.normalMap = ID; }
	public float getShineDamper() { return shineDamper; }
	public void setShineDamper(float shineDamper) { this.shineDamper = shineDamper; }
	public float getReflectivity() { return reflectivity; }
	public void setReflectivity(float reflectivity) { this.reflectivity = reflectivity; }
	public Boolean isTransparent() { return hasTransparency; }
	public void setTransparency(Boolean transparent) { this.hasTransparency = transparent; }
	public Boolean isFakeLighting() { return hasFakeLighting; }
	public void setFakeLighting(Boolean lighting) { this.hasFakeLighting = lighting; }
	public int getNumberOfRows() { return numberOfRows; }
	public void setNumberOfRows(int rows) { this.numberOfRows = rows; }
}
