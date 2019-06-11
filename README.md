# Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

## Process
### Goals
Since I don't know much about Kotlin, I set out for a few things.

1. I wanted to use a (mostly) purely functional style of programming. This is what brings all the `IO`, `Either` and `fx` goodness.

1. I wanted to make both OO and FP code work altogether by touching very little existing code.

1. Third thing was try to use the most of the Arrow library which is the only thing I know from Kotlin (from a friend that showed it to me a while ago). Since I have some background in Scala and the Cats/Cats-Effect libraries, I though this would be an nice way to ease into the Kotlin world while achieving point 1 and 2.

### Process and challenges
I started basically by looking around at the code and get familiar with the syntax. Kotlin feels like a mix of Java and ES2016 Javascript. I overall mostly liked the syntax and got familiar with it quicker than I thought.

For the main class, since I knew it could be a long running process, I wanted to provide as much concurrency as possible. The main operations are wrapped into a `parSequence` method that parallelize effects and returns those into a runnable main effect. I used `map` to do it to build that list of effect using the `fx` block (an equivalent to `for-comprehensions` in Scala to abstract over `flatMap` call). When run, success and failure handling are run sequentially, but every invoice is treated in parallel. 

Error handling is done automatically using `handleError`. I provided a retry logic for network exception and a simple one for customer not found to play around with `Exposed` a bit.

I wanted my service to run in a basic scheduler. I started by using kotlinx coroutines with a delay but ran into some issues when running in isolation in my tests. I also started to put some suspend code into my billing service methods which I disliked: I wanted to keep the solution consistent. I refactored my solution to use the parallel utilities of Arrow with great success (one less dependency)!

## Things to improve
### Testing
1. I found myself writing some redundant boilerplate (mainly creating fake data returned from mocks). I created some (very) basic "test utils" but I've been wondering about creating a random seed/fixture lib to lifts data classes with fake data (a la Factory Boy in Python).

1. While they check the essential (and they caught a pretty stealthy bug), I would like to improve the test part of the code. I think this is mainly from my understanding of `mockk` which I found both interesting and confusing. It's a big departure from what I'm used to (`Jest/Jasmine`, Python's `Unittest` and `ScalaTest`). I think with some coaching and more time I would improve a lot on what I did and maybe appreciate it better.

### Scalability/Robustness
1. One advantage of using `IO` or `effect` is to abstract over network calls. I can imagine rewriting `PaymentProvider` to call another service, a another process, a lambda, anything. The same is done with other service calls. Removing all `unsafeRunSync` is the obvious next step to make it robust but for now, it think it's OK.

1. This is a very basic scheduling task. It's work, but it would be better to run in a lambda for exemple (luckily `ScheduledProcess` and `BillingServices` are totally decoupled).

1. Logging could (obviously) be better. I didn't really put a lot of time into it (focused on other things).

### Architecture/Design/Code style
1. This was my first experience with Kotlin one way to offset that was to rely on things I know which came from Scala (mainly through Arrow). While I think it makes for an interesting solution, I would have liked to know what writing idiomatic Kotlin would look like and use more of it's feature.

1. I liked using Arrow but it comes with some caveats: it's light type system can be confusing. Stuff like `ForOption`, `Kind`, `fix()`, etc. Its documentation is also a bit incomplete but looking at the test suite helps a lot. I'd use it just for the IO stuff and Either. The AQL language seems pretty cool too.

1. There are leftovers from a few experiments that I tried but failed which I detailed in the code. One what to use extension methods an play a bit with generic types (Retryable for example).

## Conclusions
I'm overall quite satisfied with what I came up with. It took me between 2-3 days to do it (mainly spent on understanding `mockk`) and implementing a scheduler based on coroutines (which... I scrapped in the end). I also face a weird recursion bug with how my IOs were spawned (fix with the help of my tests) which took out an evening!


