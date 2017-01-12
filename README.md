# Non Linear Composition
_________________________________________________________________________

A framework for generative exploration
_________________________________________________________________________

##**What is this?**

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

##**Basic Usage**

The user builds any kind of SynthDef. The framework simply creates a parametric space that can be explored using various utilities.

##***Restriction***

- Every SynthDef must have an "out" argument mapped to an Out UGen

This is so because there is an internal method of handling routings that otherwise will fail. This is the only restriction while writing your SynthDef. Following an example with a very simple one:

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

// Create an element:
a = NLC_Element(x, \masks, \sine); 

// Create a GUI with custom ranges for parameters:
a.makeGUI([\freq, [100, 800], \amp, [0.1, 1.0], \envDur, [0.01, 0.1], \dur, [0.01, 1]]); 
)

```
The resulting GUI is self-explanatory (I hope). It is inspired in Alberto de Campo's CloudGenMini interface. You may have noticed that there is one parameter there that was not defined in the SynthDef. This was the \dur parameter. This is so because:

- There is a pattern inside every element in NLC

This means that you can control the rate of events by passing a \dur key to the GUI, as is the convention for patterns.

NLC_Element(synth:, type:, name:, patternType:);

- synth: a variable referencing your SynthDef or an array with references to various SynthDefs
- type: the type of controls. It can be \sliders (or \s) and \masks (or \m)
- name: any name you wanna give your element as a String...mmm... "Fluffy apple from Mars" ? Sure!
- patternType: can be a \Pbind or a \Pmono. \Pbind by default.

Here another example using various flavours of one synth:

```js
////////////////////////////////////////////
// A SINGLE ELEMENT (with various flavours)
////////////////////////////////////////////

(
x =  SynthDef(\testSine, {|freq = 120, amp = 0.5, envDur = 0.1, out|
  var sig = SinOsc.ar(freq) * amp;
  sig = EnvGen.kr(Env.perc(releaseTime:envDur), doneAction: 2) * sig;
  Out.ar(out, sig);
}).add;

y =  SynthDef(\testSaw, {|freq = 120, amp = 0.5, envDur = 0.1, out|
  var sig = Saw.ar(freq) * amp;
  sig = EnvGen.kr(Env.perc(releaseTime:envDur), doneAction: 2) * sig;
  Out.ar(out, sig);
}).add;

z =  SynthDef(\testTri, {|freq = 120, amp = 0.5, envDur = 0.1, out|
  var sig = VarSaw.ar(freq) * amp;
  sig = EnvGen.kr(Env.perc(releaseTime:envDur), doneAction: 2) * sig;
  Out.ar(out, sig);
}).add;

r =  SynthDef(\testPulse, {|freq = 120, amp = 0.5, envDur = 0.1, out|
  var sig = Pulse.ar(freq) * amp;
  sig = EnvGen.kr(Env.perc(releaseTime:envDur), doneAction: 2) * sig;
  Out.ar(out, sig);
}).add;

// we pass here an array with all the SynthDefs
a = NLC_Element([x,y,z,r], \masks, "Fluffy apple from Mars");
a.makeGUI([\freq, [100, 800], \amp, [0.1, 1.0], \envDur, [0.01, 0.1], \dur, [0.01, 1]]);
)
```

You can now choose the "flavours" for our "Fluffy apple from Mars" from the Synth pop-up menu in the interface. For any construct of different versions to work properly, they must share the same arguments, as you can only pass a single array of arguments to each Element.

##**Many Elements** 

Life would be boring with just one of everything... we can easily create a bunch of instances of our element with the NLC_ElementsClones class. It, well... clones things! Each clone is however independent from each other, meaning that you can control every parameter at will without afecting the others. There are macro-controls in the top of the interface to control them all at once. A clone also lets you pass an array with different parameters per clone. Every clone you pass can have a different control type if you want.

```js
////////////////////
// USING THE CLONER
///////////////////
(
var n = 4; // How many clones?

var synth =  SynthDef(\test, {|freq = 120, amp = 0.5, envDur = 0.1, out|
  var sig = SinOsc.ar(freq) * amp;
  sig = EnvGen.kr(Env.perc(releaseTime:envDur), doneAction: 2) * sig;
  Out.ar(out, sig);
}).add;

 // Get them
var elements = n.collect{|i| NLC_Element(synth, \masks, \sine)};

// Pass them to the cloner
var console =  NLC_ElementsClones(
  elements, 
  [\amp,[0, 0.9], \freq,[60, 2000], \envDur,[0.01,0.1], \dur, [0.01, 0.1]] ! n, // parameters
  "Array of testers" // Name
).display(\grid, gridCols: 2, gridRows: 2); // display mode
)

```

NLC_ElementsClones(elements:, elementsParams:, name:, skin:)

- elements: an array of elements
- elementsParams: an array of parameters for those elements (elements[0] -> elementsParams[0], etc)
- name: any name you wanna give to your cloner... "Dolly the sheep on drugs?" sure!
- skin: can be \black or \gray for now... I'm kinda hoping others will add to the GUISkins frpm dblib class as I hate coming up with colors

The parameters for the display method are:

.display(orientation:, dimension:, gridCols:, gridRows:)

- orientation: \vertical or \grid
- dimension: The maximum height for the interface
- gridCols: how many columns in grid mode
- gridRows: how many rows in grid mode

##**Many Different Elements**

There will be instances where you want to have various elements in an interface where each one of them is a unique synthesis process with its own parameters and control type. This is what the NLC_ElementsContainer is for.

```js

////////////////////////
// USING THE CONTAINER
////////////////////////

(
var synthA =  SynthDef(\test, {|freq = 120, amp = 0.5, envDur = 0.1, out|
  var sig = SinOsc.ar(freq) * amp;
  sig = EnvGen.kr(Env.perc(releaseTime:envDur), doneAction: 2) * sig;
  Out.ar(out, sig);
}).add;

var synthB =  SynthDef(\test, {|freq = 120, amp = 0.5, envDur = 0.1, cFreq = 100, out|
  var sig = Saw.ar(freq) * amp;
  sig = EnvGen.kr(Env.perc(releaseTime:envDur), doneAction: 2) * sig;
  sig = LPF.ar(sig, cFreq);
  Out.ar(out, sig);
}).add;

var elements = [ NLC_Element(synthA, \masks), NLC_Element(synthB, \sliders)];

var console =  NLC_ElementsContainer(elements, [
  [\amp,[0.1, 0.9], \freq,[60, 2000], \dur, [0.01, 0.1],\envDur,[0.01,0.1]], //Parameters Element 1
  [\amp,[0.1, 0.9], \freq,[60, 2000], \dur, [0.01, 0.1],\envDur,[0.01,0.1], \cFreq,[100, 20000]] //Parameters Element 2
],
"Testing Container", \black).display(\vertical);
)

```

NLC_ElementsContainer is a subClass of NLC_ElementsClones; it shares the same parameters.

##**Functionality**

Probably there will be ocasions when you would like to control things from code instead of an interface. Here a list with the most relevant methods of an Element:

- .play 
- .stop
- .morphStates(array, array, array, int) : Lets you move discretily across selected states of an Element

```js

// Move one time from current state to state 1 in 0.5 seconds and then to 2 in 2 seconds
myElement.morphStates(order:[1, 2], times:[0.5, 2], curves: [\lin, \exp], n:1);

```
- .setMorphTime(int) : Sets the general morph time of the element
- .autoMorph(int) : Morph n times to random states
- .stopMorphing
- .change(symbol, value) : Pairs of data. Set the given parameters to whatever you want

```js

// Change freq to be a Pattern
Myelement.change(\freq, Pseq([100, 500, 2000], inf))

// Change freq and amp to be a constant
Myelement.change(\freq, 400, \amp, 0.25)

```

This method overrides the corresponding control in the interface with the new value. Since every parameter for an Element has internally a PatternProxy, you can change it to whatever you like within what an Event can handle. 

- .revert(symbol) : Revert control of the given parameter to the interface controller 
- .plugMIDI(symbol, int) : Map given parameter to given CC
```js

// Plug frequency to CC 0
MyElement.plugMIDI(\freq, 0)

```

- .plugMIDIAll(array, float) This method assings a batch of CC's to parameters in the interface from top to bottom

```js
(
x =  SynthDef(\test, {|freq = 120, amp = 0.5, envDur = 0.1, out|
  var sig = SinOsc.ar(freq) * amp;
  sig = EnvGen.kr(Env.perc(releaseTime:envDur), doneAction: 2) * sig;
  Out.ar(out, sig);
}).add;

// Create an element:
MyElement = NLC_Element(x, \masks, \sine); 

// Create a GUI with custom ranges for parameters:
MyElement.makeGUI([\freq, [100, 800], \amp, [0.1, 1.0], \envDur, [0.01, 0.1], \dur, [0.01, 1]]); 
)

// When using masks, aperture sets the distance between min and max in the range:
MyElement.plugMIDIAll(ccArray: (0..3), aperture: 1);

// When using sliders, only an array has to be passed:
MyElement.plugMIDIAll((0..3))

```
- plugDiscreteMIDI(tripletArrays) This method is for masks mode only. You map a parameter to MIDI specifically

```js
//Plug cc's by triplets of [\param, cc, aperture]
MyElement.plugDiscreteMIDI([[\amp, 0, 0.25], [\freq, 1, 100]]);

```
- plugPairedMIDI(pairArrays) This method is for sliders mode only. You map a parameter to MIDI specifically

```js

//Plug cc's by pairs of [\param,cc]
MyElement.plugPairedMIDI([\freq, 0], [\amp, 1])

```
##**Extending**
  
- If you have an idea that feel should be included in this project just give me a shout, or if you feel like coding make a branch, and send a pull request. 

- If there's someone who enjoys designing GUI's, the dblib that is part of this package contains a class called GUISkins. You can add a method with the skin name and color combination of your preference there.


##**Bugs**

- If you want to report a bug, please use the [Issue Tracker] (https://github.com/DarienBrito/NLC/issues)




