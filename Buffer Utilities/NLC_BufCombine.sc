/////////////////////////////////////////////////////////////////////////////////////////////
//  -- Takes an input array of Buffers and puts it back together into a single one         //
//  -- Returns a single buffer of the recombined material                                  //
/////////////////////////////////////////////////////////////////////////////////////////////

//---------------------------------------------------------------------[ Darien Brito, 2015 ]

NLC_BufCombine {
	var input, final;

	*new {|in|
		^super.new.init(in)
	}

	init {|input_|
		input = input_;
	}

	makeCombination {
		var sum = 0, frames = 0, start = 0, end;
		var arrSize;

		arrSize = input.size - 1;

		for (0, input.size - 1, {|i|
			sum = input[i].numFrames + frames;
			frames = sum;
		});

		final = Buffer.alloc(Server.local, frames, input[0].numChannels); //Allocate a Buffer with the right length
		end = input[0].numFrames; //We use this to know the first frame number and use it for our first iteration

		fork {
			// We decrease the index limit so it's within bounds in the length calculation, otherwise frame count is incorrectly
			// at + n frames due to the variable "end" starting at a given value non equal to 0
			for (0, input.size - 2, {|n|
				input[n].copyData(final, dstStartAt: start, srcStartAt: 0, numSamples: end);
				(Server.default).sync;
				start = start + input[n].numFrames;
				end = end + input[n].numFrames;
			});
			//in.free;
		};
	}

	//If is a single buffer, return the first element in the array, otherwise, return the whole as an array
	getCombination {
		^final
	}

}

