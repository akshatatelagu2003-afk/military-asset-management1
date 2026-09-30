-- Military Asset Management System Database Schema
--
-- This schema preserves historical transactions. Balances should be calculated dynamically:
-- 1. Net Movement = Purchases + Transfer In - Transfer Out (Expended is NOT included in Net Movement)
-- 2. Closing Balance = Opening Balance + Purchases + Transfer In - Transfer Out - Expended
-- 3. Assigned items are tracked separately.

CREATE TABLE bases (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('ADMIN', 'BASE_COMMANDER', 'LOGISTICS_OFFICER')),
    base_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_base FOREIGN KEY (base_id) REFERENCES bases(id),
    CONSTRAINT chk_base_commander_has_base CHECK (role != 'BASE_COMMANDER' OR base_id IS NOT NULL)
);

CREATE TABLE equipment (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL CHECK (type IN ('VEHICLE', 'WEAPON', 'AMMUNITION', 'OTHER')),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    base_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    purchase_date DATE NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchases_base FOREIGN KEY (base_id) REFERENCES bases(id),
    CONSTRAINT fk_purchases_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_purchases_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE transfers (
    id BIGSERIAL PRIMARY KEY,
    from_base_id BIGINT NOT NULL,
    to_base_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    transfer_date DATE NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfers_from_base FOREIGN KEY (from_base_id) REFERENCES bases(id),
    CONSTRAINT fk_transfers_to_base FOREIGN KEY (to_base_id) REFERENCES bases(id),
    CONSTRAINT fk_transfers_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_transfers_user FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_different_bases CHECK (from_base_id <> to_base_id)
);

CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    base_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    personnel_name VARCHAR(150) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    assigned_date DATE NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assignments_base FOREIGN KEY (base_id) REFERENCES bases(id),
    CONSTRAINT fk_assignments_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_assignments_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE expenditures (
    id BIGSERIAL PRIMARY KEY,
    base_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    reason VARCHAR(255) NOT NULL,
    expended_date DATE NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenditures_base FOREIGN KEY (base_id) REFERENCES bases(id),
    CONSTRAINT fk_expenditures_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    CONSTRAINT fk_expenditures_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- INDEXES
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_base_id ON users(base_id);
CREATE INDEX idx_purchases_base_id ON purchases(base_id);
CREATE INDEX idx_purchases_equipment_id ON purchases(equipment_id);
CREATE INDEX idx_purchases_purchase_date ON purchases(purchase_date);
CREATE INDEX idx_transfers_from_base_id ON transfers(from_base_id);
CREATE INDEX idx_transfers_to_base_id ON transfers(to_base_id);
CREATE INDEX idx_transfers_equipment_id ON transfers(equipment_id);
CREATE INDEX idx_transfers_transfer_date ON transfers(transfer_date);
CREATE INDEX idx_assignments_base_id ON assignments(base_id);
CREATE INDEX idx_assignments_equipment_id ON assignments(equipment_id);
CREATE INDEX idx_expenditures_base_id ON expenditures(base_id);
CREATE INDEX idx_expenditures_equipment_id ON expenditures(equipment_id);
CREATE INDEX idx_expenditures_expended_date ON expenditures(expended_date);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);

-- Table Relationships Overview:
-- - `users` belong to `bases` via `base_id` (Admins and Logistics Officers can have NULL).
-- - `purchases` link to `bases` (where purchased), `equipment` (what was purchased), and `users` (who recorded it).
-- - `transfers` link to `bases` twice (`from_base_id` and `to_base_id`), `equipment` (what was transferred), and `users`.
-- - `assignments` link to `bases` (where assigned), `equipment`, and `users`.
-- - `expenditures` link to `bases` (where expended), `equipment`, and `users`.
-- - `audit_logs` link to `users` to track who performed actions across the system.
