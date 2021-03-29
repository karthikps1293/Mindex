package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationUrl;
    private String compensationIdUrl;

    @Autowired
    private CompensationService compensationService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationIdUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    public void testCreateRead() {
        Compensation testCompensation = new Compensation();
        // create compensation using valid employeeId
        testCompensation.setEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");
        testCompensation.setSalary(80000);
        testCompensation.setEffectiveDate("08/25/2018");

        // Create checks
        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getBody();
        assertNotNull(createdCompensation);
        assertNotNull(createdCompensation.getEmployeeId());
        assertCompensationEquivalence(testCompensation, createdCompensation);


        // Read checks
        Compensation readCompensation = restTemplate.getForEntity(compensationIdUrl, Compensation.class,
                createdCompensation.getEmployeeId()).getBody();

        assertNotNull(readCompensation);
        assertCompensationEquivalence(createdCompensation, readCompensation);

    }

    @Test
    public void testCreateInvalidEmployeeId(){
        Compensation testCompensation = new Compensation();
        // create compensation using invalid employeeId
        testCompensation.setEmployeeId(UUID.randomUUID().toString());
        testCompensation.setSalary(70000);
        testCompensation.setEffectiveDate("08/25/2018");

        // confirm that compensation creation fails as expected
        ResponseEntity<Compensation> responseEntity = restTemplate.postForEntity(compensationUrl, testCompensation,
                Compensation.class);
        assertEquals(responseEntity.getStatusCodeValue(), 500);
    }

    @Test
    public void testCreateNullEmployeeId(){
        Compensation testCompensation = new Compensation();
        testCompensation.setSalary(70000);
        testCompensation.setEffectiveDate("08/25/2018");

        // confirm that compensation creation fails as expected
        ResponseEntity<Compensation> responseEntity = restTemplate.postForEntity(compensationUrl, testCompensation,
                Compensation.class);
        assertEquals(responseEntity.getStatusCodeValue(), 500);
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployeeId(), actual.getEmployeeId());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
        assertEquals(expected.getSalary(), actual.getSalary());
    }
}
