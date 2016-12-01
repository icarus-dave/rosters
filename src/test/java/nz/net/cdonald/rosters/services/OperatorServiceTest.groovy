package nz.net.cdonald.rosters.services

import com.avaje.ebean.EbeanServer
import nz.net.cdonald.rosters.domain.Operator
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

import javax.persistence.OptimisticLockException

@RunWith(SpringRunner.class)
@SpringBootTest
class OperatorServiceTest extends Assert {

	@Autowired
	EbeanServer server;

	@Autowired
	OperatorService operatorService

	@After
	public void clear() {
		server.deleteAll(server.find(Operator.class).findList())
	}

	@Test
	public void testList() throws Exception {
		assertNotNull(server)

		def o1 = new Operator()
		o1.firstName = "Foo"
		o1.lastName = "ZBaz"
		o1.email = "foo@baz.com"
		server.save(o1)

		def o2 = new Operator()
		o2.firstName = "Baz"
		o2.lastName = "aFoo"
		o2.email = "foo@foo.com"
		server.save(o2)

		def list = operatorService.getOperators()
		assertEquals(2, list.size())

		assertTrue(list.get(0).firstName == "Baz")
		assertTrue(list.get(1).firstName == "Foo")
	}

	@Test
	public void testRetrieveOperator() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"
		server.save(o1)

		def o = operatorService.getOperator(o1.id)
		assertEquals("abc", o.firstName)
		assertEquals("def", o.lastName)
		assertEquals("foo@baz.com", o.email)
	}

	@Test
	public void testCreateOperator() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"

		def o2 = operatorService.createOperator(o1)
		assert o2.id != 0
		assertEquals(o2.firstName, "abc")
	}

	@Test
	public void testUpdateOperator() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"

		operatorService.createOperator(o1)

		o1.firstName = "zyx"

		def o2 = operatorService.updateOperator(o1)

		assertEquals("zyx", o2.firstName)
		assertEquals(2,o2.version)
	}

	@Test
	public void testUniqueEmail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"

		operatorService.createOperator(o1)

		def o2 = new Operator()
		o2.firstName = "abc"
		o2.lastName = "def"
		o2.email = "foo@baz.com"
		def ex
		try {
			operatorService.createOperator(o2)
		} catch (IllegalArgumentException e) {
			ex = e
		}
		assertNotNull(ex)

		o2.firstName = "abc"
		o2.lastName = "def"
		o2.email = "baz@foo.com"
		operatorService.createOperator(o2)

		o2.email = "foo@baz.com"
		def ex2
		try {
			operatorService.updateOperator(o2)
		} catch (IllegalArgumentException e) {
			ex2 = e
		}
		assertNotNull(ex2)
	}

	@Test
	public void testCreateSpaceyMail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "     foo@baz.com    "
		operatorService.createOperator(o1)
		assertEquals("abc", server.find(Operator.class).where().eq("email", "foo@baz.com").findUnique().firstName)
	}

	@Test
	public void testUpdateSpaceyMail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.org"
		operatorService.createOperator(o1)

		o1.email = "     baz@foo.org    "
		operatorService.updateOperator(o1)

		assertEquals("abc", server.find(Operator.class).where().eq("email", "baz@foo.org").findUnique().firstName)
	}

	@Test
	public void testUpdatedVersionNumber() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "foo@baz.com"

		operatorService.createOperator(o1)

		def o2 = new Operator()
		o2.id = o1.id
		o2.firstName = "def"
		o2.lastName = "abc"
		o2.version = 55
		o2.email = "foo@baz.com"

		def ex
		try {
			operatorService.updateOperator(o2)
		} catch (OptimisticLockException e) {
			ex = e
		}

		assertNotNull(ex)
	}

	@Test
	public void invalidEmail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"
		o1.email = "invalid"

		def ex
		try {
			operatorService.createOperator(o1)
		} catch (IllegalArgumentException e) {
			ex = e
		}

		assertNotNull(ex)

		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		o1.email = "invalid"
		def e2
		try {
			operatorService.updateOperator(o1)
		} catch (IllegalArgumentException e) {
			e2 = e
		}

		assertNotNull(e2)

	}

	@Test
	public void invalidNoEmail() throws Exception {
		def o1 = new Operator()
		o1.firstName = "abc"
		o1.lastName = "def"

		def ex
		try {
			operatorService.createOperator(o1)
		} catch (IllegalArgumentException e) {
			ex = e
		}

		assertNotNull(ex)

		o1.email = "foo@baz.com"
		operatorService.createOperator(o1)

		o1.email = null
		def e2
		try {
			operatorService.updateOperator(o1)
		} catch (IllegalArgumentException e) {
			e2 = e
		}

		assertNotNull(e2)

	}
}


