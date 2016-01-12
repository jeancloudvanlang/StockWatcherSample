package de.google.gwt.stockwatcher.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class StockWatcher implements EntryPoint {

    private static final int REFRESH_INTERVAL = 5000;
    private static final String JSON_URL = GWT.getModuleBaseURL() + "stockPrices?q=";
    private VerticalPanel mainPanel = new VerticalPanel();
    private FlexTable stocksFlexTable = new FlexTable();
    private HorizontalPanel addPanel = new HorizontalPanel();
    private TextBox newSymbolTextBox = new TextBox();
    private Button addStockButton;
    private Label lastUpdatedLabel = new Label();
    private ArrayList<String> stocks = new ArrayList<>();
    private StockWatcherConstants constants = GWT.create(StockWatcherConstants.class);
    private StockWatcherMessages messages = GWT.create(StockWatcherMessages.class);
    private Label errorMsgLabel = new Label();

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        // Set the window title, the header text, and the Add button text.
        Window.setTitle(constants.stockWatcher());
        RootPanel.get("appTitle").add(new Label(constants.stockWatcher()));
        addStockButton = new Button(constants.add());

        // Create table for stock data.
        stocksFlexTable.setText(0, 0, constants.symbol());
        stocksFlexTable.setText(0, 1, constants.price());
        stocksFlexTable.setText(0, 2, constants.change());
        stocksFlexTable.setText(0, 3, constants.remove());

        // Add styles to elements in the stock list table.
        stocksFlexTable.getRowFormatter().addStyleName(0, "watchListHeader");
        stocksFlexTable.addStyleName("watchList");
        stocksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
        stocksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
        stocksFlexTable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");

        // Add styles to elements in the stock list table.
        stocksFlexTable.setCellPadding(6);

        // Assemble Add Stock panel.
        addPanel.add(newSymbolTextBox);
        addPanel.add(addStockButton);
        addPanel.addStyleName("addPanel");

        // Assemble Main panel.
        errorMsgLabel.setStyleName("errorMessage");
        errorMsgLabel.setVisible(false);

        // Assemble Main panel.
        mainPanel.add(errorMsgLabel);
        mainPanel.add(stocksFlexTable);
        mainPanel.add(addPanel);
        mainPanel.add(lastUpdatedLabel);

        // Associate the Main panel with the HTML host page.
        RootPanel.get("stockList").add(mainPanel);

        // Move cursor focus to the input box.
        newSymbolTextBox.setFocus(true);


        // Listen for mouse events on the Add button.
        // Should be replaced in a big project by an non anonymous interface implementation
        // (see http://www.gwtproject.org/doc/latest/DevGuideUiHandlers.html)
        addStockButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                addStock();
            }
        });

        // Listen for keyboard events in the input box.
        newSymbolTextBox.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    addStock();
                }
            }
        });

        // Setup timer to refresh list automatically
        Timer refreshTimer = new Timer() {
            @Override
            public void run() {
                refreshWatchList();
            }
        };
        refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
    }


    /**
     * Add stock to FlexTable. Executed when the user clicks the addStockButton or
     * presses enter in the newSymbolTextBox.
     */
    private void addStock() {
        final String symbol = newSymbolTextBox.getText().toUpperCase().trim();
        newSymbolTextBox.setFocus(true);

        // Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
        if (!symbol.matches("^[0-9A-Z\\.]{1,10}$")) {
            Window.alert("'" + symbol + "' is not a valid symbol.");
            newSymbolTextBox.selectAll();
            return;
        }

        newSymbolTextBox.setText("");

        // Don't add the stock if it's already in the table.
        if (stocks.contains(symbol)) {
            return;
        }
        // Add the stock to the table
        int row = stocksFlexTable.getRowCount();
        stocks.add(symbol);
        stocksFlexTable.setText(row, 0, symbol);
        stocksFlexTable.setWidget(row, 2, new Label());
        // style the rows
        stocksFlexTable.getCellFormatter().addStyleName(row, 1, "watchListNumericColumn");
        stocksFlexTable.getCellFormatter().addStyleName(row, 2, "watchListNumericColumn");
        stocksFlexTable.getCellFormatter().addStyleName(row, 3, "watchListRemoveColumn");

        // Add a button to remove this stock from the table.
        Button removeStockButton = new Button("X");
        removeStockButton.addStyleDependentName("remove");

        removeStockButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int removeIndex = stocks.indexOf(symbol);
                stocks.remove(removeIndex);
                stocksFlexTable.removeRow(removeIndex + 1);
            }
        });
        stocksFlexTable.setWidget(row, 3, removeStockButton);

        // Get the stock price.
        refreshWatchList();
    }

    private void refreshWatchList() {
        if (stocks.size() == 0) {
            return;
        }

        String url = JSON_URL;

        // Append watch list stock symbols to query URL.
        Iterator<String> iter = stocks.iterator();
        while (iter.hasNext()) {
            url += iter.next();
            if (iter.hasNext()) {
                url += "+";
            }
        }

        url = URL.encode(url);

        // Send request to server and handle errors.
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

        try {
            Request request = builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    displayError("Could not retrieve Json");
                }

                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        updateTable(JsonUtils.<JsArray<StockData>>safeEval(response.getText()));
                    } else {
                        displayError("Could not retrieve Json (" + response.getStatusText() + ")");
                    }
                }
            });
        } catch (RequestException e) {
            displayError("Could not retrieve Json");
        }

    }

    private void updateTable(JsArray<StockData> prices) {
        for (int i = 0; i < prices.length(); i++) {
            updateTable(prices.get(i));
        }

        // Display timestamp showing last refresh.
        DateTimeFormat dateFormat = DateTimeFormat.getFormat(
                DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);

        lastUpdatedLabel.setText(messages.lastUpdate(new Date())
                + dateFormat.format(new Date()));

        // Clear any errors.
        errorMsgLabel.setVisible(false);
    }

    private void updateTable(StockData price) {
        // Make sure the stock is still in the stock table.
        if (!stocks.contains(price.getSymbol())) {
            return;
        }

        int row = stocks.indexOf(price.getSymbol()) + 1;

        // Format the data in the Price and Change fields.
        String priceText = NumberFormat.getFormat("#,##0.00").format(price.getPrice());
        NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
        String changeText = changeFormat.format(price.getChange());
        String changePercentText = changeFormat.format(price.getChangePercent());

        // Populate the Price and Change fields with new data.
        stocksFlexTable.setText(row, 1, priceText);
        Label changeWidget = (Label) stocksFlexTable.getWidget(row, 2);
        changeWidget.setText(changeText + " (" + changePercentText + "%)");

        // Change the color of text in the Change field based on its value.
        String changeStyleName = "noChange";
        if (price.getChangePercent() < -0.1f) {
            changeStyleName = "negativeChange";
        } else if (price.getChangePercent() > 0.1f) {
            changeStyleName = "positiveChange";
        }

        changeWidget.setStyleName(changeStyleName);
    }

    /**
     * If can't get JSON, display error message.
     *
     * @param error The error message.
     */
    private void displayError(String error) {
        errorMsgLabel.setText("Error: " + error);
        errorMsgLabel.setVisible(true);
    }
}
