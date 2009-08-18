/*
 * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.jaqu;

import static org.h2.jaqu.Function.count;
import static org.h2.jaqu.Function.isNull;
import static org.h2.jaqu.Function.length;
import static org.h2.jaqu.Function.*;
import java.math.BigDecimal;
import java.util.List;
import org.h2.jaqu.Db;
import org.h2.jaqu.Filter;
import org.h2.test.TestBase;

/**
 * <p>
 * This is the implementation of the 101 LINQ Samples as described in
 * http://msdn2.microsoft.com/en-us/vcsharp/aa336760.aspx
 * </p><p>Why should you use JaQu?
 * Type checking,
 * autocomplete,
 * no separate SQL scripts,
 * no more SQL injection.</p>
 */
public class SamplesTest extends TestBase {
    /**
     * This object represents a database (actually a connection to the database).
     */
//## Java 1.5 begin ##
    Db db;
//## Java 1.5 end ##

    /**
     * This method is called when executing this application from the command
     * line.
     *
     * @param args the command line parameters
     */
    public static void main(String... args) {
        new SamplesTest().test();
    }

    public void test() {
//## Java 1.5 begin ##
        db = Db.open("jdbc:h2:mem:", "sa", "sa");
        db.insertAll(Product.getList());
        db.insertAll(Customer.getList());
        db.insertAll(Order.getList());
        db.insertAll(ComplexObject.getList());
        // TODO support JavaBeans specification
        // TODO support all relevant data types (byte[], ...)
        // TODO nested AND/OR, >, <, ...
        // TODO NOT
        // TODO +, -, *, /, ||, nested operations
        // TODO LIKE ESCAPE...
        // TODO UPDATE: FROM ... UPDATE?
        // TODO SELECT UNION
        // TODO DatabaseAdapter
        testComplexObject();
        testComplexObject2();
        testOrAndNot();
        testDelete();
        testIsNull();
        testLike();
        testMinMax();
        testSum();
        testLength();
        testCount();
        testGroup();
        testSelectManyCompoundFrom2();
        testWhereSimple4();
        testSelectSimple2();
        testAnonymousTypes3();
        testWhereSimple2();
        testWhereSimple3();
        testReverseColumns();
        db.close();
//## Java 1.5 end ##
    }

    /**
     * A simple test table. The columns are in a different order than in the
     * database.
     */
    public static class TestReverse {
        public String name;
        public Integer id;
    }

//## Java 1.5 begin ##
    private void testReverseColumns() {
        db.executeUpdate("create table TestReverse(id int, name varchar, additional varchar)");
        TestReverse t = new TestReverse();
        t.id = 10;
        t.name = "Hello";
        db.insert(t);
        TestReverse check = db.from(new TestReverse()).selectFirst();
        assertEquals(t.name, check.name);
        assertEquals(t.id, check.id);
    }
//## Java 1.5 end ##


//## Java 1.5 begin ##
    private void testWhereSimple2() {

//            var soldOutProducts =
//                from p in products
//                where p.UnitsInStock == 0
//                select p;

        Product p = new Product();
        List<Product> soldOutProducts =
            db.from(p).
            where(p.unitsInStock).is(0).
            orderBy(p.productId).select();

        assertEquals("[Chef Anton's Gumbo Mix: 0]", soldOutProducts.toString());
    }

    private void testWhereSimple3() {

//            var expensiveInStockProducts =
//                from p in products
//                where p.UnitsInStock > 0
//                && p.UnitPrice > 3.00M
//                select p;

        Product p = new Product();
        List<Product> expensiveInStockProducts =
            db.from(p).
            where(p.unitsInStock).bigger(0).
            and(p.unitPrice).bigger(30.0).
            orderBy(p.productId).select();

        assertEquals("[Northwoods Cranberry Sauce: 6, Mishi Kobe Niku: 29, Ikura: 31]",
                expensiveInStockProducts.toString());
    }


    private void testWhereSimple4() {

//        var waCustomers =
//            from c in customers
//            where c.Region == "WA"
//            select c;

        Customer c = new Customer();
        List<Customer> waCustomers =
            db.from(c).
            where(c.region).is("WA").
            select();

        assertEquals("[ALFKI, ANATR]", waCustomers.toString());
    }

    private void testSelectSimple2() {

//        var productNames =
//            from p in products
//            select p.ProductName;

        Product p = new Product();
        List<String> productNames =
            db.from(p).
            orderBy(p.productId).select(p.productName);

        List<Product> products = Product.getList();
        for (int i = 0; i < products.size(); i++) {
            assertEquals(products.get(i).productName, productNames.get(i));
        }
    }
//## Java 1.5 end ##

    /**
     * A result set class containing the product name and price.
     */
    public static class ProductPrice {
        public String productName;
        public String category;
        public Double price;
    }

//## Java 1.5 begin ##
    private void testAnonymousTypes3() {

//        var productInfos =
//            from p in products
//            select new {
//                p.ProductName,
//                p.Category,
//                Price = p.UnitPrice
//            };

        final Product p = new Product();
        List<ProductPrice> productInfos =
            db.from(p).orderBy(p.productId).
            select(new ProductPrice() { {
                    productName = p.productName;
                    category = p.category;
                    price = p.unitPrice;
            }});

        List<Product> products = Product.getList();
        assertEquals(products.size(), productInfos.size());
        for (int i = 0; i < products.size(); i++) {
            ProductPrice pr = productInfos.get(i);
            Product p2 = products.get(i);
            assertEquals(p2.productName, pr.productName);
            assertEquals(p2.category, pr.category);
            assertEquals(p2.unitPrice, pr.price);
        }
    }
//## Java 1.5 end ##

    /**
     * A result set class containing customer data and the order total.
     */
    public static class CustOrder {
        public String customerId;
        public Integer orderId;
        public BigDecimal total;
        public String toString() {
            return customerId + ":" + orderId + ":" + total;
        }
    }

//## Java 1.5 begin ##
    private void testSelectManyCompoundFrom2() {

//        var orders =
//            from c in customers,
//            o in c.Orders
//            where o.Total < 500.00M
//            select new {
//                c.CustomerID,
//                o.OrderID,
//                o.Total
//            };

        final Customer c = new Customer();
        final Order o = new Order();
        List<CustOrder> orders =
            db.from(c).
            innerJoin(o).on(c.customerId).is(o.customerId).
            where(o.total).smaller(new BigDecimal("100.00")).
            orderBy(1).
            select(new CustOrder() { {
                customerId = c.customerId;
                orderId = o.orderId;
                total = o.total;
            }});

        assertEquals("[ANATR:10308:88.80]", orders.toString());
    }

    private void testIsNull() {
        Product p = new Product();
        String sql = db.from(p).whereTrue(isNull(p.productName)).getSQL();
        assertEquals("SELECT * FROM Product WHERE (productName IS NULL)", sql);
    }

    private void testDelete() {
        Product p = new Product();
        int deleted = db.from(p).where(p.productName).like("A%").delete();
        assertEquals(1, deleted);
        deleted = db.from(p).delete();
        assertEquals(9, deleted);
        db.insertAll(Product.getList());
    }

    private void testOrAndNot() {
        Product p = new Product();
        String sql = db.from(p).whereTrue(not(isNull(p.productName))).getSQL();
        assertEquals("SELECT * FROM Product WHERE (NOT productName IS NULL)", sql);
        sql = db.from(p).whereTrue(not(isNull(p.productName))).getSQL();
        assertEquals("SELECT * FROM Product WHERE (NOT productName IS NULL)", sql);
        sql = db.from(p).whereTrue(db.test(p.productId).is(1)).getSQL();
        assertEquals("SELECT * FROM Product WHERE ((productId = ?))", sql);
    }

    private void testLength() {
        Product p = new Product();
        List<Integer> lengths =
            db.from(p).
            where(length(p.productName)).smaller(10).
            orderBy(1).
            selectDistinct(length(p.productName));
        assertEquals("[4, 5]", lengths.toString());
    }

    private void testSum() {
        Product p = new Product();
        Long sum = db.from(p).selectFirst(sum(p.unitsInStock));
        assertEquals(323, sum.intValue());
        Double sumPrice = db.from(p).selectFirst(sum(p.unitPrice));
        assertEquals(313.35, sumPrice.doubleValue());
    }

    private void testMinMax() {
        Product p = new Product();
        Integer min = db.from(p).selectFirst(min(p.unitsInStock));
        assertEquals(0, min.intValue());
        String minName = db.from(p).selectFirst(min(p.productName));
        assertEquals("Aniseed Syrup", minName);
        Double max = db.from(p).selectFirst(max(p.unitPrice));
        assertEquals(97.0, max.doubleValue());
    }

    private void testLike() {
        Product p = new Product();
        List<Product> aList = db.from(p).
            where(p.productName).like("Cha%").
            orderBy(p.productName).select();
        assertEquals("[Chai: 39, Chang: 17]", aList.toString());
    }

    private void testCount() {
        long count = db.from(new Product()).selectCount();
        assertEquals(10, count);
    }

    private void testComplexObject() {
        ComplexObject co = new ComplexObject();
        long count = db.from(co).
            where(co.id).is(1).
            and(co.amount).is(1L).
            and(co.birthday).smaller(new java.util.Date()).
            and(co.created).smaller(java.sql.Timestamp.valueOf("2005-05-05 05:05:05")).
            and(co.name).is("hello").
            and(co.time).smaller(java.sql.Time.valueOf("23:23:23")).
            and(co.value).is(new BigDecimal("1")).
            selectCount();
        assertEquals(1, count);
    }

    private void testComplexObject2() {
        testComplexObject2(1, "hello");
    }

private void testComplexObject2(final int x, final String name) {
    final ComplexObject co = new ComplexObject();
    long count = db.from(co).
        where(new Filter() { public boolean where() {
            return co.id == x
                && co.amount == 1L
                && co.birthday.before(new java.util.Date())
                && co.created.before(java.sql.Timestamp.valueOf("2005-05-05 05:05:05"))
                && co.name.equals(name)
                && co.time.before(java.sql.Time.valueOf("23:23:23"))
                && co.value == new BigDecimal("1");
        } }).selectCount();
        // TODO should return only one object
        assertEquals(2, count);
    }

//## Java 1.5 end ##

    /**
     * A result set class containing product groups.
     */
    public static class ProductGroup {
        public String category;
        public Long productCount;
        public String toString() {
            return category + ":" + productCount;
        }
    }

//## Java 1.5 begin ##
    private void testGroup() {

//      var orderGroups =
//          from p in products
//          group p by p.Category into g
//          select new {
//                Category = g.Key,
//                Products = g
//          };

        final Product p = new Product();
        List<ProductGroup> list =
            db.from(p).
            groupBy(p.category).
            orderBy(1).
            select(new ProductGroup() { {
                category = p.category;
                productCount = count();
            }});

        assertEquals("[Beverages:2, Condiments:5, " +
                "Meat/Poultry:1, Produce:1, Seafood:1]",
                list.toString());
    }

//## Java 1.5 end ##
}
