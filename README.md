# Btc Billionaire

##

```
grpcurl -d '{"cartId":"cart1", "itemId":"socks", "quantity":3}' -plaintext 127.0.0.1:8101 shoppingcart.ShoppingCartService.AddItem

grpcurl -d '{"datetime":"2019-10-05T14:48:01+01:00",  "amount": ‌‌1.1‌‌}' -plaintext 127.0.0.1:8101 wallet.WalletService.AddBtc

```