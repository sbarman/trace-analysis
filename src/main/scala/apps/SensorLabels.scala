package apps

import scala.io.Source

class SensorTokens(val reading: String, val tokens: String) {
  val tokenList = tokens.split(",").map(token => token.split(":"))
  
  var tokenStarts = new Array[Int](tokenList.size)
  var readingIndex = 0
  for ((token, i) <- tokenList.zipWithIndex) {
    val string = token(1)
    val index = reading.indexOfSlice(string, readingIndex);
    tokenStarts(i) = index
    
    readingIndex = index + string.size
  }
  val tokenStartsList = tokenStarts.toList
  
  def matchPartition(other: SensorTokens): Boolean = {
    return tokenStartsList == other.tokenStartsList 
  }
}

class SensorLabels(fileName: String) {
  val input = Source.fromFile(fileName).getLines.grouped(2)
  val tokens = input.map(lines => new SensorTokens(lines(0), lines(1)))
  val mapping = tokens.map(token => (token.reading, token)).toMap
}