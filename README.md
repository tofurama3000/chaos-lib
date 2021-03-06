# Chaos Lib

[![Build Status](https://travis-ci.org/tofurama3000/chaos-lib.svg?branch=master)](https://travis-ci.org/tofurama3000/chaos-lib)
[![CodeFactor](https://www.codefactor.io/repository/github/tofurama3000/chaos-lib/badge)](https://www.codefactor.io/repository/github/tofurama3000/chaos-lib)
[![codecov](https://codecov.io/gh/tofurama3000/chaos-lib/branch/master/graph/badge.svg)](https://codecov.io/gh/tofurama3000/chaos-lib)
[![Known Vulnerabilities](https://snyk.io/test/github/tofurama3000/chaos-lib/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/tofurama3000/chaos-lib?targetFile=pom.xml)

This is a small library to help with [chaos testing](https://boyter.org/2016/07/chaos-testing-engineering/). The basic idea behind chaos testing is to randomly introduce failures (e.g. timeouts, exceptions, etc.) to ensure that your system can properly handle failure.

The goal of this library is to provide a utility to help with chaos testing. To use it, you create your "good behavior" function and then several "bad behavior" functions, wrap them up in a ChaosRunner, and then call `run`. Then, in environments where you don't want chaos, you can disable chaos by calling `ChaosRunner.DisableChaos()`. Below is an example:

``` java
class Position {
  double x = 0;
  double y = 0;
}

...

Position p = new Position();
ChaosRunner<TestTarget, Double> chaos = new ChaosRunner<>(
  new ChaosFunction<>(pos -> pos.y += 1.0, 0.75), // Runs with 75% chance
  new ChaosFunction<>(new Function<TestTarget, Double>(){
      @Override
      public Double apply(TestTarget t) throws RuntimeException {
          throw new RuntimeException("Error!");
      }
  }, 0.25) // Runs with 25% chance
);

chaos.run(p); // Will throw an exception 25% of the time
```

In the above code, 75% of the time we'll just add 1 to the y-coordinate and 25% of the time we'll throw a Runtime error.

Each chaos function can be given a different probability of running. The ChaosRunner will be able to handle arbitrary ranges and adjust accordingly (the total probability does not have to add up to 1). For example, instead of using 0.75 and 0.25 in the above code, we could use 75 and 25 like so:

``` java
ChaosRunner<TestTarget, Double> chaos = new ChaosRunner<>(
  new ChaosFunction<>(pos -> pos.d += 1.0, 75), // Runs with 75% chance
  new ChaosFunction<>(new Function<TestTarget, Double>(){
      @Override
      public Double apply(TestTarget t) throws RuntimeException {
          throw new RuntimeException("Error!");
      }
  }, 25) // Runs with 25% chance
);
```

## Disabling Chaos

You may not want to have chaos running in your production environments but you want to have it in test environments. To handle that, there is a static function in ChaosRunner called `DisableGlobalChaos` that will atomically disable all chaos in the program. Simply call it like so:

``` java
ChaosRunner.DisableGlobalChaos();
```

Alternatively, you can disable chaos for just a specific chaos runner by calling the `disableChaos` function for that runner. Below is an example:


``` java
ChaosRunner<TestTarget, Double> chaos = new ChaosRunner<>(
  new ChaosFunction<>(pos -> pos.d += 1.0, 75), // Runs with 75% chance
  new ChaosFunction<>(new Function<TestTarget, Double>(){
      @Override
      public Double apply(TestTarget t) throws RuntimeException {
          throw new RuntimeException("Error!");
      }
  }, 25) // Runs with 25% chance
);
chaos.disableChaos();
```

## Note on Thread Safety

Changing whether or not chaos is enabled globally is thread-safe. However, instances ChaosRunner and ChaosFunction are **NOT** thread-safe currently. This is because ChaosRunners and ChaosFunctions are meant to be in local, isolated states and not in global or shared state. The idea is that they replace function calls with ChaosRunner.

Thread-safe versions are on the roadmap and will be in other classes.

## RoadMap

### Version 1.0.0
- [x] Disable/Enable chaos in a ChaosRunner in addition to globally
- [x] Allow force-running chaos in a ChaosRunner
- [x] Allow binary functions, non-consumers, suppliers

### Version 1.1.0
- [ ] Thread-safety
