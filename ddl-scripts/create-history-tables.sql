
CREATE TABLE IF NOT EXISTS public.btc_history (
      datetime timestamp NOT NULL,
      amount double NOT NULL,
      PRIMARY KEY (datetime));