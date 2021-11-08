package com.remie.activeviam.controllers;

import com.remie.activeviam.models.Trade;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@SessionAttributes({"title", "description"})
public class TradeController {

    private static final String REGEX_PATTERN = "(\\w+)\\:\\[(.*)\\]";

    /**
     * @param model
     * @return - our page template with attributes for single trade
     */
    @GetMapping(value = {"/single", ""})
    public String getSingleTrade(Model model) {
        model.addAttribute("title", "Single Trade VaR Calculator");
        model.addAttribute("description", "Please input a confidence level for the calculation and the historical data for a single trade.");
        return "base";
    }

    /**
     * @param model
     * @return - our page template with attributes for multiple trades
     */
    @GetMapping(value = {"/portfolio"})
    public String getPortfolioTrade(Model model) {
        model.addAttribute("title", "Trade Portfolio VaR Calculator");
        model.addAttribute("description", "Please input a confidence level for the calculation and the historical data for your portfolio of trades.");
        return "base";
    }

    /**
     * Post method which gets called when the calculate button in the form is pressed inside the single trade page
     *
     * @param model
     * @param confidence
     * @param historical_data
     * @param redirect_attributes
     * @param request
     * @return - redirects to the same page but with a flash message
     */
    @PostMapping(value = {"/single", ""})
    public String postSingleTrade(Model model, @RequestParam String confidence, @RequestParam String historical_data, RedirectAttributes redirect_attributes, HttpServletRequest request) {
        try {
            final List<Trade> tradeList = parseData(historical_data);
            final int confidenceLevel = Integer.parseInt(confidence);
            if (tradeList.isEmpty()) {
                redirect_attributes.addFlashAttribute("error", "There was an error processing data for the submitted trades.");
            } else {
                redirect_attributes.addFlashAttribute("success", "VaR: " + calculateVar(tradeList.get(0), confidenceLevel) + "%");
            }
        } catch (NumberFormatException e) {
            redirect_attributes.addFlashAttribute("error", "Invalid input for confidence level, could not calculate VaR.");
        } catch (Exception e) {
            redirect_attributes.addFlashAttribute("error", "There was an error processing data for the submitted trades.");
        }

        return "redirect:" + request.getRequestURI();
    }

    /**
     * Post method which gets called when the calculate button in the form is pressed inside the portfolio trade page
     *
     * @param model
     * @param confidence
     * @param historical_data
     * @param redirect_attributes
     * @param request
     * @return - redirects to the same page but with a flash message
     */
    @PostMapping(value = {"/portfolio"})
    public String postPortfolioTrade(Model model, @RequestParam String confidence, @RequestParam String historical_data, RedirectAttributes redirect_attributes, HttpServletRequest request) {
        try {
            final List<Trade> tradeList = parseData(historical_data);
            final int confidenceLevel = Integer.parseInt(confidence);
            if (tradeList.isEmpty()) {
                redirect_attributes.addFlashAttribute("error", "There was an error processing data for the submitted trades.");
            } else {
                double totalVar = 0.0;
                for (Trade trade : tradeList) {
                    totalVar += calculateVar(trade, confidenceLevel);
                }
                totalVar = totalVar / tradeList.size();
                redirect_attributes.addFlashAttribute("success", "VaR: " + totalVar + "%");
            }
        } catch (NumberFormatException e) {
            redirect_attributes.addFlashAttribute("error", "Invalid input for confidence level, could not calculate VaR.");
        } catch (Exception e) {
            redirect_attributes.addFlashAttribute("error", "There was an error processing data for the submitted trades.");
        }
        return "redirect:" + request.getRequestURI();
    }

    /**
     * Calculates the VaR of a trade given a specified confidence_level
     *
     * @param trade
     * @param confidence_level
     * @return - VaR as a double
     */
    private double calculateVar(final Trade trade, final int confidence_level) {
        final List<BigDecimal> historicalPrices = trade.getHistoricalPrices();

        List<BigDecimal> pNL = new ArrayList<>();
        for (int i = 0; i < historicalPrices.size() - 1; i++) {
            double change = ((historicalPrices.get(i + 1).doubleValue() - historicalPrices.get(i).doubleValue()) / Math.abs(historicalPrices.get(i).doubleValue()));
            pNL.add(BigDecimal.valueOf(change));
        }
        pNL = pNL.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        final int index = (int) Math.ceil(((100 - confidence_level) / 100.0) * pNL.size());
        return pNL.get(index).doubleValue() * 100.0;
    }

    /**
     * Creates a list of trades given a string of historical_data.
     * Uses regex pattern matching to gather the specified data from the string.
     *
     * @param historical_data
     * @return - list of trades from plain text
     */
    private List<Trade> parseData(final String historical_data) {
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

}
