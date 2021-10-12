# Btc Billionaire

## Setup
1. docker-compose up -d

### node 
sbt -Dconfig.resource=local1.conf run

wallet_service_postgres-db_1

### create tables
1. docker exec -i wallet_service_postgres-db_1 psql -U btc-wallet -t < ddl-scripts/create_tables.sql
2. docker exec -i wallet_service_postgres-db_1 psql -U btc-wallet -t < ddl-scripts/create-history-tables.sql

```

grpcurl -d '{"datetime":"2019-10-05T14:48:01+01:00",  "amount": ‌‌1.1‌‌}' -plaintext 127.0.0.1:8101 wallet.WalletService.AddBtc

 sbt -Dconfig.resource=local1.conf run

```

### Todo
1. handle time zones