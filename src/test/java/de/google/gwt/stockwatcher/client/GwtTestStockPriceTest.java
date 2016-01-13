package de.google.gwt.stockwatcher.client;

import com.google.gwt.junit.client.GWTTestCase;

public class GwtTestStockPriceTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "de.google.gwt.stockwatcher.StockWatcher";
    }

    public void testStockPriceCtor() {
        String symbol = "XYZ";
        double price = 70.0;
        double change = 2.0;
        double changePercent = 100.0 * change / price;

        StockPrice stockPrice = new StockPrice(symbol, price, change);

        assertNotNull(stockPrice);
        assertEquals(symbol, stockPrice.getSymbol());
        assertEquals(price, stockPrice.getPrice(), 0.001);
        assertEquals(change, stockPrice.getChange(), 0.001);
        assertEquals(changePercent, stockPrice.getChangePercent(), 0.001);
    }

}