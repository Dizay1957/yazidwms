package com.yazidwms.warehouse.service;

import com.yazidwms.common.api.PageResponse;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.exception.NotFoundException;
import com.yazidwms.warehouse.dto.WarehouseDtos.AisleRequest;
import com.yazidwms.warehouse.dto.WarehouseDtos.BinRequest;
import com.yazidwms.warehouse.dto.WarehouseDtos.BinResponse;
import com.yazidwms.warehouse.dto.WarehouseDtos.LocationResponse;
import com.yazidwms.warehouse.dto.WarehouseDtos.ShelfRequest;
import com.yazidwms.warehouse.dto.WarehouseDtos.WarehouseRequest;
import com.yazidwms.warehouse.dto.WarehouseDtos.WarehouseResponse;
import com.yazidwms.warehouse.dto.WarehouseDtos.ZoneRequest;
import com.yazidwms.warehouse.entity.Warehouse;
import com.yazidwms.warehouse.entity.WarehouseAisle;
import com.yazidwms.warehouse.entity.WarehouseBin;
import com.yazidwms.warehouse.entity.WarehouseShelf;
import com.yazidwms.warehouse.entity.WarehouseZone;
import com.yazidwms.warehouse.mapper.WarehouseMapper;
import com.yazidwms.warehouse.repository.WarehouseAisleRepository;
import com.yazidwms.warehouse.repository.WarehouseBinRepository;
import com.yazidwms.warehouse.repository.WarehouseRepository;
import com.yazidwms.warehouse.repository.WarehouseShelfRepository;
import com.yazidwms.warehouse.repository.WarehouseZoneRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseZoneRepository zoneRepository;
    private final WarehouseAisleRepository aisleRepository;
    private final WarehouseShelfRepository shelfRepository;
    private final WarehouseBinRepository binRepository;
    private final WarehouseMapper warehouseMapper;

    public WarehouseService(WarehouseRepository warehouseRepository, WarehouseZoneRepository zoneRepository,
                            WarehouseAisleRepository aisleRepository, WarehouseShelfRepository shelfRepository,
                            WarehouseBinRepository binRepository, WarehouseMapper warehouseMapper) {
        this.warehouseRepository = warehouseRepository;
        this.zoneRepository = zoneRepository;
        this.aisleRepository = aisleRepository;
        this.shelfRepository = shelfRepository;
        this.binRepository = binRepository;
        this.warehouseMapper = warehouseMapper;
    }

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException("Warehouse code already exists");
        }
        var warehouse = new Warehouse();
        warehouse.setCode(request.code());
        warehouse.setName(request.name());
        warehouse.setCountry(request.country());
        warehouse.setCity(request.city());
        warehouse.setAddress(request.address());
        return warehouseMapper.toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public PageResponse<WarehouseResponse> search(String q, Pageable pageable) {
        var page = q == null || q.isBlank()
                ? warehouseRepository.findByDeletedFalse(pageable)
                : warehouseRepository.findByDeletedFalseAndNameContainingIgnoreCaseOrDeletedFalseAndCodeContainingIgnoreCase(q, q, pageable);
        return PageResponse.from(page.map(warehouseMapper::toResponse));
    }

    @Transactional
    public LocationResponse createZone(ZoneRequest request) {
        var zone = new WarehouseZone();
        zone.setCode(request.code());
        zone.setName(request.name());
        zone.setWarehouse(findWarehouse(request.warehouseId()));
        return warehouseMapper.toResponse(zoneRepository.save(zone));
    }

    @Transactional
    public LocationResponse createAisle(AisleRequest request) {
        var aisle = new WarehouseAisle();
        aisle.setCode(request.code());
        aisle.setZone(zoneRepository.findById(request.zoneId()).orElseThrow(() -> new NotFoundException("Zone", request.zoneId())));
        return warehouseMapper.toResponse(aisleRepository.save(aisle));
    }

    @Transactional
    public LocationResponse createShelf(ShelfRequest request) {
        var shelf = new WarehouseShelf();
        shelf.setCode(request.code());
        shelf.setAisle(aisleRepository.findById(request.aisleId()).orElseThrow(() -> new NotFoundException("Aisle", request.aisleId())));
        return warehouseMapper.toResponse(shelfRepository.save(shelf));
    }

    @Transactional
    public BinResponse createBin(BinRequest request) {
        if (binRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException("Bin code already exists");
        }
        var bin = new WarehouseBin();
        bin.setCode(request.code());
        bin.setCapacity(request.capacity());
        bin.setShelf(shelfRepository.findById(request.shelfId()).orElseThrow(() -> new NotFoundException("Shelf", request.shelfId())));
        return warehouseMapper.toResponse(binRepository.save(bin));
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> zones(Long warehouseId) {
        return zoneRepository.findByWarehouseIdAndDeletedFalse(warehouseId).stream().map(warehouseMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> aisles(Long zoneId) {
        return aisleRepository.findByZoneIdAndDeletedFalse(zoneId).stream().map(warehouseMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> shelves(Long aisleId) {
        return shelfRepository.findByAisleIdAndDeletedFalse(aisleId).stream().map(warehouseMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BinResponse> bins(Long shelfId) {
        return binRepository.findByShelfIdAndDeletedFalse(shelfId).stream().map(warehouseMapper::toResponse).toList();
    }

    public Warehouse findWarehouse(Long id) {
        var warehouse = warehouseRepository.findById(id).orElseThrow(() -> new NotFoundException("Warehouse", id));
        if (warehouse.isDeleted()) {
            throw new NotFoundException("Warehouse", id);
        }
        return warehouse;
    }

    public WarehouseBin findBin(Long id) {
        var bin = binRepository.findById(id).orElseThrow(() -> new NotFoundException("Bin", id));
        if (bin.isDeleted()) {
            throw new NotFoundException("Bin", id);
        }
        return bin;
    }
}
