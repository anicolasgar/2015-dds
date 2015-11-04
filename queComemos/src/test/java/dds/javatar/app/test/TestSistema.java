package dds.javatar.app.test;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import dds.javatar.app.domain.grupodeusuarios.GrupoDeUsuarios;
import dds.javatar.app.domain.receta.Receta;
import dds.javatar.app.domain.receta.builder.RecetaBuilder;
import dds.javatar.app.domain.receta.busqueda.Buscador;
import dds.javatar.app.domain.sistema.Administrador;
import dds.javatar.app.domain.sistema.RepositorioRecetas;
import dds.javatar.app.domain.usuario.Usuario;
import dds.javatar.app.domain.usuario.condiciones.Hipertenso;
import dds.javatar.app.util.exception.BusinessException;
import dds.javatar.app.util.exception.RecetaException;

public class TestSistema {

	private Usuario usuario;
	private RepositorioRecetas sistema = RepositorioRecetas.getInstance();
	private Administrador administrador = Administrador.getInstance();
	private Buscador buscador;

	@Before
	public void initialize() {
		this.usuario = TestFactory.crearUsuarioBasicoValido();
		this.buscador = new Buscador();
	}

	public Receta crearRecetaPrivadaSimple(Usuario autor) {
		return new RecetaBuilder("Ravioles")
			.totalCalorias(350)
			.agregarIngrediente("Harina", new BigDecimal(300))
			.agregarIngrediente("Agua", new BigDecimal(70))
			.agregarIngrediente("Verdura", new BigDecimal(100))
			.inventadaPor(autor.getNombre())
			.buildReceta();
	}

	private Receta crearRecetaPublicaSimpleRica(Usuario autor) {
		return new RecetaBuilder("Ñoquis")
			.totalCalorias(new Integer(400))
			.agregarIngrediente("Harina", new BigDecimal(300))
			.agregarIngrediente("Agua", new BigDecimal(70))
			.agregarIngrediente("Papa", new BigDecimal(100))
			.inventadaPor(autor.getNombre())
			.buildReceta();
		
	}

	private Receta crearRecetaNoAptaParaHipertensos() {
		return new RecetaBuilder("Pizza")
			.totalCalorias(new Integer(470))
			.agregarIngrediente("sal", new BigDecimal(300))
			.agregarIngrediente("agua", new BigDecimal(70))
			.agregarIngrediente("harina", new BigDecimal(100))
			.buildReceta();
	}

	private GrupoDeUsuarios crearGrupoDeUsuarios() throws BusinessException {
		GrupoDeUsuarios grupo = new GrupoDeUsuarios();
		grupo.setNombre("Amigos del club");
		HashMap<String, Boolean> preferenciasAlimenticias = new HashMap<String, Boolean>();
		preferenciasAlimenticias.put("Ravioles", true);
		preferenciasAlimenticias.put("papa", true);

		grupo.setPreferenciasAlimenticias(preferenciasAlimenticias);

		return grupo;

	}

	@Test
	public void unaRecetaQueLeGustaPuedeSugerirseAUnUsuario() throws BusinessException {
		this.usuario.agregarAlimentoQueLeDisgusta("pollo");
		this.administrador.sugerir(this.crearRecetaPublicaSimpleRica(this.usuario), TestFactory.crearUsuarioBasicoValido());

	}

	@Test(expected = BusinessException.class)
	public void unaRecetaQueNoLeGustaNoPuedeSugerirseAUnUsuario() throws BusinessException {
		this.usuario = TestFactory.crearUsuarioBasicoValido();
		this.usuario.agregarAlimentoQueLeDisgusta("Harina");
		this.administrador.sugerir(this.crearRecetaPublicaSimpleRica(this.usuario), this.usuario);

	}

	@Test(expected = BusinessException.class)
	public void unaRecetaQueNoSeaAptaParaElPerfilDelUsuarioNoSePuedeSugerir() throws BusinessException {
		Hipertenso hipertenso = new Hipertenso();
		this.usuario = TestFactory.crearUsuarioBasicoValido();
		this.usuario.agregarCondicionPreexistente(hipertenso);
		this.administrador.sugerir(this.crearRecetaNoAptaParaHipertensos(), this.usuario);

	}

	@Test
	public void recetasQueConocePorCompartirGrupo() throws RecetaException, BusinessException {
		GrupoDeUsuarios grupo = this.crearGrupoDeUsuarios();
		Usuario usuario = TestFactory.crearUsuarioBasicoValido();
		grupo.agregarUsuario(usuario);
		usuario.agregarReceta(this.crearRecetaPublicaSimpleRica(usuario));
		Usuario usuarioQueSeAgrega = TestFactory.crearUsuarioBasicoValido();
		usuarioQueSeAgrega.agregarReceta(this.crearRecetaPrivadaSimple(usuarioQueSeAgrega));
		grupo.agregarUsuario(usuarioQueSeAgrega);

		assertEquals(2, usuario.getRecetas().size() + usuarioQueSeAgrega.getRecetas().size());
	}

	@Test
	public void recetaQueNoTieneQueConocerPOrqueNoCompartenGrupo() throws BusinessException, RecetaException {
		GrupoDeUsuarios grupo = this.crearGrupoDeUsuarios();
		Usuario usuario = TestFactory.crearUsuarioBasicoValido();
		grupo.agregarUsuario(usuario);

		usuario.agregarReceta(this.crearRecetaPublicaSimpleRica(usuario));
		Usuario usuarioQueSeAgrega = TestFactory.crearUsuarioBasicoValido();
		usuarioQueSeAgrega.agregarReceta(this.crearRecetaPrivadaSimple(usuarioQueSeAgrega));

		assertEquals(3, this.buscador.recetasQueConoceEl(usuario).size());
	}

	@Test
	public void recetasQueConoce() {
		Usuario usuario = TestFactory.crearUsuarioBasicoValido();
		this.crearRecetaPublicaSimpleRica(usuario);
		this.crearRecetaPublicaSimpleRica(usuario);
		assertEquals(2, this.buscador.recetasQueConoceEl(usuario).size());
	}

	@Test
	public void laRecetaContienePalabraClaveDePreferenciaDelGrupoYEsAptaParaTodosLosIntegrantes() throws BusinessException, RecetaException {
		GrupoDeUsuarios grupo = this.crearGrupoDeUsuarios();
		Usuario usuario = TestFactory.crearUsuarioBasicoValido();
		grupo.agregarUsuario(usuario);
		Usuario usuarioQueSeAgrega = TestFactory.crearUsuarioBasicoValido();
		usuarioQueSeAgrega.agregarReceta(this.crearRecetaPrivadaSimple(usuarioQueSeAgrega));

	}

	@Test(expected = BusinessException.class)
	public void laRecetaNOContienePalabraClaveDePreferenciaDelGrupoYEsAptaParaTodosLosIntegrantes() throws BusinessException, RecetaException {
		GrupoDeUsuarios grupo = this.crearGrupoDeUsuarios();
		Usuario usuario = TestFactory.crearUsuarioBasicoValido();
		grupo.agregarUsuario(usuario);
		Usuario usuarioQueSeAgrega = TestFactory.crearUsuarioBasicoValido();
		usuarioQueSeAgrega.agregarReceta(this.crearRecetaPrivadaSimple(usuarioQueSeAgrega));
		this.administrador.sugerir(this.crearRecetaPrivadaSimple(usuario), grupo);
	}
}
