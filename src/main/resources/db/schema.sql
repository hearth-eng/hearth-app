-- Run the below script to generate the tables --
-- h2-script.sh -url jdbc:h2:tcp://localhost:9092/~/testdb -user test -password test123 -script /Users/schan280/temp/hearth-app/src/main/resources/db/schema.sql --

-- Table Script --

-- 0. Serving/Operating Cities --

CREATE TABLE fks_countries (
    country_id          INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    country_code        VARCHAR(3)      NOT NULL,
    country_name        VARCHAR(128)    NOT NULL,
    language_code       VARCHAR(3)      NOT NULL,
    language            VARCHAR(30)     NOT NULL,
    locale_code         VARCHAR(8)      NOT NULL,
    currency_code       VARCHAR(3)      NOT NULL,
    currency            VARCHAR(20)     NOT NULL,
    timezone            VARCHAR(32)     NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       
);

CREATE TABLE fks_provinces (
    province_id         INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    country_id          INT             NOT NULL,
    province_name       VARCHAR(128)    ,
    region              VARCHAR(32)     ,
    language            VARCHAR(30)     NOT NULL,
    status              VARCHAR(20)     NOT NULL CHECK (status IN ('PLANNED', 'ACTIVE', 'PAUSED', 'INACTIVE')),
    launched_at         DATE            ,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       
);

CREATE TABLE fks_cities (
    city_id             INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    province_id         INT             NOT NULL,
    city_name           VARCHAR(50)     NOT NULL,
    image_key           VARCHAR(128)    ,
    status              VARCHAR(20)     NOT NULL CHECK (status IN ('PLANNED', 'ACTIVE', 'PAUSED', 'INACTIVE')),
    launched_at         DATE            ,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       
);

CREATE TABLE fks_neighbourhoods (
    neighbourhood_id    INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    city_id             INT             NOT NULL,
    locality            VARCHAR(80)     NOT NULL,
    pincode             INT             NOT NULL,
    zone                VARCHAR(80)     NOT NULL,
    latitude            NUMERIC(20, 6)  ,
    longitude           NUMERIC(20, 6)  ,
    is_serviceable      SMALLINT        NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       
);


-- 1. Users (Customers + Service Professionals + Admins)

CREATE TABLE fks_users (
    user_id             INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    external_id         VARCHAR(36)     NOT NULL,
    full_name           VARCHAR(96)     NOT NULL,
    email               VARCHAR(128)    NOT NULL,
    phone1              VARCHAR(20)     NOT NULL,
    phone2              VARCHAR(20)     ,
    password_hash       TEXT            ,
    role                VARCHAR(16)     NOT NULL CHECK (role IN ('CUSTOMER', 'PROFESSIONAL', 'ADMIN')),
    status              VARCHAR(16)     NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- Address

CREATE TABLE fks_addresses (
    address_id          INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    user_id             INT             NOT NULL,
    address_line1       VARCHAR(128)    NOT NULL,
    address_line2       VARCHAR(128)    ,
    neighbourhood_id    INT             NOT NULL,
    latitude            NUMERIC(20, 6)  ,
    longitude           NUMERIC(20, 6)  ,
    is_default          SMALLINT        NOT NULL,
    label               VARCHAR(20)     NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- 2. Service Catalog - Categories & Services

CREATE TABLE fks_categories (
    category_id         INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    name                VARCHAR(128)    NOT NULL,
    icon                VARCHAR(16)     ,
    tag_line            VARCHAR(128)    ,
    image               VARCHAR(128)    ,
    parent_id           INT             ,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

CREATE TABLE fks_services (
    service_id          INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    category_id         INT             NOT NULL,
    name                VARCHAR(128)    NOT NULL,
    description         TEXT            ,
    base_price          NUMERIC(7, 2)   NOT NULL,
    currency            VARCHAR(3)      NOT NULL,
    duration_minutes    SMALLINT        ,
    image               VARCHAR(128)    ,
    rating_avg          NUMERIC(3, 2)   ,
    reviews             INT             ,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- 3. Professional Profiles

CREATE TABLE fks_professionals (
    professional_id     INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    user_id             INT             NOT NULL,
    bio                 TEXT            ,
    experience_years    SMALLINT        NOT NULL,
    serving_cities      VARCHAR(255)    NOT NULL,
    rating_avg          NUMERIC(3, 2)   ,
    is_verified         SMALLINT        NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- Professional Skills

CREATE TABLE fks_professional_services (
    id                  INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    professional_id     INT             NOT NULL,
    service_id          INT             NOT NULL,
    price               NUMERIC(7, 2)   NOT NULL,
    is_active           SMALLINT        NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- Professional Service Areas

CREATE TABLE fks_professional_neighbourhoods (
    id                  INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    professional_id     INT             NOT NULL,
    neighbourhood_id    INT             NOT NULL,
    status              VARCHAR(20)     NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       
);

-- Professional's Availability

CREATE TABLE fks_availabilities (
    availability_id     INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    professional_id     INT             NOT NULL,
    date                DATE            NOT NULL,
    start_time          TIME            ,
    end_time            TIME            ,
    is_booked           SMALLINT        NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);


-- 4. Booking & Scheduling

-- Booking

CREATE TABLE fks_bookings (
    booking_id          VARCHAR(36)     NOT NULL,
    customer_id         INT             NOT NULL,
    professional_id     INT             NOT NULL,
    service_id          INT             NOT NULL,
    address_id          INT             NOT NULL,
    scheduled_at        TIMESTAMP       NOT NULL,
    time_slot           VARCHAR(24)     NOT NULL,
    status              VARCHAR(16)     CHECK (status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    status_msg          VARCHAR(255)    NUll,
    total_amount        NUMERIC(20, 6)  NOT NULL,
    payment_method      VARCHAR(16)     CHECK (payment_method IN ('CARD', 'UPI', 'WALLET', 'COD')),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       ,
    updated_by          VARCHAR(50)
);

-- 5. Payments & Pricing

-- Payments

CREATE TABLE fks_payments (
    payment_id          INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    booking_id          VARCHAR(36)     NOT NULL,
    amount              NUMERIC(7, 2)   NOT NULL,
    payment_method      VARCHAR(16)     CHECK (payment_method IN ('CARD', 'UPI', 'WALLET', 'COD')),
    payment_status      VARCHAR(16)     CHECK (payment_status IN ('INITIATED', 'SUCCESS', 'FAILED', 'REFUNDED')),
    transaction_ref     VARCHAR(128)    NOT NULL,
    paid_at             TIMESTAMP       NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- Coupons & Discounts

CREATE TABLE fks_coupons (
    coupon_id           INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    code                VARCHAR(16)     NOT NULL,
    title               VARCHAR(48)     NOT NULL,
    description         VARCHAR(128)    NOT NULL,
    terms               VARCHAR(128)    NOT NULL,
    discount_type       VARCHAR(16)     NOT NULL,
    discount_value      NUMERIC(7, 2)   ,
    max_discount        NUMERIC(7, 2)   ,
    expiry_date         DATE            ,
    usage_limit         SMALLINT        ,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

CREATE TABLE fks_coupon_usage (
    usage_id            INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    coupon_id           INT             NOT NULL,
    user_id             INT             NOT NULL,
    booking_id          VARCHAR(36)     NOT NULL,
    used_at             TIMESTAMP       NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- 6. Ratings & Reviews

CREATE TABLE fks_reviews (
    review_id           INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    booking_id          VARCHAR(36)     NOT NULL,
    customer_id         INT             NOT NULL,
    professional_id     INT             NOT NULL,
    rating              SMALLINT        ,
    comment             TEXT            ,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- 7. Communication

CREATE TABLE fks_conversations (
    conversation_id     INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    booking_id          VARCHAR(36)     NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

CREATE TABLE fks_messages (
    message_id          INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    conversation_id     INT             NOT NULL,
    sender_id           INT             NOT NULL,
    message_text        TEXT            ,
    sent_at             TIMESTAMP       NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- 8. Operations & Logistics

CREATE TABLE fks_job_status (
    log_id              INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    booking_id          VARCHAR(36)     NOT NULL,
    status              VARCHAR(32)     NOT NULL,
    updated_by          INT             ,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- 9. Admin & Compliance

-- Documents (KYC, Verification)

CREATE TABLE fks_documents (
    document_id         INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    professional_id     INT             NOT NULL,
    application_id      VARCHAR(36)     NOT NULL,
    document_type       VARCHAR(50)     NOT NULL,
    document_number     VARCHAR(50)     NOT NULL,
    document_url        TEXT            ,
    name_on_document    VARCHAR(50)     NOT NULL,
    verification_status VARCHAR(16)     CHECK (verification_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    comment             VARCHAR(128)    ,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- 10. Surge Pricing

CREATE TABLE fks_pricing_rules (
    rule_id             INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    service_id          INT             NOT NULL,
    city                VARCHAR(100)    NOT NULL,
    multiplier          NUMERIC(7, 2)   NOT NULL,
    start_time          TIMESTAMP       NOT NULL,
    end_time            TIMESTAMP       NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- 11. Wallet Systems

CREATE TABLE fks_wallets (
    wallet_id           VARCHAR(36)     NOT NULL,
    user_id             INT             NOT NULL,
    balance             NUMERIC(7, 2)   ,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

CREATE TABLE fks_wallet_transactions (
    txn_id              VARCHAR(64)     NOT NULL,
    wallet_id           VARCHAR(36)     NOT NULL,
    amount              NUMERIC(7, 2)   NOT NULL,
    type                VARCHAR(16)     CHECK (type IN ('CREDIT', 'DEBIT')),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- 12. Analytics / Audit

CREATE TABLE fks_audit_logs (
    log_id              INT             GENERATED ALWAYS AS IDENTITY NOT NULL,
    user_id             INT             NOT NULL,
    action              VARCHAR(64)     NOT NULL,
    entity_type         VARCHAR(50)     NOT NULL,
    entity_id           INT             NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP     
);

-- Primary Key Constraint --

ALTER TABLE fks_countries
ADD CONSTRAINT fks_countries_pk
PRIMARY KEY (country_id);

ALTER TABLE fks_provinces
ADD CONSTRAINT fks_provinces_pk
PRIMARY KEY (province_id);

ALTER TABLE fks_cities
ADD CONSTRAINT fks_cities_pk
PRIMARY KEY (city_id);

ALTER TABLE fks_neighbourhoods
ADD CONSTRAINT fks_neighbourhoods_pk
PRIMARY KEY (neighbourhood_id);

ALTER TABLE fks_payments
ADD CONSTRAINT fks_payments_pk
PRIMARY KEY (payment_id);

ALTER TABLE fks_users
ADD CONSTRAINT fks_users_pk
PRIMARY KEY (user_id);

ALTER TABLE fks_addresses
ADD CONSTRAINT fks_addresses_pk
PRIMARY KEY (address_id);

ALTER TABLE fks_categories
ADD CONSTRAINT fks_categories_pk
PRIMARY KEY (category_id);

ALTER TABLE fks_services
ADD CONSTRAINT fks_services_pk
PRIMARY KEY (service_id);

ALTER TABLE fks_professionals
ADD CONSTRAINT fks_professionals_pk
PRIMARY KEY (professional_id);

ALTER TABLE fks_professional_services
ADD CONSTRAINT fks_professional_services_pk
PRIMARY KEY (id);

ALTER TABLE fks_professional_neighbourhoods
ADD CONSTRAINT fks_professional_neighbourhoods_pk
PRIMARY KEY (id);

ALTER TABLE fks_bookings
ADD CONSTRAINT fks_bookings_pk
PRIMARY KEY (booking_id);

ALTER TABLE fks_availabilities
ADD CONSTRAINT fks_availabilities_pk
PRIMARY KEY (availability_id);

ALTER TABLE fks_reviews
ADD CONSTRAINT fks_reviews_pk
PRIMARY KEY (review_id);

ALTER TABLE fks_conversations
ADD CONSTRAINT fks_conversations_pk
PRIMARY KEY (conversation_id);

ALTER TABLE fks_messages
ADD CONSTRAINT fks_messages_pk
PRIMARY KEY (message_id);

ALTER TABLE fks_job_status
ADD CONSTRAINT fks_job_status_pk
PRIMARY KEY (log_id);

ALTER TABLE fks_documents
ADD CONSTRAINT fks_documents_pk
PRIMARY KEY (document_id);

ALTER TABLE fks_audit_logs
ADD CONSTRAINT fks_audit_logs_pk
PRIMARY KEY (log_id);

ALTER TABLE fks_pricing_rules
ADD CONSTRAINT fks_pricing_rules_pk
PRIMARY KEY (rule_id);

ALTER TABLE fks_wallets
ADD CONSTRAINT fks_wallets_pk
PRIMARY KEY (wallet_id);

ALTER TABLE fks_wallet_transactions
ADD CONSTRAINT fks_wallet_transactions_pk
PRIMARY KEY (txn_id);

ALTER TABLE fks_coupons
ADD CONSTRAINT fks_coupon_pk
PRIMARY KEY (coupon_id);

ALTER TABLE fks_coupon_usage
ADD CONSTRAINT fks_coupon_usage_pk
PRIMARY KEY (usage_id);


-- Unique Key Constraint --

ALTER TABLE fks_countries
ADD CONSTRAINT fks_countries_uk
UNIQUE (country_code, country_name);

ALTER TABLE fks_provinces
ADD CONSTRAINT fks_provinces_uk
UNIQUE (country_id, province_name);

ALTER TABLE fks_cities
ADD CONSTRAINT fks_cities_uk
UNIQUE (province_id, city_name);

ALTER TABLE fks_neighbourhoods
ADD CONSTRAINT fks_neighbourhoods_uk
UNIQUE (locality, pincode);

ALTER TABLE fks_users
ADD CONSTRAINT fks_users_uk1
UNIQUE (external_id);

ALTER TABLE fks_users
ADD CONSTRAINT fks_users_uk2
UNIQUE (phone1);

ALTER TABLE fks_professional_neighbourhoods
ADD CONSTRAINT fks_professional_neighbourhoods_uk
UNIQUE (professional_id, neighbourhood_id);

-- Foreign Key Constraint --

ALTER TABLE fks_provinces
ADD CONSTRAINT fks_provines_fk1
FOREIGN KEY (country_id)
REFERENCES fks_countries (country_id);

ALTER TABLE fks_cities
ADD CONSTRAINT fks_cities_fk1
FOREIGN KEY (province_id)
REFERENCES fks_provinces (province_id);

ALTER TABLE fks_neighbourhoods
ADD CONSTRAINT fks_neighbourhoods_fk1
FOREIGN KEY (city_id)
REFERENCES fks_cities (city_id);

ALTER TABLE fks_addresses
ADD CONSTRAINT fks_addresses_fk1
FOREIGN KEY (user_id)
REFERENCES fks_users (user_id);

ALTER TABLE fks_addresses
ADD CONSTRAINT fks_addresses_fk2
FOREIGN KEY (neighbourhood_id)
REFERENCES fks_neighbourhoods (neighbourhood_id);

ALTER TABLE fks_professionals
ADD CONSTRAINT fks_professionals_fk1
FOREIGN KEY (user_id)
REFERENCES fks_users (user_id);

ALTER TABLE fks_documents
ADD CONSTRAINT fks_documents_fk1
FOREIGN KEY (professional_id)
REFERENCES fks_professionals (professional_id);

ALTER TABLE fks_bookings
ADD CONSTRAINT fks_bookings_fk1
FOREIGN KEY (customer_id)
REFERENCES fks_users (user_id);

ALTER TABLE fks_coupon_usage
ADD CONSTRAINT fks_coupon_usage_fk1
FOREIGN KEY (user_id)
REFERENCES fks_users (user_id);

ALTER TABLE fks_bookings
ADD CONSTRAINT fks_bookings_fk2
FOREIGN KEY (address_id)
REFERENCES fks_addresses (address_id);

ALTER TABLE fks_availabilities
ADD CONSTRAINT fks_availabilities_fk1
FOREIGN KEY (professional_id)
REFERENCES fks_professionals (professional_id);

ALTER TABLE fks_professional_services
ADD CONSTRAINT fks_professional_services_fk1
FOREIGN KEY (professional_id)
REFERENCES fks_professionals (professional_id);

ALTER TABLE fks_professional_services
ADD CONSTRAINT fks_professional_services_fk2
FOREIGN KEY (service_id)
REFERENCES fks_services (service_id);

ALTER TABLE fks_professional_neighbourhoods
ADD CONSTRAINT fks_professional_neighbourhoods_fk1
FOREIGN KEY (professional_id)
REFERENCES fks_professionals(professional_id);

ALTER TABLE fks_professional_neighbourhoods
ADD CONSTRAINT fks_professional_neighbourhoods_fk2
FOREIGN KEY (neighbourhood_id)
REFERENCES fks_neighbourhoods(neighbourhood_id);

ALTER TABLE fks_categories
ADD CONSTRAINT fks_categories_fk1
FOREIGN KEY (parent_id)
REFERENCES fks_categories (category_id);

ALTER TABLE fks_services
ADD CONSTRAINT fks_services_fk1
FOREIGN KEY (category_id)
REFERENCES fks_categories (category_id);

ALTER TABLE fks_bookings
ADD CONSTRAINT fks_bookings_fk3
FOREIGN KEY (service_id)
REFERENCES fks_services (service_id);

ALTER TABLE fks_messages
ADD CONSTRAINT fks_messages_fk1
FOREIGN KEY (conversation_id)
REFERENCES fks_conversations (conversation_id);

ALTER TABLE fks_coupon_usage
ADD CONSTRAINT fks_coupon_usage_fk2
FOREIGN KEY (booking_id)
REFERENCES fks_bookings (booking_id);

ALTER TABLE fks_payments
ADD CONSTRAINT fks_payments_fk1
FOREIGN KEY (booking_id)
REFERENCES fks_bookings (booking_id);
