package id.petrikshop.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Full application context — uses src/test/resources/application.properties (H2)
@SpringBootTest
class EshopApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring application context starts without errors.
        // A failure here indicates a misconfigured bean, missing dependency,
        // or broken property source — NOT a business-logic defect.
    }
}
