# Asynchronous interdependent tasks

This repository has code demonstrating two different approaches to executing
tasks asynchronously when some of the tasks depend on the completion of
others. The motivating example is making a pizza using the following tasks:
1. Combining the ingredients of the dough.
1. Letting the dough rise.
1. Combining the ingredients of the sauce.
1. Grating the cheese.
1. Rolling out the risen dough into a crust.
1. Putting the sauce on top of the crust.
1. Putting the grated cheese on top of the sauce.

Some of these tasks depend on others.
For example, you can't put the sauce on top of the
crust (6) until both the
ingredients for the sauce are combined (3) and the
risen dough is rolled out into a crust (5).
The complete DAG of dependencies looks like this:
```
1 --> 2 --> 5 --\
                +--> 6 --\
3 --------------/        +--> 7
4 -----------------------/
```
Multiple cooks can speed up the process by performing tasks
independently, as long as no task is started until all the
tasks it depends on are done.

The [FuturePizzaBuilder](https://github.com/Tembrel/eg4jb/blob/master/src/FuturePizzaBuilder.java)
approach uses
[CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html)s
to arrange for
the tasks to be performed in an order consistent with this DAG.

The [LatchPizzaBuilder](https://github.com/Tembrel/eg4jb/blob/master/src/LatchPizzaBuilder.java)
approach uses
[CountDownLatch](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CountDownLatch.html)es
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
Both approaches use a fixed-size thread pool
to run tasks asynchronously,
and both simulate real work by sleeping for a given amount of time.

The [PizzaDemo](https://github.com/Tembrel/eg4jb/blob/master/src/PizzaDemo.java)
class runs both versions. Both produce identical output, except that the output lines
might not be in the same order. This is because thread task scheduling can be affected
by external factors like system load.

The `CompletableFuture`-based version has several advantages over the latch-based one:
1. It's shorter and easier to understand.
1. It doesn't need to keep intermediate state in fields.
1. It can be adapted to introduce new or different dependencies very easily;
   the latch-based code would require more intricate reasoning to do the same.
1. In real usage, decisions about whether to perform a dependent task in the
   same thread as the task it depends on (or with a different Executor entirely)
   can be changed without having to redesign the code.
   This would be very difficult with latch-based approaches.

## Other possibilities

Other approaches are possible, including using plain
[Future](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html)s
returned from
[ExecutorService.submit](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html#submit-java.util.concurrent.Callable-).

The decision to use a fixed-size thread pool and the choice of that fixed size
have important consequences:
If the tasks are not started in dependency order and the pool size is less
than the number of tasks needed to make progress, the program can deadlock.
For this toy example, using a cached thread pool, which can add threads when
needed, would avoid the risk of deadlock, but in production settings this
would risk system resource exhaustion, which could be much worse than deadlock.

[ForkJoinTask](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinTask.html)s
can be used to reduce the opportunities for deadlock, by forking and joining dependent tasks.
When such tasks are used inside
[ForkJoinPool](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html)s,
work-stealing is used to make progress even when all active threads in the pool are
running tasks that are waiting for their dependencies.

