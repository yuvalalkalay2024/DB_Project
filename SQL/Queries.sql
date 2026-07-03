-- 1. הקונה המגוון (קנה לפחות מוצר אחד מכל קטגוריה - Relational Division)
SELECT u.username
FROM Users u
JOIN Orders o ON u.user_id = o.buyer_id
JOIN Order_Products op ON o.order_id = op.order_id
JOIN Products p ON op.product_id = p.product_id
WHERE u.role = 'BUYER'
GROUP BY u.user_id, u.username
HAVING COUNT(DISTINCT p.category) = 4;

-- 2. לקוחות עצלים (משתמשים רשומים ללא שום פעילות - Set Operations / EXCEPT)
SELECT username FROM Users WHERE role = 'BUYER'
EXCEPT
(SELECT u.username FROM Users u JOIN Orders o ON u.user_id = o.buyer_id
 UNION
 SELECT u.username FROM Users u JOIN Cart_Products cp ON u.user_id = cp.buyer_id);


-- 3. המוכר הרווחי ביותר (Top Seller)
SELECT u.username, SUM((p.price + COALESCE(sp.extra_pay, 0)) * op.quantity) as total_revenue
FROM Users u
JOIN Products p ON u.user_id = p.seller_id
JOIN Order_Products op ON p.product_id = op.product_id
LEFT JOIN Special_Products sp ON p.product_id = sp.product_id
WHERE u.role = 'SELLER'
GROUP BY u.user_id, u.username
ORDER BY total_revenue DESC LIMIT 1;

-- 4. מוכר עם הקטלוג הכי מגוון
SELECT u.username, COUNT(DISTINCT p.category) as categories_covered
FROM Users u
JOIN Products p ON u.user_id = p.seller_id
WHERE u.role = 'SELLER'
GROUP BY u.user_id, u.username
ORDER BY categories_covered DESC LIMIT 1;

-- 5. מוכרים רדומים (לא נקנה מהם כלום - LEFT JOIN with IS NULL)
SELECT u.username
FROM Users u
JOIN Products p ON u.user_id = p.seller_id
LEFT JOIN Order_Products op ON p.product_id = op.product_id
WHERE u.role = 'SELLER' AND op.product_id IS NULL
GROUP BY u.user_id, u.username;

-- 6. המוצרים הכי נמכרים
SELECT p.name, p.category, SUM(op.quantity) as total_sold
FROM Products p
JOIN Order_Products op ON p.product_id = op.product_id
GROUP BY p.product_id, p.name, p.category
ORDER BY total_sold DESC LIMIT 3;

-- 7. סך מכירות פר מוכר (Total Sales Per Seller)
SELECT u.username, COALESCE(SUM((p.price + COALESCE(sp.extra_pay, 0)) * op.quantity), 0) as total_revenue
FROM Users u
LEFT JOIN Products p ON u.user_id = p.seller_id
LEFT JOIN Order_Products op ON p.product_id = op.product_id
LEFT JOIN Special_Products sp ON p.product_id = sp.product_id
WHERE u.role = 'SELLER'
GROUP BY u.user_id, u.username
ORDER BY total_revenue DESC;

-- 8. עגלות נטושות לעומת קניות (במקום סטוק נמוך - דורש HAVING ו-COALESCE)
SELECT p.name, COALESCE(SUM(cp.quantity), 0) as in_carts, COALESCE(SUM(op.quantity), 0) as sold
FROM Products p
LEFT JOIN Cart_Products cp ON p.product_id = cp.product_id
LEFT JOIN Order_Products op ON p.product_id = op.product_id
GROUP BY p.product_id, p.name
HAVING COALESCE(SUM(cp.quantity), 0) > COALESCE(SUM(op.quantity), 0);

-- 9. התפלגות הכנסות לפי קטגוריה
SELECT p.category, SUM((p.price + COALESCE(sp.extra_pay,0)) * op.quantity) as category_revenue
FROM Products p
JOIN Order_Products op ON p.product_id = op.product_id
LEFT JOIN Special_Products sp ON p.product_id = sp.product_id
GROUP BY p.category
ORDER BY category_revenue DESC;

-- 10. הזמנה מעל הממוצע הכללי (Whale Orders - Subquery בתוך HAVING)
SELECT (o.order_id, u.username, SUM((p.price + COALESCE(sp.extra_pay,0)) * op.quantity) as order_total
FROM Orders o
JOIN Users u ON o.buyer_id = u.user_id
JOIN Order_Products op ON o.order_id = op.order_id
JOIN Products p ON op.product_id = p.product_id
LEFT JOIN Special_Products sp ON p.product_id = sp.product_id
GROUP BY o.order_id, u.username
HAVING SUM((p.price + COALESCE(sp.extra_pay,0)) * op.quantity) > 
    SELECT COALESCE(SUM((p2.price + COALESCE(sp2.extra_pay,0)) * op2.quantity) / NULLIF(COUNT(DISTINCT o2.order_id), 0), 0)
    FROM Orders o2
    JOIN Order_Products op2 ON o2.order_id = op2.order_id
    JOIN Products p2 ON op2.product_id = p2.product_id
    LEFT JOIN Special_Products sp2 ON p2.product_id = sp2.product_id
)
ORDER BY order_total DESC;

