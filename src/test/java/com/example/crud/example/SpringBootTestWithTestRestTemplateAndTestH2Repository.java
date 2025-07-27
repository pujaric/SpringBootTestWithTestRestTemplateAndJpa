package com.example.crud.example;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.example.crud.example.entity.Product;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
//@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application-test.yml")
class SpringBootTestWithTestRestTemplateAndTestH2Repository {

	@LocalServerPort
	private int port;

	// This can be optional; refer testGetProducts()
	private String baseUrl = "http://localhost";

	@Autowired
    private TestRestTemplate restTemplate;

	@Autowired
	private TestH2Repository h2Repository;

	@BeforeEach
	public void setUp() {
		baseUrl = baseUrl.concat(":").concat(port + "").concat("/products");
	}

	@Test
	public void testAddProduct() {
		Product product = new Product("headset", 2, 7999);
		Product response = restTemplate.postForObject(baseUrl, product, Product.class);
		assertEquals("headset", response.getName());
		assertEquals(2, h2Repository.findAll().size());
	}

	@Test
	@Sql(statements = "INSERT INTO PRODUCT_TBL (id,name, quantity, price) VALUES (4,'AC', 1, 34000)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(statements = "DELETE FROM PRODUCT_TBL WHERE name='AC'", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	public void testGetProducts() {
//		List<Product> products = restTemplate.getForObject(baseUrl, List.class);
		List<Product> products = restTemplate.getForObject("/products", List.class);
		assertEquals(2, products.size());
		assertEquals(2, h2Repository.findAll().size());
	}

	@Test
	@Sql(statements = "INSERT INTO PRODUCT_TBL (id,name, quantity, price) VALUES (1,'CAR', 1, 334000)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(statements = "DELETE FROM PRODUCT_TBL WHERE id=1", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	public void testFindProductById() {
		Product product = restTemplate.getForObject(baseUrl + "/{id}", Product.class, 1);
		assertAll(() -> assertNotNull(product), () -> assertEquals(1, product.getId()),
				() -> assertEquals("CAR", product.getName()));

	}

	@Test
	@Sql(statements = "INSERT INTO PRODUCT_TBL (id,name, quantity, price) VALUES (2,'shoes', 1, 999)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(statements = "DELETE FROM PRODUCT_TBL WHERE id=1", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	public void testUpdateProduct() {
		Product product = new Product("shoes", 1, 1999);
		restTemplate.put(baseUrl + "/update/{id}", product, 2);
		Product productFromDB = h2Repository.findById(2).get();
		assertAll(() -> assertNotNull(productFromDB), () -> assertEquals(1999, productFromDB.getPrice()));

	}

	@Test
	@Sql(statements = "INSERT INTO PRODUCT_TBL (id,name, quantity, price) VALUES (8,'books', 5, 1499)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	public void testDeleteProduct() {
		int recordCount = h2Repository.findAll().size();
		assertEquals(3, recordCount);
		restTemplate.delete(baseUrl + "/delete/{id}", 8);
		assertEquals(2, h2Repository.findAll().size());

	}

}