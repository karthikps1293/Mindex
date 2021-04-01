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
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationIdUrl;

    @Autowired
    private CompensationService compensationService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Compensation createdCompensation =
                restTemplate.exchange(compensationIdUrl,
                        HttpMethod.POST,
                        new HttpEntity<Compensation>(testCompensation, headers),
                        Compensation.class,
                        testCompensation.getEmployeeId()).getBody();

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
    public void testCreateInvalidEmployeeId() {
        Compensation testCompensation = new Compensation();
        // create compensation using invalid employeeId
        String invalidEmployeeId = "00000000-0000-0000-0000-000000000000";
        testCompensation.setEmployeeId(invalidEmployeeId);
        testCompensation.setSalary(70000);
        testCompensation.setEffectiveDate("08/25/2018");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Compensation> responseEntity =
                restTemplate.exchange(compensationIdUrl,
                        HttpMethod.POST,
                        new HttpEntity<Compensation>(testCompensation, headers),
                        Compensation.class,
                        testCompensation.getEmployeeId());

        // confirm that compensation creation fails as expected
        assertEquals(responseEntity.getStatusCodeValue(), 500);
    }

    @Test
    public void testReadInvalidEmployeeId() {

        String invalidEmployeeId = "00000000-0000-0000-0000-000000000000";

        ResponseEntity<Compensation> responseEntity = restTemplate.getForEntity(compensationIdUrl, Compensation.class,
                invalidEmployeeId);

        // confirm that compensation creation fails as expected
        assertEquals(responseEntity.getStatusCodeValue(), 500);

    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployeeId(), actual.getEmployeeId());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
        assertEquals(expected.getSalary(), actual.getSalary());
    }
}
