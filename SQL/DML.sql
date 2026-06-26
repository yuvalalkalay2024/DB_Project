-- ==========================================
-- Database: E-Commerce System (PostgreSQL)
-- DML Script - Mock Data Insertion
-- ==========================================

-- 1. Insert Users (3 Sellers, 5 Buyers)
INSERT INTO Users (username, password, role) VALUES
('TechStore', 'pass123', 'SELLER'),
('FashionHub', 'pass123', 'SELLER'),
('OfficeDepot', 'pass123', 'SELLER'),
('Alice', 'buyerpass1', 'BUYER'),
('Bob', 'buyerpass2', 'BUYER'),
('Charlie', 'buyerpass3', 'BUYER'),
('David', 'buyerpass4', 'BUYER'),
('Eve', 'buyerpass5', 'BUYER');

-- 2. Insert Addresses (For Buyers)
-- Assuming Alice is user_id 4, Bob is 5, etc.
INSERT INTO Addresses (user_id, country, city, street, house_number) VALUES
(4, 'Israel', 'Tel Aviv', 'Dizengoff', 10),
(5, 'Israel', 'Haifa', 'Herzl', 22),
(6, 'Israel', 'Jerusalem', 'Jaffa', 5),
(7, 'USA', 'New York', 'Broadway', 100),
(8, 'UK', 'London', 'Oxford St', 15);

-- 3. Insert Products
-- Sellers: TechStore(1), FashionHub(2), OfficeDepot(3)
INSERT INTO Products (seller_id, name, price, category, is_special_prod) VALUES
(1, 'Laptop Pro 15', 4500.00, 'Electricity', FALSE),
(1, 'Wireless Mouse', 150.00, 'Electricity', FALSE),
(1, 'Gaming Monitor 27"', 1200.00, 'Electricity', TRUE),  -- Special Product

(2, 'Winter Jacket', 350.00, 'Clothing', FALSE),
(2, 'Designer Sneakers', 400.00, 'Clothing', TRUE),     -- Special Product
(2, 'Cotton T-Shirt', 50.00, 'Clothing', FALSE),

(3, 'Ergonomic Desk Chair', 650.00, 'Office', TRUE),    -- Special Product
(3, 'A4 Printer Paper', 30.00, 'Office', FALSE),

(2, 'Kids School Bag', 120.00, 'Children', FALSE),
(1, 'Smartwatch', 800.00, 'Electricity', FALSE);

-- 4. Insert Special Products (Using product_id of the TRUE items above: 3, 5, 7)
INSERT INTO Special_Products (product_id, extra_pay) VALUES
(3, 100.00), -- Extra shipping/warranty for Gaming Monitor
(5, 50.00),  -- Premium packaging for Designer Sneakers
(7, 150.00); -- Heavy item delivery fee for Desk Chair

-- 5. Insert Orders
-- Buyers: 4, 5, 6, 7, 8
INSERT INTO Orders (buyer_id, order_time) VALUES
(4, '2026-06-01 10:30:00'),
(5, '2026-06-02 14:15:00'),
(4, '2026-06-05 09:00:00'),
(6, '2026-06-10 16:45:00'),
(7, '2026-06-15 11:20:00'),
(8, '2026-06-20 18:30:00');

-- 6. Insert Order_Products (Mapping products to orders)
-- Order 1 (Alice): Laptop, Mouse, Bag
INSERT INTO Order_Products (order_id, product_id) VALUES
(1, 1),
(1, 2),
(1, 9);

-- Order 2 (Bob): Monitor, T-Shirt
INSERT INTO Order_Products (order_id, product_id) VALUES
(2, 3),
(2, 6);

-- Order 3 (Alice again): Winter Jacket
INSERT INTO Order_Products (order_id, product_id) VALUES
(3, 4);

-- Order 4 (Charlie): Chair, Printer Paper, Mouse
INSERT INTO Order_Products (order_id, product_id) VALUES
(4, 7),
(4, 8),
(4, 2);

-- Order 5 (David): Designer Sneakers, Smartwatch
INSERT INTO Order_Products (order_id, product_id) VALUES
(5, 5),
(5, 10);

-- Order 6 (Eve): Laptop, Mouse
INSERT INTO Order_Products (order_id, product_id) VALUES
(6, 1),
(6, 2);