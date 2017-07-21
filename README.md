# toymath
ROFLscale enterprise Java arithmetic!

## What?

A trivial language defined by an ANTLR grammar which emits JVM bytecode.

## How do I start adding numbers and profiting?!

You'll need Java 8+ and Maven.

    git clone https://github.com/m0wfo/toymath.git
    cd toymath
    mvn clean package
    ./bin/toymathc examples/FirstExample.tm
    java examples/FirstExample

## What does it look like?

All toymath code is wrapped in a `main` method:

    main { 1 + 1 }

Let's save our game-changing program as `SpiffingApp.tm` and compile it:

    ./bin/toymathc SpiffingApp.tm

That'll generate a `SpiffingApp.class` class file. Let's run it:

    java SpiffingApp

That should print `2` on the command line. Checkout the [examples](/examples) for more, um, examples.
