package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Getting employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure getEmployeeReportingStructure(String id) {
        LOG.debug("Getting reporting structure for employee with id [{}]", id);

        // employee whose reporting structure is required
        Employee rootEmployee = read(id);
        ReportingStructure reportingStructure = new ReportingStructure(rootEmployee);
        int employeeCount = 0;

        // visited employee set to detect cycles in the employee hierarchy tree
        Set<String> visitedEmployees = new HashSet<>();

        // BFS queue
        Queue<Employee> employeeQueue = new LinkedList<>();
        employeeQueue.add(rootEmployee);

        while (!employeeQueue.isEmpty()) {
            Employee currentEmployee = employeeQueue.remove();

            /*
             * It is assumed that each employee reports to exactly one supervisor; if any employee is encountered
             * twice in a Breadth First traversal, it indicates a cycle in the employee tree.
             */
            if (visitedEmployees.contains(currentEmployee.getEmployeeId())) {
                LOG.error("Employee reporting structure contains a cycle");
                throw new RuntimeException("Employee reporting structure contains a cycle");
            } else {
                employeeCount++;
                visitedEmployees.add(currentEmployee.getEmployeeId());

                /*
                 * The currentEmployee object only contains the employee ID, hence retrieving the complete Employee
                 * object here
                 */
                Employee currentEmployeeComplete = read(currentEmployee.getEmployeeId());
                currentEmployee.copyEmployeeMetadata(currentEmployeeComplete);

                List<Employee> directReports = currentEmployeeComplete.getDirectReports();
                if (directReports != null) {
                    currentEmployee.setDirectReports(directReports);
                    employeeQueue.addAll(directReports);
                }
            }
        }

        reportingStructure.setNumberOfReports(employeeCount - 1);

        return reportingStructure;
    }
}
