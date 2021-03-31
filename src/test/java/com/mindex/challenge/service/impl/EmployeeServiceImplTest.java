package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String employeeIdReportingUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        employeeIdReportingUrl = "http://localhost:" + port + "/employee/{id}/reporting-structure";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testGetReportingStructureForRootNode() {

        // getReportingStructure check for the root node in the static DB (John Lennon)
        ReportingStructure reportingStructure = restTemplate.getForEntity(employeeIdReportingUrl, ReportingStructure.class,
                "16a596ae-edd3-4847-99fe-c4518e82c86f").getBody();

        assertNotNull(reportingStructure);
        assertEquals(reportingStructure.getNumberOfReports(), 4);

    }

    @Test
    public void testGetReportingStructureForMiddleNode() {

        // getReportingStructure check for a middle node in the static DB (Ringo Starr)
        ReportingStructure reportingStructure = restTemplate.getForEntity(employeeIdReportingUrl, ReportingStructure.class,
                "03aa1462-ffa9-4978-901b-7c001562cf6f").getBody();

        assertNotNull(reportingStructure);
        assertEquals(reportingStructure.getNumberOfReports(), 2);

    }

    @Test
    public void testGetReportingStructureForLeafNode() {

        // getReportingStructure check for a leaf node in the static DB (Paul McCartney)
        ReportingStructure reportingStructure = restTemplate.getForEntity(employeeIdReportingUrl, ReportingStructure.class,
                "b7839309-3348-463b-a7e3-5de1c168beb3").getBody();

        assertNotNull(reportingStructure);
        assertEquals(reportingStructure.getNumberOfReports(), 0);

    }

    @Test
    public void testGetReportingStructureForCycleInEmployeeTree() {

        String parentEmployeeId = UUID.randomUUID().toString();
        String childEmployeeId = UUID.randomUUID().toString();

        Employee parentEmployee = new Employee();
        parentEmployee.setEmployeeId(parentEmployeeId);
        parentEmployee.setFirstName("John");
        parentEmployee.setLastName("Doe");
        parentEmployee.setDepartment("Engineering");
        parentEmployee.setPosition("Manager");
        employeeRepository.save(parentEmployee);

        Employee childEmployee = new Employee();
        childEmployee.setEmployeeId(childEmployeeId);
        childEmployee.setFirstName("David");
        childEmployee.setLastName("Beckham");
        childEmployee.setDepartment("Engineering");
        childEmployee.setPosition("Intern");
        // make the parentEmployee a direct report of childEmployee
        childEmployee.setDirectReports(new ArrayList<>(Collections.singletonList(employeeRepository.findByEmployeeId(parentEmployeeId))));
        employeeRepository.save(childEmployee);

        // make childEmployee a directReport of parentEmployee
        parentEmployee = employeeRepository.findByEmployeeId(parentEmployeeId);
        parentEmployee.setDirectReports(new ArrayList<>(Collections.singletonList(employeeRepository.findByEmployeeId(childEmployeeId))));
        employeeRepository.save(parentEmployee);

        // call getReportingStructure and ensure that an error is thrown indicating the presence of a cycle in the tree
        ResponseEntity<ReportingStructure> responseEntity = restTemplate.getForEntity(employeeIdReportingUrl,
                ReportingStructure.class, parentEmployeeId );
        assertEquals(responseEntity.getStatusCodeValue(), 500);
    }

    @Test
    public void testReportingStructureForNonExistentEmployee(){

        String invalidEmployeeId = UUID.randomUUID().toString();

        // call getReportingStructure and ensure that an error is thrown (since the employee does not exist)
        ResponseEntity<ReportingStructure> responseEntity = restTemplate.getForEntity(employeeIdReportingUrl,
                ReportingStructure.class, invalidEmployeeId );
        assertEquals(responseEntity.getStatusCodeValue(), 500);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
