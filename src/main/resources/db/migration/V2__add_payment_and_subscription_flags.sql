ALTER TABLE policy_request
  ADD COLUMN payment_confirmed_at TIMESTAMP WITH TIME ZONE NULL,
  ADD COLUMN subscription_authorized_at TIMESTAMP WITH TIME ZONE NULL;

