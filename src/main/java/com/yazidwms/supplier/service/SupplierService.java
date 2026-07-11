package com.yazidwms.supplier.service;

import com.yazidwms.audit.service.AuditService;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.exception.NotFoundException;
import com.yazidwms.supplier.dto.SupplierDtos.SupplierRequest;
import com.yazidwms.supplier.dto.SupplierDtos.SupplierResponse;
import com.yazidwms.supplier.entity.BusinessStatus;
import com.yazidwms.supplier.entity.Supplier;
import com.yazidwms.supplier.mapper.SupplierMapper;
import com.yazidwms.supplier.repository.SupplierRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final AuditService auditService;

    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper, AuditService auditService) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.auditService = auditService;
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (supplierRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Supplier email already exists");
        }
        var supplier = new Supplier();
        apply(request, supplier);
        var saved = supplierRepository.save(supplier);
        auditService.log("SUPPLIER_CREATED", "Supplier", saved.getId(), saved.getCompanyName(), null);
        return supplierMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> search(String q, Pageable pageable) {
        var page = q == null || q.isBlank()
                ? supplierRepository.findByDeletedFalse(pageable)
                : supplierRepository.findByDeletedFalseAndCompanyNameContainingIgnoreCaseOrDeletedFalseAndEmailContainingIgnoreCase(q, q, pageable);
        return PageResponse.from(page.map(supplierMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(Long id) {
        return supplierMapper.toResponse(findActive(id));
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        var supplier = findActive(id);
        apply(request, supplier);
        auditService.log("SUPPLIER_UPDATED", "Supplier", id, supplier.getCompanyName(), null);
        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public void delete(Long id) {
        var supplier = findActive(id);
        supplier.setDeleted(true);
        supplier.setActive(false);
        supplier.setStatus(BusinessStatus.INACTIVE);
        auditService.log("SUPPLIER_DELETED", "Supplier", id, supplier.getCompanyName(), null);
    }

    public Supplier findActive(Long id) {
        var supplier = supplierRepository.findById(id).orElseThrow(() -> new NotFoundException("Supplier", id));
        if (supplier.isDeleted()) {
            throw new NotFoundException("Supplier", id);
        }
        return supplier;
    }

    private void apply(SupplierRequest request, Supplier supplier) {
        supplier.setCompanyName(request.companyName());
        supplier.setContactName(request.contactName());
        supplier.setEmail(request.email().toLowerCase());
        supplier.setPhone(request.phone());
        supplier.setTaxNumber(request.taxNumber());
        supplier.setCountry(request.country());
        supplier.setCity(request.city());
        supplier.setAddress(request.address());
        supplier.setStatus(request.status() == null ? BusinessStatus.ACTIVE : request.status());
        supplier.setActive(supplier.getStatus() == BusinessStatus.ACTIVE);
    }
}
