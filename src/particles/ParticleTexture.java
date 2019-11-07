package particles;

public class ParticleTexture {
	
	private int textureID;
	private int numberOfRows;
	private boolean useAdditiveBlending = false;
	
	public ParticleTexture(int texID, int numRows, boolean additive) {
		this.textureID = texID;
		this.numberOfRows = numRows;
		this.useAdditiveBlending = additive;
	}
	
	public ParticleTexture(int texID, int numRows) {
		this.textureID = texID;
		this.numberOfRows = numRows;
	}
	
	public int getTextureID() { return textureID; }
	public int getNumRows() { return numberOfRows; }
	public boolean isAdditiveBlending() { return useAdditiveBlending; }
	
}
