CREATE DATABASE inventory_management;

USE inventory_management;

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
    remarks TEXT
);

select * from inventory_management;

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

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
    
    


