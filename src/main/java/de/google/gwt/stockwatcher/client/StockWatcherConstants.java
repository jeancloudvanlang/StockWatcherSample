package de.google.gwt.stockwatcher.client;

import com.google.gwt.i18n.client.Constants;

public interface StockWatcherConstants extends Constants {
    @DefaultStringValue("stockwatcher")
    String stockWatcher();

    @DefaultStringValue("Symbol")
    String symbol();

    @DefaultStringValue("Price")
    String price();

    @DefaultStringValue("Change")
    String change();

    @DefaultStringValue("Remove")
    String remove();

    @DefaultStringValue("Add")
    String add();
}