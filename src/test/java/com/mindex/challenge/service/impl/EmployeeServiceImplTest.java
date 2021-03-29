package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;

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

        // Add a new employee who reports to Paul McCartney, and has the root employee (John Lennon) as a direct report,
        // then query reporting structure on the root employee
        Employee leafEmployee = new Employee();
        leafEmployee.setFirstName("David");
        leafEmployee.setLastName("Beckham");
        leafEmployee.setDepartment("Engineering");
        leafEmployee.setPosition("Intern");
        // make the root employee (John Lennon) a direct report of the leaf employee
        leafEmployee.setDirectReports(new ArrayList<>(Collections.singletonList(employeeRepository.findByEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f"))));
        employeeRepository.save(leafEmployee);

        // Make the test employee a direct report of Paul McCartney
        Employee parentOfLeafEmployee = employeeRepository.findByEmployeeId("b7839309-3348-463b-a7e3-5de1c168beb3");
        parentOfLeafEmployee.setDirectReports(new ArrayList<>(Collections.singletonList(leafEmployee)));
        employeeRepository.save(parentOfLeafEmployee);

        // call getReportingStructure and ensure that an error is thrown indicating the presence of a cycle in the tree
        restTemplate.getForEntity(employeeIdReportingUrl, ReportingStructure.class, "16a596ae-edd3-4847-99fe-c4518e82c86f");
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
