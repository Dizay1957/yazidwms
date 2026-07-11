package com.yazidwms.warehouse.mapper;

import com.yazidwms.warehouse.dto.WarehouseDtos.BinResponse;
import com.yazidwms.warehouse.dto.WarehouseDtos.LocationResponse;
import com.yazidwms.warehouse.dto.WarehouseDtos.WarehouseResponse;
import com.yazidwms.warehouse.entity.Warehouse;
import com.yazidwms.warehouse.entity.WarehouseAisle;
import com.yazidwms.warehouse.entity.WarehouseBin;
import com.yazidwms.warehouse.entity.WarehouseShelf;
import com.yazidwms.warehouse.entity.WarehouseZone;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    default WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(warehouse.getId(), warehouse.getCode(), warehouse.getName(), warehouse.getCountry(), warehouse.getCity(), warehouse.getAddress(), warehouse.isActive());
    }

    default LocationResponse toResponse(WarehouseZone zone) {
        return new LocationResponse(zone.getId(), zone.getCode(), zone.getName(), zone.getWarehouse().getId());
    }

    default LocationResponse toResponse(WarehouseAisle aisle) {
        return new LocationResponse(aisle.getId(), aisle.getCode(), null, aisle.getZone().getId());
    }

    default LocationResponse toResponse(WarehouseShelf shelf) {
        return new LocationResponse(shelf.getId(), shelf.getCode(), null, shelf.getAisle().getId());
    }

    default BinResponse toResponse(WarehouseBin bin) {
        return new BinResponse(bin.getId(), bin.getCode(), bin.getCapacity(), bin.getShelf().getId(), bin.isActive());
    }
}
