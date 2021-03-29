package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        String employeeId = compensation.getEmployeeId();
        if (StringUtils.isEmpty(employeeId)){
            LOG.error("Compensation employeeId is null or empty" );
            throw new RuntimeException("Compensation creation failed");
        }

        // check if employee with the same employeeId exists in employeeRepository
        Employee employee = employeeRepository.findByEmployeeId(employeeId);
        if (employee == null){
            LOG.error("Employee with employeeId [{}] does not exist", employeeId);
            throw new RuntimeException("Compensation creation failed");
        }

        compensationRepository.insert(compensation);

        return compensation;
    }

    @Override
    public Compensation read(String id) {
        LOG.debug("Reading compensation for employee with id [{}]", id);

        Compensation compensation = compensationRepository.findByEmployeeId(id);

        if (compensation == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return compensation;
    }

}
