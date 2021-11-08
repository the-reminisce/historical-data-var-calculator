# historical-data-var-calculator

A small program designed to calculate the VaR(Value at Risk) of trades using historical data. The program is meant specifically to not use any methods such as variance or covariance etc. and only historical data.

The program rounds up when calculating the VaR as it was recommended by multiple different articles.

#Instructions
  - Run the springboot application
  - Access the application via http://localhost:8080/
  - Input a confidence level from 1-100 (common values are: 99, 97 95)
  - Input your historical data which can either be simlated or based on an actual stock.
    - Input must be formatted in the following manner: 'SYMBOL:[(COMMA SEPERATED VALUES)]' e.g. 'GOOG:[316, 316.2, 317.5, 299.34]'
      - An example input file is inside the base directory named 'sample-data.txt'
    - Multiple lines trades with different symbols can be inputted as well.
  - Press the calculate button to see the result.

#Testing

JUnit was used for the testing and accuracy of the functions in this application. 

#Methodology
1. Take in all of the historical values of a stock and calculate the Profit & Loss(PNL's) values for that stock. This can be done by iterating through these values and finding the percent change between day n and day n+1.
2. Sort the list of PNL's in ascending order
3. Attribute indices to our PNL's and use our confidence level to find our VaR. e.g. If we have a confidence level of 95% we  take, 100% - 95% = 5%. 5% times our data points(in this example 250) = 0.05 * 250 = 12.5 -> Round up to get 13.
4. We take our index (13) and return the PNL at that index to get our VaR.

#Difficulties (This is also my an answer for the question asked #2)
When calculating the VaR of a portfolio (a group of trades) it is difficult to obtain an accurate value as certain trades do not move together uniformly. Thus the need to calculate the covariance between the trades. As a result I opted with adding the VaR's of a portfolio and dividing it by the number of trades in the portfolio to come up with the 'VaR' of a portfolio. 

#Dependencies
  - Java Springboot
  - Maven
  - Thymeleaf
  - JUnit 5.8.1
  - Boostrap 5

![image](https://user-images.githubusercontent.com/53870047/140805442-82c9f8ce-024f-4c01-904a-7f5ee2d3c3bb.png)
