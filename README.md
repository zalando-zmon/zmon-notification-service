# zmon-notification-service

Running locally enables the dry run mode by default. This disables most production services, namely:

- The TokenService supports a single shared key "DRY-RUN" valid for a long time
- PagerDuty Web Hook accepts calls but only logs what it would do;