create table roles (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    name varchar(40) not null unique,
    description varchar(120) not null
);

create table users (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    full_name varchar(120) not null,
    email varchar(180) not null unique,
    password_hash varchar(255) not null,
    status varchar(40) not null,
    phone varchar(40),
    last_login_at timestamp(6) with time zone
);

create table users_roles (
    user_id bigint not null references users(id),
    role_id bigint not null references roles(id),
    primary key (user_id, role_id)
);

create table refresh_tokens (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    token varchar(120) not null unique,
    expires_at timestamp(6) with time zone not null,
    revoked boolean not null,
    user_id bigint not null references users(id)
);

create table password_reset_tokens (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    token varchar(120) not null unique,
    expires_at timestamp(6) with time zone not null,
    used boolean not null,
    user_id bigint not null references users(id)
);

create table activation_tokens (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    token varchar(120) not null unique,
    expires_at timestamp(6) with time zone not null,
    used boolean not null,
    user_id bigint not null references users(id)
);

create table suppliers (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    company_name varchar(160) not null,
    contact_name varchar(120),
    email varchar(180) not null unique,
    phone varchar(40),
    tax_number varchar(80) unique,
    country varchar(80),
    city varchar(80),
    address varchar(300),
    status varchar(30) not null
);

create table customers (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    customer_type varchar(30) not null,
    company_name varchar(160),
    full_name varchar(160) not null,
    email varchar(180) not null unique,
    phone varchar(40),
    country varchar(80),
    city varchar(80),
    address varchar(300),
    status varchar(30) not null
);

create table categories (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    name varchar(120) not null unique,
    description varchar(300),
    parent_id bigint references categories(id)
);

create table warehouses (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    code varchar(80) not null unique,
    name varchar(160) not null,
    country varchar(80),
    city varchar(80),
    address varchar(300)
);

create table warehouse_zones (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    code varchar(80) not null,
    name varchar(160) not null,
    warehouse_id bigint not null references warehouses(id)
);

create table warehouse_aisles (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    code varchar(80) not null,
    zone_id bigint not null references warehouse_zones(id)
);

create table warehouse_shelves (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    code varchar(80) not null,
    aisle_id bigint not null references warehouse_aisles(id)
);

create table warehouse_bins (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    code varchar(100) not null unique,
    capacity integer not null,
    shelf_id bigint not null references warehouse_shelves(id)
);

create table products (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    sku varchar(80) not null unique,
    barcode varchar(80) not null unique,
    name varchar(180) not null,
    description varchar(1000),
    category_id bigint not null references categories(id),
    supplier_id bigint not null references suppliers(id),
    purchase_price numeric(19,4) not null,
    selling_price numeric(19,4) not null,
    unit varchar(30) not null,
    weight numeric(19,4) not null,
    quantity integer not null,
    minimum_quantity integer not null,
    maximum_quantity integer not null
);

create table inventories (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    product_id bigint not null references products(id),
    bin_id bigint not null references warehouse_bins(id),
    quantity integer not null,
    constraint uk_inventory_product_bin unique (product_id, bin_id)
);

create table stock_movements (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    type varchar(30) not null,
    product_id bigint not null references products(id),
    from_bin_id bigint references warehouse_bins(id),
    to_bin_id bigint references warehouse_bins(id),
    quantity integer not null,
    timestamp timestamp(6) with time zone not null,
    reference varchar(120) not null,
    reason varchar(300) not null,
    performed_by_id bigint references users(id),
    previous_quantity integer not null,
    new_quantity integer not null,
    notes varchar(1000)
);

create table purchase_orders (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    order_number varchar(80) not null unique,
    supplier_id bigint not null references suppliers(id),
    status varchar(30) not null,
    total_amount numeric(19,4) not null,
    confirmed_at timestamp(6) with time zone,
    received_at timestamp(6) with time zone,
    created_by_id bigint references users(id)
);

create table purchase_order_items (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    purchase_order_id bigint not null references purchase_orders(id),
    product_id bigint not null references products(id),
    bin_id bigint not null references warehouse_bins(id),
    quantity integer not null,
    unit_price numeric(19,4) not null,
    line_total numeric(19,4) not null
);

create table sales_orders (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    order_number varchar(80) not null unique,
    customer_id bigint not null references customers(id),
    status varchar(30) not null,
    total_amount numeric(19,4) not null,
    confirmed_at timestamp(6) with time zone,
    shipped_at timestamp(6) with time zone,
    created_by_id bigint references users(id)
);

create table sales_order_items (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    sales_order_id bigint not null references sales_orders(id),
    product_id bigint not null references products(id),
    bin_id bigint not null references warehouse_bins(id),
    quantity integer not null,
    unit_price numeric(19,4) not null,
    line_total numeric(19,4) not null
);

create table audit_events (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    user_id bigint references users(id),
    timestamp timestamp(6) with time zone not null,
    action varchar(100) not null,
    ip_address varchar(80),
    entity varchar(100) not null,
    entity_id bigint,
    details varchar(1500)
);

create table notification_logs (
    id bigserial primary key,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    version bigint,
    active boolean not null,
    deleted boolean not null,
    recipient varchar(180) not null,
    subject varchar(180) not null,
    body varchar(2000) not null,
    sent boolean not null,
    error_message varchar(500)
);

create index idx_products_sku on products(sku);
create index idx_products_barcode on products(barcode);
create index idx_inventory_product on inventories(product_id);
create index idx_stock_movement_product_time on stock_movements(product_id, timestamp);
create index idx_purchase_order_status on purchase_orders(status);
create index idx_sales_order_status on sales_orders(status);
create index idx_audit_entity on audit_events(entity, entity_id);
