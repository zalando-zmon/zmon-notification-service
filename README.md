ZMON source code on GitHub is no longer in active development. Zalando will no longer actively review issues or merge pull-requests.

ZMON is still being used at Zalando and serves us well for many purposes. We are now deeper into our observability journey and understand better that we need other telemetry sources and tools to elevate our understanding of the systems we operate. We support the [OpenTelemetry](https://opentelemetry.io/) initiative and recommended others starting their journey to begin there.

If members of the community are interested in continuing developing ZMON, consider forking it. Please review the licence before you do.

# zmon-notification-service

[![Build Status](https://travis-ci.org/zalando-zmon/zmon-notification-service.svg?branch=master)](https://travis-ci.org/zalando-zmon/zmon-notification-service)
[![codecov.io](https://codecov.io/github/zalando-zmon/zmon-notification-service/coverage.svg?branch=master)](https://codecov.io/github/zalando-zmon/zmon-notification-service?branch=master)
[![OpenTracing Badge](https://img.shields.io/badge/OpenTracing-enabled-blue.svg)](http://opentracing.io)

Running locally enables the dry run mode by default. This disables most production services, namely:

- The TokenService supports a single shared key "DRY-RUN" valid for a long time
- PagerDuty Web Hook accepts calls but only logs what it would do;
