package com.latitude.genoapay.codingchallenge.service;

import com.latitude.genoapay.codingchallenge.request.StockRequest;
import com.latitude.genoapay.codingchallenge.response.StockResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class StockService {

    private static final long MINUTE_MILLISECODS = 60000L;
    private static Logger logger = LogManager.getLogger(StockService.class);


    public boolean isValidRequest(StockRequest request) {
        return StringUtils.hasText(request.getIdentifier().trim()) && request.getEndDateTime() != null
                && request.getStartDateTime() != null && StringUtils.hasText(request.getStockPrices())
                && request.getStockPrices().split(",").length > 0;
    }

    public StockResponse getMaximumProfit(StockRequest request) throws Exception {
        Date startTime = request.getStartDateTime();
        Date endTime = request.getEndDateTime();
        String[] stockPriceStr = request.getStockPrices().split(",");
        if (stockPriceStr.length == 1) {
            throw new Exception("ERROR: Atleast 2 values of Stock Prices should be provided ");
        }

        int[] stockPricesArr = new int[stockPriceStr.length];
        for (int i = 0; i < stockPriceStr.length; i++) {
            try {
                stockPricesArr[i] = Integer.parseInt(stockPriceStr[i].trim());
            } catch (NumberFormatException e) {
                throw new Exception("ERROR: Data should have numbers only");
            }
        }

        Date dayStartTime = getDayStartTime(startTime);
        // If start time or end times are prior to day start time 10:00, the Index is assumed to be zero
        int startIdx = getDiffInMinutes(dayStartTime, startTime);
        int endIdx = getDiffInMinutes(dayStartTime, endTime);
        if (endIdx < startIdx) {
            throw new Exception("ERROR: End date cannot be before start date");
        } else if (startIdx > stockPricesArr.length) {
            throw new Exception("ERROR: Start and End Times are out of range");
        } else if (endIdx - startIdx < 2) {
            throw new Exception("ERROR: Time range should be atleast 2 mins");
        }
        if (endIdx >= stockPricesArr.length) {
            // If end time is greater than the Array of stock prices, end index will be the last element in Stock prices
            endIdx = stockPricesArr.length - 1;
        }

        int[] stockPrices = Arrays.copyOfRange(stockPricesArr, startIdx, endIdx + 1);

        int maxProfit = 0;
        int bestBuy = 0;
        int bestSell = 0;

        int profit;
        for (int i = 0; i < stockPrices.length; i++) {
            int buy = stockPrices[i];
            for (int j = i + 1; j < stockPrices.length; j++) {
                int sell = stockPrices[j];
                if (buy >= sell) {
                    continue;
                }
                profit = sell - buy;
                if (profit > maxProfit) {
                    maxProfit = profit;
                    bestBuy = buy;
                    bestSell = sell;
                }
            }
        }
        StockResponse response = new StockResponse(request, new Date(), maxProfit, bestBuy, bestSell);
        logger.debug("Profit : $" + maxProfit + " (Buy at $" + bestBuy + ", Sell at $" + bestSell + ")");
        return response;
    }

    Date getDayStartTime(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        c.set(Calendar.HOUR_OF_DAY, 10);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    int getDiffInMinutes(Date fromDate, Date toDate) {
        return (int) (Math.max(0, toDate.getTime() - fromDate.getTime()) / MINUTE_MILLISECODS);
    }
}
