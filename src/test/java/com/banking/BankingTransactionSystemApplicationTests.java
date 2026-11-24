package com.banking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"
})
class BankingTransactionSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
