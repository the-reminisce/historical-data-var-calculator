package tests;

import com.remie.activeviam.models.Trade;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TradeTest {

    private Trade trade;
    private static final String REGEX_PATTERN = "(\\w+)\\:\\[(.*)\\]";

    @BeforeEach
    void setup() {
        final String symbol = "F";
        final double[] prices = {8.2, 8.38, 8.33, 8.21, 8.54, 8.8, 8.75, 8.82, 8.82, 8.74, 8.86, 9.45, 9.08, 9.09, 9.08, 9.24, 9.2, 9.21, 9.34, 9.22, 9.25, 9.45, 9.12, 9.02, 8.91, 9.15, 9.04, 9.08, 8.95, 8.93, 8.79, 8.99, 8.86, 8.89, 8.82, 8.86, 8.79, 8.52, 8.65, 8.84, 9.06, 9, 9.3, 9.78, 9.78, 10.17, 9.83, 10.02, 10.86, 11.53, 11.52, 11.29, 11.19, 10.79, 10.72, 10.53, 10.83, 10.86, 11.2, 11.37, 11.51, 11.56, 11.93, 11.76, 11.45, 11.45, 11.54, 11.48, 11.43, 11.58, 11.7, 11.62, 12.27, 11.76, 11.7, 11.98, 12.55, 12.17, 11.93, 12.27, 12.65, 12.57, 12.91, 12.81, 13.37, 13.2, 12.49, 12.69, 12.49, 12.83, 12.85, 12.21, 12.14, 12.32, 12.3, 12.15, 12.46, 12.25, 12.17, 12.7, 12.92, 12.73, 12.51, 12.51, 12.38, 12.2, 12.24, 12.24, 12.23, 12.11, 11.45, 11.73, 11.94, 12.22, 12.27, 12.49, 12.43, 11.26, 11.54, 11.63, 11.41, 11.61, 11.74, 11.82, 11.71, 11.58, 11.33, 11.55, 11.84, 12.15, 12.14, 12.11, 12.49, 13.33, 13.06, 12.81, 13.9, 14.88, 14.53, 14.81, 14.91, 15.99, 15.97, 15.88, 15.63, 15.48, 15.11, 15.28, 14.87, 15, 15.02, 14.77, 14.52, 14.78, 14.91, 15.42, 15.26, 15.19, 14.96, 15.01, 14.86, 14.91, 14.93, 14.5, 14.23, 14.06, 14.48, 14.61, 14.42, 14.25, 14.01, 13.61, 13.28, 13.91, 14.19, 13.91, 13.82, 14.03, 13.79, 13.86, 14.39, 13.95, 13.91, 14.02, 13.32, 13.71, 13.8, 13.75, 13.82, 13.93, 13.9, 13.59, 13.46, 12.99, 13, 12.67, 12.57, 12.73, 13.08, 13.17, 12.9, 13.31, 13.05, 13.03, 13.11, 13.01, 12.89, 12.95, 13.03, 12.76, 12.68, 12.99, 12.86, 13.22, 13.4, 13.55, 12.82, 12.77, 13.23, 13.71, 13.78, 14.16, 14.31, 14.3, 14.16, 14.16, 14.35, 14.29, 14.12, 14.89, 15.12, 15.09, 15.64, 15.51, 15.45, 15.7, 15.56, 15.42, 16.040001, 16.549999, 16.280001, 16, 15.94, 15.51, 16.860001, 17.08, 17.950001, 18.01, 18.629999, 19.42, 19.290001};
        trade = new Trade(symbol, new ArrayList<>(doubleToBD(prices)));
    }

    @Test
    @DisplayName("Calculate VaR for a single Trade")
    void calculateVar() {
        final List<BigDecimal> historicalPrices = trade.getHistoricalPrices();

        assertEquals(251, historicalPrices.size(), "Historical prices were not parsed properly.");

        List<BigDecimal> pNL = new ArrayList<>();
        for (int i = 0; i < historicalPrices.size() - 1; i++) {
            double change = ((historicalPrices.get(i + 1).doubleValue() - historicalPrices.get(i).doubleValue()) / Math.abs(historicalPrices.get(i).doubleValue()));
            pNL.add(BigDecimal.valueOf(change));
        }

        assertEquals(250, pNL.size(), "Profit and loss list was not generated properly.");

        pNL = pNL.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());

        for (int i = 1; i < pNL.size(); i++) {
            assertTrue(pNL.get(0).doubleValue() < pNL.get(i).doubleValue(), "Profit and loss list is not in ascending order.");
        }

        final int confidenceLevel = 99;
        final int index = (int) Math.ceil(((100 - confidenceLevel) / 100.0) * pNL.size());

        assertEquals(3, index, "PNL return list index is incorrect.");
        assertEquals(-0.05378787878787872, pNL.get(index).doubleValue(), "Incorrect PNL value was chosen.");
        assertEquals(-5.378787878787872, pNL.get(index).doubleValue() * 100.0, "Incorrect VaR value was returned.");
    }

    @Test
    @DisplayName("Parse plain-text to trade data")
   void parseData() {
        final String testData = "GOOG:[316, 316.2, 317.5, 299.34]";
        assertTrue(testData.matches(REGEX_PATTERN), "Test historical data string is formatted improperly and cannot be parsed.");

        List<Trade> tradeList = historicalDataParser(testData);

        assertEquals(1, tradeList.size(), "Trade failed to parse from historical data.");

        Trade trade = tradeList.get(0);

        assertNotNull(trade, "Trade failed to parse from historical data.");

        assertEquals("GOOG", trade.getSymbol(), "Trade's symbol failed to parse from input data.");

        assertEquals(4, trade.getHistoricalPrices().size(), "Trade's historical prices failed to parse from input data.");

        BigDecimal[] values = { BigDecimal.valueOf(316), BigDecimal.valueOf(316.2), BigDecimal.valueOf(317.5), BigDecimal.valueOf(299.34) };

        for (int i = 0; i < trade.getHistoricalPrices().size(); i++) {
            assertEquals(values[i].doubleValue(), trade.getHistoricalPrices().get(i).doubleValue(), "Trade's historical prices failed to parse from input data, incorrect values.");
        }
   }

   private List<Trade> historicalDataParser(final String historical_data) {
       final List<Trade> tradeList = new ArrayList<>();
       final Matcher matcher = Pattern.compile(REGEX_PATTERN).matcher(historical_data);

       while (matcher.find()) {
           final String symbol = matcher.group(1);
           final List<BigDecimal> historicalPrices = new ArrayList<>();
           final String[] dataArray = matcher.group(2).split(", ");
           for (final String data : dataArray) {
               historicalPrices.add(BigDecimal.valueOf(Double.parseDouble(data)));
           }
           tradeList.add(new Trade(symbol, historicalPrices));
       }
       return tradeList;
   }

   private List<BigDecimal> doubleToBD(double[] values) {
       List<BigDecimal> list = new ArrayList<>();
       Arrays.stream(values).forEach((i) -> list.add(BigDecimal.valueOf(i)));
       return list;
   }

}
