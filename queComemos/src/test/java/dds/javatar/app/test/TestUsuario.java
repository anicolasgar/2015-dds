package dds.javatar.app.test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dds.javatar.app.dto.Usuario;

public class TestUsuario {

	MathContext mc;
	
	@Before
	public void initialize() {
		this.mc = new MathContext(MathContext.DECIMAL32.getPrecision(), RoundingMode.HALF_DOWN);
	}
	
	private void assertIMC(Usuario usuario, double expectedValue) {
		BigDecimal expected = new BigDecimal(expectedValue, mc);
		assertEquals(expected.doubleValue(), usuario.getIMC(mc.getPrecision()).doubleValue(), 0.1);
	}
	
	@Test
	public final void testPabloGomez() {
		Usuario usuario = new Usuario(new BigDecimal(1.75), new BigDecimal(65.0));
		this.assertIMC(usuario, 21.2244898);
	}

	@Test
	public void testMelinaMacko(){
		Usuario meli = new Usuario();
		BigDecimal altura = new BigDecimal(1.47);
		BigDecimal peso = new BigDecimal(42);
		meli.setAltura(altura);
		meli.setPeso(peso);
		this.assertIMC(meli,19.43635);


	}
}
