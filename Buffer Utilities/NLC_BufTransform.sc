/////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  -- Takes an input sample and performs transformations to it in the client side (arrays)                //
//  -- Returns the alterion back as a Buffer again. Input and alteration are kept                          //
//  -- Recognizes bewteen a single input buffer or an array of Buffers (to be used with DB_BufDivider      //
/////////////////////////////////////////////////////////////////////////////////////////////////////////////

//-------------------------------------------------------------------------------------[ Darien Brito, 2015 ]


NLC_BufTransform {
	var tempArray, bufferOut, collection;
	var <operation, <bufnum, <state;

	*new {|buffer,op,index|
		^super.new.init(buffer,op);
	}

	init {|tempArray_, operation_|
		tempArray = tempArray_;
		operation = operation_ ? 0; //Reverses by default
	}

	makeTransforms {
		if (class(tempArray) != Array) {
			this.mesher(tempArray);
		} { //If input is an array:
			var arrSize;
			arrSize = tempArray.size - 1;
			collection = Array.new(arrSize);

			fork {
				for(0, arrSize, {|i|
					this.arrayMesher(tempArray[i]);
					("buffer: "+i+" "+state).postln;
					(Server.default).sync;
					collection.add(bufferOut);
					if(i == arrSize) {"Done!".postln};

				});
				// Memory management
				////////////////////////////////////////
				tempArray.free;//Free the input buffer//
				////////////////////////////////////////
			};
		}
	}

	mesher {|bufArray|

		bufArray.loadToFloatArray(0,-1,{|array|
			var size;
			size = bufferOut.size - 1;

			bufferOut = array;
			bufferOut = bufferOut.as(Array);

			case
			{operation == 0} {bufferOut = bufferOut.reverse; state = "reversed"} //reverse it
			{operation == 1} {bufferOut = bufferOut.mirror1; state = "mirrored"} //mirror it
			{operation == 2} {bufferOut = bufferOut.rotate(rrand(1,size)); state = state = "rotated"}; //rotate it randomly

			bufferOut = Buffer.loadCollection(Server.default, bufferOut, action: {
				("buffer "++state).postln;
				// Memory management
				////////////////////////////////////////
				bufArray.free; //Free the input buffer//
				////////////////////////////////////////

			});
		})
	}

	arrayMesher {|bufArray|

		bufArray.loadToFloatArray(0,-1,{|array|
			var size;
			size = bufferOut.size - 1;

			bufferOut = array;
			bufferOut = bufferOut.as(Array);

			//operation = rrand(0,2); //Transforms at random
			operation = rrand(0,2); //Transforms at random

			case
			{operation == 0} {bufferOut = bufferOut.reverse; state = "reversed"} //reverse it
			{operation == 1} {bufferOut = bufferOut.mirror1; state = "mirrored"} //mirror it
			{operation == 2} {bufferOut = bufferOut.rotate(rrand(1,size)); state = state = "rotated"}; //rotate it randomly

			bufferOut = Buffer.loadCollection(Server.default, bufferOut, action: {
			});
		})
	}

	//Implement the array[0] trick

	getTransform{
		"Here you go: ".postln;
	^bufferOut;
	}

	getTransforms{
		"Here you go: ".postln;
		^collection;
	}

}