# Asynchronous interdependent tasks

This repo has code demonstrating two different approaches to executing
tasks asynchronously when the some tasks depend on the completion of
others. The example here is of making a pizza using the following tasks:
1. Combining the ingredients of the dough.
1. Letting the dough rise.
1. Combining the ingredients of the sauce.
1. Grating the cheese.
1. Rolling out the risen dough into a crust.
1. Putting the sauce on top of the crust.
1. Putting the grated cheese on top of the sauce.

The DAG of dependencies looks like this:
```
1 --> 2 --> 5 --\
                +--> 6 --\
3 --------------/        +--> 7
4 -----------------------/
```
The [FuturePizzaBuilder](https://github.com/Tembrel/eg4jb/blob/master/src/pizza/FuturePizzaBuilder.java)
approach uses
[CompletableFutures](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html)
to arrange for
the tasks to be performed in an order consistent with this DAG.

The [LatchPizzaBuilder](https://github.com/Tembrel/eg4jb/blob/master/src/pizza/LatchPizzaBuilder.java)
approach uses
[CountDownLatches](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CountDownLatch.html)
to prevent tasks from
proceeding until the tasks they depend on have completed.
Unlike the future-based approach, this class uses fields to hold
intermediate state.
Because these fields are written before and read after calls to corresponding
high-level synchronizer methods (`write -> countDown` and `await -> read`),
there is no need for further synchronization, nor do the fields need to be
volatile.
This implementation reflects a more restrictive dependency graph than the one shown above:
It waits for 3, 4, and 5 to be ready before performing 6 and 7 together.
```
1 --> 2 --> 5 --\
3 --------------+--> 6, 7
4 --------------/
```
Both cases use a fixed-size thread pool to run tasks asynchronously.

The `CompletableFuture`-based version shows the input of each task on starting
and the output of that task on finishing.
The `CountDownLatch`-based version shows the input of each task when
starting and finishing.
The reason for the difference has to do with making the structures
of these programs similar, and does not reflect an inherent limitation
of the latter approach.	

The [PizzaDemo](https://github.com/Tembrel/eg4jb/blob/master/src/pizza/PizzaDemo.java)
class runs both versions.

## Other possibilities

Many other approaches are possible, including using plain
[Future](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html)s
returned from
[ExecutorService.submit](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html#submit).
