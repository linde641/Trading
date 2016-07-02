# Trading

TOOD: 

Create Order objects probably while loading the watchlist.

Add notification feature.

Handle loss of internet connection.

Have some volume criteria in trade entries as well as just price action. Get the avg volume over some period and confirm that current volume is higher.

make a small gui for manual overriding
-update: Should probably just use TWS. Need to make sure that the program can distinguish between orders that it has sent and orders that I manually sent from TWS and then not do anything to mess them up. Basically if I override via TWS, the program should stop managing that trade until further notice etc.

will need number of shares/contracts as field in watchlist. This means there will 
need to be a check in code to make sure I didn’t accidentally mix them up which could lead to a catastrophic over/under order. ex buy 1000 contracts instead of 10 because 10 contracts = 1000 shares etc.

May need to update trade.initOrder because I just made it so that the orders are initialized with a limit 0 price by default. There will need to be more work done at order placement time to update the price before it is sent.

Still need to initialize the map from order IDs to watchlist index (ticker IDs)
Need to get all open orders at startup and populate hash map accordingly. 
-Edit: All open orders come in through orderStatus at startup automatically. Just need to handle them.

MAJOR: Still need to make it so that it requests data for the underlying stock for options trades as well as the option itself.

Need to look at changing the trade.initOrder method because it’s stupid. May be unnecessary layers and almost certainly has unnecessary code copying.

Maybe I should just read in the order.action from the watchList and then use the logic in trade.initOrder to double check that everything in the watchlist is logically consistent.

look into this message:
Error Code: 110, Error Msg: The price does not conform to the minimum price variation for this contract.
resolved 6/28: was sending order with LMT price = -1

added setMainOrderFields to IB API Order class. Need to create a list of all changes to IB API code in this file.

There are currently some places where I purposely exit the program if certain errors take place. These need to either be handled more gracefully or handled by a wrapper script. search for system.exit()

Need to set up requests and handling for all standard acct/portfolio stuff at startup. For example, request positions, store them in memory. Might need to redefine how watchlist is used (should it hold active trades as well?). Should I be updating the watchlist after a position becomes active?

Note: copy and pasted the controller.Position class into the client package using Contracts rather than NewContracts