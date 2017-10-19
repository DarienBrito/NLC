NLC_Env : Env {

	*stairs {|steps, level|
		var segs = Array.fill(steps,{|i| i });
		var segTimes = Array.fill(steps, {1/steps });
		segs = segs.mirror;
		segs = segs.normalize * level;
		segTimes = segTimes.mirror;
		^Env.step(segs,segTimes)
	}

	*sinHill {|steps, level|
		^this.stairsCurve(steps,level, \sin)
	}

	*snowHill {|steps, level|
		^this.stairsCurve(steps,level, \wel)
	}

	*stairsCurve { |steps, level, curve|
		var segs = Array.fill(steps,{|i| i });
		var segTimes = Array.fill(steps, {1/steps });
		segs = segs.mirror;
		segs = segs.normalize * level;
		segTimes = segTimes.mirror;
		^this.stepType(segs,segTimes, curve);
	}

	*stepType { |levels = #[0,1], times = #[1,1], curve, releaseNode, loopNode, offset = 0 |
		if( levels.size != times.size ) {
			Error("Env#*step : levels and times must have same size").throw
		};
		^Env([levels[0]]++levels, times, curve, releaseNode, loopNode, offset)
	}


}

NLC_Penv : Penv {

	*fadeIn { | fadeTo = 1, fadeTime = 1 |
		^Pseq( [ this.new( [0, fadeTo], [fadeTime] ), Pn(fadeTo, inf) ]);
	}

	*fadeOut { | fadeFrom = 1, fadeTime = 1 |
		^this.new([fadeFrom, 0], [fadeTime] );
	}

	*holdAndFade { |holdPoint = 1, holdTime = 1, fadeTime  = 1|
		^this.new( [holdPoint, holdPoint, 0] , [holdTime, fadeTime] );
	}

	*holdEnd { | levels, times |
		^Pseq([this.new(levels, times), Pn(levels.last,inf)]);
	}

	//Keep adding...

}

//
// \step -> 0,
// \lin -> 1,
// \linear -> 1,
// \exp -> 2,
// \exponential -> 2,
// \sin -> 3,
// \sine -> 3,
// \wel -> 4,
// \welch -> 4,
// \sqr -> 6,
// \squared -> 6,
// \cub -> 7,
// \cubed -> 7,
// \hold -> 8,