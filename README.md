# Trading

TODO: 

Create Order objects probably while loading the watchlist.

Add notification feature.

Handle loss of internet connection.

Have some volume criteria in trade entries as well as just price action. Get the avg volume over some period and confirm that current volume is higher.

make a small gui for manual overriding
-update: Should probably just use TWS. Need to make sure that the program can distinguish between orders that it has sent and orders that I manually sent from TWS and then not do anything to mess them up. Basically if I override via TWS, the program should stop managing that trade until further notice etc.
-update: according to the “Execution” page in the reference guide, TWS orders have a fixed orderID of 0 so I can use this to distinguish them from platform generated orders. Also says TWS orders have fixed clientID of 0 so maybe I should be using 1 for platform client.

will need number of shares/contracts as field in watchlist. This means there will 
need to be a check in code to make sure I didn’t accidentally mix them up which could lead to a catastrophic over/under order. ex buy 1000 contracts instead of 10 because 10 contracts = 1000 shares etc.

May need to update trade.initOrder because I just made it so that the orders are initialized with a limit 0 price by default. There will need to be more work done at order placement time to update the price before it is sent.

Still need to initialize the map from order IDs to watchlist index (ticker IDs)
Need to get all open orders at startup and populate hash map accordingly. 
-Edit: All open orders come in through orderStatus at startup automatically. Just need to handle them.
-Update 7/2/16:
This is done now I think. hashMaps from orderID -> tickerID and orderID -> permID are created. orderID -> tickerID is filled right before sending orders and is used to update status of orders. orderID->permID is filled after first orderStatus call and this should be used to store permID for trades held overnight.

MAJOR: Still need to make it so that it requests data for the underlying stock for options trades as well as the option itself.

Need to look at changing the trade.initOrder method because it’s stupid. May be unnecessary layers and almost certainly has unnecessary code copying.

Maybe I should just read in the order.action from the watchList and then use the logic in trade.initOrder to double check that everything in the watchlist is logically consistent.

look into this message:
Error Code: 110, Error Msg: The price does not conform to the minimum price variation for this contract.
resolved 6/28: was sending order with LMT price = -1

added setMainOrderFields to IB API Order class. Need to create a list of all changes to IB API code in this file.

There are currently some places where I purposely exit the program if certain errors take place. These need to either be handled more gracefully or handled by a wrapper script. search for system.exit()

Need to set up requests and handling for all standard acct/portfolio stuff at startup. For example, request positions, store them in memory. Might need to redefine how watchlist is used (should it hold active trades as well?). Should I be updating the watchlist after a position becomes active?
-Update 7/2/16:
mostly done.

Note: copy and pasted the controller.Position class into the client package using Contracts rather than NewContracts

Note: a lot of the api requests result in a boatload of handler calls, and these are usually accompanied by an end message call of some sort (ex. accountDownloadEnded). Probably should be using this feature. Could make an array of booleans to show which msgs are currently active or something.

should make getter/setter for trade.contract since I have it as a field and also contained within trade.position. Then I can change that without changing the code that accesses the contract.
-Update 7/2/16
Done. Should do this for most fields at least in Trade class. set the fields private to find all usages because it will cause errors.

Maybe the map from orderID to tickerID should use permID instead of orderID so it is the same between sessions. Still need to initialize this map though. This would make it easier because then I can initialize the map using permIDs as the keys while I’m reading in the watchList. 
Or, do I really even need to be holding order data once the orders have cleared? Because then I have positions, rather than orders. So maybe I just need a list of open orders, but these will almost never be open for more than a few seconds at a time.
-Update 7/2/16
added two maps: orderID -> tickerID and orderID -> permID. Both are useful I think.

Need to re-initialize trade.order after successfully executing the entry. This will set it up for an exit order. 

probably should update the prices/PnL fields in the portfolio positions as well as in the trade class as new price data comes in.

Should undo all changes to IB API for portability, forwards compatibility, etc. See above for notes of all changes.

7/2/16: 
Pretty much finished handling account/portfolio data at connection as well as order entry and handling of orderStatus and execution updates. Still needs a little work but the basics are there.

Need to add the ability to send partial orders, particularly on exit. Right now trade.initOrder is used to set the quantity of the orders and they are always the same amount.

Known error: Still need to handle open orders at startup. Right now you get null pointer errors because the map orderIDtoTickerID isn’t correctly populated yet. This will need to be read in from watchlist file.