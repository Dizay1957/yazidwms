package com.yazidwms.customer.service;

import com.yazidwms.audit.service.AuditService;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.customer.dto.CustomerDtos.CustomerRequest;
import com.yazidwms.customer.dto.CustomerDtos.CustomerResponse;
import com.yazidwms.customer.entity.Customer;
import com.yazidwms.customer.entity.CustomerType;
import com.yazidwms.customer.mapper.CustomerMapper;
import com.yazidwms.customer.repository.CustomerRepository;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.exception.NotFoundException;
import com.yazidwms.supplier.entity.BusinessStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AuditService auditService;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper, AuditService auditService) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.auditService = auditService;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Customer email already exists");
        }
        var customer = new Customer();
        apply(request, customer);
        var saved = customerRepository.save(customer);
        auditService.log("CUSTOMER_CREATED", "Customer", saved.getId(), saved.getFullName(), null);
        return customerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> search(String q, Pageable pageable) {
        var page = q == null || q.isBlank()
                ? customerRepository.findByDeletedFalse(pageable)
                : customerRepository.findByDeletedFalseAndFullNameContainingIgnoreCaseOrDeletedFalseAndEmailContainingIgnoreCase(q, q, pageable);
        return PageResponse.from(page.map(customerMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        return customerMapper.toResponse(findActive(id));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        var customer = findActive(id);
        apply(request, customer);
        auditService.log("CUSTOMER_UPDATED", "Customer", id, customer.getFullName(), null);
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public void delete(Long id) {
        var customer = findActive(id);
        customer.setDeleted(true);
        customer.setActive(false);
        customer.setStatus(BusinessStatus.INACTIVE);
        auditService.log("CUSTOMER_DELETED", "Customer", id, customer.getFullName(), null);
    }

    public Customer findActive(Long id) {
        var customer = customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
        if (customer.isDeleted()) {
            throw new NotFoundException("Customer", id);
        }
        return customer;
    }

    private void apply(CustomerRequest request, Customer customer) {
        customer.setCustomerType(request.customerType() == null ? CustomerType.COMPANY : request.customerType());
        customer.setCompanyName(request.companyName());
        customer.setFullName(request.fullName());
        customer.setEmail(request.email().toLowerCase());
        customer.setPhone(request.phone());
        customer.setCountry(request.country());
        customer.setCity(request.city());
        customer.setAddress(request.address());
        customer.setStatus(request.status() == null ? BusinessStatus.ACTIVE : request.status());
        customer.setActive(customer.getStatus() == BusinessStatus.ACTIVE);
    }
}
