# Non Linear Composition
_________________________________________________________________________

A framework for generative exploration
_________________________________________________________________________

##**What is this? **

During my Master studies I developed a set of tools to help myself in creating streams of compound sounds. I theorized about such streams and tried to categorize them with a given terminology. My interest was to create a method for exploring several generative synthesis methods simultaneously and to be able to define transformations. This repository contains the code I wrote for that purpose. 

If you are interested on reading about my views on sound creation and organization here you can find my [master thesis] (http://darienbrito.com/texts/). While at it, you may also find it interesting to hear some practical applications in my [music] (https://soundcloud.com/darien-brito)

##**About the implementation**

I have tried that my implementation is as clean and simple as possible while mantaining modularity and ease of use. There was a previous version of this software that I was intending to release but decided to postpone after it was pointed out to me that there was a better way to do certain things. It was indeed worth to revise some ideas as now the framework is more robust and unnecesary code has been replaced by a more compact design. This is not to say that it is in its final state. I hope more savy people will take over and do better things with it than I have been able to so far.  

I ough a lot to Alberto de Campo's "CloudGenMini", described in Chapter 16 - "Microsound" of the SuperCollider book, from where key concepts and code were taken. 

##**Requirements**

- The framework has been tested with SuperCollider 3.7 running on a Macintosh. 
  Probably it will work on Windows as well but this has not been tested.
- You need to download and install the wslib quark
- You also need my dblib extensions package

##**Installation**

1. Download and install wslib. You can do so by running: 

  ```js
    Quarks.install("wslib");
  ```
2. Download and install dblib:

  ```js
    Quarks.install("https://github.com/DarienBrito/dblib");
  ```
3. Download and install NLC

  ```js
    Quarks.install("https://github.com/DarienBrito/NLC");
  ```
4. Re-compile the SuperCollider library

```js
    press cmd+shift+l
```

##**Usage**

The user builds any kind of SynthDef. The framework simply creates a parametric space for that SynthDef that can be explored using various utilities.

The only restriction the user has while creating the SynthDef is:

- It MUST have an "out" argument mapped to the Out UGen

This is so because there is an internal method of handling routings that otherwise will fail


```js

///////////////////////
// A SINGLE ELEMENT
///////////////////////

(
x =  SynthDef(\test, {|freq = 120, amp = 0.5, envDur = 0.1, out|
  var sig = SinOsc.ar(freq) * amp;
  sig = EnvGen.kr(Env.perc(releaseTime:envDur), doneAction: 2) * sig;
  Out.ar(out, sig);
}).add;
// create an Element
a = NLC_Element(x, \masks, \sine); 
// Create a GUI with custom ranges for parameters
a.makeGUI([\freq, [100, 800], \amp, [0.1, 1.0], \envDur, [0.01, 0.1], \dur, [0.01, 1]]); 
)

```
The resulting GUI is self-explanatory (I hope). It is inspired in Alberto de Campo's CloudGenMini interface. You may have noticed that there is one parameter there that was not defined in the SynthDef. This was the \dur parameter. This is so because:

- There is a pattern inside every element in NLC

This means that you can control the rate of events by passing a \dur key to the GUI, as is the convention for patterns. 
