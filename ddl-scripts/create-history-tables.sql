
CREATE TABLE IF NOT EXISTS public.btc_wallet_history (
      datetime timestamp NOT NULL,
      amount double precision NOT NULL,
      PRIMARY KEY (datetime));