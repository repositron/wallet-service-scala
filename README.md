# Btc Billionaire
This is based on the tutorial in https://developer.lightbend.com/docs/akka-platform-guide/microservices-tutorial/index.html
## Setup
1. docker-compose up -d

### create tables
1. docker exec -i wallet_service_postgres-db_1 psql -U btc-wallet -t < ddl-scripts/create_tables.sql
2. docker exec -i wallet_service_postgres-db_1 psql -U btc-wallet -t < ddl-scripts/create-history-tables.sql

### To run a node (in sbt)
sbt -Dconfig.resource=local1.conf run

### gGRP commands
```
grpcurl -d '{"datetime":"2019-10-05T14:48:01+01:00",  "amount":1.3}' -plaintext 127.0.0.1:8101 wallet.WalletService.AddBtc
grpcurl -d '{"datetimeFrom":"2019-10-04T14:48:01+01:00", "datetimeTo":"2020-10-06T14:48:01+01:00"}' -plaintext 127.0.0.1:8101 wallet.WalletService.BtcHistory

```

### Todo
wallet_service_postgres-db_1