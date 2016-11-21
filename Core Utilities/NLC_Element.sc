////////////////////////////////////////////////////////////////////////////////////////////////////
//
// NLC_Element
//
// * Wrapper for Element GUI types
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

NLC_Element {
	var <synth, <type, <name, <obj, <patternType;

	*new {|synth, type, name, patternType = \Pbind|
		^super.newCopyArgs(synth, type, name).init(patternType);
	}

	init {|patternType_|
		patternType = patternType_;
		switch(type)
		{\masks} { obj = NLC_MasksElement(synth, name, patternType) }
		{\m} { obj = NLC_MasksElement(synth, name, patternType) }
		{\sliders} { obj = NLC_SlidersElement(synth, name, patternType) }
		{\s} { obj = NLC_SlidersElement(synth, name, patternType) };
		^obj
	}

	makeGUI { |paramPairs, pos, skin = \black, master |
		obj.makeGUI(paramPairs, pos, onTop: false, skin: skin, master: master);
	}

}