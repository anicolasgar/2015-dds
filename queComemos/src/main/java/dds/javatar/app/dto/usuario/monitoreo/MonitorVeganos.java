package dds.javatar.app.dto.usuario.monitoreo;

import queComemos.entrega3.dominio.Dificultad;
import dds.javatar.app.dto.usuario.Usuario;

public class MonitorVeganos implements ConsultaObserver {

	private Integer count;

	public MonitorVeganos() {
		this.count = 0;
	}

	@Override
	public void notificarConsulta(Usuario usuario, String nombre, Dificultad dificultad) {
		if (usuario.esVegano() && Dificultad.DIFICIL.equals(dificultad)) {
			this.count++;
		}
	}

	public Integer getCantidad() {
		return this.count;
	}

}