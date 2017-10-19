////////////////////////////////////////////////////////////////////////////////////
// Takes an array of Buffers and creates files allocated in the specified path    //
////////////////////////////////////////////////////////////////////////////////////

//------------------------------------------------------------[ Darien Brito, 2015 ]

NLC_BufExport {
	var bufList, pathString, fileFormat, bitRate;
	//debugging

	*new {|list, path, format, bitrate|
		^super.new.init(list, path, format, bitrate)
	}

	init {|bufList_, pathString_, fileFormat_,bitRate_|
		bufList = bufList_;
		pathString = pathString_;
		fileFormat = fileFormat_ ? 'AIFF';
		bitRate = bitRate_ ? 24;
	}

	export {
		var size, fileNameString, formatString, bitrateString, directory, finalPath, threadTime;
		var date;

		size = bufList.size - 1;
		fileNameString = "f_";

		//Getting seconds from current date to create unique folder names in the given path
		date = Date.getDate;
		date = ((date.bootSeconds).round(0.1));
		directory = "Files_"++date.asString++"/";

		case
		{fileFormat == 'AIFF'} {formatString = ".aiff"}
		{fileFormat == 'WAV'} {formatString = ".wav"};

		case
		{bitRate == 8  } {bitrateString = 'int8'}
		{bitRate == 16 } {bitrateString = 'int16'}
		{bitRate == 24  } {bitrateString = 'int24'}
		{bitRate == 32 } {bitrateString = 'int32'};

		finalPath = pathString++directory;
		finalPath.mkdir;

		fork{
			//To create a unique folder name based on how long SC has been running
			for (0, size, {|i|
				bufList[i].write(finalPath+fileNameString++i++formatString, fileFormat, bitrateString);
				(Server.default).sync;
				postln("writing: "+ i);
				if (i == size) {"Done!".postln};
			});
		}
	}
}
