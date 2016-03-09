require "java"

def com
	Java::Com
end

#java_import "com.raidomatic.test.JavaInterface"

class HiFromRuby
	include_package "com.raidomatic.test"
	import "com.raidomatic.test.JavaInterface"
	include JavaInterface

	def get_hi
		return "hey from HiFromRuby"
	end
end

return HiFromRuby.new