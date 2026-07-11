package com.yazidwms.config;

import com.yazidwms.category.entity.Category;
import com.yazidwms.category.repository.CategoryRepository;
import com.yazidwms.customer.entity.Customer;
import com.yazidwms.customer.entity.CustomerType;
import com.yazidwms.customer.repository.CustomerRepository;
import com.yazidwms.inventory.entity.Inventory;
import com.yazidwms.inventory.repository.InventoryRepository;
import com.yazidwms.product.entity.Product;
import com.yazidwms.product.repository.ProductRepository;
import com.yazidwms.role.entity.Role;
import com.yazidwms.role.entity.RoleName;
import com.yazidwms.role.repository.RoleRepository;
import com.yazidwms.supplier.entity.BusinessStatus;
import com.yazidwms.supplier.entity.Supplier;
import com.yazidwms.supplier.repository.SupplierRepository;
import com.yazidwms.user.entity.User;
import com.yazidwms.user.entity.UserStatus;
import com.yazidwms.user.repository.UserRepository;
import com.yazidwms.warehouse.entity.Warehouse;
import com.yazidwms.warehouse.entity.WarehouseAisle;
import com.yazidwms.warehouse.entity.WarehouseBin;
import com.yazidwms.warehouse.entity.WarehouseShelf;
import com.yazidwms.warehouse.entity.WarehouseZone;
import com.yazidwms.warehouse.repository.WarehouseAisleRepository;
import com.yazidwms.warehouse.repository.WarehouseBinRepository;
import com.yazidwms.warehouse.repository.WarehouseRepository;
import com.yazidwms.warehouse.repository.WarehouseShelfRepository;
import com.yazidwms.warehouse.repository.WarehouseZoneRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Bean
    @Profile("!test")
    CommandLineRunner seedData(SeedService seedService) {
        return args -> seedService.seed();
    }

    @Configuration
    static class SeedService {
        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final SupplierRepository supplierRepository;
        private final CategoryRepository categoryRepository;
        private final CustomerRepository customerRepository;
        private final WarehouseRepository warehouseRepository;
        private final WarehouseZoneRepository zoneRepository;
        private final WarehouseAisleRepository aisleRepository;
        private final WarehouseShelfRepository shelfRepository;
        private final WarehouseBinRepository binRepository;
        private final ProductRepository productRepository;
        private final InventoryRepository inventoryRepository;
        private final PasswordEncoder passwordEncoder;
        private final String adminEmail;
        private final String adminPassword;
        private final boolean demoData;

        SeedService(RoleRepository roleRepository, UserRepository userRepository, SupplierRepository supplierRepository,
                    CategoryRepository categoryRepository, CustomerRepository customerRepository, WarehouseRepository warehouseRepository,
                    WarehouseZoneRepository zoneRepository, WarehouseAisleRepository aisleRepository, WarehouseShelfRepository shelfRepository,
                    WarehouseBinRepository binRepository, ProductRepository productRepository, InventoryRepository inventoryRepository,
                    PasswordEncoder passwordEncoder, @Value("${app.seed.admin-email}") String adminEmail,
                    @Value("${app.seed.admin-password}") String adminPassword,
                    @Value("${app.seed.demo-data:false}") boolean demoData) {
            this.roleRepository = roleRepository;
            this.userRepository = userRepository;
            this.supplierRepository = supplierRepository;
            this.categoryRepository = categoryRepository;
            this.customerRepository = customerRepository;
            this.warehouseRepository = warehouseRepository;
            this.zoneRepository = zoneRepository;
            this.aisleRepository = aisleRepository;
            this.shelfRepository = shelfRepository;
            this.binRepository = binRepository;
            this.productRepository = productRepository;
            this.inventoryRepository = inventoryRepository;
            this.passwordEncoder = passwordEncoder;
            this.adminEmail = adminEmail;
            this.adminPassword = adminPassword;
            this.demoData = demoData;
        }

        @Transactional
        public void seed() {
            Arrays.stream(RoleName.values()).forEach(roleName -> {
                if (!roleRepository.existsByName(roleName)) {
                    roleRepository.save(new Role(roleName, roleName.name().replace('_', ' ') + " role"));
                }
            });

            if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
                var admin = new User();
                admin.setFullName("YazidWMS Administrator");
                admin.setEmail(adminEmail);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setStatus(UserStatus.ACTIVE);
                admin.getRoles().add(roleRepository.findByName(RoleName.ADMIN).orElseThrow());
                userRepository.save(admin);
            }

            if (!demoData) {
                return;
            }

            if (supplierRepository.count() == 0) {
                var supplier = new Supplier();
                supplier.setCompanyName("Atlas Industrial Supplies");
                supplier.setContactName("Operations Desk");
                supplier.setEmail("supplier@yazidwms.local");
                supplier.setPhone("+212500000001");
                supplier.setTaxNumber("SUP-001");
                supplier.setCountry("Morocco");
                supplier.setCity("Casablanca");
                supplier.setAddress("Casablanca Logistics Park");
                supplier.setStatus(BusinessStatus.ACTIVE);
                supplierRepository.save(supplier);
            }

            if (categoryRepository.count() == 0) {
                var category = new Category();
                category.setName("Electronics");
                category.setDescription("Electronic devices and accessories");
                categoryRepository.save(category);
            }

            if (customerRepository.count() == 0) {
                var customer = new Customer();
                customer.setCustomerType(CustomerType.COMPANY);
                customer.setCompanyName("Northwind Retail Morocco");
                customer.setFullName("Retail Procurement");
                customer.setEmail("customer@yazidwms.local");
                customer.setCountry("Morocco");
                customer.setCity("Rabat");
                customer.setStatus(BusinessStatus.ACTIVE);
                customerRepository.save(customer);
            }

            if (warehouseRepository.count() == 0) {
                var warehouse = new Warehouse();
                warehouse.setCode("WH-CASA-01");
                warehouse.setName("Casablanca Main Warehouse");
                warehouse.setCountry("Morocco");
                warehouse.setCity("Casablanca");
                warehouse.setAddress("Ain Sebaa Industrial Area");
                warehouseRepository.save(warehouse);

                var zone = new WarehouseZone();
                zone.setCode("Z-A");
                zone.setName("Receiving Zone");
                zone.setWarehouse(warehouse);
                zoneRepository.save(zone);

                var aisle = new WarehouseAisle();
                aisle.setCode("A-01");
                aisle.setZone(zone);
                aisleRepository.save(aisle);

                var shelf = new WarehouseShelf();
                shelf.setCode("S-01");
                shelf.setAisle(aisle);
                shelfRepository.save(shelf);

                var bin = new WarehouseBin();
                bin.setCode("BIN-A-01-01");
                bin.setCapacity(5000);
                bin.setShelf(shelf);
                binRepository.save(bin);
            }

            if (productRepository.count() == 0) {
                var supplier = supplierRepository.findAll().getFirst();
                var category = categoryRepository.findAll().getFirst();
                var bin = binRepository.findAll().getFirst();
                var product = new Product();
                product.setSku("SKU-LAPTOP-001");
                product.setBarcode("611000000001");
                product.setName("Business Laptop 14");
                product.setDescription("Seed product for complete warehouse workflows");
                product.setCategory(category);
                product.setSupplier(supplier);
                product.setPurchasePrice(new BigDecimal("650.00"));
                product.setSellingPrice(new BigDecimal("899.00"));
                product.setUnit("PCS");
                product.setWeight(new BigDecimal("1.40"));
                product.setMinimumQuantity(10);
                product.setMaximumQuantity(500);
                product.setQuantity(25);
                productRepository.save(product);

                var inventory = new Inventory();
                inventory.setProduct(product);
                inventory.setBin(bin);
                inventory.setQuantity(25);
                inventoryRepository.save(inventory);
            }
        }
    }
}
