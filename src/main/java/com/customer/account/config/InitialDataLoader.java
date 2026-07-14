package com.customer.account.config;

import com.customer.account.entity.CustomerDetail;
import com.customer.account.repository.CustomerDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements ApplicationRunner {

    @Autowired
    private CustomerDetailRepository customerDetailRepository;

    @Autowired
    public InitialDataLoader(CustomerDetailRepository customerDetailRepository) {
        this.customerDetailRepository = customerDetailRepository;
    }

    public void run(ApplicationArguments args) {
        customerDetailRepository.save(new CustomerDetail(1L, "Ahitagni", "Saha"));
        customerDetailRepository.save(new CustomerDetail(2L, "ABC", "XYZ"));
    }
}
