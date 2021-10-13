
CREATE TABLE IF NOT EXISTS public.btc_wallet_history (
      btcdatetime date NOT NULL,
      amount double precision NOT NULL);

CREATE INDEX idx_btcdatetime
    ON btc_wallet_history (btcdatetime);