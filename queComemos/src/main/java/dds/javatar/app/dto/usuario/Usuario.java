package dds.javatar.app.dto.usuario;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dds.javatar.app.dto.receta.Receta;
import dds.javatar.app.util.BusinessException;

public class Usuario {

	public enum Sexo {
		MASCULINO, FEMENINO
	};

	private static final Integer MIN_NAME_LENGTH = 4;

	private String nombre;
	private Sexo sexo;
	private Date fechaNacimiento;
	private BigDecimal altura;
	private BigDecimal peso;

	private Set<CondicionPreexistente> condicionesPreexistentes;
	private Map<String, Boolean> preferenciasAlimenticias;
	private Rutina rutina;
	private Set<Receta> recetas;

	
	
	/****	 Constructors 	****/

	public Usuario() {
		this.condicionesPreexistentes = new HashSet<CondicionPreexistente>();
		this.preferenciasAlimenticias = new HashMap<String, Boolean>();
		this.recetas = new HashSet<Receta>();
	}

	public Usuario(BigDecimal altura, BigDecimal peso) {
		this();
		this.altura = altura;
		this.peso = peso;
	}

	public Usuario(BigDecimal altura, BigDecimal peso, Sexo sexo) {
		this(altura, peso);
		this.sexo = sexo;
	}

	
	
	/****	 Setters y getters 		****/
	public BigDecimal getAltura() {
		return this.altura;
	}

	public void setAltura(BigDecimal altura) {
		this.altura = altura;
	}

	public BigDecimal getPeso() {
		return this.peso;
	}

	public void setPeso(BigDecimal peso) {
		this.peso = peso;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Sexo getSexo() {
		return this.sexo;
	}

	public void setSexo(Sexo sexo) {
		this.sexo = sexo;
	}

	public Date getFechaNacimiento() {
		return this.fechaNacimiento;
	}

	public void setFechaNacimiento(Date fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public Rutina getRutina() {
		return this.rutina;
	}

	public void setRutina(Rutina rutina) {
		this.rutina = rutina;
	}

	
	
	
	/****		Metodos		****/
	
	/* Obtener la masa corporal dada una presicion */
	public BigDecimal getIMC(int precision) {

		MathContext mc = new MathContext(precision, RoundingMode.HALF_DOWN);
		BigDecimal cuadrado = this.altura.pow(2, mc);
		return this.peso.divide(cuadrado, mc);
	}

	// TODO: deberiamos crear una especie de validadores ? porque queda medio feo chequear todos los campos asi, nose..
	public void validar() throws BusinessException {
		if (this.nombre == null || this.fechaNacimiento == null || this.peso == null || this.altura == null || this.rutina == null) {
			throw new BusinessException("El usuario tiene campos obligatorios sin completar");
		}

		if (this.nombre.length() <= MIN_NAME_LENGTH) {
			throw new BusinessException("El nombre del usuario es demasido corto");
		}

		Date today = new Date();
		if (today.compareTo(this.fechaNacimiento) <= 0) {
			throw new BusinessException("La fecha de nacimiento del usuario no puede posterior al dia de hoy.");
		}

		for (CondicionPreexistente condicionPreexistente : this.condicionesPreexistentes) {
			condicionPreexistente.validarUsuario(this);
		}
	}

	public Boolean sigueRutinaSaludable() {

		int userIMC = this.getIMC(MathContext.DECIMAL32.getPrecision()).intValue();

		if (userIMC < 18 || userIMC > 30) {
			return Boolean.FALSE;
		}

		for (CondicionPreexistente condicionPreexistente : this.condicionesPreexistentes) {
			if (!condicionPreexistente.usuarioSigueRutinaSaludable(this)) {
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}

	public Boolean tienePreferenciaAlimenticia(String alimento) {
		return Boolean.TRUE.equals(this.preferenciasAlimenticias.get(alimento));
	}

	public Boolean tieneAlgunaPreferencia() {
		return (this.preferenciasAlimenticias.values().contains(Boolean.TRUE));
	}

	public void agregarPreferenciaAlimenticia(String alimento) {
		this.preferenciasAlimenticias.put(alimento, Boolean.TRUE);
	}

	public void agregarCondicionPreexistente(CondicionPreexistente condicion) {
		this.condicionesPreexistentes.add(condicion);
	}

	public void agregarReceta(Receta receta) throws BusinessException {
		receta.validar();
		receta.setAutor(this);
		this.recetas.add(receta);
	}

	public void quitarReceta(Receta receta) throws BusinessException {
		if (this.recetas.contains(receta)) {
			this.recetas.remove(receta);
		}
		else {
			throw new BusinessException("El Usuario no tenia agregada esa receta");
		}

	}

	public void validarSiAceptaReceta(Receta receta) throws BusinessException {
		for (CondicionPreexistente condicionPreexistente : this.condicionesPreexistentes) {
			condicionPreexistente.validarReceta(receta);
		}
	}

	public void verReceta(Receta receta) throws BusinessException {
		if (!this.recetas.contains(receta) && receta.getAutor() != null) {
			throw new BusinessException("El Usuario no tiene permitido ver esta receta");
		}
	}

	public void puedeModificarReceta(Receta receta) throws BusinessException {
		this.verReceta(receta);
	}

	public void puedeAgregarSubRecetas(Set<Receta> subRecetas) throws BusinessException {
		for (Receta subReceta : subRecetas) {
			try {
				this.verReceta(subReceta);
			} catch (Exception e) {
				throw new BusinessException("El Usuario no tiene permitido agregar alguna subreceta");
			}
		}
	}

	public void modificarReceta(Receta receta, Object ...modificaciones ) throws BusinessException, CloneNotSupportedException {
		// Si le receta es publica, tenemos que clonar el objeto, para que los cmabios sean solo visibles para este usuario
		if(receta.getAutor()==null){				
				Receta modificada = (Receta) receta.clone();
				if (this.recetas.contains(receta)) {
					this.quitarReceta(receta);
				}
				receta=modificada;
				this.agregarReceta(receta);			
					
		}			
		
		/*	Aplico los cambios utilizando:
		 * [Nombre,
		 * 	listaIngredientes,
		 * 	listaCondimentos,
		 * 	preparacion,
		 * 	calorias,
		 * 	dificultad,
		 * 	temporada	]
		 * 
		 */
		receta.actualizarDatos(modificaciones);	

	}

}