# Trading

TOOD: 

Create Order objects probably while loading the watchlist.

Add notification feature.

Handle loss of internet connection.

Have some volume criteria in trade entries as well as just price action. Get the avg volume over some period and confirm that current volume is higher.

make a small gui for manual overriding

will need number of shares/contracts as field in watchlist. This means there will 
need to be a check in code to make sure I didn’t accidentally mix them up which could lead to a catastrophic over/under order. ex buy 1000 contracts instead of 10 because 10 contracts = 1000 shares etc.

May need to update trade.initOrder because I just made it so that the orders are initialized with a limit 0 price by default. There will need to be more work done at order placement time to update the price before it is sent.

Still need to initialize the map from order IDs to watchlist index (ticker IDs)

MAJOR: Still need to make it so that it requests data for the underlying stock for options trades as well as the option itself.

Need to look at changing the trade.initOrder method because it’s stupid. May be unnecessary layers and almost certainly has unnecessary code copying.

look into this message:
Error Code: 110, Error Msg: The price does not conform to the minimum price variation for this contract.

added setMainOrderFields to IB API Order class. Need to create a list of all changes to IB API code in this file.