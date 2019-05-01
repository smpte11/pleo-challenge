## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

## Coding challenge process
First thing I did was to have a good look at the code and understand what was going on and the relationship between the models. This is where I got my first question mark (`fun Foo.bar(): FooType {  }`). I wasn't familiar with the fact that Kotlin (well I'm not familiar with Kotlin period) supported extension methods and I gotta say it's a bit more explicit than Scala's `implicit/enrichment class` pattern.

In any case I had a pretty good idea of what I wanted to do with this test which was to implement a _"mostly FP-oriented"_ solution leveraging a lib that I saw a while ago.

This is the reason I installed the Arrow library. I also did it for a couple of reasons:

1. I needed to understand how Gradle works on the surface since I have no prior experience with it. Installing (or figuring where to install it).
2. I have also know experience in Kotlin but I have experience with Scala and Cats. This lib covers the same topics as Cats and so it feels a bit more familiar

I then went on to implement a basic unit test (forcing me to get familiar with `mockk`). To process was mostly painless: overall I find a lot of similarities between Kotlin and Scala and their respective framework. Kotlin is a bit more verbose (more syntax to learn) but also less "magic" that Scala. Its type system is not as rich however which led to some head-scratchers...

My idea was to implement a solution that leveraged parallelization to perform the billing. FP libs have great async primitives (Futures, IO, Tasks in Scala for example) and so I wanted to see if I could do the same in Kotlin.

`BillingService` implements `Monad<F>` and also gets its methods via delegation (the `by` reserved word as I understand it). This enables me to use `fx` functions to do comprehensions.

The main method charge uses Arrow's `fx` to run the two services procedurally. The resulting call to the service is wrapped into an IO to enable lightweight threading. Each call is made in parallel. `parSequence` then bundles up everything in the final IO that'll spit out our result (for now a list of booleans).





