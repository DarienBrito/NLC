////////////////////////////////////////////////////////////////////////////////////////////////////
//
// NLC_BaseElement
//
// * Abstract class for Elements of the NLC framework
// It handles administration of Patterns and proxies
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

NLC_BaseElement {
	var synths, <synthName, labels, parameters;
	var type, proxies, outBus;
	var morphTime, morphN, stepsPerSec, curve, stopAfterFade;
	var dataSet, <morphTypes, currentMorphType;
	var timeProxy, oscProxy, outProxy, synthProxy;
	var proxyList, paramList, isPlaying;
	var <pattern, <playBtn;
	var isPlaying, instrumentName;
	var <synth;

/*	*new {|synth|
		^super.new.init(synth);
	}

	init {|synth_|
		synth = synth_;
		this.invokeData(synth);
	}*/

	invokeData {|synth, patternType = \Pbind|
		this.initData(synth);
		this.makeParamList();
		this.makePattern(patternType);
		this.fillDataSets();
	}

	initData {|synth|
		// Add here controls that are wanted by default
		var innerLabels = [\dur];
		var innerParameters = [1];
		var argumentNames, synthParameters, template;
		var removalIndex;
		synth.postln;
		// Check if input is array of synths
		if(synth.size > 0) {
			template = synth[0];
			synths = synth.collect{|synth| synth.name };
		} {
			template = synth;
			synths = nil;
		};
		
		argumentNames = template.desc.controlNames;
		// Check  if synth has been properly initiated with an out parameter
		if(argumentNames.includes(\out).not){
			protect { 666.kaboom } {|error|
				var lines = 90.collect{"-"}.toString;
				"Your SynthDef must have an out parameter!".warn;
				Error("You passed a SynthDef without an Out argument\n" ++ lines ++"\n----> SuperCollider's wrath is upon you!!!").throw;
			}
		};

		labels = List();
		synthName = template.name;
		// Filter \out from arguments (there's and ad hoc method for it)
		argumentNames.do{|argument, i| if(argument != \out) {labels.add(argument)} {removalIndex = i}};
		synthParameters = (template.controls).copy;
		synthParameters.removeAt(removalIndex);
		labels = labels ++ innerLabels;
		parameters =  synthParameters ++ innerParameters;
		outBus = 0;
		type = 0;
		proxies = ();
		morphTime = 5;
		morphN = inf;
		stepsPerSec = 20;
		curve = 4;
		stopAfterFade = false;
		dataSet = ();
		morphTypes = [
			\lin, \exp, \explin,\lincurve,\curvelin, \ramp,
			\bilin,\biexp,\lcurve,\scurve,\gausscurve,\custom
		];
		currentMorphType = morphTypes[0];
		isPlaying = false;
	}

	makeParamList {
		var proxiesContent;
		timeProxy = PatternProxy(1);
		oscProxy = PatternProxy(nil);
		outProxy = PatternProxy(0);
		synthProxy = PatternProxy(synthName);
		parameters.do({ |parameter, i| proxies.put(labels[i],PatternProxy(parameter))});
		proxiesContent = labels.collect{|label| proxies[label] };
		proxyList = [labels, proxiesContent];
		paramList = [labels, parameters];
		isPlaying = false;
	}

	makePattern { | patternType = \Pbind |
		var pairedParams = proxyList.lace;
		var auxProxies = [\stretch, timeProxy, \osc, oscProxy, \out, outProxy];
		//Concatenation of parameters that do not need to be in the arguments of the instrument
		if(patternType != \Pmono) {
			pairedParams = [\instrument, synthProxy] ++ pairedParams ++ auxProxies;
			pattern = Pbind(*pairedParams)
		} {
			pairedParams = [synthName] ++ pairedParams ++ auxProxies;
			pattern = Pmono(*pairedParams)
		}
	}

	getGuiParams {
		var pairs = [labels, parameters].lace;
		var formatedPairs = Array(pairs.size);
		pairs.pairsDo{|label, val, i|
			var test;
			formatedPairs.add($\\++label);
			test = switch(label)
			{\freq} {[20, 20000,\exp]}
			{\amp} {[0.0, 1]}
			{\pan} {[-1,1]}
			{\dur} {[0.01, 1]}
			{[1, val]};
			formatedPairs.add(test);
		}
		^formatedPairs;
	}

	params {
		var pairedParams = paramList.flop;
		"Current Parameters: ".postln;
		"".postln;
		pairedParams.do({ |i| (Char.tab++"- "++i).postln });
		"".postln;
	}

	change { | ...args |
		args.postln;
		args.pairsDo({ |tag, val|
			var pair;
			[tag, val].postln;
			pair = [tag,val];
			labels.do({ |label, i|
				if (pair[0] == label) { proxies[label].source = val; parameters[i] = val }
			})
		});
		paramList = [labels, parameters]; //Update;
	}

	debugger {
		postln("SynthName: "++ synthName);
		postln("Proxies: "++ proxies);
		postln("Parameters: "++ parameters);
		postln("Labels: "++ labels);
		postln("ParamList: "++ paramList);
		postln("ProxyList: "++ proxyList);
		postln("SynthList: "++ synths);
	}

	play {
		isPlaying = true;
		this.makePattern();
		pattern = pattern.play;
	}

	stop {
		playBtn.value = 0;
		isPlaying = false;
		pattern.stop;
		pattern.free;
		this.stopMorphing();
	}

	//Needed for Pmono types continuity
	specialStop {
		isPlaying = false;
		pattern.stop;
		pattern.free;
	}

	setStretch { | val |
		timeProxy.source = val;
	}

	setFunc { | pfunc |
		oscProxy.source = pfunc;
	}

	setBus { | outBus |
		outProxy.source = outBus;
	}

	setSynth { |synthName|
		synthProxy.source = synthName;
	}

	fillDataSets {
		var numSteps = morphTime * stepsPerSec;
		var mapArray =  Pseries(1/numSteps,1/stepsPerSec, numSteps).asStream.nextN(numSteps);
		var mapLo =  1 / numSteps;
		var expLo = 0.0001;
		var rampData = mapArray.normalize(-1 , 1);
		var curveLData = mapArray.normalize(-10 , 10);
		var normalizedData = mapArray.normalize();
		var gaussData =  mapArray.normalize(curve * -1, curve);
		var centerA = 1.0.rand;
		var centerB = 1.0.rand;

		dataSet[\lin] = { mapArray.linlin(mapLo, morphTime, 0.0, 1) }.value;
		dataSet[\exp] = { mapArray.linexp(mapLo, morphTime, expLo, 1)}.value;
		dataSet[\explin] = { mapArray.explin(mapLo, morphTime,0.0,1) }.value;
		dataSet[\lincurve] = { mapArray.lincurve(mapLo, morphTime,expLo,1, curve) }.value;
		dataSet[\curvelin] = { mapArray.curvelin(mapLo, morphTime,0.0,1, curve) }.value;
		dataSet[\ramp] = { rampData.collect { |num| num.ramp } }.value;
		dataSet[\bilin] = { mapArray.bilin(centerA,mapLo, morphTime,centerB, 0.0, 1)}.value;
		dataSet[\biexp] = { mapArray.biexp(centerA,expLo, morphTime,centerB, expLo, 1.0) }.value;
		dataSet[\lcurve] = { curveLData.collect{|num| num.lcurve } }.value;
		dataSet[\scurve] = { normalizedData.collect {|num| num.scurve } }.value;
		dataSet[\gausscurve] = { gaussData.collect{ |num| num.gaussCurve } }.value;
		dataSet[\custom] = { mapArray.linlin(mapLo, morphTime, 0.0, 1)  }.value;
	}

	// Include a plotter in the GUIStyler
	makePlotter { |type|
		var plotter;
		var originY = Window.screenBounds.height - 20;
		if(type.isNil) {
			"You must specify a type. Available types are:".warn;
			morphTypes.do{|type| type.postln };
		} {
			plotter = Plotter("Mapping: "++type,
				bounds: Rect(0, originY - 368,415, 300))
			.plotMode_(\plines)
			.value_(dataSet[type].copy); // Copy (non-destructive)
			plotter.setProperties(
				\plotColor, (10..0).normalize(0.1, 1).collect { |i| Color.rand(i) },
				\backgroundColor, Color.black ,
				\gridColorX, Color.yellow(0.5),
				\gridColorY, Color.yellow(0.5),
				\fontColor, Color(0.5, 1, 0),
			)
			.editMode_(true)
			.editFunc_({
				|plotter|
				dataSet[type] = plotter.value; //Update
			});
		}
	}

	makeAllPlots {
		var plotter;
		var originY = Window.screenBounds.height - 20;
		plotter = Plotter("All mappings",
			bounds: Rect(0,originY - 368,415, 300))
		.plotMode_(\linear)
		.value_( morphTypes.collect{|type| dataSet[type].copy });
		plotter.setProperties(
			\plotColor, (10..0).normalize(0.1, 1).collect { |i| Color.rand(i) },
			\backgroundColor, Color.black ,
			\gridColorX, Color.yellow(0.5),
			\gridColorY, Color.yellow(0.5),
			\fontColor, Color(0.5, 1, 0),
			\labelX, "STEP",
			\labelY, "VAL"
		);
		plotter.superpose = true;
	}

}
