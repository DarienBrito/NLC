////////////////////////////////////////////////////////////////////////////////////////////////////
//
// NLC_SlidersElement
//
// * GUI for Elements of the NLC framework (Sliders)
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

NLC_SlidersElement : NLC_MetaElement {

	revert {| parameter |
		var targetLabel;
		labels.do{|label| if(label == parameter) {targetLabel = label }};
		if(targetLabel == nil) {"Parameter not found. See .params for available parameters".postln } {
			proxies[targetLabel].source = Pfunc({ current[parameter] });
		}
	}

	setProxies {
		paramRNames.do({ |name|
			var min = current[name][0];
			var max = current[name][1];
			proxies[name].source =  Pfunc({ min }) //defaults at min given value
		});
	}

	setAllStates {
		var setAllStates = ();
		paramRNames.do { |pName, i|
			setAllStates.put(pName,
				rrand(paramVals[i][0].round(0.001),paramVals[i][1].round(0.001))
			);
		};
		^setAllStates;
	}

	setFormattedData {
		var formattedMask = ();
		paramRNames.do{ |pName, i|
			formattedMask.put(pName,
				rrand(paramVals[i][0].round(0.001),paramVals[i][1].round(0.001));
			);
		};
		^formattedMask;
	}

	buildGui { |posPoint, onTop, skin, master = nil, numItem = nil|
		var skipjack;
		var mixText;
		var numberOfparameters = paramNames.size;
		var guiObjects = 7 + numberOfparameters;
		var presetsGridHeight = bHeight * 2;
		var gridHeight = guiObjects * bHeight;
		var styler, childView, childDecorator;
		var offset = 2 * bHeight;
		var masterStyler = nil;
		var headerName;
		guiHeight = gridHeight + presetsGridHeight + offset;

		if(numItem.isNil) {
			headerName = "[ " ++ elementName ++ " ]";
		} {
			headerName = "[ " ++ elementName ++"_% ]".format(numItem);
		};

		// Master Window
		if(master.isNil) {
			win = Window("NLC_" ++ elementName, Rect.fromPoints(posPoint ? 0@0, guiWidth@guiHeight));
			win.view.decorator = FlowLayout(win.view.bounds);
		} {
			masterStyler = GUIStyler(master, skin);
			win = masterStyler.getWindow("NLC_"++ elementName, guiWidth@guiHeight);
			win.decorator = FlowLayout(win.bounds);
		};

		if(onTop == true) { win.alwaysOnTop_(true) };

		// Child
		styler = GUIStyler(win, skin);
		childView = styler.getWindow("Nodes", win.bounds);
		childView.decorator = FlowLayout(childView.bounds);
		childDecorator = childView.decorator;

		//Adjust Color
		win.background_(styler.backgroundColor).alpha_(0.98);
		win.front;

		styler.getSubtitleText(childView, headerName, childDecorator);
		paramRNames.do { |name, i|
			ezControls.put(name,
				styler.getSizableEZSlider(childView, name,  380@sliderHeight, paramNames[i], \horz)
				.action_({|sl|
					current[name] = sl.value;
					if (name == \amp) {
						proxies[name].source = Pfunc({ current[name] * masterVol; });
					} {
						proxies[name].source = Pfunc({ current[name] });
					}
				})
				.valueAction_(current[name][0])
			);

			//Lock Buttons
			lockButtons.put(name,
				styler.getSizableButton(childView,\, \L, Rect(1,0,bWidth/4,20))
				.action = {|btn|
					if(btn.value == 1) {
						lockTest[name] = true;
						ezControls[name].setColors(knobColor: Color.red);
						(name++" locked").postln;
					} {
						lockTest[name] = false;
						ezControls[name].setColors(knobColor: Color.blue(0.3));
						(name++" unlocked").postln;
					}
				};
			)
		};

		childDecorator.nextLine;

		//Randomize
		randBtn = styler.getSizableButton(childView, \randomize, size: bWidth@bHeight)
		.action = {
			paramRNames.do{ |key|
				if (lockTest[key])
				{current[key] = current[key]}
				{current[key] = this.setFormattedData[key]}
			};
		};

		//Interpolate
		interBtn = styler.getSizableButton(childView, \interpolate, size: bWidth@bHeight)
		.action = {
			"morphing...".postln;
			target = this.setFormattedData;
			this.morphTask.stop.play;
		};

		//Morphing time
		styler.getSizableText(childView,"time", bWidth*0.5);
		fdBox = styler.getSizableNumberBox(childView, (bWidth*0.5)@bHeight, 0.1)
		.value_(5)
		.action = { |nbx|
			morphTime = nbx.value;
		};

		//Play/Stop
		playBtn = styler.getSizableButton(childView, \play, \stop, bWidth@bHeight)
		.action = { |btn|
			if(btn.value == 1)
			{ this.play }
			{ this.stop }
		};

		//Print
		styler.getSizableButton(childView, \print, size: (bWidth - 15)@bHeight)
		.action = (
			Routine({ var printCount = 0;
				inf.do{
					printCount = printCount + 1;
					"".postln;
					(current++";").postln;
					("Printed: "++ printCount).postln;
					("With synth: "++currentSynth).postln;
					"".postln;
					0.yield;
				}
			});
		);

		/*__________________

		Automatic functions
		____________________*/

		//AutoRandom
		autoRandBtn = styler.getSizableButton(childView, \autoRandom, \stopAutoRandom, bWidth@bHeight)
		.action = { |btn|
			if(btn.value == 1)
			{ this.autoRandom(morphN) }
			{ this.stopAutoRandom() }
		};

		//AutoMorph
		automorphBtn = styler.getSizableButton(childView, \autoMorph, \stopAutoMorph, size:(bWidth)@bHeight)
		.action = { |btn|
			if(btn.value == 1)
			{ this.autoMorphFunc(morphN) }
			{ this.stopMorphing() }
		};

		//N morphs
		styler.getSizableText(childView,"morphs", bWidth*0.5);
		nMorphsNumber = styler.getSizableNumberBox(childView, (bWidth*0.5)@bHeight, 0.1)
		.value_(0)
		.action = { |nbx|
			// 0 or negative number means infinite
			if (nbx.value > 0) {
				morphN = nbx.value;
			} { morphN = inf }
		};

		//Play states
		presetsBtn = styler.getSizableButton(childView, \states, \stopStates, size:(bWidth)@bHeight)
		.action = { |btn|
			if(btn.value == 1)
			{ this.morphMasksPresetsBy() }
			{ this.stopMorphing() }
		};

		// Set out bus
		styler.getSizableText(childView,"out", bWidth*0.3);
		styler.getSizableNumberBox(childView, (bWidth*0.45)@bHeight, 1)
		.value_(0)
		.action = { |nbx|
			outBus = nbx.value;
			this.setBus(outBus);
		};

		skipjack = SkipJack ({
			paramRNames.do { |name|
				ezControls[name].value = current[name];
			};
		}, updateVizTime, { win.isClosed }, name: "Updater");

		childDecorator.nextLine;

		setNames.do { |setname, i|
			var bWidth = 47;
			var bHeight = 20;
			var sliderWidth = 45;
			var sliderHeight = 8;

			var zone = GUI.compositeView.new(childView, Rect(0,0,bWidth, presetsGridHeight));
			zone.decorator = FlowLayout(zone.bounds, 0@0, 5@0);

			//statesEv
			styler.getSizableButton(zone, setname, size: Rect(0,0,bWidth,bHeight))
			.action = {
				"morphing...".postln;
				target = statesEv[setname];
				this.morphTask.stop.play;
			};

			//Save
			styler.getSizableButton(zone, "save" ++ (i+1), size: Rect(0,0,bWidth,bHeight))
			.action = {
				synthEv[setname] = currentSynth;
				statesEv[setname] = current.copy;
				this.setFormattedData;
				"saved".postln;
			};
		};

		childDecorator.nextLine;

		//Master fader
		styler.getSizableText(childView, "Mix", 30);
		mixText = styler.getSizableText(childView, "0.5", 37);
		styler.getSizableSlider(childView, 200@sliderHeight, \h)
		.action_({|sl|
			masterVol = sl.value;
			mixText.string_((sl.value.round(0.01)).asString);
		});

		// Pop-up menu for synth types
		styler.getSizableText(childView, "Synth", 40);
		styler.getPopUpMenu(childView, bWidth)
		.items_(synths)
		.action = { |pop|

			/*
			This condition is for the sake of Pmono types. Swapping of SynthName is not done with a PatternProxy
			(because it is not possible), so the only way to do it is to change it directly via a variable.
			The disadvantage of this is that if synth is changed, Pattern dur resets to when triggered,
			something maybe not desirable in very strict-tempo live-coding, but for our general purposes will do!
			*/

			if  ( class(pattern) == class(Pmono()) ) {
				synthName = pop.items[pop.value];
				currentSynth = pop.items[pop.value];
				this.specialStop.play; // This prevents interpolation from breaking
			} {
				this.setSynth(pop.items[pop.value]);
				currentSynth = pop.items[pop.value];
			}
		};

		// Stop after fade
		styler.getSizableButton(childView, \continous,\interrupt, (bWidth/1.45)@bHeight)
		.action = { |btn|
			stopAfterFade = (btn.value == 1);
		};

		// Visualization On/Off
		styler.getSizableButton(childView, \viz, \noViz, (bWidth*0.5)@bHeight)
		.action = { |btn|
			[ {skipjack.play }, {skipjack.stop } ] [btn.value].value
		};

		// Print All
		styler.getSizableButton(childView, \pAll, size: (bWidth*0.5)@bHeight)
		.action = { |btn|
			setNames.do({ |name|
				" ".postln;
				("statesEv["++$\\++name++"] = ").postln;
				(statesEv[name]).post;
				";".postln;
				//("With synth: "++synthEv[name] ? currentSynth).postln;
			});
		};

		// Print all formated
		styler.getSizableButton(childView, \params, size:(bWidth*0.5)@bHeight)
		.action = { |btn|
			this.params;
		};

		// Plot mapping
		styler.getSizableButton(childView, \plot, size:(bWidth*0.5)@bHeight)
		.action = { |btn|
			this.fillDataSets;
			this.makePlotter(currentMorphType);
		};

		// Curve
		styler.getSizableNumberBox(childView, (bWidth*0.5)@bHeight)
		.action = { |nbx|
			curve = nbx.value;
		};

		// Choose interpolation
		styler.getSizableText(childView, "Morph", 40);
		morphTypeBox = styler.getPopUpMenu(childView, bWidth)
		.items_(morphTypes)
		.action = { |pop|
			currentMorphType = pop.items[pop.value];
		};

		win.onClose_({ this.stop; paramRNames.do{|name| midiFuncs[name].free}});

	}

	checkMIDI{
		if(MIDIClient.sources.isNil, {MIDIIn.connectAll});
	}

	//Plug all cc's by array
	plugMIDIAll { | ccArray |
		this.checkMIDI;
		midiFuncs.do{|func| func.free }; //Replace array
		paramRNames.do{ |name, i|
			midiFuncs.put(name,
				MIDIFunc.cc({ | val, num |
					if( num == ccArray[i] ) {
						{ezControls[name].value = ezControls[name].controlSpec.map(val/127.0)}.defer;
						ezControls[name].doAction;
					};
				});
			);
		};
	}

	//Plug cc's by pairs of [\param,cc]
	plugPairedMIDI { | pairedCCs |
		if (pairedCCs.size.even) {
			this.checkMIDI;
			pairedCCs.pairsDo({ |name, cc|
				midiFuncs.put(name,
					MIDIFunc.cc({ | val, num |
						if( num == cc ) {
							{ezControls[name].value = ezControls[name].controlSpec.map(val/127.0)}.defer;
							ezControls[name].doAction
						};
					});
				);
			});
		} { "Array should have an even number of elements".warn;}
	}

	//Plug a single cc
	plugMIDI { |parameter, cc|
		this.checkMIDI;
		midiFuncs.put(parameter,
			MIDIFunc.cc({ |val, num|
				if(num == cc) {
					{ezControls[parameter].value = ezControls[parameter].controlSpec.map(val/127.0)}.defer;
					ezControls[parameter].doAction
				}
			});
		);
	}

}

