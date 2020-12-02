![Maven release](https://github.com/balsmn/iota-utils-ext/workflows/Maven%20release/badge.svg?branch=master)
![Maven snapshot build](https://github.com/balsmn/iota-utils-ext/workflows/Maven%20snapshot%20publish/badge.svg?branch=master)

# IoTA Utility Extension Library

Developers wishing to use IoTA Blockchain end up learning a lot in depth in IoTA, which is a good thing.
At the same time, it is cubersome to build micro services based on IoTA and especially if one wishes to use the

Multisignature feature that IoTA provides. The IoTA API 1.0.0-beta9, offers primitive support for Multisignature transactions.
Compared to IoTA API's normal transactions, Multisignature transaction features has a lot of gap when it comes to developer
friendliness. This library aims to bridge the gap and provide an easy to use API on top of IoTA API and comes with Spring integration.

A microservice can simply add this library to its dependency and can start using it.

## Usage
### Stable Release
Include the following maven dependency in your microservice. The artifacts are available in Maven central.

```
<dependency>
    <groupId>io.github.balsmn</groupId>
    <artifactId>iota-utils-ext</artifactId>
    <version>${latest.version}</version>
</dependency>

### Snapshot Release
Include the following maven dependency and snapshot repository to your repository list in your microservice

```
<dependency>
    <groupId>io.github.balsmn</groupId>
    <artifactId>iota-utils-ext</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>

....
<repositories>
    <repository>
        <id>ossrh-snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    </repository>
</repositories>
```

Add the following to your spring application configurations
```
iota:
  config:
    host: nodes.devnet.iota.org
    seedSecurityLevel: 1
    testMode: true
```