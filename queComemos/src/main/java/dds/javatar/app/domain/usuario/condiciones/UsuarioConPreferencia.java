package dds.javatar.app.domain.usuario.condiciones;

import dds.javatar.app.domain.usuario.Usuario;
import dds.javatar.app.util.exception.UsuarioException;

public abstract class UsuarioConPreferencia implements CondicionPreexistente {

	@Override
	public void validarUsuario(Usuario usuario) throws UsuarioException {
		if (!usuario.tieneAlgunaPreferencia()) {
			throw new UsuarioException("El usuario debe tener como minimo una preferencia");
		}
	}

}