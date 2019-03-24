# Examples

All examples skip the use of a SMTP proxy, or Application which sends SMTP based messages. These examples also serve as integration tests to prove interoperability between the default SMTP sampler and this project.

Using Maven plugin `jmeter-maven-plugin`, the examples automatically download a version of JMeter, install the SMTP server plugin, and execute a predefined scenario.

All examples are configured to run headless via:
```bash
mvn verify
```

To edit an example in the usual JMeter UI, with automatic JMeter download and plugin installation, run:
```bash
mvn jmeter:gui
```

## Example Overview

- Simple

  - Simulates a load test targeting message throughput per hour.

- Auth

  - As __Simple__ with SMTP Auth enabled
  - All clients send valid credentials

- SSL

  - As __Simple__ with SSL transport
  - Uses and ignores self signed certs

- STARTTLS

  - As __Simple__ with transport upgraded to SSL
  - Uses and ignores self signed certs
