Stream
======

The `stream` module implements a domain-specific language (DSL) for working with data streams.

Built around an extension of the familiar `Iterator` interface, the language can be used to

* map, filter, group, and ungroup the elements of input streams into elements of output streams
* configure handling policies stream iteration failures
* receive notifications of stream iteration events
* consume streams through callbacks, rather than explicit iteration

A stream is a lazily-evaluated sequence of data elements. Consumers process the elements as these become available, and discard them as soon as they are no longer required. This is of value when the sequence is large and originates from secondary storage or the network. Consumers need not wait for the entire dataset to become available before they can start to process it, nor do they need to fit it in main memory. This keeps them responsive when working at large scale.


.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-concept.png
   :align: center

Use Cases
---------

Data streaming is used in a number of use cases, including

* a service streams the elements of a persistent dataset
* a service streams the results of a query over a persistent dataset
* a service derives a stream from a stream provided by the client

The last is a case of ''circular streaming''. The client consumes a stream `A` which is produced by the service by iterating over a stream `B`, which is produced by the client. Examples of circular streaming include

* bulk lookups, e.g. clients retrieve the elements of a remote dataset which have given identifiers
* bulk updates, e.g. clients retrieve the outcomes of adding elements to a remote dataset

More complex uses cases involve multiple streams, producers, and consumers. As an example that combines lookups and updates, consider the case of a client that

* obtains a locator to a stream `S1` of element identifiers published by a service `A`
* passes the locator of `S1` to another service `B` in order to get back the locator of a stream `S2` of elements resolved from the identifiers
* processes the elements in `S2` in order to update some of their properties
* publishes a stream `S3` of updated elements and passes its locator back to `B` so as to have the updates committed
* obtains from `B` the locator of a stream `S4` of commit outcomes, and consumes it to report or otherwise handle the failures that may have occurred in the process
￼
.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-usecase.png
   :align: center

Programming over Streams
------------------------

The advantages of data streaming come with an increased complexity in the programming model. Consuming a stream can be relatively simple, but

* the assumption of external data puts more emphasis on correct failure handling
* since streams may be partially consumed, resources allocated to streams at the producer’s side need to be explicitly released
* consumers that act also as producers need to remain within the stream paradigm, i.e. avoid the accumulation of data in main memory as they transform elements of input streams into elements of outputs streams
* implementing streams is typically more challenging that consuming streams. Filtering out some elements or absorbing some failures requires ''look-ahead'' implementations. Look-ahead implementations are notoriously error prone
* stream implementations are typically hard to reuse, particularly look-ahead implementations

The rich use case outlined above illustrates the issues. Throughout the workflow, the client needs to interleave the consumption of two streams (`S1`, `S3`) with the generation of a third stream (`S2`). identifiers come from `A` as elements come from `B`, as updated elements move towards `B`, and as outcomes flow back from `B`. The client is responsible for implementing `S2`. At any time, client and services may observe a wide range of failures, some recoverable and some fatal.

The Stream Interface
--------------------

The streams library revolves around the model defined by the `Stream` interface. The interface defines an API for iterating over streams of homogeneously typed elements. The API extends the standard `Iterator` API to reflect the arbitrary origin of the data, which includes memory, secondary storage, and network.

.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-usecase.png
   :align: center

The interface shows that `Stream`s are:

* ''addressable'' (cf. `locator()`) i.e. return a URI reference to their address. The precise form and use of such stream ''locators'' is implementation-dependent. For example, when the stream originates from the network, the locator will reference the endpoint of a remote service which can serve the data through some protocol;

* ''closable'' (cf. `close()`), i.e. may be told to release computational resources allocated for their production and consumption. Consumers close streams to free up their own resources, and to signal producers that they may do the same. This is an idempotent operation, i.e. can be invoked an arbitrary number of times because it has no side-effects. A closed `Stream` can no longer be iterated over: `hasNext()` will return false, and `next()` will throw `NoSuchElementException`s.

Streams may raise a broader range of failures than `Iterator`s defined over in-memory collections:

* `locator()` may return an `IllegalStateException` if the implementation does not allow consuming the stream more than once. For example, the `Stream` implementation based on gCube Resultsets does not return locators after `hasNext()` or `next()` have been invoked;

* `next()` may return a wide range of failures (beyond the usual `NoSuchElementException` and `ConcurrentModificationException`). [[#Guarding_Streams|Later on]], we will discuss in detail the nature of such failures and the strategies that are available to handle them.

'''note''': `hasNext()` and `close()` do not return failures (other than implementation errors, of course). This aligns with standard expectations for `hasNext()`, but less so for `close()` (e.g cf. `Closeable.close()`). The justification for this is that clients cannot normally recover from closing failures, only log them and debug them. The `Stream` API leaves implementations responsible for logging or, when appropriate, retrying it. Clients have only the onus of enabling the logs.

Due the increased likelihood of failures and the need to release resources, a safe idiom for `Stream` consumption is the following::

 Stream<MyElement> stream = ...
 try {
  while (stream.hasNext()) {
   ....stream.next()...
  }
  finally {
   stream.close();
  }

With this idiom, `Stream` implementations release resources regardless of whether all its elements have been iterated over, e.g. when clients terminate abruptly due to a failure or an intentional early exit (a `break` in the main loop).

The Stream Language
-------------------

Based on the `Stream` interface, the streams library implements an embedded Domain-Specific Language (`eDSL`) of stream sentences.

Sentences are comprised of clauses. Based on the verb clause that starts a sentences, we distinguish between:

* `convert` sentences: adapt existing stream implementations to the `Stream` interface (e.g. `Iterator` [[#Adapting Plain Iterators|implementations]] and gCube Resultsets [[#Adapting Resultsets|implementations]]). The resulting `Stream`s can then be manipulated further within the language
* [[#Piping Streams|`pipe` sentences]]: transform elements of given `Stream`s into elements of new `Stream`s
* [[#Folding and Unfolding Streams|`fold` sentences]]: group elements of given `Stream`s into elements of new `Stream`s
* [[#Folding and Unfolding Streams|`unfold` sentences]]: expands elements of given `Stream`s into many elements of new `Stream`s
* [[#Guarding Streams|`guard` sentences]]: configure `Stream`s with given fault handling policies
* [[#Monitoring Streams|`monitor` sentences]]: configure `Stream`s with iteration event listeners
* [[#Logging Streams|`log` sentences]]: log `Stream`s throughput
* [[#Publishing Streams|`publish` sentences]]: make `Stream`s available to remote consumers
* [[#Stream Callbacks|`consume` sentences]]: consume `Stream`s by passing their elements to callbacks

All the verb clauses above are implemented as static methods of the `Streams` class. The methods return objects that capture the state of the sentence under construction. These objects offer instance methods that allows us continue the construction of the sentence in a type-safe manner.

.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-sentences.png
   :align: center
￼
To fold a `Stream` of strings into a `Stream` of 10-string elements, we can write::

 Stream<String> strings = ...
 Stream<List<String>> folded = Streams.fold(strings).in(10);

We can use a `static` import on the whole `Streams` class to improve the fluency of the code::

 import static org.xbib.stream.dsl.Streams.*;
 ...
 Stream<String> strings = ...
 Stream<List<String>> folded = fold(strings).in(10);

Now we discuss `fold` sentences and all the other sentence types in detail.

Adapting Plain Iterators
------------------------

The simplest sentences of the stream eDSL are those that turn a standard `Iterator` into a `Stream`::

 Iterator<String> strings = ...
 Stream<String> stream = '''convert'''(strings);

We may use this interface conversion over streams based on in-memory collections. This is useful for testing, but also when services return data in collections or arrays, i.e. when they are not designed for streaming. We can then turn the data into streams, publish them (we will see it later how), and then push them towards another service that expects data in this form.

We can of course convert any `Iterator` implementation (e.g. persistent datasets), not only those returned by the standard Collections API. As a case in point, we can convert directly from `Iterable`s::

 List<String> strings = Arrays.asList(“1”,”2”,”3”);
 Stream<String> stream = '''convert'''(strings);

In all cases, the conversions make the datasets eligible to further manipulation with our eDSL.

By default, the `Iterator`s are expected to be over in-memory collections. Invoking `locator()` on the adapted `Stream` returns a pseudo URI of the form `local://`''to-string'', where ''to-string'' is  the output of the `toString()` method of the `Iterator`. Clearly, a local locator serves solely for debugging purposes and cannot be resolved. Similarly, invoking `close()` on the adapted `Stream` has an effect only if the `Iterator` implements the `Closeable` interface. In this case, the stream simply delegates to the `Iterator`.

We may override these defaults by extending `IteratorAdapter` and overriding its `locator()` and/or `close()` methods, as appropriate. For example, if we know that the original Iterator streams the contents of a given file, we may extend `IteratorAdapter`::

 IteratorAdapter fileAdapter = new IteratorAdapter(strings) {
  @Override '''URI locator'''() {
   return URI.create(“file://...”);
  }
 };

and then obtain a `Stream` from it::

 Stream<String> stream = '''convert'''(fileAdapter);

Adapting Resultsets
-------------------

We can also use convert `gCube Resultsets` into `Stream`s. The starting point is now a URI locator to the `Resultset`::

 URI rs = ...
 Stream<MyRecord> stream = convert(rs).of(MyRecord.class).withDefaults();

Here we have assumed the `Resultset` is comprised of custom `MyRecord`s and we have used default settings for the translation. We can also use pre-defined record types, such as `GenericRecord`, as well as act on the read timeout::

 Stream<GenericRecord> stream = convert(rs).of(GenericRecord.class).withTimeout(1,TimeUnit.MINUTES);

A common use of `GenericRecord`s within the system is to carry string serialisation of elements in a single field. Effectively, this marks an “untyped” use of the `Resultset` mechanism. In this case, we can simplify the sentence further::

 Stream<String> stream = convert(rs).ofStrings().withDefaults();

Note that, since streams are based on memory buffers at both consumer and producer ends, `Resultset` can no longer be consumed after we’ve started iterating over their elements. If we invoke `locator()` after `hasNext()` or `next()` we will raise an `IllegalStateException`.

Piping Streams
--------------

Given a `Stream`, we can transform its elements into elements of another `Stream`. The simplest transformations are one-to-one: for each element of the input stream we generate an element of the output stream.

Visually, it’s as if we were ''piping'' the input stream into the output stream and see the elements that enter at one end of the resulting pipe come out changed as they exit at the other end. We may of course change the type of elements as they flow through the pipe, or update them in place. While we can define arbitrarily complex transformations, we will normally keep them simple: parse strings into objects, serialise objects into strings, extract selected information from objects, change that information, create new objects from that information, and so on.

.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-pipe.png
   :align: center

We define transformations by implementing the `Generator` interface::

 Generator<String,Integer> sizer = new Generator<String, Integer>() {
    public Integer yield(String element) {
      return element.length();
    }
 };

and then use the `Generator` in a `pipe` sentence::

 Stream<String> strings = ...
 Stream<Integer> lengths = pipe(strings).through(sizer);

When we need to update the elements of the input stream, we can directly implement the `Processor` interface::

 final Calendar now = Calendar.getInstance();
 Processor<MyElement> updater = new Processor<MyElement>() {
    public void process(MyElement element) {
      element.setLastModificationDate(now);
    }
 };
 Stream<MyElement> elements = ...
 Stream<MyElement> updated = pipe(strings).through(updater);
￼
If we need to implement a ''filter'', i.e. exclude some elements from the output stream, we can throw a `StreamSkipElementException` in the guise of a signal::

 Generator<String,String> sizeFilter = new Generator<String,String>() {
    public String yield(String element) {
       if (element.length() <5)
 		return element;
 	 else
            throw new StreamSkipElementException();
    }
 };
 Stream<String> strings = ...
 Stream<Integer> smallStrings = pipe(strings).through(sizeFilter);

Folding and Unfolding Streams
-----------------------------

Sometimes we need to transform an input `Stream` into an output `Stream` by grouping the elements of the elements of the first into individual elements of the second. For example, we may need a folding transformation if we have a stream and need to pass its elements to a service designed to take only finite data collections.

We can fold a `Stream`::

 Stream<String> strings = ...
 Stream<List<String>> folded = fold(strings).in(50);

Here we are grouping a maximum of 50 strings at the time. We will terminate the output stream with a smaller group if there are less than 50 elements left in the input stream.

.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-fold.png
   :align: center

￼Conversely, we can unfold a `Stream`, i.e. expand each of its elements into a `Stream` using a `Generator`, and then flatten all such `Stream`s into a final single `Stream`.

The following sentence inverts the transformation above::

 Generator<List<String>,Stream<String>> streamer =
 	 	new Generator<List<String>,Stream<String>>() {
    public Stream<String> yield(List<String> element) {
 		return convert(element);
    }
 };
 Stream<String> strings = unfold(folded).through(streamer);

Our `Generator`s may derive arbitrary `Stream`s from individual elements of the input `Stream`. For example, if we pass each element of the input stream to a service that returns a `Stream`, we obtain a final `Stream` that flattens all the elements returned by the service across all our calls.

.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-unfold.png
   :align: center

Guarding Streams
----------------

So far we have happily ignored the possibility of failures during iterations, i.e. at the point of invoking `next()`. Yet dealing with persistent and remote data makes failure quite likely, and allowing transformations between input streams and output streams increases the likelihood in principle. Failure handling is perhaps the hardest part of any programming model for streams.

When failures do occur, they percolate across all our transformations and emerge as unchecked exceptions when we consume the streams. We may continue to ignore them if we deal with them higher up in the call stack, in a so-called ''fault barrier''. In this case, we are effectively adopting a “re-throw” policy for stream consumption.

Often we may wish to have more control over failures. Depending on the context, we may want to ignore them, stop iterating instead of re-throwing, or even re-throw them as different exceptions. We may want to base our decision on the type of failure, how many times it occurs, or the particular state of our computation. Indeed, the range of possible failure handling policies is unbound.

We may of course implement such policies at the point of stream consumption, relying on standard try/catch blocks. As we transform streams and compose those transformations, however, a more modular approach is to explicitly configure our policies on the `Stream`s themselves. To do this, we can guard any given `Stream` with an implementation of the `FaultHandler` interface::

 Stream<MyElement> stream = ...;
 FaultHandler handler = new FaultHandler() {
  @Override FaultResponse handle(RuntimeException failure) {
     ... policy implementation...
  }
 };
 Stream<MyElement> guarded = guard(stream).with(handler);

.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-guard.png
   :align: center

When a failure occurs, the `Stream` implementation passes the failure to `handle()` and the handler responds with `FaultResponse.CONTINUE` if the failure should be ignored and `FaultResponse.STOP` if the failure should silently stop the iteration. The handler may also re-throw the same or another exception.

When convenient, we can also extend `CountingHandler`, a `FaultHandler` that keeps a count of the failure we process (in case we tolerate them) and reminds us each time of the failure we observed and tolerated last::

 FaultHandler handler = new CountingHandler() {
  FaultResponse handle(RuntimeException failure,
                       Exception lastFailure,
                       int failureCount) {
     ... policy implementation...
  }
 };

.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-handlers.png
   :align: center

What failures can a policy observe? There are two broad classes to consider. Some failures are ''unrecoverable'', i.e. carry the guarantee that the consumer will not be able to read further elements from the stream. Others are instead ''recoverable'', i.e. indicate that there is a good chance that continuing the iteration may produce more elements. (Note that recoverability here is with respect to the iteration alone, the client may always recover in a broader context).

We also distinguish between:

* ''errors'', which are due to faulty implementations or missing/invalid configurations. Errors are nearly always unrecoverable;
* ''outages'', which may occur unpredictably in the runtime and include network failures, disk failures, and out-of-memory errors. Outages are always unrecoverable in practice, in that the conditions that brought them about typically persist for longer than the consumer can tolerate;
* ''contingencies'', which occur when elements are derived from other elements and the former cannot be derived because the latter violate some pre-conditions in predictable ways. This dependencies between elements may occur in circular streaming, or simply because we apply transformations to stream elements. Since violations are strictly related to individual input elements, contingencies are recoverable in principle.

To help out defining policies outside or inside `FaultHandler`s, the `streams` library defines its own hierarchy of unchecked exception types:

* `StreamException` is at the root of the hierarchy, and can be used to define broad strategies for iteration failures (e.g. in so-called fault barriers at the top of the call stack);

* `StreamContingencyException` is a `StreamException` that models contingencies. `Stream` implementations throw it along with the original cause, giving a hint of recoverability that can inform strategies. Failure handling policies that observe `RuntimeException`s other than `StreamContingencyException` may then consider them unrecoverable failures;

* one type of unrecoverable failure is represented by `StreamOpenException`, which some `Stream` implementations may throw when consumers or producers are not correctly initialised, producers are unavailable or cannot be located, or when the producer cannot locate a target dataset. At the time of writing, the `Stream` implementation that adapts `gCube Resultsets` is the only source of `StreamOpenException`s;

* another type of unrecoverable failure is represented by `StreamPublishException`s, which some `StreamPublisher` implementations may throw when the attempt to publish the stream at a given endpoint is not successful. We discuss stream publication [[#Publishing_Streams|below];

* we have already encountered `StreamSkipException`s when talking about `Generator`s and `pipe` sentences. These exceptions, however, do not capture actual failures but serve as signals for `Stream` implementations. They indicate there is no transformation for a given element of an input `Stream`, i.e. the element should simply be excluded from the output stream.
￼
.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-exceptions.png
   :align: center

Building on the reusability of `FaultHandlers` and the hierarchy above, the `Streams` class includes constants for generic `FaultHandler`s which capture common failure handling policies:

* `IGNORE_POLICY`: systematically ignores all failures
* `STOPFAST_POLICY`: stops at the first failure
* `RETHROW_UNRECOVERABLE_POLICY`: ignores all contingencies and re-throws the first unrecoverable failure
* `STOP_UNRECOVERABLE_POLICY`: ignores all contingencies and stops at the first unrecoverable failure

We can quickly manifest full tolerance to failure::

 Stream<MyElement> stream = ...;
 Stream<MyElement> guarded = guard(stream).with(IGNORE_POLICY);

Note that failures may also occur when we add our own logic to stream processing, e.g. when we pipe streams through `Generator`s. Our `Generator`s may then need to capture failures and re-throw them either as `StreamContingencyException`s or as `RuntimeExceptions`, depending on the failure. They may also re-throw them as `StreamSkipException`s, effectively making an autonomous decision that they failure should be ignored.

Monitoring Streams
------------------

Like with failures, we may wish to encapsulate a lifetime policy within `Stream`s. This means to register a set of callbacks that the `Stream` implementations will use to notify us of key events in the lifetime of a `Stream`. We can then define the callbacks in an implementation of the `StreamListener` and then use the listener to build a monitor sentence of the language::

 StreamListener listener = new StreamListener() {
   @Override public void onStart() {...}
   @Override public void onEnd() {...}
   @Override public void onClose() {...}
 };
 Stream<MyElement> stream = ...;
 Stream<MyElement> monitored = monitor(stream).with(listener);

.. image:: https://github.com/xbib/xbib/raw/master/src/site/resources/Streams-monitor.png
   :align: center
￼
Notice that

* `onStart()` is invoked after we consume the first element of the stream;
* `onEnd()` is invoked after we consume the last element of the stream;
* `onClose()` is invoked when we invoke `close()` on the stream, or when the self-closing `Stream` implementation does. Since both these cases may happen, `onClose()` should be as idempotent as `close()` is.

Notice also that we can extend `StreamListenerAdatper` if we want to listen only to selected events.

Logging Streams
---------------

It is often useful to monitor a `Stream` so as to log an indication of throughput based on the number of elements streamed and the time based to stream them. This can be accomplished by combining a `Generator`s and `StreamListener` into a single component and then use the component in `pipe` and `monitor` sentences, e.g.:

 Stream<MyElement> stream = ...
 LoggingListener listener = ...
 Stream<MyElement> piped = pipe(stream).through(listener);
 Stream<MyElement> logged = monitor(piped).with(listener);
 For convenience, we can obtain the same result with:
 Stream<MyElement> stream = ...
 Stream<MyElement> logged = log(stream);

Publishing Streams
------------------

When we produce a stream for remote consumption, perhaps transforming a remote input stream in turn, we need to publish it at a given endpoint and pass a reference to that endpoint to our remote clients as the locator of the stream.

At the time of writing, the only publication mechanism used by the streams library is in terms of `gCube Resultsets`. We can publish one easily by constructing a `publish` sentence::

 Stream<MyElement> stream = ...
 RecordFactory<MyElement> factory = new RecordFactory() {
  @Override RecordDefinition[] definitions() {
     ... describe resultset records that correspond to elements...
  }
  @Override Record newRecord(MyElement element) {
     ... convert element into corresponding record...
  }
 };
 URI rs = publish(stream).using(factory).withDefaults();

`RecordFactory` is an interface with direct dependencies to the gRS2 API. It indicates the type of records that will comprise the published `Resultsets` and can generate one such record from a given element.
￼
As we [[#Adapting_Resultsets|have seen]] when adapting `Resultsets` to the`Stream` interface, `publish` sentences can be simplified further if we want an “untyped” `Resultset` made of single-fielded and string-valued records. In this case, all we need to provide is a `Generator` that can serialise elements to strings:::

 Stream<MyElement> stream = ...
 Generator<MyElement,String> serialiser = ...
 URI rs = publish(stream).using(serialiser).withDefaults();

Once the type of records to be used is configured, we may want to configure publication further. For example, we may want to override the default size of the write buffer and/or the default writing timeout:

URI rs = publish(stream).using(...).withBufferOf(10).withTimeoutOf(3,TimeUnit.Minutes).withDefaults();

Notice that we still close the sentence by requiring defaults for any option that we have not explicitly configured.

We may also configure a `FaultHandler` for publication failures::

 FaultHandler handler = ...
 URI rs = publish(stream).using(...).with(handler).withDefaults();

By default the stream will be published on demand, i.e. as the client consumes it. This allows us to consume resources as they are really needed. We may also indicate that the stream is to be published continuously, i.e. regardless of whether the remote clients is actually consuming the stream. This is sometimes required when publishing the stream has important side-effects that we want to trigger regardless of the client’s behaviour::

 URI rs = publish(stream).using(...).nonstop().withDefaults();

Whether on demand or continuous, the elements of the stream are always published asynchronously in a dedicate thread. In some cases, we may need to have control on the publication thread, e.g. to set thread-local variables on it. We can then configure the publication to use our own `ThreadProvider`::

 ThreadProvider provider = new ThreadProvider() {
    @Override public Thread newThread(Runnable task) {
       ....yields a Thread for executing the publication task...
    }
 };
 URI rs = publish(stream).using(...).with(provider).withDefaults();

Putting it Together
-------------------

Putting together some of the sentences of the eDSL, we may implement the use case introduced [[#Use_Cases|above]] as follows. For simplicity, we assume the existence of a local API to the remote service which can resolves `MyElement`s from their identifiers and update `MyElement`s. The API takes and returns locators to `gCube Resultsets` of “untyped” records::

 MyElementService service = ...
 URI idRs = ...
 //lookup elements
 URI elementRs =  service.lookup(idRs);
 Stream<MyElement> elements = convert(elementRs).ofStrings().withDefaults();

 //update elements (ignoring failures)
 elements = guard(elements).with(IGNORE_POLICY);
 Processor<MyElement> updater = ...
 Stream<MyElement> updated = pipe(elements).through(updater);

 //publish updated elements (stopping at first problem)
 updated = guard(updated).with(STOPFAST_POLICY);
 Generator<MyElement,String> serialiser = ...
 URI updatedRS = publish(updated).using(serialiser).withDefaults();
 URI outcomeRs = service.update(updatedRS);

 //process outcomes (letting failures through)
 Stream<Outcome> outcomes = convert(outcomeRs).ofStrings().withDefaults();

 try {
   while(outcomes.hasNext())
     ...outcomes.next()...
     ...process outcome...
 }
 finally {
  outcomes.close();
 }