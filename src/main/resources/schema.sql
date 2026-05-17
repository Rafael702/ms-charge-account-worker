-- Create charge table
CREATE TABLE IF NOT EXISTS charge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    charge_id VARCHAR(36) NOT NULL UNIQUE,
    account_id VARCHAR(20) NOT NULL,
    charge_type CHAR(1) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    charge_status VARCHAR(25) NOT NULL,
    account_status VARCHAR(25),
    processing_result VARCHAR(10),
    rejection_reason TEXT,
    charge_date TIMESTAMP NOT NULL,
    processing_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);