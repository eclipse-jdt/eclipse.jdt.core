m_logger.info(
  this,
  "Subscription to ["
    + subDetails.getGpi()
    + "] ["
    + subDetails.getCombinationType()
    + "] sent to "
    + m_exchange
    + " OBS (subType="
    + subDetails.getSubscriptionType()
    + " subSubType="
    + subDetails.getSubscriptionSubType()
    + " subId="
    + subDetails.getSubscriptionId()
    + ")");