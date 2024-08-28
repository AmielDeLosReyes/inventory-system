CREATE DATABASE inventory_management;

USE inventory_management;

ALTER TABLE inventory 
MODIFY COLUMN cost VARCHAR(255) NOT NULL,
MODIFY COLUMN in_amount VARCHAR(255),
MODIFY COLUMN out_amount VARCHAR(255),
MODIFY COLUMN balance VARCHAR(255);


CREATE TABLE inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    entry_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    in_amount DECIMAL(10, 2),
    out_amount DECIMAL(10, 2),
    balance DECIMAL(10, 2),
    remarks TEXT,
    status_code VARCHAR(10) DEFAULT '1',
    added_by VARCHAR(255) DEFAULT 'SYSTEM',
	added_date VARCHAR(20),  -- Leave default value out
    modified_by VARCHAR(255) DEFAULT 'SYSTEM',
	modified_date VARCHAR(20) -- Automatically update on row modification

);


select * from inventory;
select * from products;

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status_code VARCHAR(10) DEFAULT '1',
    added_by VARCHAR(255) DEFAULT 'SYSTEM',
	added_date VARCHAR(20),  -- Leave default value out
    modified_by VARCHAR(255) DEFAULT 'SYSTEM',
	modified_date VARCHAR(20) -- Automatically update on row modification
);


-- Add columns to the inventory table
ALTER TABLE inventory
ADD COLUMN status_code VARCHAR(10) DEFAULT '1',
ADD COLUMN added_by VARCHAR(255) DEFAULT 'SYSTEM',
ADD COLUMN added_date VARCHAR(20) DEFAULT '2024-08-25', -- Set a static default date
ADD COLUMN modified_by VARCHAR(255) DEFAULT 'SYSTEM',
ADD COLUMN modified_date VARCHAR(20) DEFAULT '2024-08-25'; -- Set a static default date

-- Add columns to the products table
ALTER TABLE products
ADD COLUMN status_code VARCHAR(10) DEFAULT '1',
ADD COLUMN added_by VARCHAR(255) DEFAULT 'SYSTEM',
ADD COLUMN added_date VARCHAR(20) DEFAULT '2024-08-25', -- Set a static default date
ADD COLUMN modified_by VARCHAR(255) DEFAULT 'SYSTEM',
ADD COLUMN modified_date VARCHAR(20) DEFAULT '2024-08-25'; -- Set a static default date





INSERT INTO products (name, price) VALUES
    ('Georgina', 35),
    ('Mia', 40),
    ('Metilda', 40),
    ('Lily', 40),
    ('Oda', 40),
    ('Crista', 70),
    ('Ava', 70),
    ('Serenity', 40),
    ('Sophia', 40),
    ('Amara', 16),
    ('Lana', 40),
    ('Aria', 40),
    ('Atena', 30),
    ('Cristina', 40),
    ('Cassey', 40),
    ('Rosie', 40),
    ('Stella', 85),
    ('Kara', 80),
    ('Olivia', 55);
    
    
SELECT *
FROM inventory
WHERE product_name = 'Georgina'
ORDER BY entry_date DESC, id DESC
LIMIT 1;

truncate table inventory;
truncate table products;
SHOW COLUMNS FROM inventory;

-- Script to get daily quantities and out_amounts
SELECT
    entry_date,
    product_name,
    description,
    SUM(quantity) AS total_quantity_day,
    SUM(out_amount) AS total_out_amount_day
FROM
    inventory
WHERE
    product_name = 'Georgina'
    AND description = 'Sale of Merchandise'
GROUP BY
    entry_date, product_name, description
ORDER BY
    entry_date;


-- Script to get monthly totals for out_amounts and quantities
SELECT
    YEAR(entry_date) AS year,
    MONTH(entry_date) AS month,
    product_name,
    description,
    SUM(quantity) AS total_quantity_month,
    SUM(out_amount) AS total_out_amount_month
FROM
    inventory
WHERE
    product_name = 'Georgina'
    AND description = 'Sale of Merchandise'
GROUP BY
    YEAR(entry_date), MONTH(entry_date), product_name, description
ORDER BY
    year, month;





