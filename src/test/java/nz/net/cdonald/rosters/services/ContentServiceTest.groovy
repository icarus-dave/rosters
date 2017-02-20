package nz.net.cdonald.rosters.services

import org.junit.Assert
import org.junit.Test

class ContentServiceTest extends Assert {

	@Test
	public void testRetrieveEmailEntity() {
		def service = new ContentService("28flpi5v5vs0","0fc02b8cf38c2abee506808e7b964d700480a0748c11095c857bab1d8e614e71")

		def content  = service.getEntityForName("email","invite")
		assertEquals("invite",content["name"])
		assertNotNull(content["htmlContent"])
	}

	@Test
	public void testMissingEmailEntity() {
		def service = new ContentService("28flpi5v5vs0","0fc02b8cf38c2abee506808e7b964d700480a0748c11095c857bab1d8e614e71")

		Exception e = null

		try {
			def content  = service.getEntityForName("email","invite1")
		} catch (Exception ex) {
			e = ex
		}

		assertNotNull(e)
	}

	@Test
	public void testWrongSpace() {
		def service = new ContentService("abc","0fc02b8cf38c2abee506808e7b964d700480a0748c11095c857bab1d8e614e71")

		Exception e = null

		try {
			def content  = service.getEntityForName("email","invite")
		} catch (Exception ex) {
			e = ex
		}

		assertNotNull(e)
		assertTrue(e.message.contains("404"))
	}

	@Test
	public void testWrongCredentials() {
		def service = new ContentService("28flpi5v5vs0","abc")

		Exception e = null

		try {
			def content  = service.getEntityForName("email","invite")
		} catch (Exception ex) {
			e = ex
		}

		assertNotNull(e)
		assertTrue(e.message.contains("401"))
	}

}
