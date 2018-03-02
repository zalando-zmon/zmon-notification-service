# zmon-notification-service

[![Build Status](https://travis-ci.org/zalando-zmon/zmon-notification-service.svg?branch=master)](https://travis-ci.org/zalando-zmon/zmon-notification-service)
[![codecov.io](https://codecov.io/github/zalando-zmon/zmon-notification-service/coverage.svg?branch=master)](https://codecov.io/github/zalando-zmon/zmon-notification-service?branch=master)
[![OpenTracing Badge](https://img.shields.io/badge/OpenTracing-enabled-blue.svg)](http://opentracing.io)

Running locally enables the dry run mode by default. This disables most production services, namely:

- The TokenService supports a single shared key "DRY-RUN" valid for a long time
- PagerDuty Web Hook accepts calls but only logs what it would do;