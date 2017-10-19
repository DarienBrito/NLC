////////////////////////////////////////////////////////////////////////////////////////////////////
//
// NLC_ElementsClones
//
// * High level control for Elements of the NLC framework which are identical
//
// NLC_ElementsContainer
//
// * High level control for Elements of the NLC framework which are different
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

NLC_ElementsClones {
	var <elements, <elementsParams, <name, skin;
	var moduleWidth, moduleHeight;
	var backgroundColor;
	var bWidth, bHeight;
	var <dOrientation;

	*new{|elements, elementsParams, name = "", skin = \black|
		^super.newCopyArgs(elements, elementsParams, name, skin).init();
	}

	setDefaults {  | defaultsArray | // an array of dictionaries
		elements.do{|e, i|
			e.setDefaults(defaultsArray[i]);
		}
	}

	init{
		var skinObj = GUISkins(skin);
		backgroundColor = skinObj.backgroundColor;
		skinObj.postln;
		backgroundColor.postln;
		this.computeModuleSize(elements[0].bWidth, elements[0].bHeight, elementsParams[0].size);
	}

	display { | orientation = \grid, dimension = 800, gridCols = 2, gridRows = 2 |
		dOrientation = orientation;
		switch(orientation)
		{\vertical} { this.createMaster(backgroundColor, (moduleWidth + 25)@dimension) }
		{\grid} { this.createMaster(backgroundColor, ((moduleWidth * gridCols) + 15)@((moduleHeight * gridRows) + 140))};
	}

	computeModuleSize { |bWidth_, bHeight_, numberOfParameters|
		var guiObjects = 7 + numberOfParameters;
		var presetsGridHeight = bHeight_ * 2;
		var gridHeight = guiObjects * bHeight_;
		var styler, childView, childDecorator;
		moduleWidth = elements[0].guiWidth;
		moduleHeight = gridHeight;
		bWidth = bWidth_;
		bHeight = bHeight_;
	}

	createMaster {|backgroundColor, bounds, macroPosMult = 6|
		var masterWindow = Window("NLC_" ++ name, bounds, scroll: true);
		var decorator, styler, macroStyler, macroWindow, macroDecorator;
		var macroBtnWidth = bWidth;
		var macroBtnHeight = bHeight;
		var macroWindowWidth = macroBtnWidth * 4.25;
		var macroWindowHeight = macroBtnHeight * macroPosMult + 15;
		var maxBtnWidth = macroBtnWidth * 2;
		var macrosLabels1state = [
			"Randomize", "Interpolate", "RandomTimes", "RandomMorphs"
		];
		var macrosLabels2states = [
			["PlayAll", "PlayNone"],
			["AutoRand", "RandNone"],
			["AutoMorph","MorphNone"],
			["AutoPresets", "PresetsNone"]
		];
		var macrosRow1 = List(), macrosRow2 = List(), actions;
		var logoWindow, logo;
		masterWindow.view.decorator = FlowLayout(masterWindow.view.bounds, gap: 0@5);
		decorator = masterWindow.view.decorator;
		masterWindow.background = backgroundColor;

		// Macro controls label
		styler = GUIStyler(masterWindow, skin);
		styler.getSubtitleText(masterWindow, "Macro controls", decorator);
		masterWindow.alpha = styler.alpha;

		if(dOrientation != \vertical) {
			logoWindow = styler.getWindow("Logo", 180@75)
			.moveTo(masterWindow.view.bounds.width * 0.75, macroBtnHeight);
			logoWindow.decorator = FlowLayout(logoWindow.bounds);
			styler.getSizableText(logoWindow, "NLC", macroBtnWidth, \left, fontSize: 20);
			styler.drawLine(logoWindow,(logoWindow.bounds.width)@1, 0@0, logoWindow.bounds.width@0, Color.white);
			styler.getSizableText(logoWindow, "A framework for generative exploration", macroBtnWidth * 4, \left, fontSize: 10);
		};

		// Macro window
		macroWindow = styler.getWindow("Macros", macroWindowWidth@macroWindowHeight)
		.moveTo(masterWindow.view.bounds.width * 0.5 - (macroWindowWidth*0.5), macroBtnHeight);
		macroWindow.decorator = FlowLayout(macroWindow.bounds);
		4.do{|i| macrosRow1.add(
			styler.getSizableButton(macroWindow, macrosLabels1state[i], size: macroBtnWidth@macroBtnHeight)
			.maxHeight_(macroBtnHeight).minHeight_(macroBtnHeight).minWidth_(macroBtnWidth).maxWidth_(maxBtnWidth));
		};
		4.do{|i| macrosRow2.add(
			styler.getSizableButton(macroWindow, macrosLabels2states[i][0], macrosLabels2states[i][1], macroBtnWidth@macroBtnHeight)
			.maxHeight_(macroBtnHeight).minHeight_(macroBtnHeight).minWidth_(macroBtnWidth).maxWidth_(maxBtnWidth));
		};
		actions = [
			{|btn| elements.do{|element| element.randBtn.valueAction = 1 }},
			{|btn| elements.do{|element| element.interBtn.valueAction = 1 }},
			{|btn| elements.do{|element| element.setMorphTime(rrand(0.1, 10)) }},
			{|num| elements.do{|element| element.nMorphsNumber.value = rrand(1, 10) }},

			{|btn| if(btn.value > 0) { elements.do{|element| element.playBtn.valueAction = 1 }}
				{ elements.do{|element| element.playBtn.valueAction = 0}}},

			{|btn| if(btn.value > 0) { elements.do{|element| element.autoRandBtn.valueAction = 1 }}
				{ elements.do{|element| element.autoRandBtn.valueAction = 0}}},

			{|btn| if(btn.value > 0) { elements.do{|element| element.automorphBtn.valueAction = 1 }}
				{ elements.do{|element| element.automorphBtn.valueAction = 0}}},

			{|btn| if(btn.value > 0) { elements.do{|element| element.presetsBtn.valueAction = 1 }}
				{elements.do{|element| element.presetsBtn.valueAction = 0}}}
		];
		(macrosRow1 ++ macrosRow2).do{|button, i| button.action_(actions[i])};

		// Extra functionality
		styler.getSizableText(macroWindow, "Set buses to ->", macroBtnWidth);
		styler.getSizableNumberBox(macroWindow, macroBtnWidth@macroBtnHeight)
		.action = {|box| elements.do{|element| element.setBus(box.value) }};

		styler.getSizableText(macroWindow, "Set all times to -> ", macroBtnWidth);
		styler.getSizableNumberBox(macroWindow, macroBtnWidth@macroBtnHeight)
		.action = {|box| elements.do{|element| element.setMorphTime(box.value) }};


		styler.getSizableText(macroWindow, "Lock all ------->", macroBtnWidth);
		(elementsParams[0].size * 0.5).do{|i|
			styler.getCheckBox(macroWindow, "")
			.action = {|box|
				elements.do{|element| element.lockButtons[element.paramRNames[i]].postln; };
				elements.do{|element| element.lockButtons[element.paramRNames[i]].valueAction = box.value };
			}
		};

		macroWindow.decorator.nextLine;
		styler.getSizableText(macroWindow, "Set curves to ->", macroBtnWidth);
		styler.getPopUpMenu(macroWindow, macroBtnWidth)
		.items = elements[0].morphTypes;////////
		styler.getSizableButton(macroWindow, "RandomCurves", size: macroBtnWidth@macroBtnHeight);
		styler.getSizableButton(macroWindow, "Cont", "NonCont", size: (macroBtnWidth *0.45)@macroBtnHeight);
		styler.getSizableButton(macroWindow, "Viz", "NoViz", size: (macroBtnWidth *0.45)@macroBtnHeight);

		styler.drawLine(masterWindow, masterWindow.view.bounds.width@1, 0@0, masterWindow.view.bounds.width@0, Color.white);
		decorator.nextLine;



		elements.do{|element, i|
			element.makeGUI(elementsParams[i], 0@0, false, skin, masterWindow, i);
		};
		masterWindow.front;
	}

}

NLC_ElementsContainer : NLC_ElementsClones {

	createMaster {|backgroundColor, bounds, macroPosMult = 6|
		var masterWindow = Window("NLC_" ++ name, bounds, scroll: true);
		var decorator, styler, macroStyler, macroWindow, macroDecorator;
		var macroBtnWidth = bWidth;
		var macroBtnHeight = bHeight;
		var macroWindowWidth = macroBtnWidth * 4.25;
		var macroWindowHeight = macroBtnHeight * macroPosMult + 15;
		var maxBtnWidth = macroBtnWidth * 2;
		var macrosLabels1state = [
			"Randomize", "Interpolate", "RandomTimes", "RandomMorphs"
		];
		var macrosLabels2states = [
			["PlayAll", "PlayNone"],
			["AutoRand", "RandNone"],
			["AutoMorph","MorphNone"],
			["AutoPresets", "PresetsNone"]
		];
		var macrosRow1 = List(), macrosRow2 = List(), actions;
		var logoWindow, logo;
		masterWindow.view.decorator = FlowLayout(masterWindow.view.bounds, gap: 0@5);
		decorator = masterWindow.view.decorator;
		masterWindow.background = backgroundColor;

		// Macro controls label
		styler = GUIStyler(masterWindow, skin);
		styler.getSubtitleText(masterWindow, "Macro controls", decorator);
		masterWindow.alpha = styler.alpha;

		if(dOrientation != \vertical) {
			logoWindow = styler.getWindow("Logo", 180@75)
			.moveTo(masterWindow.view.bounds.width * 0.75, macroBtnHeight);
			logoWindow.decorator = FlowLayout(logoWindow.bounds);
			styler.getSizableText(logoWindow, "NLC", macroBtnWidth, \left, fontSize: 20);
			styler.drawLine(logoWindow,(logoWindow.bounds.width)@1, 0@0, logoWindow.bounds.width@0, Color.white);
			styler.getSizableText(logoWindow, "A framework for generative exploration", macroBtnWidth * 4, \left, fontSize: 10);
		};

		// Macro window
		macroWindow = styler.getWindow("Macros", macroWindowWidth@macroWindowHeight)
		.moveTo(masterWindow.view.bounds.width * 0.5 - (macroWindowWidth*0.5), macroBtnHeight);
		macroWindow.decorator = FlowLayout(macroWindow.bounds);
		4.do{|i| macrosRow1.add(
			styler.getSizableButton(macroWindow, macrosLabels1state[i], size: macroBtnWidth@macroBtnHeight)
			.maxHeight_(macroBtnHeight).minHeight_(macroBtnHeight).minWidth_(macroBtnWidth).maxWidth_(maxBtnWidth));
		};
		4.do{|i| macrosRow2.add(
			styler.getSizableButton(macroWindow, macrosLabels2states[i][0], macrosLabels2states[i][1], macroBtnWidth@macroBtnHeight)
			.maxHeight_(macroBtnHeight).minHeight_(macroBtnHeight).minWidth_(macroBtnWidth).maxWidth_(maxBtnWidth));
		};
		actions = [
			{|btn| elements.do{|element| element.randBtn.valueAction = 1 }},
			{|btn| elements.do{|element| element.interBtn.valueAction = 1 }},
			{|btn| elements.do{|element| element.setMorphTime(rrand(0.1, 10)) }},
			{|num| elements.do{|element| element.nMorphsNumber.value = rrand(1, 10) }},

			{|btn| if(btn.value > 0) { elements.do{|element| element.playBtn.valueAction = 1 }}
				{ elements.do{|element| element.playBtn.valueAction = 0}}},

			{|btn| if(btn.value > 0) { elements.do{|element| element.autoRandBtn.valueAction = 1 }}
				{ elements.do{|element| element.autoRandBtn.valueAction = 0}}},

			{|btn| if(btn.value > 0) { elements.do{|element| element.automorphBtn.valueAction = 1 }}
				{ elements.do{|element| element.automorphBtn.valueAction = 0}}},

			{|btn| if(btn.value > 0) { elements.do{|element| element.presetsBtn.valueAction = 1 }}
				{elements.do{|element| element.presetsBtn.valueAction = 0}}}
		];
		(macrosRow1 ++ macrosRow2).do{|button, i| button.action_(actions[i])};

		// Extra functionality
		styler.getSizableText(macroWindow, "Set buses to ->", macroBtnWidth);
		styler.getSizableNumberBox(macroWindow, macroBtnWidth@macroBtnHeight)
		.action = {|box| elements.do{|element| element.setBus(box.value) }};

		styler.getSizableText(macroWindow, "Set all times to -> ", macroBtnWidth);
		styler.getSizableNumberBox(macroWindow, macroBtnWidth@macroBtnHeight)
		.action = {|box| elements.do{|element| element.setMorphTime(box.value) }};

		styler.getSizableText(macroWindow, "Set curves to ->", macroBtnWidth);
		styler.getPopUpMenu(macroWindow, macroBtnWidth)
		.items = elements[0].morphTypes;////////
		styler.getSizableButton(macroWindow, "RandomCurves", size: macroBtnWidth@macroBtnHeight);
		styler.getSizableButton(macroWindow, "Cont", "NonCont", size: (macroBtnWidth *0.45)@macroBtnHeight);
		styler.getSizableButton(macroWindow, "Viz", "NoViz", size: (macroBtnWidth *0.45)@macroBtnHeight);

		styler.drawLine(masterWindow, masterWindow.view.bounds.width@1, 0@0, masterWindow.view.bounds.width@0, Color.white);

		elements.do{|element, i|
			element.makeGUI(elementsParams[i], 0@0, false, skin, masterWindow, i);
		};
		masterWindow.front;
	}

}
