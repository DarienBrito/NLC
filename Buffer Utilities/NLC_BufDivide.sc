/////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  -- Takes an input sample and subdivides its frames in n-number of subsidiary buffers                   //
//  -- Returns the array of subsidiary buffers and frees the original input (encourages experimentation)   //
/////////////////////////////////////////////////////////////////////////////////////////////////////////////

//-------------------------------------------------------------------------------------[ Darien Brito, 2015 ]

NLC_BufDivide {
	var sample, subdivisions, framesNum, chunksSize, chunks, numChans;

	*new{|sample, subdivisions|
		^super.new.init(sample, subdivisions);
	}

	init {|sample_,subdivisions_|
		sample = sample_;
		subdivisions = subdivisions_ ? 2;
		numChans = sample.numChannels;
		^this.makeDivisions;
	}

	fill {
		var server = Server.default;
		framesNum = sample.numFrames;
		chunksSize = framesNum/subdivisions;

		if (numChans < 2)
		//Mono
		{ chunks = subdivisions.collect({Buffer.alloc(server,chunksSize,1)}) }
		//Stereo
		{ chunks = subdivisions.collect({Buffer.alloc(server,chunksSize,2)}) };

	}

	makeDivisions {
		var start = 0, end;
		this.fill;
		end = chunksSize;
		for (0, chunks.size - 1, {|i|
			chunks[i].zero;
			sample.copyData(chunks[i],dstStartAt:0,srcStartAt:start,numSamples:end);
			start = start + chunksSize;
			end = end + chunksSize;
		});
		//sample.free; Keep the original sample intact (has proven to be more useful than freeing it)
		^chunks;
	}
}

