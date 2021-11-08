package com.remie.activeviam.models;

import java.math.BigDecimal;
import java.util.List;

public class Trade {

    private final String symbol;
    private final List<BigDecimal> historicalPrices;

    public Trade(final String symbol, final List<BigDecimal> historicalPrices) {
        this.symbol = symbol;
        this.historicalPrices = historicalPrices;
    }

    /**
     *
     * @return - the trade's trading symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     *
     * @return - list of historical prices for this trade
     */
    public List<BigDecimal> getHistoricalPrices() {
        return historicalPrices;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "symbol='" + symbol + '\'' +
                ", historicalPrices=" + historicalPrices +
                '}';
    }

}
