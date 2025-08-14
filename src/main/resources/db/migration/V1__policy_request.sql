CREATE TABLE IF NOT EXISTS policy_request (
  id UUID PRIMARY KEY,
  customer_id UUID NOT NULL,
  product_id UUID NOT NULL,
  category VARCHAR(32) NOT NULL,
  payment_method VARCHAR(32) NOT NULL,
  sales_channel VARCHAR(32) NOT NULL,
  total_monthly_premium_amount NUMERIC(19,2) NOT NULL,
  insured_amount NUMERIC(19,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  finished_at TIMESTAMP WITH TIME ZONE NULL,
  version BIGINT
);

CREATE TABLE IF NOT EXISTS policy_coverages (
  policy_id UUID NOT NULL REFERENCES policy_request(id) ON DELETE CASCADE,
  name VARCHAR(128) NOT NULL,
  amount NUMERIC(19,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS policy_assistances (
  policy_id UUID NOT NULL REFERENCES policy_request(id) ON DELETE CASCADE,
  value VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS policy_history (
  policy_id UUID NOT NULL REFERENCES policy_request(id) ON DELETE CASCADE,
  status VARCHAR(32) NOT NULL,
  timestamp TIMESTAMP WITH TIME ZONE NOT NULL
);
