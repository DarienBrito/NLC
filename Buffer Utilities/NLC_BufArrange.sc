/////////////////////////////////////////////////////////////////////////////////////////////
//  -- Takes an input array of Buffers and puts it back together into a single one         //
//  -- Returns a single buffer of the recombined material                                  //
/////////////////////////////////////////////////////////////////////////////////////////////

//---------------------------------------------------------------------[ Darien Brito, 2015 ]

NLC_BufArrange {
	var input, permutations;
	var collection;

	// Change the permutations argument to something more correct, like "arrangements"
	*new {|in, permutations|
		^super.new.init(in, permutations)
	}

	init { |input_, permutations_|
		input = input_;
		permutations = permutations_;
	}

	permutationsAmount {
		var limit = input.size;
		var multiplier = 1, out;
		for(limit, 1, {|i|
			out = i * multiplier;
			multiplier = out;
		});
		^out;
	}

	//This implementation is linear. Starts at permutation n and goes upFront...maybe is more interesing to retrieve
	//permutations at random... don't know how different than scramble would that be, but well, for now this is it.
	makePermutations {|start = 0|
		var end;
		end = start + permutations;
		collection = Array.new(permutations);
		fork{
			for(start, end,{|i|
				var temp;
				input = input.permute(i); //Perhaps is better just to use scramble here
				temp = NLC_BufCombine(input).makeCombination;
				(Server.default).sync;
				collection.add(temp.getCombination);
				("making permutation: " + i).postln;
				//if (i == (permutations - 1)) {input.free}; //If count reached, free the input buffer. Doesn't work...WHY!???
				if (i == (permutations - 1)) {"Done!".postln};
			});
		}
	}

	//Added this because scrambling leads to more interesting results
	makeVariations {
		collection = Array.new(permutations);
		fork{
			for(0, permutations - 1,{|i|
				var temp;
				input = input.scramble;
				temp = NLC_BufCombine(input).makeCombination;
				(Server.default).sync;
				collection.add(temp.getCombination);
				("making variation: " + i).postln;
				//if (i == (permutations - 1)) {input.free}; //If count reached, free the input buffer. Doesn't work...WHY!???
				if (i == (permutations - 1)) {"Done!".postln}; //If count reached, free the input buffer. Doesn't work...WHY!???
			});
		}
	}

	getArrangements {
		//FREE MEMORY////
		input.free;//////
		/////////////////
		//How to put this in the loop up so it's done after the making of all the buffers?
		^collection;
	}
}
