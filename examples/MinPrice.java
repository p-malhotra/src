package examples;

public class MinPrice {
	
	  public int getMaxProfit(int[] stockPricesYesterday) {

		    // make sure we have at least 2 prices
		    if (stockPricesYesterday.length < 2) {
		        throw new IllegalArgumentException("Getting a profit requires at least 2 prices");
		    }

		    // we'll greedily update minPrice and maxProfit, so we initialize
		    // them to the first price and the first possible profit
		    int minPrice = stockPricesYesterday[0];
		    int maxProfit = stockPricesYesterday[1] - stockPricesYesterday[0];

		    // start at the second (index 1) time
		    // we can't sell at the first time, since we must buy first,
		    // and we can't buy and sell at the same time!
		    // if we started at index 0, we'd try to buy /and/ sell at time 0.
		    // this would give a profit of 0, which is a problem if our
		    // maxProfit is supposed to be /negative/--we'd return 0!
		    for (int i = 1; i < stockPricesYesterday.length; i++) {
		        int currentPrice = stockPricesYesterday[i];

		        // see what our profit would be if we bought at the
		        // min price and sold at the current price
		        int potentialProfit = currentPrice - minPrice;

		        // update maxProfit if we can do better
		        maxProfit = Math.max(maxProfit, potentialProfit);

		        // update minPrice so it's always
		        // the lowest price we've seen so far
		        minPrice = Math.min(minPrice, currentPrice);
		    }

		    return maxProfit;
		}
	  
	  public int getMaxProfits(int[] stockPricesYesterday) {

		    int minPrice = stockPricesYesterday[0];
		    int maxProfit = 0;

		    for (int currentPrice : stockPricesYesterday) {

		        // ensure min_price is the lowest price we've seen so far
		        minPrice = Math.min(minPrice, currentPrice);

		        // see what our profit would be if we bought at the
		        // min price and sold at the current price
		        int potentialProfit = currentPrice - minPrice;

		        // update maxProfit if we can do better
		        maxProfit = Math.max(maxProfit, potentialProfit);
		    }

		    return maxProfit;
		}

	  /**
	   * Recurrsive so run time is o(2ñ)
	   * @param n
	   * @return
	   */
	  public int fibRec(int n)
	  {
		  if(n==0 ||n ==1) return n;
		  return fibRec(n-1) + fibRec(n-2); 
	  }
	  
	  public int fibIterative(int n)
	  {
		  if(n==0 || n ==1 )
			  return n;
		  int prev =1;
		  int prevP = 0;
		  int sum=0;
		  for(int i =2;i<n;i++)
		  {
			  sum =prev+prevP;
			  prevP= prev;
			  prev=sum;
			  
		  }
		  sum = prev+prevP;

		  return sum;
	  }
	  public static void main(String[] args) {
		MinPrice minPrice= new MinPrice();
		System.out.println(minPrice.fibRec(6));
		System.out.println(minPrice.fibIterative(6));

	}
}
