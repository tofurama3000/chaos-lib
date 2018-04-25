# Chaos Lib

This is a small library to help with [chaos testing](https://boyter.org/2016/07/chaos-testing-engineering/). The basic idea behind chaos testing is to randomly introduce failures (e.g. timeouts, exceptions, etc.) to ensure that your system can properly handle failure.

The goal of this library is to provide a utility to help with chaos testing. To use it, you create your "good behavior" function and then several "bad behavior" functions, wrap them up in a ChaosRunner, and then call `run`. Then, in environments where you don't want chaos, you can disable chaos by calling `ChaosRunner.DisableChaos()`. Below is an example:

``` java
class Position {
  double x = 0;
  double y = 0;
}

...

Position p = new Position();
ChaosRunner<TestTarget> chaos = new ChaosRunner<>(
  new ChaosFunction<>(pos -> pos.y += 1, 0.75), // Runs with 75% chance
  new ChaosFunction<>(pos -> throw new Exception("Error!"), 0.25) // Runs with 25% chance
);

chaos.run(p); // Will throw an exception 25% of the time
```

Each chaos function can be given a different probability of running. The ChaosRunner will be able to handle arbitrary ranges and adjust accordingly (the total probability does not have to add up to 1). For example, instead of using 0.75 and 0.25 in the above code, we could use 75 and 25 like so:

``` java
ChaosRunner<TestTarget> chaos = new ChaosRunner<>(
  new ChaosFunction<>(pos -> pos.y += 1, 75), // Runs with 75% chance
  new ChaosFunction<>(pos -> throw new Exception("Error!"), 25) // Runs with 25% chance
);
```

## Disabling Chaos

You may not want to have chaos running in your production environments but you want to have it in test environments. To handle that, there is a static function in ChaosRunner called `DisableGlobalChaos` that will atomically disable all chaos in the program. Simply call it like so:

``` java
ChaosRunner.DisableGlobalChaos();
```
