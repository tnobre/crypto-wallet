# crypto-wallet

This project evaluates the current total of a crypto wallet using Coincap api. 

The multi thread behaviour is implemented using Thread Pool, ReentrantLock and Atomic Reference.

It can be run from the main method in the class Engine.

Update your wallet status in:

```
crypto-wallet-assets.csv
```

Change parameters if you need:

```
config.properties
```

Change test parameters if you need.

```
src/test/resources/config.properties
```

The tests are configured to run multiple times to ensure multi thread safety.

It uses Lombok. Make sure you configure annotation processing.