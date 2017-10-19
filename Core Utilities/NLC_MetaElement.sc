////////////////////////////////////////////////////////////////////////////////////////////////////
//
// NLC_MetaElement
//
// * Functionality for Elements of the NLC framework
//
// Copyright (C) <2016>
//
// by Darien Brito
// http://www.darienbrito.com
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.

// Disclaimer: key concepts and code of this program are based on
// Alberto de Campo's "CloudGenMini", described in "The SuperCollider Book",
// Chapter 16 - Microsound. Thank you Alberto!

NLC_MetaElement : NLC_BaseElement {
	var paramNames, <paramRNames, paramVals;
	var <current, target, statesEv, lockTest;
	var synthEv, ezControls, updateVizTime, midiFuncs;
	var formattedObjects, formattedPresets, morphTypeBox;
	var setNames, currentSynth;
	var taskMorph;
	var <autoMorph, autoRand, autoRandMask;
	var <>win, <guiHeight, <guiWidth, masterVol;
	var <bWidth, <bHeight, <sliderHeight;
	var <automorphBtn, <autoRandBtn, <presetsBtn, <fdBox;
	var <randBtn, <interBtn, <nMorphsNumber, <lockButtons;
	var <elementName, <patternType;

	*new { |synth, name, patternType|
		^super.new.init(synth, name, patternType)
	}

	init { |synth_, name_, patternType_|
		synth = synth_;
		bWidth = 80;
		bHeight = 20;
		sliderHeight = bHeight;
		guiWidth = 415;
		elementName = name_;
		patternType = patternType_;
		this.invokeData(synth, patternType);
	}

	initVals { |paramsPairs|
		var size = (paramsPairs.size) * 0.5;
		paramNames = Array.new(size);
		paramRNames = Array.new(size);
		paramVals = Array.new(size);

		current = ();
		target = ();
		statesEv = ();
		lockTest = ();
		lockButtons = ();
		synthEv = ();
		ezControls = ();
		updateVizTime = 0.1;
		midiFuncs = ();
		formattedObjects = ();
		formattedPresets = ();
		masterVol = 0.5;

		paramsPairs.pairsDo{ |name, val|
			paramNames.add(name);
			paramVals.add(val);
			paramRNames.add(name);
			Spec.add(name,val);
		};
		Spec.add(\morphTime, [0.001, 1000, \exp]);
		Spec.add(\curve, [1, 14, \lin]);

		paramRNames.do({|name, i|
			current.put(name, paramVals[i]);
			lockTest.put(name, false);
		});
		setNames.do{|name, i| synthEv.put(name,currentSynth)};
	}

	// Notice: .play and .stop override BaseElement's methods:
	play {
		playBtn.value = 1;
		isPlaying = true;
		this.makePattern(patternType);
		pattern = pattern.play;
	}

	stop {
		playBtn.value = 0;
		isPlaying = false;
		pattern.stop;
		pattern.free;
		this.stopMorphing();
	}

	makeGUI { | paramsPairs, pos, onTop = false, skin = \gray, master = nil |
		this.initVals(paramsPairs);
		this.makeMasksPresetTags;
		this.setProxies;
		this.buildGui(pos, onTop, skin, master);
	}

	close {
		win.close;
		this.stop();
	}

	revertToMask { | parameter |
		//If array
		if (parameter.size > 0) { parameter.do{|param| this.revert(param) }
		} { this.revert(parameter) }
	}

	setCurrent { | state |
		current = state;
		paramRNames.do{|name|
			ezControls[name].value = state[name];
			ezControls[name].doAction
		};
	}

	makeMasksPresetTags {
		setNames = (1..8).collect{ |i| ("set"++i).asSymbol };
		setNames.do {|key|
			statesEv.put(key, this.setAllStates;);
			synthEv.put(key, currentSynth);
		};
		this.setFormattedData;
	}

	morphTask {
		taskMorph = TaskProxy({
			var startSet = current, endSet = target;
			var numSteps = morphTime * stepsPerSec;
			var blendVal, morphSettings;
			if (target.notNil) {
				(numSteps).do { |i|
					blendVal = (i + 1) / numSteps;
					morphSettings = endSet.collect({ |val, key|
						(startSet[key] ? val).blend(val,
							dataSet[currentMorphType].blendAt(blendVal*dataSet[currentMorphType].size-1));
					});
					//Update
					current = morphSettings;
					//Check if locked
					paramRNames.do{ |key, i|
						if(lockTest[key]) { current[key] = startSet[key] }
						{ current[key] = morphSettings[key]}
					};
					(1/stepsPerSec).wait;
				};
				//Update globals
				paramRNames.do{ |key|
					if(lockTest[key])
					{current[key] = startSet[key].copy}
					{current[key] = target[key].copy}
				};
				"morph done.".postln;
				if (stopAfterFade) { this.stop };
			};
		}).quant_(0);
		^taskMorph
	}

	morphMaskTo  { | start, end, time, interpolation, autoStop |
		current = start ? current;
		target = end;
		morphTime = time ? morphTime;
		currentMorphType = interpolation ? \lin;
		if (autoStop.notNil) {stopAfterFade = autoStop };
		this.morphTask.stop.play;
	}

	pauseMasksMorph {
		taskMorph.pause
	}

	resumeMasksMorph{
		taskMorph.resume
	}

	autoMorphFunc { |n = 0|
		automorphBtn.value = 1;
		autoMorph = TaskProxy{
			n.do{
				target = this.setAllStates;
				this.morphTask.stop.play;
				(morphTime + 0.1).wait;
			}
		}.quant_(0).play;
		^autoMorph;
	}

	stopMorphing {
		automorphBtn.value = 0;
		autoMorph.stop;
	}

	autoRandom {|n = 0|
		autoRandBtn.value = 1;
		autoRandMask = TaskProxy{
			n.do{
				paramRNames.do{ |key|
					if (lockTest[key])
					{current[key] = current[key] }
					{current[key]  = this.setAllStates[key]}
				};
				(morphTime + 0.1).wait;
			}
		}.quant_(0).play;
		^autoRandMask
	}

	stopAutoRandom {
		autoRandBtn.value = 0;
		autoRandMask.stop
	}

	morphStates{ | order, times, curves, n = 1 |
		var o, t, c;
		o = order ? (1..8);
		t = times ?  (morphTime ! o.size);
		c = curves ? (currentMorphType ! o.size);
		if(t.size != o.size || c.size != t.size ) { "Parameters length do not match".error } {
			autoMorph = TaskProxy {
				(o.size * n).do{| i |
					this.morphMaskTo(
						end: statesEv[setNames[o[i] - 1]],
						time: t[i],
						interpolation: c[i]);
					(morphTime+ 0.1).wait; // For safety
				}
			}.quant_(0).play;
			^autoMorph;
		}
	}

	setMorphTime { |time = 5|
		fdBox.value = time;
		morphTime = time;
	}

	setMorphType { |type = \lin |
		currentMorphType = type;
	}


	lockParam { | param, locked = false |
		lockTest[param] = locked;
	}

}
