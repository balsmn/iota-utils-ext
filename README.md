# IoTA Utility Extension Library

Developers wishing to use IoTA Blockchain end up learning a lot in depth in IoTA, which is a good thing.
At the same time, it is cubersome to build micro services based on IoTA and especially if one wishes to use the
Multisignature feature that IoTA provides. The IoTA API 1.0.0-beta9, offers primitive support for Multisignature transactions.
Compared to IoTA API's normal transactions, Multisignature transaction features has a lot of gap when it comes to developer
friendliness. This library aims to bridge the gap and provide an easy to use API on top of IoTA API and comes with Spring integration.

A microservice can simply add this library to its dependency and can start using it.

## Usage
Include the following maven dependency to your microservice

```
<dependency>
    <groupId>util.iota</groupId>
    <artifactId>iota-utils-ext</artifactId>
    <packaging>jar</packaging>
    <version>0.1-SNAPSHOT</version>
</dependency>
```

Add the following to your spring application configurations
```
iota:
  config:
    host: nodes.devnet.iota.org
    seedSecurityLevel: 1
    testMode: true
```