package Entity;

public class webCamInfo
{
	@Override
	public String toString() {
		return nombre;
	}
	private int indice;
	private String nombre;
	
	
	public webCamInfo()
	{
		
	}
	public int getIndice() {
		return indice;
	}
	public void setIndice(int indice) {
		this.indice = indice;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	

}
